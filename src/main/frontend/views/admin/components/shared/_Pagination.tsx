import { memo } from 'react';
import { FaAngleLeft, FaAngleRight } from 'react-icons/fa';
import { Button } from '@/components/ui/button';

/**
 * Props for the Pagination component.
 *
 * @interface PaginationProps
 * @property {number} currentPage - Current page number (1-indexed)
 * @property {number} totalPages - Total number of pages available
 * @property {(page: number) => void} onPageChange - Callback when page navigation is requested
 * @property {string} [className] - Optional additional CSS classes
 */
interface PaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
  className?: string;
}

/**
 * Pagination component for navigating through paginated content.
 * Displays previous/next buttons and current page indicator.
 *
 * Features:
 * - Clean black & white design
 * - Disabled state for boundary pages
 * - Accessible button labels
 * - Memoized for performance optimization
 *
 * @component
 *
 * @param {PaginationProps} props - Component props
 * @returns {JSX.Element | null} Rendered pagination controls or null if only one page
 *
 * @author Philipp Borkovic
 */
function Pagination({
  currentPage,
  totalPages,
  onPageChange,
  className = '',
}: PaginationProps): JSX.Element | null {
  if (totalPages <= 1) {
    return null;
  }

  const isFirstPage = currentPage === 1;
  const isLastPage = currentPage === totalPages;

  return (
    <div className={`flex items-center justify-center gap-2 ${className}`}>
      {/* Previous Button */}
      <Button
        variant="outline"
        size="sm"
        onClick={() => onPageChange(currentPage - 1)}
        disabled={isFirstPage}
        className="h-9 w-9 p-0 border-black hover:bg-black hover:text-white disabled:opacity-30 disabled:border-gray-300"
        aria-label="Vorherige Seite"
      >
        <FaAngleLeft style={{ width: '18px', height: '18px' }} />
      </Button>

      {/* Current Page Display */}
      <div
        className="flex items-center gap-2 px-4 py-2 min-w-[80px] justify-center"
        aria-label={`Seite ${currentPage} von ${totalPages}`}
      >
        <span className="text-sm font-medium text-black">
          {currentPage} / {totalPages}
        </span>
      </div>

      {/* Next Button */}
      <Button
        variant="outline"
        size="sm"
        onClick={() => onPageChange(currentPage + 1)}
        disabled={isLastPage}
        className="h-9 w-9 p-0 border-black hover:bg-black hover:text-white disabled:opacity-30 disabled:border-gray-300"
        aria-label="Nächste Seite"
      >
        <FaAngleRight style={{ width: '18px', height: '18px' }} />
      </Button>
    </div>
  );
}

/**
 * Memoized version of the Pagination component.
 * Only re-renders when currentPage or totalPages change.
 *
 * @author Philipp Borkovic
 */
export default memo(Pagination);
