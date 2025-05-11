package mrs.config.mybatis;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Stream;

import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.Reflector;
import org.apache.ibatis.reflection.invoker.Invoker;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.IntegerTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.LocalDateTypeHandler;
import org.apache.ibatis.type.LocalTimeTypeHandler;
import org.apache.ibatis.type.StringTypeHandler;
import org.junit.jupiter.api.Test;

import mrs.domain.model.MyBatisSelectiveNullValues;

class StatementHandlerInterceptorTests {

    @Test
    void testIntercept() {

    }

    @Test
    void getPreparedStatement() {

    }

    @Test
    void getMappedStatement() {

    }

    @Test
    void isInterceptTarget() {
        var statementHandlerInterceptor = new StatementHandlerInterceptor();
        Stream.of("insertSelective", "updateByExampleSelective", "updateByPrimaryKeySelective")
                .forEach((methodName) -> {
                    StringJoiner id = new StringJoiner(".");
                    id.add("aaa");
                    id.add("bbb");
                    id.add("CccClass");
                    id.add(methodName);
                    assertTrue(statementHandlerInterceptor.isInterceptTarget(id.toString()));
                });
        Stream.of("selectByPrimaryKey", "insert", "updateByPrimaryKey").forEach((methodName) -> {
            StringJoiner id = new StringJoiner(".");
            id.add("aaa");
            id.add("bbb");
            id.add("CccClass");
            id.add(methodName);
            assertFalse(statementHandlerInterceptor.isInterceptTarget(id.toString()));
        });
    }

    @Test
    void updateNullValues() throws IllegalAccessException, InvocationTargetException, SQLException {
        String prop1 = "prop1";
        String prop2 = "prop2";
        String prop3 = "prop3";
        String prop4 = "prop4";
        LocalDate value1 = MyBatisSelectiveNullValues.DATE;
        LocalTime value2 = MyBatisSelectiveNullValues.TIME;
        Integer value3 = MyBatisSelectiveNullValues.INTEGER;
        String value4 = "test";

        PreparedStatement ps = mock(PreparedStatement.class);
        doNothing().when(ps).setNull(anyInt(), anyInt());

        Reflector ref = mock(Reflector.class);

        when(ref.getGetablePropertyNames()).thenReturn(List.of(prop1, prop2, prop3, prop4).toArray(new String[0]));

        Invoker invoker1 = mock(Invoker.class);
        when(invoker1.invoke(any(), any())).thenReturn(value1);
        when(ref.getGetInvoker(eq(prop1))).thenReturn(invoker1);

        Invoker invoker2 = mock(Invoker.class);
        when(invoker2.invoke(any(), any())).thenReturn(value2);
        when(ref.getGetInvoker(eq(prop2))).thenReturn(invoker2);

        Invoker invoker3 = mock(Invoker.class);
        when(invoker3.invoke(any(), any())).thenReturn(value3);
        when(ref.getGetInvoker(eq(prop3))).thenReturn(invoker3);

        Invoker invoker4 = mock(Invoker.class);
        when(invoker4.invoke(any(), any())).thenReturn(value4);
        when(ref.getGetInvoker(eq(prop4))).thenReturn(invoker4);

        Object parameter = new Object();
        Map<String, List<Integer>> columnParamIndexMap = Map.of("col1", List.of(0, 4), "col2", List.of(1), "col3",
                List.of(2), "col4", List.of(3));

        Configuration conf = mock(Configuration.class);
        doReturn(false).when(conf).isLazyLoadingEnabled();

        ResultMap resultMap = mock(ResultMap.class);
        doReturn(List.of(
                new ResultMapping.Builder(conf, prop1, "col1", new LocalDateTypeHandler()).jdbcType(JdbcType.DATE)
                        .build(),
                new ResultMapping.Builder(conf, prop2, "col2", new LocalTimeTypeHandler()).jdbcType(JdbcType.TIME)
                        .build(),
                new ResultMapping.Builder(conf, prop3, "col3", new IntegerTypeHandler()).jdbcType(JdbcType.INTEGER)
                        .build(),
                new ResultMapping.Builder(conf, prop4, "col4", new StringTypeHandler()).jdbcType(JdbcType.VARCHAR)
                        .build()))
                .when(resultMap).getResultMappings();

        var statementHandlerInterceptor = new StatementHandlerInterceptor();
        statementHandlerInterceptor.updateNullValues(ps, ref, parameter, columnParamIndexMap, resultMap);

        verify(ps, times(1)).setNull(eq(0), anyInt());
        verify(ps, times(1)).setNull(eq(1), anyInt());
        verify(ps, times(1)).setNull(eq(2), anyInt());
        verify(ps, times(1)).setNull(eq(4), anyInt());
    }

    @Test
    void getColumnParamIndexMap_sqlCommandTypeがINSERTの場合getColumnParamIndexMapForInsertが呼び出されること() {
        var statementHandlerInterceptor = spy(StatementHandlerInterceptor.class);
        var sql = "test";

        doReturn(Map.of()).when(statementHandlerInterceptor).getColumnParamIndexMapForInsert(eq(sql));

        statementHandlerInterceptor.getColumnParamIndexMap(SqlCommandType.INSERT, sql);

        verify(statementHandlerInterceptor, times(1)).getColumnParamIndexMapForInsert(eq(sql));
        verify(statementHandlerInterceptor, times(0)).getColumnParamIndexMapForUpdate(anyString());
    }

    @Test
    void getColumnParamIndexMap_sqlCommandTypeがUPDATEの場合getColumnParamIndexMapForUpdateが呼び出されること() {
        var statementHandlerInterceptor = spy(StatementHandlerInterceptor.class);
        var sql = "test";

        doReturn(Map.of()).when(statementHandlerInterceptor).getColumnParamIndexMapForUpdate(eq(sql));

        statementHandlerInterceptor.getColumnParamIndexMap(SqlCommandType.UPDATE, sql);

        verify(statementHandlerInterceptor, times(0)).getColumnParamIndexMapForInsert(anyString());
        verify(statementHandlerInterceptor, times(1)).getColumnParamIndexMapForUpdate(eq(sql));
    }

    @Test
    void getColumnParamIndexMap_sqlCommandTypeがINSERTかUPDATEではない場合getColumnParamIndexMapForUpdateが呼び出されること() {
        var statementHandlerInterceptor = mock(StatementHandlerInterceptor.class);
        var sql = "test";

        Stream.of(SqlCommandType.values()).filter((sqlCommandType) -> {
            return SqlCommandType.INSERT != sqlCommandType && SqlCommandType.UPDATE != sqlCommandType;
        }).forEach((sqlCommandType) -> {
            statementHandlerInterceptor.getColumnParamIndexMap(sqlCommandType, sql);
        });

        verify(statementHandlerInterceptor, times(0)).getColumnParamIndexMapForInsert(sql);
        verify(statementHandlerInterceptor, times(0)).getColumnParamIndexMapForUpdate(sql);
    }

    @Test
    void getColumnParamIndexMapForInsert_insert文からcolumnごとにパラメータのindexを保持したMapが正しく作成されること() {
        StringJoiner column = new StringJoiner(",", "INSERT INTO table_name (", ")");
        column.add("prop1");
        column.add("prop2");
        column.add("prop3");
        column.add("prop4");
        column.add("prop5");

        StringJoiner param = new StringJoiner(",", " VALUES (", ")");
        param.add("?");
        param.add("?");
        param.add("?");
        param.add("NULL");
        param.add("?");

        String sql = column.toString() + param.toString();
        var statementHandlerInterceptor = new StatementHandlerInterceptor();
        var expected = statementHandlerInterceptor.getColumnParamIndexMapForInsert(sql);

        var actual = Map.of("prop1", List.of(1), "prop2", List.of(2), "prop3", List.of(3), "prop5", List.of(4));

        assertEquals(expected, actual);
    }

    @Test
    void getColumnParamIndexMapForUpdate_update文からcolumnごとにパラメータのindexを保持したMapが正しく作成されること() {
        StringJoiner column = new StringJoiner(",", "UPDATE table_name SET ", "");
        column.add("prop1 = ?");
        column.add("prop2 = ?");
        column.add("prop3 = ?");
        column.add("prop4 = NULL");

        StringJoiner param = new StringJoiner(" AND ", " WHERE ", "");
        param.add("prop4 IS NULL");
        param.add("prop2 = ?");
        param.add("prop3 = ?");

        String sql = column.toString() + param.toString();
        var statementHandlerInterceptor = new StatementHandlerInterceptor();
        var expected = statementHandlerInterceptor.getColumnParamIndexMapForUpdate(sql);

        var actual = Map.of("prop1", List.of(1), "prop2", List.of(2, 4), "prop3", List.of(3, 5));

        assertEquals(expected, actual);
    }

    @Test
    void getMapByColumnAndParam_columnごとにindexを保持したMapが正しく作成されること() {
        var statementHandlerInterceptor = new StatementHandlerInterceptor();

        List<String> columnList = List.of("prop1", "prop2", "prop3", "prop2");
        List<String> paramList = List.of("?", "?", "?", "?");
        var expected = statementHandlerInterceptor.getMapByColumnAndParam(columnList, paramList);
        var actual = Map.of("prop1", List.of(1), "prop2", List.of(2, 4), "prop3", List.of(3));

        assertEquals(actual, expected);
    }

}
