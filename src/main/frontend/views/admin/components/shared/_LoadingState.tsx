import { memo } from 'react';
import { FaSpinner } from 'react-icons/fa';
import type { LoadingStateProps } from '../../types';

/**
 * Loading state component for displaying during async operations.
 * Provides consistent loading indicators across the application.
 *
 * Features:
 * - Animated spinner icon
 * - Customizable loading message
 * - Clean, centered layout
 * - Accessible screen reader text
 *
 * @component
 *
 * @param {LoadingStateProps} props - Component props
 * @returns {JSX.Element} Rendered loading state display
 *
 * @author Philipp Borkovic
 */
function LoadingState({ message, className = '' }: LoadingStateProps): JSX.Element {
  return (
    <div
      className={`rounded-lg bg-white flex items-center justify-center ${className}`}
      role="status"
      aria-live="polite"
    >
      <div className="text-center space-y-3">
        {/* Spinner Icon */}
        <FaSpinner
          className="animate-spin text-black mx-auto"
          style={{ width: '40px', height: '40px' }}
          aria-hidden="true"
        />

        {/* Loading Message */}
        <p className="text-sm text-gray-600">{message}</p>
      </div>
    </div>
  );
}

/**
 * Memoized version of the LoadingState component.
 * Only re-renders when message or className change.
 *
 * @author Philipp Borkovic
 */
export default memo(LoadingState);
