/**
 * @fileoverview Profile Information Form component for member settings.
 *
 * This component provides a comprehensive form for users to edit their
 * personal information with real-time Zod validation.
 *
 * @module views/member/components/ProfileInformationForm
 * @author Philipp Borkovic
 */

import { useCallback } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Icon } from '@vaadin/react-components';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { DatePicker } from '@/components/ui/date-picker';
import { profileFormSchema } from '../schemas/validation';
import { useFormValidation } from '../hooks';
import { SALUTATION_OPTIONS, UI_TEXT } from '../constants';
import type { ProfileFormData } from '../types';

/**
 * Props for the ProfileInformationForm component.
 *
 * @interface ProfileInformationFormProps
 *
 * @author Philipp Borkovic
 */
interface ProfileInformationFormProps {
  formData: ProfileFormData;
  onChange: (data: ProfileFormData) => void;
  onSubmit: (e: React.FormEvent) => Promise<void>;
  isLoading: boolean;
  isSaving: boolean;
}

/**
 * ProfileInformationForm Component.
 *
 * Provides comprehensive profile editing functionality with:
 * - Real-time Zod validation
 * - Organized sections (Basic, Personal, Contact, Address)
 * - Field-specific error messages
 * - Loading and saving states
 * - Type-safe form handling (no `any` types)
 * - Responsive grid layout
 *
 * **Form Sections:**
 * - Basic Information: Name, Email
 * - Personal Data: Salutation, Academic Title, Rank, Birthday
 * - Contact: Phone
 * - Address: Street, Postal Code, City
 *
 * @component
 * @param {ProfileInformationFormProps} props - Component props
 *
 * @returns {JSX.Element} The rendered profile form
 *
 * @author Philipp Borkovic
 */
export default function ProfileInformationForm({
  formData,
  onChange,
  onSubmit,
  isLoading,
  isSaving,
}: ProfileInformationFormProps): JSX.Element {
  const { errors, validate, clearError } = useFormValidation<ProfileFormData>(profileFormSchema);

  /**
   * Updates a single field in the form data.
   *
   * @param {keyof ProfileFormData} field - The field to update
   * @param {string | Date | undefined} value - The new value
   */
  const updateField = useCallback(
    (field: keyof ProfileFormData, value: string | Date | undefined): void => {
      onChange({ ...formData, [field]: value });
      clearError(field);
    },
      [formData, onChange, clearError]
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

      await onSubmit(e);
    },
    [formData, validate, onSubmit]
  );

  /**
   * Renders a text input field with error handling.
   *
   * @param {Object} params - Field parameters
   * @returns {JSX.Element} The rendered input field
   */
  const renderTextField = useCallback(
    ({
      id,
      label,
      field,
      type = 'text',
      required = false,
      placeholder,
    }: {
      id: string;
      label: string;
      field: keyof ProfileFormData;
      type?: string;
      required?: boolean;
      placeholder?: string;
    }): JSX.Element => {
      const value = formData[field];
      const hasError = Boolean(errors[field]);

      return (
        <div className="space-y-2">
          <Label htmlFor={id}>
            {label} {required && '*'}
          </Label>
          <Input
            id={id}
            type={type}
            value={typeof value === 'string' ? value : (value === undefined ? '' : String(value))}
            onChange={(e) => updateField(field, e.target.value)}
            disabled={isSaving}
            required={required}
            placeholder={placeholder}
            className={`border-black text-black ${hasError ? 'border-red-500' : ''}`}
          />
          {hasError && <p className="text-xs text-red-600 mt-1">{errors[field]}</p>}
        </div>
      );
    },
    [formData, errors, isSaving, updateField]
  );

  if (isLoading) {
    return (
      <div className="bg-white rounded-lg border border-gray-200 p-6">
        <div className="mb-4 flex items-center space-x-3">
          <Icon
            icon="vaadin:user-card"
            className="text-black"
            style={{ width: '24px', height: '24px' }}
          />
          <h3 className="text-lg font-semibold text-black">
            {UI_TEXT.PROFILE_INFO_LABEL}
          </h3>
        </div>
        <div className="flex items-center justify-center py-8">
          <Icon
            icon="vaadin:spinner"
            className="animate-spin text-black"
            style={{ width: '32px', height: '32px' }}
          />
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg border border-gray-200 p-6">
      <div className="mb-4 flex items-center space-x-3">
        <Icon
          icon="vaadin:user-card"
          className="text-black"
          style={{ width: '24px', height: '24px' }}
        />
        <h3 className="text-lg font-semibold text-black">
          {UI_TEXT.PROFILE_INFO_LABEL}
        </h3>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Basic Information */}
        <div>
          <h4 className="text-sm font-semibold mb-3 text-gray-700">Grundinformationen</h4>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {renderTextField({
              id: 'name',
              label: 'Name',
              field: 'name',
              required: true,
            })}
            {renderTextField({
              id: 'email',
              label: 'E-Mail',
              field: 'email',
              type: 'email',
              required: true,
            })}
          </div>
        </div>

        {/* Personal Information */}
        <div>
          <h4 className="text-sm font-semibold mb-3 text-gray-700">Persönliche Daten</h4>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="salutation">Anrede</Label>
              <Select
                value={formData.salutation || undefined}
                onValueChange={(value) => updateField('salutation', value)}
              >
                <SelectTrigger className="w-full border-black text-black">
                  <SelectValue placeholder="Keine Angabe" />
                </SelectTrigger>
                <SelectContent className="bg-white border-black">
                  {SALUTATION_OPTIONS.map((option) => (
                    <SelectItem
                      key={option.value}
                      value={option.value}
                      className="text-black hover:bg-gray-100 focus:bg-gray-100"
                    >
                      {option.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {renderTextField({
              id: 'academicTitle',
              label: 'Akademischer Titel',
              field: 'academicTitle',
              placeholder: 'z.B. Dr., Prof.',
            })}

            {renderTextField({
              id: 'rank',
              label: 'Dienstgrad',
              field: 'rank',
            })}

            <div className="space-y-2">
              <Label htmlFor="birthday">Geburtsdatum</Label>
              <DatePicker
                value={formData.birthday}
                onChange={(date) => updateField('birthday', date)}
              />
              {errors.birthday && (
                <p className="text-xs text-red-600 mt-1">{errors.birthday}</p>
              )}
            </div>
          </div>
        </div>

        {/* Contact Information */}
        <div>
          <h4 className="text-sm font-semibold mb-3 text-gray-700">Kontaktdaten</h4>
          <div className="space-y-4">
            {renderTextField({
              id: 'phone',
              label: 'Telefon',
              field: 'phone',
              type: 'tel',
            })}
          </div>
        </div>

        {/* Address Information */}
        <div>
          <h4 className="text-sm font-semibold mb-3 text-gray-700">Adresse</h4>
          <div className="space-y-4">
            {renderTextField({
              id: 'street',
              label: 'Straße & Hausnummer',
              field: 'street',
            })}

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {renderTextField({
                id: 'postalCode',
                label: 'Postleitzahl',
                field: 'postalCode',
                placeholder: 'z.B. 1234 oder 12345',
              })}

              {renderTextField({
                id: 'city',
                label: 'Stadt',
                field: 'city',
              })}
            </div>

            {renderTextField({
              id: 'country',
              label: 'Land',
              field: 'country',
              placeholder: 'z.B. Deutschland',
            })}
          </div>
        </div>

        <div className="flex justify-end pt-4 border-t">
          <Button
            type="submit"
            disabled={isSaving}
            className="bg-black text-white hover:bg-gray-800"
          >
            {isSaving ? `${UI_TEXT.SAVING}..` : UI_TEXT.UPDATE_PROFILE}
          </Button>
        </div>
      </form>
    </div>
  );
}
