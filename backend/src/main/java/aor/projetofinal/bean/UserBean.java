package aor.projetofinal.bean;

import aor.projetofinal.context.RequestContext;
import aor.projetofinal.dao.RoleDao;
import aor.projetofinal.dao.UserDao;
import aor.projetofinal.dto.LoginUserDto;
import aor.projetofinal.dto.UserDto;
import aor.projetofinal.entity.RoleEntity;
import aor.projetofinal.entity.SessionTokenEntity;
import aor.projetofinal.entity.UserEntity;
import aor.projetofinal.exception.EmailAlreadyExistsException;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.UUID;

@Stateless
public class UserBean {

    private static final Logger logger = LogManager.getLogger(UserBean.class);

    @Inject
    private UserDao userDao;

    @Inject
    private RoleDao roleDao;

    //@Inject
    //private SettingsDao settingsDao;

    public UserDto registerUser(UserDto userDto) {
        logger.info("User: {} | IP: {} - Checking if email {} is already in use.",
                RequestContext.getAuthor(), RequestContext.getIp(), userDto.getEmail());

        // Verificar se email já existe
        if (userDao.findByEmail(userDto.getEmail()) != null) {
            logger.warn("User: {} | IP: {} - Registration failed: email {} already in use.",
                    RequestContext.getAuthor(), RequestContext.getIp(), userDto.getEmail());
            throw new EmailAlreadyExistsException("Email already in use.");
        }

        // Obter configurações
        //SettingsEntity settings = settingsDao.getSettings();
        //int tokenExpiryMinutes = (settings != null) ? settings.getConfirmTokenTimeoutMinutes() : 60;

        // Obter role "USER" e garantir que está configurada
        RoleEntity userRole = roleDao.findByName("USER");
        if (userRole == null) {
            logger.error("User: {} | IP: {} - Default role 'USER' not found. Registration aborted.",
                    RequestContext.getAuthor(), RequestContext.getIp());
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
            logger.info("User: {} | IP: {} - User successfully registered with email: {}",
                    RequestContext.getAuthor(), RequestContext.getIp(), user.getEmail());
        } catch (Exception e) {
            logger.error("User: {} | IP: {} - Error while registering user: {}",
                    RequestContext.getAuthor(), RequestContext.getIp(), user.getEmail(), e);
            throw e;
        }
        // Converter para DTO e devolver
        return new UserDto(user);
    }

    // Gerar um hash da password
    public String hashPassword(String password) {
        logger.info("User: {} | IP: {} - Hashing password.", RequestContext.getAuthor(), RequestContext.getIp());
        return BCrypt.hashpw(password, BCrypt.gensalt());
        // o valor default em gensalt será 10: significa que o algoritmo de criptografia bcrypt vai iterar 2^10 = 1024 vezes para criar hash
    }

    // Verificar se a password inserida corresponde ao hash armazenado
    public static boolean checkPassword(String rawPassword, String hashedPassword) {
        logger.info("User: {} | IP: {} - Checking password.", RequestContext.getAuthor(), RequestContext.getIp());
        boolean match = BCrypt.checkpw(rawPassword, hashedPassword);
        logger.info("User: {} | IP: {} - Password match: {}", RequestContext.getAuthor(), RequestContext.getIp(), match);
        return match;
    }


    public String login(LoginUserDto logUser) {
        logger.info("Inicio de logIn para utilizador:{}", logUser.getEmail());
        UserEntity userEntity = userDao.findByEmail(logUser.getEmail());

        if (userEntity == null) {
            logger.warn("Utilizador não encontrado para o email:{}", logUser.getEmail());
            return null;
        }

        if (!userEntity.isActive()) {
            logger.warn("Utilizador com email {} está desativado", logUser.getEmail());
            return null;
        }


        // Verificar a password com hash
        if (!checkPassword(logUser.getPassword(), userEntity.getPassword())) {
            logger.warn("Utilizador com email {} inseriu a password incorreta", logUser.getEmail());
            return null;
        }

        SessionTokenEntity sessionTokenEntity = new SessionTokenEntity();
        String sessionToken = UUID.randomUUID().toString();
        sessionTokenEntity.setTokenValue(sessionToken);

        sessionTokenEntity.setCreatedAt(LocalDateTime.now());
        //sessionTokenEntity.setExpiryDate(LocalDateTime.now().plusMinutes(settingsBean.getSessionTimeoutMinutes()));


        logger.info("Login realizado com sucesso para o utilizador com email {}", userEntity.getEmail());
        return sessionToken;
    }







}

