/**
 * @fileoverview Theme context for managing light/dark mode throughout the application.
 * Provides a React context for theme state management with persistent storage.
 *
 * @author Philipp Borkovic
 * @version 1.0.0
 * @since 2025-09-15
 */

import React, {
  createContext,
  useContext,
  useEffect,
  useState,
  ReactNode
} from 'react';

/**
 * Available theme options for the application.
 * @typedef {'light' | 'dark'} Theme
 */
export type Theme = 'light' | 'dark';

/**
 * Type definition for the theme context value.
 * @interface ThemeContextType
 * @property {Theme} theme - Current active theme
 * @property {() => void} toggleTheme - Function to toggle between light and dark themes
 */
interface ThemeContextType {
  theme: Theme;
  toggleTheme: () => void;
}

/**
 * React context for theme management.
 * @type {React.Context<ThemeContextType | undefined>}
 */
const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

/**
 * Custom hook to access theme context.
 * Must be used within a ThemeProvider component.
 *
 * @function useTheme
 * @returns {ThemeContextType} The theme context value containing current theme and toggle function
 * @throws {Error} When used outside of ThemeProvider
 *
 * @author Philipp Borkovic
 */
export const useTheme = () => {
  const context = useContext(ThemeContext);
  if (!context) {
    throw new Error('useTheme must be used within a ThemeProvider');
  }

  return context;
};

/**
 * Props for the ThemeProvider component.
 * @interface ThemeProviderProps
 * @property {ReactNode} children - Child components to receive theme context
 */
interface ThemeProviderProps {
  children: ReactNode;
}

/**
 * Theme provider component that manages theme state and provides context to child components.
 *
 * Features:
 * - Persists theme selection in localStorage
 * - Applies theme attributes to document element
 * - Provides theme context to all child components
 * - Automatically loads saved theme on initialization
 *
 * @component
 * @param {ThemeProviderProps} props - Component props
 * @param {ReactNode} props.children - Child components to wrap with theme context
 * @returns {JSX.Element} Provider component wrapping children with theme context
 *
 * @author Philipp Borkovic
 */
export const ThemeProvider: React.FC<ThemeProviderProps> = ({ children }) => {
  const [theme, setTheme] = useState<Theme>(() => {
    const savedTheme = localStorage.getItem('theme') as Theme;

    return savedTheme || 'light';
  });

  /**
   * Effect to persist theme changes and apply them to the document.
   * Updates localStorage, document attributes, and CSS classes when theme changes.
   */
  useEffect(() => {
    localStorage.setItem('theme', theme);

    const root = document.documentElement;

    root.setAttribute('theme', theme);
    root.setAttribute('data-theme', theme);

    if (theme === 'dark') {
      root.classList.add('dark');
    } else {
      root.classList.remove('dark');
    }
  }, [theme]);

  /**
   * Toggles between light and dark themes.
   * @function toggleTheme
   * @returns {void}
   */
  const toggleTheme = () => {
    setTheme(prev => prev === 'light' ? 'dark' : 'light');
  };

  return (
    <ThemeContext.Provider value={{ theme, toggleTheme }}>
      {children}
    </ThemeContext.Provider>
  );
};