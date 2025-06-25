package aor.projetofinal.context;

public class RequestContext {

    private static final ThreadLocal<String> ipThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<String> authorThreadLocal = new ThreadLocal<>();

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

    public static void clear() {
        ipThreadLocal.remove();
        authorThreadLocal.remove();
    }
}

