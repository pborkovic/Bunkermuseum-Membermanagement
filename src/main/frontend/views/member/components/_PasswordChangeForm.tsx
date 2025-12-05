/**
 * @fileoverview Password Change Form component for member settings.
 *
 * This component provides a secure form for users to change their password
 * with client-side validation using Zod schema.
 *
 * @module views/member/components/PasswordChangeForm
 * @author Philipp Borkovic
 */

import { useState, useCallback } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { FaLock } from 'react-icons/fa';
import { toast } from 'sonner';
import { AuthController } from 'Frontend/generated/endpoints';
import { passwordChangeSchema } from '../schemas/validation';
import { useFormValidation } from '../hooks';
import { getErrorMessage } from '../utils/errorHandling';
import { SUCCESS_MESSAGES, ERROR_MESSAGES, UI_TEXT } from '../constants';
import type { PasswordFormData } from '../types';

/**
 * PasswordChangeForm Component.
 *
 * Provides password change functionality with the following features:
 * - Real-time Zod validation
 * - Error display with field-specific messages
 * - Loading states during submission
 * - Auto-clear on success
 * - Type-safe error handling (no `any` types)
 *
 * **Validation Rules:**
 * - Current password required
 * - New password must be at least 8 characters
 * - New password must contain letters and numbers
 * - New password must be different from current
 * - Confirmation must match new password
 *
 * @component
 *
 * @returns {JSX.Element} The rendered password change form
 *
 * @author Philipp Borkovic
 */
export default function PasswordChangeForm(): JSX.Element {
  const [formData, setFormData] = useState<PasswordFormData>({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  });
  const [isSaving, setIsSaving] = useState(false);

  const { errors, validate, clearError, clearAllErrors } =
    useFormValidation<PasswordFormData>(passwordChangeSchema);

  /**
   * Updates a single field in the form data.
   *
   * @param {keyof PasswordFormData} field - The field to update
   * @param {string} value - The new value
   */
  const updateField = useCallback(
    (field: keyof PasswordFormData, value: string): void => {
      setFormData((prev) => ({ ...prev, [field]: value }));
      clearError(field);
    },
    [clearError]
  );

  /**
   * Handles form submission with validation.
   *
   * @param {React.FormEvent} e - The form submit event
   */
  const handleSubmit = useCallback(
    async (e: React.FormEvent): Promise<void> => {
      e.preventDefault();

      if (!validate(formData)) {
        return;
      }

      setIsSaving(true);

      try {
        await AuthController.changePassword(
          formData.currentPassword,
          formData.newPassword
        );

        toast.success(SUCCESS_MESSAGES.PASSWORD_CHANGED);

        setFormData({
          currentPassword: '',
          newPassword: '',
          confirmPassword: '',
        });
        clearAllErrors();
      } catch (error) {
        const errorMessage = getErrorMessage(
          error,
          ERROR_MESSAGES.CHANGE_PASSWORD_FAILED
        );
        toast.error(errorMessage);
      } finally {
        setIsSaving(false);
      }
    },
    [formData, validate, clearAllErrors]
  );

  /**
   * Renders a password input field with error handling.
   *
   * @param {Object} params - Field parameters
   * @param {string} params.id - Input ID
   * @param {string} params.label - Field label
   * @param {keyof PasswordFormData} params.field - Form field name
   * @param {string} [params.helpText] - Optional help text
   *
   * @returns {JSX.Element} The rendered input field
   */
  const renderPasswordField = useCallback(
    ({
      id,
      label,
      field,
      helpText,
    }: {
      id: string;
      label: string;
      field: keyof PasswordFormData;
      helpText?: string;
    }): JSX.Element => {
      const hasError = Boolean(errors[field]);

      return (
        <div className="space-y-2">
          <Label htmlFor={id}>{label}</Label>
          <Input
            id={id}
            type="password"
            value={formData[field]}
            onChange={(e) => updateField(field, e.target.value)}
            disabled={isSaving}
            required
            className={`border-black text-black ${hasError ? 'border-red-500' : ''}`}
            autoComplete={field === 'currentPassword' ? 'current-password' : 'new-password'}
          />
          {hasError ? (
            <p className="text-xs text-red-600 mt-1">{errors[field]}</p>
          ) : (
            helpText && <p className="text-xs text-gray-600 mt-1">{helpText}</p>
          )}
        </div>
      );
    },
    [formData, errors, isSaving, updateField]
  );

  return (
    <div className="bg-white rounded-lg border border-gray-200 p-6">
      <div className="mb-4 flex items-center space-x-3">
        <FaLock
          className="text-black"
          style={{ width: '24px', height: '24px' }}
        />
        <h3 className="text-lg font-semibold text-black">
          {UI_TEXT.PASSWORD_CHANGE_LABEL}
        </h3>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4">
        {renderPasswordField({
          id: 'currentPassword',
          label: 'Aktuelles Passwort',
          field: 'currentPassword',
        })}

        {renderPasswordField({
          id: 'newPassword',
          label: 'Neues Passwort',
          field: 'newPassword',
          helpText: 'Mindestens 8 Zeichen, mit Buchstaben und Zahlen',
        })}

        {renderPasswordField({
          id: 'confirmPassword',
          label: 'Neues Passwort bestätigen',
          field: 'confirmPassword',
        })}

        <div className="flex justify-end pt-4 border-t">
          <Button
            type="submit"
            disabled={isSaving}
            className="bg-black text-white hover:bg-gray-800"
          >
            {isSaving ? `${UI_TEXT.SAVING}..` : UI_TEXT.CHANGE_PASSWORD}
          </Button>
        </div>
      </form>
    </div>
  );
}
