import { Dialog } from '@vaadin/react-components/Dialog';
import { Button } from '@/components/ui/button';
import { Icon } from '@vaadin/react-components';
import type BookingDTO from 'Frontend/generated/com/bunkermuseum/membermanagement/dto/BookingDTO';

/**
 * BookingDetailsModal component props.
 */
interface BookingDetailsModalProps {
  booking: BookingDTO | null;
  isOpen: boolean;
  onClose: () => void;
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
 * BookingDetailsModal component - Displays detailed booking information.
 *
 * Shows all booking fields including expected vs actual values,
 * member information, and metadata.
 *
 * @component
 *
 * @author Philipp Borkovic
 */
export default function BookingDetailsModal({
  booking,
  isOpen,
  onClose,
}: BookingDetailsModalProps): JSX.Element {
  return (
    <Dialog
      opened={isOpen}
      onOpenedChanged={(e: any) => {
        if (!e.detail.value) onClose();
      }}
      headerTitle="Buchungsdetails"
    >
      {booking && (
        <div className="p-4 sm:p-6 min-w-[300px] sm:min-w-[600px] lg:min-w-[800px] max-w-[95vw]">
          {/* Header Section */}
          <div className="flex flex-col sm:flex-row items-start gap-4 sm:gap-6 pb-6 border-b">
            <div className="flex-shrink-0 bg-muted rounded-full p-4">
              <Icon icon="vaadin:invoice" className="text-foreground" style={{ width: '64px', height: '64px' }} />
            </div>
            <div className="flex-1">
              <h3 className="text-2xl font-semibold mb-1">{booking.code || 'Keine Code'}</h3>
              <p className="text-muted-foreground font-mono text-sm">{booking.id}</p>
            </div>
          </div>

          {/* Details Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-4 sm:gap-x-8 gap-y-4 py-6">
            {/* Member */}
            <div className="space-y-1">
              <label className="text-sm font-medium text-muted-foreground">Mitglied</label>
              <div className="text-sm font-medium">{booking.ofMG || 'N/A'}</div>
            </div>

            {/* User ID */}
            {booking.userId && (
              <div className="space-y-1">
                <label className="text-sm font-medium text-muted-foreground">Benutzer-ID</label>
                <div className="text-sm font-mono">{booking.userId}</div>
              </div>
            )}

            {/* Expected Section */}
            <div className="space-y-1 sm:col-span-2 border-t pt-4">
              <h4 className="font-semibold text-gray-900 mb-3">Erwartete Werte</h4>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="text-sm font-medium text-muted-foreground">Betrag</label>
                  <div className="text-lg font-semibold text-green-600">
                    {formatCurrency(booking.expectedAmount)}
                  </div>
                </div>
                <div>
                  <label className="text-sm font-medium text-muted-foreground">Verwendungszweck</label>
                  <div className="text-sm">{booking.expectedPurpose || 'N/A'}</div>
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
                    {formatCurrency(booking.actualAmount)}
                  </div>
                </div>
                <div>
                  <label className="text-sm font-medium text-muted-foreground">Verwendungszweck</label>
                  <div className="text-sm">{booking.actualPurpose || 'N/A'}</div>
                </div>
                <div>
                  <label className="text-sm font-medium text-muted-foreground">Empfangen am</label>
                  <div className="text-sm">{formatDate(booking.receivedAt)}</div>
                </div>
              </div>
            </div>

            {/* Additional Info */}
            <div className="space-y-1 sm:col-span-2 border-t pt-4">
              <h4 className="font-semibold text-gray-900 mb-3">Zusätzliche Informationen</h4>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                {booking.note && (
                  <div className="sm:col-span-2">
                    <label className="text-sm font-medium text-muted-foreground">Notiz</label>
                    <div className="text-sm">{booking.note}</div>
                  </div>
                )}
                {booking.accountStatementPage && (
                  <div>
                    <label className="text-sm font-medium text-muted-foreground">Kontoauszug Seite</label>
                    <div className="text-sm">{booking.accountStatementPage}</div>
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* Action Buttons */}
          <div className="flex justify-end gap-3 pt-6 border-t mt-6">
            <Button variant="destructive" onClick={onClose} className="text-white">
              <Icon icon="vaadin:close" className="mr-2" style={{ width: '16px', height: '16px', color: 'white' }} />
              Schließen
            </Button>
          </div>
        </div>
      )}
    </Dialog>
  );
}
