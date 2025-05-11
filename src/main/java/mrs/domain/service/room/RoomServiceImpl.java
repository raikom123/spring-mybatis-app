package mrs.domain.service.room;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.EntityNotFoundException;
import mrs.domain.mapper.mybatis.MeetingRoomMapper;
import mrs.domain.model.mybatis.MeetingRoom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RoomServiceImpl implements RoomService {

    private MeetingRoomMapper meetingRoomMapper;

    public RoomServiceImpl(MeetingRoomMapper meetingRoomMapper) {
        this.meetingRoomMapper = meetingRoomMapper;
    }

    @Override
    public List<MeetingRoom> findReservableRoomList() {
        return meetingRoomMapper.selectByExample(null);
    }

    @Override
    public MeetingRoom findMeetingRoom(Integer roomId) {
        return Optional.ofNullable(meetingRoomMapper.selectByPrimaryKey(roomId))
                .orElseThrow(() -> new EntityNotFoundException("部屋が取得できませんでした。"));
    }
}
