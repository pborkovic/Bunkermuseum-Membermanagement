/**
 * @fileoverview Bookings Tab component for member dashboard.
 *
 * This component displays a user's bookings categorized by completion status,
 * with comprehensive statistics and a card-based layout for easy viewing.
 *
 * @module views/member/components/BookingsTab
 * @author Philipp Borkovic
 */

import { useCallback, useMemo } from 'react';
import { Icon } from '@vaadin/react-components';
import { BookingController } from 'Frontend/generated/endpoints';
import type BookingDTO from 'Frontend/generated/com/bunkermuseum/membermanagement/dto/BookingDTO';
import { useAsyncData, useCategorizedBookings } from '../hooks';
import { formatCurrency, formatDate } from '../utils/formatting';
import { ERROR_MESSAGES, UI_TEXT } from '../constants';

/**
 * BookingsTab Component.
 *
 * Displays user's bookings with the following features:
 * - Categorization into finished and pending bookings
 * - Statistics cards showing total, finished, and pending counts
 * - Card-based layout with booking details
 * - Loading and error states
 * - Responsive design (1/2/3 columns based on screen size)
 * - Empty state handling
 *
 * A booking is considered "finished" when both `receivedAt` and `actualAmount` are set.
 *
 * @component
 *
 * @returns {JSX.Element} The rendered bookings tab content
 *
 * @author Philipp Borkovic
 */
export default function BookingsTab(): JSX.Element {
  const {
    data: bookings,
    isLoading,
    error,
  } = useAsyncData(
    () => BookingController.getCurrentUserBookings(),
    []
  );

  const validBookings = (bookings || []).filter(
    (booking): booking is BookingDTO => booking !== undefined
  );

  const { finished, pending, total } = useCategorizedBookings(validBookings);

  /**
   * Renders a single booking card with all relevant information.
   *
   * @param {BookingDTO} booking - The booking data to display
   * @param {boolean} isFinished - Whether the booking is completed
   * @returns {JSX.Element} The rendered booking card
   */
  const renderBookingCard = useCallback(
    (booking: BookingDTO, isFinished: boolean): JSX.Element => {
      return (
        <div
          key={booking.id}
          className="bg-white rounded-lg border border-gray-200 p-5 hover:border-black transition-colors"
        >
          {/* Header */}
          <div className="flex items-start justify-between mb-4 pb-3 border-b border-gray-200">
            <div className="flex items-center gap-3">
              <div
                className={`flex items-center justify-center w-10 h-10 rounded-full ${
                  isFinished ? 'bg-black' : 'bg-gray-200'
                }`}
              >
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
              {isFinished
                ? UI_TEXT.BOOKING_STATUS_FINISHED
                : UI_TEXT.BOOKING_STATUS_PENDING}
            </span>
          </div>

          {/* Details */}
          <div className="space-y-3 text-sm">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <p className="text-xs text-gray-500 mb-1">Erwarteter Betrag</p>
                <p className="font-semibold text-black">
                  {formatCurrency(booking.expectedAmount)}
                </p>
              </div>
              {isFinished && (
                <div>
                  <p className="text-xs text-gray-500 mb-1">Tatsächlicher Betrag</p>
                  <p className="font-semibold text-black">
                    {formatCurrency(booking.actualAmount)}
                  </p>
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
    },
    []
  );

  /**
   * Renders a statistics card showing booking count.
   *
   * @param {Object} params - Card parameters
   * @param {string} params.label - Card label
   * @param {number} params.count - Number to display
   * @param {string} params.icon - Vaadin icon name
   * @param {boolean} params.highlighted - Whether to highlight with black background
   *
   * @returns {JSX.Element} The rendered stat card
   */
  const renderStatCard = useCallback(
    ({
      label,
      count,
      icon,
      highlighted,
    }: {
      label: string;
      count: number;
      icon: string;
      highlighted: boolean;
    }): JSX.Element => {
      return (
        <div className="bg-white rounded-lg border border-gray-200 p-5">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-xs font-medium text-gray-600 mb-1">{label}</p>
              <p className="text-3xl font-bold text-black">{count}</p>
            </div>
            <div
              className={`flex items-center justify-center w-12 h-12 rounded-full ${
                highlighted ? 'bg-black' : 'bg-gray-100'
              }`}
            >
              <Icon
                icon={icon}
                className={highlighted ? 'text-white' : 'text-black'}
                style={
                  highlighted
                    ? { width: '20px', height: '20px' }
                    : { width: '24px', height: '24px' }
                }
              />
            </div>
          </div>
        </div>
      );
    },
    []
  );

  /**
   * Memoized stat cards to prevent unnecessary re-renders.
   */
  const statCards = useMemo(
    () => (
      <>
        {renderStatCard({
          label: 'Gesamt',
          count: total,
          icon: 'vaadin:invoice',
          highlighted: false,
        })}
        {renderStatCard({
          label: 'Abgeschlossen',
          count: finished.length,
          icon: 'vaadin:check',
          highlighted: true,
        })}
        {renderStatCard({
          label: 'Ausstehend',
          count: pending.length,
          icon: 'vaadin:clock',
          highlighted: false,
        })}
      </>
    ),
    [total, finished.length, pending.length, renderStatCard]
  );

  /**
   * Renders loading state.
   */
  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-12">
        <Icon
          icon="vaadin:spinner"
          className="animate-spin text-black"
          style={{ width: '48px', height: '48px' }}
        />
      </div>
    );
  }

  /**
   * Renders error state.
   */
  if (error) {
    return (
      <div className="bg-white border border-gray-200 rounded-lg p-8 text-center">
        <div className="flex items-center justify-center w-16 h-16 rounded-full bg-gray-100 mx-auto mb-4">
          <Icon
            icon="vaadin:warning"
            className="text-black"
            style={{ width: '32px', height: '32px' }}
          />
        </div>
        <h3 className="text-lg font-semibold text-black mb-2">Fehler beim Laden</h3>
        <p className="text-sm text-gray-600">{error}</p>
      </div>
    );
  }

  /**
   * Renders empty state when no bookings exist.
   */
  if (validBookings.length === 0) {
    return (
      <div className="bg-white rounded-lg border border-gray-200 p-12 text-center">
        <div className="flex items-center justify-center w-20 h-20 rounded-full bg-gray-100 mx-auto mb-4">
          <Icon
            icon="vaadin:invoice"
            className="text-gray-400"
            style={{ width: '40px', height: '40px' }}
          />
        </div>
        <h3 className="text-xl font-semibold text-black mb-2">
          {UI_TEXT.NO_BOOKINGS}
        </h3>
        <p className="text-sm text-gray-600 max-w-md mx-auto">
          {UI_TEXT.NO_BOOKINGS_DESCRIPTION}
        </p>
      </div>
    );
  }

  /**
   * Renders main content with statistics and booking lists.
   */
  return (
    <div className="space-y-8">
      {/* Header with Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">{statCards}</div>

      {/* Pending Bookings */}
      {pending.length > 0 && (
        <div>
          <div className="flex items-center justify-between mb-4 pb-3 border-b border-gray-200">
            <div className="flex items-center gap-3">
              <Icon
                icon="vaadin:clock"
                className="text-black"
                style={{ width: '20px', height: '20px' }}
              />
              <h2 className="text-lg font-semibold text-black">
                {UI_TEXT.PENDING_BOOKINGS}
              </h2>
            </div>
            <span className="px-2.5 py-1 bg-gray-100 text-black text-xs font-medium border border-gray-200">
              {pending.length}
            </span>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {pending.map((booking) => renderBookingCard(booking, false))}
          </div>
        </div>
      )}

      {/* Finished Bookings */}
      {finished.length > 0 && (
        <div>
          <div className="flex items-center justify-between mb-4 pb-3 border-b border-gray-200">
            <div className="flex items-center gap-3">
              <Icon
                icon="vaadin:check"
                className="text-black"
                style={{ width: '20px', height: '20px' }}
              />
              <h2 className="text-lg font-semibold text-black">
                {UI_TEXT.FINISHED_BOOKINGS}
              </h2>
            </div>
            <span className="px-2.5 py-1 bg-black text-white text-xs font-medium">
              {finished.length}
            </span>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {finished.map((booking) => renderBookingCard(booking, true))}
          </div>
        </div>
      )}
    </div>
  );
}
