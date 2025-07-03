package aor.projetofinal.util;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class to track online users based on active WebSocket connections.
 */
public class OnlineUserTracker {

    // Thread-safe set of user IDs currently online
    private static final Set<Integer> ONLINE_USERS = ConcurrentHashMap.newKeySet();

    /**
     * Marks a user as online.
     * @param userId The user ID
     */
    public static void markOnline(int userId) {
        ONLINE_USERS.add(userId);
    }

    /**
     * Marks a user as offline.
     * @param userId The user ID
     */
    public static void markOffline(int userId) {
        ONLINE_USERS.remove(userId);
    }

    /**
     * Checks if a user is currently online.
     * @param userId The user ID
     * @return true if online, false otherwise
     */
    public static boolean isOnline(int userId) {
        return ONLINE_USERS.contains(userId);
    }
}
