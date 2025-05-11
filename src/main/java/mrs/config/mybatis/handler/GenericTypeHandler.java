package mrs.config.mybatis.handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Getter;
import mrs.config.mybatis.MybatisMappedValue;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

@Getter
public class GenericTypeHandler<T extends MybatisMappedValue> extends BaseTypeHandler<T> {

    private T[] enumValues;

    public GenericTypeHandler(Class<T> type) {
        Optional.ofNullable(type)
                .orElseThrow(() -> new IllegalArgumentException("Type argument cannot be null."));
        enumValues = Optional.ofNullable(type.getEnumConstants())
                .orElseThrow(() -> new IllegalArgumentException("Type argument is not enum."));
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, parameter.getValue());
    }

    @Override
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return getResult(value);
    }

    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return getResult(value);
    }

    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return getResult(value);
    }

    private T getResult(String value) {
        return Stream.of(enumValues)
                .filter((enumValue) -> Objects.equals(enumValue.getValue(), value))
                .findFirst()
                .orElse(null);
    }
}
