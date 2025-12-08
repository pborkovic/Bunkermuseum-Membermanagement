/**
 * PWA Service Worker Registration Utility
 *
 * Handles service worker registration, updates, and lifecycle management.
 * Provides a clean API for the application to interact with the PWA features.
 *
 * @module utils/pwa-registration
 *
 * @author Philipp Borkovic
 */

/**
 * Configuration for PWA registration
 */
export interface PWAConfig {
  /**
   * Path to the service worker file
   */
  swPath?: string;

  /**
   * Callback when a new service worker is available
   */
  onUpdate?: (registration: ServiceWorkerRegistration) => void;

  /**
   * Callback when service worker is successfully registered
   */
  onSuccess?: (registration: ServiceWorkerRegistration) => void;

  /**
   * Callback when registration fails
   */
  onError?: (error: Error) => void;

  /**
   * Enable update checks on page visibility change
   */
  checkForUpdates?: boolean;

  /**
   * Update check interval in milliseconds (default: 1 hour)
   */
  updateCheckInterval?: number;
}

/**
 * Default configuration
 */
const DEFAULT_CONFIG: Required<PWAConfig> = {
  swPath: '/service-worker.js',
  onUpdate: () => console.log('[PWA] New version available'),
  onSuccess: () => console.log('[PWA] Service worker registered successfully'),
  onError: (error) => console.error('[PWA] Registration failed:', error),
  checkForUpdates: true,
  updateCheckInterval: 60 * 60 * 1000,
};

/**
 * Registers the service worker and sets up update handling
 *
 * @param config - PWA configuration options
 * @returns Promise that resolves to the service worker registration
 *
 * @example
 * ```typescript
 * registerServiceWorker({
 *   onUpdate: (registration) => {
 *     // Show update notification to user
 *     toast.info('New version available! Reload to update.');
 *   },
 *   onSuccess: () => {
 *     console.log('App is ready to work offline');
 *   }
 * });
 * ```
 */
export async function registerServiceWorker(
  config: PWAConfig = {}
): Promise<ServiceWorkerRegistration | undefined> {
  const finalConfig = { ...DEFAULT_CONFIG, ...config };

  if (!('serviceWorker' in navigator)) {
    console.warn('[PWA] Service workers are not supported in this browser');
    return undefined;
  }

  try {
    console.log('[PWA] Registering service worker...');

    const registration = await navigator.serviceWorker.register(
      finalConfig.swPath,
      { scope: '/' }
    );

    console.log('[PWA] Service worker registered with scope:', registration.scope);

    registration.addEventListener('updatefound', () => {
      handleUpdateFound(registration, finalConfig);
    });

    if (finalConfig.checkForUpdates) {
      setupUpdateChecks(registration, finalConfig);
    }

    finalConfig.onSuccess(registration);

    return registration;
  } catch (error) {
    console.error('[PWA] Service worker registration failed:', error);
    finalConfig.onError(error as Error);
    return undefined;
  }
}

/**
 * Handles the updatefound event
 */
function handleUpdateFound(
  registration: ServiceWorkerRegistration,
  config: Required<PWAConfig>
): void {
  const newWorker = registration.installing;

  if (!newWorker) {
    return;
  }

  console.log('[PWA] New service worker found, installing...');

  newWorker.addEventListener('statechange', () => {
    if (newWorker.state === 'installed' && navigator.serviceWorker.controller) {
      console.log('[PWA] New service worker installed, update available');
      config.onUpdate(registration);
    }

    if (newWorker.state === 'activated') {
      console.log('[PWA] New service worker activated');
    }
  });
}

/**
 * Sets up periodic update checks
 */
function setupUpdateChecks(
  registration: ServiceWorkerRegistration,
  config: Required<PWAConfig>
): void {
  document.addEventListener('visibilitychange', () => {
    if (!document.hidden) {
      console.log('[PWA] Page became visible, checking for updates...');
      registration.update();
    }
  });

  setInterval(() => {
    console.log('[PWA] Periodic update check...');
    registration.update();
  }, config.updateCheckInterval);

  window.addEventListener('focus', () => {
    console.log('[PWA] Window focused, checking for updates...');
    registration.update();
  });
}

/**
 * Unregisters the service worker
 *
 * @returns Promise that resolves when unregistration is complete
 */
export async function unregisterServiceWorker(): Promise<boolean> {
  if (!('serviceWorker' in navigator)) {
    return false;
  }

  try {
    const registration = await navigator.serviceWorker.ready;
    const success = await registration.unregister();

    if (success) {
      console.log('[PWA] Service worker unregistered successfully');
    }

    return success;
  } catch (error) {
    console.error('[PWA] Failed to unregister service worker:', error);
    return false;
  }
}

/**
 * Prompts the user to update the app when a new version is available
 *
 * @param registration - The service worker registration
 */
export function updateServiceWorker(
  registration: ServiceWorkerRegistration
): void {
  const waitingWorker = registration.waiting;

  if (!waitingWorker) {
    console.warn('[PWA] No waiting service worker found');
    return;
  }

  console.log('[PWA] Skipping waiting and activating new service worker...');

  waitingWorker.postMessage({ type: 'SKIP_WAITING' });

  navigator.serviceWorker.addEventListener('controllerchange', () => {
    console.log('[PWA] New service worker took control, reloading page...');
    window.location.reload();
  });
}

/**
 * Checks if the app is running in standalone mode (installed as PWA)
 *
 * @returns true if running as installed PWA
 */
export function isRunningAsInstalledPWA(): boolean {
  const isStandalone = window.matchMedia('(display-mode: standalone)').matches;

  const isIOSStandalone = (window.navigator as any).standalone === true;

  return isStandalone || isIOSStandalone;
}

/**
 * Checks if the app can be installed as a PWA
 *
 * @returns true if installation prompt is available
 */
export function canInstallPWA(): boolean {
  return 'BeforeInstallPromptEvent' in window;
}

/**
 * Shows the PWA installation prompt
 *
 * @param promptEvent - The beforeinstallprompt event
 * @returns Promise that resolves to the user's choice
 */
export async function showInstallPrompt(
  promptEvent: any
): Promise<'accepted' | 'dismissed'> {
  if (!promptEvent) {
    console.warn('[PWA] No install prompt event available');
    return 'dismissed';
  }

  try {
    await promptEvent.prompt();

    const { outcome } = await promptEvent.userChoice;

    console.log('[PWA] Install prompt outcome:', outcome);

    return outcome;
  } catch (error) {
    console.error('[PWA] Failed to show install prompt:', error);
    return 'dismissed';
  }
}

/**
 * Clears all service worker caches
 *
 * @returns Promise that resolves when caches are cleared
 */
export async function clearAllCaches(): Promise<void> {
  if (!('caches' in window)) {
    console.warn('[PWA] Cache API not supported');
    return;
  }

  try {
    const cacheNames = await caches.keys();
    await Promise.all(cacheNames.map((name) => caches.delete(name)));
    console.log('[PWA] All caches cleared');
  } catch (error) {
    console.error('[PWA] Failed to clear caches:', error);
  }
}

/**
 * Gets the current service worker status
 *
 * @returns Promise that resolves to the service worker status
 */
export async function getServiceWorkerStatus(): Promise<{
  supported: boolean;
  registered: boolean;
  controller: boolean;
  waiting: boolean;
}> {
  if (!('serviceWorker' in navigator)) {
    return {
      supported: false,
      registered: false,
      controller: false,
      waiting: false,
    };
  }

  try {
    const registration = await navigator.serviceWorker.getRegistration();

    return {
      supported: true,
      registered: !!registration,
      controller: !!navigator.serviceWorker.controller,
      waiting: !!registration?.waiting,
    };
  } catch (error) {
    console.error('[PWA] Failed to get service worker status:', error);
    return {
      supported: true,
      registered: false,
      controller: false,
      waiting: false,
    };
  }
}

/**
 * Sends a message to the service worker
 *
 * @param message - The message to send
 */
export function sendMessageToServiceWorker(message: any): void {
  if (!navigator.serviceWorker.controller) {
    console.warn('[PWA] No service worker controller available');
    return;
  }

  navigator.serviceWorker.controller.postMessage(message);
}

/**
 * Pre-caches a list of URLs
 *
 * @param urls - Array of URLs to cache
 */
export function precacheUrls(urls: string[]): void {
  sendMessageToServiceWorker({
    type: 'CACHE_URLS',
    urls,
  });
}
