package aor.projetofinal.bean;

import aor.projetofinal.dao.RoleDao;
import aor.projetofinal.dao.UserDao;
import aor.projetofinal.dto.UserDto;
import aor.projetofinal.entity.RoleEntity;
import aor.projetofinal.entity.UserEntity;
import aor.projetofinal.exception.EmailAlreadyExistsException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.UUID;

@ApplicationScoped
public class UserBean {

    private static final Logger logger = LogManager.getLogger(UserBean.class);

    @Inject
    private UserDao userDao;

    @Inject
    private RoleDao roleDao;

    //@Inject
    //private SettingsDao settingsDao;

    public UserDto registerUser(UserDto userDto) {
        logger.info("Checking if email {} is already in use.", userDto.getEmail());

        // Verificar se email já existe
        if (userDao.findByEmail(userDto.getEmail()) != null) {
            logger.warn("Registration failed: email {} already in use.", userDto.getEmail());
            throw new EmailAlreadyExistsException("Email already in use.");
        }

        // Obter configurações
        //SettingsEntity settings = settingsDao.getSettings();
        //int tokenExpiryMinutes = (settings != null) ? settings.getConfirmTokenTimeoutMinutes() : 60;

        // Obter role "USER" e garantir que está configurada
        RoleEntity userRole = roleDao.findByName("USER");
        if (userRole == null) {
            logger.error("Default role 'USER' not found. Registration aborted.");
            throw new IllegalStateException("Default role 'USER' not configured.");
        }

        // Criar novo utilizador com os dados apropriados
        UserEntity user = new UserEntity();
        user.setEmail(userDto.getEmail());
        user.setPassword(hashPassword(userDto.getPassword()));
        user.setConfirmed(false);
        user.setActive(true);
        user.setConfirmationToken(UUID.randomUUID().toString());
        //user.setConfirmationTokenExpiry(LocalDateTime.now().plusMinutes(tokenExpiryMinutes));
        user.setRole(userRole); // atribuir a role USER


        // Persistir
        try {
            userDao.create(user);
            logger.info("User successfully registered with email: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Error while registering user: {}", user.getEmail(), e);
            throw e;
        }
        // Converter para DTO e devolver
        return new UserDto(user);
    }

    public String hashPassword(String password) {
        logger.info("Hashing password.");
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
}

