/**
 * Notification API
 * Centralized functions for notification-related operations.
 * 
 * Usage:
 * import { notificationAPI } from './notificationAPI';
 * await notificationAPI.getNotifications();
 * await notificationAPI.getUnreadNotifications();
 * await notificationAPI.markAllAsRead();
 * await notificationAPI.getUnreadNotificationCountsByType();
 */

import { apiConfig } from './apiConfig.js';

const { apiCall, API_ENDPOINTS } = apiConfig;

/**
 * Fetches all notifications for the authenticated user.
 * @returns {Promise<Array>} List of NotificationDto objects.
 */
const getNotifications = async () => {
  return apiCall(API_ENDPOINTS.notifications.list, {
    method: 'GET',
  });
};

/**
 * Fetches all unread notifications for the authenticated user.
 * @returns {Promise<Array>} List of unread NotificationDto objects.
 */
const getUnreadNotifications = async () => {
  return apiCall(API_ENDPOINTS.notifications.unread, {
    method: 'GET',
  });
};

/**
 * Marks all notifications as read for the authenticated user.
 * @returns {Promise<Object>} API response, usually with number of notifications updated.
 */
const markAllAsRead = async () => {
  return apiCall(API_ENDPOINTS.notifications.markAllAsRead, {
    method: 'PUT',
  });
};

/**
 * Marks all MESSAGE-type notifications as read for the authenticated user.
 * Should be called when the user enters the chat/messages section.
 * @returns {Promise<Object>} API response, usually with number of notifications updated.
 */
const markAllMessageNotificationsAsRead = async () => {
  return apiCall(API_ENDPOINTS.notifications.markAllMessageNotificationsAsRead, {
    method: 'PUT',
  });
};

/**
 * Fetches all unread notifications for the authenticated user, excluding MESSAGE type.
 * @returns {Promise<Array>} List of NotificationDto objects (non-MESSAGE types).
 */
const getUnreadNonMessageNotifications = async () => {
  return apiCall(API_ENDPOINTS.notifications.unreadNonMessage, {
    method: 'GET',
  });
};

/**
 * Gets a map of unread notification counts, grouped by notification type.
 * 
 * Example response:
 * {
 *   MESSAGE: 2,
 *   ALERT: 1,
 *   SYSTEM: 0,
 *   WARNING: 0
 * }
 * 
 * @returns {Promise<Object>} Map of notification type (string) to count (number).
 */
const getUnreadNotificationCountsByType = async () => {
  return apiCall(API_ENDPOINTS.notifications.unreadCountByType, {
    method: 'GET',
  });
};

export const notificationAPI = {
  getNotifications,
  getUnreadNotifications,
  markAllAsRead,
  markAllMessageNotificationsAsRead,
  getUnreadNonMessageNotifications,
  getUnreadNotificationCountsByType,
};