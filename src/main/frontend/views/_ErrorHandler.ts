import { Notification } from '@vaadin/react-components/Notification';
import { getErrorMessage } from '../types/vaadin';

/**
 * Global error handler for application errors.
 *
 * Handles errors that occur during runtime by:
 * - Logging the error to console
 * - Showing a user-friendly notification
 * - Optionally redirecting to error page for severe errors
 *
 * @param error - The error that occurred
 * @param options - Optional configuration
 * @param options.showNotification - Whether to show notification toast (default: true)
 * @param options.redirectToErrorPage - Whether to redirect to /500 page for severe errors (default: false)
 *
 * @author Philipp Borkovic
 */
export default function handleError(
  error: unknown,
  options: { showNotification?: boolean; redirectToErrorPage?: boolean } = {}
): void {
  const { showNotification = true, redirectToErrorPage = false } = options;

  const errorMessage = getErrorMessage(error);
  console.error('An unexpected error occurred:', errorMessage, error);

  if (showNotification) {
    Notification.show('An unexpected error occurred. Please try again later.', {
      duration: 3000,
      position: 'top-center',
      theme: 'error',
    });
  }

  if (redirectToErrorPage) {
    window.location.href = '/500';
  }
}
