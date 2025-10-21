import { Icon } from '@vaadin/react-components';
import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import type Booking from 'Frontend/generated/com/bunkermuseum/membermanagement/model/Booking';

/**
 * Pagination component props.
 */
interface PaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

/**
 * Pagination component - Simple previous/current/next design.
 *
 * @author Philipp Borkovic
 */
function Pagination({ currentPage, totalPages, onPageChange }: PaginationProps) {
  if (totalPages <= 1) return null;

  return (
    <div className="flex items-center justify-center gap-2">
      {/* Previous Button */}
      <Button
        variant="outline"
        size="sm"
        onClick={() => onPageChange(currentPage - 1)}
        disabled={currentPage === 1}
        className="h-9 w-9 p-0 border-black hover:bg-black hover:text-white disabled:opacity-30 disabled:border-gray-300"
      >
        <Icon icon="vaadin:angle-left" style={{ width: '18px', height: '18px' }} />
      </Button>

      {/* Current Page Display */}
      <div className="flex items-center gap-2 px-4 py-2 min-w-[80px] justify-center">
        <span className="text-sm font-medium text-black">
          {currentPage} / {totalPages}
        </span>
      </div>

      {/* Next Button */}
      <Button
        variant="outline"
        size="sm"
        onClick={() => onPageChange(currentPage + 1)}
        disabled={currentPage === totalPages}
        className="h-9 w-9 p-0 border-black hover:bg-black hover:text-white disabled:opacity-30 disabled:border-gray-300"
      >
        <Icon icon="vaadin:angle-right" style={{ width: '18px', height: '18px' }} />
      </Button>
    </div>
  );
}

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
 * Formats a date to German locale string.
 *
 * @param {string | null | undefined} dateString - The date string to format
 * @returns {string} Formatted date or 'N/A'
 *
 * @author Philipp Borkovic
 */
const formatDate = (dateString: string | null | undefined): string => {
  if (!dateString) return 'N/A';
  return new Date(dateString).toLocaleDateString('de-DE');
};

/**
 * Formats a number as currency in EUR.
 *
 * @param {number | null | undefined} amount - The amount to format
 * @returns {string} Formatted currency or 'N/A'
 *
 * @author Philipp Borkovic
 */
const formatCurrency = (amount: number | null | undefined): string => {
  if (amount === null || amount === undefined) return 'N/A';
  return new Intl.NumberFormat('de-DE', {
    style: 'currency',
    currency: 'EUR',
  }).format(amount);
};

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
 *
 * @component
 *
 * @author Philipp Borkovic
 */
export default function BookingsList({
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
  // Calculate display range
  const startIndex = (currentPage - 1) * bookingsPerPage + 1;
  const endIndex = Math.min(currentPage * bookingsPerPage, totalElements);

  if (isLoading) {
    return (
      <div className="rounded-lg bg-white flex items-center justify-center" style={{ height: '560px' }}>
        <div className="text-center space-y-3">
          <Icon
            icon="vaadin:spinner"
            className="animate-spin text-black mx-auto"
            style={{ width: '40px', height: '40px' }}
          />
          <p className="text-sm text-gray-600">Lädt Buchungen...</p>
        </div>
      </div>
    );
  }

  if (bookings.length === 0) {
    return (
      <div className="rounded-lg bg-white flex items-center justify-center" style={{ height: '560px' }}>
        <div className="text-center space-y-4">
          <Icon
            icon="vaadin:invoice"
            className="text-gray-300 mx-auto"
            style={{ width: '64px', height: '64px' }}
          />
          <div>
            <p className="text-base font-medium text-gray-900">
              {hasActiveFilters ? 'Keine Ergebnisse gefunden' : 'Keine Buchungen vorhanden'}
            </p>
            <p className="text-sm text-gray-500 mt-1">
              {hasActiveFilters
                ? 'Keine Buchungen entsprechen den aktuellen Filtern'
                : 'Es wurden noch keine Buchungen erstellt'}
            </p>
          </div>
        </div>
      </div>
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
        <div className="text-sm text-gray-600 flex-shrink-0">
          Zeige <span className="font-medium text-black">{startIndex}</span> bis{' '}
          <span className="font-medium text-black">{endIndex}</span> von{' '}
          <span className="font-medium text-black">{totalElements}</span> Buchungen
          {hasActiveFilters && (
            <span className="text-gray-500"> (gefiltert)</span>
          )}
        </div>

        {/* Pagination Controls - Right side */}
        {totalPages > 1 && (
          <div className="flex-shrink-0">
            <Pagination
              currentPage={currentPage}
              totalPages={totalPages}
              onPageChange={onPageChange}
            />
          </div>
        )}
      </div>
    </div>
  );
}
