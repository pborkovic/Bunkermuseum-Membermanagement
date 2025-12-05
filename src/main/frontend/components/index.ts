/**
 * @fileoverview Barrel exports for UI components.
 * Provides centralized exports for all reusable React components.
 *
 * @author Philipp Borkovic
 */

/**
 * Theme-related components.
 * @see {@link ThemeToggle} for theme switching functionality
 */
export { ThemeToggle } from './ThemeToggle';

/**
 * Layout and utility components.
 * @see {@link ViewToolbar} for page header functionality
 * @see {@link Group} for grouping form elements
 */
export { ViewToolbar, Group } from './ViewToolbar';

/**
 * Error handling components.
 * @see {@link ErrorBoundary} for React error boundary functionality
 */
export { ErrorBoundary } from './ErrorBoundary';

/**
 * Component prop type definitions.
 * @see {@link ViewToolbar} for implementation details
 */
export type { ViewToolbarProps } from './ViewToolbar';