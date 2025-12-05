/**
 * @fileoverview Account Information component for member settings.
 *
 * This component displays read-only account information including
 * email verification status and assigned roles.
 *
 * @module views/member/components/AccountInformation
 * @author Philipp Borkovic
 */

import { useMemo } from 'react';
import { FaInfoCircle, FaCheckCircle } from 'react-icons/fa';
import type UserDTO from 'Frontend/generated/com/bunkermuseum/membermanagement/dto/UserDTO';
import { formatDate, formatRoles } from '../utils/formatting';
import { UI_TEXT } from '../constants';

/**
 * Props for the AccountInformation component.
 *
 * @interface AccountInformationProps
 */
interface AccountInformationProps {
  /** Current user data from authentication context */
  currentUser: UserDTO | null;
}

/**
 * AccountInformation Component.
 *
 * Displays read-only account information including:
 * - Email verification status with verification date
 * - User roles (comma-separated)
 *
 * The component handles null values gracefully and displays
 * appropriate fallback text when data is unavailable.
 *
 * @component
 * @param {AccountInformationProps} props - Component props
 *
 * @returns {JSX.Element} The rendered account information section
 *
 * @author Philipp Borkovic
 */
export default function AccountInformation({
  currentUser,
}: AccountInformationProps): JSX.Element {
  /**
   * Formatted roles string, memoized to prevent unnecessary recalculations.
   */
  const rolesText = useMemo(() => {
    if (!currentUser?.roles) return null;
    return formatRoles(currentUser.roles);
  }, [currentUser?.roles]);

  /**
   * Email verification status, memoized for performance.
   */
  const emailVerificationStatus = useMemo(() => {
    if (!currentUser) {
      return UI_TEXT.EMAIL_NOT_VERIFIED;
    }

    if (currentUser.emailVerifiedAt) {
      return `${UI_TEXT.EMAIL_VERIFIED} ${formatDate(currentUser.emailVerifiedAt)}`;
    }

    return UI_TEXT.EMAIL_NOT_VERIFIED;
  }, [currentUser]);

  /**
   * Whether email is verified.
   */
  const isEmailVerified = useMemo(
    () => Boolean(currentUser?.emailVerifiedAt),
    [currentUser?.emailVerifiedAt]
  );

  return (
    <div className="bg-white rounded-lg border border-gray-200 p-6">
      <div className="mb-4 flex items-center space-x-3">
        <FaInfoCircle
          className="text-black"
          style={{ width: '24px', height: '24px' }}
        />
        <h3 className="text-lg font-semibold text-black">
          {UI_TEXT.ACCOUNT_INFO_LABEL}
        </h3>
      </div>

      <div className="space-y-3 text-sm">
        {/* Email Verification Status */}
        <div className="grid grid-cols-3 gap-2">
          <span className="font-medium text-gray-700">E-Mail verifiziert:</span>
          <span className="col-span-2 text-black">
            {isEmailVerified ? (
              <>
                <FaCheckCircle
                  className="inline text-green-600 mr-1"
                  style={{ width: '16px', height: '16px' }}
                  aria-label="Email verified"
                />
                {emailVerificationStatus}
              </>
            ) : (
              <>{emailVerificationStatus}</>
            )}
          </span>
        </div>

        {/* User Roles */}
        {rolesText && (
          <div className="grid grid-cols-3 gap-2">
            <span className="font-medium text-gray-700">Rollen:</span>
            <span className="col-span-2 text-black">{rolesText}</span>
          </div>
        )}
      </div>
    </div>
  );
}
