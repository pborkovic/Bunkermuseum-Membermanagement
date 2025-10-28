/**
 * @fileoverview Type-safe error handling utilities for the member dashboard.
 *
 * This module provides utilities for handling errors in a type-safe manner,
 * extracting error messages, and formatting errors for display to users.
 *
 * @module member/utils/errorHandling
 * @author Philipp Borkovic
 */

import { isError, isApiError, type ApiError } from '../types';
import { ERROR_MESSAGES } from '../constants';

/**
 * Extracts a human-readable error message from any error type.
 *
 * This function handles multiple error types in a type-safe manner:
 * - Standard Error objects: Returns the error message
 * - API errors: Returns the API error message
 * - Fetch errors: Returns network error message
 * - Unknown errors: Returns fallback message
 *
 * @param {unknown} error - The error to extract a message from
 * @param {string} [fallbackMessage] - Optional fallback message if error cannot be parsed
 * @returns {string} The extracted error message
 *
 * @author Philipp Borkovic
 */
export function getErrorMessage(
  error: unknown,
  fallbackMessage: string = ERROR_MESSAGES.UNKNOWN_ERROR
): string {
  if (isError(error)) {
    return error.message || fallbackMessage;
  }

  if (isApiError(error)) {
    return error.message || fallbackMessage;
  }

  if (typeof error === 'string') {
    return error;
  }

  if (error instanceof Response) {
    return `${ERROR_MESSAGES.NETWORK_ERROR} (${error.status})`;
  }

  return fallbackMessage;
}

/**
 * Extracts error details from a fetch Response object.
 *
 * @param {Response} response - The fetch Response object
 * @returns {Promise<ApiError>} The extracted error details
 *
 * @author Philipp Borkovic
 */
export async function extractResponseError(response: Response): Promise<ApiError> {
  try {
    const errorData = await response.json();
    return {
      message: errorData.error || errorData.message || ERROR_MESSAGES.UNKNOWN_ERROR,
      status: response.status,
      details: errorData,
    };
  } catch {
    return {
      message: `${ERROR_MESSAGES.NETWORK_ERROR} (${response.status})`,
      status: response.status,
    };
  }
}

/**
 * Creates a standardized API error object.
 *
 * @param {string} message - The error message
 * @param {number} [status] - Optional HTTP status code
 * @param {unknown} [details] - Optional additional error details
 * @returns {ApiError} The created API error object
 *
 * @author Philipp Borkovic
 */
export function createApiError(
  message: string,
  status?: number,
  details?: unknown
): ApiError {
  return {
    message,
    status,
    details,
  };
}

/**
 * Checks if a response indicates a successful operation.
 *
 * @param {Response} response - The fetch Response object
 * @returns {boolean} True if response is successful (status 200-299)
 *
 * @author Philipp Borkovic
 */
export function isSuccessResponse(response: Response): boolean {
  return response.ok && response.status >= 200 && response.status < 300;
}

/**
 * Type guard to check if an error indicates a network failure.
 *
 * @param {unknown} error - The error to check
 * @returns {boolean} True if error is a network failure
 *
 * @author Philipp Borkovic
 */
export function isNetworkError(error: unknown): boolean {
  if (isError(error)) {
    return (
      error.message.includes('Failed to fetch') ||
      error.message.includes('Network') ||
      error.message.includes('network') ||
      error.name === 'NetworkError' ||
      error.name === 'TypeError'
    );
  }

  return false;
}

/**
 * Handles async operations with standardized error handling.
 *
 * @template T - The return type of the operation
 * @param {() => Promise<T>} operation - The async operation to execute
 * @param {string} [errorMessage] - Optional custom error message
 * @returns {Promise<T>} The result of the operation
 * @throws {ApiError} If the operation fails
 *
 * @author Philipp Borkovic
 */
export async function handleAsyncOperation<T>(
  operation: () => Promise<T>,
  errorMessage?: string
): Promise<T> {
  try {
    return await operation();
  } catch (error) {
    const message = getErrorMessage(error, errorMessage);

    throw createApiError(message);
  }
}

/**
 * Validates a file before upload.
 *
 * @param {File} file - The file to validate
 * @param {Object} options - Validation options
 * @param {number} options.maxSize - Maximum file size in bytes
 * @param {readonly string[]} options.acceptedTypes - Accepted MIME types
 * @returns {{ isValid: boolean; error?: string }} Validation result
 *
 * @author Philipp Borkovic
 */
export function validateFile(
  file: File,
  options: {
    maxSize: number;
    acceptedTypes: readonly string[];
  }
): { isValid: boolean; error?: string } {
  if (file.size > options.maxSize) {
    return {
      isValid: false,
      error: ERROR_MESSAGES.FILE_TOO_LARGE,
    };
  }

  if (!options.acceptedTypes.includes(file.type)) {
    return {
      isValid: false,
      error: ERROR_MESSAGES.INVALID_FILE_TYPE,
    };
  }

  return { isValid: true };
}
