/**
 * @fileoverview Booking assignment modal component for administrators.
 *
 * This module provides a streamlined modal interface for assigning bookings
 * to users based on their member type. It implements a type-safe, validated
 * form with automatic error handling and user feedback.
 *
 * @module AssignBookingModal
 * @author Philipp Borkovic
 * @since 1.0.0
 */

import { useEffect, useState } from 'react';
import { Dialog } from '@vaadin/react-components/Dialog';
import { Icon } from '@vaadin/react-components';
import { Button } from '@/components/ui/button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { BookingController } from 'Frontend/generated/endpoints';
import MemberType from 'Frontend/generated/com/bunkermuseum/membermanagement/dto/MemberType';
import { z } from 'zod';

/**
 * Zod validation schema for booking assignment form.
 *
 * This schema provides type-safe, comprehensive validation for all form fields
 * with localized German error messages. It validates:
 * - Member type must be a valid enum value
 * - Expected amount must be a positive number > 0
 * - Actual amount must be a positive number > 0
 * - Actual purpose must be non-blank and <= 500 characters
 *
 * @constant
 * @type {z.ZodObject}
 *
 * @example
 * ```typescript
 * const result = bookingAssignmentSchema.safeParse({
 *   memberType: MemberType.REGULAR_MEMBERS,
 *   expectedAmount: '50.00',
 *   actualAmount: '50.00',
 *   actualPurpose: 'Mitgliedsbeitrag 2024'
 * });
 *
 * if (!result.success) {
 *   console.error(result.error.issues[0].message);
 * }
 * ```
 */
const bookingAssignmentSchema = z.object({
  memberType: z.nativeEnum(MemberType, {
    message: 'Bitte wählen Sie einen Mitgliedstyp aus.',
  }),
  expectedAmount: z
    .string()
    .min(1, 'Bitte geben Sie einen erwarteten Betrag ein.')
    .refine(
      (val) => !isNaN(Number(val)) && Number(val) > 0,
      'Bitte geben Sie einen gültigen erwarteten Betrag ein.'
    ),
  actualAmount: z
    .string()
    .min(1, 'Bitte geben Sie einen tatsächlichen Betrag ein.')
    .refine(
      (val) => !isNaN(Number(val)) && Number(val) > 0,
      'Bitte geben Sie einen gültigen tatsächlichen Betrag ein.'
    ),
  actualPurpose: z
    .string()
    .min(1, 'Bitte geben Sie den tatsächlichen Verwendungszweck ein.')
    .max(500, 'Verwendungszweck darf maximal 500 Zeichen lang sein.')
    .refine(
      (val) => val.trim().length > 0,
      'Bitte geben Sie den tatsächlichen Verwendungszweck ein.'
    ),
});

/**
 * TypeScript type inferred from the Zod schema.
 * Ensures type safety between schema and form state.
 *
 * @typedef {z.infer<typeof bookingAssignmentSchema>} BookingAssignmentFormData
 */
type BookingAssignmentFormData = z.infer<typeof bookingAssignmentSchema>;

/**
 * Props interface for the AssignBookingModal component.
 *
 * @interface AssignBookingModalProps
 * @property {boolean} isOpen - Controls modal visibility state
 * @property {Function} onClose - Callback invoked when modal is closed
 * @property {Function} [onAssigned] - Optional callback invoked after successful assignment with count
 *
 * @example
 * ```tsx
 * <AssignBookingModal
 *   isOpen={showModal}
 *   onClose={() => setShowModal(false)}
 *   onAssigned={(count) => console.log(`${count} bookings created`)}
 * />
 * ```
 */
interface AssignBookingModalProps {
  /** Controls whether the modal is visible */
  isOpen: boolean;

  /** Callback function invoked when the modal should be closed */
  onClose: () => void;

  /** Optional callback invoked after successful booking assignment
   * @param count - Number of bookings that were created
   */
  onAssigned?: (count: number) => void;
}

/**
 * AssignBookingModal - A modal dialog for assigning bookings to member groups.
 *
 * This component provides a streamlined interface for administrators to create
 * bookings for all users of a specified member type. It features:
 *
 * **Key Features:**
 * - Type-safe member type selection (Regular or Supporting members)
 * - Real-time form validation with German error messages
 * - Automatic submission state management
 * - Error handling with user-friendly feedback
 * - Responsive design for mobile and desktop
 * - Accessibility support with ARIA labels
 *
 * **Form Fields:**
 * 1. Member Type (dropdown) - Select target member group
 * 2. Expected Amount (EUR) - The expected transaction amount
 * 3. Actual Amount (EUR) - The actual received amount
 * 4. Actual Purpose - Transaction description/purpose
 *
 * **Validation Rules:**
 * - All fields are required
 * - Amounts must be positive numbers (> 0)
 * - Purpose text must not be blank
 * - Member type must be selected
 *
 * **Backend Integration:**
 * - Submits to BookingController.assignBookingToUsers()
 * - Receives count of created bookings on success
 * - Handles errors with localized messages
 *
 * @component
 * @param {AssignBookingModalProps} props - Component props
 * @returns {JSX.Element} The rendered modal dialog
 *
 * @example
 * ```tsx
 * const [isModalOpen, setIsModalOpen] = useState(false);
 *
 * const handleAssigned = (count: number) => {
 *   showNotification(`Successfully assigned booking to ${count} users`);
 *   loadBookings(); // Refresh the bookings list
 * };
 *
 * return (
 *   <>
 *     <Button onClick={() => setIsModalOpen(true)}>
 *       Neue Buchung zuweisen
 *     </Button>
 *     <AssignBookingModal
 *       isOpen={isModalOpen}
 *       onClose={() => setIsModalOpen(false)}
 *       onAssigned={handleAssigned}
 *     />
 *   </>
 * );
 * ```
 *
 * @author Philipp Borkovic
 * @since 1.0.0
 */
export default function AssignBookingModal({
  isOpen,
  onClose,
  onAssigned,
}: AssignBookingModalProps): JSX.Element {
  // ============================================================================
  // State Management
  // ============================================================================

  /**
   * Selected member type for booking assignment.
   * Defaults to REGULAR_MEMBERS (Ordentliche Mitglieder).
   * @type {MemberType}
   */
  const [memberType, setMemberType] = useState<MemberType>(MemberType.REGULAR_MEMBERS);

  /**
   * Expected transaction amount in EUR.
   * Stored as string for input compatibility, converted to number on submit.
   * @type {string}
   */
  const [expectedAmount, setExpectedAmount] = useState<string>('');

  /**
   * Actual transaction amount in EUR.
   * Stored as string for input compatibility, converted to number on submit.
   * @type {string}
   */
  const [actualAmount, setActualAmount] = useState<string>('');

  /**
   * Transaction purpose/description text.
   * @type {string}
   */
  const [actualPurpose, setActualPurpose] = useState<string>('');

  /**
   * Submission state flag.
   * True when form is being submitted to prevent duplicate submissions.
   * @type {boolean}
   */
  const [isSubmitting, setIsSubmitting] = useState(false);

  /**
   * Error message to display to the user.
   * Empty string when no error exists.
   * @type {string}
   */
  const [error, setError] = useState('');

  // ============================================================================
  // Effects
  // ============================================================================

  /**
   * Reset form state when modal opens.
   *
   * This effect ensures the form is in a clean state each time the modal
   * is opened, preventing stale data from previous submissions.
   *
   * @effect
   * @listens isOpen
   */
  useEffect(() => {
    if (isOpen) {
      setMemberType(MemberType.REGULAR_MEMBERS);
      setExpectedAmount('');
      setActualAmount('');
      setActualPurpose('');
      setError('');
      setIsSubmitting(false);
    }
  }, [isOpen]);

  // ============================================================================
  // Validation
  // ============================================================================

  /**
   * Validates all form fields using Zod schema before submission.
   *
   * This function uses the Zod validation schema to perform comprehensive
   * validation on all required fields and returns a localized German error
   * message if validation fails.
   *
   * **Validation Checks (via Zod):**
   * - Member type must be a valid MemberType enum value
   * - Expected amount must be a valid positive number > 0
   * - Actual amount must be a valid positive number > 0
   * - Actual purpose must not be blank and <= 500 characters
   *
   * @function
   * @returns {string | null} Error message in German if validation fails, null if valid
   *
   * @example
   * ```tsx
   * const error = validateForm();
   * if (error) {
   *   setError(error);
   *   return;
   * }
   * // Proceed with submission
   * ```
   */
  const validateForm = (): string | null => {
    const result = bookingAssignmentSchema.safeParse({
      memberType,
      expectedAmount,
      actualAmount,
      actualPurpose,
    });

    if (!result.success) {
      // Return the first validation error message
      return result.error.issues[0]?.message || 'Validierungsfehler';
    }

    return null;
  };

  // ============================================================================
  // Event Handlers
  // ============================================================================

  /**
   * Handles form submission and booking assignment.
   *
   * This async function orchestrates the complete submission workflow:
   * 1. Validates form data
   * 2. Sets submission state to prevent duplicates
   * 3. Prepares payload with type conversions
   * 4. Submits to backend via BookingController
   * 5. Invokes success callback with created booking count
   * 6. Closes modal on success
   * 7. Displays error message on failure
   * 8. Resets submission state in finally block
   *
   * **Error Handling:**
   * - Validation errors: Displayed in UI, no backend call
   * - Network errors: Caught and displayed with fallback message
   * - Backend errors: Error message from server displayed
   *
   * @async
   * @function
   * @returns {Promise<void>} Promise that resolves when submission completes
   * @throws {Error} Caught internally and displayed to user
   *
   * @example
   * ```tsx
   * <Button onClick={() => void handleConfirm()}>
   *   Zuweisen
   * </Button>
   * ```
   */
  const handleConfirm = async (): Promise<void> => {
    try {
      // Clear any previous errors
      setError('');

      // Validate form data
      const validationError = validateForm();
      if (validationError) {
        setError(validationError);
        return;
      }

      // Set submitting state to disable form
      setIsSubmitting(true);

      // Prepare payload with proper types
      const payload = {
        memberType,
        expectedAmount: Number(expectedAmount),
        actualAmount: Number(actualAmount),
        actualPurpose: actualPurpose.trim(),
      };

      // Submit to backend
      const count = await BookingController.assignBookingToUsers(payload);

      // Invoke success callback if provided
      if (onAssigned) {
        onAssigned(count || 0);
      }

      // Close modal on success
      onClose();
    } catch (e: any) {
      // Display error to user with fallback message
      setError(e?.message || 'Fehler beim Zuweisen der Buchung');
    } finally {
      // Always reset submission state
      setIsSubmitting(false);
    }
  };

  // ============================================================================
  // Render
  // ============================================================================

  return (
    <Dialog
      opened={isOpen}
      onOpenedChanged={(e: any) => {
        if (!e.detail.value) onClose();
      }}
      headerTitle="Buchung zuweisen"
    >
      <div className="p-6 min-w-[380px] sm:min-w-[560px] max-w-[95vw]">
        {/* Introduction */}
        <div className="mb-6 p-4 bg-gray-50 rounded-md border border-gray-200">
          <div className="flex items-start gap-3">
            <Icon
              icon="vaadin:info-circle"
              className="text-black flex-shrink-0 mt-0.5"
              style={{ width: '20px', height: '20px' }}
            />
            <p className="text-sm text-gray-700">
              Weisen Sie eine Buchung allen Mitgliedern eines bestimmten Mitgliedstyps zu.
              Die Buchung wird automatisch für alle Benutzer mit der ausgewählten Rolle erstellt.
            </p>
          </div>
        </div>

        {/* Form Fields */}
        <div className="space-y-4 mb-6">
          {/* Member Type Dropdown */}
          <div>
            <label className="block text-sm font-semibold text-black mb-2">
              Mitgliedstyp <span className="text-red-600">*</span>
            </label>
            <Select
              value={memberType}
              onValueChange={(value) => setMemberType(value as MemberType)}
              disabled={isSubmitting}
            >
              <SelectTrigger className="w-full border-black text-black [&_svg]:text-black [&_svg]:opacity-100 [&_svg]:-mt-4">
                <SelectValue />
              </SelectTrigger>
              <SelectContent className="bg-white border-black z-[9999]">
                <SelectItem value={MemberType.REGULAR_MEMBERS} className="text-black hover:bg-gray-100 hover:text-black focus:bg-gray-100 focus:text-black">
                  Ordentliche Mitglieder
                </SelectItem>
                <SelectItem value={MemberType.SUPPORTING_MEMBERS} className="text-black hover:bg-gray-100 hover:text-black focus:bg-gray-100 focus:text-black">
                  Fördernde Mitglieder
                </SelectItem>
              </SelectContent>
            </Select>
            <p className="text-xs text-gray-600 mt-1">
              Die Buchung wird allen Benutzern mit dieser Rolle zugewiesen.
            </p>
          </div>

          {/* Expected Amount */}
          <div>
            <label className="block text-sm font-semibold text-black mb-2">
              Erwarteter Betrag (EUR) <span className="text-red-600">*</span>
            </label>
            <div className="relative">
              <Icon
                icon="vaadin:euro"
                className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500"
                style={{ width: '18px', height: '18px' }}
              />
              <input
                type="number"
                step="0.01"
                min="0"
                inputMode="decimal"
                value={expectedAmount}
                onChange={(e) => setExpectedAmount(e.target.value)}
                placeholder="z.B. 50.00"
                className="w-full pl-10 pr-3 py-2.5 text-sm text-black border border-black rounded-md focus:outline-none focus:ring-2 focus:ring-black focus:ring-offset-1 placeholder:text-gray-400"
                disabled={isSubmitting}
              />
            </div>
          </div>

          {/* Actual Amount */}
          <div>
            <label className="block text-sm font-semibold text-black mb-2">
              Tatsächlicher Betrag (EUR) <span className="text-red-600">*</span>
            </label>
            <div className="relative">
              <Icon
                icon="vaadin:euro"
                className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500"
                style={{ width: '18px', height: '18px' }}
              />
              <input
                type="number"
                step="0.01"
                min="0"
                inputMode="decimal"
                value={actualAmount}
                onChange={(e) => setActualAmount(e.target.value)}
                placeholder="z.B. 50.00"
                className="w-full pl-10 pr-3 py-2.5 text-sm text-black border border-black rounded-md focus:outline-none focus:ring-2 focus:ring-black focus:ring-offset-1 placeholder:text-gray-400"
                disabled={isSubmitting}
              />
            </div>
          </div>

          {/* Actual Purpose */}
          <div>
            <label className="block text-sm font-semibold text-black mb-2">
              Tatsächlicher Verwendungszweck <span className="text-red-600">*</span>
            </label>
            <div className="relative">
              <Icon
                icon="vaadin:clipboard-text"
                className="absolute left-3 top-3 text-gray-500"
                style={{ width: '18px', height: '18px' }}
              />
              <textarea
                value={actualPurpose}
                onChange={(e) => setActualPurpose(e.target.value)}
                placeholder="z.B. Mitgliedsbeitrag 2024"
                rows={3}
                className="w-full pl-10 pr-3 py-2.5 text-sm text-black border border-black rounded-md focus:outline-none focus:ring-2 focus:ring-black focus:ring-offset-1 placeholder:text-gray-400 resize-none"
                disabled={isSubmitting}
              />
            </div>
          </div>
        </div>

        {/* Error Message */}
        {error && (
          <div className="rounded-md bg-red-50 border border-red-200 p-3 mb-4">
            <div className="flex items-start gap-2">
              <Icon
                icon="vaadin:warning"
                className="text-red-600 flex-shrink-0 mt-0.5"
                style={{ width: '18px', height: '18px' }}
              />
              <p className="text-sm text-red-600">{error}</p>
            </div>
          </div>
        )}

        {/* Action Buttons */}
        <div className="flex justify-end gap-2 pt-4">
          <Button
            variant="destructive"
            onClick={onClose}
            disabled={isSubmitting}
            className="text-white"
          >
            <Icon
              icon="vaadin:close"
              className="mr-2"
              style={{ width: '16px', height: '16px', color: 'white' }}
            />
            Abbrechen
          </Button>
          <Button
            onClick={() => {
              void handleConfirm();
            }}
            disabled={isSubmitting}
            className="bg-black text-white hover:bg-gray-800"
          >
            {isSubmitting ? (
              <>
                <Icon
                  icon="vaadin:spinner"
                  className="mr-2 animate-spin"
                  style={{ width: '16px', height: '16px', color: 'white' }}
                />
                Wird zugewiesen...
              </>
            ) : (
              <>
                <Icon
                  icon="vaadin:check"
                  className="mr-2"
                  style={{ width: '16px', height: '16px', color: 'white' }}
                />
                Zuweisen
              </>
            )}
          </Button>
        </div>
      </div>
    </Dialog>
  );
}
