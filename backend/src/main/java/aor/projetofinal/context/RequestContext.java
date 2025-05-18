package aor.projetofinal.context;

public class RequestContext {

    private static final ThreadLocal<String> ipThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<String> authorThreadLocal = new ThreadLocal<>();

    public static void setIp(String ip) {
        ipThreadLocal.set(ip);
    }

    public static String getIp() {
        return ipThreadLocal.get();
    }

    public static void setAuthor(String author) {
        authorThreadLocal.set(author);
    }

    public static String getAuthor() {
        return authorThreadLocal.get();
    }

    public static void clear() {
        ipThreadLocal.remove();
        authorThreadLocal.remove();
    }
}

