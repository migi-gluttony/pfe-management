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
      const response = await ApiService.post('/auth/register', userData);
      window.dispatchEvent(new CustomEvent('auth-state-changed'));
      return response;
    } catch (error) {
      console.error('Registration error:', error);
      throw error;
    }
  },

  /**
   * Parse JWT token to extract user information
   * @param {string} token - JWT token
   * @returns {Object} - User information from token
   */
  parseToken(token) {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const payload = JSON.parse(window.atob(base64));
      
      return {
        email: payload.sub,
        role: payload.role,
        userId: payload.userId,
        nom: payload.nom,          
        prenom: payload.prenom     
      };
    } catch (e) {
      console.error('Error parsing token:', e);
      return null;
    }
  },

  /**
   * Login a user with email and password
   * @param {string} email - User's email
   * @param {string} password - User's password
   * @param {boolean} rememberMe - Whether to remember the user
   * @returns {Promise} - Promise with authentication response
   */
  async login(email, motDePasse, rememberMe = false) {
    try {
      const data = await ApiService.post('/auth/login', { 
        email,
        motDePasse
      });

      // Store token in localStorage/sessionStorage based on remember me option
      if (data.token) {
        // Parse token to extract user info
        const userInfo = this.parseToken(data.token);
        
        if (rememberMe) {
          localStorage.setItem('token', data.token);
          if (userInfo) {
            localStorage.setItem('user', JSON.stringify(userInfo));
          }
        } else {
          sessionStorage.setItem('token', data.token);
          if (userInfo) {
            sessionStorage.setItem('user', JSON.stringify(userInfo));
          }
        }
      }

      // Dispatch event when authentication state changes
      window.dispatchEvent(new CustomEvent('auth-state-changed'));
      
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
    
    // Dispatch event when authentication state changes
    window.dispatchEvent(new CustomEvent('auth-state-changed'));
  },

  /**
   * Request password reset
   * @param {Object} resetData - Password reset data (email, cni/cne, dateNaissance)
   * @returns {Promise} - Promise with request response
   */
  async requestPasswordReset(resetData) {
    try {
      return await ApiService.post('/auth/reset-password-request', resetData);
    } catch (error) {
      console.error('Password reset request error:', error);
      throw error;
    }
  },

  /**
   * Reset password with token and new password
   * @param {string} token - Reset password token
   * @param {string} newPassword - New password
   * @returns {Promise} - Promise with reset response
   */
  async confirmPasswordReset(token, newPassword) {
    try {
      return await ApiService.post('/auth/reset-password-confirm', { 
        token, 
        newPassword 
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
  }
};