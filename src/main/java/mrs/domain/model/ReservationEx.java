package mrs.domain.model;

import lombok.Getter;
import lombok.Setter;
import mrs.domain.model.mybatis.Reservation;

@Getter
@Setter
public class ReservationEx extends Reservation {

    private String lastName;

    private String firstName;
}
