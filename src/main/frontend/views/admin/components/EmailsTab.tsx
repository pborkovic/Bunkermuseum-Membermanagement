import { useState, useEffect, useCallback } from 'react';
import { Button } from '@/components/ui/button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Icon } from '@vaadin/react-components/Icon';
import { EmailController } from 'Frontend/generated/endpoints';
import type Email from 'Frontend/generated/com/bunkermuseum/membermanagement/model/Email';
import type { PageResponse } from '../types';
import EmailsList from './_EmailsList';
import { SendEmailModal } from './_SendEmailModal';
import { useModal } from '../hooks/useModal';
import { useWindowSize } from '../hooks/useWindowSize';
import { PAGE_SIZE_OPTIONS, DEFAULT_PAGE_SIZE } from '../utils/constants';

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
  const { isMobile } = useWindowSize();
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
          className="flex items-center gap-2 bg-black text-white hover:bg-gray-800"
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
          <div className="relative">
            <Icon
              icon="vaadin:chevron-down"
              className="absolute left-2 top-1/2 -translate-y-1/2 pointer-events-none z-10"
              style={{ width: '14px', height: '14px', color: 'black' }}
            />
            <Select
              value={emailsPerPage.toString()}
              onValueChange={handlePageSizeChange}
            >
              <SelectTrigger className="w-20 pl-8 text-black bg-white border-black">
                <SelectValue className="text-black" />
              </SelectTrigger>
              <SelectContent className="bg-white border-black">
                {PAGE_SIZE_OPTIONS.map((size) => (
                  <SelectItem
                    key={size}
                    value={size.toString()}
                    className="text-black hover:bg-gray-100 hover:text-black focus:bg-gray-100 focus:text-black"
                  >
                    {size}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </div>
      </div>

      {/* Emails List */}
      <EmailsList
        emails={emails}
        isLoading={isLoading}
        currentPage={currentPage}
        totalPages={totalPages}
        totalElements={totalElements}
        emailsPerPage={emailsPerPage}
        isMobile={isMobile}
        onPageChange={handlePageChange}
      />

      {/* Send Email Modal */}
      <SendEmailModal
        isOpen={sendEmailModal.isOpen}
        onClose={sendEmailModal.close}
        onEmailSent={handleEmailSent}
      />
    </div>
  );
}
