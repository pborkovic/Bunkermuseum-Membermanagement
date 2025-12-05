import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { FaLock } from 'react-icons/fa';
import { toast } from 'sonner';
import { AuthController } from 'Frontend/generated/endpoints';
import { getErrorMessage } from '../../../types/vaadin';

/**
 * PasswordChangeForm component - Change user password.
 *
 * @component
 *
 * @author Philipp Borkovic
 */
export default function PasswordChangeForm(): JSX.Element {
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isSaving, setIsSaving] = useState(false);

  const handleSubmit = async (e: React.FormEvent): Promise<void> => {
    e.preventDefault();

    // Validate passwords match
    if (newPassword !== confirmPassword) {
      toast.error('Die neuen Passwörter stimmen nicht überein');
      return;
    }

    // Validate password strength
    if (newPassword.length < 8) {
      toast.error('Das neue Passwort muss mindestens 8 Zeichen lang sein');
      return;
    }

    setIsSaving(true);

    try {
      await AuthController.changePassword(currentPassword, newPassword);
      toast.success('Passwort erfolgreich geändert');
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
    } catch (err: unknown) {
      const errorMessage = getErrorMessage(err);
      toast.error(errorMessage || 'Fehler beim Ändern des Passworts');
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className="bg-white rounded-lg border border-gray-200 p-6">
      <div className="mb-4 flex items-center space-x-3">
        <FaLock className="text-black" style={{ width: '24px', height: '24px' }} />
        <h3 className="text-lg font-semibold text-black">Passwort ändern</h3>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4">
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
  );
}
