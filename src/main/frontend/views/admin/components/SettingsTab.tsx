import { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card } from '@/components/ui/card';
import { Icon } from '@vaadin/react-components';
import { AuthController, UserController } from 'Frontend/generated/endpoints';

/**
 * SettingsTab component - Admin profile and account settings.
 *
 * Features:
 * - View and edit admin profile information
 * - Change password
 * - Account management
 * - Loading and error states
 *
 * @component
 *
 * @returns {JSX.Element} The settings tab content
 *
 * @author Philipp Borkovic
 */
export default function SettingsTab(): JSX.Element {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  /**
   * Loads admin profile information on component mount.
   */
  useEffect(() => {
    loadProfile();
  }, []);

  /**
   * Fetches the current admin user profile.
   *
   * @author Philipp Borkovic
   */
  const loadProfile = async (): Promise<void> => {
    try {
      setIsLoading(true);
      setError('');
      const user = await AuthController.getCurrentUser();
      if (user) {
        setName(user.name || '');
        setEmail(user.email || '');
      }
    } catch (err: any) {
      setError(err.message || 'Fehler beim Laden des Profils');
    } finally {
      setIsLoading(false);
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
      const user = await AuthController.getCurrentUser();
      if (user && user.id) {
        await UserController.updateProfile(user.id, name, email);
        setSuccess('Profil erfolgreich aktualisiert');
      }
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

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h2 className="text-xl font-semibold">Einstellungen</h2>
        <p className="text-sm text-muted-foreground">
          Verwalten Sie Ihre Profilinformationen und Kontoeinstellungen
        </p>
      </div>

      {/* Success message */}
      {success && (
        <div className="rounded-md bg-success/10 p-3 text-sm text-success border border-success/20">
          <Icon icon="vaadin:check-circle" className="inline mr-2" style={{ width: '16px', height: '16px' }} />
          {success}
        </div>
      )}

      {/* Error message */}
      {error && (
        <div className="rounded-md bg-destructive/10 p-3 text-sm text-destructive">
          <Icon icon="vaadin:exclamation-circle" className="inline mr-2" style={{ width: '16px', height: '16px' }} />
          {error}
        </div>
      )}

      {/* Profile information */}
      <div className="bg-white rounded-lg border p-6">
        <div className="mb-4 flex items-center space-x-3">
          <Icon icon="vaadin:user" className="text-primary" style={{ width: '24px', height: '24px' }} />
          <h3 className="text-lg font-semibold">Profilinformationen</h3>
        </div>

        {isLoading ? (
          <div className="flex items-center justify-center py-8">
            <Icon icon="vaadin:spinner" className="animate-spin text-primary" style={{ width: '32px', height: '32px' }} />
          </div>
        ) : (
          <form onSubmit={handleUpdateProfile} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="name">Name</Label>
              <Input
                id="name"
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                disabled={isSaving}
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="email">E-Mail</Label>
              <Input
                id="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                disabled={isSaving}
                required
              />
            </div>

            <div className="flex justify-end">
              <Button type="submit" disabled={isSaving}>
                {isSaving ? 'Speichern...' : 'Profil aktualisieren'}
              </Button>
            </div>
          </form>
        )}
      </div>

      {/* Change password */}
      <div className="bg-white rounded-lg border p-6">
        <div className="mb-4 flex items-center space-x-3">
          <Icon icon="vaadin:lock" className="text-primary" style={{ width: '24px', height: '24px' }} />
          <h3 className="text-lg font-semibold">Passwort ändern</h3>
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
            />
            <p className="text-xs text-muted-foreground">
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
            />
          </div>

          <div className="flex justify-end">
            <Button type="submit" disabled={isSaving}>
              {isSaving ? 'Ändern...' : 'Passwort ändern'}
            </Button>
          </div>
        </form>
      </div>

      {/* Account information */}
      <div className="bg-white rounded-lg border p-6">
        <div className="mb-4 flex items-center space-x-3">
          <Icon icon="vaadin:info-circle" className="text-primary" style={{ width: '24px', height: '24px' }} />
          <h3 className="text-lg font-semibold">Kontoinformationen</h3>
        </div>

        <div className="space-y-3 text-sm">
          <div className="grid grid-cols-3 gap-2">
            <span className="font-medium">Rolle:</span>
            <span className="col-span-2">Administrator</span>
          </div>

          <div className="grid grid-cols-3 gap-2">
            <span className="font-medium">E-Mail verifiziert:</span>
            <span className="col-span-2">
              <Icon icon="vaadin:check-circle" className="inline text-success mr-1" style={{ width: '16px', height: '16px' }} />
              Verifiziert
            </span>
          </div>

          <div className="grid grid-cols-3 gap-2">
            <span className="font-medium">Konto erstellt:</span>
            <span className="col-span-2">{new Date().toLocaleDateString('de-DE')}</span>
          </div>
        </div>
      </div>
    </div>
  );
}
