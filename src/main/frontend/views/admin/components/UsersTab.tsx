import { useState, useEffect, useCallback } from 'react';
import { Dialog } from '@vaadin/react-components/Dialog';
import { Button } from '@/components/ui/button';
import { Icon } from '@vaadin/react-components';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { DatePicker } from '@/components/ui/date-picker';
import { UserController } from 'Frontend/generated/endpoints';
import type User from 'Frontend/generated/com/bunkermuseum/membermanagement/model/User';
import UsersList from './_UsersList';
import { useWindowSize } from '../hooks/useWindowSize';
import { useModalWithData, useModal } from '../hooks/useModal';
import { formatDate } from '../utils/formatting';
import { ANREDE_OPTIONS, USER_STATUS_OPTIONS, PAGE_SIZE_OPTIONS, DEFAULT_PAGE_SIZE, EXPORT_USER_TYPE_OPTIONS, EXPORT_FORMAT_OPTIONS } from '../utils/constants';
import type { ProfileFormData } from '../types';

/**
 * UsersTab component - Displays all users in a grid with detailed modal view.
 *
 * Features:
 * - Grid view of all users
 * - Click to open detailed user information modal
 * - Loading and error states
 * - Responsive layout
 *
 * @component
 *
 * @returns {JSX.Element} The users tab content
 *
 * @author Philipp Borkovic
 */
export default function UsersTab(): JSX.Element {
  const { isMobile } = useWindowSize();
  const detailsModal = useModalWithData<User>();
  const deleteModal = useModalWithData<User>();
  const editModal = useModalWithData<User>();
  const exportModal = useModal();
  const [users, setUsers] = useState<User[]>([]);
  const [editForm, setEditForm] = useState<ProfileFormData>({
    name: '',
    email: '',
    salutation: '',
    academicTitle: '',
    rank: '',
    birthday: undefined,
    phone: '',
    street: '',
    city: '',
    postalCode: '',
    country: ''
  });
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [usersPerPage, setUsersPerPage] = useState(DEFAULT_PAGE_SIZE);
  const [statusFilter, setStatusFilter] = useState('active');
  const [exportUserType, setExportUserType] = useState('all');
  const [exportFormat, setExportFormat] = useState('xlsx');

  /**
   * Loads users from the backend with pagination.
   */
  useEffect(() => {
    loadUsers();
  }, [currentPage, searchQuery, usersPerPage, statusFilter]);

  /**
   * Fetches paginated users from the UserController.
   *
   * @author Philipp Borkovic
   */
  const loadUsers = useCallback(async (): Promise<void> => {
    try {
      setIsLoading(true);
      setError('');
      const pageResponse = await UserController.getUsersPage(
        currentPage - 1,
        usersPerPage,
        searchQuery || undefined,
        statusFilter
      );

      if (pageResponse) {
        setUsers((pageResponse.content || []).filter((user): user is User => user !== undefined && user !== null));
        setTotalPages(pageResponse.totalPages || 0);
        setTotalElements(pageResponse.totalElements || 0);
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Fehler beim Laden der Benutzer';
      setError(errorMessage);
    } finally {
      setIsLoading(false);
    }
  }, [currentPage, usersPerPage, searchQuery, statusFilter]);

  /**
   * Opens the user details modal for a specific user.
   *
   * @param {User} user - The user to display
   *
   * @author Philipp Borkovic
   */
  const handleUserClick = useCallback((user: User): void => {
    detailsModal.openWith(user);
  }, [detailsModal]);

  /**
   * Opens the delete confirmation modal for a specific user.
   *
   * @param {User} user - The user to delete
   *
   * @author Philipp Borkovic
   */
  const handleDeleteClick = useCallback((user: User): void => {
    deleteModal.openWith(user);
  }, [deleteModal]);

  /**
   * Deletes the selected user account.
   *
   * @author Philipp Borkovic
   */
  const handleConfirmDelete = useCallback(async (): Promise<void> => {
    if (!deleteModal.data) return;

    try {
      console.log('Deleting user:', deleteModal.data.id);
      deleteModal.close();
      await loadUsers();
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Fehler beim Löschen des Benutzers';
      setError(errorMessage);
    }
  }, [deleteModal, loadUsers]);

  /**
   * Opens the edit modal for a specific user.
   *
   * @param {User} user - The user to edit
   *
   * @author Philipp Borkovic
   */
  const handleEditClick = useCallback((user: User): void => {
    setEditForm({
      name: user.name || '',
      email: user.email || '',
      salutation: user.salutation || '',
      academicTitle: user.academicTitle || '',
      rank: user.rank || '',
      birthday: user.birthday ? new Date(user.birthday) : undefined,
      phone: user.phone || '',
      street: user.street || '',
      city: user.city || '',
      postalCode: user.postalCode || '',
      country: user.country || ''
    });
    editModal.openWith(user);
    detailsModal.close();
  }, [editModal, detailsModal]);

  /**
   * Saves the edited user information.
   *
   * @author Philipp Borkovic
   */
  const handleSaveEdit = useCallback(async (): Promise<void> => {
    if (!editModal.data) return;

    try {
      const updatedUser: User = {
        ...editModal.data,
        name: editForm.name,
        email: editForm.email,
        salutation: editForm.salutation || undefined,
        academicTitle: editForm.academicTitle || undefined,
        rank: editForm.rank || undefined,
        birthday: editForm.birthday ? editForm.birthday.toISOString().split('T')[0] : undefined,
        phone: editForm.phone || undefined,
        street: editForm.street || undefined,
        city: editForm.city || undefined,
        postalCode: editForm.postalCode || undefined,
        country: editForm.country || undefined
      };

      await UserController.updateUser(editModal.data.id!, updatedUser);
      editModal.close();
      await loadUsers();
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Fehler beim Aktualisieren des Benutzers';
      setError(errorMessage);
    }
  }, [editModal, editForm, loadUsers]);

  /**
   * Handles page navigation with bounds checking.
   *
   * @param {number} page - The page number to navigate to
   *
   * @author Philipp Borkovic
   */
  const handlePageChange = (page: number): void => {
    setCurrentPage(Math.min(Math.max(1, page), totalPages));
  };

  /**
   * Handles the export action.
   * TODO: Implement backend export service call
   *
   * @author Philipp Borkovic
   */
  const handleExport = useCallback(async (): Promise<void> => {
    try {
      console.log('Exporting users with type:', exportUserType, 'format:', exportFormat);
      // TODO: Call ExportController.exportUsers(exportUserType, exportFormat) when backend is ready
      exportModal.close();
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Fehler beim Exportieren der Benutzer';
      setError(errorMessage);
    }
  }, [exportUserType, exportFormat, exportModal]);

  return (
    <div className="flex flex-col h-full space-y-4">
      {/* Header with Search and Controls */}
      <div className="flex-shrink-0 flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4">
        <div className="flex flex-col sm:flex-row sm:items-center gap-4 flex-1">
          <div>
            <h2 className="text-2xl font-bold text-black">Mitgliederverwaltung</h2>
            <p className="text-sm text-gray-600 mt-1">
              Übersicht aller registrierten Mitglieder
            </p>
          </div>

          {/* Export Button */}
          <Button
            variant="outline"
            onClick={exportModal.open}
            className="text-white bg-black hover:bg-gray-800 border-black self-start sm:self-auto sm:mt-7"
          >
            <Icon icon="vaadin:download" className="mr-2" style={{ width: '16px', height: '16px', color: 'white' }} />
            Exportieren
          </Button>
        </div>

        {/* Search Bar and Controls */}
        <div className="flex flex-col sm:flex-row gap-3 sm:ml-auto">
          {/* Status Filter */}
          <div className="flex items-center gap-2">
            <label className="text-sm text-gray-600 whitespace-nowrap">Status:</label>
            <Select
              value={statusFilter}
              onValueChange={(value) => {
                setStatusFilter(value);
                setCurrentPage(1); // Reset to first page when changing filter
              }}
            >
              <SelectTrigger className="w-[180px] h-9 border-black text-black [&_svg]:text-black [&_svg]:opacity-100 [&_svg]:-mt-4">
                <SelectValue />
              </SelectTrigger>
              <SelectContent className="bg-white border-black">
                {USER_STATUS_OPTIONS.map((option) => (
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

          {/* Page Size Selector */}
          <div className="flex items-center gap-2">
            <label className="text-sm text-gray-600 whitespace-nowrap">Zeilen:</label>
            <Select
              value={usersPerPage.toString()}
              onValueChange={(value) => {
                setUsersPerPage(parseInt(value));
                setCurrentPage(1); // Reset to first page when changing size
              }}
            >
              <SelectTrigger className="w-[90px] h-9 border-black text-black [&_svg]:text-black [&_svg]:opacity-100 [&_svg]:-mt-4">
                <SelectValue />
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

          {/* Search Bar */}
          <div className="relative w-full sm:w-48">
            <Icon
              icon="vaadin:search"
              className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400"
              style={{ width: '18px', height: '18px' }}
            />
            <input
              type="text"
              placeholder="Suchen..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-10 pr-4 py-2 text-sm text-black border border-black rounded-md focus:outline-none focus:ring-2 focus:ring-black focus:ring-offset-1 placeholder:text-gray-400"
            />
          </div>
        </div>
      </div>

      {/* Error message */}
      {error && (
        <div className="rounded-md bg-destructive/10 p-3 text-sm text-destructive flex-shrink-0">
          {error}
        </div>
      )}

      {/* Users List with Pagination */}
      <div className="bg-white rounded-lg p-4 sm:p-6 w-full flex-shrink-0">
        <UsersList
          users={users}
          isLoading={isLoading}
          searchQuery={searchQuery}
          currentPage={currentPage}
          totalPages={totalPages}
          totalElements={totalElements}
          usersPerPage={usersPerPage}
          isMobile={isMobile}
          onUserClick={handleUserClick}
          onDeleteClick={handleDeleteClick}
          onPageChange={handlePageChange}
        />
      </div>

      {/* User details modal */}
      <Dialog
        opened={detailsModal.isOpen}
        onOpenedChanged={(e: any) => {
          if (!e.detail.value) detailsModal.close();
        }}
        headerTitle="Benutzerdetails"
      >
        {detailsModal.data && (
          <div className="p-4 sm:p-6 min-w-[300px] sm:min-w-[600px] lg:min-w-[800px] max-w-[95vw]">
            {/* Header Section */}
            <div className="flex flex-col sm:flex-row items-start gap-4 sm:gap-6 pb-6 border-b">
              <div className="flex-shrink-0 bg-muted rounded-full p-4">
                <Icon icon="vaadin:user-card" className="text-foreground" style={{ width: '64px', height: '64px' }} />
              </div>
              <div className="flex-1">
                <h3 className="text-2xl font-semibold mb-1">{detailsModal.data.name}</h3>
                <p className="text-muted-foreground">{detailsModal.data.email}</p>
              </div>
            </div>

            {/* Details Grid */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-4 sm:gap-x-8 gap-y-4 py-6">
              {/* Email Verification Status */}
              <div className="space-y-1">
                <label className="text-sm font-medium text-muted-foreground">E-Mail Status</label>
                <div className="flex items-center gap-2">
                  {detailsModal.data.emailVerifiedAt ? (
                    <>
                      <Icon icon="vaadin:check-circle" className="text-success" style={{ width: '20px', height: '20px' }} />
                      <div>
                        <div className="text-sm font-medium">Verifiziert</div>
                        <div className="text-xs text-muted-foreground">{formatDate(detailsModal.data.emailVerifiedAt)}</div>
                      </div>
                    </>
                  ) : (
                    <>
                      <Icon icon="vaadin:close-circle" className="text-destructive" style={{ width: '20px', height: '20px' }} />
                      <div className="text-sm font-medium">Nicht verifiziert</div>
                    </>
                  )}
                </div>
              </div>

              {/* Phone */}
              {detailsModal.data.phone && (
                <div className="space-y-1">
                  <label className="text-sm font-medium text-muted-foreground">Telefon</label>
                  <div className="flex items-center gap-2">
                    <Icon icon="vaadin:phone" className="text-foreground" style={{ width: '20px', height: '20px' }} />
                    <div className="text-sm font-medium">{detailsModal.data.phone}</div>
                  </div>
                </div>
              )}

              {/* Birthday */}
              {detailsModal.data.birthday && (
                <div className="space-y-1">
                  <label className="text-sm font-medium text-muted-foreground">Geburtsdatum</label>
                  <div className="flex items-center gap-2">
                    <Icon icon="vaadin:cake" className="text-foreground" style={{ width: '20px', height: '20px' }} />
                    <div className="text-sm font-medium">{formatDate(detailsModal.data.birthday)}</div>
                  </div>
                </div>
              )}

              {/* Address */}
              {(detailsModal.data.street || detailsModal.data.city || detailsModal.data.postalCode || detailsModal.data.country) && (
                <div className="space-y-1 sm:col-span-2">
                  <label className="text-sm font-medium text-muted-foreground">Adresse</label>
                  <div className="flex items-start gap-2">
                    <Icon icon="vaadin:home" className="text-foreground mt-0.5" style={{ width: '20px', height: '20px' }} />
                    <div className="text-sm font-medium">
                      {detailsModal.data.street && <div>{detailsModal.data.street}</div>}
                      {(detailsModal.data.postalCode || detailsModal.data.city) && (
                        <div>{detailsModal.data.postalCode} {detailsModal.data.city}</div>
                      )}
                      {detailsModal.data.country && <div>{detailsModal.data.country}</div>}
                    </div>
                  </div>
                </div>
              )}

              {/* Salutation */}
              {detailsModal.data.salutation && (
                <div className="space-y-1">
                  <label className="text-sm font-medium text-muted-foreground">Anrede</label>
                  <div className="flex items-center gap-2">
                    <Icon icon="vaadin:user" className="text-foreground" style={{ width: '20px', height: '20px' }} />
                    <div className="text-sm font-medium">{detailsModal.data.salutation}</div>
                  </div>
                </div>
              )}

              {/* Academic Title */}
              {detailsModal.data.academicTitle && (
                <div className="space-y-1">
                  <label className="text-sm font-medium text-muted-foreground">Titel</label>
                  <div className="flex items-center gap-2">
                    <Icon icon="vaadin:academy-cap" className="text-foreground" style={{ width: '20px', height: '20px' }} />
                    <div className="text-sm font-medium">{detailsModal.data.academicTitle}</div>
                  </div>
                </div>
              )}

              {/* Rank */}
              {detailsModal.data.rank && (
                <div className="space-y-1">
                  <label className="text-sm font-medium text-muted-foreground">Dienstgrad</label>
                  <div className="flex items-center gap-2">
                    <Icon icon="vaadin:medal" className="text-foreground" style={{ width: '20px', height: '20px' }} />
                    <div className="text-sm font-medium">{detailsModal.data.rank}</div>
                  </div>
                </div>
              )}
            </div>

            {/* Action Buttons */}
            <div className="flex flex-col sm:flex-row justify-end gap-3 pt-6 border-t mt-6">
              <Button variant="destructive" onClick={detailsModal.close} className="text-white w-full sm:w-auto">
                <Icon icon="vaadin:close" className="mr-2" style={{ width: '16px', height: '16px', color: 'white' }} />
                Schließen
              </Button>
              <Button variant="outline" onClick={() => handleEditClick(detailsModal.data!)} className="text-white bg-black hover:bg-gray-800 w-full sm:w-auto">
                <Icon icon="vaadin:edit" className="mr-2" style={{ width: '16px', height: '16px', color: 'white' }} />
                Bearbeiten
              </Button>
            </div>
          </div>
        )}
      </Dialog>

      {/* Delete confirmation modal */}
      <Dialog
        opened={deleteModal.isOpen}
        onOpenedChanged={(e: any) => {
          if (!e.detail.value) deleteModal.close();
        }}
        headerTitle="Benutzer löschen"
      >
        {deleteModal.data && (
          <div className="space-y-4 p-4 min-w-[400px]">
            <div className="flex justify-center">
              <Icon icon="vaadin:warning" className="text-destructive" style={{ width: '64px', height: '64px' }} />
            </div>

            <div className="text-center space-y-2">
              <p className="font-medium">Sind Sie sicher, dass Sie diesen Benutzer löschen möchten?</p>
              <p className="text-sm text-muted-foreground">
                Benutzer: <span className="font-semibold">{deleteModal.data.name}</span>
              </p>
              <p className="text-sm text-muted-foreground">
                E-Mail: <span className="font-semibold">{deleteModal.data.email}</span>
              </p>
              <p className="text-sm text-destructive mt-4">
                Diese Aktion kann nicht rückgängig gemacht werden.
              </p>
            </div>

            <div className="flex justify-end gap-2 pt-4">
              <Button variant="outline" onClick={deleteModal.close}>
                Abbrechen
              </Button>
              <Button variant="destructive" onClick={handleConfirmDelete}>
                Löschen
              </Button>
            </div>
          </div>
        )}
      </Dialog>

      {/* Edit user modal */}
      <Dialog
        opened={editModal.isOpen}
        onOpenedChanged={(e: any) => {
          if (!e.detail.value) editModal.close();
        }}
        headerTitle="Benutzer bearbeiten"
      >
        {editModal.data && (
          <div className="p-4 sm:p-6 min-w-[300px] sm:min-w-[600px] lg:min-w-[700px] max-w-[95vw] max-h-[90vh] overflow-y-auto">
            <div className="space-y-6">
              {/* Basic Information */}
              <div>
                <h3 className="text-sm font-semibold mb-3 text-muted-foreground">Grundinformationen</h3>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <label className="text-sm font-medium">Name *</label>
                    <input
                      type="text"
                      className="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-offset-2"
                      value={editForm.name}
                      onChange={(e) => setEditForm({ ...editForm, name: e.target.value })}
                      required
                    />
                  </div>

                  <div className="space-y-2">
                    <label className="text-sm font-medium">E-Mail *</label>
                    <input
                      type="email"
                      className="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-offset-2"
                      value={editForm.email}
                      onChange={(e) => setEditForm({ ...editForm, email: e.target.value })}
                      required
                    />
                  </div>
                </div>
              </div>

              {/* Personal Information */}
              <div>
                <h3 className="text-sm font-semibold mb-3 text-muted-foreground">Persönliche Daten</h3>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <label className="text-sm font-medium">Anrede</label>
                    <Select
                      value={editForm.salutation}
                      onValueChange={(value) => setEditForm({ ...editForm, salutation: value })}
                    >
                      <SelectTrigger className="w-full">
                        <SelectValue placeholder="Wählen" />
                      </SelectTrigger>
                      <SelectContent>
                        {ANREDE_OPTIONS.map(option => (
                          <SelectItem key={option.value} value={option.value}>
                            {option.label}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  <div className="space-y-2">
                    <label className="text-sm font-medium">Akademischer Titel</label>
                    <input
                      type="text"
                      className="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-offset-2"
                      value={editForm.academicTitle}
                      onChange={(e) => setEditForm({ ...editForm, academicTitle: e.target.value })}
                      placeholder="z.B. Dr., Prof."
                    />
                  </div>

                  <div className="space-y-2">
                    <label className="text-sm font-medium">Dienstgrad</label>
                    <input
                      type="text"
                      className="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-offset-2"
                      value={editForm.rank}
                      onChange={(e) => setEditForm({ ...editForm, rank: e.target.value })}
                    />
                  </div>

                  <div className="space-y-2">
                    <label className="text-sm font-medium">Geburtsdatum</label>
                    <DatePicker
                      value={editForm.birthday}
                      onChange={(date) => setEditForm({ ...editForm, birthday: date })}
                    />
                  </div>
                </div>
              </div>

              {/* Contact Information */}
              <div>
                <h3 className="text-sm font-semibold mb-3 text-muted-foreground">Kontaktdaten</h3>
                <div className="space-y-4">
                  <div className="space-y-2">
                    <label className="text-sm font-medium">Telefon</label>
                    <input
                      type="tel"
                      className="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-offset-2"
                      value={editForm.phone}
                      onChange={(e) => setEditForm({ ...editForm, phone: e.target.value })}
                    />
                  </div>
                </div>
              </div>

              {/* Address Information */}
              <div>
                <h3 className="text-sm font-semibold mb-3 text-muted-foreground">Adresse</h3>
                <div className="space-y-4">
                  <div className="space-y-2">
                    <label className="text-sm font-medium">Straße & Hausnummer</label>
                    <input
                      type="text"
                      className="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-offset-2"
                      value={editForm.street}
                      onChange={(e) => setEditForm({ ...editForm, street: e.target.value })}
                    />
                  </div>

                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <label className="text-sm font-medium">Postleitzahl</label>
                      <input
                        type="text"
                        className="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-offset-2"
                        value={editForm.postalCode}
                        onChange={(e) => setEditForm({ ...editForm, postalCode: e.target.value })}
                      />
                    </div>

                    <div className="space-y-2">
                      <label className="text-sm font-medium">Stadt</label>
                      <input
                        type="text"
                        className="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-offset-2"
                        value={editForm.city}
                        onChange={(e) => setEditForm({ ...editForm, city: e.target.value })}
                      />
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div className="flex flex-col sm:flex-row justify-end gap-3 pt-6 border-t mt-6">
              <Button variant="destructive" onClick={editModal.close} className="text-white w-full sm:w-auto">
                <Icon icon="vaadin:close" className="mr-2" style={{ width: '16px', height: '16px', color: 'white' }} />
                Abbrechen
              </Button>
              <Button variant="outline" onClick={handleSaveEdit} className="text-white bg-black hover:bg-gray-800 w-full sm:w-auto">
                <Icon icon="vaadin:check" className="mr-2" style={{ width: '16px', height: '16px', color: 'white' }} />
                Speichern
              </Button>
            </div>
          </div>
        )}
      </Dialog>

      {/* Export options modal */}
      <Dialog
        opened={exportModal.isOpen}
        onOpenedChanged={(e: any) => {
          if (!e.detail.value) exportModal.close();
        }}
        headerTitle="Benutzerexport Optionen"
      >
        <div className="p-4 sm:p-6 min-w-[300px] sm:min-w-[500px] max-w-[95vw]">
          <div className="space-y-6">
            {/* Icon */}
            <div className="flex justify-center">
              <Icon icon="vaadin:download-alt" className="text-black" style={{ width: '64px', height: '64px' }} />
            </div>

            {/* Description */}
            <div className="text-center space-y-2">
              <p className="font-medium text-lg">Mitglieder exportieren</p>
              <p className="text-sm text-muted-foreground">
                Wählen Sie die Art der Mitglieder aus, die Sie exportieren möchten.
              </p>
            </div>

            {/* Export Type Selector */}
            <div className="space-y-3">
              <label className="text-sm font-medium">Mitgliedertyp</label>
              <Select
                value={exportUserType}
                onValueChange={(value) => setExportUserType(value)}
              >
                <SelectTrigger className="w-full border-black text-black [&_svg]:text-black [&_svg]:opacity-100">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent className="bg-white border-black z-[9999]">
                  {EXPORT_USER_TYPE_OPTIONS.map((option) => (
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
                    const iconMap: Record<string, string> = {
                      'xlsx': 'vaadin:file-table',
                      'pdf': 'vaadin:file-text',
                      'xml': 'vaadin:file-code',
                      'json': 'vaadin:curly-brackets'
                    };
                    return (
                      <SelectItem
                        key={option.value}
                        value={option.value}
                        className="text-black hover:bg-gray-100 hover:text-black focus:bg-gray-100 focus:text-black"
                      >
                        <div className="flex items-center gap-2">
                          <Icon icon={iconMap[option.value]} style={{ width: '16px', height: '16px' }} />
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
              <Icon icon="vaadin:download" className="mr-2" style={{ width: '16px', height: '16px', color: 'white' }} />
              Exportieren
            </Button>
          </div>
        </div>
      </Dialog>
    </div>
  );
}
