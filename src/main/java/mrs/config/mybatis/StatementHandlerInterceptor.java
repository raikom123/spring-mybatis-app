package mrs.config.mybatis;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.ibatis.executor.statement.BaseStatementHandler;
import org.apache.ibatis.executor.statement.PreparedStatementHandler;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.Reflector;

import lombok.extern.slf4j.Slf4j;
import mrs.domain.model.MyBatisSelectiveNullValues;

@Intercepts({
		@Signature(type = StatementHandler.class, method = "update", args = { Statement.class }),
})
@Slf4j
public class StatementHandlerInterceptor implements Interceptor {

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		if (!(invocation.getArgs()[0] instanceof PreparedStatement)) {
			return invocation.proceed();
		}

		Optional<PreparedStatement> preparedStatement = getPreparedStatement(invocation);
		if (preparedStatement.isEmpty()) {
			return invocation.proceed();
		}

		Optional<MappedStatement> mappedStatement = getMappedStatement(invocation);
		if (mappedStatement.isEmpty()) {
			return invocation.proceed();
		}

		PreparedStatement ps = preparedStatement.get();
		MappedStatement ms = mappedStatement.get();

		if (!isInterceptTarget(ms.getId())) {
			return invocation.proceed();
		}

		var handler = (StatementHandler) invocation.getTarget();
		var conf = ms.getConfiguration();
		getResultMap(conf.getResultMapNames(), ms.getId()).ifPresent((id) -> {
			var resultMap = conf.getResultMap(id);
			var sql = handler.getBoundSql();
			getColumnParamIndexMap(ms.getSqlCommandType(), sql.getSql()).ifPresent(columnParamIndexMap -> {
				var parameter = sql.getParameterObject();
				var ref = conf.getReflectorFactory().findForClass(parameter.getClass());
				updateNullValues(ps, ref, parameter, columnParamIndexMap, resultMap);
			});
		});

		return invocation.proceed();
	}

	Optional<PreparedStatement> getPreparedStatement(Invocation invocation) {
		if (invocation.getArgs()[0] instanceof PreparedStatement) {
			return Optional.of((PreparedStatement) invocation.getArgs()[0]);
		}

		return Optional.empty();
	}

	Optional<MappedStatement> getMappedStatement(Invocation invocation) {
		try {
			StatementHandler handler = (StatementHandler) invocation.getTarget();

			Field field = RoutingStatementHandler.class.getDeclaredField("delegate");
			field.setAccessible(true);
			PreparedStatementHandler psHandler = (PreparedStatementHandler) field.get(handler);

			Field msField = BaseStatementHandler.class.getDeclaredField("mappedStatement");
			msField.setAccessible(true);

			MappedStatement ms = (MappedStatement) msField.get(psHandler);

			return Optional.of(ms);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			log.warn("Cannot access MappedStatement.", e);
			return Optional.empty();
		}
	}

	public boolean isInterceptTarget(String id) {
		String methodName = id.substring(id.lastIndexOf(".") + 1);
		return List.of("insertSelective", "updateByExampleSelective", "updateByPrimaryKeySelective")
				.contains(methodName);
	}

	public Optional<String> getResultMap(Collection<String> resultMapNameList, String id) {
		return resultMapNameList.stream()
				.filter((resultMapName) -> resultMapName.startsWith(id.substring(0, id.lastIndexOf("."))))
				.findFirst();
	}

	public void updateNullValues(PreparedStatement ps, Reflector ref, Object parameter,
			Map<String, List<Integer>> columnParamIndexMap, ResultMap resultMap) {
		Map<String, String> propColumnMap = resultMap.getResultMappings().stream()
				.collect(Collectors.toMap(ResultMapping::getProperty, ResultMapping::getColumn));
		Map<String, ResultMapping> propMappingMap = resultMap.getResultMappings().stream()
				.collect(Collectors.toMap(ResultMapping::getProperty, (mapping) -> mapping));

		Stream.of(ref.getGetablePropertyNames()).filter(propColumnMap::containsKey).forEach(propertyName -> {
			try {
				var getInvoker = ref.getGetInvoker(propertyName);
				var obj = getInvoker.invoke(parameter, null);
				if (!MyBatisSelectiveNullValues.isNullValue(obj)) {
					return;
				}

				var columnName = propColumnMap.get(propertyName);
				var resultMapping = propMappingMap.get(propertyName);
				for (Integer parameterIndex : columnParamIndexMap.get(columnName)) {
					ps.setNull(parameterIndex, resultMapping.getJdbcType().TYPE_CODE);
				}
			} catch (IllegalAccessException | InvocationTargetException | SQLException e) {
				log.warn("Cannot update parameter value to null. propertyName: " + propertyName, e);
			}
		});
	}

	public Optional<Map<String, List<Integer>>> getColumnParamIndexMap(SqlCommandType sqlCommandType,
			String sql) {
		switch (sqlCommandType) {
		case INSERT:
			return Optional.ofNullable(getColumnParamIndexMapForInsert(sql));
		case UPDATE:
			return Optional.ofNullable(getColumnParamIndexMapForUpdate(sql));
		default:
			return Optional.empty();
		}
	}

	public Map<String, List<Integer>> getColumnParamIndexMapForInsert(String sql) {
		var evaluteSql = sql.toLowerCase();
		var columnList = Stream.of(evaluteSql.substring(evaluteSql.indexOf("(") + 1, evaluteSql.indexOf(")")).split(",")).map(String::trim)
				.collect(Collectors.toList());
		var paramList = Stream.of(evaluteSql.substring(evaluteSql.lastIndexOf("(") + 1, evaluteSql.lastIndexOf(")")).split(","))
				.map(String::trim).collect(Collectors.toList());
		Stream.iterate(0, (i) -> i + 1).limit(paramList.size()).filter((i) -> !paramList.get(i).equals("?")).sorted(Comparator.reverseOrder()).forEach((i) -> {
			columnList.remove(columnList.get(i));
			paramList.remove(paramList.get(i));
		});
		return getMapByColumnAndParam(columnList, paramList);
	}

	public Map<String, List<Integer>> getColumnParamIndexMapForUpdate(String sql) {
		var evaluteSql = sql.toLowerCase();
		List<String> columnParamList = Stream.concat(
				Stream.of(evaluteSql.substring(evaluteSql.indexOf("set") + 3, evaluteSql.indexOf("where")).split(",")).map(String::trim),
				Stream.of(evaluteSql.substring(evaluteSql.lastIndexOf("where") + 5).split("and")).map(String::trim))
				.filter((str) -> str.indexOf("?") > 0)
				.collect(Collectors.toList());
		List<String> columnList = columnParamList.stream().map((str) -> str.split("=")[0].trim()).collect(Collectors.toList());
		List<String> paramList = columnParamList.stream().map((str) -> str.split("=")[1].trim()).collect(Collectors.toList());
		return getMapByColumnAndParam(columnList, paramList);
	}

	public Map<String, List<Integer>> getMapByColumnAndParam(List<String> columnList, List<String> paramList) {
		return Stream.iterate(0, (i) -> i + 1)
				.limit(columnList.size())
				.filter((i) -> Objects.equals("?", paramList.get(i)))
				.collect(Collectors.toMap(columnList::get, (i) -> List.of(i + 1), (list1, list2) -> {
					return Stream.concat(list1.stream(), list2.stream()).collect(Collectors.toList());
				}));
	}

}
