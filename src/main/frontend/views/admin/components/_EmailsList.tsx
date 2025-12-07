import { memo, useMemo } from 'react';
import { FaEnvelopeOpen } from 'react-icons/fa';
import type EmailDTO from 'Frontend/generated/com/bunkermuseum/membermanagement/dto/EmailDTO';
import Pagination from './shared/_Pagination';
import PaginationInfo from './shared/_PaginationInfo';
import LoadingState from './shared/_LoadingState';
import EmptyState from './shared/_EmptyState';
import { formatDateTime } from '../utils/formatting';
import { LIST_CONTAINER_HEIGHT } from '../utils/constants';

/**
 * EmailsList component props.
 */
interface EmailsListProps {
  emails: EmailDTO[];
  isLoading: boolean;
  currentPage: number;
  totalPages: number;
  totalElements: number;
  emailsPerPage: number;
  isMobile: boolean;
  onPageChange: (page: number) => void;
}

/**
 * EmailsList component - Displays emails in a table/cards with pagination.
 *
 * Clean black & white design matching the UsersTab and BookingsTab style.
 *
 * Features:
 * - Responsive table/card layout
 * - Pagination controls
 * - Loading and empty states
 * - Memoized for performance optimization
 *
 * @component
 *
 * @author Philipp Borkovic
 */
function EmailsList({
  emails,
  isLoading,
  currentPage,
  totalPages,
  totalElements,
  emailsPerPage,
  isMobile,
  onPageChange,
}: EmailsListProps): JSX.Element {
  /**
   * Calculate display range for pagination info.
   * Memoized to prevent unnecessary recalculations.
   */
  const { startIndex, endIndex } = useMemo(() => ({
    startIndex: (currentPage - 1) * emailsPerPage + 1,
    endIndex: Math.min(currentPage * emailsPerPage, totalElements),
  }), [currentPage, emailsPerPage, totalElements]);

  /**
   * Determine empty state message.
   */
  const emptyStateMessage = useMemo(() => ({
    title: 'Keine E-Mails vorhanden',
    description: 'Es wurden noch keine E-Mails gesendet. Klicken Sie auf "Neue E-Mail senden", um eine E-Mail zu versenden.',
  }), []);

  if (isLoading) {
    return <LoadingState message="Lädt E-Mails..." className={`h-[${LIST_CONTAINER_HEIGHT}px]`} />;
  }

  if (emails.length === 0) {
    return (
      <EmptyState
        icon={<FaEnvelopeOpen />}
        title={emptyStateMessage.title}
        description={emptyStateMessage.description}
        className={`h-[${LIST_CONTAINER_HEIGHT}px]`}
      />
    );
  }

  return (
    <div>
      {/* Emails Grid - Fixed height for 10 entries */}
      <div className="rounded-lg overflow-hidden bg-white" style={{ height: '560px' }}>
        {/* Desktop Table */}
        <div className="hidden md:block h-full overflow-y-auto">
          <table className="w-full">
            <thead className="sticky top-0 bg-white z-10">
              <tr className="border-b border-gray-200">
                <th className="text-left py-3 px-3 lg:px-4 text-xs lg:text-sm font-semibold text-gray-700 bg-white">Von</th>
                <th className="text-left py-3 px-3 lg:px-4 text-xs lg:text-sm font-semibold text-gray-700 bg-white">An</th>
                <th className="text-left py-3 px-3 lg:px-4 text-xs lg:text-sm font-semibold text-gray-700 bg-white">Betreff</th>
                <th className="text-left py-3 px-3 lg:px-4 text-xs lg:text-sm font-semibold text-gray-700 bg-white">Gesendet von</th>
                <th className="text-left py-3 px-3 lg:px-4 text-xs lg:text-sm font-semibold text-gray-700 bg-white">Gesendet am</th>
              </tr>
            </thead>
            <tbody>
              {emails.map((email) => (
                <tr
                  key={email.id}
                  className="border-b border-gray-100 hover:bg-gray-50"
                >
                  <td className="py-3 px-3 lg:px-4 text-xs lg:text-sm text-gray-900">
                    <div className="max-w-[120px] lg:max-w-xs truncate" title={email.fromAddress}>
                      {email.fromAddress}
                    </div>
                  </td>
                  <td className="py-3 px-3 lg:px-4 text-xs lg:text-sm text-gray-900">
                    <div className="max-w-[120px] lg:max-w-xs truncate" title={email.toAddress}>
                      {email.toAddress}
                    </div>
                  </td>
                  <td className="py-3 px-3 lg:px-4 text-xs lg:text-sm text-gray-900">
                    <div className="max-w-[150px] lg:max-w-xs truncate" title={email.subject || ''}>
                      {email.subject || ''}
                    </div>
                  </td>
                  <td className="py-3 px-3 lg:px-4 text-xs lg:text-sm">
                    {email.user ? (
                      <div>
                        <p className="font-medium text-gray-900 truncate max-w-[100px] lg:max-w-xs">{email.user.name}</p>
                        <p className="text-xs text-gray-500 truncate max-w-[100px] lg:max-w-xs">{email.user.email}</p>
                      </div>
                    ) : (
                      <span className="inline-block px-2 py-1 text-xs bg-gray-100 text-gray-700 border border-gray-300 whitespace-nowrap">
                        System
                      </span>
                    )}
                  </td>
                  <td className="py-3 px-3 lg:px-4 text-xs lg:text-sm text-gray-600 whitespace-nowrap">{formatDateTime(email.createdAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Mobile Cards */}
        <div className="md:hidden space-y-3 p-3 sm:p-4 h-full overflow-y-auto">
          {emails.map((email) => (
            <div
              key={email.id}
              className="border border-gray-200 rounded-lg p-3 sm:p-4 hover:border-gray-300 transition-colors"
            >
              <div className="space-y-2">
                <div>
                  <div className="text-xs text-gray-600 font-medium">Von:</div>
                  <div className="text-sm font-medium text-gray-900 break-all">{email.fromAddress}</div>
                </div>
                <div>
                  <div className="text-xs text-gray-600 font-medium">An:</div>
                  <div className="text-sm font-medium text-gray-900 break-all">{email.toAddress}</div>
                </div>
                <div>
                  <div className="text-xs text-gray-600 font-medium">Betreff:</div>
                  <div className="text-sm font-medium text-gray-900 break-words">
                    {(email.subject || '').length > 50
                      ? `${(email.subject || '').substring(0, 50)}...`
                      : (email.subject || '')}
                  </div>
                </div>
                {email.user && (
                  <div className="pt-2 border-t border-gray-200">
                    <div className="text-xs text-gray-600 font-medium">Gesendet von:</div>
                    <div className="text-sm font-medium text-gray-900 break-words">{email.user.name}</div>
                    <div className="text-xs text-gray-500 break-all">{email.user.email}</div>
                  </div>
                )}
                {!email.user && (
                  <div className="pt-2 border-t border-gray-200">
                    <span className="inline-block px-2 py-1 text-xs bg-gray-100 border border-gray-300 font-medium">
                      System-E-Mail
                    </span>
                  </div>
                )}
                <div className="pt-2 text-xs text-gray-500 font-medium">
                  Gesendet: {formatDateTime(email.createdAt)}
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Pagination Footer */}
      <div className="flex flex-col-reverse sm:flex-row items-center justify-between gap-3 sm:gap-4 px-1 pt-3 sm:pt-4">
        {/* Results Info - Left side */}
        <PaginationInfo
          startIndex={startIndex}
          endIndex={endIndex}
          totalElements={totalElements}
          itemLabel="E-Mails"
          isFiltered={false}
        />

        {/* Pagination Controls - Right side */}
        <Pagination
          currentPage={currentPage}
          totalPages={totalPages}
          onPageChange={onPageChange}
        />
      </div>
    </div>
  );
}

/**
 * Memoized version of EmailsList component.
 * Only re-renders when props change.
 */
export default memo(EmailsList);

EmailsList.displayName = 'EmailsList';
