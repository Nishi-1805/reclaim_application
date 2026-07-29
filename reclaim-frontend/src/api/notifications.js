import axiosClient from './axiosClient';

export const getMyNotifications = () => axiosClient.get('/notifications').then((r) => r.data);

export const getUnreadNotifications = () =>
  axiosClient.get('/notifications/unread').then((r) => r.data);

export const getUnreadCount = () =>
  axiosClient.get('/notifications/unread/count').then((r) => r.data);

export const markAsRead = (notificationId) =>
  axiosClient.patch(`/notifications/${notificationId}/read`).then((r) => r.data);

export const markAllAsRead = () =>
  axiosClient.patch('/notifications/read-all').then((r) => r.data);
