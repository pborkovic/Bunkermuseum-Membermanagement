/**
 * @fileoverview Type definitions for Vaadin components.
 *
 * This module provides proper TypeScript type definitions for Vaadin
 * component events and properties to avoid using 'any' types.
 *
 * @module types/vaadin
 * @author Philipp Borkovic
 */

/**
 * Event detail for Vaadin Dialog opened state change.
 */
export interface DialogOpenedChangedEventDetail {
  value: boolean;
}

/**
 * Custom event fired when a Vaadin Dialog's opened state changes.
 */
export interface DialogOpenedChangedEvent extends CustomEvent<DialogOpenedChangedEventDetail> {
  detail: DialogOpenedChangedEventDetail;
}

/**
 * Type guard to check if an error is an Error instance.
 */
export function isError(error: unknown): error is Error {
  return error instanceof Error;
}

/**
 * Type guard to check if an error has a message property.
 */
export function hasMessage(error: unknown): error is { message: string } {
  return (
    typeof error === 'object' &&
    error !== null &&
    'message' in error &&
    typeof (error as { message: unknown }).message === 'string'
  );
}

/**
 * Safely extracts error message from unknown error type.
 */
export function getErrorMessage(error: unknown): string {
  if (isError(error)) {
    return error.message;
  }
  if (hasMessage(error)) {
    return error.message;
  }
  if (typeof error === 'string') {
    return error;
  }
  return 'An unknown error occurred';
}
