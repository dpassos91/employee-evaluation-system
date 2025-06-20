package aor.projetofinal.Util;

import aor.projetofinal.entity.ProfileEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for validating user profiles.
 * This class provides methods to check if a profile is complete and to retrieve missing fields.
 */
public class ProfileValidator {

    /**
     * Checks if all required fields of the profile are filled in.
     *
     * @param profile the profile to validate
     * @return true if profile is complete, false otherwise
     */
    public static boolean isProfileComplete(ProfileEntity profile) {
        return getMissingFields(profile).isEmpty();
    }

    /**
     * Returns the list of required fields that are missing or invalid.
     *
     * @param profile the profile to validate
     * @return list of missing field names
     */
    public static List<String> getMissingFields(ProfileEntity profile) {
        List<String> missing = new ArrayList<>();

        if (profile == null) {
            // If profile doesn't exist, all fields are missing
            missing.add("firstName");
            missing.add("lastName");
            missing.add("birthDate");
            missing.add("address");
            missing.add("zipCode");
            missing.add("phone");
            missing.add("usualWorkplace");
            return missing;
        }

        // First name
        if (isNullOrBlank(profile.getFirstName())) {
            missing.add("firstName");
        }

        // Last name
        if (isNullOrBlank(profile.getLastName())) {
            missing.add("lastName");
        }

        // Birth date
        if (profile.getBirthDate() == null) {
            missing.add("birthDate");
        }

        // Address
        if (isNullOrBlank(profile.getAddress())) {
            missing.add("address");
        }

        // ZipCode 
        if (isNullOrBlank(profile.getZipCode())) {
            missing.add("zipCode");
        }

        // Phone
        if (isNullOrBlank(profile.getPhone())) {
            missing.add("phone");
        }

        // Usual workplace
        if (profile.getUsualWorkplace() == null) {
            missing.add("usualWorkplace");
        } 

        return missing;
    }

    /**
     * Helper to check if string is null or blank.
     */
    private static boolean isNullOrBlank(String str) {
        return (str == null || str.trim().isEmpty());
    }
}

