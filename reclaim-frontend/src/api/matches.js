import axiosClient from './axiosClient';

export const getMatchesForLostItem = (lostItemId) =>
  axiosClient.get(`/matches/lost-item/${lostItemId}`).then((r) => r.data);

export const getMatchesForFoundItem = (foundItemId) =>
  axiosClient.get(`/matches/found-item/${foundItemId}`).then((r) => r.data);

export const getMatchById = (matchId) =>
  axiosClient.get(`/matches/${matchId}`).then((r) => r.data);

export const confirmMatch = (matchId) =>
  axiosClient.patch(`/matches/${matchId}/confirm`).then((r) => r.data);

export const rejectMatch = (matchId) =>
  axiosClient.patch(`/matches/${matchId}/reject`).then((r) => r.data);
