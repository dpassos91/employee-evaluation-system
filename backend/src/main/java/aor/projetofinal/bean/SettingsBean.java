package aor.projetofinal.bean;

import aor.projetofinal.dao.SettingsDao;
import aor.projetofinal.entity.SettingsEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;

@Stateless
public class SettingsBean implements Serializable {

    private static final Logger logger = LogManager.getLogger(SettingsBean.class);

    @EJB
    SettingsDao settingsDao;


    public SettingsEntity getSettings() {
        logger.info("A obter as definições da aplicação.");

        SettingsEntity settings = settingsDao.getSettings();

        return settings;
    }


    public int getSessionTimeoutMinutes() {
        return getSettings().getSessionTokenTimeout();
    }

    public boolean updateSessionTimeoutMinutes(int minutes) {
        try {
            SettingsEntity settings = getSettings();
            settings.setSessionTokenTimeout(minutes);
            settingsDao.save(settings);
            return true;
        } catch (Exception e) {
            logger.error("Erro ao atualizar o tempo de vida da sessão: ", e);
            return false;
        }
    }



    public int getConfirmationTokenTimeout() {
        return getSettings().getConfirmationTokenTimeout();
    }

    public int getRecoveryTokenTimeout() {
        return getSettings().getRecoveryTokenTimeout();
    }

    public boolean updateConfirmationTokenTimeout(int minutes) {
        try {
            SettingsEntity settings = getSettings();
            settings.setConfirmationTokenTimeout(minutes);
            settingsDao.save(settings);
            return true;
        } catch (Exception e) {
            logger.error("Erro ao atualizar o tempo de vida do token de confirmação: ", e);
            return false;
        }
    }

    public boolean updatePasswordRecoveryTokenTimeout(int minutes) {
        try {
            SettingsEntity settings = getSettings();
            settings.setRecoveryTokenTimeout(minutes);
            settingsDao.save(settings);
            return true;
        } catch (Exception e) {
            logger.error("Erro ao atualizar o tempo de vida do token de esquecimento de password: ", e);
            return false;
        }
    }

    public int getForgottenPasswordTokenTimeout() {
        return getSettings().getRecoveryTokenTimeout();
    }












}
