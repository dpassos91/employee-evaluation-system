package aor.projetofinal.context;

import aor.projetofinal.entity.UserEntity;

public class RequestContext {

    private static final ThreadLocal<String> ipThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<String> authorThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<UserEntity> userThreadLocal = new ThreadLocal<>();

    public static void setIp(String ip) {
        ipThreadLocal.set(ip);
    }

    public static String getIp() {
        String ip = ipThreadLocal.get();
        return ip != null ? ip : "Unknown";
    }

    public static void setAuthor(String author) {
        authorThreadLocal.set(author);
    }

    public static String getAuthor() {
        String author = authorThreadLocal.get();
        return author != null ? author : "Anonymous";
    }

        public static void setCurrentUser(UserEntity user) {
        userThreadLocal.set(user);
    }

    public static UserEntity getCurrentUser() {
        return userThreadLocal.get();
    }

    public static void clear() {
        ipThreadLocal.remove();
        authorThreadLocal.remove();
        userThreadLocal.remove();
    }
}

