package aor.projetofinal.bean;

import aor.projetofinal.Util.JavaConversionUtil;
import aor.projetofinal.Util.StringUtils;
import aor.projetofinal.context.RequestContext;
import aor.projetofinal.dao.ProfileDao;
import aor.projetofinal.dao.RoleDao;
import aor.projetofinal.dao.SessionTokenDao;
import aor.projetofinal.dao.UserDao;
import aor.projetofinal.dto.LoginUserDto;
import aor.projetofinal.dto.ProfileDto;
import aor.projetofinal.dto.UserDto;
import aor.projetofinal.entity.ProfileEntity;
import aor.projetofinal.entity.RoleEntity;
import aor.projetofinal.entity.SessionTokenEntity;
import aor.projetofinal.dto.SessionStatusDto;
import aor.projetofinal.entity.UserEntity;
import aor.projetofinal.entity.enums.UsualWorkPlaceType;
import aor.projetofinal.exception.EmailAlreadyExistsException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Stateless
public class UserBean implements Serializable {


    @Inject
    JavaConversionUtil javaConversionUtil;


    private static final Logger logger = LogManager.getLogger(UserBean.class);

    @Inject
    private UserDao userDao;

    @Inject
    private RoleDao roleDao;

    @Inject
    private SessionTokenDao sessionTokenDao;

    @Inject
    private ProfileDao profileDao;


    @EJB
    SettingsBean settingsBean;


    public boolean authorization(String sessionTokenValue) {
        logger.info("Pedido de autorização recebido");

        SessionTokenEntity sessionToken = sessionTokenDao.findBySessionToken(sessionTokenValue);
        if (sessionToken == null) {
            logger.warn("Autorização recusada: token inexistente ou inválido.");
            return false;
        }

        // Verifica se o sessionToken está expirado
        if (sessionToken.getExpiryDate() != null && sessionToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            logger.warn("Autorização recusada: token expirado.");
            return false;
        }

        UserEntity user = sessionToken.getUser();

        if (user == null || !user.isActive()) {
            logger.warn("Autorização recusada: utilizador inexistente ou inativo.");
            return false;
        }

        logger.info("Autorização concedida com sucesso para o utilizador: {}", user.getEmail());
        return true;
    }

    // Verificar se a password inserida corresponde ao hash armazenado
    public static boolean checkPassword(String rawPassword, String hashedPassword) {
        logger.info("User: {} | IP: {} - Checking password.", RequestContext.getAuthor(), RequestContext.getIp());
        boolean match = BCrypt.checkpw(rawPassword, hashedPassword);
        logger.info("User: {} | IP: {} - Password match: {}", RequestContext.getAuthor(), RequestContext.getIp(), match);
        return match;
    }

    public boolean confirmAccount(String confirmToken) {
        UserEntity user = userDao.findUserByConfirmToken(confirmToken);

        if (user == null || user.isConfirmed()) {
            return false; // conta do user já foi autenticada ou user ou token é inválido
        }

        // Verificar se o token de confirmação expirou , e se sim gerar um novo
        if (user.getConfirmationTokenExpiry() != null && user.getConfirmationTokenExpiry().isBefore(LocalDateTime.now())) {
            String newAccountConfirmToken  = generateConfirmToken(user.getEmail());
            user.setConfirmationToken(newAccountConfirmToken);

            int lifetimeMinutes = settingsBean.getConfirmationTokenTimeout();
            user.setConfirmationTokenExpiry(LocalDateTime.now().plusMinutes(lifetimeMinutes));
            userDao.save(user);

            logger.info("Token expirado. A gerar novo token de confirmação para {}", user.getEmail());
            return false; // Token expirado
        }



        user.setConfirmed(true); // verificar/ativar conta

        user.setConfirmationToken(null); // descartar token após confirmacao de conta
        user.setConfirmationTokenExpiry(null);
        userDao.save(user);

        logger.info("Conta confirmada com sucesso para user: {}", user.getEmail());
        return true;
    }

    public UserDto findUserByEmail(String email){
        logger.info("Inicio findByEmail email : {}", email);

        UserEntity userEntity = userDao.findByEmail(email);
        if (userEntity == null) {
            logger.warn("Utilizador não encotrado");
            return null;
        }

        logger.info("Utilizador com email {} encontrado", userEntity.getEmail());
        return javaConversionUtil.convertUserEntityToUserDto(userEntity);
    }

    public UserEntity findUserEntityByEmail(String email) {
        return userDao.findByEmail(email);
    }

    public String generateConfirmToken(String email) {
        UserEntity user = userDao.findByEmail(email);

        if (user == null) {
            logger.warn("Utilizador {} não encontrado para gerar token", email);
            return null;
        }

        // Se a conta já estiver verificada, não precisa de token
        if (user.isConfirmed()) {
            logger.info("Utilizador {} já está verificado. Não é necessário token.", email);
            return null;
        }


        // Gerar novo token
        String newConfirmationToken = UUID.randomUUID().toString();
        user.setConfirmationToken(newConfirmationToken);

        logger.info("Novo token de confirmação de conta gerado para {}", email);
        return newConfirmationToken;
    }

    public String generateRecoveryToken(String email) {
        UserEntity user = userDao.findByEmail(email);

        if (user == null) {
            logger.warn("Utilizador {} não encontrado para gerar token de recuperação", user.getEmail());
            return null;
        }

        // Se já existir um token válido, substituir por um novo
        LocalDateTime expiry = user.getRecoveryTokenExpiry();
        if (user.getRecoveryToken() != null && expiry != null && expiry.isAfter(LocalDateTime.now())) {
            logger.info("Token de recuperação de pass ainda válido para {}, mas por motivos de segurança a gerar novo token.", user.getEmail());

            String newRecoveryToken = UUID.randomUUID().toString();

            // Define a nova data de expiração
            int lifetimeMinutes = settingsBean.getRecoveryTokenTimeout();
            user.setRecoveryToken(newRecoveryToken);
            user.setRecoveryTokenExpiry(LocalDateTime.now().plusMinutes(lifetimeMinutes));
            userDao.save(user);

            logger.info("Novo token de recuperação gerado para {}", user.getEmail());
            return newRecoveryToken;
        }

        // Gerar novo token
        logger.info("A gerar token de recuperação de password para {}", user.getEmail());
        String recoveryToken = UUID.randomUUID().toString();
        int lifetimeMinutes = settingsBean.getRecoveryTokenTimeout(); // configurable pelo admin

        user.setRecoveryToken(recoveryToken);
        user.setRecoveryTokenExpiry(LocalDateTime.now().plusMinutes(lifetimeMinutes));
        userDao.save(user);

        logger.info("Novo token de recuperação de password gerado para {}", user.getEmail());
        return recoveryToken;
    }

    public String getConfirmToken(String email) {
        UserEntity user = userDao.findByEmail(email);
        if (user != null) {
            return user.getConfirmationToken();
        }
        return null;
    }


    // Gerar um hash da password
    public String hashPassword(String password) {
        logger.info("User: {} | IP: {} - Hashing password.", RequestContext.getAuthor(), RequestContext.getIp());
        return BCrypt.hashpw(password, BCrypt.gensalt());
        // o valor default em gensalt será 10: significa que o algoritmo de criptografia bcrypt vai iterar 2^10 = 1024 vezes para criar hash
    }

    public boolean isAccountConfirmed(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        UserEntity user = userDao.findByEmail(email);

        if (user == null) {
            return false;
        }

        return user.isConfirmed();
    }

// Method que quando o frontend chama o endpoint para redefinir a password, com token no URL,
// verifica se o token ainda é válido antes de aceitar nova password

    public boolean isRecoveryTokenValid(String recoveryToken) {
        UserEntity user = userDao.findUserByRecoveryToken(recoveryToken);

        if (user == null || user.getRecoveryTokenExpiry() == null) {
            return false;
        }

        return user.getRecoveryTokenExpiry().isAfter(LocalDateTime.now());
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
        sessionTokenEntity.setUser(userEntity);
        sessionTokenEntity.setCreatedAt(LocalDateTime.now());
        sessionTokenEntity.setExpiryDate(LocalDateTime.now().plusMinutes(settingsBean.getSessionTimeoutMinutes()));

        sessionTokenDao.persist(sessionTokenEntity);

        logger.info("Login realizado com sucesso para o utilizador com email {}", userEntity.getEmail());
        return sessionToken;
    }

    public boolean logout(String sessionTokenValue) {
        logger.info("Início de logout");

        SessionTokenEntity sessionTokenEntity = sessionTokenDao.findBySessionToken(sessionTokenValue);

        if (sessionTokenEntity != null) {
            sessionTokenDao.delete(sessionTokenEntity);

            logger.info("Logout efetuado com sucesso para o utilizador: {}", sessionTokenEntity.getUser().getEmail());
            return true;
        }

        logger.warn("Token de sessão não encontrado. Logout não realizado.");
        return false;
    }



    public UserDto registerUser(LoginUserDto loginUserDto) throws EmailAlreadyExistsException {
        logger.info("User: {} | IP: {} - Checking if email {} is already in use.",
                RequestContext.getAuthor(), RequestContext.getIp(), loginUserDto.getEmail());

        // Verificar se email já existe
        if (userDao.findByEmail(loginUserDto.getEmail()) != null) {
            logger.warn("User: {} | IP: {} - Registration failed: email {} already in use.",
                    RequestContext.getAuthor(), RequestContext.getIp(), loginUserDto.getEmail());
            throw new EmailAlreadyExistsException("Email already in use.");
        }

        // Obter role "USER" e garantir que está configurada
        RoleEntity userRole = roleDao.findByName("USER");
        if (userRole == null) {
            logger.error("User: {} | IP: {} - Default role 'USER' not found. Registration aborted.",
                    RequestContext.getAuthor(), RequestContext.getIp());
            throw new IllegalStateException("Default role 'USER' not configured.");
        }

        // Criar novo utilizador com os dados apropriados
        UserEntity user = new UserEntity();
        user.setEmail(loginUserDto.getEmail());
        user.setPassword(hashPassword(loginUserDto.getPassword()));
        user.setConfirmed(false);
        user.setActive(true);
        user.setConfirmationToken(UUID.randomUUID().toString());

        int lifetimeMinutes = settingsBean.getConfirmationTokenTimeout();
        user.setConfirmationTokenExpiry(LocalDateTime.now().plusMinutes(lifetimeMinutes));
        user.setRole(userRole); // atribuir a role USER


        // Persistir
        try {
            userDao.create(user);

            // Criar perfil e associar ao utilizador
            ProfileEntity profile = new ProfileEntity();
            profile.setUser(user);
            user.setProfile(profile);


            profileDao.create(profile);
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

    public boolean resetPasswordWithToken(String forgottenPassToken, String newPassword) {
        // Verificar se o token é válido
        UserEntity user = userDao.findUserByRecoveryToken(forgottenPassToken);

        if (user == null) {
            logger.warn("Token inválido: {}", forgottenPassToken);
            return false;
        }

        // Verificar se o token está expirado
        if (user.getRecoveryTokenExpiry() == null || user.getRecoveryTokenExpiry().isBefore(LocalDateTime.now())) {
            logger.warn("Token expirado para utilizador {}", user.getEmail());
            return false;
        }

        // Hash da nova password
        String hashedPassword = hashPassword(newPassword);
        user.setPassword(hashedPassword);  // Armazenar a password com hash
        user.setRecoveryToken(null);  // Eliminar o token após redefinir a password
        user.setRecoveryTokenExpiry(null);  // Limpar a expiry date do token
        userDao.save(user);  // Guardar as alterações na base de dados

        logger.info("Password redefinida e atualizada com sucesso para o utilizador {}", user.getEmail());
        return true;
    }

    public boolean updateInfo(ProfileDto profileDto, String email) {

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


    profileToUpdate.setNormalizedFirstName(StringUtils.normalize(profileDto.getFirstName()));
    profileToUpdate.setNormalizedLastName(StringUtils.normalize(profileDto.getLastName()));


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

    logger.info("User: {} | IP: {} | Email: {} - Successfully updated profile for user.",
            RequestContext.getAuthor(), RequestContext.getIp(), email);

    return true;
}


    public SessionStatusDto validateAndRefreshSessionToken(String sessionToken) {
        if (sessionToken == null || sessionToken.isEmpty()) {
            return null;
        }

        SessionTokenEntity sessionTokenEntity = sessionTokenDao.findBySessionToken(sessionToken);

        UserEntity user = sessionTokenEntity.getUser();
        if (user == null || sessionTokenEntity.getExpiryDate() == null || sessionTokenEntity.getExpiryDate().isBefore(LocalDateTime.now()) || !user.isActive()) {
            if (user != null) {
                sessionTokenDao.delete(sessionTokenEntity);
            }
            return null;
        }

        // Verificar a diferença entre a data de expiração e a data atual
        int minutesDifference = (int) Duration.between(LocalDateTime.now(), sessionTokenEntity.getExpiryDate()).toMinutes();



        // Renovar tempo de sessão se for igual ou superior ao tempo configurado

        if (minutesDifference <= settingsBean.getSessionTimeoutMinutes()) {
            sessionTokenEntity.setExpiryDate(LocalDateTime.now().plusMinutes(settingsBean.getSessionTimeoutMinutes()));
            sessionTokenDao.save(sessionTokenEntity);}

        return javaConversionUtil.convertSessionTokenEntityToSessionStatusDto(sessionTokenEntity);
    }

}

