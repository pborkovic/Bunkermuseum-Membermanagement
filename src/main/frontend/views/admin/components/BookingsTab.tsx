import { useState, useEffect } from 'react';
import { Grid } from '@vaadin/react-components/Grid';
import { GridColumn } from '@vaadin/react-components/GridColumn';
import { Dialog } from '@vaadin/react-components/Dialog';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { Icon } from '@vaadin/react-components';
import { BookingController } from 'Frontend/generated/endpoints';
import type Booking from 'Frontend/generated/com/bunkermuseum/membermanagement/model/Booking';

/**
 * BookingsTab component - Displays all bookings in a grid with detailed modal view.
 *
 * Features:
 * - Grid view of all bookings
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
  const [selectedBooking, setSelectedBooking] = useState<Booking | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  /**
   * Loads all bookings from the backend on component mount.
   */
  useEffect(() => {
    loadBookings();
  }, []);

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

  return (
    <div className="space-y-4">
      {/* Header */}
      <div className="mb-4">
        <h2 className="text-xl font-semibold">Buchungsverwaltung</h2>
        <p className="text-sm text-muted-foreground">
          Übersicht aller Buchungen und Transaktionen
        </p>
      </div>

      {/* Error message */}
      {error && (
        <div className="rounded-md bg-destructive/10 p-3 text-sm text-destructive">
          {error}
        </div>
      )}

      {/* Bookings grid */}
      <div className="bg-white rounded-lg p-4">
        {isLoading ? (
          <div className="flex items-center justify-center py-12">
            <div className="text-center">
              <Icon icon="vaadin:spinner" className="animate-spin text-primary mb-2" style={{ width: '32px', height: '32px' }} />
              <p className="text-sm text-muted-foreground">Lädt Buchungen...</p>
            </div>
          </div>
        ) : bookings.length === 0 ? (
          <div className="flex items-center justify-center py-12">
            <div className="text-center">
              <Icon icon="vaadin:invoice" className="text-muted-foreground mb-2" style={{ width: '48px', height: '48px' }} />
              <p className="text-sm text-muted-foreground">Keine Buchungen gefunden</p>
            </div>
          </div>
        ) : (
          <Grid
            items={bookings}
            onActiveItemChanged={(e: any) => {
              const booking = e.detail.value;
              if (booking) handleBookingClick(booking);
            }}
            className="cursor-pointer"
          >
            <GridColumn
              path="code"
              header="Code"
              autoWidth
            />
            <GridColumn
              path="ofMG"
              header="Mitglied"
              autoWidth
            />
            <GridColumn
              header="Erwarteter Betrag"
              autoWidth
              renderer={({ item }: any) => formatCurrency(item.expectedAmount)}
            />
            <GridColumn
              header="Tatsächlicher Betrag"
              autoWidth
              renderer={({ item }: any) => formatCurrency(item.actualAmount)}
            />
            <GridColumn
              path="receivedAt"
              header="Empfangen am"
              autoWidth
              renderer={({ item }: any) => formatDate(item.receivedAt)}
            />
            <GridColumn
              path="createdAt"
              header="Erstellt am"
              autoWidth
              renderer={({ item }: any) => formatDate(item.createdAt)}
            />
          </Grid>
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
          <div className="space-y-4 p-4 min-w-[600px]">
            {/* Booking icon */}
            <div className="flex justify-center">
              <Icon icon="vaadin:invoice" className="text-primary" style={{ width: '64px', height: '64px' }} />
            </div>

            {/* Booking information */}
            <div className="space-y-3">
              <div className="grid grid-cols-3 gap-2">
                <span className="font-medium">ID:</span>
                <span className="col-span-2 font-mono text-sm">{selectedBooking.id}</span>
              </div>

              <div className="grid grid-cols-3 gap-2">
                <span className="font-medium">Code:</span>
                <span className="col-span-2 font-mono">{selectedBooking.code || 'N/A'}</span>
              </div>

              <div className="grid grid-cols-3 gap-2">
                <span className="font-medium">Mitglied:</span>
                <span className="col-span-2">{selectedBooking.ofMG || 'N/A'}</span>
              </div>

              <div className="grid grid-cols-3 gap-2">
                <span className="font-medium">Benutzer-ID:</span>
                <span className="col-span-2 font-mono text-sm">{selectedBooking.user?.id || 'N/A'}</span>
              </div>

              <div className="border-t pt-3">
                <h3 className="font-medium mb-2">Erwartete Werte</h3>

                <div className="grid grid-cols-3 gap-2">
                  <span className="text-sm">Betrag:</span>
                  <span className="col-span-2 text-sm font-semibold">
                    {formatCurrency(selectedBooking.expectedAmount)}
                  </span>
                </div>

                <div className="grid grid-cols-3 gap-2 mt-2">
                  <span className="text-sm">Verwendungszweck:</span>
                  <span className="col-span-2 text-sm">{selectedBooking.expectedPurpose || 'N/A'}</span>
                </div>
              </div>

              <div className="border-t pt-3">
                <h3 className="font-medium mb-2">Tatsächliche Werte</h3>

                <div className="grid grid-cols-3 gap-2">
                  <span className="text-sm">Betrag:</span>
                  <span className="col-span-2 text-sm font-semibold">
                    {formatCurrency(selectedBooking.actualAmount)}
                  </span>
                </div>

                <div className="grid grid-cols-3 gap-2 mt-2">
                  <span className="text-sm">Verwendungszweck:</span>
                  <span className="col-span-2 text-sm">{selectedBooking.actualPurpose || 'N/A'}</span>
                </div>

                <div className="grid grid-cols-3 gap-2 mt-2">
                  <span className="text-sm">Empfangen am:</span>
                  <span className="col-span-2 text-sm">{formatDate(selectedBooking.receivedAt)}</span>
                </div>
              </div>

              <div className="border-t pt-3">
                <div className="grid grid-cols-3 gap-2">
                  <span className="text-sm">Erstellt am:</span>
                  <span className="col-span-2 text-sm">{formatDate(selectedBooking.createdAt)}</span>
                </div>

                <div className="grid grid-cols-3 gap-2 mt-2">
                  <span className="text-sm">Aktualisiert am:</span>
                  <span className="col-span-2 text-sm">{formatDate(selectedBooking.updatedAt)}</span>
                </div>
              </div>
            </div>

            {/* Close button */}
            <div className="flex justify-end pt-4">
              <Button onClick={handleCloseModal}>Schließen</Button>
            </div>
          </div>
        )}
      </Dialog>
    </div>
  );
}
