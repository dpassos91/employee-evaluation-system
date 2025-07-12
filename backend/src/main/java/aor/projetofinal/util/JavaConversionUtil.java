package aor.projetofinal.util;

import aor.projetofinal.dao.UserDao;
import aor.projetofinal.dto.*;
import aor.projetofinal.entity.EvaluationEntity;
import aor.projetofinal.entity.ProfileEntity;
import aor.projetofinal.entity.SessionTokenEntity;
import aor.projetofinal.entity.UserEntity;
import aor.projetofinal.entity.enums.UsualWorkPlaceEnum;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for converting JPA entities to DTOs (Data Transfer Objects).
 * <p>
 * Ensures that conversion logic is centralized and reusable, preventing code duplication.
 * <b>Note:</b> FlatProfileDto should never expose JPA references or complex nested objects—only simple fields,
 * for API safety and serialization performance.
 */
@ApplicationScoped
public class JavaConversionUtil {

    @Inject
    UserDao userDao;

    /**
     * Builds a CSV string from a list of FlatEvaluationDto.
     * Each row includes evaluation metadata for export.
     *
     * @param evaluations The list of evaluation DTOs to export.
     * @return A CSV-formatted string.
     */
    public static String buildCsvFromEvaluations(List<FlatEvaluationDto> evaluations) {
        StringBuilder sb = new StringBuilder();
        sb.append("Evaluated Name,Email,State,Grade,Evaluator,Cycle End Date\n");

        for (FlatEvaluationDto dto : evaluations) {
            sb.append('"').append(dto.getEvaluatedName() != null ? dto.getEvaluatedName() : "").append("\",")
                    .append(dto.getEvaluatedEmail() != null ? dto.getEvaluatedEmail() : "").append(",")
                    .append(dto.getState() != null ? dto.getState() : "").append(",")
                    .append(dto.getGrade() != null ? dto.getGrade() : "").append(",")
                    .append(dto.getEvaluatorName() != null ? dto.getEvaluatorName() : "").append(",")
                    .append(dto.getCycleEndDate() != null ? dto.getCycleEndDate() : "").append("\n");
        }

        return sb.toString();
    }







    /**
     * Builds a CSV string from a list of FlatProfileDto objects.
     * Each row will include the user's full name, workplace, manager name, and photograph URL.
     *
     * @param profiles The list of FlatProfileDto objects to export.
     * @return A CSV-formatted string representing the user profiles.
     */
    public static String buildUsersCsvFromFlatProfiles(List<FlatProfileDto> profiles) {
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("Full Name,Workplace,Manager,Photograph\n");

        for (FlatProfileDto p : profiles) {
            String name = (p.getFirstName() != null ? p.getFirstName() : "") + " " +
                    (p.getLastName() != null ? p.getLastName() : "");
            String workplace = p.getUsualWorkplace() != null ? p.getUsualWorkplace() : "";
            String manager = p.getManagerName() != null ? p.getManagerName() : "";
            String photo = p.getPhotograph() != null ? p.getPhotograph() : "";

            csvBuilder.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    name.trim(), workplace, manager, photo));
        }
        return csvBuilder.toString();
    }

    /**
     * Converts an EvaluationEntity into a FlatEvaluationDto for use in REST API responses.
     * This method extracts and formats only basic string-based information for display,
     * avoiding exposure of complex enums or entity references.
     *
     * @param evaluation The evaluation entity to convert.
     * @return A FlatEvaluationDto representing the simplified evaluation data.
     */
    public static FlatEvaluationDto convertEvaluationToFlatDto(EvaluationEntity evaluation) {
        FlatEvaluationDto dto = new FlatEvaluationDto();

        if (evaluation.getId() != null) {
            dto.setEvaluationId(evaluation.getId());
        }

        // 1. Evaluated user info
        UserEntity evaluated = evaluation.getEvaluated();
        if (evaluated != null) {
            dto.setEvaluatedEmail(evaluated.getEmail());

            if (evaluated.getProfile() != null) {
                String fullName = evaluated.getProfile().getFirstName() + " " + evaluated.getProfile().getLastName();
                dto.setEvaluatedName(fullName);
                dto.setPhotograph(evaluated.getProfile().getPhotograph());
            }
        }

        // 2. Evaluation state (as string, e.g. "IN_EVALUATION")
        if (evaluation.getState() != null) {
            dto.setState(evaluation.getState().name());
        }

        // 3. Grade (converted to string, e.g. "3")
        if (evaluation.getGrade() != null) {
            dto.setGrade(String.valueOf(evaluation.getGrade().getGrade()));
        }

        // 4. Evaluator name
        UserEntity evaluator = evaluation.getEvaluator();
        if (evaluator != null && evaluator.getProfile() != null) {
            String evaluatorName = evaluator.getProfile().getFirstName() + " " + evaluator.getProfile().getLastName();
            dto.setEvaluatorName(evaluatorName);
        }

        // 5. Cycle end date (formatted as "yyyy-MM-dd HH:mm")
        if (evaluation.getCycle() != null && evaluation.getCycle().getEndDate() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            dto.setCycleEndDate(evaluation.getCycle().getEndDate().format(formatter));
        }

        return dto;
    }






    /**
     * Converts a list of ProfileEntity to a list of FlatProfileDto.
     *
     * @param profileEntities The list of ProfileEntity objects.
     * @return A list of FlatProfileDto.
     */
    public static List<FlatProfileDto> convertProfileEntityListToFlatProfileDtoList(List<ProfileEntity> profileEntities) {
        List<FlatProfileDto> flatDtos = new ArrayList<>();
        for (ProfileEntity p : profileEntities) {
            FlatProfileDto flat = convertProfileEntityToFlatProfileDto(p);
            if (flat != null) flatDtos.add(flat);
        }
        return flatDtos;
    }

    /**
     * Converts a ProfileEntity to a FlatProfileDto (flat, no JPA references).
     * Only exposes simple fields for use in API list views or exports.
     *
     * @param profile The ProfileEntity to convert.
     * @return A FlatProfileDto populated with simple fields, or null if profile/user is null.
     */
public static FlatProfileDto convertProfileEntityToFlatProfileDto(ProfileEntity profile) {
    if (profile == null || profile.getUser() == null) {
        return null;
    }

    UserEntity user = profile.getUser();
    UserEntity manager = user.getManager();

    // Compute manager's display name (first + last or email, or empty)
    String managerName = "";
    if (manager != null && manager.getProfile() != null) {
        String mFirst = manager.getProfile().getFirstName() != null ? manager.getProfile().getFirstName() : "";
        String mLast = manager.getProfile().getLastName() != null ? manager.getProfile().getLastName() : "";
        managerName = (mFirst + " " + mLast).trim();
        if (managerName.isEmpty() && manager.getEmail() != null) {
            managerName = manager.getEmail();
        }
    }

    // UsualWorkplace as String (Enum to String, or empty)
    String usualWorkplace = profile.getUsualWorkplace() != null
            ? profile.getUsualWorkplace().name()
            : "";

    // Get the user's role name (e.g. "USER", "MANAGER", "ADMIN")
    String roleName = (user.getRole() != null && user.getRole().getName() != null)
            ? user.getRole().getName()
            : "USER";

    // Get the managerId if exists
    Long managerId = (manager != null) ? Long.valueOf(manager.getId()) : null;

    // Now use the updated constructor (or setters)
    FlatProfileDto flat = new FlatProfileDto();
    flat.setUserId(Long.valueOf(user.getId()));
    flat.setFirstName(profile.getFirstName());
    flat.setLastName(profile.getLastName());
    flat.setEmail(user.getEmail());
    flat.setUsualWorkplace(usualWorkplace);
    flat.setManagerName(managerName);
    flat.setManagerId(managerId);
    flat.setPhotograph(profile.getPhotograph());
    flat.setRole(roleName);

    return flat;
}


 /**
 * Converts a ProfileEntity to a ProfileDto for detailed profile view/edit.
 * This method extracts all relevant data as "flat" fields (no JPA entities),
 * including user identity, manager, role, and profile details.
 *
 * @param p The ProfileEntity to convert.
 * @return A fully populated ProfileDto.
 */
public static ProfileDto convertProfileEntityToProfileDto(ProfileEntity p) {
    ProfileDto profileDto = new ProfileDto();

    // Basic profile fields
    profileDto.setUserId(p.getUser() != null ? Long.valueOf(p.getUser().getId()) : null);
    profileDto.setFirstName(p.getFirstName());
    profileDto.setLastName(p.getLastName());
    profileDto.setBirthDate(p.getBirthDate());
    profileDto.setAddress(p.getAddress());
    profileDto.setPhone(p.getPhone());
    profileDto.setPhotograph(p.getPhotograph());
    profileDto.setBio(p.getBio());

    // Usual workplace as string
    if (p.getUsualWorkplace() != null) {
        profileDto.setUsualWorkplace(p.getUsualWorkplace().name());
    } else {
        profileDto.setUsualWorkplace(null);
    }

    // Email, role from User
    if (p.getUser() != null) {
        profileDto.setEmail(p.getUser().getEmail());
        if (p.getUser().getRole() != null) {
            profileDto.setRole(p.getUser().getRole().getName());
        }
    }

    // Manager info
    if (p.getUser() != null && p.getUser().getManager() != null) {
        UserEntity manager = p.getUser().getManager();
        profileDto.setManagerId(Long.valueOf(manager.getId()));

        // Manager's display name (first + last name, or email if name missing)
        String mName = "";
        ProfileEntity mProfile = manager.getProfile();
        if (mProfile != null) {
            String mFirst = mProfile.getFirstName() != null ? mProfile.getFirstName() : "";
            String mLast = mProfile.getLastName() != null ? mProfile.getLastName() : "";
            mName = (mFirst + " " + mLast).trim();
        }
        if (mName.isEmpty() && manager.getEmail() != null) {
            mName = manager.getEmail();
        }
        profileDto.setManagerName(mName);
    }

    // Profile completeness info (if you have a validator utility)
    // These lines are optional, based on the logic for completeness/UI validation
    profileDto.setProfileComplete(ProfileValidator.isProfileComplete(p));
    profileDto.setMissingFields(ProfileValidator.getMissingFields(p));

    return profileDto;
}


    /**
     * Converts a SessionTokenEntity to a SessionStatusDto.
     *
     * @param sessionTokenEntity The SessionTokenEntity.
     * @return The SessionStatusDto.
     */
    public static SessionStatusDto convertSessionTokenEntityToSessionStatusDto(SessionTokenEntity sessionTokenEntity) {
        SessionStatusDto sessionStatusDto = new SessionStatusDto(sessionTokenEntity.getTokenValue(), sessionTokenEntity.getExpiryDate());
        sessionStatusDto.setSessionToken(sessionTokenEntity.getTokenValue());
        sessionStatusDto.setExpiryDate(sessionTokenEntity.getExpiryDate());
        return sessionStatusDto;
    }

    /**
     * Converts a closed EvaluationEntity into a flat DTO for listing in the user's evaluation history.
     *
     * @param evaluation The closed evaluation entity.
     * @return A flat DTO containing cycle number, date, grade, and evaluation ID.
     */
    public static FlatEvaluationHistoryDto convertToFlatHistoryDto(EvaluationEntity evaluation) {
        if (evaluation == null || evaluation.getCycle() == null || evaluation.getGrade() == null) {
            return null;
        }

        FlatEvaluationHistoryDto dto = new FlatEvaluationHistoryDto();
        dto.setEvaluationId(evaluation.getId());
        dto.setCycleNumber(evaluation.getCycle().getId().intValue());

        if (evaluation.getDate() != null) {
            dto.setEvaluationDate(evaluation.getDate().toLocalDate().toString()); // yyyy-MM-dd
        }

        dto.setGrade(evaluation.getGrade().getGrade());

        return dto;
    }





    /**
     * Converts a UserEntity to a UserDto.
     *
     * @param userEntity The UserEntity to convert.
     * @return The UserDto.
     */
    public static UserDto convertUserEntityToUserDto(UserEntity userEntity) {
        UserDto userDto = new UserDto();
        userDto.setEmail(userEntity.getEmail());
        userDto.setActive(userEntity.isActive());
        userDto.setConfirmed(userEntity.isConfirmed());
        userDto.setCreatedAt(userEntity.getCreatedAt());
        userDto.setConfirmationToken(userEntity.getConfirmationToken());
        userDto.setConfirmationTokenExpiry(userEntity.getConfirmationTokenExpiry());
        userDto.setRecoveryToken(userEntity.getRecoveryToken());
        userDto.setRecoveryTokenExpiry(userEntity.getRecoveryTokenExpiry());
        return userDto;
    }
}


