import { useState, useEffect } from 'react';
import { Dialog } from '@vaadin/react-components/Dialog';
import { Button } from '@/components/ui/button';
import { Icon } from '@vaadin/react-components';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { DatePicker } from '@/components/ui/date-picker';
import { BookingController } from 'Frontend/generated/endpoints';
import type Booking from 'Frontend/generated/com/bunkermuseum/membermanagement/model/Booking';

/**
 * Date range presets for quick filtering
 */
const DATE_RANGE_PRESETS = [
  { value: '1week', label: '1 Woche', days: 7 },
  { value: '1month', label: '1 Monat', days: 30 },
  { value: '6months', label: '6 Monate', days: 180 },
  { value: '1year', label: '1 Jahr', days: 365 },
  { value: 'custom', label: 'Benutzerdefiniert', days: 0 },
] as const;

/**
 * BookingsTab component - Displays all bookings with pagination, search, and date filtering.
 *
 * Features:
 * - Table view of all bookings (similar to UsersTab)
 * - Date range filter with predefined options
 * - Custom date range selection
 * - Pagination support
 * - Search functionality
 * - Click to open detailed booking information modal
 * - Loading and error states
 * - Responsive layout
 * - Currency formatting for amounts
 *
 * @component
 *
 * @returns {JSX.Element} The bookings tab content
 *
 * @author Philipp Borkovic
 */
export default function BookingsTab(): JSX.Element {
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [filteredBookings, setFilteredBookings] = useState<Booking[]>([]);
  const [selectedBooking, setSelectedBooking] = useState<Booking | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [isMobile, setIsMobile] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const [bookingsPerPage, setBookingsPerPage] = useState(10);
  const [dateRangePreset, setDateRangePreset] = useState('1month');
  const [startDate, setStartDate] = useState<Date | undefined>(undefined);
  const [endDate, setEndDate] = useState<Date | undefined>(undefined);

  // Detect mobile screen size
  useEffect(() => {
    const checkMobile = () => {
      setIsMobile(window.innerWidth < 768);
    };

    checkMobile();
    window.addEventListener('resize', checkMobile);
    return () => window.removeEventListener('resize', checkMobile);
  }, []);

  /**
   * Loads all bookings from the backend on component mount.
   */
  useEffect(() => {
    loadBookings();
  }, []);

  /**
   * Apply filters whenever bookings, search query, or date range changes
   */
  useEffect(() => {
    applyFilters();
  }, [bookings, searchQuery, dateRangePreset, startDate, endDate]);

  /**
   * Update date range based on preset selection
   */
  useEffect(() => {
    if (dateRangePreset !== 'custom') {
      const preset = DATE_RANGE_PRESETS.find(p => p.value === dateRangePreset);
      if (preset && preset.days > 0) {
        const end = new Date();
        const start = new Date();
        start.setDate(start.getDate() - preset.days);
        setStartDate(start);
        setEndDate(end);
      }
    }
  }, [dateRangePreset]);

  /**
   * Fetches all bookings from the BookingController.
   *
   * @author Philipp Borkovic
   */
  const loadBookings = async (): Promise<void> => {
    try {
      setIsLoading(true);
      setError('');
      const allBookings = await BookingController.getAllBookings();
      setBookings((allBookings || []).filter((booking): booking is Booking => booking !== undefined));
    } catch (err: any) {
      setError(err.message || 'Fehler beim Laden der Buchungen');
    } finally {
      setIsLoading(false);
    }
  };

  /**
   * Applies search and date range filters to bookings
   *
   * @author Philipp Borkovic
   */
  const applyFilters = (): void => {
    let filtered = [...bookings];

    // Apply search filter
    if (searchQuery.trim()) {
      const query = searchQuery.toLowerCase();
      filtered = filtered.filter(booking =>
        (booking.code?.toLowerCase().includes(query)) ||
        (booking.ofMG?.toLowerCase().includes(query)) ||
        (booking.expectedPurpose?.toLowerCase().includes(query)) ||
        (booking.actualPurpose?.toLowerCase().includes(query)) ||
        (booking.expectedAmount?.toString().includes(query)) ||
        (booking.actualAmount?.toString().includes(query))
      );
    }

    // Apply date range filter
    if (startDate && endDate) {
      filtered = filtered.filter(booking => {
        if (!booking.receivedAt) return false;
        const receivedDate = new Date(booking.receivedAt);
        return receivedDate >= startDate && receivedDate <= endDate;
      });
    }

    setFilteredBookings(filtered);
    setCurrentPage(1); // Reset to first page when filters change
  };

  /**
   * Opens the booking details modal for a specific booking.
   *
   * @param {Booking} booking - The booking to display
   *
   * @author Philipp Borkovic
   */
  const handleBookingClick = (booking: Booking): void => {
    setSelectedBooking(booking);
    setIsModalOpen(true);
  };

  /**
   * Closes the booking details modal.
   *
   * @author Philipp Borkovic
   */
  const handleCloseModal = (): void => {
    setIsModalOpen(false);
    setSelectedBooking(null);
  };

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
   * Handles page navigation with bounds checking.
   *
   * @param {number} page - The page number to navigate to
   *
   * @author Philipp Borkovic
   */
  const handlePageChange = (page: number): void => {
    const totalPages = Math.ceil(filteredBookings.length / bookingsPerPage);
    setCurrentPage(Math.min(Math.max(1, page), totalPages));
  };

  // Calculate pagination
  const totalPages = Math.ceil(filteredBookings.length / bookingsPerPage);
  const startIndex = (currentPage - 1) * bookingsPerPage;
  const endIndex = startIndex + bookingsPerPage;
  const currentBookings = filteredBookings.slice(startIndex, endIndex);

  return (
    <div className="flex flex-col h-full space-y-4">
      {/* Header with Search and Controls */}
      <div className="flex-shrink-0 flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-black">Buchungsverwaltung</h2>
          <p className="text-sm text-gray-600 mt-1">
            Übersicht aller Buchungen und Transaktionen
          </p>
        </div>

        {/* Search Bar and Controls */}
        <div className="flex flex-col sm:flex-row gap-3 sm:ml-auto">
          {/* Page Size Selector */}
          <div className="flex items-center gap-2">
            <label className="text-sm text-gray-600 whitespace-nowrap">Zeilen:</label>
            <Select
              value={bookingsPerPage.toString()}
              onValueChange={(value) => {
                setBookingsPerPage(parseInt(value));
                setCurrentPage(1);
              }}
            >
              <SelectTrigger className="w-[90px] h-9 border-black text-black [&_svg]:text-black [&_svg]:opacity-100 [&_svg]:-mt-4">
                <SelectValue />
              </SelectTrigger>
              <SelectContent className="bg-white border-black">
                <SelectItem value="5" className="text-black hover:bg-gray-100 hover:text-black focus:bg-gray-100 focus:text-black">5</SelectItem>
                <SelectItem value="10" className="text-black hover:bg-gray-100 hover:text-black focus:bg-gray-100 focus:text-black">10</SelectItem>
                <SelectItem value="25" className="text-black hover:bg-gray-100 hover:text-black focus:bg-gray-100 focus:text-black">25</SelectItem>
                <SelectItem value="50" className="text-black hover:bg-gray-100 hover:text-black focus:bg-gray-100 focus:text-black">50</SelectItem>
                <SelectItem value="100" className="text-black hover:bg-gray-100 hover:text-black focus:bg-gray-100 focus:text-black">100</SelectItem>
              </SelectContent>
            </Select>
          </div>

          {/* Search Bar */}
          <div className="relative w-full sm:w-48">
            <Icon
              icon="vaadin:search"
              className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400"
              style={{ width: '18px', height: '18px' }}
            />
            <input
              type="text"
              placeholder="Suchen..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-10 pr-4 py-2 text-sm text-black border border-black rounded-md focus:outline-none focus:ring-2 focus:ring-black focus:ring-offset-1 placeholder:text-gray-400"
            />
          </div>
        </div>
      </div>

      {/* Date Range Filter */}
      <div className="flex-shrink-0 bg-white rounded-lg p-4 border border-gray-200">
        <div className="flex flex-col sm:flex-row gap-4 items-start sm:items-center">
          <div className="flex items-center gap-2">
            <Icon
              icon="vaadin:calendar"
              className="text-black"
              style={{ width: '20px', height: '20px' }}
            />
            <label className="text-sm font-medium text-black">Zeitraum:</label>
          </div>

          {/* Date Range Preset Selector */}
          <Select
            value={dateRangePreset}
            onValueChange={setDateRangePreset}
          >
            <SelectTrigger className="w-[180px] h-9 border-black text-black [&_svg]:text-black [&_svg]:opacity-100 [&_svg]:-mt-4">
              <SelectValue />
            </SelectTrigger>
            <SelectContent className="bg-white border-black">
              {DATE_RANGE_PRESETS.map(preset => (
                <SelectItem
                  key={preset.value}
                  value={preset.value}
                  className="text-black hover:bg-gray-100 hover:text-black focus:bg-gray-100 focus:text-black"
                >
                  {preset.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>

          {/* Custom Date Range Pickers */}
          {dateRangePreset === 'custom' && (
            <div className="flex flex-col sm:flex-row gap-2 items-start sm:items-center">
              <div className="flex items-center gap-2">
                <label className="text-sm text-gray-600">Von:</label>
                <DatePicker
                  value={startDate}
                  onChange={setStartDate}
                />
              </div>
              <div className="flex items-center gap-2">
                <label className="text-sm text-gray-600">Bis:</label>
                <DatePicker
                  value={endDate}
                  onChange={setEndDate}
                />
              </div>
            </div>
          )}

          {/* Active Filter Display */}
          {startDate && endDate && (
            <div className="text-sm text-gray-600">
              {formatDate(startDate.toISOString())} - {formatDate(endDate.toISOString())}
            </div>
          )}
        </div>
      </div>

      {/* Error message */}
      {error && (
        <div className="rounded-md bg-destructive/10 p-3 text-sm text-destructive flex-shrink-0">
          {error}
        </div>
      )}

      {/* Bookings Table */}
      <div className="bg-white rounded-lg p-4 sm:p-6 w-full flex-shrink-0">
        {isLoading ? (
          <div className="flex items-center justify-center py-12">
            <div className="text-center">
              <Icon icon="vaadin:spinner" className="animate-spin text-primary mb-2" style={{ width: '32px', height: '32px' }} />
              <p className="text-sm text-muted-foreground">Lädt Buchungen...</p>
            </div>
          </div>
        ) : filteredBookings.length === 0 ? (
          <div className="flex items-center justify-center py-12">
            <div className="text-center">
              <Icon icon="vaadin:invoice" className="text-muted-foreground mb-2" style={{ width: '48px', height: '48px' }} />
              <p className="text-sm text-muted-foreground">
                {searchQuery || (startDate && endDate)
                  ? 'Keine Buchungen gefunden für die aktuellen Filter'
                  : 'Keine Buchungen vorhanden'}
              </p>
            </div>
          </div>
        ) : (
          <>
            {/* Desktop Table */}
            <div className="hidden md:block overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-gray-200">
                    <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700">Code</th>
                    <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700">Mitglied</th>
                    <th className="text-right py-3 px-4 text-sm font-semibold text-gray-700">Erwarteter Betrag</th>
                    <th className="text-right py-3 px-4 text-sm font-semibold text-gray-700">Tatsächlicher Betrag</th>
                    <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700">Empfangen am</th>
                    <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700">Erstellt am</th>
                    <th className="text-center py-3 px-4 text-sm font-semibold text-gray-700">Aktionen</th>
                  </tr>
                </thead>
                <tbody>
                  {currentBookings.map((booking) => (
                    <tr
                      key={booking.id}
                      className="border-b border-gray-100 hover:bg-gray-50 cursor-pointer"
                      onClick={() => handleBookingClick(booking)}
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
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={(e) => {
                            e.stopPropagation();
                            handleBookingClick(booking);
                          }}
                        >
                          <Icon icon="vaadin:eye" style={{ width: '16px', height: '16px' }} />
                        </Button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Mobile Cards */}
            <div className="md:hidden space-y-3">
              {currentBookings.map((booking) => (
                <div
                  key={booking.id}
                  onClick={() => handleBookingClick(booking)}
                  className="border border-gray-200 rounded-lg p-4 hover:border-gray-300 cursor-pointer"
                >
                  <div className="flex justify-between items-start mb-2">
                    <div>
                      <div className="font-medium text-gray-900">{booking.code || 'Kein Code'}</div>
                      <div className="text-sm text-gray-600">{booking.ofMG || 'N/A'}</div>
                    </div>
                    <Icon icon="vaadin:invoice" className="text-gray-400" style={{ width: '20px', height: '20px' }} />
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

            {/* Pagination */}
            {totalPages > 1 && (
              <div className="flex items-center justify-between mt-6 pt-4 border-t border-gray-200">
                <div className="text-sm text-gray-600">
                  Seite {currentPage} von {totalPages} ({filteredBookings.length} Buchungen gesamt)
                </div>
                <div className="flex gap-2">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => handlePageChange(1)}
                    disabled={currentPage === 1}
                  >
                    <Icon icon="vaadin:angle-double-left" style={{ width: '16px', height: '16px' }} />
                  </Button>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => handlePageChange(currentPage - 1)}
                    disabled={currentPage === 1}
                  >
                    <Icon icon="vaadin:angle-left" style={{ width: '16px', height: '16px' }} />
                  </Button>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => handlePageChange(currentPage + 1)}
                    disabled={currentPage === totalPages}
                  >
                    <Icon icon="vaadin:angle-right" style={{ width: '16px', height: '16px' }} />
                  </Button>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => handlePageChange(totalPages)}
                    disabled={currentPage === totalPages}
                  >
                    <Icon icon="vaadin:angle-double-right" style={{ width: '16px', height: '16px' }} />
                  </Button>
                </div>
              </div>
            )}
          </>
        )}
      </div>

      {/* Booking details modal */}
      <Dialog
        opened={isModalOpen}
        onOpenedChanged={(e: any) => {
          if (!e.detail.value) handleCloseModal();
        }}
        headerTitle="Buchungsdetails"
      >
        {selectedBooking && (
          <div className="p-4 sm:p-6 min-w-[300px] sm:min-w-[600px] lg:min-w-[800px] max-w-[95vw]">
            {/* Header Section */}
            <div className="flex flex-col sm:flex-row items-start gap-4 sm:gap-6 pb-6 border-b">
              <div className="flex-shrink-0 bg-muted rounded-full p-4">
                <Icon icon="vaadin:invoice" className="text-foreground" style={{ width: '64px', height: '64px' }} />
              </div>
              <div className="flex-1">
                <h3 className="text-2xl font-semibold mb-1">{selectedBooking.code || 'Keine Code'}</h3>
                <p className="text-muted-foreground font-mono text-sm">{selectedBooking.id}</p>
              </div>
            </div>

            {/* Details Grid */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-4 sm:gap-x-8 gap-y-4 py-6">
              {/* Member */}
              <div className="space-y-1">
                <label className="text-sm font-medium text-muted-foreground">Mitglied</label>
                <div className="text-sm font-medium">{selectedBooking.ofMG || 'N/A'}</div>
              </div>

              {/* User ID */}
              {selectedBooking.user?.id && (
                <div className="space-y-1">
                  <label className="text-sm font-medium text-muted-foreground">Benutzer-ID</label>
                  <div className="text-sm font-mono">{selectedBooking.user.id}</div>
                </div>
              )}

              {/* Expected Section */}
              <div className="space-y-1 sm:col-span-2 border-t pt-4">
                <h4 className="font-semibold text-gray-900 mb-3">Erwartete Werte</h4>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div>
                    <label className="text-sm font-medium text-muted-foreground">Betrag</label>
                    <div className="text-lg font-semibold text-green-600">
                      {formatCurrency(selectedBooking.expectedAmount)}
                    </div>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-muted-foreground">Verwendungszweck</label>
                    <div className="text-sm">{selectedBooking.expectedPurpose || 'N/A'}</div>
                  </div>
                </div>
              </div>

              {/* Actual Section */}
              <div className="space-y-1 sm:col-span-2 border-t pt-4">
                <h4 className="font-semibold text-gray-900 mb-3">Tatsächliche Werte</h4>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div>
                    <label className="text-sm font-medium text-muted-foreground">Betrag</label>
                    <div className="text-lg font-semibold text-blue-600">
                      {formatCurrency(selectedBooking.actualAmount)}
                    </div>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-muted-foreground">Verwendungszweck</label>
                    <div className="text-sm">{selectedBooking.actualPurpose || 'N/A'}</div>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-muted-foreground">Empfangen am</label>
                    <div className="text-sm">{formatDate(selectedBooking.receivedAt)}</div>
                  </div>
                </div>
              </div>

              {/* Additional Info */}
              <div className="space-y-1 sm:col-span-2 border-t pt-4">
                <h4 className="font-semibold text-gray-900 mb-3">Zusätzliche Informationen</h4>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  {selectedBooking.note && (
                    <div className="sm:col-span-2">
                      <label className="text-sm font-medium text-muted-foreground">Notiz</label>
                      <div className="text-sm">{selectedBooking.note}</div>
                    </div>
                  )}
                  {selectedBooking.accountStatementPage && (
                    <div>
                      <label className="text-sm font-medium text-muted-foreground">Kontoauszug Seite</label>
                      <div className="text-sm">{selectedBooking.accountStatementPage}</div>
                    </div>
                  )}
                  <div>
                    <label className="text-sm font-medium text-muted-foreground">Erstellt am</label>
                    <div className="text-sm">{formatDate(selectedBooking.createdAt)}</div>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-muted-foreground">Aktualisiert am</label>
                    <div className="text-sm">{formatDate(selectedBooking.updatedAt)}</div>
                  </div>
                </div>
              </div>
            </div>

            {/* Action Buttons */}
            <div className="flex justify-end gap-3 pt-6 border-t mt-6">
              <Button variant="destructive" onClick={handleCloseModal} className="text-white">
                <Icon icon="vaadin:close" className="mr-2" style={{ width: '16px', height: '16px', color: 'white' }} />
                Schließen
              </Button>
            </div>
          </div>
        )}
      </Dialog>
    </div>
  );
}
