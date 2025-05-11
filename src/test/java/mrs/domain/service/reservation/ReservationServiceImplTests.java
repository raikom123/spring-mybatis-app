package mrs.domain.service.reservation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import jakarta.persistence.EntityNotFoundException;
import mrs.domain.mapper.ReservationsMapper;
import mrs.domain.mapper.mybatis.MeetingRoomMapper;
import mrs.domain.mapper.mybatis.ReservableRoomMapper;
import mrs.domain.mapper.mybatis.ReservationMapper;
import mrs.domain.model.ReservationEx;
import mrs.domain.model.RoleName;
import mrs.domain.model.mybatis.MeetingRoom;
import mrs.domain.model.mybatis.ReservableRoomKey;
import mrs.domain.model.mybatis.Reservation;
import mrs.domain.model.mybatis.Usr;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTests {

    @Mock
    ReservationsMapper reservationsMapper;

    @Mock
    ReservationMapper reservationMapper;

    @Mock
    MeetingRoomMapper meetingRoomMapper;

    @Mock
    ReservableRoomMapper reservableRoomMapper;

    @Test
    void findReservationList_Mapperの結果がそのまま返却されること() {
        var actual = List.of(reservationEx(1, user(), 9), reservationEx(2, admin(), 10));

        // モック作成
        when(reservationsMapper.selectReservationExList(any(), any())).thenReturn(actual);

        var reservationServiceImpl = new ReservationServiceImpl(null, null, reservationsMapper, null);
        var expected = reservationServiceImpl.findReservationList(1, LocalDate.now());

        assertEquals(expected, actual);
    }

    @Test
    void reserve_ReservableRoomIdでデータが取得できない場合_reservableRoomが登録されること() {
        // モック作成
        when(reservableRoomMapper.selectByExample(any())).thenReturn(List.of());
        when(reservableRoomMapper.insert(any())).thenReturn(1);
        when(reservationMapper.selectByExample(any())).thenReturn(List.of());
        when(reservationMapper.insert(any())).thenReturn(1);

        ReservationServiceImpl reservationServiceImpl = new ReservationServiceImpl(reservationMapper, null, null,
                reservableRoomMapper);
        reservationServiceImpl.reserve(reservation(1, user(), 9));

        verify(reservableRoomMapper, times(1)).insert(any());
    }

    @Test
    void reserve_登録済のデータと重複している場合Exceptionがthrowされること() {
        var reservation = reservation(1, user(), 9);

        // モック作成
        when(reservationMapper.selectByExample(any())).thenReturn(List.of(reservation));
        when(reservableRoomMapper.selectByExample(any())).thenReturn(List.of(reservableRoomKey(reservation)));

        assertThrows(
                AlreadyReservedException.class,
                () -> {
                    var reservationServiceImpl = new ReservationServiceImpl(reservationMapper, null, null,
                            reservableRoomMapper);
                    reservationServiceImpl.reserve(reservation);
                },
                "入力の時間帯はすでに予約済みです。");
    }

    @Test
    void reserve_重複したデータが存在しない場合_予約情報が登録されること() {
        var actual = reservation(1, user(), 10);
        var registered = reservation(1, user(), 11);

        // モック作成
        when(reservableRoomMapper.selectByExample(any())).thenReturn(List.of(reservableRoomKey(actual)));
        when(reservationMapper.selectByExample(any())).thenReturn(List.of(registered));
        when(reservationMapper.insert(any())).thenReturn(1);

        var reservationServiceImpl = new ReservationServiceImpl(reservationMapper, null, null, reservableRoomMapper);
        var expected = reservationServiceImpl.reserve(actual);

        assertEquals(expected, actual);

        // reservationMapper#insert が1度呼び出されたことを確認
        verify(reservationMapper, times(1)).insert(actual);
    }

    @Test
    void cancel_予約が見つからない場合Exceptionをthrowすること() {
        // モックを作成
        when(reservationMapper.selectByPrimaryKey(any())).thenReturn(null);

        assertThrows(
                EntityNotFoundException.class,
                () -> {
                    var reservationServiceImpl = new ReservationServiceImpl(reservationMapper, null, null, null);
                    reservationServiceImpl.cancel(1, user());
                },
                "指定された予約が見つかりません。");
    }

    @Test
    void cancel_キャンセルする権限がない場合Exceptionをthrowすること() {
        Reservation reservation = reservation(1, admin(), 9);

        // モックを作成
        when(reservationMapper.selectByPrimaryKey(any())).thenReturn(reservation);

        assertThrows(
                AccessDeniedException.class,
                () -> {
                    var reservationServiceImpl = new ReservationServiceImpl(reservationMapper, null, null, null);
                    reservationServiceImpl.cancel(2, user());
                },
                "要求されたキャンセルは許可できません。");
    }

    @Test
    void cancel_キャンセルした場合deleteが呼び出されること() {
        Usr user = admin();
        Reservation reservation = reservation(1, user, 9);

        // モックを作成
        when(reservationMapper.selectByPrimaryKey(any())).thenReturn(reservation);
        when(reservationMapper.deleteByPrimaryKey(any())).thenReturn(1);

        var reservationServiceImpl = new ReservationServiceImpl(reservationMapper, null, null, null);
        reservationServiceImpl.cancel(reservation.getReservationId(), user);

        verify(reservationMapper, times(1)).deleteByPrimaryKey(any());
    }

    @Test
    void findMeetingRoom_部屋が取得できない場合Exceptionがthrowされること() {
        // モック作成
        when(meetingRoomMapper.selectByPrimaryKey(any())).thenReturn(null);

        assertThrows(
                EntityNotFoundException.class,
                () -> {
                    var reservationServiceImpl = new ReservationServiceImpl(null, meetingRoomMapper, null, null);
                    reservationServiceImpl.findMeetingRoom(any());
                },
                "指定された部屋が見つかりません。");
    }

    @Test
    void findMeetingRoom_部屋が取得できた場合Mapperから取得した結果が返却されること() {
        var actual = meetingRoom(1, "test");

        // モック作成
        when(meetingRoomMapper.selectByPrimaryKey(anyInt())).thenReturn(actual);

        var reservationServiceImpl = new ReservationServiceImpl(null, meetingRoomMapper, null, null);
        var expected = reservationServiceImpl.findMeetingRoom(anyInt());

        assertEquals(expected, actual);
    }

    private Reservation reservation(Integer reservationId, Usr user, int hour) {
        // 予約情報を作成
        var reservation = new Reservation();
        reservation.setReservationId(reservationId);
        reservation.setReservedDate(LocalDate.now());
        reservation.setRoomId(1);
        reservation.setUserId(user.getUserId());
        reservation.setStartTime(LocalTime.of(hour, 00));
        reservation.setEndTime(LocalTime.of(hour, 30));
        return reservation;
    }

    private ReservationEx reservationEx(Integer reservationId, Usr user, int hour) {
        var reservationEx = new ReservationEx();
        reservationEx.setReservationId(reservationId);
        reservationEx.setReservedDate(LocalDate.now());
        reservationEx.setRoomId(1);
        reservationEx.setUserId(user.getUserId());
        reservationEx.setStartTime(LocalTime.of(hour, 00));
        reservationEx.setEndTime(LocalTime.of(hour, 30));
        reservationEx.setFirstName(user.getFirstName());
        reservationEx.setLastName(user.getLastName());
        return reservationEx;
    }

    private MeetingRoom meetingRoom(Integer roomId, String roomName) {
        MeetingRoom meetingRoom = new MeetingRoom();
        meetingRoom.setRoomId(roomId);
        meetingRoom.setRoomName(roomName);
        return meetingRoom;
    }

    private Usr user() {
        var usr = new Usr();
        usr.setFirstName("test");
        usr.setLastName("user");
        usr.setPassword("pass");
        usr.setRoleName(RoleName.USER);
        usr.setUserId("test-user");
        return usr;
    }

    private Usr admin() {
        var usr = new Usr();
        usr.setFirstName("test");
        usr.setLastName("admin");
        usr.setPassword("word");
        usr.setRoleName(RoleName.ADMIN);
        usr.setUserId("test-admin");
        return usr;
    }

    private ReservableRoomKey reservableRoomKey(Reservation reservation) {
        // 予約可能な部屋のキーを作成
        var reservableRoomKey = new ReservableRoomKey();
        reservableRoomKey.setReservedDate(reservation.getReservedDate());
        reservableRoomKey.setRoomId(reservation.getRoomId());
        return reservableRoomKey;
    }

}
