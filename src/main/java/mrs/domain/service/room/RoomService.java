package mrs.domain.service.room;

import java.util.List;
import mrs.domain.model.mybatis.MeetingRoom;

public interface RoomService {

    List<MeetingRoom> findReservableRoomList();

    MeetingRoom findMeetingRoom(Integer roomId);
}
