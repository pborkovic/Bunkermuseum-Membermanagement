/**
 * Formatting utilities for the admin dashboard.
 * Provides consistent date and currency formatting across the application.
 *
 * @module admin/utils/formatting
 *
 * @author Philipp Borkovic
 */

/**
 * Formats a date string to German locale format (DD.MM.YYYY).
 *
 * @param {string | null | undefined} dateString - The ISO date string to format
 * @returns {string} Formatted date string or 'N/A' if input is null/undefined
 *
 * @example
 * formatDate('2024-01-15') // Returns: '15.01.2024'
 * formatDate(null) // Returns: 'N/A'
 * formatDate(undefined) // Returns: 'N/A'
 *
 * @author Philipp Borkovic
 */
export function formatDate(dateString: string | null | undefined): string {
  if (!dateString) return 'N/A';

  try {
    return new Date(dateString).toLocaleDateString('de-DE');
  } catch (error) {
    return 'N/A';
  }
}

/**
 * Formats a number as EUR currency using German locale.
 *
 * @param {number | null | undefined} amount - The amount to format
 * @returns {string} Formatted currency string or 'N/A' if input is null/undefined
 *
 * @author Philipp Borkovic
 */
export function formatCurrency(amount: number | null | undefined): string {
  if (amount === null || amount === undefined) return 'N/A';

  try {
    return new Intl.NumberFormat('de-DE', {
      style: 'currency',
      currency: 'EUR',
    }).format(amount);
  } catch (error) {
    return 'N/A';
  }
}

/**
 * Formats a date string to a full German datetime format.
 *
 * @param {string | null | undefined} dateString - The ISO date string to format
 * @returns {string} Formatted datetime string or 'N/A' if input is null/undefined
 *
 * @author Philipp Borkovic
 */
export function formatDateTime(dateString: string | null | undefined): string {
  if (!dateString) {
      return 'N/A';
  }

  try {
    return new Date(dateString).toLocaleString('de-DE');
  } catch (error) {
    return 'N/A';
  }
}

/**
 * Formats a phone number to a standardized format.
 * Currently returns the input as-is, but can be extended for specific formatting.
 *
 * @param {string | null | undefined} phone - The phone number to format
 * @returns {string} Formatted phone number or 'N/A' if input is null/undefined
 *
 * @author Philipp Borkovic
 */
export function formatPhoneNumber(phone: string | null | undefined): string {
  if (!phone) {
      return 'N/A';
  }

  return phone;
}
