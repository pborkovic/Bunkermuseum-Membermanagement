import { useState, useCallback, useEffect } from 'react';
import { Dialog } from '@vaadin/react-components/Dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { toast } from 'sonner';
import type UserDTO from 'Frontend/generated/com/bunkermuseum/membermanagement/dto/UserDTO';
import { EmailController } from 'Frontend/generated/endpoints';
import { getErrorMessage, DialogOpenedChangedEvent } from '../../../types/vaadin';
import ReactQuill from 'react-quill';
import 'react-quill/dist/quill.snow.css';

interface SendEmailModalProps {
  isOpen: boolean;
  onClose: () => void;
  onEmailSent: () => void;
}

/**
 * Modal for composing and sending emails to users or custom addresses.
 *
 * Features:
 * - Select existing user from dropdown OR enter custom email
 * - Subject line input
 * - Rich text editor (React Quill) for email content
 * - HTML email support
 * - Validation for all fields
 * - Toast notifications for success/error
 *
 * @author Philipp Borkovic
 */
export function SendEmailModal({ isOpen, onClose, onEmailSent }: SendEmailModalProps) {
  const [users, setUsers] = useState<UserDTO[]>([]);
  const [isLoadingUsers, setIsLoadingUsers] = useState(false);
  const [isSending, setIsSending] = useState(false);

  // Form state
  const [recipientType, setRecipientType] = useState<'user' | 'custom'>('user');
  const [selectedUserId, setSelectedUserId] = useState<string>('');
  const [customEmail, setCustomEmail] = useState('');
  const [subject, setSubject] = useState('');
  const [content, setContent] = useState('');

  // Load users when modal opens
  useEffect(() => {
    if (isOpen) {
      loadUsers();
    }
  }, [isOpen]);

  const loadUsers = useCallback(async () => {
    try {
      setIsLoadingUsers(true);
      const allUsers = await EmailController.getAllActiveUsers();
      setUsers((allUsers || []) as UserDTO[]);
    } catch (error: unknown) {
      console.error('Error loading users:', error);
      const errorMessage = getErrorMessage(error);
      toast.error(errorMessage || 'Fehler beim Laden der Benutzer');
    } finally {
      setIsLoadingUsers(false);
    }
  }, []);

  const handleSendEmail = useCallback(async () => {
    // Validation
    if (recipientType === 'user' && !selectedUserId) {
      toast.error('Bitte wählen Sie einen Empfänger aus');
      return;
    }

    if (recipientType === 'custom' && !customEmail) {
      toast.error('Bitte geben Sie eine E-Mail-Adresse ein');
      return;
    }

    if (recipientType === 'custom' && !isValidEmail(customEmail)) {
      toast.error('Bitte geben Sie eine gültige E-Mail-Adresse ein');
      return;
    }

    if (!subject.trim()) {
      toast.error('Bitte geben Sie einen Betreff ein');
      return;
    }

    if (!content.trim() || content.trim() === '<p><br></p>') {
      toast.error('Bitte geben Sie einen Inhalt ein');
      return;
    }

    try {
      setIsSending(true);

      await EmailController.sendEmail(
        recipientType === 'user' ? selectedUserId : undefined,
        recipientType === 'custom' ? customEmail : undefined,
        subject,
        content
      );

      toast.success('E-Mail erfolgreich gesendet');
      resetForm();
      onClose();
      onEmailSent();
    } catch (error: unknown) {
      console.error('Error sending email:', error);
      const errorMessage = getErrorMessage(error);
      toast.error(errorMessage || 'Fehler beim Senden der E-Mail');
    } finally {
      setIsSending(false);
    }
  }, [recipientType, selectedUserId, customEmail, subject, content, onClose, onEmailSent]);

  const resetForm = () => {
    setRecipientType('user');
    setSelectedUserId('');
    setCustomEmail('');
    setSubject('');
    setContent('');
  };

  const handleClose = () => {
    resetForm();
    onClose();
  };

  const isValidEmail = (email: string): boolean => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  };

  // React Quill modules configuration
  const modules = {
    toolbar: [
      [{ header: [1, 2, 3, false] }],
      ['bold', 'italic', 'underline', 'strike'],
      [{ list: 'ordered' }, { list: 'bullet' }],
      [{ color: [] }, { background: [] }],
      [{ align: [] }],
      ['link'],
      ['clean'],
    ],
  };

  const formats = [
    'header',
    'bold',
    'italic',
    'underline',
    'strike',
    'list',
    'bullet',
    'color',
    'background',
    'align',
    'link',
  ];

  return (
    <Dialog
      opened={isOpen}
      onOpenedChanged={(e: DialogOpenedChangedEvent) => !e.detail.value && handleClose()}
      headerTitle="Neue E-Mail senden"
      className="email-modal-large"
    >
      <div className="space-y-4 sm:space-y-6 py-4 sm:py-6 w-full sm:w-[600px] md:w-[800px] lg:w-[1000px] xl:w-[1200px] max-w-[95vw] px-2 sm:px-0">
        {/* Recipient Type Selection */}
        <div className="space-y-2">
          <Label className="text-sm sm:text-base">Empfänger-Typ</Label>
          <div className="flex flex-col sm:flex-row gap-3 sm:gap-4">
            <label className="flex items-center gap-2 cursor-pointer">
              <input
                type="radio"
                name="recipientType"
                value="user"
                checked={recipientType === 'user'}
                onChange={() => setRecipientType('user')}
                className="w-4 h-4 border-black text-black focus:ring-2 focus:ring-black"
              />
              <span className="text-xs sm:text-sm">Benutzer auswählen</span>
            </label>
            <label className="flex items-center gap-2 cursor-pointer">
              <input
                type="radio"
                name="recipientType"
                value="custom"
                checked={recipientType === 'custom'}
                onChange={() => setRecipientType('custom')}
                className="w-4 h-4 border-black text-black focus:ring-2 focus:ring-black"
              />
              <span className="text-xs sm:text-sm">Benutzerdefinierte E-Mail</span>
            </label>
          </div>
        </div>

        {/* User Selection Dropdown */}
        {recipientType === 'user' && (
          <div className="space-y-2">
            <Label htmlFor="user-select" className="text-sm sm:text-base">Empfänger</Label>
            {isLoadingUsers ? (
              <p className="text-xs sm:text-sm text-gray-500">Lade Benutzer...</p>
            ) : (
              <Select value={selectedUserId} onValueChange={setSelectedUserId}>
                <SelectTrigger className="w-full border-black focus:ring-black focus:border-black text-black bg-white text-sm sm:text-base">
                  <SelectValue placeholder="Benutzer auswählen..." className="text-black" />
                </SelectTrigger>
                <SelectContent
                  className="bg-white border-black z-[9999] max-h-48 sm:max-h-60 overflow-y-auto"
                  style={{ zIndex: 9999 }}
                >
                  {users.map((user) => (
                    <SelectItem
                      key={user.id}
                      value={user.id?.toString() || ''}
                      className="text-black hover:bg-gray-100 hover:text-black focus:bg-gray-100 focus:text-black text-xs sm:text-sm"
                    >
                      <span className="block truncate">{user.name} ({user.email})</span>
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            )}
          </div>
        )}

        {/* Custom Email Input */}
        {recipientType === 'custom' && (
          <div className="space-y-2">
            <Label htmlFor="custom-email" className="text-sm sm:text-base">E-Mail-Adresse</Label>
            <Input
              id="custom-email"
              type="email"
              placeholder="beispiel@email.com"
              value={customEmail}
              onChange={(e) => setCustomEmail(e.target.value)}
              className="border-black focus:ring-black focus:border-black text-sm sm:text-base"
            />
          </div>
        )}

        {/* Subject Field */}
        <div className="space-y-2">
          <Label htmlFor="subject" className="text-sm sm:text-base">Betreff</Label>
          <Input
            id="subject"
            type="text"
            placeholder="E-Mail Betreff"
            value={subject}
            onChange={(e) => setSubject(e.target.value)}
            className="border-black focus:ring-black focus:border-black text-sm sm:text-base"
          />
        </div>

        {/* Content Editor (React Quill) */}
        <div className="space-y-2">
          <Label className="text-sm sm:text-base">Inhalt</Label>
          <div className="border border-black rounded-md overflow-hidden">
            <ReactQuill
              theme="snow"
              value={content}
              onChange={setContent}
              modules={modules}
              formats={formats}
              placeholder="Verfassen Sie hier Ihre E-Mail..."
              style={{ minHeight: '150px' }}
              className="text-sm sm:text-base"
            />
          </div>
        </div>

        {/* Footer Buttons */}
        <div className="flex flex-col-reverse sm:flex-row justify-end gap-2 pt-4 border-t border-gray-200">
          <Button
            onClick={handleClose}
            disabled={isSending}
            className="bg-red-600 text-white hover:bg-red-700 disabled:bg-red-300 w-full sm:w-auto text-sm sm:text-base"
          >
            Abbrechen
          </Button>
          <Button
            onClick={handleSendEmail}
            disabled={isSending}
            className="bg-black text-white hover:bg-gray-800 disabled:bg-gray-400 w-full sm:w-auto text-sm sm:text-base"
          >
            {isSending ? 'Senden...' : 'E-Mail senden'}
          </Button>
        </div>
      </div>
    </Dialog>
  );
}
