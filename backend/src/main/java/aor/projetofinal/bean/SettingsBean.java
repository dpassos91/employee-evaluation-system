package aor.projetofinal.bean;

import aor.projetofinal.context.RequestContext;
import aor.projetofinal.dao.SettingsDao;
import aor.projetofinal.entity.SettingsEntity;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;

@Stateless
public class SettingsBean implements Serializable {

    private static final Logger logger = LogManager.getLogger(SettingsBean.class);

    @Inject
    private SettingsDao settingsDao;


    /**
     * Retrieves the configured timeout value (in minutes) for confirmation tokens, used at confirming accounts.
     * Logs the access to this setting for audit purposes.
     *
     * @return The confirmation token timeout value as configured in the system settings.
     */
    public int getConfirmationTokenTimeout() {
        int timeout = getSettings().getConfirmationTokenTimeout();

        logger.info(
                "User: {} | IP: {} - Retrieved confirmation token timeout value: {}.",
                RequestContext.getAuthor(),
                RequestContext.getIp(),
                timeout
        );

        return timeout;
    }


    /**
     * Retrieves the configured timeout value for password recovery tokens.
     * Logs the access to this setting for audit and security traceability.
     *
     * @return The recovery token timeout value as defined in the system settings.
     */
    public int getRecoveryTokenTimeout() {
        int timeout = getSettings().getRecoveryTokenTimeout();

        logger.info(
                "User: {} | IP: {} - Retrieved recovery token timeout value: {}.",
                RequestContext.getAuthor(),
                RequestContext.getIp(),
                timeout
        );

        return timeout;
    }


    /**
     * Retrieves the configured session timeout value in minutes.
     * Logs the access to this setting for audit and session management traceability.
     *
     * @return The session timeout value in minutes as defined in the system settings.
     */
    public int getSessionTimeoutMinutes() {
        int timeout = getSettings().getSessionTokenTimeout();

        logger.info(
                "User: {} | IP: {} - Retrieved session timeout value: {} minutes.",
                RequestContext.getAuthor(),
                RequestContext.getIp(),
                timeout
        );


        return timeout;
    }


    /**
     * Retrieves the system-wide settings entity from the database.
     * Logs the access to application settings for audit purposes.
     *
     * @return The SettingsEntity containing application configuration values.
     */
    public SettingsEntity getSettings() {
        logger.info(
                "User: {} | IP: {} - Accessing application settings.",
                RequestContext.getAuthor(),
                RequestContext.getIp()
        );

        SettingsEntity settings = settingsDao.getSettings();

        return settings;
    }

    /**
     * Updates the confirmation token timeout value in the system settings.
     * Logs the update attempt and handles any errors that occur during the operation.
     *
     * @param minutes The new timeout value (in minutes) to be set for confirmation tokens.
     * @return true if the update was successful, false if an exception occurred.
     */
    public boolean updateConfirmationTokenTimeout(int minutes) {
        try {
            SettingsEntity settings = getSettings();
            settings.setConfirmationTokenTimeout(minutes);
            settingsDao.save(settings);

            logger.info(
                    "User: {} | IP: {} - Successfully updated confirmation token timeout to {} minutes.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    minutes
            );
            return true;
        }
        catch (Exception e) {
            logger.error(
                    "User: {} | IP: {} - Failed to update confirmation token timeout to {} minutes.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    minutes,
                    e
            );
            return false;
        }
    }


    /**
     * Updates the recovery token timeout value in the system settings.
     * Logs the update attempt and handles any errors that occur during the operation.
     *
     * @param minutes The new timeout value (in minutes) to be set for recovery tokens.
     * @return true if the update was successful, false if an exception occurred.
     */
    public boolean updateRecoveryTokenTimeout(int minutes) {
        try {
            SettingsEntity settings = getSettings();
            settings.setRecoveryTokenTimeout(minutes);
            settingsDao.save(settings);
            logger.info(
                    "User: {} | IP: {} - Successfully updated recovery token timeout to {} minutes.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    minutes
            );
            return true;
        } catch (Exception e) {
            logger.error(
                    "User: {} | IP: {} - Failed to update recovery token timeout to {} minutes.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    minutes,
                    e
            );
            return false;
        }
    }



    /**
     * Updates the session timeout value (in minutes) in the system settings.
     * Logs the update attempt and handles any errors that occur during the operation.
     *
     * @param minutes The new session timeout value in minutes.
     * @return true if the update was successful, false if an exception occurred.
     */
    public boolean updateSessionTimeoutMinutes(int minutes) {
        try {
            SettingsEntity settings = getSettings();
            settings.setSessionTokenTimeout(minutes);
            settingsDao.save(settings);

            logger.info(
                    "User: {} | IP: {} - Successfully updated session timeout to {} minutes.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    minutes
            );

            return true;
        } catch (Exception e) {
            logger.error(
                    "User: {} | IP: {} - Failed to update session timeout to {} minutes.",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    minutes,
                    e
            );
            return false;
        }
    }






















}
