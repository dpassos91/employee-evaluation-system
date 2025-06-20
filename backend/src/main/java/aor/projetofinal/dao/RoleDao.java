package aor.projetofinal.dao;

import aor.projetofinal.entity.RoleEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

@ApplicationScoped
public class RoleDao {

    @PersistenceContext
    private EntityManager em;

    public void create(RoleEntity role) {
        em.persist(role);
    }

    public RoleEntity findByName(String name) {
        try {
            TypedQuery<RoleEntity> query = em.createQuery(
                    "SELECT r FROM RoleEntity r WHERE r.name = :name", RoleEntity.class);
            query.setParameter("name", name);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}

