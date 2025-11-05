import { memo } from 'react';
import type Email from 'Frontend/generated/com/bunkermuseum/membermanagement/model/Email';
import { formatDate, formatDateTime } from '../utils/formatting';
import { useWindowSize } from '../hooks/useWindowSize';

interface EmailsListProps {
  emails: Email[];
  isLoading: boolean;
}

/**
 * Displays a list of sent emails in either table or card format.
 *
 * Features:
 * - Responsive: Table on desktop, cards on mobile
 * - Shows from, to, subject, and sent date
 * - Truncates long subjects and content
 * - Loading state support
 *
 * @author Philipp Borkovic
 */
export const EmailsList = memo(({ emails, isLoading }: EmailsListProps) => {
  const { isMobile } = useWindowSize();

  if (isLoading) {
    return <div className="text-center py-8 text-gray-500">Laden...</div>;
  }

  if (emails.length === 0) {
    return (
      <div className="text-center py-8 text-gray-500">
        <p>Keine E-Mails gefunden.</p>
      </div>
    );
  }

  // Mobile card view
  if (isMobile) {
    return (
      <div className="space-y-4">
        {emails.map((email) => (
          <div key={email.id} className="border border-black p-4 bg-white">
            <div className="space-y-2">
              <div>
                <span className="text-xs text-gray-600">Von:</span>
                <p className="text-sm font-medium">{email.fromAddress}</p>
              </div>
              <div>
                <span className="text-xs text-gray-600">An:</span>
                <p className="text-sm font-medium">{email.toAddress}</p>
              </div>
              <div>
                <span className="text-xs text-gray-600">Betreff:</span>
                <p className="text-sm font-medium">
                  {(email.subject || '').length > 40
                    ? `${(email.subject || '').substring(0, 40)}...`
                    : (email.subject || '')}
                </p>
              </div>
              <div>
                <span className="text-xs text-gray-600">Inhalt:</span>
                <p className="text-sm text-gray-700 line-clamp-2">
                  {(email.content || '').replace(/<[^>]*>/g, '').substring(0, 100)}
                  {(email.content || '').length > 100 ? '...' : ''}
                </p>
              </div>
              <div>
                <span className="text-xs text-gray-600">Gesendet:</span>
                <p className="text-sm">{formatDateTime(email.createdAt)}</p>
              </div>
              {email.user && (
                <div className="pt-2 border-t border-gray-200">
                  <span className="text-xs text-gray-600">Gesendet von:</span>
                  <p className="text-sm font-medium">{email.user.name}</p>
                </div>
              )}
              {!email.user && (
                <div className="pt-2 border-t border-gray-200">
                  <span className="inline-block px-2 py-1 text-xs bg-gray-100 border border-gray-300">
                    System-E-Mail
                  </span>
                </div>
              )}
            </div>
          </div>
        ))}
      </div>
    );
  }

  // Desktop table view
  return (
    <div className="overflow-x-auto border border-black">
      <table className="w-full">
        <thead>
          <tr className="bg-gray-50 border-b border-black">
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-700 uppercase tracking-wider">
              Von
            </th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-700 uppercase tracking-wider">
              An
            </th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-700 uppercase tracking-wider">
              Betreff
            </th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-700 uppercase tracking-wider">
              Inhalt
            </th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-700 uppercase tracking-wider">
              Gesendet von
            </th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-700 uppercase tracking-wider">
              Gesendet
            </th>
          </tr>
        </thead>
        <tbody className="bg-white divide-y divide-gray-200">
          {emails.map((email) => (
            <tr key={email.id} className="hover:bg-gray-50">
              <td className="px-4 py-3 text-sm text-gray-900">
                {email.fromAddress}
              </td>
              <td className="px-4 py-3 text-sm text-gray-900">
                {email.toAddress}
              </td>
              <td className="px-4 py-3 text-sm text-gray-900">
                <div className="max-w-xs truncate" title={email.subject || ''}>
                  {email.subject || ''}
                </div>
              </td>
              <td className="px-4 py-3 text-sm text-gray-700">
                <div className="max-w-md truncate" title={(email.content || '').replace(/<[^>]*>/g, '')}>
                  {(email.content || '').replace(/<[^>]*>/g, '').substring(0, 80)}
                  {(email.content || '').length > 80 ? '...' : ''}
                </div>
              </td>
              <td className="px-4 py-3 text-sm">
                {email.user ? (
                  <div>
                    <p className="font-medium text-gray-900">{email.user.name}</p>
                    <p className="text-xs text-gray-500">{email.user.email}</p>
                  </div>
                ) : (
                  <span className="inline-block px-2 py-1 text-xs bg-gray-100 text-gray-700 border border-gray-300">
                    System
                  </span>
                )}
              </td>
              <td className="px-4 py-3 text-sm text-gray-500">
                {formatDateTime(email.createdAt)}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
});

EmailsList.displayName = 'EmailsList';
