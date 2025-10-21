import { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Icon } from '@vaadin/react-components';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { DatePicker } from '@/components/ui/date-picker';
import { AuthController, UserController } from 'Frontend/generated/endpoints';
import type UserDTO from 'Frontend/generated/com/bunkermuseum/membermanagement/dto/UserDTO';
import type User from 'Frontend/generated/com/bunkermuseum/membermanagement/model/User';

/**
 * Gender options for the Anrede (salutation) field.
 *
 * @author Philipp Borkovic
 */
const ANREDE_OPTIONS = [
  { value: 'männlich', label: 'Männlich' },
  { value: 'weiblich', label: 'Weiblich' },
  { value: 'divers', label: 'Divers' },
] as const;

/**
 * SettingsTab component - Comprehensive user profile and account settings.
 *
 * Features:
 * - Profile picture upload to MinIO
 * - Edit all profile fields (name, email, salutation, title, rank, birthday, phone, address)
 * - Change password
 * - Account information display
 * - Loading and error states
 *
 * @component
 *
 * @returns {JSX.Element} The settings tab content
 *
 * @author Philipp Borkovic
 */
export default function SettingsTab(): JSX.Element {
  const [currentUser, setCurrentUser] = useState<UserDTO | null>(null);
  const [profilePictureUrl, setProfilePictureUrl] = useState<string | null>(null);
  const [isUploading, setIsUploading] = useState(false);

  // Profile form state
  const [profileForm, setProfileForm] = useState({
    name: '',
    email: '',
    salutation: '',
    academicTitle: '',
    rank: '',
    birthday: undefined as Date | undefined,
    phone: '',
    street: '',
    city: '',
    postalCode: ''
  });

  // Password form state
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  const [isLoading, setIsLoading] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  /**
   * Loads user profile information on component mount.
   */
  useEffect(() => {
    loadProfile();
  }, []);

  /**
   * Fetches the current user profile and profile picture.
   *
   * @author Philipp Borkovic
   */
  const loadProfile = async (): Promise<void> => {
    try {
      setIsLoading(true);
      setError('');
      const user = await AuthController.getCurrentUser();

      if (user) {
        setCurrentUser(user);
        setProfileForm({
          name: user.name || '',
          email: user.email || '',
          salutation: user.salutation || '',
          academicTitle: user.academicTitle || '',
          rank: user.rank || '',
          birthday: user.birthday ? new Date(user.birthday) : undefined,
          phone: user.phone || '',
          street: user.street || '',
          city: user.city || '',
          postalCode: user.postalCode || ''
        });

        // Load profile picture if exists
        if (user.avatarPath && user.id) {
          try {
            const response = await fetch(`/api/upload/profile-picture/${user.id}`);
            if (response.ok) {
              const data = await response.json();
              setProfilePictureUrl(data.url);
            }
          } catch (err) {
            console.error('Failed to load profile picture:', err);
          }
        }
      }
    } catch (err: any) {
      setError(err.message || 'Fehler beim Laden des Profils');
    } finally {
      setIsLoading(false);
    }
  };

  /**
   * Handles profile picture file selection and upload.
   *
   * @param {React.ChangeEvent<HTMLInputElement>} e - The file input change event
   *
   * @author Philipp Borkovic
   */
  const handleProfilePictureUpload = async (e: React.ChangeEvent<HTMLInputElement>): Promise<void> => {
    const file = e.target.files?.[0];
    if (!file) return;

    // Validate file size (5MB max)
    if (file.size > 5 * 1024 * 1024) {
      setError('Datei ist zu groß. Maximale Größe ist 5MB');
      return;
    }

    // Validate file type
    if (!['image/jpeg', 'image/jpg', 'image/png', 'image/webp'].includes(file.type)) {
      setError('Ungültiger Dateityp. Nur JPEG, PNG und WebP sind erlaubt');
      return;
    }

    try {
      setIsUploading(true);
      setError('');
      setSuccess('');

      const formData = new FormData();
      formData.append('file', file);

      const response = await fetch('/api/upload/profile-picture', {
        method: 'POST',
        body: formData,
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Upload fehlgeschlagen');
      }

      const data = await response.json();
      setProfilePictureUrl(data.url);
      setSuccess('Profilbild erfolgreich hochgeladen');

      // Reload profile to get updated avatar path
      await loadProfile();

    } catch (err: any) {
      setError(err.message || 'Fehler beim Hochladen des Profilbilds');
    } finally {
      setIsUploading(false);
    }
  };

  /**
   * Handles profile information update.
   *
   * @param {React.FormEvent} e - The form submission event
   *
   * @author Philipp Borkovic
   */
  const handleUpdateProfile = async (e: React.FormEvent): Promise<void> => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setIsSaving(true);

    try {
      if (!currentUser || !currentUser.id) {
        throw new Error('Benutzer nicht gefunden');
      }

      // Create a User object with the fields we want to update
      const updatedUser: User = {
        id: currentUser.id,
        name: profileForm.name,
        email: profileForm.email,
        salutation: profileForm.salutation || undefined,
        academicTitle: profileForm.academicTitle || undefined,
        rank: profileForm.rank || undefined,
        birthday: profileForm.birthday ? profileForm.birthday.toISOString().split('T')[0] : undefined,
        phone: profileForm.phone || undefined,
        street: profileForm.street || undefined,
        city: profileForm.city || undefined,
        postalCode: profileForm.postalCode || undefined,
        avatarPath: currentUser.avatarPath,
        emailVerifiedAt: currentUser.emailVerifiedAt,
        roles: currentUser.roles
      } as User;

      await UserController.updateUser(currentUser.id, updatedUser);
      setSuccess('Profil erfolgreich aktualisiert');

      // Reload profile
      await loadProfile();

    } catch (err: any) {
      setError(err.message || 'Fehler beim Aktualisieren des Profils');
    } finally {
      setIsSaving(false);
    }
  };

  /**
   * Handles password change.
   *
   * @param {React.FormEvent} e - The form submission event
   *
   * @author Philipp Borkovic
   */
  const handleChangePassword = async (e: React.FormEvent): Promise<void> => {
    e.preventDefault();
    setError('');
    setSuccess('');

    // Validate passwords match
    if (newPassword !== confirmPassword) {
      setError('Die neuen Passwörter stimmen nicht überein');
      return;
    }

    // Validate password strength
    if (newPassword.length < 8) {
      setError('Das neue Passwort muss mindestens 8 Zeichen lang sein');
      return;
    }

    setIsSaving(true);

    try {
      await AuthController.changePassword(currentPassword, newPassword);
      setSuccess('Passwort erfolgreich geändert');
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
    } catch (err: any) {
      setError(err.message || 'Fehler beim Ändern des Passworts');
    } finally {
      setIsSaving(false);
    }
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

  return (
    <div className="space-y-6 max-w-4xl">
      {/* Header */}
      <div>
        <h2 className="text-2xl font-bold text-black">Einstellungen</h2>
        <p className="text-sm text-gray-600 mt-1">
          Verwalten Sie Ihre Profilinformationen und Kontoeinstellungen
        </p>
      </div>

      {/* Success message */}
      {success && (
        <div className="rounded-md bg-green-50 border border-green-200 p-3 text-sm text-green-800">
          <Icon icon="vaadin:check-circle" className="inline mr-2" style={{ width: '16px', height: '16px' }} />
          {success}
        </div>
      )}

      {/* Error message */}
      {error && (
        <div className="rounded-md bg-red-50 border border-red-200 p-3 text-sm text-red-800">
          <Icon icon="vaadin:exclamation-circle" className="inline mr-2" style={{ width: '16px', height: '16px' }} />
          {error}
        </div>
      )}

      {/* Profile Picture */}
      <div className="bg-white rounded-lg border border-gray-200 p-6">
        <div className="mb-4 flex items-center space-x-3">
          <Icon icon="vaadin:picture" className="text-black" style={{ width: '24px', height: '24px' }} />
          <h3 className="text-lg font-semibold text-black">Profilbild</h3>
        </div>

        <div className="flex flex-col sm:flex-row items-start sm:items-center gap-6">
          {/* Profile Picture Preview */}
          <div className="flex-shrink-0">
            <div className="w-32 h-32 rounded-full bg-gray-100 flex items-center justify-center overflow-hidden border-2 border-gray-200">
              {profilePictureUrl ? (
                <img
                  src={profilePictureUrl}
                  alt="Profile"
                  className="w-full h-full object-cover"
                />
              ) : (
                <Icon icon="vaadin:user" className="text-gray-400" style={{ width: '64px', height: '64px' }} />
              )}
            </div>
          </div>

          {/* Upload Controls */}
          <div className="flex-1">
            <p className="text-sm text-gray-600 mb-3">
              Laden Sie ein Profilbild hoch (max. 5MB, JPEG/PNG/WebP)
            </p>
            <input
              type="file"
              accept="image/jpeg,image/jpg,image/png,image/webp"
              onChange={handleProfilePictureUpload}
              disabled={isUploading}
              className="block w-full text-sm text-gray-600 file:mr-4 file:py-2 file:px-4 file:rounded-md file:border file:border-black file:text-sm file:font-medium file:bg-white file:text-black hover:file:bg-gray-50 disabled:opacity-50"
            />
            {isUploading && (
              <p className="text-sm text-gray-600 mt-2">
                <Icon icon="vaadin:spinner" className="animate-spin inline mr-2" style={{ width: '16px', height: '16px' }} />
                Hochladen...
              </p>
            )}
          </div>
        </div>
      </div>

      {/* Profile Information */}
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
          <form onSubmit={handleUpdateProfile} className="space-y-6">
            {/* Basic Information */}
            <div>
              <h4 className="text-sm font-semibold mb-3 text-gray-700">Grundinformationen</h4>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="name">Name *</Label>
                  <Input
                    id="name"
                    type="text"
                    value={profileForm.name}
                    onChange={(e) => setProfileForm({ ...profileForm, name: e.target.value })}
                    disabled={isSaving}
                    required
                    className="border-black text-black"
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="email">E-Mail *</Label>
                  <Input
                    id="email"
                    type="email"
                    value={profileForm.email}
                    onChange={(e) => setProfileForm({ ...profileForm, email: e.target.value })}
                    disabled={isSaving}
                    required
                    className="border-black text-black"
                  />
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
                    value={profileForm.salutation || undefined}
                    onValueChange={(value) => setProfileForm({ ...profileForm, salutation: value })}
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
                    value={profileForm.academicTitle}
                    onChange={(e) => setProfileForm({ ...profileForm, academicTitle: e.target.value })}
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
                    value={profileForm.rank}
                    onChange={(e) => setProfileForm({ ...profileForm, rank: e.target.value })}
                    disabled={isSaving}
                    className="border-black text-black"
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="birthday">Geburtsdatum</Label>
                  <DatePicker
                    value={profileForm.birthday}
                    onChange={(date) => setProfileForm({ ...profileForm, birthday: date })}
                  />
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
                    value={profileForm.phone}
                    onChange={(e) => setProfileForm({ ...profileForm, phone: e.target.value })}
                    disabled={isSaving}
                    className="border-black text-black"
                  />
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
                    value={profileForm.street}
                    onChange={(e) => setProfileForm({ ...profileForm, street: e.target.value })}
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
                      value={profileForm.postalCode}
                      onChange={(e) => setProfileForm({ ...profileForm, postalCode: e.target.value })}
                      disabled={isSaving}
                      className="border-black text-black"
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="city">Stadt</Label>
                    <Input
                      id="city"
                      type="text"
                      value={profileForm.city}
                      onChange={(e) => setProfileForm({ ...profileForm, city: e.target.value })}
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

      {/* Change Password */}
      <div className="bg-white rounded-lg border border-gray-200 p-6">
        <div className="mb-4 flex items-center space-x-3">
          <Icon icon="vaadin:lock" className="text-black" style={{ width: '24px', height: '24px' }} />
          <h3 className="text-lg font-semibold text-black">Passwort ändern</h3>
        </div>

        <form onSubmit={handleChangePassword} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="currentPassword">Aktuelles Passwort</Label>
            <Input
              id="currentPassword"
              type="password"
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
              disabled={isSaving}
              required
              className="border-black text-black"
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="newPassword">Neues Passwort</Label>
            <Input
              id="newPassword"
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              disabled={isSaving}
              required
              className="border-black text-black"
            />
            <p className="text-xs text-gray-600">
              Mindestens 8 Zeichen
            </p>
          </div>

          <div className="space-y-2">
            <Label htmlFor="confirmPassword">Neues Passwort bestätigen</Label>
            <Input
              id="confirmPassword"
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              disabled={isSaving}
              required
              className="border-black text-black"
            />
          </div>

          <div className="flex justify-end pt-4 border-t">
            <Button
              type="submit"
              disabled={isSaving}
              className="bg-black text-white hover:bg-gray-800"
            >
              {isSaving ? 'Ändern...' : 'Passwort ändern'}
            </Button>
          </div>
        </form>
      </div>

      {/* Account Information */}
      <div className="bg-white rounded-lg border border-gray-200 p-6">
        <div className="mb-4 flex items-center space-x-3">
          <Icon icon="vaadin:info-circle" className="text-black" style={{ width: '24px', height: '24px' }} />
          <h3 className="text-lg font-semibold text-black">Kontoinformationen</h3>
        </div>

        <div className="space-y-3 text-sm">
          <div className="grid grid-cols-3 gap-2">
            <span className="font-medium text-gray-700">Benutzer-ID:</span>
            <span className="col-span-2 font-mono text-xs">{currentUser?.id}</span>
          </div>

          <div className="grid grid-cols-3 gap-2">
            <span className="font-medium text-gray-700">E-Mail verifiziert:</span>
            <span className="col-span-2">
              {currentUser?.emailVerifiedAt ? (
                <>
                  <Icon icon="vaadin:check-circle" className="inline text-green-600 mr-1" style={{ width: '16px', height: '16px' }} />
                  Verifiziert am {formatDate(currentUser.emailVerifiedAt)}
                </>
              ) : (
                <>
                  <Icon icon="vaadin:close-circle" className="inline text-red-600 mr-1" style={{ width: '16px', height: '16px' }} />
                  Nicht verifiziert
                </>
              )}
            </span>
          </div>

          {currentUser?.roles && currentUser.roles.length > 0 && (
            <div className="grid grid-cols-3 gap-2">
              <span className="font-medium text-gray-700">Rollen:</span>
              <span className="col-span-2">
                {currentUser.roles.map((role) => role?.name).filter(Boolean).join(', ')}
              </span>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
