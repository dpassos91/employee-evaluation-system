package aor.projetofinal.bean;

import aor.projetofinal.util.JavaConversionUtil;
import aor.projetofinal.util.StringUtils;
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

    /**
     * Validates a session token to determine whether access should be authorized.
     * Logs the request and all authorization outcomes for audit and security purposes.
     *
     * @param sessionTokenValue The value of the session token to validate.
     * @return true if the session token is valid, not expired, and associated with an active user; false otherwise.
     */
    public boolean authorization(String sessionTokenValue) {
        logger.info(
                "User: {} | IP: {} - Authorization request received.",
                RequestContext.getAuthor(),
                RequestContext.getIp()
        );

        SessionTokenEntity sessionToken = sessionTokenDao.findBySessionToken(sessionTokenValue);
        if (sessionToken == null) {
            logger.warn(
                    "User: {} | IP: {} - Authorization denied: session token does not exist or is invalid.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp()
            );
            return false;
        }
        // Verifies whether sessionToken has expired
        if (sessionToken.getExpiryDate() != null && sessionToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            logger.warn(
                    "User: {} | IP: {} - Authorization denied: session token has expired.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp()
            );
            return false;
        }

        UserEntity user = sessionToken.getUser();

        if (user == null || !user.isActive()) {
            logger.warn(
                    "User: {} | IP: {} - Authorization denied: user is null or inactive.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp()
            );
            return false;
        }

        logger.info(
                "User: {} | IP: {} - Authorization granted for user: {}.",
                RequestContext.getAuthor(),
                RequestContext.getIp(),
                user.getEmail()
        );
        return true;
    }


    /**
     * Verifies whether a raw (plain text) password matches the stored hashed password using BCrypt.
     * Logs the verification attempt and whether the passwords matched.
     *
     * @param rawPassword    The plain text password entered by the user.
     * @param hashedPassword The BCrypt-hashed password stored in the database.
     * @return true if the raw password matches the hashed password; false otherwise.
     */
    public static boolean checkPassword(String rawPassword, String hashedPassword) {
        logger.info(
                "User: {} | IP: {} - Checking password.",
                RequestContext.getAuthor(),
                RequestContext.getIp()
        );

        boolean match = BCrypt.checkpw(rawPassword, hashedPassword);

        logger.info(
                "User: {} | IP: {} - Password match: {}",
                RequestContext.getAuthor(),
                RequestContext.getIp(),
                match
        );
        return match;
    }


    /**
     * Confirms a user account using a provided confirmation token.
     * If the token is invalid, already used, or expired, confirmation is denied.
     * If expired, a new token is generated and stored. Logs all key actions for audit tracking.
     *
     * @param confirmToken The token provided to confirm an account.
     * @return true if the account was successfully confirmed; false if the token is invalid, already used, or expired.
     */
    public boolean confirmAccount(String confirmToken) {

        logger.info(
                "User: {} | IP: {} - Account confirmation attempt received.",
                RequestContext.getAuthor(),
                RequestContext.getIp()
        );

        UserEntity user = userDao.findUserByConfirmToken(confirmToken);

        if (user == null || user.isConfirmed()) {
            logger.warn(
                    "User: {} | IP: {} - Account confirmation failed. Token invalid or account already confirmed.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp()
            );
            return false;
        }

        //verifies if confirmation token has expired, and it generates a new one if it has
        if (user.getConfirmationTokenExpiry() != null && user.getConfirmationTokenExpiry().isBefore(LocalDateTime.now())) {
            String newAccountConfirmToken = generateConfirmToken(user.getEmail());
            user.setConfirmationToken(newAccountConfirmToken);

            int lifetimeMinutes = settingsBean.getConfirmationTokenTimeout();
            user.setConfirmationTokenExpiry(LocalDateTime.now().plusMinutes(lifetimeMinutes));
            userDao.save(user);

            logger.warn(
                    "User: {} | IP: {} - Confirmation token expired. Generated new token for user: {}.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    user.getEmail()
            );
            return false; // token expired
        }


        user.setConfirmed(true); // confirm/activate account

        user.setConfirmationToken(null); // delete token after successfull account confirmation
        user.setConfirmationTokenExpiry(null);
        userDao.save(user);

        logger.info(
                "User: {} | IP: {} - Account successfully confirmed for user: {}.",
                RequestContext.getAuthor(),
                RequestContext.getIp(),
                user.getEmail()
        );

        return true;
    }

    /**
     * Finds a user by their email address and converts the result to a UserDto.
     * Logs the lookup attempt and result for audit and debugging purposes.
     *
     * @param email The email address of the user to look up.
     * @return A UserDto if a user with the given email exists; null otherwise.
     */
    public UserDto findUserByEmail(String email) {
        logger.info(
                "User: {} | IP: {} - Attempting to find user by email: {}.",
                RequestContext.getAuthor(),
                RequestContext.getIp(),
                email
        );

        UserEntity userEntity = userDao.findByEmail(email);
        if (userEntity == null) {
            logger.warn(
                    "User: {} | IP: {} - No user found for email: {}.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    email
            );
            return null;
        }

        logger.info(
                "User: {} | IP: {} - User found for email: {}.",
                RequestContext.getAuthor(),
                RequestContext.getIp(),
                userEntity.getEmail()
        );
        return javaConversionUtil.convertUserEntityToUserDto(userEntity);
    }

    /**
     * Retrieves the UserEntity associated with the given email address.
     * Logs the lookup attempt for audit and traceability purposes.
     *
     * @param email The email address of the user to retrieve.
     * @return The UserEntity if found; null if no user exists with the given email.
     */
    public UserEntity findUserEntityByEmail(String email) {
        logger.info(
                "User: {} | IP: {} - Attempting to find UserEntity by email: {}.",
                RequestContext.getAuthor(),
                RequestContext.getIp(),
                email
        );
        UserEntity user = userDao.findByEmail(email);

        if (user == null) {
            logger.warn(
                    "User: {} | IP: {} - No UserEntity found for email: {}.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    email
            );
        } else {
            logger.info(
                    "User: {} | IP: {} - UserEntity found for email: {}.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    email
            );
        }

        return user;
    }


    /**
     * Generates a new confirmation token for the user with the given email address.
     * If the user is not found or already confirmed, no token is generated.
     * Logs the process and results for auditing purposes.
     *
     * @param email The email address of the user for whom to generate a confirmation token.
     * @return The newly generated token as a String, or null if the user does not exist or is already confirmed.
     */
    public String generateConfirmToken(String email) {

        logger.info(
                "User: {} | IP: {} - Request to generate confirmation token for email: {}.",
                RequestContext.getAuthor(),
                RequestContext.getIp(),
                email
        );


        UserEntity user = userDao.findByEmail(email);

        if (user == null) {
            logger.warn(
                    "User: {} | IP: {} - Cannot generate confirmation token: user not found for email: {}.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    email
            );
            return null;
        }

        // If account has been confirmed before, no token will be generated
        if (user.isConfirmed()) {
            logger.info(
                    "User: {} | IP: {} - No token generated: user with email {} is already confirmed.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    email
            );
            return null;
        }


        // Generated a new token for account confirmatiomn
        String newConfirmationToken = UUID.randomUUID().toString();
        user.setConfirmationToken(newConfirmationToken);

        logger.info(
                "User: {} | IP: {} - New confirmation token generated for user with email: {}.",
                RequestContext.getAuthor(),
                RequestContext.getIp(),
                email
        );
        return newConfirmationToken;
    }


    /**
     * Generates a new recovery token for password reset, associated with the user identified by the given email.
     * If a valid token already exists, it is replaced for security issues. Logs all steps and decisions for audit and traceability.
     *
     * @param email The email address of the user for whom to generate a recovery token.
     * @return The newly generated recovery token, or null if the user does not exist.
     */
    public String generateRecoveryToken(String email) {

        logger.info(
                "User: {} | IP: {} - Request to generate password recovery token for email: {}.",
                RequestContext.getAuthor(),
                RequestContext.getIp(),
                email
        );


        UserEntity user = userDao.findByEmail(email);

        if (user == null) {
            logger.warn(
                    "User: {} | IP: {} - Cannot generate recovery token: user not found for email: {}.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    email
            );
            return null;
        }

        // If there already is a valid token, replace by a new one for security reasons
        LocalDateTime expiry = user.getRecoveryTokenExpiry();
        if (user.getRecoveryToken() != null && expiry != null && expiry.isAfter(LocalDateTime.now())) {
            logger.info(
                    "User: {} | IP: {} - A valid recovery token already exists for user: {}. Generating new one for security reasons.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    email
            );

            String newRecoveryToken = UUID.randomUUID().toString();

            // Sets the new expiration date
            int lifetimeMinutes = settingsBean.getRecoveryTokenTimeout();
            user.setRecoveryToken(newRecoveryToken);
            user.setRecoveryTokenExpiry(LocalDateTime.now().plusMinutes(lifetimeMinutes));
            userDao.save(user);

            logger.info(
                    "User: {} | IP: {} - New recovery token generated and saved for user: {}.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    email
            );
            return newRecoveryToken;

        } else {
            // Generate new token for password recovery
            logger.info(
                    "User: {} | IP: {} - No valid recovery token found. Generating a new one for user: {}.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    email
            );
            String recoveryToken = UUID.randomUUID().toString();
            int lifetimeMinutes = settingsBean.getRecoveryTokenTimeout(); // configurable by admin

            user.setRecoveryToken(recoveryToken);
            user.setRecoveryTokenExpiry(LocalDateTime.now().plusMinutes(lifetimeMinutes));
            userDao.save(user);

            logger.info(
                    "User: {} | IP: {} - New recovery token created and saved for user: {}.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    email
            );

            return recoveryToken;
        }
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
            sessionTokenDao.save(sessionTokenEntity);
        }

        return javaConversionUtil.convertSessionTokenEntityToSessionStatusDto(sessionTokenEntity);
    }

}

