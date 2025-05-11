package mrs.domain.service.room;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import mrs.domain.mapper.mybatis.MeetingRoomMapper;
import mrs.domain.model.mybatis.MeetingRoom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoomServiceImplTests {

    @Mock
    MeetingRoomMapper meetingRoomMapper;

    @Test
    void findReservableRoomList_Mapperから取得した結果が返却されること() {
        var actual = List.of(meetingRoom(1, "test1"), meetingRoom(2, "test2"));

        // モック作成
        when(meetingRoomMapper.selectByExample(any())).thenReturn(actual);

        RoomServiceImpl roomServiceImpl = new RoomServiceImpl(meetingRoomMapper);
        var expected = roomServiceImpl.findReservableRoomList();

        assertEquals(expected, actual);
    }

    @Test
    void findMeetingRoom_データが取得できない場合Exceptionがthrowされること() {
        // モック作成
        when(meetingRoomMapper.selectByPrimaryKey(any())).thenReturn(null);

        assertThrows(
                RuntimeException.class,
                () -> {
                    RoomServiceImpl roomServiceImpl = new RoomServiceImpl(meetingRoomMapper);
                    roomServiceImpl.findMeetingRoom(any());
                },
                "部屋が取得できませんでした。");
    }

    @Test
    void findMeetingRoom_Mapperから取得した結果が返却されること() {
        MeetingRoom actual = meetingRoom(1, "test");
        // モック作成
        when(meetingRoomMapper.selectByPrimaryKey(any())).thenReturn(actual);

        RoomServiceImpl roomServiceImpl = new RoomServiceImpl(meetingRoomMapper);
        MeetingRoom expected = roomServiceImpl.findMeetingRoom(any());

        assertEquals(expected, actual);
    }

    private MeetingRoom meetingRoom(Integer roomId, String roomName) {
        MeetingRoom meetingRoom = new MeetingRoom();
        meetingRoom.setRoomId(roomId);
        meetingRoom.setRoomName(roomName);
        return meetingRoom;
    }
}
