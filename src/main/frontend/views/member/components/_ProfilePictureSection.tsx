/**
 * @fileoverview Profile Picture Section component for member settings.
 *
 * This component handles profile picture upload and display with
 * comprehensive validation and error handling.
 *
 * @module views/member/components/ProfilePictureSection
 * @author Philipp Borkovic
 */

import { useCallback } from 'react';
import { Icon } from '@vaadin/react-components';
import { toast } from 'sonner';
import type UserDTO from 'Frontend/generated/com/bunkermuseum/membermanagement/dto/UserDTO';
import { useImageLoadError } from '../hooks';
import {
  getErrorMessage,
  extractResponseError,
  validateFile,
} from '../utils/errorHandling';
import {
  FILE_UPLOAD,
  PROFILE_PICTURE,
  API_ENDPOINTS,
  UI_TEXT,
  SUCCESS_MESSAGES,
  ERROR_MESSAGES,
} from '../constants';

/**
 * Props for the ProfilePictureSection component.
 *
 * @interface ProfilePictureSectionProps
 *
 * @author Philipp Borkovic
 */
interface ProfilePictureSectionProps {
  currentUser: UserDTO | null;
  profilePictureUrl: string | null;
  isUploading: boolean;
  onUpload: () => Promise<void>;
}

/**
 * ProfilePictureSection Component.
 *
 * Provides profile picture upload functionality with the following features:
 * - Preview of current profile picture
 * - File validation (size and type)
 * - Upload progress indication
 * - Error handling with user feedback
 * - Automatic fallback to default avatar on load errors
 * - Type-safe error handling (no `any` types)
 *
 * **File Constraints:**
 * - Maximum size: 5MB
 * - Accepted formats: JPEG, PNG, WebP
 *
 * @component
 * @param {ProfilePictureSectionProps} props - Component props
 *
 * @returns {JSX.Element} The rendered profile picture section
 *
 * @author Philipp Borkovic
 */
export default function ProfilePictureSection({
  currentUser,
  profilePictureUrl,
  isUploading,
  onUpload,
}: ProfilePictureSectionProps): JSX.Element {
  const { hasError, handleError, reset } = useImageLoadError();

  /**
   * Handles file selection and upload with validation.
   *
   * @param {React.ChangeEvent<HTMLInputElement>} e - File input change event
   */
  const handleFileChange = useCallback(
    async (e: React.ChangeEvent<HTMLInputElement>): Promise<void> => {
      const file = e.target.files?.[0];

      if (!file) {
        return;
      }

      const validationResult = validateFile(file, {
        maxSize: FILE_UPLOAD.MAX_SIZE,
        acceptedTypes: FILE_UPLOAD.ACCEPTED_TYPES,
      });

      if (!validationResult.isValid) {
        toast.error(validationResult.error);
        e.target.value = '';
        return;
      }

      try {
        const formData = new FormData();
        formData.append('file', file);

        const response = await fetch(API_ENDPOINTS.PROFILE_PICTURE_UPLOAD, {
          method: 'POST',
          body: formData,
        });

        if (!response.ok) {
          const errorData = await extractResponseError(response);

          throw new Error(errorData.message);
        }

        toast.success(SUCCESS_MESSAGES.PICTURE_UPLOADED);

        reset();

        await onUpload();
      } catch (error) {
        const errorMessage = getErrorMessage(error, ERROR_MESSAGES.UPLOAD_FAILED);

        toast.error(errorMessage);
      } finally {
        e.target.value = '';
      }
    },
    [onUpload, reset]
  );

  /**
   * Renders the profile picture preview with error handling.
   *
   * @returns {JSX.Element} The rendered preview
   */
  const renderPreview = useCallback((): JSX.Element => {
    const showImage = profilePictureUrl && !hasError;

    return (
      <div
        className={`w-${PROFILE_PICTURE.SIZE_LARGE} h-${PROFILE_PICTURE.SIZE_LARGE} rounded-full bg-gray-100 flex items-center justify-center overflow-hidden border-2 border-gray-200`}
        style={{
          width: `${PROFILE_PICTURE.SIZE_LARGE}px`,
          height: `${PROFILE_PICTURE.SIZE_LARGE}px`,
        }}
      >
        {showImage ? (
          <img
            src={profilePictureUrl}
            alt="Profile"
            className="w-full h-full object-cover"
            onError={handleError}
            loading="eager"
          />
        ) : (
          <Icon
            icon={PROFILE_PICTURE.DEFAULT_ICON}
            className="text-gray-400"
            style={{
              width: `${PROFILE_PICTURE.ICON_SIZE}px`,
              height: `${PROFILE_PICTURE.ICON_SIZE}px`,
            }}
          />
        )}
      </div>
    );
  }, [profilePictureUrl, hasError, handleError]);

  return (
    <div className="bg-white rounded-lg border border-gray-200 p-6">
      <div className="mb-4 flex items-center space-x-3">
        <Icon
          icon="vaadin:picture"
          className="text-black"
          style={{ width: '24px', height: '24px' }}
        />
        <h3 className="text-lg font-semibold text-black">
          {UI_TEXT.PROFILE_PICTURE_LABEL}
        </h3>
      </div>

      <div className="flex flex-col sm:flex-row items-start sm:items-center gap-6">
        {/* Profile Picture Preview */}
        <div className="flex-shrink-0">{renderPreview()}</div>

        {/* Upload Controls */}
        <div className="flex-1">
          <p className="text-sm text-gray-600 mb-3">
            Laden Sie ein Profilbild hoch (max. {FILE_UPLOAD.MAX_SIZE_MB}MB,{' '}
            {FILE_UPLOAD.ACCEPTED_EXTENSIONS.join('/')})
          </p>
          <input
            type="file"
            accept={FILE_UPLOAD.ACCEPTED_TYPES.join(',')}
            onChange={handleFileChange}
            disabled={isUploading}
            className="block w-full text-sm text-gray-600 file:mr-4 file:py-2 file:px-4 file:rounded-md file:border file:border-black file:text-sm file:font-medium file:bg-white file:text-black hover:file:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
            aria-label="Upload profile picture"
          />
          {isUploading && (
            <p className="text-sm text-gray-600 mt-2 flex items-center">
              <Icon
                icon="vaadin:spinner"
                className="animate-spin inline mr-2"
                style={{ width: '16px', height: '16px' }}
              />
              {UI_TEXT.UPLOADING}
            </p>
          )}
        </div>
      </div>
    </div>
  );
}
