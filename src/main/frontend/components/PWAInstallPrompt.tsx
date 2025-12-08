/**
 * PWA Install Prompt Component
 *
 * Displays a banner prompting users to install the PWA on their device.
 * Automatically detects when the app can be installed and shows the prompt.
 *
 * @module components/PWAInstallPrompt
 *
 * @author Philipp Borkovic
 */

import {useEffect, useState} from 'react';
import {FaDownload, FaTimes} from 'react-icons/fa';
import {showInstallPrompt} from '../utils/pwa-registration';

/**
 * Props for the PWAInstallPrompt component
 */
interface PWAInstallPromptProps {
  /**
   * Custom className for styling
   */
  className?: string;

  /**
   * Whether to show on mobile devices only
   * @default false
   */
  mobileOnly?: boolean;

  /**
   * Whether to auto-hide after installation
   * @default true
   */
  autoHide?: boolean;

  /**
   * Storage key for remembering dismissed state
   * @default 'pwa-install-prompt-dismissed'
   */
  storageKey?: string;
}

/**
 * PWA Install Prompt Component
 *
 * Shows a banner prompting users to install the PWA.
 * Only appears when the browser supports installation.
 *
 * @example
 * ```tsx
 * <PWAInstallPrompt mobileOnly={true} />
 * ```
 */
export default function PWAInstallPrompt({
  className = '',
  mobileOnly = false,
  autoHide = true,
  storageKey = 'pwa-install-prompt-dismissed',
}: PWAInstallPromptProps): JSX.Element | null {
  const [deferredPrompt, setDeferredPrompt] = useState<any>(null);
  const [isVisible, setIsVisible] = useState<boolean>(false);
  const [isInstalling, setIsInstalling] = useState<boolean>(false);

  useEffect(() => {
    const isDismissed = localStorage.getItem(storageKey) === 'true';

    if (isDismissed && autoHide) {
      return;
    }

    const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(
      navigator.userAgent
    );

    if (mobileOnly && !isMobile) {
      return;
    }

    const handleBeforeInstallPrompt = (e: Event) => {
      e.preventDefault();

      setDeferredPrompt(e);
      setIsVisible(true);

      console.log('[PWA] Install prompt ready');
    };

    const handleAppInstalled = () => {
      console.log('[PWA] App installed successfully');
      setIsVisible(false);
      setDeferredPrompt(null);

      if (autoHide) {
        localStorage.setItem(storageKey, 'true');
      }
    };

    window.addEventListener('beforeinstallprompt', handleBeforeInstallPrompt);
    window.addEventListener('appinstalled', handleAppInstalled);

    return () => {
      window.removeEventListener('beforeinstallprompt', handleBeforeInstallPrompt);
      window.removeEventListener('appinstalled', handleAppInstalled);
    };
  }, [mobileOnly, autoHide, storageKey]);

  /**
   * Handles the install button click
   */
  const handleInstallClick = async (): Promise<void> => {
    if (!deferredPrompt) {
      console.warn('[PWA] No install prompt available');
      return;
    }

    setIsInstalling(true);

    try {
      const outcome = await showInstallPrompt(deferredPrompt);

      console.log('[PWA] Install outcome:', outcome);

      if (outcome === 'accepted') {
        setIsVisible(false);
        setDeferredPrompt(null);

        if (autoHide) {
          localStorage.setItem(storageKey, 'true');
        }
      }
    } catch (error) {
      console.error('[PWA] Install prompt error:', error);
    } finally {
      setIsInstalling(false);
    }
  };

  /**
   * Handles the dismiss button click
   */
  const handleDismissClick = (): void => {
    setIsVisible(false);

    if (autoHide) {
      localStorage.setItem(storageKey, 'true');
    }
  };

  if (!isVisible || !deferredPrompt) {
    return null;
  }

  return (
    <div
      className={`fixed bottom-0 left-0 right-0 z-50 bg-black text-white shadow-lg border-t border-gray-700 animate-slide-up ${className}`}
      role="banner"
      aria-live="polite"
    >
      <div className="container mx-auto px-4 py-3 sm:py-4">
        <div className="flex items-center justify-between gap-4">
          {/* Message */}
          <div className="flex items-center gap-3 flex-1 min-w-0">
            <div className="flex-shrink-0">
              <FaDownload className="w-5 h-5 sm:w-6 sm:h-6" />
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm sm:text-base font-semibold mb-0.5">
                App installieren
              </p>
              <p className="text-xs sm:text-sm text-gray-300 truncate">
                Installieren Sie die App für schnelleren Zugriff
              </p>
            </div>
          </div>

          {/* Action Buttons */}
          <div className="flex items-center gap-2 flex-shrink-0">
            <button
              onClick={handleInstallClick}
              disabled={isInstalling}
              className="px-3 sm:px-4 py-1.5 sm:py-2 bg-white text-black text-xs sm:text-sm font-semibold rounded-md hover:bg-gray-200 transition-colors disabled:opacity-50 disabled:cursor-not-allowed whitespace-nowrap"
              aria-label="Install app"
            >
              {isInstalling ? 'Installieren...' : 'Installieren'}
            </button>
            <button
              onClick={handleDismissClick}
              className="p-1.5 sm:p-2 hover:bg-gray-800 rounded-md transition-colors"
              aria-label="Dismiss install prompt"
            >
              <FaTimes className="w-4 h-4 sm:w-5 sm:h-5" />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
