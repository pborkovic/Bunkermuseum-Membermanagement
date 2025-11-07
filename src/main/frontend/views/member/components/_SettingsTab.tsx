/**
 * @fileoverview Settings Tab component for member dashboard.
 *
 * This component orchestrates all settings-related sub-components including
 * profile picture upload, profile information editing, password changes,
 * and account information display.
 *
 * @module views/member/components/SettingsTab
 * @author Philipp Borkovic
 */

import { useState, useEffect, useCallback } from 'react';
import { toast } from 'sonner';
import { AuthController, UserController } from 'Frontend/generated/endpoints';
import type UserDTO from 'Frontend/generated/com/bunkermuseum/membermanagement/dto/UserDTO';
import type User from 'Frontend/generated/com/bunkermuseum/membermanagement/model/User';
import ProfilePictureSection from './_ProfilePictureSection';
import ProfileInformationForm from './_ProfileInformationForm';
import PasswordChangeForm from './_PasswordChangeForm';
import AccountInformation from './_AccountInformation';
import { getErrorMessage } from '../utils/errorHandling';
import { getProfilePictureUrl } from '../utils/formatting';
import { SUCCESS_MESSAGES, ERROR_MESSAGES } from '../constants';
import type { ProfileFormData } from '../types';

/**
 * Props for the SettingsTab component.
 *
 * @interface SettingsTabProps
 *
 * @author Philipp Borkovic
 */
interface SettingsTabProps {
  onProfileUpdate: () => Promise<void>;
}

/**
 * SettingsTab Component.
 *
 * Provides comprehensive user settings management with:
 * - Profile picture upload and management
 * - Profile information editing (all fields)
 * - Password change functionality
 * - Account information display (email verification, roles)
 * - Type-safe error handling (no `any` types)
 * - Coordinated state management across sub-components
 *
 * **Sub-components:**
 * - ProfilePictureSection: Avatar upload
 * - ProfileInformationForm: Personal data editing
 * - PasswordChangeForm: Secure password updates
 * - AccountInformation: Read-only account details
 *
 * @component
 * @param {SettingsTabProps} props - Component props
 *
 * @returns {JSX.Element} The rendered settings tab content
 *
 * @author Philipp Borkovic
 */
export default function SettingsTab({ onProfileUpdate }: SettingsTabProps): JSX.Element {
  const [currentUser, setCurrentUser] = useState<UserDTO | null>(null);
  const [profilePictureUrl, setProfilePictureUrl] = useState<string | null>(null);
  const [isUploading, setIsUploading] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isSaving, setIsSaving] = useState(false);

  const [profileForm, setProfileForm] = useState<ProfileFormData>({
    name: '',
    email: '',
  });

  /**
   * Loads user profile information from the backend.
   */
  const loadProfile = useCallback(async (): Promise<void> => {
    try {
      setIsLoading(true);
      const user = await AuthController.getCurrentUser();

      if (!user) {
        toast.error(ERROR_MESSAGES.USER_NOT_FOUND);
        return;
      }

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
        postalCode: user.postalCode || '',
        country: user.country || '',
      });

      if (user.avatarPath && user.id) {
        setProfilePictureUrl(getProfilePictureUrl(user.id));
      } else {
        setProfilePictureUrl(null);
      }
    } catch (error) {
      const errorMessage = getErrorMessage(error, ERROR_MESSAGES.LOAD_PROFILE_FAILED);

      toast.error(errorMessage);
    } finally {
      setIsLoading(false);
    }
  }, []);

  /**
   * Loads profile on component mount.
   */
  useEffect(() => {
    loadProfile();
  }, [loadProfile]);

  /**
   * Handles profile picture upload completion.
   */
  const handleProfilePictureUpload = useCallback(async (): Promise<void> => {
    setIsUploading(true);
    try {
      await loadProfile();
      await onProfileUpdate();
    } finally {
      setIsUploading(false);
    }
  }, [loadProfile, onProfileUpdate]);

  /**
   * Handles profile information update submission.
   *
   * @param {React.FormEvent} e - Form submit event
   */
  const handleUpdateProfile = useCallback(
    async (e: React.FormEvent): Promise<void> => {
      e.preventDefault();
      setIsSaving(true);

      try {
        if (!currentUser || !currentUser.id) {
          throw new Error(ERROR_MESSAGES.USER_NOT_FOUND);
        }

        const updatedUser: User = {
          ...currentUser,
          name: profileForm.name,
          email: profileForm.email,
          salutation: profileForm.salutation || undefined,
          academicTitle: profileForm.academicTitle || undefined,
          rank: profileForm.rank || undefined,
          birthday: profileForm.birthday
            ? profileForm.birthday.toISOString().split('T')[0]
            : undefined,
          phone: profileForm.phone || undefined,
          street: profileForm.street || undefined,
          city: profileForm.city || undefined,
          postalCode: profileForm.postalCode || undefined,
          country: profileForm.country || undefined,
          active: true,
          deleted: false,
        } as User;

        await UserController.updateUser(currentUser.id, updatedUser);
        toast.success(SUCCESS_MESSAGES.PROFILE_UPDATED);

        await loadProfile();
        await onProfileUpdate();
      } catch (error) {
        const errorMessage = getErrorMessage(
          error,
          ERROR_MESSAGES.UPDATE_PROFILE_FAILED
        );

        toast.error(errorMessage);
      } finally {
        setIsSaving(false);
      }
    },
    [currentUser, profileForm, loadProfile, onProfileUpdate]
  );

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
