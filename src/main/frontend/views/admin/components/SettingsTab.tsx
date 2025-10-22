import { useState, useEffect } from 'react';
import { toast } from 'sonner';
import { AuthController, UserController } from 'Frontend/generated/endpoints';
import type UserDTO from 'Frontend/generated/com/bunkermuseum/membermanagement/dto/UserDTO';
import type User from 'Frontend/generated/com/bunkermuseum/membermanagement/model/User';
import ProfilePictureSection from './_ProfilePictureSection';
import ProfileInformationForm, { type ProfileFormData } from './_ProfileInformationForm';
import PasswordChangeForm from './_PasswordChangeForm';
import AccountInformation from './_AccountInformation';

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
 * @param {Object} props - Component props
 * @param {() => void} props.onProfileUpdate - Callback to refresh profile in parent component
 *
 * @returns {JSX.Element} The settings tab content
 *
 * @author Philipp Borkovic
 */
export default function SettingsTab({ onProfileUpdate }: { onProfileUpdate?: () => void }): JSX.Element {
  const [currentUser, setCurrentUser] = useState<UserDTO | null>(null);
  const [profilePictureUrl, setProfilePictureUrl] = useState<string | null>(null);
  const [isUploading, setIsUploading] = useState(false);

  // Profile form state
  const [profileForm, setProfileForm] = useState<ProfileFormData>({
    name: '',
    email: '',
    salutation: '',
    academicTitle: '',
    rank: '',
    birthday: undefined,
    phone: '',
    street: '',
    city: '',
    postalCode: ''
  });

  const [isLoading, setIsLoading] = useState(false);
  const [isSaving, setIsSaving] = useState(false);

  /**
   * Loads user profile information on component mount.
   */
  useEffect(() => {
    loadProfile();
  }, []);

  /**
   * Fetches the current user profile and profile picture.
   */
  const loadProfile = async (): Promise<void> => {
    try {
      setIsLoading(true);
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

        // Set profile picture URL directly if avatar path exists
        if (user.avatarPath && user.id) {
          setProfilePictureUrl(`/api/upload/profile-picture/${user.id}`);
        } else {
          setProfilePictureUrl(null);
        }
      }
    } catch (err: any) {
      toast.error('Fehler beim Laden des Profils');
    } finally {
      setIsLoading(false);
    }
  };

  /**
   * Handles profile picture upload completion.
   */
  const handleProfilePictureUpload = async (): Promise<void> => {
    setIsUploading(true);
    try {
      await loadProfile();

      // Notify parent component to refresh profile picture in navbar
      if (onProfileUpdate) {
        onProfileUpdate();
      }
    } finally {
      setIsUploading(false);
    }
  };

  /**
   * Handles profile information update.
   */
  const handleUpdateProfile = async (e: React.FormEvent): Promise<void> => {
    e.preventDefault();
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
      toast.success('Profil erfolgreich aktualisiert');

      // Reload profile
      await loadProfile();

      // Notify parent component to refresh user info
      if (onProfileUpdate) {
        onProfileUpdate();
      }

    } catch (err: any) {
      toast.error(err.message || 'Fehler beim Aktualisieren des Profils');
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      {/* Header */}
      <div>
        <h2 className="text-2xl font-bold text-black">Einstellungen</h2>
        <p className="text-sm text-gray-600 mt-1">
          Verwalten Sie Ihre Profilinformationen und Kontoeinstellungen
        </p>
      </div>

      {/* Profile Picture */}
      <ProfilePictureSection
        currentUser={currentUser}
        profilePictureUrl={profilePictureUrl}
        isUploading={isUploading}
        onUpload={handleProfilePictureUpload}
      />

      {/* Profile Information */}
      <ProfileInformationForm
        formData={profileForm}
        onChange={setProfileForm}
        onSubmit={handleUpdateProfile}
        isLoading={isLoading}
        isSaving={isSaving}
      />

      {/* Change Password */}
      <PasswordChangeForm />

      {/* Account Information */}
      <AccountInformation currentUser={currentUser} />
    </div>
  );
}
