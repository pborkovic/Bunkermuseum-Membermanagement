import { memo, useMemo } from 'react';
import { Icon } from '@vaadin/react-components';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import type Booking from 'Frontend/generated/com/bunkermuseum/membermanagement/model/Booking';
import Pagination from './shared/_Pagination';
import PaginationInfo from './shared/_PaginationInfo';
import LoadingState from './shared/_LoadingState';
import EmptyState from './shared/_EmptyState';
import { formatDate, formatCurrency } from '../utils/formatting';
import { LIST_CONTAINER_HEIGHT } from '../utils/constants';

/**
 * BookingsList component props.
 */
interface BookingsListProps {
  bookings: Booking[];
  isLoading: boolean;
  searchQuery: string;
  hasActiveFilters: boolean;
  currentPage: number;
  totalPages: number;
  totalElements: number;
  bookingsPerPage: number;
  isMobile: boolean;
  onBookingClick: (booking: Booking) => void;
  onDeleteClick: (booking: Booking) => void;
  onPageChange: (page: number) => void;
}


/**
 * BookingsList component - Displays bookings in a table/cards with pagination.
 *
 * Clean black & white design matching the UsersTab style.
 *
 * Features:
 * - Responsive table/card layout
 * - Pagination controls
 * - Action menu per booking
 * - Loading and empty states
 * - Search result indicators
 * - Memoized for performance optimization
 *
 * @component
 *
 * @author Philipp Borkovic
 */
function BookingsList({
  bookings,
  isLoading,
  searchQuery,
  hasActiveFilters,
  currentPage,
  totalPages,
  totalElements,
  bookingsPerPage,
  isMobile,
  onBookingClick,
  onDeleteClick,
  onPageChange,
}: BookingsListProps): JSX.Element {
  /**
   * Calculate display range for pagination info.
   * Memoized to prevent unnecessary recalculations.
   */
  const { startIndex, endIndex } = useMemo(() => ({
    startIndex: (currentPage - 1) * bookingsPerPage + 1,
    endIndex: Math.min(currentPage * bookingsPerPage, totalElements),
  }), [currentPage, bookingsPerPage, totalElements]);

  /**
   * Determine empty state message based on filters.
   */
  const emptyStateMessage = useMemo(() => ({
    title: hasActiveFilters ? 'Keine Ergebnisse gefunden' : 'Keine Buchungen vorhanden',
    description: hasActiveFilters
      ? 'Keine Buchungen entsprechen den aktuellen Filtern'
      : 'Es wurden noch keine Buchungen erstellt',
  }), [hasActiveFilters]);

  if (isLoading) {
    return <LoadingState message="Lädt Buchungen..." className={`h-[${LIST_CONTAINER_HEIGHT}px]`} />;
  }

  if (bookings.length === 0) {
    return (
      <EmptyState
        icon="vaadin:invoice"
        title={emptyStateMessage.title}
        description={emptyStateMessage.description}
        className={`h-[${LIST_CONTAINER_HEIGHT}px]`}
      />
    );
  }

  return (
    <div>
      {/* Bookings Grid - Fixed height for 10 entries */}
      <div className="rounded-lg overflow-hidden bg-white" style={{ height: '560px' }}>
        {/* Desktop Table */}
        <div className="hidden md:block h-full overflow-y-auto">
          <table className="w-full">
            <thead className="sticky top-0 bg-white z-10">
              <tr className="border-b border-gray-200">
                <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700 bg-white">Code</th>
                <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700 bg-white">Mitglied</th>
                <th className="text-right py-3 px-4 text-sm font-semibold text-gray-700 bg-white">Erwarteter Betrag</th>
                <th className="text-right py-3 px-4 text-sm font-semibold text-gray-700 bg-white">Tatsächlicher Betrag</th>
                <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700 bg-white">Empfangen am</th>
                <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700 bg-white">Erstellt am</th>
                <th className="text-center py-3 px-4 text-sm font-semibold text-gray-700 bg-white">Aktionen</th>
              </tr>
            </thead>
            <tbody>
              {bookings.map((booking) => (
                <tr
                  key={booking.id}
                  className="border-b border-gray-100 hover:bg-gray-50"
                >
                  <td className="py-3 px-4 text-sm text-gray-900 font-mono">{booking.code || 'N/A'}</td>
                  <td className="py-3 px-4 text-sm text-gray-900">{booking.ofMG || 'N/A'}</td>
                  <td className="py-3 px-4 text-sm text-gray-900 text-right font-medium">
                    {formatCurrency(booking.expectedAmount)}
                  </td>
                  <td className="py-3 px-4 text-sm text-gray-900 text-right font-medium">
                    {formatCurrency(booking.actualAmount)}
                  </td>
                  <td className="py-3 px-4 text-sm text-gray-600">{formatDate(booking.receivedAt)}</td>
                  <td className="py-3 px-4 text-sm text-gray-600">{formatDate(booking.createdAt)}</td>
                  <td className="py-3 px-4 text-center">
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <button
                          onClick={(e) => e.stopPropagation()}
                          className="p-2 hover:bg-gray-100 rounded-md transition-colors"
                          aria-label="Aktionen"
                        >
                          <Icon
                            icon="vaadin:ellipsis-dots-v"
                            className="text-black"
                            style={{ width: '18px', height: '18px' }}
                          />
                        </button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end" className="bg-white border-black">
                        <DropdownMenuItem
                          onClick={() => onBookingClick(booking)}
                          className="text-black hover:bg-gray-100 focus:bg-gray-100 cursor-pointer"
                        >
                          <Icon
                            icon="vaadin:eye"
                            className="mr-2"
                            style={{ width: '16px', height: '16px' }}
                          />
                          Details anzeigen
                        </DropdownMenuItem>
                        <DropdownMenuItem
                          onClick={() => onDeleteClick(booking)}
                          className="text-red-600 hover:bg-gray-100 focus:bg-gray-100 focus:text-red-600 cursor-pointer"
                        >
                          <Icon
                            icon="vaadin:trash"
                            className="mr-2"
                            style={{ width: '16px', height: '16px' }}
                          />
                          Löschen
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Mobile Cards */}
        <div className="md:hidden space-y-3 p-4 h-full overflow-y-auto">
          {bookings.map((booking) => (
            <div
              key={booking.id}
              className="border border-gray-200 rounded-lg p-4 hover:border-gray-300"
            >
              <div className="flex justify-between items-start mb-2">
                <div className="flex-1">
                  <div className="font-medium text-gray-900">{booking.code || 'Kein Code'}</div>
                  <div className="text-sm text-gray-600">{booking.ofMG || 'N/A'}</div>
                </div>
                <DropdownMenu>
                  <DropdownMenuTrigger asChild>
                    <button
                      onClick={(e) => e.stopPropagation()}
                      className="p-2 hover:bg-gray-100 rounded-md transition-colors"
                      aria-label="Aktionen"
                    >
                      <Icon
                        icon="vaadin:ellipsis-dots-v"
                        className="text-black"
                        style={{ width: '18px', height: '18px' }}
                      />
                    </button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent align="end" className="bg-white border-black">
                    <DropdownMenuItem
                      onClick={() => onBookingClick(booking)}
                      className="text-black hover:bg-gray-100 focus:bg-gray-100 cursor-pointer"
                    >
                      <Icon
                        icon="vaadin:eye"
                        className="mr-2"
                        style={{ width: '16px', height: '16px' }}
                      />
                      Details anzeigen
                    </DropdownMenuItem>
                    <DropdownMenuItem
                      onClick={() => onDeleteClick(booking)}
                      className="text-red-600 hover:bg-gray-100 focus:bg-gray-100 focus:text-red-600 cursor-pointer"
                    >
                      <Icon
                        icon="vaadin:trash"
                        className="mr-2"
                        style={{ width: '16px', height: '16px' }}
                      />
                      Löschen
                    </DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>
              </div>
              <div className="grid grid-cols-2 gap-2 text-sm">
                <div>
                  <div className="text-gray-600">Erwartet:</div>
                  <div className="font-medium">{formatCurrency(booking.expectedAmount)}</div>
                </div>
                <div>
                  <div className="text-gray-600">Tatsächlich:</div>
                  <div className="font-medium">{formatCurrency(booking.actualAmount)}</div>
                </div>
              </div>
              <div className="mt-2 text-xs text-gray-500">
                Empfangen: {formatDate(booking.receivedAt)}
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Pagination Footer */}
      <div className="flex flex-col-reverse sm:flex-row items-center justify-between gap-4 px-1 pt-4">
        {/* Results Info - Left side */}
        <PaginationInfo
          startIndex={startIndex}
          endIndex={endIndex}
          totalElements={totalElements}
          itemLabel="Buchungen"
          isFiltered={hasActiveFilters}
        />

        {/* Pagination Controls - Right side */}
        <Pagination
          currentPage={currentPage}
          totalPages={totalPages}
          onPageChange={onPageChange}
        />
      </div>
    </div>
  );
}

/**
 * Memoized version of BookingsList component.
 * Only re-renders when props change.
 */
export default memo(BookingsList);
