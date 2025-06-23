package aor.projetofinal.dao;


import aor.projetofinal.entity.ProfileEntity;
import aor.projetofinal.entity.UserEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.postgresql.shaded.com.ongres.stringprep.Profile;
import aor.projetofinal.util.StringUtils;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class ProfileDao {

    @PersistenceContext
    private EntityManager em;


    public void create(ProfileEntity profile) {
        em.persist(profile);
    }


    //pretende receber email do gestor do frontend, visto que ppode haver gestores com nomes iguais , e o email é unico

    public List<ProfileEntity> findProfilesWithFilters(String employeeName, String workplace, String managerName) {
        try { //1=1 permite criar uma query onde se previne que adicionar WHERE a meio da query possa dar erro de sintaxe. Também previne começar uma condição com AND sem uma cláusula anterior.
            StringBuilder jpql = new StringBuilder("SELECT p FROM ProfileEntity p WHERE 1=1");

            if (employeeName != null && !employeeName.isBlank()) {
                jpql.append(" AND LOWER(CONCAT(' ', p.firstName, ' ', p.lastName, ' ')) LIKE LOWER(CONCAT('% ', :employeeName, ' %'))");
            }

            if (workplace != null && !workplace.isBlank()) {
                jpql.append(" AND LOWER(p.usualWorkplace) = LOWER(:workplace)");
            }

            if (managerName != null && !managerName.isBlank()) {
                jpql.append(" AND LOWER(p.user.manager.email) LIKE LOWER(CONCAT('%', :managerName, '%'))");
            }

            TypedQuery<ProfileEntity> query = em.createQuery(jpql.toString(), ProfileEntity.class);

            if (employeeName != null && !employeeName.isBlank()) {
                query.setParameter("employeeName", employeeName);
            }

            if (workplace != null && !workplace.isBlank()) {
                query.setParameter("workplace", workplace);
            }

            if (managerName != null && !managerName.isBlank()) {
                query.setParameter("managerName", managerName);
            }

            return query.getResultList();
        } catch (NoResultException e) {
            return Collections.emptyList();
        }
    }






    public void save(ProfileEntity profile) {
        em.merge(profile);
    }
}
