package mrs.domail.model.mybatis;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import lombok.experimental.var;
import mrs.domain.model.RoleName;
import mrs.domain.model.mybatis.Reservation;
import mrs.domain.model.mybatis.Usr;

class ReservationTests {

    @Test
    void overlap_RoomIdが異なる場合_falseを返却すること() {
        var source = new Reservation();
        source.setRoomId(1);
        source.setReservedDate(LocalDate.now());

        var target = new Reservation();
        target.setRoomId(2);
        target.setReservedDate(LocalDate.now());

        assertFalse(source.overlap(target));
        assertFalse(target.overlap(source));
    }

    @ParameterizedTest
    @CsvSource({
            // 同じ時刻の場合
            "1, 2, 1, 2, true",
            // 時刻が重なっている場合
            "1, 2, 1, 3, true",
            "1, 3, 1, 2, true",
            "1, 3, 2, 4, true",
            "2, 4, 1, 3, true",
            // 開始時刻、終了時刻が異なる場合
            "1, 1, 2, 2, false",
            "1, 1, 2, 2, false",
            "1, 2, 2, 3, false",
    })
    void overlap_RoomIdが同じ場合_時刻によって想定した結果が返却されること(int sourceStartHour, int sourceEndHour, int targetStartHour,
            int targetEndHour, boolean expected) {
        var source = new Reservation();
        source.setRoomId(1);
        source.setReservedDate(LocalDate.now());
        source.setStartTime(LocalTime.of(sourceStartHour, 0));
        source.setEndTime(LocalTime.of(sourceEndHour, 0));

        var target = new Reservation();
        target.setRoomId(1);
        target.setReservedDate(source.getReservedDate());
        target.setStartTime(LocalTime.of(targetStartHour, 0));
        target.setEndTime(LocalTime.of(targetEndHour, 0));

        assertEquals(source.overlap(target), expected);
    }

    @Test
    void enabledCancel_RoleNameがADMINの場合trueを返却すること() {
        var user = new Usr();
        user.setRoleName(RoleName.ADMIN);

        var reservation = new Reservation();
        assertTrue(reservation.enabledCancel(user));
    }

    @Test
    void enabledCancel_userIdが同じ場合trueを返却すること() {
        var user = new Usr();
        user.setRoleName(RoleName.USER);
        user.setUserId("test");

        var reservation = new Reservation();
        reservation.setUserId("test");
        assertTrue(reservation.enabledCancel(user));
    }

    @Test
    void enabledCancel_userIdが異なる場合falseを返却すること() {
        var user = new Usr();
        user.setRoleName(RoleName.USER);
        user.setUserId("test");

        var reservation = new Reservation();
        reservation.setUserId("reservation");

        assertFalse(reservation.enabledCancel(user));
    }

}
