package aor.projetofinal.dao;


import aor.projetofinal.entity.ProfileEntity;
import aor.projetofinal.entity.UserEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class ProfileDao {

    @PersistenceContext
    private EntityManager em;

    public void saveOrUpdateProfile(ProfileEntity profile, UserEntity user) {
        profile.setUser(user);
        user.setProfile(profile);

        if (em.contains(profile)) {
            // Já está gerido, não precisa de fazer nada — será atualizado no commit
            return;
        }

        // Verificar se já existe na base de dados
        ProfileEntity existingProfile = em.find(ProfileEntity.class, user);
        if (existingProfile == null) {
            em.persist(profile); // Novo perfil
        } else {
            em.merge(profile); // Atualização
        }
    }
}
