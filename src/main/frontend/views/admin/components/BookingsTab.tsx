import { useState, useEffect } from 'react';
import { Icon } from '@vaadin/react-components';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Button } from '@/components/ui/button';
import { BookingController } from 'Frontend/generated/endpoints';
import type BookingDTO from 'Frontend/generated/com/bunkermuseum/membermanagement/dto/BookingDTO';
import BookingsList from './_BookingsList';
import BookingDetailsModal from './_BookingDetailsModal';
import DeleteBookingModal from './_DeleteBookingModal';
import AssignBookingModal from './_AssignBookingModal';
import BookingsDateRangeFilter, { DATE_RANGE_PRESETS } from './_BookingsDateRangeFilter';

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
  const [bookings, setBookings] = useState<BookingDTO[]>([]);
  const [filteredBookings, setFilteredBookings] = useState<BookingDTO[]>([]);
  const [selectedBooking, setSelectedBooking] = useState<BookingDTO | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [bookingToDelete, setBookingToDelete] = useState<BookingDTO | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [isMobile, setIsMobile] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const [bookingsPerPage, setBookingsPerPage] = useState(10);
  const [dateRangePreset, setDateRangePreset] = useState('1month');
  const [startDate, setStartDate] = useState<Date | undefined>(undefined);
  const [endDate, setEndDate] = useState<Date | undefined>(undefined);
  const [isAssignModalOpen, setIsAssignModalOpen] = useState(false);

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
      setBookings((allBookings || []).filter((booking): booking is BookingDTO => booking !== undefined));
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
   * @param {BookingDTO} booking - The booking to display
   *
   * @author Philipp Borkovic
   */
  const handleBookingClick = (booking: BookingDTO): void => {
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
   * Opens the delete confirmation modal for a specific booking.
   *
   * @param {BookingDTO} booking - The booking to delete
   *
   * @author Philipp Borkovic
   */
  const handleDeleteClick = (booking: BookingDTO): void => {
    setBookingToDelete(booking);
    setIsDeleteModalOpen(true);
  };

  /**
   * Closes the delete confirmation modal.
   *
   * @author Philipp Borkovic
   */
  const handleCloseDeleteModal = (): void => {
    setIsDeleteModalOpen(false);
    setBookingToDelete(null);
  };

  /**
   * Deletes the selected booking.
   *
   * @author Philipp Borkovic
   */
  const handleConfirmDelete = async (): Promise<void> => {
    if (!bookingToDelete) return;

    try {
      // TODO: Implement booking deletion via BookingController
      console.log('Deleting booking:', bookingToDelete.id);
      handleCloseDeleteModal();
      loadBookings(); // Reload bookings after deletion
    } catch (err: any) {
      setError(err.message || 'Fehler beim Löschen der Buchung');
    }
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
  const startIndexZeroBased = (currentPage - 1) * bookingsPerPage;
  const endIndexZeroBased = startIndexZeroBased + bookingsPerPage;
  const currentBookings = filteredBookings.slice(startIndexZeroBased, endIndexZeroBased);

  // Check if filters are active
  const hasActiveFilters = !!(searchQuery || (startDate && endDate));

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
        <div className="flex flex-col sm:flex-row gap-3 sm:ml-auto items-stretch sm:items-center">
          {/* Assign Button */}
          <div className="flex">
            <Button onClick={() => setIsAssignModalOpen(true)} className="text-white whitespace-nowrap">
              <Icon icon="vaadin:plus" className="mr-2" style={{ width: 16, height: 16, color: 'white' }} />
              Neue Buchung zuweisen
            </Button>
          </div>

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
      <BookingsDateRangeFilter
        dateRangePreset={dateRangePreset}
        startDate={startDate}
        endDate={endDate}
        onPresetChange={setDateRangePreset}
        onStartDateChange={setStartDate}
        onEndDateChange={setEndDate}
      />

      {/* Error message */}
      {error && (
        <div className="rounded-md bg-destructive/10 p-3 text-sm text-destructive flex-shrink-0">
          {error}
        </div>
      )}
      {/* Success message */}
      {success && (
        <div className="rounded-md bg-emerald-50 border border-emerald-200 text-emerald-800 p-3 text-sm flex-shrink-0">
          {success}
        </div>
      )}

      {/* Bookings List */}
      <div className="bg-white rounded-lg p-4 sm:p-6 w-full flex-shrink-0">
        <BookingsList
          bookings={currentBookings}
          isLoading={isLoading}
          searchQuery={searchQuery}
          hasActiveFilters={hasActiveFilters}
          currentPage={currentPage}
          totalPages={totalPages}
          totalElements={filteredBookings.length}
          bookingsPerPage={bookingsPerPage}
          isMobile={isMobile}
          onBookingClick={handleBookingClick}
          onDeleteClick={handleDeleteClick}
          onPageChange={handlePageChange}
        />
      </div>

      {/* Modals */}
      <DeleteBookingModal
        booking={bookingToDelete}
        isOpen={isDeleteModalOpen}
        onClose={handleCloseDeleteModal}
        onConfirm={handleConfirmDelete}
      />

      <BookingDetailsModal
        booking={selectedBooking}
        isOpen={isModalOpen}
        onClose={handleCloseModal}
      />

      <AssignBookingModal
        isOpen={isAssignModalOpen}
        onClose={() => setIsAssignModalOpen(false)}
        onAssigned={(count) => {
          setSuccess(`Buchung für ${count} Benutzer zugewiesen.`);
          setTimeout(() => setSuccess(''), 4000);
          loadBookings();
        }}
      />
    </div>
  );
}
