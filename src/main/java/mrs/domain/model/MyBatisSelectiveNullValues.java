package mrs.domain.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import jakarta.transaction.NotSupportedException;

public class MyBatisSelectiveNullValues {

    private MyBatisSelectiveNullValues() throws NotSupportedException {
        throw new NotSupportedException("Cannot create instance.");
    }

    public static final LocalDate DATE = LocalDate.of(9999, 12, 31);
    public static final LocalTime TIME = LocalTime.of(23, 59, 59, 999_999_999);
    public static final Integer INTEGER = Integer.MIN_VALUE;

    public static LocalDate defaultOrNullValue(LocalDate defaultValue) {
        return Objects.requireNonNullElse(defaultValue, DATE);
    }

    public static LocalTime defaultOrNullValue(LocalTime defaultValue) {
        return Objects.requireNonNullElse(defaultValue, TIME);
    }

    public static Integer defaultOrNullValue(Integer defaultValue) {
        return Objects.requireNonNullElse(defaultValue, INTEGER);
    }

    public static boolean isNullValue(Object obj) {
        if (DATE == obj) {
            return true;
        }
        if (TIME == obj) {
            return true;
        }
        return INTEGER == obj;
    }
}
