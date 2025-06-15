package aor.projetofinal.Util;

import aor.projetofinal.dao.UserDao;
import aor.projetofinal.dto.UserDto;
import aor.projetofinal.entity.UserEntity;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.ArrayList;

@ApplicationScoped
public class JavaConversionUtil {

    @EJB
    UserDao userDao;




    public UserDto convertUserEntityToUserDto(UserEntity userEntity) {
        UserDto userDto = new UserDto();
        userDto.setEmail(userEntity.getEmail());
        userDto.setPassword(userEntity.getPassword());
        userDto.setActive(userEntity.isActive());
        userDto.setVerified(userEntity.isConfirmed());
        userDto.setRegisterDate(userEntity.getCreatedAt());
        userDto.setAccountConfirmToken(userEntity.getConfirmationToken());
        userDto.setAccountConfirmTokenExpiryDate(userEntity.getConfirmationTokenExpiry());
        userDto.setForgottenPassToken(userEntity.getRecoveryToken());
        userDto.setForgottenPassTokenExpiryDate(userEntity.getRecoveryTokenExpiry());


        userDto.setSessionTokenExpiryDate(userEntity.getSessionTokenExpiryDate());

        return userDto;
    }





}
