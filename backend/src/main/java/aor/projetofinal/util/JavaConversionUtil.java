package aor.projetofinal.util;

import aor.projetofinal.dao.UserDao;
import aor.projetofinal.dto.ProfileDto;
import aor.projetofinal.dto.SessionStatusDto;
import aor.projetofinal.dto.UserDto;
import aor.projetofinal.entity.ProfileEntity;
import aor.projetofinal.entity.SessionTokenEntity;
import aor.projetofinal.entity.UserEntity;
import aor.projetofinal.entity.enums.UsualWorkPlaceType;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.ArrayList;

@ApplicationScoped
public class JavaConversionUtil {

    @Inject
    UserDao userDao;



    //Converter de UserEntity para SessionStatusDto
    public SessionStatusDto convertSessionTokenEntityToSessionStatusDto(SessionTokenEntity sessionTokenEntity) {
        SessionStatusDto sessionStatusDto = new SessionStatusDto(sessionTokenEntity.getTokenValue(), sessionTokenEntity.getExpiryDate());


        sessionStatusDto.setSessionToken(sessionTokenEntity.getTokenValue());
        sessionStatusDto.setExpiryDate(sessionTokenEntity.getExpiryDate());

        return sessionStatusDto;
    }


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


        //userDto.setSessionTokenExpiryDate(userEntity.getSessionTokenExpiryDate());

        return userDto;
    }



    public ProfileDto convertProfileEntityToProfileDto(ProfileEntity p) {
        ProfileDto profileDto = new ProfileDto();

        profileDto.setFirstName(p.getFirstName());
        profileDto.setLastName(p.getLastName());
        profileDto.setBirthDate(p.getBirthDate());
        profileDto.setAddress(p.getAddress());
        profileDto.setBirthDate(p.getBirthDate());
        profileDto.setPhone(p.getPhone());
        profileDto.setPhotograph(p.getPhotograph());
        profileDto.setBio(p.getBio());

        // Define workplace como string (ex: "LISBOA")
        if (p.getUsualWorkplace() != null) {
            profileDto.setUsualWorkplace(UsualWorkPlaceType.transformToString(p.getUsualWorkplace()));

        } else {
            profileDto.setUsualWorkplace(null);
        }

        if (p.getUser() == null) {
            profileDto.setUser(null);
        } else {
            profileDto.setUser(p.getUser());
        }

        return profileDto;

    }





    public ArrayList<ProfileDto> convertProfileEntityListtoProfileDtoList(ArrayList<ProfileEntity> profileEntityEntities) {
        ArrayList<ProfileDto> profilesDtos = new ArrayList<ProfileDto>();
        for (ProfileEntity p : profileEntityEntities) {
            profilesDtos.add(convertProfileEntityToProfileDto(p));
        }
        return profilesDtos;
    }






}
