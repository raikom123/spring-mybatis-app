package mrs.domain.mapper;

import java.time.LocalDate;
import java.util.List;
import mrs.domain.model.mybatis.MeetingRoom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RoomsMapper {

    List<MeetingRoom> selectReservationRoomList(@Param("reserved_date") LocalDate date);
}
