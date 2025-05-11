package mrs.domain.service.reservation;

import java.time.LocalDate;
import java.util.List;
import mrs.domain.model.ReservationEx;
import mrs.domain.model.mybatis.MeetingRoom;
import mrs.domain.model.mybatis.Reservation;
import mrs.domain.model.mybatis.Usr;

public interface ReservationService {

    List<ReservationEx> findReservationList(Integer roomId, LocalDate date);

    Reservation reserve(Reservation reservation);

    Reservation update(Reservation reservation);

    void cancel(Integer reservationId, Usr requestUser);

    MeetingRoom findMeetingRoom(Integer roomId);
}
