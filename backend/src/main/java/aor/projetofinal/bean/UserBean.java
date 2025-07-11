package aor.projetofinal.bean;

import aor.projetofinal.dao.*;
import aor.projetofinal.dto.*;
import aor.projetofinal.entity.*;
import aor.projetofinal.util.JavaConversionUtil;
import aor.projetofinal.util.PasswordUtil;

import aor.projetofinal.context.RequestContext;

import aor.projetofinal.exception.EmailAlreadyExistsException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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

    @Inject
    private EvaluationDao evaluationDao;

    @Inject
    private EvaluationCycleDao evaluationCycleDao;


    @EJB
    SettingsBean settingsBean;


    /**
     * Assigns a manager to a specified user. If the manager has a "USER" role,
     * it promotes them to "MANAGER". If an active evaluation cycle exists, it also
     * updates the evaluator in the corresponding evaluation record.
     *
     * @param userEmail    the email of the user to assign the manager to
     * @param managerEmail the email of the manager to be assigned
     * @return true if the assignment is successful, false otherwise
     */
    public boolean assignManagerToUser(String userEmail, String managerEmail) {
        UserEntity user = userDao.findByEmail(userEmail);
        UserEntity manager = userDao.findByEmail(managerEmail);

        if (user == null || manager == null) {
            logger.warn("User: {} | IP: {} - User or manager not found. Assignment skipped.",
                    RequestContext.getAuthor(), RequestContext.getIp());
            return false;
        }

        if (user.getEmail().equalsIgnoreCase(manager.getEmail())) {
            logger.warn("User: {} | IP: {} - User {} attempted to assign themselves as manager. Operation aborted.",
                    RequestContext.getAuthor(), RequestContext.getIp(), user.getEmail());
            return false;
        }

        // Ensure manager has the 'MANAGER' role; if not, promote them
        if (manager.getRole().getName().equalsIgnoreCase("user")) {
            RoleEntity managerRole = roleDao.findByName("MANAGER");
            if (managerRole != null) {
                manager.setRole(managerRole);
                userDao.save(manager); // persist role change
                logger.info("User: {} | IP: {} - User {} promoted to MANAGER role.",
                        RequestContext.getAuthor(), RequestContext.getIp(), manager.getEmail());
            } else {
                logger.error("User: {} | IP: {} - MANAGER role not found in database. Cannot assign manager.",
                        RequestContext.getAuthor(), RequestContext.getIp());
                return false;
            }
        }

        user.setManager(manager);
        userDao.save(user);

        logger.info("User: {} | IP: {} - Manager {} assigned to user {} successfully.",
                RequestContext.getAuthor(), RequestContext.getIp(), manager.getEmail(), user.getEmail());

        EvaluationCycleEntity activeCycle = evaluationCycleDao.findActiveCycle();
        if (activeCycle != null) {
            EvaluationEntity evaluation = evaluationDao.findEvaluationByCycleAndUser(activeCycle, user);
            if (evaluation != null) {
                evaluation.setEvaluator(manager);
                evaluationDao.save(evaluation);
                logger.info("User: {} | IP: {} - Evaluator updated in active cycle for user {}.",
                        RequestContext.getAuthor(), RequestContext.getIp(), user.getEmail());
            }
        }

        return true;
    }

    /**
     * Assigns a random eligible manager to the given user.
     * Eligible managers are confirmed, active users who are not admins, not the user themselves,
     * and not the user's current manager.
     * If no eligible manager is available, the method returns null.
     * If the selected manager has only USER role, they are promoted to MANAGER automatically.
     * If the user has an active evaluation, the evaluator is updated accordingly.
     *
     * @param userEmail The email of the user to whom a random manager should be assigned.
     * @return The UserEntity of the newly assigned manager, or null if assignment failed.
     */
    public UserEntity assignRandomManagerToUser(String userEmail) {
        UserEntity user = userDao.findByEmail(userEmail);
        if (user == null) {
            logger.warn(
                    "User: {} | IP: {} - Random manager assignment failed: user {} not found.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    userEmail
            );
            return null;
        }

        UserEntity currentManager = user.getManager();


        // Get eligible managers (confirmed, active, non-admin, not the user or current manager)
        List<UserEntity> eligibleManagers = userDao.findSuitableManager(user, currentManager);
        if (eligibleManagers.isEmpty()) {
            logger.warn(
                    "User: {} | IP: {} - No eligible managers available to assign to user {}.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    userEmail
            );
            return null;
        }

        // Select a random eligible manager
        UserEntity chosenManager = eligibleManagers.get(new Random().nextInt(eligibleManagers.size()));

        // Promote to MANAGER if still USER
        if (chosenManager.getRole().getName().equalsIgnoreCase("user")) {
            RoleEntity managerRole = roleDao.findByName("MANAGER");
            if (managerRole != null) {
                chosenManager.setRole(managerRole);
                userDao.save(chosenManager);
                logger.info(
                        "User: {} | IP: {} - User {} promoted to MANAGER for random assignment.",
                        RequestContext.getAuthor(),
                        RequestContext.getIp(),
                        chosenManager.getEmail()
                );
            } else {
                logger.error(
                        "User: {} | IP: {} - Role 'MANAGER' not found. Cannot promote {}.",
                        RequestContext.getAuthor(),
                        RequestContext.getIp(),
                        chosenManager.getEmail()
                );
                return null;
            }
        }

        // Assign manager to user
        user.setManager(chosenManager);
        userDao.save(user);

        logger.info(
                "User: {} | IP: {} - User {} assigned to random manager {}.",
                RequestContext.getAuthor(),
                RequestContext.getIp(),
                user.getEmail(),
                chosenManager.getEmail()
        );

        // Update evaluation if in active cycle
        EvaluationCycleEntity activeCycle = evaluationCycleDao.findActiveCycle();
        if (activeCycle != null) {
            EvaluationEntity evaluation = evaluationDao.findEvaluationByCycleAndUser(activeCycle, user);
            if (evaluation != null) {
                evaluation.setEvaluator(chosenManager);
                evaluationDao.save(evaluation);
                logger.info(
                        "User: {} | IP: {} - Evaluation evaluator updated to {} for user {} in active cycle.",
                        RequestContext.getAuthor(),
                        RequestContext.getIp(),
                        chosenManager.getEmail(),
                        user.getEmail()
                );
            }
        }

        return chosenManager;
    }


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

        UserEntity user = userDao.findByConfirmToken(confirmToken);

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


    /**
     * Retrieves a list of non-admin, active and confirmed users to populate the manager assignment dropdown menu.
     * This is intended for admin use when assigning managers to users.
     * Only users who have a valid profile are included in the response.
     *
     * @return A list of UsersDropdownMenuDto containing email, first name, and last name for each eligible user.
     */
    public List<UsersDropdownMenuDto> getUsersForManagerDropdownMenu() {
        logger.info("User: {} | IP: {} - Fetching users for manager dropdown menu.",
                RequestContext.getAuthor(), RequestContext.getIp());

        List<UserEntity> users = userDao.findNonAdminActiveConfirmedUsersForDropDownMenu();
        List<UsersDropdownMenuDto> dropdownMenuList = new ArrayList<>();

        for (UserEntity user : users) {
            if (user.getProfile() != null) {
                dropdownMenuList.add(new UsersDropdownMenuDto(
                        user.getEmail(),
                        user.getProfile().getFirstName(),
                        user.getProfile().getLastName()
                ));
            } else {
                logger.warn("User: {} | IP: {} - Skipped user {} due to missing profile.",
                        RequestContext.getAuthor(), RequestContext.getIp(), user.getEmail());
            }
        }

        logger.info("User: {} | IP: {} - Found {} eligible users for dropdown menu.",
                RequestContext.getAuthor(), RequestContext.getIp(), dropdownMenuList.size());

        return dropdownMenuList;
    }


    /**
     * Hashes the given plain text password using the BCrypt algorithm.
     * Logs the operation for traceability. The default BCrypt strength is 10 (2^10 = 1024 iterations).
     *
     * @param password The raw password to be hashed.
     * @return The hashed password string.
     */
    public String hashPassword(String password) {
        logger.info(
                "User: {} | IP: {} - Hashing password.",
                RequestContext.getAuthor(),
                RequestContext.getIp()
        );
        return BCrypt.hashpw(password, BCrypt.gensalt());
        // default value at gensalt will be 10: it means the cryptography algorithm bcrypt is gonna iterate 2^10 = 1024 times in order to hash
    }


    /**
     * Checks whether the user account associated with the given email is confirmed.
     * Logs the check request and relevant outcomes for audit and debugging purposes.
     *
     * @param email The email address of the user to check.
     * @return true if the account exists and is confirmed; false otherwise.
     */
    public boolean isAccountConfirmed(String email) {

        logger.info(
                "User: {} | IP: {} - Checking if account is confirmed for email: {}.",
                RequestContext.getAuthor(),
                RequestContext.getIp(),
                email
        );

        if (email == null || email.trim().isEmpty()) {
            logger.warn(
                    "User: {} | IP: {} - Cannot check confirmation status: email is null or empty.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp()
            );
            return false;
        }

        UserEntity user = userDao.findByEmail(email);

        if (user == null) {
            logger.warn(
                    "User: {} | IP: {} - Cannot check confirmation status: user not found for email: {}.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    email
            );
            return false;
        }


        logger.info(
                "User: {} | IP: {} - Account confirmation status for {}: {}.",
                RequestContext.getAuthor(),
                RequestContext.getIp(),
                email,
                user.isConfirmed()
        );


        return user.isConfirmed();
    }


    /**
     * Validates whether the given password recovery token is still active.
     * Logs the validation attempt and outcome for audit purposes.
     *
     * @param recoveryToken The password recovery token to validate.
     * @return true if the token exists and is not expired; false otherwise.
     */
    public boolean isRecoveryTokenValid(String recoveryToken) {

        logger.info(
                "User: {} | IP: {} - Checking validity of recovery token: {}.",
                RequestContext.getAuthor(),
                RequestContext.getIp(),
                recoveryToken
        );

        UserEntity user = userDao.findByRecoveryToken(recoveryToken);

        if (user == null || user.getRecoveryTokenExpiry() == null) {
            logger.warn(
                    "User: {} | IP: {} - Invalid or unknown recovery token: {}.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    recoveryToken
            );
            return false;
        }

        boolean valid = user.getRecoveryTokenExpiry().isAfter(LocalDateTime.now());

        logger.info(
                "User: {} | IP: {} - Recovery token {} is {}.",
                RequestContext.getAuthor(),
                RequestContext.getIp(),
                recoveryToken,
                valid ? "valid" : "expired"
        );

        return valid;
    }


    /**
     * Retrieves a list of confirmed users who do not have a manager assigned.
     * Converts them into DTOs and returns them in a wrapper object that includes the count.
     *
     * @return UsersWithoutManagerDto containing the list and count of users without a manager.
     */
    public UsersWithoutManagerDto listConfirmedUsersWithoutManager() {
        logger.info(
                "User: {} | IP: {} - Attempting to retrieve confirmed users without assigned managers.",
                RequestContext.getAuthor(),
                RequestContext.getIp()
        );

        List<UserEntity> users = userDao.findConfirmedUsersWithoutManager();

        if (users == null) {
            users = new ArrayList<>();
            logger.warn(
                    "User: {} | IP: {} - UserDao returned null while retrieving confirmed users without managers.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp()
            );
        }

        List<UserDto> userDtos = new ArrayList<>();
        for (UserEntity user : users) {
            UserDto dto = javaConversionUtil.convertUserEntityToUserDto(user);
            if (dto != null) {
                userDtos.add(dto);
            } else {
                logger.warn(
                        "User: {} | IP: {} - Failed to convert UserEntity to UserDto. User ID: {}.",
                        RequestContext.getAuthor(),
                        RequestContext.getIp(),
                        user.getId()
                );
            }
        }

        if (userDtos.isEmpty()) {
            logger.info(
                    "User: {} | IP: {} - No confirmed users found without a manager.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp()
            );
        } else {
            logger.info(
                    "User: {} | IP: {} - Found {} confirmed users without assigned managers.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    userDtos.size()
            );
        }

        UsersWithoutManagerDto result = new UsersWithoutManagerDto();
        result.setUsersWithoutManager(userDtos);
        result.setNumberOfUsersWithoutManager(userDtos.size());

        return result;
    }


    public UsersManagingThemselvesDto listUsersManagingThemselves() {
        List<UserEntity> selfManagedUsers = userDao.findUsersManagingThemselves();

        List<UserDto> userDtos = new ArrayList<>();
        for (UserEntity user : selfManagedUsers) {
            userDtos.add(JavaConversionUtil.convertUserEntityToUserDto(user));
        }

        UsersManagingThemselvesDto dto = new UsersManagingThemselvesDto();
        dto.setUsers(userDtos);
        dto.setNumberOfUsers(userDtos.size());

        return dto;
    }


    /**
     * Authenticates a user based on provided login credentials and generates a new session token if successful.
     * Logs every authentication step and outcome for audit and security purposes.
     *
     * @param logUser A LoginUserDto containing the email and raw password entered by the user.
     * @return A session token string if login is successful; null otherwise.
     */
    public String login(LoginUserDto logUser) {
        logger.info(
                "User: {} | IP: {} - Login attempt received for email: {}.",
                RequestContext.getAuthor(),
                RequestContext.getIp(),
                logUser.getEmail()
        );

        UserEntity userEntity = userDao.findByEmail(logUser.getEmail());

        if (userEntity == null) {
            logger.warn(
                    "User: {} | IP: {} - Login failed: no user found for email: {}.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    logUser.getEmail()
            );
            return null;
        }

        if (!userEntity.isActive()) {
            logger.warn(
                    "User: {} | IP: {} - Login failed: user with email {} is deactivated.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    logUser.getEmail()
            );
            return null;
        }


        // Verifies hashed password
        if (!checkPassword(logUser.getPassword(), userEntity.getPassword())) {
            logger.warn(
                    "User: {} | IP: {} - Login failed: incorrect password for user with email {}.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    logUser.getEmail()
            );
            return null;
        }

        RequestContext.setAuthor(userEntity.getEmail());

        SessionTokenEntity sessionTokenEntity = new SessionTokenEntity();
        String sessionToken = UUID.randomUUID().toString();

        sessionTokenEntity.setTokenValue(sessionToken);
        sessionTokenEntity.setUser(userEntity);
        sessionTokenEntity.setCreatedAt(LocalDateTime.now());
        sessionTokenEntity.setExpiryDate(LocalDateTime.now().plusMinutes(settingsBean.getSessionTimeoutMinutes()));

        sessionTokenDao.persist(sessionTokenEntity);

        logger.info(
                "User: {} | IP: {} - Login successful. Session token generated for user: {}.",
                RequestContext.getAuthor(),
                RequestContext.getIp(),
                userEntity.getEmail()
        );

        return sessionToken;
    }

    /**
     * Logs out a user by invalidating the session associated with the provided token.
     * Logs both successful and failed logout attempts for audit purposes.
     *
     * @param sessionTokenValue The session token identifying the user's session.
     * @return true if the session was found and removed; false if the token was invalid or not found.
     */
    public boolean logout(String sessionTokenValue) {

        logger.info(
                "User: {} | IP: {} - Logout attempt received for session token.",
                RequestContext.getAuthor(),
                RequestContext.getIp()
        );

        SessionTokenEntity sessionTokenEntity = sessionTokenDao.findBySessionToken(sessionTokenValue);

        if (sessionTokenEntity != null) {
            sessionTokenDao.delete(sessionTokenEntity);

            logger.info(
                    "User: {} | IP: {} - Logout successful for user: {}.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    sessionTokenEntity.getUser().getEmail()
            );
            RequestContext.clear();
            return true;
        }

        logger.warn(
                "User: {} | IP: {} - Logout failed: session token not found.",
                RequestContext.getAuthor(),
                RequestContext.getIp()
        );
        return false;
    }



    /**
     * Promotes a specified user to ADMIN role. This action is restricted to existing admins.
     * Upon promotion:
     * - The user's role is updated to ADMIN;
     * - Any users under their management are reassigned to random eligible managers;
     * - If the user was being evaluated in the active cycle, that evaluation is deleted.
     *
     * @param admin           The admin performing the promotion.
     * @param emailToPromote  The email of the user to promote to admin.
     * @return true if promotion was successful or user is already admin, false if validation failed.
     */
    public boolean promoteUserToAdmin(UserEntity admin, String emailToPromote) {
        // Ensure current user has admin privileges
        if (!admin.getRole().getName().equalsIgnoreCase("admin")) {
            logger.warn(
                    "User: {} | IP: {} - Unauthorized attempt to promote {}.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    emailToPromote
            );
            return false;
        }

        // 2. Find the target user
        UserEntity user = userDao.findByEmail(emailToPromote);
        if (user == null) {
            logger.warn(
                    "User: {} | IP: {} - Promotion failed: user with email {} not found.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    emailToPromote
            );
            return false;
        }

        // Skip if already admin
        if (user.getRole().getName().equalsIgnoreCase("admin")) {
            logger.info(
                    "User: {} | IP: {} - User {} is already an admin. No changes made.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    emailToPromote
            );
            return true;
        }


        // Promote to ADMIN role
        user.setRole(roleDao.findByName("ADMIN")); // Assume que tens RoleDao
        userDao.save(user);
        logger.info(
                "User: {} | IP: {} - User {} successfully promoted to ADMIN.",
                RequestContext.getAuthor(),
                RequestContext.getIp(),
                emailToPromote
        );

        // Reassign users previously managed by the promoted user
        List<UserEntity> managedUsers = userDao.findUsersByManager(user);
        for (UserEntity managed : managedUsers) {
            UserEntity newManager = assignRandomManagerToUser(managed.getEmail());
            if (newManager == null) {
                logger.warn(
                        "User: {} | IP: {} - Failed to assign new manager to {} after promotion.",
                        RequestContext.getAuthor(),
                        RequestContext.getIp(),
                        managed.getEmail()
                );
            } else {
                logger.info(
                        "User: {} | IP: {} - User {} reassigned to new manager {}.",
                        RequestContext.getAuthor(),
                        RequestContext.getIp(),
                        managed.getEmail(),
                        newManager.getEmail()
                );
            }
        }
        logger.info(
                "User: {} | IP: {} - {} users were reassigned after {} lost management responsibilities.",
                RequestContext.getAuthor(),
                RequestContext.getIp(),
                managedUsers.size(),
                emailToPromote
        );

        // Remove evaluation if user is being evaluated in the active cycle
        EvaluationCycleEntity activeCycle = evaluationCycleDao.findActiveCycle();
        if (activeCycle != null) {
            EvaluationEntity evaluation = evaluationDao.findEvaluationByCycleAndUser(activeCycle, user);
            if (evaluation != null) {
                evaluationDao.deleteEvaluation(evaluation);
                logger.info(
                        "User: {} | IP: {} - Evaluation of {} in active cycle {} was deleted due to promotion.",
                        RequestContext.getAuthor(),
                        RequestContext.getIp(),
                        emailToPromote,
                        activeCycle.getId()
                );
            }
        }

        return true;
    }


    /**
     * Registers a new user using the provided login credentials.
     * Performs validation against existing emails, assigns the default role, creates a user profile,
     * and generates a confirmation token. Logs all steps and handles registration errors.
     *
     * @param loginUserDto The user's registration credentials (email and raw password).
     * @return A UserDto representing the newly created user.
     * @throws EmailAlreadyExistsException if the provided email is already associated with an existing user.
     * @throws IllegalStateException       if the default "USER" role is not configured in the system.
     */
    public UserDto registerUser(LoginUserDto loginUserDto) throws EmailAlreadyExistsException {
        logger.info(
                "User: {} | IP: {} - Checking if email {} is already in use.",
                RequestContext.getAuthor(),
                RequestContext.getIp(),
                loginUserDto.getEmail()
        );

        // Verify if this email already exists
        if (userDao.findByEmail(loginUserDto.getEmail()) != null) {
            logger.warn(
                    "User: {} | IP: {} - Registration failed: email {} already in use.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    loginUserDto.getEmail()
            );
            throw new EmailAlreadyExistsException("Email already in use.");
        }

        // Get role "USER" and make sure it is configured correctly
        RoleEntity userRole = roleDao.findByName("USER");
        if (userRole == null) {
            logger.error(
                    "User: {} | IP: {} - Default role 'USER' not found. Registration aborted.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp()
            );
            throw new IllegalStateException("Default role 'USER' not configured.");
        }

        // Create new user
        UserEntity user = new UserEntity();
        user.setEmail(loginUserDto.getEmail());
        user.setPassword(hashPassword(loginUserDto.getPassword()));
        user.setConfirmed(false);
        user.setActive(true);
        user.setConfirmationToken(UUID.randomUUID().toString());

        int lifetimeMinutes = settingsBean.getConfirmationTokenTimeout();
        user.setConfirmationTokenExpiry(LocalDateTime.now().plusMinutes(lifetimeMinutes));
        user.setRole(userRole); // name with the role "User"


        // Persist at database
        try {
            userDao.create(user);

            // Create profile and associate to user
            ProfileEntity profile = new ProfileEntity();
            profile.setUser(user);
            user.setProfile(profile);


            profileDao.create(profile);
            logger.info(
                    "User: {} | IP: {} - User successfully registered with email: {}.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    user.getEmail()
            );
        } catch (Exception e) {
            logger.error(
                    "User: {} | IP: {} - Error while registering user: {}.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    loginUserDto.getEmail(),
                    e
            );
            throw e;
        }
        // Convert to userDto and return
        return new UserDto(user);
    }

    /**
     * Resets the password of the given user profile.
     *
     * @param currentProfile The UserEntity whose password is to be reset.
     * @param newPassword    The new plain text password to be hashed and stored.
     * @return true if the password was reset successfully, false if the user is null.
     */
    public boolean resetPasswordOnProfile(UserEntity currentProfile, String newPassword) {
        String email = currentProfile != null ? currentProfile.getEmail() : "unknown";
        if (currentProfile == null) {
            logger.warn("User: {} | IP: {} | Email: {} - Attempted to reset password for null user. Operation aborted.",
                    RequestContext.getAuthor(), RequestContext.getIp(), email);
            return false;
        }
        String hashedPassword = PasswordUtil.hashPassword(newPassword);
        currentProfile.setPassword(hashedPassword);
        userDao.save(currentProfile);
        logger.info("User: {} | IP: {} | Email: {} - Successfully reset password.",
                RequestContext.getAuthor(), RequestContext.getIp(), email);
        return true;
    }

    /**
     * Resets a user's password using a valid recovery token.
     * Verifies the token's validity and expiration, hashes the new password,
     * and clears the recovery token upon success. Logs all outcomes for audit and security.
     *
     * @param forgottenPassToken The password recovery token provided by the user.
     * @param newPassword        The new plain text password to be securely hashed and saved.
     * @return true if the password was successfully reset; false otherwise.
     */
    public boolean resetPasswordWithToken(String forgottenPassToken, String newPassword) {

        logger.info(
                "User: {} | IP: {} - Password reset attempt using recovery token: {}.",
                RequestContext.getAuthor(),
                RequestContext.getIp(),
                forgottenPassToken
        );

        // Verifies if recoverytoken is valid
        UserEntity user = userDao.findByRecoveryToken(forgottenPassToken);

        if (user == null) {
            logger.warn(
                    "User: {} | IP: {} - Password reset failed: invalid recovery token: {}.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    forgottenPassToken
            );
            return false;
        }

        // Verifies if recoverytoken is expired
        if (user.getRecoveryTokenExpiry() == null || user.getRecoveryTokenExpiry().isBefore(LocalDateTime.now())) {
            logger.warn(
                    "User: {} | IP: {} - Password reset failed: token expired for user: {}.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    user.getEmail()
            );
            return false;
        }

        // Hashing new password
        String hashedPassword = hashPassword(newPassword);
        user.setPassword(hashedPassword);  // Persist hashed password on DB
        user.setRecoveryToken(null);  // Eliminates token after resetting passwword
        user.setRecoveryTokenExpiry(null);  // Deletes token expiration date
        userDao.save(user);  // Persist every change on database

        logger.info(
                "User: {} | IP: {} - Password successfully reset for user: {}.",
                RequestContext.getAuthor(),
                RequestContext.getIp(),
                user.getEmail()
        );
        return true;
    }


    /**
     * Validates the provided session token and refreshes its expiration if it is still valid.
     * Logs the session validation, expiration checks, and any token refresh activity for auditing purposes.
     *
     * @param sessionToken The session token to validate and potentially refresh.
     * @return A SessionStatusDto representing the current session status, or null if the session is invalid or expired.
     */
    public SessionStatusDto validateAndRefreshSessionToken(String sessionToken) {

        logger.info(
                "User: {} | IP: {} - Validating session token: {}.",
                RequestContext.getAuthor(),
                RequestContext.getIp(),
                sessionToken
        );


        if (sessionToken == null || sessionToken.isEmpty()) {
            logger.warn(
                    "User: {} | IP: {} - Session token is null or empty. Validation aborted.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp()
            );
            return null;
        }

        SessionTokenEntity sessionTokenEntity = sessionTokenDao.findBySessionToken(sessionToken);

        if (sessionTokenEntity == null) {
            logger.warn(
                    "User: {} | IP: {} - Session token not found: {}.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    sessionToken
            );
            return null;
        }


        UserEntity user = sessionTokenEntity.getUser();
        if (user == null || sessionTokenEntity.getExpiryDate() == null || sessionTokenEntity.getExpiryDate().isBefore(LocalDateTime.now()) || !user.isActive()) {
            if (user != null) {
                sessionTokenDao.delete(sessionTokenEntity);

                logger.warn(
                        "User: {} | IP: {} - Session token expired or invalid. Cleaning up token for user: {}.",
                        RequestContext.getAuthor(),
                        RequestContext.getIp(),
                        user != null ? user.getEmail() : "unknown"
                );

            }
            return null;
        }

        // Assesses the difference between current date and sessionToken's expiration date
        int minutesDifference = (int) Duration.between(LocalDateTime.now(), sessionTokenEntity.getExpiryDate()).toMinutes();

        // Renew's session expiration date if it's equal or greater than the configured time

        if (minutesDifference <= settingsBean.getSessionTimeoutMinutes()) {
            sessionTokenEntity.setExpiryDate(LocalDateTime.now().plusMinutes(settingsBean.getSessionTimeoutMinutes()));
            sessionTokenDao.save(sessionTokenEntity);

            logger.info(
                    "User: {} | IP: {} - Session token refreshed for user: {}.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    user.getEmail()
            );
        } else {
            logger.info(
                    "User: {} | IP: {} - Session token is still valid for user: {}. No refresh needed.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    user.getEmail()
            );
        }

        return javaConversionUtil.convertSessionTokenEntityToSessionStatusDto(sessionTokenEntity);
    }

    /**
     * Finds the user associated with a valid session token.
     * Returns null if token is invalid or user not found.
     */
    public UserEntity findUserBySessionToken(String sessionToken) {
        if (sessionToken == null || sessionToken.isBlank()) {
            logger.warn("User: {} | IP: {} - Attempted to find user by null or blank session token.",
                    RequestContext.getAuthor(), RequestContext.getIp());
            return null;
        }
        UserEntity user = userDao.findBySessionToken(sessionToken);
        if (user == null) {
            logger.warn("User: {} | IP: {} - No user found for session token: {}.",
                    RequestContext.getAuthor(), RequestContext.getIp(), sessionToken);
        } else {
            logger.info("User: {} | IP: {} - User found for session token.",
                    RequestContext.getAuthor(), RequestContext.getIp());
        }
        return user;
    }

    /**
     * Finds the user ID associated with a valid session token.
     * Returns null if token is invalid or user not found.
     */
    public Integer findUserIdBySessionToken(String sessionToken) {
        UserEntity user = findUserBySessionToken(sessionToken);
        if (user != null) {
            logger.info("User: {} | IP: {} - UserId {} found for session token.", RequestContext.getAuthor(), RequestContext.getIp(), user.getId());
            return user.getId();
        } else {
            logger.warn("User: {} | IP: {} - No UserId found for session token: {}.", RequestContext.getAuthor(), RequestContext.getIp(), sessionToken);
            return null;
        }
    }

    /**
     * Checks if the provided raw password matches the current user's password.
     *
     * @param user        The user whose password to check.
     * @param rawPassword The password entered by the user.
     * @return true if the password matches, false otherwise.
     */
    public boolean isPasswordValid(UserEntity user, String rawPassword) {
        if (user == null || user.getPassword() == null || rawPassword == null) {
            return false;
        }
        return checkPassword(rawPassword, user.getPassword());
    }

    /**
 * Updates the role and manager of a given user.
 * Logs all actions and errors using log4j.
 *
 * @param userId        The ID of the user to update.
 * @param newRoleName   The new role name ("USER", "MANAGER", "ADMIN").
 * @param newManagerId  The ID of the new manager (nullable).
 * @throws NotFoundException         If the user does not exist.
 * @throws IllegalArgumentException  If the role does not exist.
 */
@Transactional
public void updateRoleAndManager(int userId, String newRoleName, Integer newManagerId) {
    UserEntity user = userDao.findById(userId);
    if (user == null) {
        logger.warn("User: {} | IP: {} - Tried to update role/manager for non-existing userId={}",
                RequestContext.getAuthor(), RequestContext.getIp(), userId);
        throw new NotFoundException();
    }

    // Update Role
    RoleEntity newRole = roleDao.findByName(newRoleName);
    if (newRole == null) {
        logger.warn("User: {} | IP: {} - Tried to update userId={} to non-existing role '{}'",
                RequestContext.getAuthor(), RequestContext.getIp(), userId, newRoleName);
        throw new IllegalArgumentException("Invalid role: " + newRoleName);
    }
    user.setRole(newRole);

    // Update Manager (can be null)
    UserEntity newManager = (newManagerId != null) ? userDao.findById(newManagerId) : null;
    user.setManager(newManager);

    userDao.save(user);

    logger.info("User: {} | IP: {} - Updated userId={} to role '{}' and managerId={}",
            RequestContext.getAuthor(), RequestContext.getIp(), userId, newRoleName, newManagerId);
}


}

