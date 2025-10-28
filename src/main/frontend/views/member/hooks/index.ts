/**
 * @fileoverview Custom React hooks for the member dashboard.
 *
 * This module provides reusable hooks for common operations in the member dashboard,
 * including user data loading, form validation, and state management.
 *
 * @module member/hooks
 * @author Philipp Borkovic
 */

import { useState, useEffect, useCallback, useMemo } from 'react';
import { toast } from 'sonner';
import { AuthController } from 'Frontend/generated/endpoints';
import type UserDTO from 'Frontend/generated/com/bunkermuseum/membermanagement/dto/UserDTO';
import type BookingDTO from 'Frontend/generated/com/bunkermuseum/membermanagement/dto/BookingDTO';
import type { z } from 'zod';
import type {
  ValidationErrors,
  UserProfileState,
  CategorizedBookings,
} from '../types';
import { getErrorMessage } from '../utils/errorHandling';
import { getProfilePictureUrl, isBookingFinished } from '../utils/formatting';
import { ERROR_MESSAGES } from '../constants';

/**
 * Hook for managing current user state and loading user data.
 *
 * @returns {UserProfileState & { refetch: () => Promise<void> }} User profile state and refetch function
 *
 * @author Philipp Borkovic
 */
export function useCurrentUser(): UserProfileState & { refetch: () => Promise<void> } {
  const [state, setState] = useState<UserProfileState>({
    user: null,
    profilePictureUrl: null,
    isLoading: true,
    error: null,
  });

  const loadUser = useCallback(async (): Promise<void> => {
    try {
      setState((prev) => ({ ...prev, isLoading: true, error: null }));

      const user = await AuthController.getCurrentUser();

      if (!user) {
        setState({
          user: null,
          profilePictureUrl: null,
          isLoading: false,
          error: ERROR_MESSAGES.USER_NOT_FOUND,
        });

        return;
      }

      const pictureUrl =
        user.avatarPath && user.id ? getProfilePictureUrl(user.id) : null;

      setState({
        user,
        profilePictureUrl: pictureUrl,
        isLoading: false,
        error: null,
      });
    } catch (error) {
      const errorMessage = getErrorMessage(error, ERROR_MESSAGES.LOAD_USER_FAILED);
      toast.error(errorMessage);

      setState({
        user: null,
        profilePictureUrl: null,
        isLoading: false,
        error: errorMessage,
      });
    }
  }, []);

  useEffect(() => {
    loadUser();
  }, [loadUser]);

  return { ...state, refetch: loadUser };
}

/**
 * Hook for managing form validation with Zod schemas.
 *
 * @template T - The form data type
 * @param {z.ZodType<T>} schema - Zod validation schema
 * @returns {Object} Validation state and methods
 *
 * @author Philipp Borkovic
 */
export function useFormValidation<T>(schema: z.ZodType<T>) {
  const [errors, setErrors] = useState<ValidationErrors>({});

  /**
   * Validates form data against the schema.
   *
   * @param {T} data - The form data to validate
   * @returns {boolean} True if validation passes, false otherwise
   */
  const validate = useCallback(
    (data: T): boolean => {
      const result = schema.safeParse(data);

      if (!result.success) {
        const fieldErrors: ValidationErrors = {};
        result.error.issues.forEach((issue) => {
          const path = issue.path[0]?.toString();
          if (path) {
            fieldErrors[path] = issue.message;
          }
        });
        setErrors(fieldErrors);

        return false;
      }

      setErrors({});

      return true;
    },
    [schema]
  );

  /**
   * Clears error for a specific field.
   *
   * @param {string} field - The field name to clear error for
   */
  const clearError = useCallback((field: string): void => {
    setErrors((prev) => {
      const newErrors = { ...prev };
      delete newErrors[field];
      return newErrors;
    });
  }, []);

  /**
   * Clears all validation errors.
   */
  const clearAllErrors = useCallback((): void => {
    setErrors({});
  }, []);

  return {
    errors,
    validate,
    clearError,
    clearAllErrors,
    hasErrors: Object.keys(errors).length > 0,
  };
}

/**
 * Hook for categorizing bookings into finished and pending.
 *
 * @param {BookingDTO[]} bookings - Array of bookings to categorize
 * @returns {CategorizedBookings} Categorized bookings
 *
 * @author Philipp Borkovic
 */
export function useCategorizedBookings(bookings: BookingDTO[]): CategorizedBookings {
  return useMemo(() => {
    const finished = bookings.filter(isBookingFinished);
    const pending = bookings.filter((booking) => !isBookingFinished(booking));

    return {
      finished,
      pending,
      total: bookings.length,
    };
  }, [bookings]);
}

/**
 * Hook for managing async loading state.
 *
 * @template T - The data type
 * @param {() => Promise<T>} fetchFunction - The async function to execute
 * @param {unknown[]} [dependencies=[]] - Dependencies that trigger refetch
 *
 * @returns {Object} Loading state and data
 *
 * @author Philipp Borkovic
 */
export function useAsyncData<T>(
  fetchFunction: () => Promise<T>,
  dependencies: unknown[] = []
) {
  const [data, setData] = useState<T | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadData = useCallback(async (): Promise<void> => {
    try {
      setIsLoading(true);
      setError(null);
      const result = await fetchFunction();

      setData(result);
    } catch (err) {
      const errorMessage = getErrorMessage(err);

      setError(errorMessage);
    } finally {
      setIsLoading(false);
    }
  }, dependencies);

  useEffect(() => {
    loadData();
  }, [loadData]);

  return {
    data,
    isLoading,
    error,
    refetch: loadData,
  };
}

/**
 * Hook for debouncing a value.
 *
 * @template T - The value type
 * @param {T} value - The value to debounce
 * @param {number} delay - Delay in milliseconds
 *
 * @returns {T} The debounced value
 *
 * @author Philipp Borkovic
 */
export function useDebounce<T>(value: T, delay: number): T {
  const [debouncedValue, setDebouncedValue] = useState<T>(value);

  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    return () => {
      clearTimeout(handler);
    };
  }, [value, delay]);

  return debouncedValue;
}

/**
 * Hook for managing image load error state.
 *
 * @returns {Object} Image error state and handler
 *
 * @author Philipp Borkovic
 */
export function useImageLoadError() {
  const [hasError, setHasError] = useState(false);

  const handleError = useCallback(() => {
    setHasError(true);
  }, []);

  const reset = useCallback(() => {
    setHasError(false);
  }, []);

  return {
    hasError,
    handleError,
    reset,
  };
}
