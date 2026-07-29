import axiosClient from './axiosClient';

export const submitClaim = (data) => axiosClient.post('/claims', data).then((r) => r.data);

export const getClaimById = (claimId) =>
  axiosClient.get(`/claims/${claimId}`).then((r) => r.data);

export const getMyClaims = () => axiosClient.get('/claims/my-claims').then((r) => r.data);

export const getClaimsByItem = (itemId) =>
  axiosClient.get(`/claims/item/${itemId}`).then((r) => r.data);

export const getClaimsByMatch = (matchId) =>
  axiosClient.get(`/claims/match/${matchId}`).then((r) => r.data);

export const withdrawClaim = (claimId) =>
  axiosClient.patch(`/claims/${claimId}/withdraw`).then((r) => r.data);

export const getOwnershipResponses = (claimId) =>
  axiosClient.get(`/claims/${claimId}/ownership-responses`).then((r) => r.data);
