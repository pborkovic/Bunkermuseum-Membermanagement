import { useState, useEffect, useCallback } from 'react';
import { Button } from '@/components/ui/button';
import { Dialog } from '@vaadin/react-components/Dialog';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { FaDownload, FaEnvelope, FaCloudDownloadAlt, FaTable, FaFileAlt, FaFileCode, FaCode } from 'react-icons/fa';
import { MdExpandMore } from 'react-icons/md';
import { EmailController } from 'Frontend/generated/endpoints';
import type EmailDTO from 'Frontend/generated/com/bunkermuseum/membermanagement/dto/EmailDTO';
import type { PageResponse } from '../types';
import { getErrorMessage, DialogOpenedChangedEvent } from '../../../types/vaadin';
import EmailsList from './_EmailsList';
import { SendEmailModal } from './_SendEmailModal';
import { useModal } from '../hooks/useModal';
import { useWindowSize } from '../hooks/useWindowSize';
import { PAGE_SIZE_OPTIONS, DEFAULT_PAGE_SIZE, EXPORT_EMAIL_TYPE_OPTIONS, EXPORT_FORMAT_OPTIONS } from '../utils/constants';

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
  const [emails, setEmails] = useState<EmailDTO[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  // Pagination state
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [emailsPerPage, setEmailsPerPage] = useState(DEFAULT_PAGE_SIZE);

  // Modal state
  const sendEmailModal = useModal();
  const exportModal = useModal();

  // Export modal state
  const [exportEmailType, setExportEmailType] = useState('system');
  const [exportFormat, setExportFormat] = useState('xlsx');

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
      ) as unknown as PageResponse<EmailDTO>;

      // Filter out null/undefined entries
      const validEmails = (pageResponse.content || [])
        .filter((email): email is EmailDTO => email !== undefined && email !== null);

      setEmails(validEmails);
      setTotalPages(pageResponse.totalPages || 0);
      setTotalElements(pageResponse.totalElements || 0);
    } catch (err: unknown) {
      console.error('Error loading emails:', err);
      const errorMessage = getErrorMessage(err);
      setError(errorMessage || 'Fehler beim Laden der E-Mails');
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

  /**
   * Handles the export action by triggering a file download.
   *
   * @author Philipp Borkovic
   */
  const handleExport = useCallback(async (): Promise<void> => {
    try {
      const url = `/api/export/emails?emailType=${encodeURIComponent(exportEmailType)}&format=${encodeURIComponent(exportFormat)}`;

      const link = document.createElement('a');
      link.href = url;
      link.download = `emails_${exportEmailType}_${new Date().toISOString().split('T')[0]}.${exportFormat}`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);

      exportModal.close();
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Fehler beim Exportieren der E-Mails';
      setError(errorMessage);
    }
  }, [exportEmailType, exportFormat, exportModal]);

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
        <div className="flex gap-3">
          <Button
            variant="outline"
            onClick={exportModal.open}
            className="text-white bg-black hover:bg-gray-800 border-black"
          >
            <FaDownload className="mr-2" style={{ width: '16px', height: '16px', color: 'white' }} />
            Exportieren
          </Button>
          <Button
            onClick={sendEmailModal.open}
            className="flex items-center gap-2 bg-black text-white hover:bg-gray-800"
          >
            <FaEnvelope style={{ width: '16px', height: '16px' }} />
            Neue E-Mail senden
          </Button>
        </div>
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
            <MdExpandMore
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

      {/* Export options modal */}
      <Dialog
        opened={exportModal.isOpen}
        onOpenedChanged={(e: DialogOpenedChangedEvent) => {
          if (!e.detail.value) exportModal.close();
        }}
        headerTitle="E-Mail-Export Optionen"
      >
        <div className="p-4 sm:p-6 min-w-[300px] sm:min-w-[500px] max-w-[95vw]">
          <div className="space-y-6">
            {/* Icon */}
            <div className="flex justify-center">
              <FaCloudDownloadAlt className="text-black" style={{ width: '64px', height: '64px' }} />
            </div>

            {/* Description */}
            <div className="text-center space-y-2">
              <p className="font-medium text-lg">E-Mails exportieren</p>
              <p className="text-sm text-muted-foreground">
                Wählen Sie die Art der E-Mails und das Exportformat aus.
              </p>
            </div>

            {/* Email Type Selector */}
            <div className="space-y-3">
              <label className="text-sm font-medium">E-Mail-Typ</label>
              <Select
                value={exportEmailType}
                onValueChange={(value) => setExportEmailType(value)}
              >
                <SelectTrigger className="w-full border-black text-black [&_svg]:text-black [&_svg]:opacity-100">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent className="bg-white border-black z-[9999]">
                  {EXPORT_EMAIL_TYPE_OPTIONS.map((option) => (
                    <SelectItem
                      key={option.value}
                      value={option.value}
                      className="text-black hover:bg-gray-100 hover:text-black focus:bg-gray-100 focus:text-black"
                    >
                      {option.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {/* Export Format Selector */}
            <div className="space-y-3">
              <label className="text-sm font-medium">Exportformat</label>
              <Select
                value={exportFormat}
                onValueChange={(value) => setExportFormat(value)}
              >
                <SelectTrigger className="w-full border-black text-black [&_svg]:text-black [&_svg]:opacity-100">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent className="bg-white border-black z-[9999]">
                  {EXPORT_FORMAT_OPTIONS.map((option) => {
                    const iconMap: Record<string, JSX.Element> = {
                      'xlsx': <FaTable style={{ width: '16px', height: '16px' }} />,
                      'pdf': <FaFileAlt style={{ width: '16px', height: '16px' }} />,
                      'xml': <FaFileCode style={{ width: '16px', height: '16px' }} />,
                      'json': <FaCode style={{ width: '16px', height: '16px' }} />
                    };
                    return (
                      <SelectItem
                        key={option.value}
                        value={option.value}
                        className="text-black hover:bg-gray-100 hover:text-black focus:bg-gray-100 focus:text-black"
                      >
                        <div className="flex items-center gap-2">
                          {iconMap[option.value]}
                          {option.label}
                        </div>
                      </SelectItem>
                    );
                  })}
                </SelectContent>
              </Select>
            </div>
          </div>

          {/* Action Buttons */}
          <div className="flex flex-col sm:flex-row justify-end gap-3 pt-6 border-t mt-6">
            <Button variant="destructive" onClick={exportModal.close} className="text-white w-full sm:w-auto">
              Abbrechen
            </Button>
            <Button
              variant="outline"
              onClick={handleExport}
              className="text-white bg-black hover:bg-gray-800 border-black w-full sm:w-auto"
            >
              <FaDownload className="mr-2" style={{ width: '16px', height: '16px', color: 'white' }} />
              Exportieren
            </Button>
          </div>
        </div>
      </Dialog>
    </div>
  );
}
