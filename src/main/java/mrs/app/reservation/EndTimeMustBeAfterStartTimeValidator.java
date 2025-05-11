package mrs.app.reservation;

import java.time.LocalTime;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EndTimeMustBeAfterStartTimeValidator
        implements ConstraintValidator<EndTimeMustBeAfterStartTime, ReservationForm> {

    private String message;

    @Override
    public void initialize(EndTimeMustBeAfterStartTime constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(ReservationForm form, ConstraintValidatorContext context) {
        LocalTime startTime = form.getStartTime();
        LocalTime endTime = form.getEndTime();
        if (startTime == null || endTime == null) {
            return true;
        }

        if (startTime.isAfter(endTime)) {
            context.disableDefaultConstraintViolation();
            context
                    .buildConstraintViolationWithTemplate(message)
                    .addPropertyNode("endTime")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
