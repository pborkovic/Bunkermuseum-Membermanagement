import { useState, useCallback, useEffect } from 'react';
import { Dialog } from '@vaadin/react-components/Dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { toast } from 'sonner';
import type UserDTO from 'Frontend/generated/com/bunkermuseum/membermanagement/dto/UserDTO';
import { EmailController } from 'Frontend/generated/endpoints';
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
    } catch (error: any) {
      console.error('Error loading users:', error);
      toast.error('Fehler beim Laden der Benutzer');
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
    } catch (error: any) {
      console.error('Error sending email:', error);
      toast.error(error.message || 'Fehler beim Senden der E-Mail');
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
      onOpenedChanged={(e) => !e.detail.value && handleClose()}
      headerTitle="Neue E-Mail senden"
      className="email-modal-large"
    >
      <div className="space-y-4 py-4 w-[900px] max-w-[95vw]">
        {/* Recipient Type Selection */}
        <div className="space-y-2">
          <Label>Empfänger-Typ</Label>
          <div className="flex gap-4">
            <label className="flex items-center gap-2 cursor-pointer">
              <input
                type="radio"
                name="recipientType"
                value="user"
                checked={recipientType === 'user'}
                onChange={() => setRecipientType('user')}
                className="w-4 h-4 border-black text-black focus:ring-2 focus:ring-black"
              />
              <span className="text-sm">Benutzer auswählen</span>
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
              <span className="text-sm">Benutzerdefinierte E-Mail</span>
            </label>
          </div>
        </div>

        {/* User Selection Dropdown */}
        {recipientType === 'user' && (
          <div className="space-y-2">
            <Label htmlFor="user-select">Empfänger</Label>
            {isLoadingUsers ? (
              <p className="text-sm text-gray-500">Lade Benutzer...</p>
            ) : (
              <Select value={selectedUserId} onValueChange={setSelectedUserId}>
                <SelectTrigger className="w-full border-black focus:ring-black focus:border-black text-black bg-white">
                  <SelectValue placeholder="Benutzer auswählen..." className="text-black" />
                </SelectTrigger>
                <SelectContent 
                  className="bg-white border-black z-[9999] max-h-60 overflow-y-auto"
                  style={{ zIndex: 9999 }}
                >
                  {users.map((user) => (
                    <SelectItem
                      key={user.id}
                      value={user.id?.toString() || ''}
                      className="text-black hover:bg-gray-100 hover:text-black focus:bg-gray-100 focus:text-black"
                    >
                      {user.name} ({user.email})
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
            <Label htmlFor="custom-email">E-Mail-Adresse</Label>
            <Input
              id="custom-email"
              type="email"
              placeholder="beispiel@email.com"
              value={customEmail}
              onChange={(e) => setCustomEmail(e.target.value)}
              className="border-black focus:ring-black focus:border-black"
            />
          </div>
        )}

        {/* Subject Field */}
        <div className="space-y-2">
          <Label htmlFor="subject">Betreff</Label>
          <Input
            id="subject"
            type="text"
            placeholder="E-Mail Betreff"
            value={subject}
            onChange={(e) => setSubject(e.target.value)}
            className="border-black focus:ring-black focus:border-black"
          />
        </div>

        {/* Content Editor (React Quill) */}
        <div className="space-y-2">
          <Label>Inhalt</Label>
          <div className="border border-black rounded-md overflow-hidden">
            <ReactQuill
              theme="snow"
              value={content}
              onChange={setContent}
              modules={modules}
              formats={formats}
              placeholder="Verfassen Sie hier Ihre E-Mail..."
              style={{ minHeight: '200px' }}
            />
          </div>
        </div>

        {/* Footer Buttons */}
        <div className="flex justify-end gap-2 pt-4 border-t border-gray-200">
          <Button
            onClick={handleClose}
            disabled={isSending}
            className="bg-red-600 text-white hover:bg-red-700 disabled:bg-red-300"
          >
            Abbrechen
          </Button>
          <Button
            onClick={handleSendEmail}
            disabled={isSending}
            className="bg-black text-white hover:bg-gray-800 disabled:bg-gray-400"
          >
            {isSending ? 'Senden...' : 'E-Mail senden'}
          </Button>
        </div>
      </div>
    </Dialog>
  );
}
