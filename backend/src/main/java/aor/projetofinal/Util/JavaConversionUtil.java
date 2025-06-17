package aor.projetofinal.Util;

import aor.projetofinal.dao.UserDao;
import aor.projetofinal.dto.SessionStatusDto;
import aor.projetofinal.dto.UserDto;
import aor.projetofinal.entity.SessionTokenEntity;
import aor.projetofinal.entity.UserEntity;
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





    public UserDto convertUserEntityToUserDto(UserEntity userEntity) {
        UserDto userDto = new UserDto();
        userDto.setEmail(userEntity.getEmail());
        userDto.setPassword(userEntity.getPassword());
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





}
