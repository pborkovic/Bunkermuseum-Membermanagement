import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Icon } from '@vaadin/react-components';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { DatePicker } from '@/components/ui/date-picker';
import { profileFormSchema } from '../schemas/validation';

/**
 * Gender options for the Anrede (salutation) field.
 */
const ANREDE_OPTIONS = [
  { value: 'männlich', label: 'Männlich' },
  { value: 'weiblich', label: 'Weiblich' },
  { value: 'divers', label: 'Divers' },
] as const;

/**
 * Profile form data interface.
 */
export interface ProfileFormData {
  name: string;
  email: string;
  salutation: string;
  academicTitle: string;
  rank: string;
  birthday: Date | undefined;
  phone: string;
  street: string;
  city: string;
  postalCode: string;
}

/**
 * ProfileInformationForm component - Edit user profile information.
 *
 * @component
 *
 * @param {Object} props - Component props
 * @param {ProfileFormData} props.formData - Current form data
 * @param {(data: ProfileFormData) => void} props.onChange - Form data change handler
 * @param {(e: React.FormEvent) => Promise<void>} props.onSubmit - Form submission handler
 * @param {boolean} props.isLoading - Loading state
 * @param {boolean} props.isSaving - Saving state
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

export default function ProfileInformationForm({
  formData,
  onChange,
  onSubmit,
  isLoading,
  isSaving
}: ProfileInformationFormProps): JSX.Element {
  const [errors, setErrors] = useState<Record<string, string>>({});

  /**
   * Validates the form data using Zod schema.
   * Returns true if valid, false otherwise.
   * Sets error messages for invalid fields.
   */
  const validateForm = (): boolean => {
    const result = profileFormSchema.safeParse(formData);

    if (!result.success) {
      const fieldErrors: Record<string, string> = {};
      result.error.issues.forEach((issue) => {
        const path = issue.path[0]?.toString();
        if (path) {
          fieldErrors[path] = issue.message;
        }
      });
      setErrors(fieldErrors);
      return false;
    }

    setErrors({});
    return true;
  };

  /**
   * Handles form submission with validation.
   */
  const handleSubmit = async (e: React.FormEvent): Promise<void> => {
    e.preventDefault();

    // Validate before submitting
    if (!validateForm()) {
      return;
    }

    // Clear errors and proceed with original onSubmit
    setErrors({});
    await onSubmit(e);
  };

  return (
    <div className="bg-white rounded-lg border border-gray-200 p-6">
      <div className="mb-4 flex items-center space-x-3">
        <Icon icon="vaadin:user-card" className="text-black" style={{ width: '24px', height: '24px' }} />
        <h3 className="text-lg font-semibold text-black">Profilinformationen</h3>
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center py-8">
          <Icon icon="vaadin:spinner" className="animate-spin text-black" style={{ width: '32px', height: '32px' }} />
        </div>
      ) : (
        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Basic Information */}
          <div>
            <h4 className="text-sm font-semibold mb-3 text-gray-700">Grundinformationen</h4>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="name">Name *</Label>
                <Input
                  id="name"
                  type="text"
                  value={formData.name}
                  onChange={(e) => {
                    onChange({ ...formData, name: e.target.value });
                    // Clear error on change
                    if (errors.name) {
                      setErrors({ ...errors, name: '' });
                    }
                  }}
                  disabled={isSaving}
                  required
                  className={`border-black text-black ${errors.name ? 'border-red-500' : ''}`}
                />
                {errors.name && (
                  <p className="text-xs text-red-600 mt-1">{errors.name}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="email">E-Mail *</Label>
                <Input
                  id="email"
                  type="email"
                  value={formData.email}
                  onChange={(e) => {
                    onChange({ ...formData, email: e.target.value });
                    // Clear error on change
                    if (errors.email) {
                      setErrors({ ...errors, email: '' });
                    }
                  }}
                  disabled={isSaving}
                  required
                  className={`border-black text-black ${errors.email ? 'border-red-500' : ''}`}
                />
                {errors.email && (
                  <p className="text-xs text-red-600 mt-1">{errors.email}</p>
                )}
              </div>
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
                  onValueChange={(value) => onChange({ ...formData, salutation: value })}
                >
                  <SelectTrigger className="w-full border-black text-black">
                    <SelectValue placeholder="Keine Angabe" />
                  </SelectTrigger>
                  <SelectContent className="bg-white border-black">
                    {ANREDE_OPTIONS.map(option => (
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

              <div className="space-y-2">
                <Label htmlFor="academicTitle">Akademischer Titel</Label>
                <Input
                  id="academicTitle"
                  type="text"
                  value={formData.academicTitle}
                  onChange={(e) => onChange({ ...formData, academicTitle: e.target.value })}
                  disabled={isSaving}
                  placeholder="z.B. Dr., Prof."
                  className="border-black text-black"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="rank">Rang</Label>
                <Input
                  id="rank"
                  type="text"
                  value={formData.rank}
                  onChange={(e) => onChange({ ...formData, rank: e.target.value })}
                  disabled={isSaving}
                  className="border-black text-black"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="birthday">Geburtsdatum</Label>
                <DatePicker
                  value={formData.birthday}
                  onChange={(date) => {
                    onChange({ ...formData, birthday: date });
                    // Clear error on change
                    if (errors.birthday) {
                      setErrors({ ...errors, birthday: '' });
                    }
                  }}
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
              <div className="space-y-2">
                <Label htmlFor="phone">Telefon</Label>
                <Input
                  id="phone"
                  type="tel"
                  value={formData.phone}
                  onChange={(e) => {
                    onChange({ ...formData, phone: e.target.value });
                    // Clear error on change
                    if (errors.phone) {
                      setErrors({ ...errors, phone: '' });
                    }
                  }}
                  disabled={isSaving}
                  className={`border-black text-black ${errors.phone ? 'border-red-500' : ''}`}
                />
                {errors.phone && (
                  <p className="text-xs text-red-600 mt-1">{errors.phone}</p>
                )}
              </div>
            </div>
          </div>

          {/* Address Information */}
          <div>
            <h4 className="text-sm font-semibold mb-3 text-gray-700">Adresse</h4>
            <div className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="street">Straße & Hausnummer</Label>
                <Input
                  id="street"
                  type="text"
                  value={formData.street}
                  onChange={(e) => onChange({ ...formData, street: e.target.value })}
                  disabled={isSaving}
                  className="border-black text-black"
                />
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="postalCode">Postleitzahl</Label>
                  <Input
                    id="postalCode"
                    type="text"
                    value={formData.postalCode}
                    onChange={(e) => {
                      onChange({ ...formData, postalCode: e.target.value });
                      // Clear error on change
                      if (errors.postalCode) {
                        setErrors({ ...errors, postalCode: '' });
                      }
                    }}
                    disabled={isSaving}
                    className={`border-black text-black ${errors.postalCode ? 'border-red-500' : ''}`}
                    placeholder="z.B. 1234 oder 12345"
                  />
                  {errors.postalCode && (
                    <p className="text-xs text-red-600 mt-1">{errors.postalCode}</p>
                  )}
                </div>

                <div className="space-y-2">
                  <Label htmlFor="city">Stadt</Label>
                  <Input
                    id="city"
                    type="text"
                    value={formData.city}
                    onChange={(e) => onChange({ ...formData, city: e.target.value })}
                    disabled={isSaving}
                    className="border-black text-black"
                  />
                </div>
              </div>
            </div>
          </div>

          <div className="flex justify-end pt-4 border-t">
            <Button
              type="submit"
              disabled={isSaving}
              className="bg-black text-white hover:bg-gray-800"
            >
              {isSaving ? 'Speichern...' : 'Profil aktualisieren'}
            </Button>
          </div>
        </form>
      )}
    </div>
  );
}
