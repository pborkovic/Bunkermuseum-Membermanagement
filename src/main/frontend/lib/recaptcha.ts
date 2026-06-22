/**
 * Google reCAPTCHA v3 client helper.
 *
 * reCAPTCHA v3 runs invisibly: there is no checkbox or widget. The script is
 * loaded once with the configured site key, and a fresh token is generated on
 * demand via {@link executeRecaptcha} for a given action. The token is sent to
 * the backend, which verifies the score and action with Google's API.
 *
 * If no site key is configured (e.g. local development), all functions degrade
 * gracefully by returning an empty token. The backend treats a blank token as
 * optional, so registration still works without reCAPTCHA configured.
 *
 * @author Philipp Borkovic
 */

declare global {
  interface Window {
    grecaptcha?: {
      ready: (callback: () => void) => void;
      execute: (siteKey: string, options: { action: string }) => Promise<string>;
    };
  }
}

const SITE_KEY = import.meta.env.VITE_RECAPTCHA_SITE_KEY;
const PLACEHOLDER_KEYS = ['', 'your-site-key-here'];

let scriptPromise: Promise<void> | null = null;

/**
 * Whether a usable reCAPTCHA site key is configured.
 */
function isConfigured(): boolean {
  return Boolean(SITE_KEY) && !PLACEHOLDER_KEYS.includes(SITE_KEY);
}

/**
 * Lazily injects the reCAPTCHA v3 script tag. Resolves once the script has
 * loaded (or immediately if it is already present / not configured).
 */
function loadScript(): Promise<void> {
  if (!isConfigured()) {
    return Promise.resolve();
  }

  if (scriptPromise) {
    return scriptPromise;
  }

  scriptPromise = new Promise<void>((resolve, reject) => {
    if (window.grecaptcha) {
      resolve();
      return;
    }

    const script = document.createElement('script');
    script.src = `https://www.google.com/recaptcha/api.js?render=${SITE_KEY}`;
    script.async = true;
    script.defer = true;
    script.dataset.recaptcha = 'true';
    script.onload = () => resolve();
    script.onerror = () => {
      scriptPromise = null;
      reject(new Error('Failed to load reCAPTCHA'));
    };
    document.head.appendChild(script);
  });

  return scriptPromise;
}

/**
 * Generates a reCAPTCHA v3 token for the given action.
 *
 * Never throws: on any failure (not configured, script blocked, execution
 * error) it resolves to an empty string so the caller can still submit. The
 * backend decides whether a blank token is acceptable.
 *
 * @param action a short, descriptive action name (e.g. "register")
 * @returns the reCAPTCHA token, or an empty string when unavailable
 */
export async function executeRecaptcha(action: string): Promise<string> {
  if (!isConfigured()) {
    return '';
  }

  try {
    await loadScript();

    const { grecaptcha } = window;
    if (!grecaptcha) {
      return '';
    }

    await new Promise<void>((resolve) => grecaptcha.ready(resolve));

    return await grecaptcha.execute(SITE_KEY, { action });
  } catch {
    return '';
  }
}
