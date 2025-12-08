import React from 'react';
import {FaImage, FaSpinner, FaUser} from 'react-icons/fa';
import {toast} from 'sonner';
import type UserDTO from 'Frontend/generated/com/bunkermuseum/membermanagement/dto/UserDTO';
import {getErrorMessage} from '../../../types/vaadin';

/**
 * ProfilePictureSection component - Profile picture upload and display.
 *
 * @component
 *
 * @param {Object} props - Component props
 * @param {UserDTO | null} props.currentUser - Current user data
 * @param {string | null} props.profilePictureUrl - URL of the current profile picture
 * @param {boolean} props.isUploading - Upload in progress state
 * @param {() => Promise<void>} props.onUpload - Callback to refresh after upload
 *
 * @author Philipp Borkovic
 */
interface ProfilePictureSectionProps {
  currentUser: UserDTO | null;
  profilePictureUrl: string | null;
  isUploading: boolean;
  onUpload: () => Promise<void>;
}

export default function ProfilePictureSection({
  currentUser,
  profilePictureUrl,
  isUploading,
  onUpload
}: ProfilePictureSectionProps): JSX.Element {
  const [imageError, setImageError] = React.useState(false);
  const [selectedFileName, setSelectedFileName] = React.useState<string | null>(null);

  // Reset error state when URL changes
  React.useEffect(() => {
    setImageError(false);
  }, [profilePictureUrl]);

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>): Promise<void> => {
    const file = e.target.files?.[0];
    if (!file) {
      setSelectedFileName(null);
      return;
    }

    // Set the selected file name
    setSelectedFileName(file.name);

    // Validate file size (5MB max)
    if (file.size > 5 * 1024 * 1024) {
      toast.error('Datei ist zu groß. Maximale Größe ist 5MB');
      setSelectedFileName(null);
      e.target.value = '';
      return;
    }

    // Validate file type
    if (!['image/jpeg', 'image/jpg', 'image/png', 'image/webp'].includes(file.type)) {
      toast.error('Ungültiger Dateityp. Nur JPEG, PNG und WebP sind erlaubt');
      setSelectedFileName(null);
      e.target.value = '';
      return;
    }

    try {
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

      await response.json();
      toast.success('Profilbild erfolgreich hochgeladen');

      // Reload profile
      await onUpload();

      // Reset file name after successful upload
      setSelectedFileName(null);
      e.target.value = '';

    } catch (err: unknown) {
      const errorMessage = getErrorMessage(err);
      toast.error(errorMessage || 'Fehler beim Hochladen des Profilbilds');
      setSelectedFileName(null);
      e.target.value = '';
    }
  };

  return (
    <div className="bg-white rounded-lg border border-gray-200 p-6">
      <div className="mb-4 flex items-center space-x-3">
        <FaImage className="text-black" style={{ width: '24px', height: '24px' }} />
        <h3 className="text-lg font-semibold text-black">Profilbild</h3>
      </div>

      <div className="flex flex-col sm:flex-row items-start sm:items-center gap-6">
        {/* Profile Picture Preview */}
        <div className="flex-shrink-0">
          <div className="w-32 h-32 rounded-full bg-gray-100 flex items-center justify-center overflow-hidden border-2 border-gray-200">
            {profilePictureUrl && !imageError ? (
              <img
                src={profilePictureUrl}
                alt="Profile"
                className="w-full h-full object-cover"
                onError={() => {
                  console.error('Failed to load profile picture:', profilePictureUrl);
                  setImageError(true);
                }}
                onLoad={() => console.log('Profile picture loaded successfully')}
              />
            ) : (
              <FaUser className="text-gray-400" style={{ width: '64px', height: '64px' }} />
            )}
          </div>
        </div>

        {/* Upload Controls */}
        <div className="flex-1">
          <p className="text-sm text-gray-600 mb-3">
            Laden Sie ein Profilbild hoch (max. 5MB, JPEG/PNG/WebP)
          </p>
          <div className="flex items-center gap-3">
            <label className="inline-flex items-center px-4 py-2 border border-black rounded-md text-sm font-medium text-black bg-white hover:bg-gray-50 cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed">
              <input
                type="file"
                accept="image/jpeg,image/jpg,image/png,image/webp"
                onChange={handleFileChange}
                disabled={isUploading}
                className="hidden"
              />
              Datei auswählen
            </label>
            <span className="text-sm text-gray-600">
              {selectedFileName || 'Keine Datei ausgewählt'}
            </span>
          </div>
          {isUploading && (
            <p className="text-sm text-gray-600 mt-2">
              <FaSpinner className="animate-spin inline mr-2" style={{ width: '16px', height: '16px' }} />
              Hochladen...
            </p>
          )}
        </div>
      </div>
    </div>
  );
}
