/**
 * Shared constants for the admin dashboard.
 * Centralized configuration values used across multiple components.
 *
 * @module admin/utils/constants
 *
 * @author Philipp Borkovic
 */

/**
 * Gender/salutation options for user profiles.
 * Used in user edit forms and profile settings.
 *
 * @constant
 *
 * @type {ReadonlyArray<{value: string, label: string}>}
 */
export const ANREDE_OPTIONS = [
  { value: 'männlich', label: 'Männlich' },
  { value: 'weiblich', label: 'Weiblich' },
  { value: 'divers', label: 'Divers' },
] as const;

/**
 * Date range preset options for booking filters.
 * Each preset defines a label, value identifier, and number of days to look back.
 *
 * @constant
 *
 * @type {ReadonlyArray<{label: string, value: string, days: number}>}
 */
export const DATE_RANGE_PRESETS = [
  { label: '1 Woche', value: '1week', days: 7 },
  { label: '1 Monat', value: '1month', days: 30 },
  { label: '6 Monate', value: '6months', days: 180 },
  { label: '1 Jahr', value: '1year', days: 365 },
  { label: 'Benutzerdefiniert', value: 'custom', days: 0 },
] as const;

/**
 * Available page size options for pagination.
 * Defines how many items can be displayed per page.
 *
 * @constant
 *
 * @type {ReadonlyArray<number>}
 */
export const PAGE_SIZE_OPTIONS = [5, 10, 25, 50, 100] as const;

/**
 * Default page size for paginated lists.
 *
 * @constant
 *
 * @type {number}
 */
export const DEFAULT_PAGE_SIZE = 10;

/**
 * User status filter options.
 *
 * @constant
 *
 * @type {ReadonlyArray<{value: string, label: string}>}
 */
export const USER_STATUS_OPTIONS = [
  { value: 'active', label: 'Aktives Mitglied' },
  { value: 'deleted', label: 'Inaktives Mitglied' },
  { value: 'all', label: 'Alle Mitglieder' },
] as const;

/**
 * Fixed height for list containers to ensure consistent layout.
 * Sized to accommodate 10 rows with headers and padding.
 *
 * @constant
 *
 * @type {number}
 */
export const LIST_CONTAINER_HEIGHT = 560;

/**
 * Mobile breakpoint in pixels.
 * Matches Tailwind's 'md' breakpoint.
 *
 * @constant
 *
 * @type {number}
 */
export const MOBILE_BREAKPOINT = 768;

/**
 * Debounce delay for window resize events in milliseconds.
 *
 * @constant
 *
 * @type {number}
 */
export const RESIZE_DEBOUNCE_DELAY = 150;

/**
 * User export type options.
 * Defines different member categories for export functionality.
 *
 * @constant
 *
 * @type {ReadonlyArray<{value: string, label: string}>}
 */
export const EXPORT_USER_TYPE_OPTIONS = [
  { value: 'all', label: 'Alle Mitglieder' },
  { value: 'ordentlich', label: 'Ordentliche Mitglieder' },
  { value: 'foerdernd', label: 'Fördernde Mitglieder' },
  { value: 'ausgetreten', label: 'Ausgetretene Mitglieder' },
] as const;

/**
 * Export format options.
 * Defines available file formats for data export.
 *
 * @constant
 *
 * @type {ReadonlyArray<{value: string, label: string}>}
 */
export const EXPORT_FORMAT_OPTIONS = [
  { value: 'xlsx', label: 'Excel - XLSX' },
  { value: 'pdf', label: 'PDF' },
  { value: 'xml', label: 'XML' },
  { value: 'json', label: 'JSON' },
] as const;

/**
 * Booking export type options.
 * Defines different booking categories for export functionality.
 *
 * @constant
 *
 * @type {ReadonlyArray<{value: string, label: string}>}
 */
export const EXPORT_BOOKING_TYPE_OPTIONS = [
  { value: 'all', label: 'Alle Buchungen' },
  { value: 'open', label: 'Offene Buchungen' },
  { value: 'completed', label: 'Abgeschlossene Buchungen' },
] as const;

/**
 * Email export type options.
 * Defines different email categories for export functionality.
 *
 * @constant
 *
 * @type {ReadonlyArray<{value: string, label: string}>}
 */
export const EXPORT_EMAIL_TYPE_OPTIONS = [
  { value: 'system', label: 'System-E-Mails' },
  { value: 'user', label: 'Benutzer-gesendete E-Mails' },
] as const;
