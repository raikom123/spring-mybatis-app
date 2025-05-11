package mrs.app.reservation;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@EndTimeMustBeAfterStartTime(message = "終了時刻は開始時刻より後にしてください")
public class ReservationForm implements Serializable {

    private static final long serialVersionUID = -4953320549163307784L;

    @NotNull(message = "必須です")
    @ThirtyMinutesUnit(message = "30分単位で入力してください")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @NotNull(message = "必須です")
    @ThirtyMinutesUnit(message = "30分単位で入力してください")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime endTime;

    private String memo;
    private Integer memberCount;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate remindDate;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime remindTime;
}
