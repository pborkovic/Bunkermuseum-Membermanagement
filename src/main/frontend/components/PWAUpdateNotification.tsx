/**
 * PWA Update Notification Component
 *
 * Displays a notification when a new version of the app is available.
 * Uses the existing toast notification system (Sonner).
 *
 * @module components/PWAUpdateNotification
 * @author Philipp Borkovic
 */

import {useEffect} from 'react';
import {toast} from 'sonner';
import {usePWA} from '../hooks/usePWA';

/**
 * Props for the PWAUpdateNotification component
 */
interface PWAUpdateNotificationProps {
  /**
   * Whether to show the notification automatically
   * @default true
   */
  autoShow?: boolean;

  /**
   * Custom message for the update notification
   * @default 'Eine neue Version ist verfügbar!'
   */
  message?: string;

  /**
   * Custom action text
   * @default 'Jetzt aktualisieren'
   */
  actionText?: string;

  /**
   * Custom dismiss text
   * @default 'Später'
   */
  dismissText?: string;

  /**
   * Toast duration in milliseconds (0 = never auto-dismiss)
   * @default 0
   */
  duration?: number;
}

/**
 * PWA Update Notification Component
 *
 * Automatically shows a toast notification when a PWA update is available.
 * Integrates with the existing Sonner toast system.
 *
 * @example
 * ```tsx
 * // Add to your root component (e.g., App.tsx or index.tsx)
 * <PWAUpdateNotification />
 * ```
 *
 * @example
 * ```tsx
 * // With custom message
 * <PWAUpdateNotification
 *   message="Neue Features verfügbar!"
 *   actionText="Aktualisieren"
 *   dismissText="Nicht jetzt"
 * />
 * ```
 */
export default function PWAUpdateNotification({
  autoShow = true,
  message = 'Eine neue Version ist verfügbar!',
  actionText = 'Jetzt aktualisieren',
  dismissText = 'Später',
  duration = 0,
}: PWAUpdateNotificationProps): null {
  const { hasUpdate, update, isUpdating } = usePWA();

  useEffect(() => {
    if (!autoShow || !hasUpdate) {
      return;
    }

    const toastId = toast.info(message, {
      duration,
      description: 'Klicken Sie auf "Aktualisieren", um die neueste Version zu laden.',
      action: {
        label: actionText,
        onClick: () => {
          update();
          toast.dismiss(toastId);
        },
      },
      cancel: {
        label: dismissText,
        onClick: () => {
          toast.dismiss(toastId);
        },
      },
    });

    return () => {
      toast.dismiss(toastId);
    };
  }, [hasUpdate, autoShow, message, actionText, dismissText, duration, update]);

  useEffect(() => {
    if (isUpdating) {
      toast.loading('App wird aktualisiert...', {
        description: 'Bitte warten Sie einen Moment.',
      });
    }
  }, [isUpdating]);

  return null;
}
