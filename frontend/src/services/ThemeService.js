/**
 * Theme service for managing application theme
 * Handles theme preferences, persistence, and system theme detection
 */

export const ThemeService = {
  // Key for storing theme preference in localStorage
  THEME_STORAGE_KEY: 'app-theme-preference',
  
  // CSS class that triggers dark mode (must match with darkModeSelector in PrimeVue config)
  DARK_MODE_CLASS: 'dark-mode',
  
  /**
   * Initialize theme based on saved preference or system preference
   */
  init() {
    // First check if user has previously set a preference
    const savedTheme = localStorage.getItem(this.THEME_STORAGE_KEY);
    
    if (savedTheme) {
      // Apply saved theme
      this.setTheme(savedTheme);
    } else {
      // Check system preference
      const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
      this.setTheme(prefersDark ? 'dark' : 'light');
    }
    
    // Listen for system theme changes if no user preference is set
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
      if (!localStorage.getItem(this.THEME_STORAGE_KEY)) {
        this.setTheme(e.matches ? 'dark' : 'light');
      }
    });
  },
  
  /**
   * Get current theme
   * @returns {string} 'light' or 'dark'
   */
  getTheme() {
    return document.documentElement.classList.contains(this.DARK_MODE_CLASS) ? 'dark' : 'light';
  },
  
  /**
   * Set theme
   * @param {string} theme - 'light' or 'dark'
   */
  setTheme(theme) {
    if (theme === 'dark') {
      document.documentElement.classList.add(this.DARK_MODE_CLASS);
      // Set meta theme-color for browser UI
      this.updateMetaThemeColor('#0f172a'); // Dark blue background
    } else {
      document.documentElement.classList.remove(this.DARK_MODE_CLASS);
      // Set meta theme-color for browser UI
      this.updateMetaThemeColor('#f8fafc'); // Light background
    }
    
    // Save user preference
    localStorage.setItem(this.THEME_STORAGE_KEY, theme);
    
    // Dispatch event for theme change
    window.dispatchEvent(new CustomEvent('themechange', { detail: { theme } }));
    
    return theme;
  },
  
  /**
   * Toggle between light and dark themes
   * @returns {string} The new theme after toggling
   */
  toggleTheme() {
    const currentTheme = this.getTheme();
    const newTheme = currentTheme === 'light' ? 'dark' : 'light';
    this.setTheme(newTheme);
    return newTheme;
  },
  
  /**
   * Update the meta theme-color tag for browser UI
   * @param {string} color - Color in hex format
   */
  updateMetaThemeColor(color) {
    let metaThemeColor = document.querySelector('meta[name="theme-color"]');
    
    if (!metaThemeColor) {
      metaThemeColor = document.createElement('meta');
      metaThemeColor.name = 'theme-color';
      document.head.appendChild(metaThemeColor);
    }
    
    metaThemeColor.content = color;
  }
};