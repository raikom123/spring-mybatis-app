package mrs.domain.service.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.EntityNotFoundException;
import mrs.domain.mapper.ReservationsMapper;
import mrs.domain.mapper.mybatis.MeetingRoomMapper;
import mrs.domain.mapper.mybatis.ReservableRoomMapper;
import mrs.domain.mapper.mybatis.ReservationMapper;
import mrs.domain.model.MyBatisSelectiveNullValues;
import mrs.domain.model.ReservationEx;
import mrs.domain.model.mybatis.MeetingRoom;
import mrs.domain.model.mybatis.ReservableRoomExample;
import mrs.domain.model.mybatis.ReservableRoomKey;
import mrs.domain.model.mybatis.Reservation;
import mrs.domain.model.mybatis.ReservationExample;
import mrs.domain.model.mybatis.Usr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private final ReservationMapper reservationMapper;

    private final MeetingRoomMapper meetingRoomMapper;

    private final ReservationsMapper reservationsMapper;

    private final ReservableRoomMapper reservableRoomMapper;

    public ReservationServiceImpl(
            ReservationMapper reservationMapper,
            MeetingRoomMapper meetingRoomMapper,
            ReservationsMapper reservationsMapper,
            ReservableRoomMapper reservableRoomMapper) {
        this.reservationMapper = reservationMapper;
        this.meetingRoomMapper = meetingRoomMapper;
        this.reservationsMapper = reservationsMapper;
        this.reservableRoomMapper = reservableRoomMapper;
    }

    @Override
    public List<ReservationEx> findReservationList(Integer roomId, LocalDate date) {
        return reservationsMapper.selectReservationExList(roomId, date);
    }

    @Override
    public Reservation reserve(Reservation reservation) {
        // 対象の部屋が予約可能かどうかをチェック
        var reservableRoomExample = new ReservableRoomExample();
        reservableRoomExample.createCriteria()
                .andRoomIdEqualTo(reservation.getRoomId())
                .andReservedDateEqualTo(reservation.getReservedDate());

        reservableRoomMapper.selectByExample(reservableRoomExample).stream().findAny()
                .orElseGet(() -> {
                    ReservableRoomKey reservableRoomKey = new ReservableRoomKey();
                    reservableRoomKey.setReservedDate(reservation.getReservedDate());
                    reservableRoomKey.setRoomId(reservation.getRoomId());
                    reservableRoomMapper.insert(reservableRoomKey);
                    return reservableRoomKey;
                });

        // 重複チェック
        var reservationExample = new ReservationExample();
        reservationExample.createCriteria()
                .andRoomIdEqualTo(reservation.getRoomId())
                .andReservedDateEqualTo(reservation.getReservedDate());
        boolean overlap = reservationMapper
                .selectByExample(reservationExample)
                .stream()
                .anyMatch(x -> x.overlap(reservation));
        if (overlap) {
            throw new AlreadyReservedException("入力の時間帯はすでに予約済みです。");
        }

        reservationMapper.insert(reservation);
        return reservation;
    }

    @Override
    public Reservation update(Reservation reservation) {
        // 対象の部屋が予約可能かどうかをチェック
        Optional.ofNullable(
                reservationsMapper.selectReservableRoomForUpdate(
                        reservation.getRoomId(), reservation.getReservedDate()))
                .orElseThrow(() -> new UnavailableReservationException("入力の日付・部屋の組み合わせは予約できません。"));

        var example = new ReservationExample();
        example.createCriteria()
                .andRoomIdEqualTo(reservation.getRoomId())
                .andReservedDateEqualTo(reservation.getReservedDate())
                .andReservationIdNotEqualTo(reservation.getReservationId());
        boolean overlap = reservationMapper
                .selectByExample(example)
                .stream()
                .anyMatch(x -> x.overlap(reservation));
        if (overlap) {
            throw new AlreadyReservedException("入力の時間帯はすでに予約済みです。");
        }

        reservation.setRemindDate(
                MyBatisSelectiveNullValues.defaultOrNullValue(reservation.getRemindDate()));
        reservation.setRemindTime(
                MyBatisSelectiveNullValues.defaultOrNullValue(reservation.getRemindTime()));
        reservation.setMemberCount(
                MyBatisSelectiveNullValues.defaultOrNullValue(reservation.getMemberCount()));

        // 本当はupdateByPrimaryKeyでいいが、updateByPrimaryKeySelectiveでnullに更新する検証のため
        reservationMapper.updateByPrimaryKeySelective(reservation);
        return reservation;
    }

    @Override
    public void cancel(Integer reservationId, Usr requestUser) {
        Reservation reservation = Optional.ofNullable(reservationMapper.selectByPrimaryKey(reservationId))
                .orElseThrow(() -> new EntityNotFoundException("指定された予約が見つかりません。"));
        if (reservation.enabledCancel(requestUser)) {
            reservationMapper.deleteByPrimaryKey(reservationId);
        } else {
            throw new AccessDeniedException("要求されたキャンセルは許可できません。");
        }
    }

    @Override
    public MeetingRoom findMeetingRoom(Integer roomId) {
        return Optional.ofNullable(meetingRoomMapper.selectByPrimaryKey(roomId))
                .orElseThrow(() -> new EntityNotFoundException("指定された部屋が見つかりません。"));
    }
}
