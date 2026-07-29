import axios from 'axios';

const axiosClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Attach JWT token to every outgoing request
axiosClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('reclaim_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Normalize error messages coming from the backend's ApiError / GlobalExceptionHandler
axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('reclaim_token');
      localStorage.removeItem('reclaim_user');
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = '/login';
      }
    }

    const data = error.response?.data;

    // Backend's GlobalExceptionHandler returns { message: "Validation failed",
    // validationErrors: { field: "reason" } } for 400s — surface the first
    // field-specific reason instead of the generic top-level message.
    const firstValidationError = data?.validationErrors
      ? Object.values(data.validationErrors)[0]
      : null;

    const message =
      firstValidationError ||
      data?.message ||
      data?.error ||
      (typeof data === 'string' ? data : null) ||
      error.message ||
      'Something went wrong. Please try again.';

    return Promise.reject({ ...error, message });
  }
);

export default axiosClient;
