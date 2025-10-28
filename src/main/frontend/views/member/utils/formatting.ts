/**
 * @fileoverview Formatting utilities for the member dashboard.
 *
 * This module provides utilities for formatting dates, currency, and other data
 * for display in the member dashboard UI.
 *
 * @module member/utils/formatting
 * @author Philipp Borkovic
 */

import { LOCALE_DE, DATE_FORMAT_OPTIONS, CURRENCY_FORMAT_OPTIONS } from '../constants';
import type BookingDTO from 'Frontend/generated/com/bunkermuseum/membermanagement/dto/BookingDTO';

/**
 * Formats a date string or Date object to German locale format.
 *
 * @param {string | Date | null | undefined} date - The date to format
 * @param {Intl.DateTimeFormatOptions} [options] - Optional custom format options
 * @returns {string} The formatted date string, or '-' if date is null/undefined
 *
 * @author Philipp Borkovic
 */
export function formatDate(
  date: string | Date | null | undefined,
  options: Intl.DateTimeFormatOptions = DATE_FORMAT_OPTIONS
): string {
  if (!date){
      return '-';
  }

  try {
    const dateObject = typeof date === 'string' ? new Date(date) : date;

    return dateObject.toLocaleDateString(LOCALE_DE, options);
  } catch {
    return '-';
  }
}

/**
 * Formats a number as EUR currency.
 *
 * @param {number | null | undefined} amount - The amount to format
 * @returns {string} The formatted currency string, or '-' if amount is null/undefined
 *
 * @author Philipp Borkovic
 */
export function formatCurrency(amount: number | null | undefined): string {
  if (amount === null || amount === undefined) {
      return '-';
  }

  try {
    return new Intl.NumberFormat(LOCALE_DE, CURRENCY_FORMAT_OPTIONS).format(amount);
  } catch {
    return '-';
  }
}

/**
 * Determines if a booking is finished (payment received).
 *
 * A booking is considered finished when both receivedAt and actualAmount are set.
 *
 * @param {BookingDTO} booking - The booking to check
 * @returns {boolean} True if booking is finished
 *
 * @author Philipp Borkovic
 */
export function isBookingFinished(booking: BookingDTO): boolean {
  return booking.receivedAt != null && booking.actualAmount != null;
}

/**
 * Generates a profile picture URL with cache-busting timestamp.
 *
 * @param {string} userId - The user ID
 * @param {number} [timestamp] - Optional timestamp for cache busting (defaults to current time)
 * @returns {string} The profile picture URL
 *
 * @author Philipp Borkovic
 */
export function getProfilePictureUrl(userId: string, timestamp?: number): string {
  const ts = timestamp ?? Date.now();

  return `/api/upload/profile-picture/${userId}?t=${ts}`;
}

/**
 * Truncates a string to a maximum length and adds ellipsis if needed.
 *
 * @param {string} text - The text to truncate
 * @param {number} maxLength - Maximum length before truncation
 * @returns {string} The truncated text
 *
 * @author Philipp Borkovic
 */
export function truncateText(text: string, maxLength: number): string {
  if (text.length <= maxLength) {
      return text;
  }

  return text.substring(0, maxLength) + '...';
}

/**
 * Formats a list of role names as a comma-separated string.
 *
 * @param {Array<{ name?: string | null } | null | undefined> | null | undefined} roles - Array of role objects
 * @returns {string} Comma-separated role names, or empty string if no roles
 *
 * @author Philipp Borkovic
 */
export function formatRoles(
  roles: Array<{ name?: string | null } | null | undefined> | null | undefined
): string {
  if (!roles || roles.length === 0) {
      return '';
  }

  return roles
    .map((role) => role?.name)
    .filter((name): name is string => Boolean(name))
    .join(', ');
}

/**
 * Formats file size in bytes to human-readable format.
 *
 * @param {number} bytes - The file size in bytes
 * @returns {string} The formatted file size
 *
 * @author Philipp Borkovic
 */
export function formatFileSize(bytes: number): string {
  if (bytes === 0) {
      return '0 Bytes';
  }

  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));

  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(1))} ${sizes[i]}`;
}
