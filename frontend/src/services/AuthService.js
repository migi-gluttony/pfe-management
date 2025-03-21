import ApiService from './ApiService';

/**
 * Service for handling authentication-related API calls
 */
export default {
  /**
   * Register a new user
   * @param {Object} userData - User registration data
   * @returns {Promise} - Promise with registration response
   */
  async register(userData) {
    try {
      return await ApiService.post('/auth/register', userData);
    } catch (error) {
      console.error('Registration error:', error);
      throw error;
    }
  },

  /**
   * Login a user with email and password
   * @param {string} email - User's email
   * @param {string} password - User's password
   * @param {boolean} rememberMe - Whether to remember the user
   * @returns {Promise} - Promise with authentication response
   */
  async login(email, password, rememberMe = false) {
    try {
      const data = await ApiService.post('/auth/login', { 
        email, 
        password, 
        rememberMe 
      });

      // Store token in localStorage/sessionStorage based on remember me option
      if (data.token) {
        if (rememberMe) {
          localStorage.setItem('token', data.token);
          localStorage.setItem('user', JSON.stringify(data.user));
        } else {
          sessionStorage.setItem('token', data.token);
          sessionStorage.setItem('user', JSON.stringify(data.user));
        }
      }

      return data;
    } catch (error) {
      console.error('Login error:', error);
      throw error;
    }
  },

  /**
   * Logout the current user
   */
  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    sessionStorage.removeItem('token');
    sessionStorage.removeItem('user');
    
    // Optionally, you can call a logout endpoint to invalidate the token on the server
    // ApiService.post('/auth/logout').catch(err => console.error('Logout error:', err));
  },

  /**
   * Request password reset link
   * @param {string} email - User's email
   * @returns {Promise} - Promise with request response
   */
  async requestPasswordReset(email) {
    try {
      return await ApiService.post('/auth/forgot-password', { email });
    } catch (error) {
      console.error('Password reset request error:', error);
      throw error;
    }
  },

  /**
   * Reset password with token and new password
   * @param {string} token - Reset password token from email
   * @param {string} newPassword - New password
   * @returns {Promise} - Promise with reset response
   */
  async resetPassword(token, newPassword) {
    try {
      return await ApiService.post('/auth/reset-password', { 
        token, 
        password: newPassword 
      });
    } catch (error) {
      console.error('Password reset error:', error);
      throw error;
    }
  },

  /**
   * Check if user is authenticated
   * @returns {boolean} - True if authenticated
   */
  isAuthenticated() {
    return localStorage.getItem('token') !== null || sessionStorage.getItem('token') !== null;
  },

  /**
   * Get the authentication token
   * @returns {string|null} - The token or null if not authenticated
   */
  getToken() {
    return localStorage.getItem('token') || sessionStorage.getItem('token') || null;
  },
  
  /**
   * Get the current user data
   * @returns {Object|null} - User data or null if not authenticated
   */
  getCurrentUser() {
    const userStr = localStorage.getItem('user') || sessionStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  },
  
  /**
   * Fetch current user's profile from API
   * @returns {Promise} - Promise with user data
   */
  async fetchUserProfile() {
    try {
      return await ApiService.get('/auth/me');
    } catch (error) {
      console.error('Get current user error:', error);
      throw error;
    }
  }
};