import axiosClient from './axiosClient';

// --- Logged-in user ---

export const getMyProfile = () => axiosClient.get('/users/me').then((r) => r.data);

export const updateProfile = (data) => axiosClient.put('/users/me', data).then((r) => r.data);

export const deactivateMyAccount = () =>
  axiosClient.delete('/users/me').then((r) => r.data);

export const changePassword = (data) =>
  axiosClient.put('/users/me/change-password', data).then((r) => r.data);

export const getDashboard = () => axiosClient.get('/users/me/dashboard').then((r) => r.data);

// --- Admin ---

export const getAllUsers = () => axiosClient.get('/users').then((r) => r.data);

export const getUsersByStatus = (status) =>
  axiosClient.get(`/users/status/${status}`).then((r) => r.data);

export const updateUserStatus = (userId, accountStatus) =>
  axiosClient.patch(`/users/${userId}/status`, { accountStatus }).then((r) => r.data);

export const getUserById = (userId) => axiosClient.get(`/users/${userId}`).then((r) => r.data);
