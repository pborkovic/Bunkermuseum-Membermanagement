import { memo } from 'react';

/**
 * Props for the PaginationInfo component.
 *
 * @interface PaginationInfoProps
 * @property {number} startIndex - First item number on current page (1-indexed)
 * @property {number} endIndex - Last item number on current page (1-indexed)
 * @property {number} totalElements - Total number of items across all pages
 * @property {string} itemLabel - Label for items (e.g., "Mitglieder", "Buchungen")
 * @property {boolean} [isFiltered] - Whether results are currently filtered
 * @property {string} [className] - Optional additional CSS classes
 */
interface PaginationInfoProps {
  startIndex: number;
  endIndex: number;
  totalElements: number;
  itemLabel: string;
  isFiltered?: boolean;
  className?: string;
}

/**
 * Pagination information component.
 * Displays "Showing X to Y of Z items" text.
 *
 * Features:
 * - Clear indication of current range
 * - Filtered state indicator
 * - German localization
 * - Responsive text sizing
 *
 * @component
 *
 * @param {PaginationInfoProps} props - Component props
 * @returns {JSX.Element} Rendered pagination information
 *
 * @author Philipp Borkovic
 */
function PaginationInfo({
  startIndex,
  endIndex,
  totalElements,
  itemLabel,
  isFiltered = false,
  className = '',
}: PaginationInfoProps): JSX.Element {
  return (
    <div className={`text-sm text-gray-600 flex-shrink-0 ${className}`}>
      Zeige <span className="font-medium text-black">{startIndex}</span> bis{' '}
      <span className="font-medium text-black">{endIndex}</span> von{' '}
      <span className="font-medium text-black">{totalElements}</span> {itemLabel}
      {isFiltered && <span className="text-gray-500"> (gefiltert)</span>}
    </div>
  );
}

/**
 * Memoized version of the PaginationInfo component.
 * Only re-renders when props change.
 *
 * @author Philipp Borkovic
 */
export default memo(PaginationInfo);
