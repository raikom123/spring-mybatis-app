package mrs.domain.service.reservation;

public class AlreadyReservedException extends RuntimeException {

    private static final long serialVersionUID = 7458724358664493713L;

    public AlreadyReservedException(String message) {
        super(message);
    }
}
