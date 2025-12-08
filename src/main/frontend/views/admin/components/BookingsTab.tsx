import {useCallback, useEffect, useState} from 'react';
import {FaCloudDownloadAlt, FaCode, FaDownload, FaFileAlt, FaFileCode, FaPlus, FaSearch, FaTable} from 'react-icons/fa';
import {Dialog} from '@vaadin/react-components/Dialog';
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from '@/components/ui/select';
import {DatePicker} from '@/components/ui/date-picker';
import {Button} from '@/components/ui/button';
import {BookingController} from 'Frontend/generated/endpoints';
import type BookingDTO from 'Frontend/generated/com/bunkermuseum/membermanagement/dto/BookingDTO';
import {DialogOpenedChangedEvent, getErrorMessage} from '../../../types/vaadin';
import BookingsList from './_BookingsList';
import BookingDetailsModal from './_BookingDetailsModal';
import DeleteBookingModal from './_DeleteBookingModal';
import AssignBookingModal from './_AssignBookingModal';
import BookingsDateRangeFilter, {DATE_RANGE_PRESETS} from './_BookingsDateRangeFilter';
import {useModal} from '../hooks/useModal';
import {EXPORT_BOOKING_TYPE_OPTIONS, EXPORT_FORMAT_OPTIONS} from '../utils/constants';

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
  const exportModal = useModal();
  const [exportBookingType, setExportBookingType] = useState('all');
  const [exportFormat, setExportFormat] = useState('xlsx');
  const [exportDateRangePreset, setExportDateRangePreset] = useState('1month');
  const [exportStartDate, setExportStartDate] = useState<Date | undefined>(undefined);
  const [exportEndDate, setExportEndDate] = useState<Date | undefined>(undefined);
  const [singleBookingExportModal, setSingleBookingExportModal] = useState<BookingDTO | null>(null);
  const [singleBookingExportFormat, setSingleBookingExportFormat] = useState('xlsx');

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
   * Update export date range based on preset selection
   */
  useEffect(() => {
    if (exportDateRangePreset !== 'custom') {
      const preset = DATE_RANGE_PRESETS.find(p => p.value === exportDateRangePreset);
      if (preset && preset.days > 0) {
        const end = new Date();
        const start = new Date();
        start.setDate(start.getDate() - preset.days);
        setExportStartDate(start);
        setExportEndDate(end);
      }
    }
  }, [exportDateRangePreset]);

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
    } catch (err: unknown) {
      const errorMessage = getErrorMessage(err);
      setError(errorMessage || 'Fehler beim Laden der Buchungen');
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
    } catch (err: unknown) {
      const errorMessage = getErrorMessage(err);
      setError(errorMessage || 'Fehler beim Löschen der Buchung');
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

  /**
   * Opens the export modal and pre-fills date range from current dashboard settings.
   *
   * @author Philipp Borkovic
   */
  const handleOpenExportModal = useCallback((): void => {
    // Pre-fill export dates and preset with current dashboard settings
    setExportDateRangePreset(dateRangePreset);
    setExportStartDate(startDate);
    setExportEndDate(endDate);
    exportModal.open();
  }, [startDate, endDate, dateRangePreset, exportModal]);

  /**
   * Handles the export action by triggering a file download.
   *
   * @author Philipp Borkovic
   */
  const handleExport = useCallback(async (): Promise<void> => {
    try {
      let url = `/api/export/bookings?bookingType=${encodeURIComponent(exportBookingType)}&format=${encodeURIComponent(exportFormat)}`;

      if (exportStartDate) {
        url += `&startDate=${exportStartDate.toISOString().split('T')[0]}`;
      }
      if (exportEndDate) {
        url += `&endDate=${exportEndDate.toISOString().split('T')[0]}`;
      }

      const link = document.createElement('a');
      link.href = url;
      link.download = `bookings_${exportBookingType}_${new Date().toISOString().split('T')[0]}.${exportFormat}`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);

      exportModal.close();
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Fehler beim Exportieren der Buchungen';
      setError(errorMessage);
    }
  }, [exportBookingType, exportFormat, exportStartDate, exportEndDate, exportModal]);

  /**
   * Opens the single booking export modal.
   *
   * @author Philipp Borkovic
   */
  const handleExportBooking = useCallback((booking: BookingDTO): void => {
    setSingleBookingExportFormat('xlsx');
    setSingleBookingExportModal(booking);
  }, []);

  /**
   * Closes the single booking export modal.
   *
   * @author Philipp Borkovic
   */
  const handleCloseSingleBookingExport = useCallback((): void => {
    setSingleBookingExportModal(null);
  }, []);

  /**
   * Handles exporting a single booking's data with selected format.
   *
   * @author Philipp Borkovic
   */
  const handleConfirmSingleBookingExport = useCallback((): void => {
    if (!singleBookingExportModal) return;

    try {
      const booking = singleBookingExportModal;
      const url = `/api/export/booking/${booking.id}?format=${singleBookingExportFormat}`;

      const link = document.createElement('a');
      link.href = url;
      link.download = `booking_${booking.code}_${new Date().toISOString().split('T')[0]}.${singleBookingExportFormat}`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);

      handleCloseSingleBookingExport();
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Fehler beim Exportieren der Buchung';
      setError(errorMessage);
    }
  }, [singleBookingExportModal, singleBookingExportFormat, handleCloseSingleBookingExport]);

  const totalPages = Math.ceil(filteredBookings.length / bookingsPerPage);
  const startIndexZeroBased = (currentPage - 1) * bookingsPerPage;
  const endIndexZeroBased = startIndexZeroBased + bookingsPerPage;
  const currentBookings = filteredBookings.slice(startIndexZeroBased, endIndexZeroBased);

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
          {/* Export Button */}
          <Button
            variant="outline"
            onClick={handleOpenExportModal}
            className="text-white bg-black hover:bg-gray-800 border-black h-9 whitespace-nowrap w-full sm:w-auto"
          >
            <FaDownload className="mr-2" style={{ width: '16px', height: '16px', color: 'white' }} />
            Exportieren
          </Button>

          {/* Assign Button */}
          <Button
            onClick={() => setIsAssignModalOpen(true)}
            className="bg-black text-white hover:bg-gray-800 whitespace-nowrap w-full sm:w-auto h-9"
          >
            <FaPlus className="mr-2" style={{ width: 16, height: 16, color: 'white' }} />
            Neue Buchung zuweisen
          </Button>

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
            <FaSearch
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
          onExportClick={handleExportBooking}
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

      {/* Export options modal */}
      <Dialog
        opened={exportModal.isOpen}
        onOpenedChanged={(e: DialogOpenedChangedEvent) => {
          if (!e.detail.value) exportModal.close();
        }}
        headerTitle="Buchungsexport Optionen"
      >
        <div className="p-4 sm:p-6 min-w-[300px] sm:min-w-[500px] max-w-[95vw]">
          <div className="space-y-6">
            {/* Icon */}
            <div className="flex justify-center">
              <FaCloudDownloadAlt className="text-black" style={{ width: '64px', height: '64px' }} />
            </div>

            {/* Description */}
            <div className="text-center space-y-2">
              <p className="font-medium text-lg">Buchungen exportieren</p>
              <p className="text-sm text-muted-foreground">
                Wählen Sie die Art der Buchungen, den Zeitraum und das Format aus.
              </p>
            </div>

            {/* Booking Type Selector */}
            <div className="space-y-3">
              <label className="text-sm font-medium">Buchungstyp</label>
              <Select
                value={exportBookingType}
                onValueChange={(value) => setExportBookingType(value)}
              >
                <SelectTrigger className="w-full border-black text-black [&_svg]:text-black [&_svg]:opacity-100">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent className="bg-white border-black z-[9999]">
                  {EXPORT_BOOKING_TYPE_OPTIONS.map((option) => (
                    <SelectItem
                      key={option.value}
                      value={option.value}
                      className="text-black hover:bg-gray-100 hover:text-black focus:bg-gray-100 focus:text-black"
                    >
                      {option.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {/* Date Range Selection */}
            <div className="space-y-3">
              <label className="text-sm font-medium">Zeitraum</label>
              <Select
                value={exportDateRangePreset}
                onValueChange={(value) => setExportDateRangePreset(value)}
              >
                <SelectTrigger className="w-full border-black text-black [&_svg]:text-black [&_svg]:opacity-100">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent className="bg-white border-black z-[9999]">
                  {DATE_RANGE_PRESETS.map((preset) => (
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

              {/* Custom Date Range Pickers - Only show when custom is selected */}
              {exportDateRangePreset === 'custom' && (
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-2">
                  <div className="space-y-2">
                    <label className="text-xs text-muted-foreground">Von</label>
                    <DatePicker
                      value={exportStartDate}
                      onChange={(date) => setExportStartDate(date)}
                    />
                  </div>
                  <div className="space-y-2">
                    <label className="text-xs text-muted-foreground">Bis</label>
                    <DatePicker
                      value={exportEndDate}
                      onChange={(date) => setExportEndDate(date)}
                    />
                  </div>
                </div>
              )}
            </div>

            {/* Export Format Selector */}
            <div className="space-y-3">
              <label className="text-sm font-medium">Exportformat</label>
              <Select
                value={exportFormat}
                onValueChange={(value) => setExportFormat(value)}
              >
                <SelectTrigger className="w-full border-black text-black [&_svg]:text-black [&_svg]:opacity-100">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent className="bg-white border-black z-[9999]">
                  {EXPORT_FORMAT_OPTIONS.map((option) => {
                    const iconMap: Record<string, JSX.Element> = {
                      'xlsx': <FaTable style={{ width: '16px', height: '16px' }} />,
                      'pdf': <FaFileAlt style={{ width: '16px', height: '16px' }} />,
                      'xml': <FaFileCode style={{ width: '16px', height: '16px' }} />,
                      'json': <FaCode style={{ width: '16px', height: '16px' }} />
                    };
                    return (
                      <SelectItem
                        key={option.value}
                        value={option.value}
                        className="text-black hover:bg-gray-100 hover:text-black focus:bg-gray-100 focus:text-black"
                      >
                        <div className="flex items-center gap-2">
                          {iconMap[option.value]}
                          {option.label}
                        </div>
                      </SelectItem>
                    );
                  })}
                </SelectContent>
              </Select>
            </div>
          </div>

          {/* Action Buttons */}
          <div className="flex flex-col sm:flex-row justify-end gap-3 pt-6 border-t mt-6">
            <Button variant="destructive" onClick={exportModal.close} className="text-white w-full sm:w-auto">
              Abbrechen
            </Button>
            <Button
              variant="outline"
              onClick={handleExport}
              className="text-white bg-black hover:bg-gray-800 border-black w-full sm:w-auto"
            >
              <FaDownload className="mr-2" style={{ width: '16px', height: '16px', color: 'white' }} />
              Exportieren
            </Button>
          </div>
        </div>
      </Dialog>

      {/* Single booking export modal */}
      <Dialog
        opened={!!singleBookingExportModal}
        onOpenedChanged={(e: DialogOpenedChangedEvent) => {
          if (!e.detail.value) handleCloseSingleBookingExport();
        }}
        headerTitle="Buchung exportieren"
      >
        {singleBookingExportModal && (
          <div className="p-4 sm:p-6 min-w-[300px] sm:min-w-[500px] max-w-[95vw]">
            <div className="space-y-6">
              {/* Icon */}
              <div className="flex justify-center">
                <FaCloudDownloadAlt className="text-black" style={{ width: '64px', height: '64px' }} />
              </div>

              {/* Description */}
              <div className="text-center space-y-2">
                <p className="font-medium text-lg">Buchung exportieren</p>
                <p className="text-sm text-muted-foreground">
                  Exportieren Sie die Daten der Buchung <span className="font-semibold">{singleBookingExportModal.code || 'N/A'}</span> in einem der verfügbaren Formate.
                </p>
              </div>

              {/* Export Format Selector */}
              <div className="space-y-3">
                <label className="text-sm font-medium">Exportformat</label>
                <Select
                  value={singleBookingExportFormat}
                  onValueChange={(value) => setSingleBookingExportFormat(value)}
                >
                  <SelectTrigger className="w-full border-black text-black [&_svg]:text-black [&_svg]:opacity-100">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent className="bg-white border-black z-[9999]">
                    {EXPORT_FORMAT_OPTIONS.map((option) => {
                      const iconMap: Record<string, JSX.Element> = {
                        'xlsx': <FaTable style={{ width: '16px', height: '16px' }} />,
                        'pdf': <FaFileAlt style={{ width: '16px', height: '16px' }} />,
                        'xml': <FaFileCode style={{ width: '16px', height: '16px' }} />,
                        'json': <FaCode style={{ width: '16px', height: '16px' }} />
                      };
                      return (
                        <SelectItem
                          key={option.value}
                          value={option.value}
                          className="text-black hover:bg-gray-100 hover:text-black focus:bg-gray-100 focus:text-black"
                        >
                          <div className="flex items-center gap-2">
                            {iconMap[option.value]}
                            {option.label}
                          </div>
                        </SelectItem>
                      );
                    })}
                  </SelectContent>
                </Select>
              </div>
            </div>

            {/* Action Buttons */}
            <div className="flex flex-col sm:flex-row justify-end gap-3 pt-6 border-t mt-6">
              <Button variant="destructive" onClick={handleCloseSingleBookingExport} className="text-white w-full sm:w-auto">
                Abbrechen
              </Button>
              <Button
                variant="outline"
                onClick={handleConfirmSingleBookingExport}
                className="text-white bg-black hover:bg-gray-800 border-black w-full sm:w-auto"
              >
                <FaDownload className="mr-2" style={{ width: '16px', height: '16px', color: 'white' }} />
                Exportieren
              </Button>
            </div>
          </div>
        )}
      </Dialog>
    </div>
  );
}
