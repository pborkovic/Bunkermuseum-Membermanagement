import { Notification } from '@vaadin/react-components/Notification';
import { getErrorMessage } from '../types/vaadin';

export default function handleError(error: unknown): void {
  const errorMessage = getErrorMessage(error);
  console.error('An unexpected error occurred:', errorMessage, error);
  Notification.show('An unexpected error occurred. Please try again later.', {
    duration: 3000,
    position: 'top-center',
    theme: 'error',
  });
}
