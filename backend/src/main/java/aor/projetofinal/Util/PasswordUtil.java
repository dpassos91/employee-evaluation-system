package aor.projetofinal.Util;

import aor.projetofinal.context.RequestContext;
import aor.projetofinal.service.ProfileService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    private static final Logger logger = LogManager.getLogger(ProfileService.class);

    // Gerar um hash da password
    public static String hashPassword(String password) {
        logger.info("User: {} | IP: {} - Hashing password.", RequestContext.getAuthor(), RequestContext.getIp());
        return BCrypt.hashpw(password, BCrypt.gensalt());
        // o valor default em gensalt será 10: significa que o algoritmo de criptografia bcrypt vai iterar 2^10 = 1024 vezes para criar hash
    }

    // Verificar se a password inserida corresponde ao hash armazenado
    public static boolean checkPassword(String rawPassword, String hashedPassword) {
        logger.info("User: {} | IP: {} - Checking password.", RequestContext.getAuthor(), RequestContext.getIp());
        boolean match = BCrypt.checkpw(rawPassword, hashedPassword);
        logger.info("User: {} | IP: {} - Password match: {}", RequestContext.getAuthor(), RequestContext.getIp(), match);
        return match;
    }
}