/**
 * @fileoverview Theme toggle button component for switching between light and dark modes.
 * Provides an accessible button with dynamic icons that integrates with the theme context.
 *
 * @author Philipp Borkovic
 * @version 1.0.0
 * @since 2025-09-15
 */

import React from 'react';
import { useTheme } from 'Frontend/contexts/ThemeContext';
import { FaMoon, FaSun } from 'react-icons/fa';

/**
 * Theme toggle button component that allows users to switch between light and dark modes.
 *
 * Features:
 * - Dynamic icon display (moon for light mode, sun for dark mode)
 * - Accessible with proper ARIA labels and tooltips
 * - Integrates with ThemeContext for state management
 * - Styled with CSS classes for consistent appearance
 *
 * The component automatically:
 * - Shows a moon icon when in light mode (suggesting switch to dark)
 * - Shows a sun icon when in dark mode (suggesting switch to light)
 * - Updates tooltip and aria-label text based on current theme
 * - Triggers theme toggle when clicked
 *
 * @component
 * @returns {JSX.Element} Button element with theme toggle functionality
 *
 * @requires ThemeProvider Must be wrapped in a ThemeProvider component
 * @see {@link useTheme} for the theme hook used internally
 *
 * @author Philipp Borkovic
 */
export const ThemeToggle: React.FC = () => {
  const { theme, toggleTheme } = useTheme();
  const toggleLabel = `Switch to ${theme === 'light' ? 'dark' : 'light'} mode`;

  return (
    <button
      className="theme-toggle"
      onClick={toggleTheme}
      title={toggleLabel}
      aria-label={toggleLabel}
      type="button"
    >
      {theme === 'light' ? (
        <FaMoon className="theme-toggle-icon" />
      ) : (
        <FaSun className="theme-toggle-icon" />
      )}
    </button>
  );
};