import { create } from "zustand";
import { notificationAPI } from "../api/notificationAPI";

/**
 * Notification Store
 * Centralized state for notification counts and badge logic.
 */
export const useNotificationStore = create((set, get) => ({
  counts: {
    MESSAGE: 0,
    ALERT: 0,
    SYSTEM: 0,
    WARNING: 0,
  },
  notifications: [],
  isLoading: false,
  error: null,

  /** Fetches unread notification counts by type and updates the store. */
  fetchCounts: async () => {
    set({ isLoading: true, error: null });
    try {
      const counts = await notificationAPI.getUnreadNotificationCountsByType();
      set({ counts, isLoading: false });
    } catch (err) {
      set({ error: err, isLoading: false });
    }
  },

  /** Optionally, fetches all notifications (for a notifications center). */
  fetchNotifications: async () => {
    set({ isLoading: true, error: null });
    try {
      const notifications = await notificationAPI.getNotifications();
      set({ notifications, isLoading: false });
    } catch (err) {
      set({ error: err, isLoading: false });
    }
  },

  /** Marks all notifications as read and resets the counts. */
  markAllAsRead: async () => {
    await notificationAPI.markAllAsRead();
    set({
      counts: {
        MESSAGE: 0,
        ALERT: 0,
        SYSTEM: 0,
        WARNING: 0,
      },
    });
  },

  /** Increments the count for a specific notification type. */
  incrementCount: (type) =>
    set((state) => ({
      counts: {
        ...state.counts,
        [type]: (state.counts[type] || 0) + 1,
      },
    })),

  /** Resets the count for a specific notification type. */
  resetCount: (type) =>
    set((state) => ({
      counts: {
        ...state.counts,
        [type]: 0,
      },
    })),
}));
