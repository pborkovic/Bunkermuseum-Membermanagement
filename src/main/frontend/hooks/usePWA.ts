/**
 * PWA React Hook
 *
 * Custom React hook for managing PWA state and functionality.
 * Provides access to installation status, update state, and PWA controls.
 *
 * @module hooks/usePWA
 *
 * @author Philipp Borkovic
 */

import {useCallback, useEffect, useState} from 'react';
import {
    getServiceWorkerStatus,
    isRunningAsInstalledPWA,
    showInstallPrompt,
    updateServiceWorker,
} from '../utils/pwa-registration';

/**
 * PWA state interface
 */
interface PWAState {
  /**
   * Whether the app is running as an installed PWA
   */
  isInstalled: boolean;

  /**
   * Whether service worker is supported
   */
  isSupported: boolean;

  /**
   * Whether service worker is registered
   */
  isRegistered: boolean;

  /**
   * Whether an update is available
   */
  hasUpdate: boolean;

  /**
   * Whether installation prompt is available
   */
  canInstall: boolean;

  /**
   * Whether currently installing
   */
  isInstalling: boolean;

  /**
   * Whether currently updating
   */
  isUpdating: boolean;

  /**
   * Service worker registration object
   */
  registration: ServiceWorkerRegistration | null;

  /**
   * Install prompt event
   */
  installPromptEvent: any;
}

/**
 * PWA hook result interface
 */
interface UsePWAResult extends PWAState {
  /**
   * Triggers the install prompt
   */
  install: () => Promise<'accepted' | 'dismissed'>;

  /**
   * Updates the service worker to the latest version
   */
  update: () => void;

  /**
   * Checks for service worker updates
   */
  checkForUpdates: () => Promise<void>;

  /**
   * Dismisses the install prompt
   */
  dismissInstallPrompt: () => void;
}

/**
 * Custom hook for managing PWA functionality
 *
 * @returns PWA state and control functions
 *
 * @example
 * ```tsx
 * const { isInstalled, canInstall, install, hasUpdate, update } = usePWA();
 *
 * if (canInstall) {
 *   return <button onClick={install}>Install App</button>;
 * }
 *
 * if (hasUpdate) {
 *   return <button onClick={update}>Update Available</button>;
 * }
 * ```
 */
export function usePWA(): UsePWAResult {
  const [state, setState] = useState<PWAState>({
    isInstalled: false,
    isSupported: false,
    isRegistered: false,
    hasUpdate: false,
    canInstall: false,
    isInstalling: false,
    isUpdating: false,
    registration: null,
    installPromptEvent: null,
  });

  /**
   * Updates PWA status
   */
  const updateStatus = useCallback(async () => {
    const status = await getServiceWorkerStatus();

    setState((prev) => ({
      ...prev,
      isInstalled: isRunningAsInstalledPWA(),
      isSupported: status.supported,
      isRegistered: status.registered,
      hasUpdate: status.waiting,
    }));
  }, []);

  /**
   * Initialize PWA status on mount
   */
  useEffect(() => {
    updateStatus();

    const handleBeforeInstallPrompt = (e: Event) => {
      e.preventDefault();

      setState((prev) => ({
        ...prev,
        canInstall: true,
        installPromptEvent: e,
      }));

      console.log('[usePWA] Install prompt available');
    };

    const handleAppInstalled = () => {
      setState((prev) => ({
        ...prev,
        isInstalled: true,
        canInstall: false,
        installPromptEvent: null,
      }));

      console.log('[usePWA] App installed');
    };

    const handleServiceWorkerUpdate = () => {
      updateStatus();
    };

    window.addEventListener('beforeinstallprompt', handleBeforeInstallPrompt);
    window.addEventListener('appinstalled', handleAppInstalled);

    if ('serviceWorker' in navigator) {
      navigator.serviceWorker.addEventListener('controllerchange', handleServiceWorkerUpdate);
    }

    return () => {
      window.removeEventListener('beforeinstallprompt', handleBeforeInstallPrompt);
      window.removeEventListener('appinstalled', handleAppInstalled);

      if ('serviceWorker' in navigator) {
        navigator.serviceWorker.removeEventListener('controllerchange', handleServiceWorkerUpdate);
      }
    };
  }, [updateStatus]);

  /**
   * Periodically check for updates
   */
  useEffect(() => {
    const checkInterval = setInterval(() => {
      updateStatus();
    }, 60000);

    return () => clearInterval(checkInterval);
  }, [updateStatus]);

  /**
   * Triggers the install prompt
   */
  const install = useCallback(async (): Promise<'accepted' | 'dismissed'> => {
    if (!state.installPromptEvent) {
      console.warn('[usePWA] No install prompt available');
      return 'dismissed';
    }

    setState((prev) => ({ ...prev, isInstalling: true }));

    try {
      const outcome = await showInstallPrompt(state.installPromptEvent);

      if (outcome === 'accepted') {
        setState((prev) => ({
          ...prev,
          canInstall: false,
          installPromptEvent: null,
        }));
      }

      return outcome;
    } catch (error) {
      console.error('[usePWA] Install failed:', error);
      return 'dismissed';
    } finally {
      setState((prev) => ({ ...prev, isInstalling: false }));
    }
  }, [state.installPromptEvent]);

  /**
   * Updates the service worker to the latest version
   */
  const update = useCallback(async () => {
    if (!state.registration) {
      const registration = await navigator.serviceWorker.getRegistration();

      if (!registration) {
        console.warn('[usePWA] No service worker registration found');
        return;
      }

      setState((prev) => ({ ...prev, registration }));
    }

    setState((prev) => ({ ...prev, isUpdating: true }));

    try {
      if (state.registration) {
        updateServiceWorker(state.registration);
      }
    } catch (error) {
      console.error('[usePWA] Update failed:', error);
      setState((prev) => ({ ...prev, isUpdating: false }));
    }
  }, [state.registration]);

  /**
   * Checks for service worker updates
   */
  const checkForUpdates = useCallback(async () => {
    if (!('serviceWorker' in navigator)) {
      return;
    }

    try {
      const registration = await navigator.serviceWorker.getRegistration();

      if (registration) {
        await registration.update();
        await updateStatus();
      }
    } catch (error) {
      console.error('[usePWA] Update check failed:', error);
    }
  }, [updateStatus]);

  /**
   * Dismisses the install prompt
   */
  const dismissInstallPrompt = useCallback(() => {
    setState((prev) => ({
      ...prev,
      canInstall: false,
      installPromptEvent: null,
    }));
  }, []);

  return {
    ...state,
    install,
    update,
    checkForUpdates,
    dismissInstallPrompt,
  };
}

export default usePWA;
