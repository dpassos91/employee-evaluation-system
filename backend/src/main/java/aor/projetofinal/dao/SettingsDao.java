package aor.projetofinal.dao;

import aor.projetofinal.entity.SettingsEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class SettingsDao {

    @PersistenceContext
    private EntityManager em;

    public SettingsEntity getSettings() {
        // ID fixo = 1, como definido no SettingsInitializer
        return em.find(SettingsEntity.class, 1);
    }




    public void save(SettingsEntity settings) {
        em.merge(settings);
    }
}

