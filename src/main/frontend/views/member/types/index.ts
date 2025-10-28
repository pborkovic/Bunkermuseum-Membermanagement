/**
 * @fileoverview Type definitions for the member dashboard module.
 *
 * This module provides comprehensive type definitions for all member dashboard
 * components, ensuring type safety and consistency across the application.
 *
 * @module member/types
 * @author Philipp Borkovic
 */

import type UserDTO from 'Frontend/generated/com/bunkermuseum/membermanagement/dto/UserDTO';
import type BookingDTO from 'Frontend/generated/com/bunkermuseum/membermanagement/dto/BookingDTO';

/**
 * Tab identifier enum for member dashboard navigation.
 *
 * @enum {number}
 */
export enum TabId {
  BOOKINGS = 0,
  SETTINGS = 1,
}

/**
 * Profile form data structure for user profile updates.
 *
 * @interface ProfileFormData
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
 * Password change form data structure.
 *
 * @interface PasswordFormData
 */
export interface PasswordFormData {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

/**
 * Validation error map for form fields.
 *
 * @type {Record<string, string>}
 */
export type ValidationErrors = Record<string, string>;

/**
 * API error response structure.
 *
 * @interface ApiError
 */
export interface ApiError {
  message: string;
  status?: number;
  details?: unknown;
}

/**
 * Loading state for async operations.
 *
 * @interface LoadingState
 */
export interface LoadingState {
  isLoading: boolean;
  error: string | null;
}

/**
 * Upload state for file uploads.
 *
 * @interface UploadState
 */
export interface UploadState extends LoadingState {
  progress?: number;
}

/**
 * User profile state.
 *
 * @interface UserProfileState
 */
export interface UserProfileState {
  user: UserDTO | null;
  profilePictureUrl: string | null;
  isLoading: boolean;
  error: string | null;
}

/**
 * Booking categorization result.
 *
 * @interface CategorizedBookings
 */
export interface CategorizedBookings {
  finished: BookingDTO[];
  pending: BookingDTO[];
  total: number;
}

/**
 * Gender/salutation option.
 *
 * @interface SalutationOption
 */
export interface SalutationOption {
  readonly value: string;
  readonly label: string;
}

/**
 * Tab configuration for navigation.
 *
 * @interface TabConfig
 */
export interface TabConfig {
  tabId: TabId;
  icon: string;
  label: string;
}

/**
 * File validation result.
 *
 * @interface FileValidationResult
 */
export interface FileValidationResult {
  isValid: boolean;
  error?: string;
}

/**
 * Type guard to check if an error is an instance of Error.
 *
 * @param {unknown} error - The error to check
 *
 * @returns {error is Error} True if error is an Error instance
 */
export function isError(error: unknown): error is Error {
  return error instanceof Error;
}

/**
 * Type guard to check if an error is an ApiError.
 *
 * @param {unknown} error - The error to check
 * @returns {error is ApiError} True if error is an ApiError
 */
export function isApiError(error: unknown): error is ApiError {
  return (
    typeof error === 'object' && error !== null && 'message' in error && typeof (error as ApiError).message === 'string'
  );
}
