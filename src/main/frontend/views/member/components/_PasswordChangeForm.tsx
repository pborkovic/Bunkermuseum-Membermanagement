import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Icon } from '@vaadin/react-components';
import { toast } from 'sonner';
import { AuthController } from 'Frontend/generated/endpoints';
import { passwordChangeSchema } from '../schemas/validation';

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
  const [errors, setErrors] = useState<Record<string, string>>({});

  /**
   * Validates the form data using Zod schema.
   * Returns true if valid, false otherwise.
   */
  const validateForm = (): boolean => {
    const result = passwordChangeSchema.safeParse({
      currentPassword,
      newPassword,
      confirmPassword,
    });

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

  const handleSubmit = async (e: React.FormEvent): Promise<void> => {
    e.preventDefault();

    // Validate form using Zod
    if (!validateForm()) {
      return;
    }

    setIsSaving(true);

    try {
      await AuthController.changePassword(currentPassword, newPassword);
      toast.success('Passwort erfolgreich geändert');
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
      setErrors({});
    } catch (err: any) {
      toast.error(err.message || 'Fehler beim Ändern des Passworts');
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className="bg-white rounded-lg border border-gray-200 p-6">
      <div className="mb-4 flex items-center space-x-3">
        <Icon icon="vaadin:lock" className="text-black" style={{ width: '24px', height: '24px' }} />
        <h3 className="text-lg font-semibold text-black">Passwort ändern</h3>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="space-y-2">
          <Label htmlFor="currentPassword">Aktuelles Passwort</Label>
          <Input
            id="currentPassword"
            type="password"
            value={currentPassword}
            onChange={(e) => {
              setCurrentPassword(e.target.value);
              // Clear error on change
              if (errors.currentPassword) {
                setErrors({ ...errors, currentPassword: '' });
              }
            }}
            disabled={isSaving}
            required
            className={`border-black text-black ${errors.currentPassword ? 'border-red-500' : ''}`}
          />
          {errors.currentPassword && (
            <p className="text-xs text-red-600 mt-1">{errors.currentPassword}</p>
          )}
        </div>

        <div className="space-y-2">
          <Label htmlFor="newPassword">Neues Passwort</Label>
          <Input
            id="newPassword"
            type="password"
            value={newPassword}
            onChange={(e) => {
              setNewPassword(e.target.value);
              // Clear error on change
              if (errors.newPassword) {
                setErrors({ ...errors, newPassword: '' });
              }
            }}
            disabled={isSaving}
            required
            className={`border-black text-black ${errors.newPassword ? 'border-red-500' : ''}`}
          />
          {errors.newPassword ? (
            <p className="text-xs text-red-600 mt-1">{errors.newPassword}</p>
          ) : (
            <p className="text-xs text-gray-600 mt-1">
              Mindestens 8 Zeichen, mit Buchstaben und Zahlen
            </p>
          )}
        </div>

        <div className="space-y-2">
          <Label htmlFor="confirmPassword">Neues Passwort bestätigen</Label>
          <Input
            id="confirmPassword"
            type="password"
            value={confirmPassword}
            onChange={(e) => {
              setConfirmPassword(e.target.value);
              // Clear error on change
              if (errors.confirmPassword) {
                setErrors({ ...errors, confirmPassword: '' });
              }
            }}
            disabled={isSaving}
            required
            className={`border-black text-black ${errors.confirmPassword ? 'border-red-500' : ''}`}
          />
          {errors.confirmPassword && (
            <p className="text-xs text-red-600 mt-1">{errors.confirmPassword}</p>
          )}
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
