/**
 * @fileoverview Constants for the member dashboard module.
 *
 * This module defines all constants used across the member dashboard,
 * including validation rules, file upload limits, error messages, and UI text.
 *
 * @module member/constants
 * @author Philipp Borkovic
 */

import type { SalutationOption, TabConfig } from '../types';
import { TabId } from '../types';
import type { IconType } from 'react-icons';

/**
 * File upload constraints.
 *
 * @const {Object} FILE_UPLOAD
 */
export const FILE_UPLOAD = {
  MAX_SIZE: 5 * 1024 * 1024,
  MAX_SIZE_MB: 5,
  ACCEPTED_TYPES: ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'] as const,
  ACCEPTED_EXTENSIONS: ['JPEG', 'PNG', 'WebP'] as const,
} as const;

/**
 * Form validation constants.
 *
 * @const {Object} VALIDATION
 */
export const VALIDATION = {
  NAME_MIN_LENGTH: 2,
  NAME_MAX_LENGTH: 100,
  PASSWORD_MIN_LENGTH: 8,
  PASSWORD_MAX_LENGTH: 128,
  POSTAL_CODE_MIN_LENGTH: 4,
  EMAIL_MAX_LENGTH: 255,
} as const;

/**
 * Profile picture constraints.
 *
 * @const {Object} PROFILE_PICTURE
 */
export const PROFILE_PICTURE = {
  SIZE_LARGE: 128,
  SIZE_SMALL: 40,
  ICON_SIZE: 64,
  ICON_WIDTH: 20,
  ICON_HEIGHT: 20,
} as const;

/**
 * Gender/salutation options for the profile form.
 *
 * @const {ReadonlyArray<SalutationOption>}
 */
export const SALUTATION_OPTIONS: readonly SalutationOption[] = [
  { value: 'männlich', label: 'Männlich' },
  { value: 'weiblich', label: 'Weiblich' },
  { value: 'divers', label: 'Divers' },
] as const;

/**
 * Tab icon components for the member dashboard navigation.
 * Exported separately to avoid JSX in .ts file.
 */
export { FaFileInvoice as BookingsIcon, FaCog as SettingsIcon, FaUser as UserIcon } from 'react-icons/fa';

/**
 * Error messages in German.
 *
 * @const {Object} ERROR_MESSAGES
 */
export const ERROR_MESSAGES = {
  LOAD_USER_FAILED: 'Fehler beim Laden der Benutzerdaten',
  USER_NOT_FOUND: 'Benutzer nicht gefunden',
  LOAD_PROFILE_FAILED: 'Fehler beim Laden des Profils',
  UPDATE_PROFILE_FAILED: 'Fehler beim Aktualisieren des Profils',
  CHANGE_PASSWORD_FAILED: 'Fehler beim Ändern des Passworts',
  LOAD_BOOKINGS_FAILED: 'Fehler beim Laden der Buchungen',
  FILE_TOO_LARGE: `Datei ist zu groß. Maximale Größe ist ${FILE_UPLOAD.MAX_SIZE_MB}MB`,
  INVALID_FILE_TYPE: `Ungültiger Dateityp. Nur ${FILE_UPLOAD.ACCEPTED_EXTENSIONS.join(', ')} sind erlaubt`,
  UPLOAD_FAILED: 'Fehler beim Hochladen des Profilbilds',
  UPLOAD_GENERIC_FAILED: 'Upload fehlgeschlagen',
  UNKNOWN_ERROR: 'Ein unbekannter Fehler ist aufgetreten',
  NETWORK_ERROR: 'Netzwerkfehler. Bitte überprüfen Sie Ihre Verbindung',
} as const;

/**
 * Success messages in German.
 *
 * @const {Object} SUCCESS_MESSAGES
 */
export const SUCCESS_MESSAGES = {
  PROFILE_UPDATED: 'Profil erfolgreich aktualisiert',
  PICTURE_UPLOADED: 'Profilbild erfolgreich hochgeladen',
  PASSWORD_CHANGED: 'Passwort erfolgreich geändert',
} as const;

/**
 * UI text constants.
 *
 * @const {Object} UI_TEXT
 */
export const UI_TEXT = {
  LOADING: 'Laden...',
  SAVING: 'Speichern...',
  UPLOADING: 'Hochladen...',
  NO_BOOKINGS: 'Keine Buchungen vorhanden',
  NO_BOOKINGS_DESCRIPTION: 'Sie haben derzeit keine Buchungen. Sobald Buchungen für Sie erstellt werden, erscheinen sie hier.',
  EMAIL_VERIFIED: 'Verifiziert am',
  EMAIL_NOT_VERIFIED: 'Nicht verifiziert',
  PROFILE_PICTURE_LABEL: 'Profilbild',
  PROFILE_INFO_LABEL: 'Profilinformationen',
  PASSWORD_CHANGE_LABEL: 'Passwort ändern',
  ACCOUNT_INFO_LABEL: 'Kontoinformationen',
  BOOKING_STATUS_FINISHED: 'Abgeschlossen',
  BOOKING_STATUS_PENDING: 'Ausstehend',
  FINISHED_BOOKINGS: 'Abgeschlossene Buchungen',
  PENDING_BOOKINGS: 'Ausstehende Buchungen',
  UPDATE_PROFILE: 'Profil aktualisieren',
  CHANGE_PASSWORD: 'Passwort ändern',
} as const;

/**
 * API endpoints.
 *
 * @const {Object} API_ENDPOINTS
 */
export const API_ENDPOINTS = {
  PROFILE_PICTURE_UPLOAD: '/api/upload/profile-picture',
  PROFILE_PICTURE_FETCH: (userId: string) => `/api/upload/profile-picture/${userId}`,
} as const;

/**
 * Date format options for German locale.
 *
 * @const {Intl.DateTimeFormatOptions}
 */
export const DATE_FORMAT_OPTIONS: Intl.DateTimeFormatOptions = {
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
} as const;

/**
 * Currency format options for EUR.
 *
 * @const {Intl.NumberFormatOptions}
 */
export const CURRENCY_FORMAT_OPTIONS: Intl.NumberFormatOptions = {
  style: 'currency',
  currency: 'EUR',
} as const;

/**
 * Locale for German formatting.
 *
 * @const {string}
 */
export const LOCALE_DE = 'de-DE' as const;
