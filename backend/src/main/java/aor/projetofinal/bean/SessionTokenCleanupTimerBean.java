package aor.projetofinal.bean;

import aor.projetofinal.dao.SessionTokenDao;
import aor.projetofinal.entity.SessionTokenEntity;
import aor.projetofinal.bean.UserBean;
import aor.projetofinal.context.RequestContext;

import jakarta.ejb.Singleton;
import jakarta.ejb.Schedule;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Singleton EJB responsible for periodically cleaning up expired session tokens.
 *
 * This timer runs every 30 seconds and performs the following:
 * - Finds all session tokens that are expired (expiryDate <= now).
 * - Forces logout of the associated users.
 * - Deletes the expired tokens from the database.
 */
@Singleton
public class SessionTokenCleanupTimerBean {

    private static final Logger logger = LogManager.getLogger(SessionTokenCleanupTimerBean.class);

    @Inject
    private SessionTokenDao sessionTokenDao;

    @Inject
    private UserBean userBean;

    /**
     * Scheduled task that runs every 30 seconds to clean up expired session tokens.
     */
    @Schedule(hour = "*", minute = "*", second = "*/30", persistent = false)
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        logger.info("System | IP: {} - Running session token cleanup at {}",
                RequestContext.getIp(), now);

        List<SessionTokenEntity> expiredTokens = sessionTokenDao.findExpiredSessionTokens(now);

        if (expiredTokens != null && !expiredTokens.isEmpty()) {
            for (SessionTokenEntity token : expiredTokens) {
                userBean.forcedLogout(token);
            }
            logger.info("System | IP: {} - Cleaned up {} expired session tokens.",
                    RequestContext.getIp(), expiredTokens.size());
        } else {
            logger.info("System | IP: {} - No expired session tokens found.",
                    RequestContext.getIp());
        }
    }
}
