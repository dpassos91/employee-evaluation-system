package aor.projetofinal.dao;


import aor.projetofinal.util.StringUtils;
import aor.projetofinal.entity.ProfileEntity;
import aor.projetofinal.entity.enums.UsualWorkPlaceEnum;
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

    public long countProfilesWithFilters(String employeeName, UsualWorkPlaceEnum workplace, String managerEmail) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(p) FROM ProfileEntity p WHERE 1=1");

        // Apenas utilizadores com conta confirmada
        jpql.append(" AND p.user.confirmed = true");

        String normalizedEmployeeName = null;

        if (employeeName != null && !employeeName.isBlank()) {
            normalizedEmployeeName = StringUtils.normalize(employeeName);
            jpql.append(" AND CONCAT(' ', p.normalizedFirstName, ' ', p.normalizedLastName, ' ') LIKE CONCAT('% ', :employeeName, ' %')");
        }

        if (workplace != null) {
            jpql.append(" AND p.usualWorkplace = :workplace");
        }

        if (managerEmail != null && !managerEmail.isBlank()) {
            jpql.append(" AND p.user.manager.email = :managerEmail");
        }

        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);

        if (employeeName != null && !employeeName.isBlank()) {
            query.setParameter("employeeName", normalizedEmployeeName);
        }

        if (workplace != null) {
            query.setParameter("workplace", workplace);
        }

        if (managerEmail != null && !managerEmail.isBlank()) {
            query.setParameter("managerEmail", managerEmail);
        }

        return query.getSingleResult();
    }


    public void create(ProfileEntity profile) {
        em.persist(profile);
    }


    //pretende receber email do gestor do frontend, visto que ppode haver gestores com nomes iguais , e o email é unico

    public List<ProfileEntity> findProfilesWithFiltersPaginated(String employeeName, UsualWorkPlaceEnum workplace, String managerEmail, int page) {
        try { //1=1 permite criar uma query onde se previne que adicionar WHERE a meio da query possa dar erro de sintaxe. Também previne começar uma condição com AND sem uma cláusula anterior.
            StringBuilder jpql = new StringBuilder("SELECT p FROM ProfileEntity p WHERE 1=1");
//StringBuilder acumula as cláusulas WHERE

            // Apenas utilizadores com conta confirmada
            jpql.append(" AND p.user.confirmed = true");


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
//ordenar por nome
            jpql.append(" ORDER BY p.firstName ASC, p.lastName ASC");

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

            //users por página; valor por defeito
            int pageSize = 10;

            //define o número do registo de onde começa a página que o utilziador pediu no frontend
            int offset = (page > 0 ? page - 1 : 0) * pageSize;
            query.setFirstResult(offset);
            query.setMaxResults(pageSize);


            return query.getResultList();
        } catch (NoResultException e) {
            return Collections.emptyList();
            //a excecao retorna uma colecao vazia
        }

    }


//para produzir lista de perfis a exportar por Excel/csv
    public List<ProfileEntity> findProfilesWithFilters(String employeeName, UsualWorkPlaceEnum workplace, String managerEmail) {
        try { //1=1 permite criar uma query onde se previne que adicionar WHERE a meio da query possa dar erro de sintaxe. Também previne começar uma condição com AND sem uma cláusula anterior.
            StringBuilder jpql = new StringBuilder("SELECT p FROM ProfileEntity p WHERE 1=1");
//StringBuilder acumula as cláusulas WHERE

            // Apenas utilizadores com conta confirmada
            jpql.append(" AND p.user.confirmed = true");


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

            //ordenar por nome
            jpql.append(" ORDER BY p.firstName ASC, p.lastName ASC");

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
