package mrs.domail.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import mrs.domain.model.MyBatisSelectiveNullValues;
import org.junit.jupiter.api.Test;

class MyBatisSelectiveNullValuesTests {

    @Test
    void defaultOrNullValueLocalDate_引数にnullを渡すとdefault値が返却されること() {
        LocalDate actual = MyBatisSelectiveNullValues.defaultOrNullValue((LocalDate) null);
        LocalDate expected = MyBatisSelectiveNullValues.DATE;
        assertEquals(expected, actual);
    }

    @Test
    void defaultOrNullValueLocalDate_引数にnull以外を渡すと引数の値が返却されること() {
        LocalDate expected = LocalDate.now();
        LocalDate actual = MyBatisSelectiveNullValues.defaultOrNullValue(expected);
        assertEquals(expected, actual);
    }

    @Test
    void defaultOrNullValueLocalTime_引数にnullを渡すとdefault値が返却されること() {
        LocalTime actual = MyBatisSelectiveNullValues.defaultOrNullValue((LocalTime) null);
        LocalTime expected = MyBatisSelectiveNullValues.TIME;
        assertEquals(expected, actual);
    }

    @Test
    void defaultOrNullValueLocalTime_引数にnull以外を渡すと引数の値が返却されること() {
        LocalTime expected = LocalTime.now();
        LocalTime actual = MyBatisSelectiveNullValues.defaultOrNullValue(expected);
        assertEquals(expected, actual);
    }

    @Test
    void defaultOrNullValueInteger_引数にnullを渡すとdefault値が返却されること() {
        Integer actual = MyBatisSelectiveNullValues.defaultOrNullValue((Integer) null);
        Integer expected = MyBatisSelectiveNullValues.INTEGER;
        assertEquals(expected, actual);
    }

    @Test
    void defaultOrNullValueInteger_引数にnull以外を渡すと引数の値が返却されること() {
        Integer expected = 999_999_999;
        Integer actual = MyBatisSelectiveNullValues.defaultOrNullValue(expected);
        assertEquals(expected, actual);
    }
}
