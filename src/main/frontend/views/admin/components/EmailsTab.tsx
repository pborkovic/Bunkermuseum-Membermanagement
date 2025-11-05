import { useState, useEffect, useCallback } from 'react';
import { Button } from '@/components/ui/button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Icon } from '@vaadin/react-components/Icon';
import { EmailController } from 'Frontend/generated/endpoints';
import type Email from 'Frontend/generated/com/bunkermuseum/membermanagement/model/Email';
import type { PageResponse } from '../types';
import { EmailsList } from './_EmailsList';
import { SendEmailModal } from './_SendEmailModal';
import Pagination from './shared/_Pagination';
import PaginationInfo from './shared/_PaginationInfo';
import LoadingState from './shared/_LoadingState';
import EmptyState from './shared/_EmptyState';
import { useModal } from '../hooks/useModal';
import { PAGE_SIZE_OPTIONS, DEFAULT_PAGE_SIZE, LIST_CONTAINER_HEIGHT } from '../utils/constants';

/**
 * Emails tab component for the admin dashboard.
 *
 * Features:
 * - Paginated email list (server-side pagination)
 * - Send new email button (opens modal)
 * - Responsive table/card view
 * - Loading states
 * - Empty states
 * - Page size selector
 *
 * @author Philipp Borkovic
 */
export default function EmailsTab() {
  const [emails, setEmails] = useState<Email[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  // Pagination state
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [emailsPerPage, setEmailsPerPage] = useState(DEFAULT_PAGE_SIZE);

  // Modal state
  const sendEmailModal = useModal();

  // Load emails whenever pagination changes
  useEffect(() => {
    loadEmails();
  }, [currentPage, emailsPerPage]);

  const loadEmails = useCallback(async () => {
    try {
      setIsLoading(true);
      setError('');

      // Backend expects 0-indexed pages
      const pageResponse = await EmailController.getEmailsPage(
        currentPage - 1,
        emailsPerPage
      ) as unknown as PageResponse<Email>;

      // Filter out null/undefined entries
      const validEmails = (pageResponse.content || [])
        .filter((email): email is Email => email !== undefined && email !== null);

      setEmails(validEmails);
      setTotalPages(pageResponse.totalPages || 0);
      setTotalElements(pageResponse.totalElements || 0);
    } catch (err: any) {
      console.error('Error loading emails:', err);
      setError(err.message || 'Fehler beim Laden der E-Mails');
      setEmails([]);
    } finally {
      setIsLoading(false);
    }
  }, [currentPage, emailsPerPage]);

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  const handlePageSizeChange = (size: string) => {
    setEmailsPerPage(Number(size));
    setCurrentPage(1); // Reset to first page when changing page size
  };

  const handleEmailSent = () => {
    // Refresh the email list after sending
    loadEmails();
  };

  const startIndex = (currentPage - 1) * emailsPerPage + 1;
  const endIndex = Math.min(currentPage * emailsPerPage, totalElements);

  return (
    <div className="space-y-6">
      {/* Header with Send Email Button */}
      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-2xl font-bold">E-Mail-Verwaltung</h2>
          <p className="text-sm text-gray-600 mt-1">
            Übersicht aller gesendeten E-Mails
          </p>
        </div>
        <Button
          onClick={sendEmailModal.open}
          className="flex items-center gap-2"
        >
          <Icon icon="vaadin:envelope" style={{ width: '16px', height: '16px' }} />
          Neue E-Mail senden
        </Button>
      </div>

      {/* Error Display */}
      {error && (
        <div className="rounded-md bg-red-50 border border-red-200 p-3 text-sm text-red-800">
          {error}
        </div>
      )}

      {/* Controls Row: Page Size Selector */}
      <div className="flex justify-between items-center">
        <div className="flex items-center gap-2">
          <span className="text-sm text-gray-700">Einträge pro Seite:</span>
          <Select
            value={emailsPerPage.toString()}
            onValueChange={handlePageSizeChange}
          >
            <SelectTrigger className="w-20">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {PAGE_SIZE_OPTIONS.map((size) => (
                <SelectItem key={size} value={size.toString()}>
                  {size}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        {/* Pagination Info */}
        {!isLoading && totalElements > 0 && (
          <PaginationInfo
            startIndex={startIndex}
            endIndex={endIndex}
            totalElements={totalElements}
            itemLabel="E-Mails"
            isFiltered={false}
          />
        )}
      </div>

      {/* Emails List */}
      <div style={{ minHeight: `${LIST_CONTAINER_HEIGHT}px` }}>
        {isLoading ? (
          <LoadingState
            message="E-Mails werden geladen..."
            className={`h-[${LIST_CONTAINER_HEIGHT}px]`}
          />
        ) : totalElements === 0 ? (
          <EmptyState
            icon="vaadin:envelope-open"
            title="Keine E-Mails vorhanden"
            description="Es wurden noch keine E-Mails gesendet. Klicken Sie auf 'Neue E-Mail senden', um eine E-Mail zu versenden."
            className={`h-[${LIST_CONTAINER_HEIGHT}px]`}
          />
        ) : (
          <EmailsList emails={emails} isLoading={isLoading} />
        )}
      </div>

      {/* Pagination Controls */}
      {!isLoading && totalElements > 0 && (
        <div className="flex justify-center">
          <Pagination
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={handlePageChange}
          />
        </div>
      )}

      {/* Send Email Modal */}
      <SendEmailModal
        isOpen={sendEmailModal.isOpen}
        onClose={sendEmailModal.close}
        onEmailSent={handleEmailSent}
      />
    </div>
  );
}
