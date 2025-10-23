import { useState, useCallback, useMemo } from 'react';
import { DEFAULT_PAGE_SIZE } from '../utils/constants';
import type { PaginationState, PaginationActions } from '../types';

/**
 * Return type for the usePagination hook.
 *
 * @interface UsePaginationReturn
 *
 * @extends {PaginationState}
 * @extends {PaginationActions}
 */
interface UsePaginationReturn extends PaginationState, PaginationActions {
  /**
   * Calculate which items to display based on current page.
   *
   * @template T
   *
   * @param {T[]} items - The full array of items to paginate
   *
   * @returns {T[]} The items for the current page
   */
  getPaginatedItems: <T>(items: T[]) => T[];

  /**
   * Reset pagination to the first page.
   * Useful when filters or search query changes.
   */
  reset: () => void;
}

/**
 * Configuration options for the usePagination hook.
 *
 * @interface UsePaginationOptions
 *
 * @property {number} [initialPageSize] - Initial number of items per page
 * @property {number} [initialPage] - Initial page number (1-indexed)
 */
interface UsePaginationOptions {
  initialPageSize?: number;
  initialPage?: number;
}

/**
 * Custom hook for managing pagination state and logic.
 * Provides comprehensive pagination functionality with type safety.
 *
 * @param {number} totalItems - Total number of items to paginate
 * @param {UsePaginationOptions} [options] - Configuration options
 *
 * @returns {UsePaginationReturn} Pagination state and actions
 *
 * @author Philipp Borkovic
 */
export function usePagination(
  totalItems: number,
  options: UsePaginationOptions = {}
): UsePaginationReturn {
  const { initialPageSize = DEFAULT_PAGE_SIZE, initialPage = 1 } = options;

  const [currentPage, setCurrentPage] = useState<number>(initialPage);
  const [pageSize, setPageSize] = useState<number>(initialPageSize);

  /**
   * Calculate total pages based on total items and page size.
   */
  const totalPages = useMemo(() => {
    return Math.max(1, Math.ceil(totalItems / pageSize));
  }, [totalItems, pageSize]);

  /**
   * Navigate to a specific page with bounds checking.
   */
  const goToPage = useCallback(
    (page: number): void => {
      const boundedPage = Math.min(Math.max(1, page), totalPages);
      setCurrentPage(boundedPage);
    },
    [totalPages]
  );

  /**
   * Navigate to the next page if available.
   */
  const nextPage = useCallback((): void => {
    goToPage(currentPage + 1);
  }, [currentPage, goToPage]);

  /**
   * Navigate to the previous page if available.
   */
  const previousPage = useCallback((): void => {
    goToPage(currentPage - 1);
  }, [currentPage, goToPage]);

  /**
   * Change page size and reset to first page.
   */
  const handleSetPageSize = useCallback((size: number): void => {
    setPageSize(size);
    setCurrentPage(1);
  }, []);

  /**
   * Get the items for the current page from the full array.
   */
  const getPaginatedItems = useCallback(
    <T,>(items: T[]): T[] => {
      const startIndex = (currentPage - 1) * pageSize;
      const endIndex = startIndex + pageSize;
      return items.slice(startIndex, endIndex);
    },
    [currentPage, pageSize]
  );

  /**
   * Reset pagination to the first page.
   */
  const reset = useCallback((): void => {
    setCurrentPage(1);
  }, []);

  return {
    currentPage,
    totalPages,
    pageSize,
    totalElements: totalItems,
    goToPage,
    nextPage,
    previousPage,
    setPageSize: handleSetPageSize,
    getPaginatedItems,
    reset,
  };
}
