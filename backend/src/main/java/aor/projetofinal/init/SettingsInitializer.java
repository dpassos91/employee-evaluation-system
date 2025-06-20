package aor.projetofinal.init;

import aor.projetofinal.dao.ProfileDao;
import aor.projetofinal.dao.RoleDao;
import aor.projetofinal.dao.SettingsDao;
import aor.projetofinal.dao.UserDao;
import aor.projetofinal.entity.ProfileEntity;
import aor.projetofinal.entity.RoleEntity;
import aor.projetofinal.entity.SettingsEntity;
import aor.projetofinal.entity.UserEntity;
import aor.projetofinal.bean.UserBean;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

import java.time.LocalDateTime;

@Singleton
@Startup
public class SettingsInitializer {

    @Inject
    SettingsDao settingsDao;

    @Inject
    UserDao userDao;

    @Inject
    UserBean userBean;

    @Inject
    RoleDao roleDao;

    @Inject
    ProfileDao profileDao;

    @Transactional
    @PostConstruct
    public void init() {
        // Criar configurações padrão, se não existirem
        if (settingsDao.getSettings() == null) {
            SettingsEntity settings = new SettingsEntity();
            settings.setId(1); // ID fixo
            settings.setConfirmationTokenTimeout(60);
            settings.setSessionTokenTimeout(30);
            settings.setRecoveryTokenTimeout(15);

            settingsDao.save(settings);
            System.out.println("⚙️ Configuração de sessão criada com valores por omissão.");
        } else {
            System.out.println("⚙️ Configuração de sessão já existe.");
        }

        // Criar roles base, se não existirem
        createRoleIfMissing("ADMIN");
        createRoleIfMissing("MANAGER");
        createRoleIfMissing("USER");

        // Criar admin, se ainda não existir, e atribuir-lhe um perfil
        if (userDao.countAdmins() == 0) {
            UserEntity admin = new UserEntity();
            admin.setEmail("amourinho.grupo7@gmail.com");
            admin.setPassword(userBean.hashPassword("senha123?"));
            admin.setConfirmed(true);
            admin.setActive(true);
            admin.setCreatedAt(LocalDateTime.now());

            RoleEntity adminRole = roleDao.findByName("ADMIN");
            admin.setRole(adminRole);

            userDao.create(admin);

            // Criar perfil e associar ao utilizador
            ProfileEntity profile = new ProfileEntity();
            profile.setUser(admin);
            admin.setProfile(profile);

            profileDao.create(profile);

            System.out.println("🛡️ Administrador criado automaticamente.");
        }
    }

    private void createRoleIfMissing(String roleName) {
        if (roleDao.findByName(roleName) == null) {
            RoleEntity role = new RoleEntity();
            role.setName(roleName);
            roleDao.create(role);
            System.out.println("🔐 Role '" + roleName + "' criada.");
        }
    }
}
