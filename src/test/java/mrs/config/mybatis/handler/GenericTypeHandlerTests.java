/** */
package mrs.config.mybatis.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;
import mrs.config.mybatis.MybatisMappedValue;
import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.Test;

class GenericTypeHandlerTests {

    @Test
    void GenericTypeHandler_nullを渡すとExceptionがthrowされること() {
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    new GenericTypeHandler<>(null);
                },
                "Type argument cannot be null.");
    }

    @Test
    void GenericTypeHandler_enumではないclassを渡すとExceptionがthrowされること() {
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    new GenericTypeHandler<>(ErrorType.class);
                },
                "Type argument is not enum.");
    }

    @Test
    void GenericTypeHandler_MybatisMappedValueを継承したenumを渡すとhandlerからenumが取得できること() {
        var handler = new GenericTypeHandler<>(SuccessType.class);
        var successTypeList = List.of(handler.getEnumValues());
        assertTrue(Stream.of(SuccessType.values()).allMatch(successTypeList::contains));
    }

    @Test
    void setNonNullParameter_引数のMybatisMappedValueのgetValueの結果が返却されること() throws SQLException {
        // 引数の値
        int index = 1;
        String value = SuccessType.A.name();

        // モック作成
        PreparedStatement ps = mock(PreparedStatement.class);
        doNothing().when(ps).setString(index, value);

        var handler = new GenericTypeHandler<>(SuccessType.class);
        handler.setNonNullParameter(ps, index, SuccessType.A, JdbcType.VARCHAR);

        // setString
        verify(ps, times(1)).setString(index, value);
    }

    @Test
    void getNullableResultResultSetString_引数のMybatisMappedValueのgetValueの結果が返却されること()
            throws SQLException {
        SuccessType actual = SuccessType.B;
        String columnName = "test";

        // モック作成
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString(columnName)).thenReturn(actual.getValue());

        var handler = new GenericTypeHandler<>(SuccessType.class);
        SuccessType expected = handler.getNullableResult(rs, columnName);

        assertEquals(expected, actual);
    }

    @Test
    void getNullableResultResultSetInt_引数のMybatisMappedValueのgetValueの結果が返却されること()
            throws SQLException {
        SuccessType actual = SuccessType.B;
        int columnIndex = 1;

        // モック作成
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString(columnIndex)).thenReturn(actual.getValue());

        var handler = new GenericTypeHandler<>(SuccessType.class);
        SuccessType expected = handler.getNullableResult(rs, columnIndex);

        assertEquals(expected, actual);
    }

    @Test
    void getNullableResultCallableStatementInt_引数のMybatisMappedValueのgetValueの結果が返却されること()
            throws SQLException {
        SuccessType actual = SuccessType.B;
        int columnIndex = 1;

        // モック作成
        CallableStatement cs = mock(CallableStatement.class);
        when(cs.getString(columnIndex)).thenReturn(actual.getValue());

        var handler = new GenericTypeHandler<>(SuccessType.class);
        SuccessType expected = handler.getNullableResult(cs, columnIndex);

        assertEquals(expected, actual);
    }

    private class ErrorType implements MybatisMappedValue {

        @Override
        public String getValue() {
            // TODO 自動生成されたメソッド・スタブ
            return null;
        }
    }

    private enum SuccessType implements MybatisMappedValue {
        A,
        B;

        @Override
        public String getValue() {
            return name();
        }
    }
}
