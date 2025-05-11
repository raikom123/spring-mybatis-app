package mrs.domain.model;

import lombok.Getter;
import lombok.Setter;
import mrs.domain.model.mybatis.ReservableRoomKey;

@Getter
@Setter
public class ReservableRoom extends ReservableRoomKey {

    private String roomName;
}
