package aor.projetofinal.util;

import java.time.LocalDate;

public class DateValidator {


    /**
     * validates whether a given date is in the future or null.
     */
    public static boolean isValidFutureDate(LocalDate date) {
        return date != null && !date.isBefore(LocalDate.now());
    }

}
