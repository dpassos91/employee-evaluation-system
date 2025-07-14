package aor.projetofinal.dao;

import aor.projetofinal.context.RequestContext;
import aor.projetofinal.entity.SettingsEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@ApplicationScoped
public class SettingsDao {

    @PersistenceContext
    private EntityManager em;


    private static final Logger logger = LogManager.getLogger(SettingsDao.class);

    /**
     * Retrieves the system settings entity. Assumes a fixed ID of 1 as initialized in SettingsInitializer.
     *
     * @return The SettingsEntity object with ID 1.
     */
    public SettingsEntity getSettings() {
        SettingsEntity settings = em.find(SettingsEntity.class, 1);

        logger.info("User: {} | IP: {} - Retrieved system settings (ID = 1).",
                RequestContext.getAuthor(), RequestContext.getIp());

        return settings;
    }


    public void save(SettingsEntity settings) {
        em.merge(settings);
    }
}

