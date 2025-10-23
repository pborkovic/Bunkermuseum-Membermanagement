import { useState, useEffect } from 'react';
import { MOBILE_BREAKPOINT, RESIZE_DEBOUNCE_DELAY } from '../utils/constants';
import type { WindowSize } from '../types';

/**
 * Custom hook that tracks window dimensions and breakpoint states.
 * Implements debouncing to optimize performance during resize events.
 *
 * @returns {WindowSize} Current window size and breakpoint information
 *
 * @author Philipp Borkovic
 */
export function useWindowSize(): WindowSize {
  const [windowSize, setWindowSize] = useState<WindowSize>(() => {
    const width = typeof window !== 'undefined' ? window.innerWidth : 0;
    const height = typeof window !== 'undefined' ? window.innerHeight : 0;

    return {
      width,
      height,
      isMobile: width < MOBILE_BREAKPOINT,
      isTablet: width >= MOBILE_BREAKPOINT && width < 1024,
      isDesktop: width >= 1024,
    };
  });

  useEffect(() => {
    let timeoutId: NodeJS.Timeout | null = null;

    /**
     * Debounced handler for window resize events.
     * Prevents excessive state updates during rapid resize operations.
     */
    const handleResize = (): void => {
      if (timeoutId) {
        clearTimeout(timeoutId);
      }

      timeoutId = setTimeout(() => {
        const width = window.innerWidth;
        const height = window.innerHeight;

        setWindowSize({
          width,
          height,
          isMobile: width < MOBILE_BREAKPOINT,
          isTablet: width >= MOBILE_BREAKPOINT && width < 1024,
          isDesktop: width >= 1024,
        });
      }, RESIZE_DEBOUNCE_DELAY);
    };

    window.addEventListener('resize', handleResize);

    return () => {
      if (timeoutId) {
        clearTimeout(timeoutId);
      }
      window.removeEventListener('resize', handleResize);
    };
  }, []);

  return windowSize;
}
