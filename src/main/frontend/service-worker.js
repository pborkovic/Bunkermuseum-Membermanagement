/**
 * Service Worker for Bunkermuseum Mitgliederverwaltung PWA
 *
 * Implements caching strategies for optimal performance:
 * - Cache-first for static assets (JS, CSS, images, fonts)
 * - Network-first for API calls with cache fallback
 * - Offline fallback page when network is unavailable
 *
 * @author Philipp Borkovic
 */

// Cache version - increment when you want to force cache refresh
const CACHE_VERSION = 'v1.0.0';
const CACHE_NAME = `bunkermuseum-cache-${CACHE_VERSION}`;
const OFFLINE_CACHE = `bunkermuseum-offline-${CACHE_VERSION}`;
const API_CACHE = `bunkermuseum-api-${CACHE_VERSION}`;

// Resources to cache immediately on install
const STATIC_CACHE_URLS = [
  '/',
  '/index.html',
  '/manifest.json',
  '/offline.html',
];

// API endpoints that should be cached (for offline fallback)
const API_CACHE_URLS = [
  '/api/auth/current-user',
];

// Cache duration in milliseconds
const CACHE_DURATION = {
  SHORT: 5 * 60 * 1000,      // 5 minutes
  MEDIUM: 60 * 60 * 1000,    // 1 hour
  LONG: 24 * 60 * 60 * 1000, // 24 hours
};

/**
 * Install event - cache essential resources
 */
self.addEventListener('install', (event) => {
  console.log('[Service Worker] Installing...');

  event.waitUntil(
    Promise.all([
      // Cache static resources
      caches.open(CACHE_NAME).then((cache) => {
        console.log('[Service Worker] Caching static resources');
        return cache.addAll(STATIC_CACHE_URLS);
      }),
      // Cache offline page
      caches.open(OFFLINE_CACHE).then((cache) => {
        console.log('[Service Worker] Caching offline page');
        return cache.add('/offline.html');
      }),
    ]).then(() => {
      console.log('[Service Worker] Installation complete');
      // Force the waiting service worker to become active
      return self.skipWaiting();
    })
  );
});

/**
 * Activate event - clean up old caches
 */
self.addEventListener('activate', (event) => {
  console.log('[Service Worker] Activating...');

  event.waitUntil(
    caches.keys().then((cacheNames) => {
      return Promise.all(
        cacheNames.map((cacheName) => {
          // Delete old cache versions
          if (cacheName.startsWith('bunkermuseum-') &&
              cacheName !== CACHE_NAME &&
              cacheName !== OFFLINE_CACHE &&
              cacheName !== API_CACHE) {
            console.log('[Service Worker] Deleting old cache:', cacheName);
            return caches.delete(cacheName);
          }
        })
      );
    }).then(() => {
      console.log('[Service Worker] Activation complete');
      // Take control of all clients immediately
      return self.clients.claim();
    })
  );
});

/**
 * Fetch event - implement caching strategies
 */
self.addEventListener('fetch', (event) => {
  const { request } = event;
  const url = new URL(request.url);

  // Skip non-GET requests
  if (request.method !== 'GET') {
    return;
  }

  // Skip Chrome extension requests
  if (url.protocol === 'chrome-extension:') {
    return;
  }

  // Handle different types of requests
  if (url.pathname.startsWith('/api/')) {
    // API requests: Network-first with cache fallback
    event.respondWith(networkFirstWithCache(request));
  } else if (isStaticAsset(url.pathname)) {
    // Static assets: Cache-first with network fallback
    event.respondWith(cacheFirstWithNetwork(request));
  } else if (url.pathname.startsWith('/VAADIN/')) {
    // Vaadin resources: Cache-first
    event.respondWith(cacheFirstWithNetwork(request));
  } else {
    // HTML pages: Network-first with offline fallback
    event.respondWith(networkFirstWithOfflineFallback(request));
  }
});

/**
 * Cache-first strategy: Serve from cache, fallback to network
 */
async function cacheFirstWithNetwork(request) {
  const cache = await caches.open(CACHE_NAME);
  const cached = await cache.match(request);

  if (cached) {
    console.log('[Service Worker] Serving from cache:', request.url);
    return cached;
  }

  try {
    console.log('[Service Worker] Fetching from network:', request.url);
    const response = await fetch(request);

    // Cache successful responses
    if (response.ok) {
      cache.put(request, response.clone());
    }

    return response;
  } catch (error) {
    console.error('[Service Worker] Fetch failed:', error);
    throw error;
  }
}

/**
 * Network-first strategy with cache fallback (for API calls)
 */
async function networkFirstWithCache(request) {
  const cache = await caches.open(API_CACHE);

  try {
    console.log('[Service Worker] Fetching API from network:', request.url);
    const response = await fetch(request);

    // Cache successful API responses
    if (response.ok) {
      cache.put(request, response.clone());
    }

    return response;
  } catch (error) {
    console.warn('[Service Worker] Network failed, trying cache:', request.url);
    const cached = await cache.match(request);

    if (cached) {
      console.log('[Service Worker] Serving API from cache:', request.url);
      return cached;
    }

    console.error('[Service Worker] No cache available for:', request.url);
    throw error;
  }
}

/**
 * Network-first with offline fallback (for HTML pages)
 */
async function networkFirstWithOfflineFallback(request) {
  try {
    console.log('[Service Worker] Fetching page from network:', request.url);
    const response = await fetch(request);

    // Cache successful page responses
    if (response.ok) {
      const cache = await caches.open(CACHE_NAME);
      cache.put(request, response.clone());
    }

    return response;
  } catch (error) {
    console.warn('[Service Worker] Network failed, trying cache:', request.url);
    const cache = await caches.open(CACHE_NAME);
    const cached = await cache.match(request);

    if (cached) {
      console.log('[Service Worker] Serving page from cache:', request.url);
      return cached;
    }

    // Return offline page as last resort
    console.log('[Service Worker] Serving offline page');
    const offlineCache = await caches.open(OFFLINE_CACHE);
    return offlineCache.match('/offline.html');
  }
}

/**
 * Check if URL is a static asset
 */
function isStaticAsset(pathname) {
  const staticExtensions = [
    '.js', '.css', '.png', '.jpg', '.jpeg', '.svg', '.gif',
    '.webp', '.woff', '.woff2', '.ttf', '.eot', '.ico'
  ];

  return staticExtensions.some(ext => pathname.endsWith(ext));
}

/**
 * Message event - handle messages from the app
 */
self.addEventListener('message', (event) => {
  if (event.data && event.data.type === 'SKIP_WAITING') {
    console.log('[Service Worker] Received SKIP_WAITING message');
    self.skipWaiting();
  }

  if (event.data && event.data.type === 'CACHE_URLS') {
    console.log('[Service Worker] Caching additional URLs:', event.data.urls);
    const urls = event.data.urls;
    caches.open(CACHE_NAME).then((cache) => {
      cache.addAll(urls);
    });
  }

  if (event.data && event.data.type === 'CLEAR_CACHE') {
    console.log('[Service Worker] Clearing all caches');
    caches.keys().then((cacheNames) => {
      return Promise.all(
        cacheNames.map((cacheName) => caches.delete(cacheName))
      );
    });
  }
});

/**
 * Background sync event (for future use)
 */
self.addEventListener('sync', (event) => {
  console.log('[Service Worker] Background sync:', event.tag);

  if (event.tag === 'sync-data') {
    event.waitUntil(syncData());
  }
});

/**
 * Sync data when back online (placeholder for future implementation)
 */
async function syncData() {
  console.log('[Service Worker] Syncing data...');
  // Implement your sync logic here
  // For example: retry failed API requests from IndexedDB
}

/**
 * Push notification event (for future use)
 */
self.addEventListener('push', (event) => {
  console.log('[Service Worker] Push notification received');

  const options = {
    body: event.data ? event.data.text() : 'Neue Benachrichtigung',
    icon: '/icons/icon-192x192.png',
    badge: '/icons/icon-72x72.png',
    vibrate: [200, 100, 200],
    tag: 'bunkermuseum-notification',
    requireInteraction: false,
  };

  event.waitUntil(
    self.registration.showNotification('Bunkermuseum', options)
  );
});

/**
 * Notification click event
 */
self.addEventListener('notificationclick', (event) => {
  console.log('[Service Worker] Notification clicked');
  event.notification.close();

  event.waitUntil(
    clients.openWindow('/')
  );
});

console.log('[Service Worker] Script loaded');
