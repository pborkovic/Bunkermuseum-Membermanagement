/**
 * Shared TypeScript type definitions for the admin dashboard.
 * Provides type safety and reusability across components.
 *
 * @module admin/types
 *
 * @author Philipp Borkovic
 */

/**
 * Profile form data interface used in settings and user edit forms.
 *
 * @interface ProfileFormData
 * @property {string} name - Full name of the user
 * @property {string} email - Email address
 * @property {string} salutation - Gender/salutation (männlich, weiblich, divers)
 * @property {string} academicTitle - Academic title (Dr., Prof., etc.)
 * @property {string} rank - Military or organizational rank
 * @property {Date | undefined} birthday - Date of birth
 * @property {string} phone - Phone number
 * @property {string} street - Street address with house number
 * @property {string} city - City name
 * @property {string} postalCode - Postal/ZIP code
 */
export interface ProfileFormData {
  name: string;
  email: string;
  salutation: string;
  academicTitle: string;
  rank: string;
  birthday: Date | undefined;
  phone: string;
  street: string;
  city: string;
  postalCode: string;
}

/**
 * Pagination state interface.
 *
 * @interface PaginationState
 * @property {number} currentPage - Current page number (1-indexed)
 * @property {number} totalPages - Total number of pages
 * @property {number} pageSize - Number of items per page
 * @property {number} totalElements - Total number of items across all pages
 */
export interface PaginationState {
  currentPage: number;
  totalPages: number;
  pageSize: number;
  totalElements: number;
}

/**
 * Pagination actions interface.
 *
 * @interface PaginationActions
 * @property {(page: number) => void} goToPage - Navigate to a specific page
 * @property {() => void} nextPage - Navigate to the next page
 * @property {() => void} previousPage - Navigate to the previous page
 * @property {(size: number) => void} setPageSize - Change the page size
 */
export interface PaginationActions {
  goToPage: (page: number) => void;
  nextPage: () => void;
  previousPage: () => void;
  setPageSize: (size: number) => void;
}

/**
 * Modal state interface.
 *
 * @interface ModalState
 * @property {boolean} isOpen - Whether the modal is currently open
 * @property {() => void} open - Function to open the modal
 * @property {() => void} close - Function to close the modal
 * @property {() => void} toggle - Function to toggle the modal state
 */
export interface ModalState {
  isOpen: boolean;
  open: () => void;
  close: () => void;
  toggle: () => void;
}

/**
 * Window size state interface.
 *
 * @interface WindowSize
 * @property {number} width - Current window width in pixels
 * @property {number} height - Current window height in pixels
 * @property {boolean} isMobile - Whether the viewport is mobile-sized
 * @property {boolean} isTablet - Whether the viewport is tablet-sized
 * @property {boolean} isDesktop - Whether the viewport is desktop-sized
 */
export interface WindowSize {
  width: number;
  height: number;
  isMobile: boolean;
  isTablet: boolean;
  isDesktop: boolean;
}

/**
 * Empty state props interface.
 *
 * @interface EmptyStateProps
 * @property {string} icon - Vaadin icon name to display
 * @property {string} title - Main title text
 * @property {string} description - Descriptive text
 * @property {string} [className] - Optional additional CSS classes
 */
export interface EmptyStateProps {
  icon: string;
  title: string;
  description: string;
  className?: string;
}

/**
 * Loading state props interface.
 *
 * @interface LoadingStateProps
 * @property {string} message - Loading message to display
 * @property {string} [className] - Optional additional CSS classes
 */
export interface LoadingStateProps {
  message: string;
  className?: string;
}

/**
 * Tab identifier enum for admin dashboard navigation.
 *
 * @enum {number}
 */
export enum TabId {
  USERS = 0,
  BOOKINGS = 1,
  SETTINGS = 2,
}
