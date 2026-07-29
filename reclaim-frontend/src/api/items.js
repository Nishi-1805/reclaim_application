import axiosClient from './axiosClient';

export const createItem = (data) => axiosClient.post('/items', data).then((r) => r.data);

export const getItemById = (itemId) => axiosClient.get(`/items/${itemId}`).then((r) => r.data);

export const getAllItems = () => axiosClient.get('/items').then((r) => r.data);

export const getMyItems = () => axiosClient.get('/items/my-items').then((r) => r.data);

export const getItemsByType = (itemType) =>
  axiosClient.get(`/items/type/${itemType}`).then((r) => r.data);

export const updateItem = (itemId, data) =>
  axiosClient.put(`/items/${itemId}`, data).then((r) => r.data);

export const deleteItem = (itemId) =>
  axiosClient.delete(`/items/${itemId}`).then((r) => r.data);

// --- Item images ---

export const getImagesByItem = (itemId) =>
  axiosClient.get(`/items/${itemId}/images`).then((r) => r.data);

// URL-based image add (kept for compatibility with backend as-is)
export const addImageToItem = (itemId, imageUrl) =>
  axiosClient.post(`/items/${itemId}/images`, { imageUrl }).then((r) => r.data);

// Multipart file upload (new endpoint we're adding to the backend in this build)
export const uploadItemImage = (itemId, file) => {
  const formData = new FormData();
  formData.append('file', file);
  return axiosClient
    .post(`/items/${itemId}/images/upload`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    .then((r) => r.data);
};

export const deleteItemImage = (itemId, imageId) =>
  axiosClient.delete(`/items/${itemId}/images/${imageId}`).then((r) => r.data);

// --- Ownership questions ---

export const getOwnershipQuestions = (itemId) =>
  axiosClient.get(`/items/${itemId}/ownership-questions`).then((r) => r.data);