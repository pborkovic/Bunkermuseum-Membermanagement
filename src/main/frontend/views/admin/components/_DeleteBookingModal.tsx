import { Dialog } from '@vaadin/react-components/Dialog';
import { Button } from '@/components/ui/button';
import { FaExclamationTriangle } from 'react-icons/fa';
import type BookingDTO from 'Frontend/generated/com/bunkermuseum/membermanagement/dto/BookingDTO';

/**
 * DeleteBookingModal component props.
 */
interface DeleteBookingModalProps {
  booking: BookingDTO | null;
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
}

/**
 * DeleteBookingModal component - Confirmation dialog for deleting a booking.
 *
 * Shows a warning with booking details and requires explicit confirmation
 * before deletion.
 *
 * @component
 *
 * @author Philipp Borkovic
 */
export default function DeleteBookingModal({
  booking,
  isOpen,
  onClose,
  onConfirm,
}: DeleteBookingModalProps): JSX.Element {
  return (
    <Dialog
      opened={isOpen}
      onOpenedChanged={(e: any) => {
        if (!e.detail.value) onClose();
      }}
      headerTitle="Buchung löschen"
    >
      {booking && (
        <div className="space-y-4 p-4 min-w-[400px]">
          <div className="flex justify-center">
            <FaExclamationTriangle className="text-destructive" style={{ width: '64px', height: '64px' }} />
          </div>

          <div className="text-center space-y-2">
            <p className="font-medium">Sind Sie sicher, dass Sie diese Buchung löschen möchten?</p>
            <p className="text-sm text-muted-foreground">
              Code: <span className="font-semibold">{booking.code || 'N/A'}</span>
            </p>
            <p className="text-sm text-muted-foreground">
              Mitglied: <span className="font-semibold">{booking.ofMG || 'N/A'}</span>
            </p>
            <p className="text-sm text-destructive mt-4">
              Diese Aktion kann nicht rückgängig gemacht werden.
            </p>
          </div>

          <div className="flex justify-end gap-2 pt-4">
            <Button variant="outline" onClick={onClose}>
              Abbrechen
            </Button>
            <Button variant="destructive" onClick={onConfirm}>
              Löschen
            </Button>
          </div>
        </div>
      )}
    </Dialog>
  );
}
