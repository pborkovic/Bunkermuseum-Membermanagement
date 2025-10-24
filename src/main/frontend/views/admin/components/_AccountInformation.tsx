import { Icon } from '@vaadin/react-components';
import type UserDTO from 'Frontend/generated/com/bunkermuseum/membermanagement/dto/UserDTO';

/**
 * AccountInformation component - Display account information.
 *
 * @component
 *
 * @param {Object} props - Component props
 * @param {UserDTO | null} props.currentUser - Current user data
 *
 * @author Philipp Borkovic
 */
interface AccountInformationProps {
    currentUser: UserDTO | null;
}

export default function AccountInformation({ currentUser }: AccountInformationProps) {

    /**
     * Formats a date to German locale string.
     */
    const formatDate = (dateString: string | null | undefined): string => {
        if (!dateString) return 'N/A';
        return new Date(dateString).toLocaleDateString('de-DE');
    };

    return (
        <div className="bg-white rounded-lg border border-gray-200 p-6">
            <div className="mb-4 flex items-center space-x-3">
                <Icon icon="vaadin:info-circle" className="text-black" style={{ width: '24px', height: '24px' }} />
                <h3 className="text-lg font-semibold text-black">Kontoinformationen</h3>
            </div>

            <div className="space-y-3 text-sm">
                <div className="grid grid-cols-3 gap-2">
                    <span className="font-medium text-gray-700">E-Mail verifiziert:</span>
                    <span className="col-span-2 text-black">
            {currentUser?.emailVerifiedAt ? (
                <>
                    <Icon icon="vaadin:check-circle" className="inline text-green-600 mr-1" style={{ width: '16px', height: '16px' }} />
                    Verifiziert am {formatDate(currentUser.emailVerifiedAt)}
                </>
            ) : (
                <>
                    Nicht verifiziert
                </>
            )}
          </span>
                </div>

                {currentUser?.roles && currentUser.roles.length > 0 && (
                    <div className="grid grid-cols-3 gap-2">
                        <span className="font-medium text-gray-700">Rollen:</span>
                        <span className="col-span-2 text-black">
              {currentUser.roles.map((role) => role?.name).filter(Boolean).join(', ')}
            </span>
                    </div>
                )}
            </div>
        </div>
    );
}
