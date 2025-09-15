/**
 * @fileoverview Barrel exports for context modules.
 * Provides centralized exports for all React contexts used in the application.
 *
 * @author Philipp Borkovic
 * @version 1.0.0
 * @since 2025-09-15
 */

/**
 * Theme management exports.
 * @see {@link ThemeContext} for implementation details
 */
export { ThemeProvider, useTheme } from './ThemeContext';

/**
 * Theme-related type definitions.
 * @see {@link ThemeContext} for implementation details
 */
export type { Theme } from './ThemeContext';