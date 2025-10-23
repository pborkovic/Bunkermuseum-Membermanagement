import { memo } from 'react';
import { Icon } from '@vaadin/react-components';
import type { EmptyStateProps } from '../../types';

/**
 * Empty state component for displaying when no data is available.
 * Provides consistent, user-friendly messaging across the application.
 *
 * Features:
 * - Large icon for visual clarity
 * - Title and description text
 * - Clean, centered layout
 * - Customizable via className prop
 *
 * @component
 *
 * @param {EmptyStateProps} props - Component props
 * @returns {JSX.Element} Rendered empty state display
 *
 * @author Philipp Borkovic
 */
function EmptyState({
  icon,
  title,
  description,
  className = '',
}: EmptyStateProps): JSX.Element {
  return (
    <div className={`rounded-lg bg-white flex items-center justify-center ${className}`}>
      <div className="text-center space-y-4">
        {/* Icon */}
        <Icon
          icon={icon}
          className="text-gray-300 mx-auto"
          style={{ width: '64px', height: '64px' }}
          aria-hidden="true"
        />

        {/* Text Content */}
        <div>
          <p className="text-base font-medium text-gray-900">{title}</p>
          <p className="text-sm text-gray-500 mt-1">{description}</p>
        </div>
      </div>
    </div>
  );
}

/**
 * Memoized version of the EmptyState component.
 * Only re-renders when props change.
 *
 * @author Philipp Borkovic
 */
export default memo(EmptyState);
