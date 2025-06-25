package aor.projetofinal.bean;

import aor.projetofinal.util.JavaConversionUtil;
import aor.projetofinal.dto.PaginatedProfilesDto;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import aor.projetofinal.context.RequestContext;
import aor.projetofinal.dto.ProfileDto;
import aor.projetofinal.entity.ProfileEntity;
import aor.projetofinal.entity.UserEntity;
import aor.projetofinal.entity.enums.UsualWorkPlaceType;
import aor.projetofinal.util.ProfileValidator;
import aor.projetofinal.dao.UserDao;
import aor.projetofinal.dao.ProfileDao;
import aor.projetofinal.util.PasswordUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class ProfileBean implements Serializable {

    private static final Logger logger = LogManager.getLogger(ProfileBean.class);

    @Inject
    JavaConversionUtil javaConversionUtil;


    @Inject
    private UserDao userDao;

    @Inject
    private ProfileDao profileDao;


    public ArrayList<ProfileDto> findProfilesWithFilters(String employeeName, UsualWorkPlaceType workplace, String managerEmail) {

        List<ProfileEntity> profilesDB = profileDao.findProfilesWithFilters(employeeName, workplace, managerEmail);

        return javaConversionUtil.convertProfileEntityListtoProfileDtoList(new ArrayList<>(profilesDB));
    }


    public PaginatedProfilesDto findProfilesWithFiltersPaginated(String employeeName, UsualWorkPlaceType workplace, String managerEmail, int page) {

        List<ProfileEntity> profilesDB = profileDao.findProfilesWithFiltersPaginated(employeeName, workplace, managerEmail, page);
        //Contar o total de perfis correspondentes aos filtros
        long totalCount = profileDao.countProfilesWithFilters(employeeName, workplace, managerEmail);

        //Converter os resultados para DTO
        List<ProfileDto> profileDtos = javaConversionUtil.convertProfileEntityListtoProfileDtoList(new ArrayList<>(profilesDB));

        //Calcular número total de páginas
        int pageSize = 10; // valor fixo por agora
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);

        // Construir o DTO de resposta com paginação
        return new PaginatedProfilesDto(profileDtos, totalCount, totalPages, page);
    }



    public boolean updateProfile(ProfileDto profileDto, String email) {

        logger.info("birthDate recebido no DTO: {}", profileDto.getBirthDate());
        logger.info("usualWorkplace recebido no DTO: {}", profileDto.getUsualWorkplace());

        UserEntity user = userDao.findByEmail(email);

        if (user == null) {
            logger.warn("User: {} | IP: {} | Email: {} - Could not find user to update.",
                    RequestContext.getAuthor(), RequestContext.getIp(), email);
            return false;
        }

        ProfileEntity profileToUpdate = user.getProfile();

        if (profileToUpdate == null) {
            profileToUpdate = new ProfileEntity();
            profileToUpdate.setUser(user);
            user.setProfile(profileToUpdate);
            logger.info("User: {} | IP: {} | Email: {} - Created new profile for user.",
                    RequestContext.getAuthor(), RequestContext.getIp(), email);
        }

        // Required fields
        profileToUpdate.setFirstName(profileDto.getFirstName());
        profileToUpdate.setLastName(profileDto.getLastName());
        profileToUpdate.setBirthDate(profileDto.getBirthDate());
        profileToUpdate.setAddress(profileDto.getAddress());
        profileToUpdate.setPhone(profileDto.getPhone());

        // Optional fields
        profileToUpdate.setPhotograph(profileDto.getPhotograph());
        profileToUpdate.setBio(profileDto.getBio());

        // Conversion and validation for usualWorkplace (Enum)
        String usualWorkplaceString = profileDto.getUsualWorkplace();
        if (usualWorkplaceString != null && !usualWorkplaceString.isBlank()) {
            try {
                UsualWorkPlaceType type = UsualWorkPlaceType.valueOf(usualWorkplaceString.toUpperCase());
                profileToUpdate.setUsualWorkplace(type);
            } catch (IllegalArgumentException e) {
                logger.warn("User: {} | IP: {} | Email: {} | Value: {} - Invalid usualWorkplace value received.",
                        RequestContext.getAuthor(), RequestContext.getIp(), email, usualWorkplaceString);
                return false;
            }
        } else {
            logger.warn("User: {} | IP: {} | Email: {} - Missing required usualWorkplace field.",
                    RequestContext.getAuthor(), RequestContext.getIp(), email);
            return false;
        }

        profileDao.save(profileToUpdate);
        logger.info("birthDate depois de guardar: {}", profileToUpdate.getBirthDate());
        logger.info("usualWorkplace depois de guardar: {}", profileToUpdate.getUsualWorkplace());

        logger.info("User: {} | IP: {} | Email: {} - Successfully updated profile for user.",
                RequestContext.getAuthor(), RequestContext.getIp(), email);

        return true;
    }

    public boolean resetPasswordOnProfile(UserEntity currentProfile, String newPassword) {

        if (currentProfile == null) {
            return false;
        }

        // Hash da nova password
        String hashedPassword = PasswordUtil.hashPassword(newPassword);
        currentProfile.setPassword(hashedPassword);  // Armazenar a password com hash

        userDao.save(currentProfile);  // Guardar as alterações na base de dados
        return true;
    }

    public boolean changePhotographOnProfile(UserEntity currentProfile, String photographUpdated) {

        if (currentProfile == null) {
            return false;
        }

        currentProfile.getProfile().setPhotograph(photographUpdated);
        userDao.save(currentProfile);
        return true;

    }


    public ProfileDto convertToDto(ProfileEntity entity) {
        if (entity == null) return null;

        ProfileDto dto = new ProfileDto();
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setAddress(entity.getAddress());
        dto.setPhone(entity.getPhone());
        dto.setPhotograph(entity.getPhotograph());
        dto.setBio(entity.getBio());
        dto.setProfileComplete(ProfileValidator.isProfileComplete(entity));
        dto.setMissingFields(ProfileValidator.getMissingFields(entity));

        // birthDate pode ser null
        if (entity.getBirthDate() != null) {
            dto.setBirthDate(entity.getBirthDate()); // YYYY-MM-DD
        } else {
            dto.setBirthDate(null);
        }

        if (entity.getUsualWorkplace() != null) {
            dto.setUsualWorkplace(entity.getUsualWorkplace().name()); // devolve em maiúsculas
        } else {
            dto.setUsualWorkplace(null);
        }

        return dto;
    }

}
