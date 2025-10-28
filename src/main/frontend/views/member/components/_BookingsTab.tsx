import { useState, useEffect } from 'react';
import { Icon } from '@vaadin/react-components';
import { BookingController } from 'Frontend/generated/endpoints';
import type BookingDTO from 'Frontend/generated/com/bunkermuseum/membermanagement/dto/BookingDTO';

/**
 * BookingsTab component - Displays user's bookings categorized by completion status.
 *
 * Features:
 * - Shows finished bookings (payment received)
 * - Shows pending bookings (payment not yet received)
 * - Card-based layout for better UX
 * - Currency formatting for amounts
 * - Date formatting
 * - Loading and error states
 * - Responsive design
 *
 * @component
 * @author Philipp Borkovic
 */
export default function BookingsTab(): JSX.Element {
  const [bookings, setBookings] = useState<BookingDTO[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  /**
   * Loads bookings for the current user on component mount.
   */
  useEffect(() => {
    loadBookings();
  }, []);

  /**
   * Fetches bookings for the current user from the backend.
   */
  const loadBookings = async (): Promise<void> => {
    try {
      setIsLoading(true);
      setError('');
      const userBookings = await BookingController.getCurrentUserBookings();
      setBookings((userBookings || []).filter((booking): booking is BookingDTO => booking !== undefined));
    } catch (err: any) {
      setError(err.message || 'Fehler beim Laden der Buchungen');
    } finally {
      setIsLoading(false);
    }
  };

  /**
   * Determines if a booking is finished (payment received).
   */
  const isBookingFinished = (booking: BookingDTO): boolean => {
    return booking.receivedAt != null && booking.actualAmount != null;
  };

  /**
   * Categorizes bookings into finished and pending.
   */
  const finishedBookings = bookings.filter(isBookingFinished);
  const pendingBookings = bookings.filter(booking => !isBookingFinished(booking));

  /**
   * Formats a number as EUR currency.
   */
  const formatCurrency = (amount: number | undefined | null): string => {
    if (amount === undefined || amount === null) return '-';
    return new Intl.NumberFormat('de-DE', {
      style: 'currency',
      currency: 'EUR',
    }).format(amount);
  };

  /**
   * Formats a date to German locale.
   */
  const formatDate = (dateString: string | null | undefined): string => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('de-DE', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    });
  };

  /**
   * Renders a single booking card.
   */
  const renderBookingCard = (booking: BookingDTO, isFinished: boolean): JSX.Element => {
    return (
      <div
        key={booking.id}
        className="bg-white rounded-lg border border-gray-200 p-5 hover:border-black transition-colors"
      >
        {/* Header */}
        <div className="flex items-start justify-between mb-4 pb-3 border-b border-gray-200">
          <div className="flex items-center gap-3">
            <div className={`flex items-center justify-center w-10 h-10 rounded-full ${
              isFinished ? 'bg-black' : 'bg-gray-200'
            }`}>
              <Icon
                icon={isFinished ? 'vaadin:check' : 'vaadin:clock'}
                className={isFinished ? 'text-white' : 'text-gray-600'}
                style={{ width: '20px', height: '20px' }}
              />
            </div>
            <div>
              <h3 className="font-semibold text-black text-base">
                {booking.actualPurpose || booking.expectedPurpose || 'Buchung'}
              </h3>
              {booking.code && (
                <p className="text-xs text-gray-500 mt-0.5">Code: {booking.code}</p>
              )}
            </div>
          </div>
          <span
            className={`px-2.5 py-1 text-xs font-medium border ${
              isFinished
                ? 'bg-black text-white border-black'
                : 'bg-white text-gray-700 border-gray-300'
            }`}
          >
            {isFinished ? 'Abgeschlossen' : 'Ausstehend'}
          </span>
        </div>

        {/* Details */}
        <div className="space-y-3 text-sm">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <p className="text-xs text-gray-500 mb-1">Erwarteter Betrag</p>
              <p className="font-semibold text-black">{formatCurrency(booking.expectedAmount)}</p>
            </div>
            {isFinished && (
              <div>
                <p className="text-xs text-gray-500 mb-1">Tatsächlicher Betrag</p>
                <p className="font-semibold text-black">{formatCurrency(booking.actualAmount)}</p>
              </div>
            )}
          </div>

          {isFinished && booking.receivedAt && (
            <div>
              <p className="text-xs text-gray-500 mb-1">Eingegangen am</p>
              <p className="font-medium text-black">{formatDate(booking.receivedAt)}</p>
            </div>
          )}

          {booking.note && (
            <div className="pt-3 border-t border-gray-200">
              <p className="text-xs text-gray-500 mb-1">Notiz</p>
              <p className="text-sm text-black">{booking.note}</p>
            </div>
          )}
        </div>
      </div>
    );
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-12">
        <Icon icon="vaadin:spinner" className="animate-spin text-black" style={{ width: '48px', height: '48px' }} />
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-white border border-gray-200 rounded-lg p-8 text-center">
        <div className="flex items-center justify-center w-16 h-16 rounded-full bg-gray-100 mx-auto mb-4">
          <Icon icon="vaadin:warning" className="text-black" style={{ width: '32px', height: '32px' }} />
        </div>
        <h3 className="text-lg font-semibold text-black mb-2">Fehler beim Laden</h3>
        <p className="text-sm text-gray-600">{error}</p>
      </div>
    );
  }

  if (bookings.length === 0) {
    return (
      <div className="bg-white rounded-lg border border-gray-200 p-12 text-center">
        <div className="flex items-center justify-center w-20 h-20 rounded-full bg-gray-100 mx-auto mb-4">
          <Icon icon="vaadin:invoice" className="text-gray-400" style={{ width: '40px', height: '40px' }} />
        </div>
        <h3 className="text-xl font-semibold text-black mb-2">Keine Buchungen vorhanden</h3>
        <p className="text-sm text-gray-600 max-w-md mx-auto">
          Sie haben derzeit keine Buchungen. Sobald Buchungen für Sie erstellt werden, erscheinen sie hier.
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-8">
      {/* Header with Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="bg-white rounded-lg border border-gray-200 p-5">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-xs font-medium text-gray-600 mb-1">Gesamt</p>
              <p className="text-3xl font-bold text-black">{bookings.length}</p>
            </div>
            <div className="flex items-center justify-center w-12 h-12 rounded-full bg-gray-100">
              <Icon icon="vaadin:invoice" className="text-black" style={{ width: '24px', height: '24px' }} />
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg border border-gray-200 p-5">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-xs font-medium text-gray-600 mb-1">Abgeschlossen</p>
              <p className="text-3xl font-bold text-black">{finishedBookings.length}</p>
            </div>
            <div className="flex items-center justify-center w-12 h-12 rounded-full bg-black">
              <Icon icon="vaadin:check" className="text-white" style={{ width: '20px', height: '20px' }} />
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg border border-gray-200 p-5">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-xs font-medium text-gray-600 mb-1">Ausstehend</p>
              <p className="text-3xl font-bold text-black">{pendingBookings.length}</p>
            </div>
            <div className="flex items-center justify-center w-12 h-12 rounded-full bg-gray-100">
              <Icon icon="vaadin:clock" className="text-gray-600" style={{ width: '20px', height: '20px' }} />
            </div>
          </div>
        </div>
      </div>

      {/* Pending Bookings */}
      {pendingBookings.length > 0 && (
        <div>
          <div className="flex items-center justify-between mb-4 pb-3 border-b border-gray-200">
            <div className="flex items-center gap-3">
              <Icon icon="vaadin:clock" className="text-black" style={{ width: '20px', height: '20px' }} />
              <h2 className="text-lg font-semibold text-black">Ausstehende Buchungen</h2>
            </div>
            <span className="px-2.5 py-1 bg-gray-100 text-black text-xs font-medium border border-gray-200">
              {pendingBookings.length}
            </span>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {pendingBookings.map(booking => renderBookingCard(booking, false))}
          </div>
        </div>
      )}

      {/* Finished Bookings */}
      {finishedBookings.length > 0 && (
        <div>
          <div className="flex items-center justify-between mb-4 pb-3 border-b border-gray-200">
            <div className="flex items-center gap-3">
              <Icon icon="vaadin:check" className="text-black" style={{ width: '20px', height: '20px' }} />
              <h2 className="text-lg font-semibold text-black">Abgeschlossene Buchungen</h2>
            </div>
            <span className="px-2.5 py-1 bg-black text-white text-xs font-medium">
              {finishedBookings.length}
            </span>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {finishedBookings.map(booking => renderBookingCard(booking, true))}
          </div>
        </div>
      )}
    </div>
  );
}
