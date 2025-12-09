import {Dialog} from '@vaadin/react-components/Dialog';
import {Button} from '@/components/ui/button';
import {FaCheck, FaIdCard, FaTimes} from 'react-icons/fa';
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from '@/components/ui/select';
import {DatePicker} from '@/components/ui/date-picker';
import {DialogOpenedChangedEvent} from '../../../types/vaadin';
import {ANREDE_OPTIONS} from '../utils/constants';
import type {ProfileFormData} from '../types';

/**
 * Props for the CreateMemberModal component.
 *
 * @property {boolean} isOpen - Whether the modal is open
 * @property {() => void} onClose - Callback when modal is closed
 * @property {ProfileFormData} formData - The form data for the new member
 * @property {(data: ProfileFormData) => void} onFormChange - Callback when form data changes
 * @property {() => Promise<void>} onCreate - Callback when create button is clicked
 * @property {Record<string, string>} validationErrors - Validation errors for form fields
 */
interface CreateMemberModalProps {
  isOpen: boolean;
  onClose: () => void;
  formData: ProfileFormData;
  onFormChange: (data: ProfileFormData) => void;
  onCreate: () => Promise<void>;
  validationErrors: Record<string, string>;
}

/**
 * CreateMemberModal component - Modal for creating a new member.
 *
 * This component provides a comprehensive form for creating new members with:
 * - Basic information (name, email)
 * - Personal data (salutation, academic title, rank, birthday)
 * - Contact information (phone)
 * - Address information (street, city, postal code, country)
 *
 * Features:
 * - Form validation with error display
 * - Responsive layout
 * - Field-level error highlighting
 * - Accessible form controls
 *
 * @component
 *
 * @param {CreateMemberModalProps} props - Component props
 * @returns {JSX.Element} The create member modal
 *
 * @author Philipp Borkovic
 */
export default function CreateMemberModal({
  isOpen,
  onClose,
  formData,
  onFormChange,
  onCreate,
  validationErrors
}: CreateMemberModalProps): JSX.Element {
  return (
    <Dialog
      opened={isOpen}
      onOpenedChanged={(e: DialogOpenedChangedEvent) => {
        if (!e.detail.value) onClose();
      }}
      headerTitle="Neues Mitglied erstellen"
      noCloseOnOutsideClick
    >
      <div
        className="p-4 sm:p-6 min-w-[300px] sm:min-w-[900px] lg:min-w-[1100px] max-w-[95vw] max-h-[90vh] overflow-y-auto"
        onKeyDown={(e) => e.stopPropagation()}
        onKeyUp={(e) => e.stopPropagation()}
        onKeyPress={(e) => e.stopPropagation()}
      >
        <div className="space-y-6">
          {/* Icon */}
          <div className="flex justify-center">
            <FaIdCard className="text-black" style={{ width: '64px', height: '64px' }} />
          </div>

          {/* Description */}
          <div className="text-center space-y-2">
            <p className="font-medium text-lg">Neues Mitglied anlegen</p>
            <p className="text-sm text-muted-foreground">
              Geben Sie die Mitgliedsdaten ein. Das neue Mitglied erhält eine E-Mail zur Passwort-Einrichtung.
            </p>
          </div>

          {/* Basic Information */}
          <div className="space-y-3">
            <label className="text-sm font-medium">Grundinformationen</label>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-2">
                <label className="text-xs text-muted-foreground">Name *</label>
                <input
                  type="text"
                  className={`w-full px-3 py-2 border rounded-md bg-white text-black placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:border-black transition-all ${
                    validationErrors.name ? 'border-red-500 focus:ring-red-500' : 'border-black focus:ring-black'
                  }`}
                  value={formData.name}
                  onChange={(e) => onFormChange({ ...formData, name: e.target.value })}
                  placeholder="Max Mustermann"
                  required
                />
                {validationErrors.name && (
                  <p className="text-xs text-red-600 mt-1">{validationErrors.name}</p>
                )}
              </div>

              <div className="space-y-2">
                <label className="text-xs text-muted-foreground">E-Mail *</label>
                <input
                  type="email"
                  className={`w-full px-3 py-2 border rounded-md bg-white text-black placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:border-black transition-all ${
                    validationErrors.email ? 'border-red-500 focus:ring-red-500' : 'border-black focus:ring-black'
                  }`}
                  value={formData.email}
                  onChange={(e) => onFormChange({ ...formData, email: e.target.value })}
                  placeholder="max@example.com"
                  required
                />
                {validationErrors.email && (
                  <p className="text-xs text-red-600 mt-1">{validationErrors.email}</p>
                )}
              </div>
            </div>
          </div>

          {/* Personal Information */}
          <div className="space-y-3">
            <label className="text-sm font-medium">Persönliche Daten</label>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-2">
                <label className="text-xs text-muted-foreground">Anrede</label>
                <Select
                  value={formData.salutation}
                  onValueChange={(value) => onFormChange({ ...formData, salutation: value })}
                >
                  <SelectTrigger className={`w-full text-black [&_svg]:text-black [&_svg]:opacity-100 [&_svg]:-mt-4 ${
                    validationErrors.salutation ? 'border-red-500' : 'border-black'
                  }`}>
                    <SelectValue placeholder="Wählen" />
                  </SelectTrigger>
                  <SelectContent className="bg-white border-black z-[9999]">
                    {ANREDE_OPTIONS.map(option => (
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
                {validationErrors.salutation && (
                  <p className="text-xs text-red-600 mt-1">{validationErrors.salutation}</p>
                )}
              </div>

              <div className="space-y-2">
                <label className="text-xs text-muted-foreground">Akademischer Titel</label>
                <input
                  type="text"
                  className={`w-full px-3 py-2 border rounded-md bg-white text-black placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:border-black transition-all ${
                    validationErrors.academicTitle ? 'border-red-500 focus:ring-red-500' : 'border-black focus:ring-black'
                  }`}
                  value={formData.academicTitle}
                  onChange={(e) => onFormChange({ ...formData, academicTitle: e.target.value })}
                  placeholder="z.B. Dr., Prof."
                />
                {validationErrors.academicTitle && (
                  <p className="text-xs text-red-600 mt-1">{validationErrors.academicTitle}</p>
                )}
              </div>

              <div className="space-y-2">
                <label className="text-xs text-muted-foreground">Dienstgrad</label>
                <input
                  type="text"
                  className={`w-full px-3 py-2 border rounded-md bg-white text-black placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:border-black transition-all ${
                    validationErrors.rank ? 'border-red-500 focus:ring-red-500' : 'border-black focus:ring-black'
                  }`}
                  value={formData.rank}
                  onChange={(e) => onFormChange({ ...formData, rank: e.target.value })}
                  placeholder="z.B. Oberst, Major"
                />
                {validationErrors.rank && (
                  <p className="text-xs text-red-600 mt-1">{validationErrors.rank}</p>
                )}
              </div>

              <div className="space-y-2">
                <label className="text-xs text-muted-foreground">Geburtsdatum</label>
                <DatePicker
                  value={formData.birthday}
                  onChange={(date) => onFormChange({ ...formData, birthday: date })}
                />
                {validationErrors.birthday && (
                  <p className="text-xs text-red-600 mt-1">{validationErrors.birthday}</p>
                )}
              </div>
            </div>
          </div>

          {/* Contact Information */}
          <div className="space-y-3">
            <label className="text-sm font-medium">Kontaktdaten</label>
            <div className="space-y-2">
              <label className="text-xs text-muted-foreground">Telefon</label>
              <input
                type="tel"
                className={`w-full px-3 py-2 border rounded-md bg-white text-black placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:border-black transition-all ${
                  validationErrors.phone ? 'border-red-500 focus:ring-red-500' : 'border-black focus:ring-black'
                }`}
                value={formData.phone}
                onChange={(e) => onFormChange({ ...formData, phone: e.target.value })}
                placeholder="+49 123 456789"
              />
              {validationErrors.phone && (
                <p className="text-xs text-red-600 mt-1">{validationErrors.phone}</p>
              )}
            </div>
          </div>

          {/* Address Information */}
          <div className="space-y-3">
            <label className="text-sm font-medium">Adresse</label>
            <div className="space-y-4">
              <div className="space-y-2">
                <label className="text-xs text-muted-foreground">Straße & Hausnummer</label>
                <input
                  type="text"
                  className={`w-full px-3 py-2 border rounded-md bg-white text-black placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:border-black transition-all ${
                    validationErrors.street ? 'border-red-500 focus:ring-red-500' : 'border-black focus:ring-black'
                  }`}
                  value={formData.street}
                  onChange={(e) => onFormChange({ ...formData, street: e.target.value })}
                  placeholder="Krainberg 73"
                />
                {validationErrors.street && (
                  <p className="text-xs text-red-600 mt-1">{validationErrors.street}</p>
                )}
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <label className="text-xs text-muted-foreground">Postleitzahl</label>
                  <input
                    type="text"
                    className={`w-full px-3 py-2 border rounded-md bg-white text-black placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:border-black transition-all ${
                      validationErrors.postalCode ? 'border-red-500 focus:ring-red-500' : 'border-black focus:ring-black'
                    }`}
                    value={formData.postalCode}
                    onChange={(e) => onFormChange({ ...formData, postalCode: e.target.value })}
                    placeholder="9587"
                  />
                  {validationErrors.postalCode && (
                    <p className="text-xs text-red-600 mt-1">{validationErrors.postalCode}</p>
                  )}
                </div>

                <div className="space-y-2">
                  <label className="text-xs text-muted-foreground">Stadt</label>
                  <input
                    type="text"
                    className={`w-full px-3 py-2 border rounded-md bg-white text-black placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:border-black transition-all ${
                      validationErrors.city ? 'border-red-500 focus:ring-red-500' : 'border-black focus:ring-black'
                    }`}
                    value={formData.city}
                    onChange={(e) => onFormChange({ ...formData, city: e.target.value })}
                    placeholder="Riegersdorf"
                  />
                  {validationErrors.city && (
                    <p className="text-xs text-red-600 mt-1">{validationErrors.city}</p>
                  )}
                </div>
              </div>

              <div className="space-y-2">
                <label className="text-xs text-muted-foreground">Land</label>
                <input
                  type="text"
                  className={`w-full px-3 py-2 border rounded-md bg-white text-black placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:border-black transition-all ${
                    validationErrors.country ? 'border-red-500 focus:ring-red-500' : 'border-black focus:ring-black'
                  }`}
                  value={formData.country}
                  onChange={(e) => onFormChange({ ...formData, country: e.target.value })}
                  placeholder="Österreich"
                />
                {validationErrors.country && (
                  <p className="text-xs text-red-600 mt-1">{validationErrors.country}</p>
                )}
              </div>
            </div>
          </div>
        </div>

        <div className="flex justify-end gap-2 pt-4">
          <Button
            variant="destructive"
            onClick={onClose}
            className="text-white"
          >
            <FaTimes
              className="mr-2"
              style={{ width: '16px', height: '16px', color: 'white' }}
            />
            Abbrechen
          </Button>
          <Button
            onClick={onCreate}
            className="bg-black text-white hover:bg-gray-800"
          >
            <FaCheck
              className="mr-2"
              style={{ width: '16px', height: '16px', color: 'white' }}
            />
            Erstellen
          </Button>
        </div>
      </div>
    </Dialog>
  );
}
