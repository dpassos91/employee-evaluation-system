package aor.projetofinal.dao;


import aor.projetofinal.Util.StringUtils;
import aor.projetofinal.entity.ProfileEntity;
import aor.projetofinal.entity.enums.UsualWorkPlaceType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

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

    public List<ProfileEntity> findProfilesWithFilters(String employeeName, UsualWorkPlaceType workplace, String managerEmail) {
        try { //1=1 permite criar uma query onde se previne que adicionar WHERE a meio da query possa dar erro de sintaxe. Também previne começar uma condição com AND sem uma cláusula anterior.
            StringBuilder jpql = new StringBuilder("SELECT p FROM ProfileEntity p WHERE 1=1");
//StringBuilder acumula as cláusulas WHERE

            String normalizedEmployeeName = null;

            if (employeeName != null && !employeeName.isBlank()) {

                normalizedEmployeeName = StringUtils.normalize(employeeName);
                jpql.append(" AND CONCAT(' ', p.normalizedFirstName, ' ', p.normalizedLastName, ' ') LIKE CONCAT('% ', :employeeName, ' %')");
            }
//workplace é um enum
            if (workplace != null) {
                jpql.append(" AND p.usualWorkplace = :workplace");
            }
//procura-se por managerEmail para obter resultados mais exatos
            if (managerEmail != null && !managerEmail.isBlank()) {
                jpql.append(" AND p.user.manager.email = :managerEmail");
            }

            TypedQuery<ProfileEntity> query = em.createQuery(jpql.toString(), ProfileEntity.class);

            if (employeeName != null && !employeeName.isBlank()) {
                query.setParameter("employeeName", normalizedEmployeeName);
            }

            if (workplace != null) {
                query.setParameter("workplace", workplace);
            }

            if (managerEmail != null && !managerEmail.isBlank()) {
                query.setParameter("managerEmail", managerEmail);
            }

            return query.getResultList();
        } catch (NoResultException e) {
            return Collections.emptyList();
            //a excecao retorna uma colecao vazia
        }

    }


    
        public void save(ProfileEntity profile) {
        em.merge(profile);
    }
}
