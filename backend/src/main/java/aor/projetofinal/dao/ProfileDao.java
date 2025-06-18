package aor.projetofinal.dao;


import aor.projetofinal.entity.ProfileEntity;
import aor.projetofinal.entity.UserEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.postgresql.shaded.com.ongres.stringprep.Profile;

@ApplicationScoped
public class ProfileDao {

    @PersistenceContext
    private EntityManager em;


    public void create(ProfileEntity profile) {
        em.persist(profile);
    }


    public void save(ProfileEntity profile) {
        em.merge(profile);
    }
}
