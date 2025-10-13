import { useState, useEffect } from 'react';
import { Dialog } from '@vaadin/react-components/Dialog';
import { Button } from '@/components/ui/button';
import { Icon } from '@vaadin/react-components';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { DatePicker } from '@/components/ui/date-picker';
import { UserController } from 'Frontend/generated/endpoints';
import type User from 'Frontend/generated/com/bunkermuseum/membermanagement/model/User';
import UsersList from './_UsersList';

/**
 * Gender options for the Anrede (salutation) field.
 *
 * @author Philipp Borkovic
 */
const ANREDE_OPTIONS = [
  { value: 'männlich', label: 'Männlich' },
  { value: 'weiblich', label: 'Weiblich' },
  { value: 'divers', label: 'Divers' },
] as const;

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
  const [users, setUsers] = useState<User[]>([]);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [userToDelete, setUserToDelete] = useState<User | null>(null);
  const [userToEdit, setUserToEdit] = useState<User | null>(null);
  const [editForm, setEditForm] = useState({
    name: '',
    email: '',
    salutation: '',
    academicTitle: '',
    rank: '',
    birthday: undefined as Date | undefined,
    phone: '',
    street: '',
    city: '',
    postalCode: ''
  });
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [isMobile, setIsMobile] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [usersPerPage, setUsersPerPage] = useState(10);

  // Detect mobile screen size
  useEffect(() => {
    const checkMobile = () => {
      setIsMobile(window.innerWidth < 768);
    };

    checkMobile();
    window.addEventListener('resize', checkMobile);
    return () => window.removeEventListener('resize', checkMobile);
  }, []);

  /**
   * Loads users from the backend with pagination.
   */
  useEffect(() => {
    loadUsers();
  }, [currentPage, searchQuery, usersPerPage]);

  /**
   * Fetches paginated users from the UserController.
   *
   * @author Philipp Borkovic
   */
  const loadUsers = async (): Promise<void> => {
    try {
      setIsLoading(true);
      setError('');
      // Server uses 0-indexed pages, UI uses 1-indexed
      const pageResponse = await (UserController as any).getUsersPage(
        currentPage - 1,
        usersPerPage,
        searchQuery || null
      );
      setUsers((pageResponse.content || []).filter((user: any): user is User => user !== undefined && user !== null));
      setTotalPages(pageResponse.totalPages || 0);
      setTotalElements(pageResponse.totalElements || 0);
    } catch (err: any) {
      setError(err.message || 'Fehler beim Laden der Benutzer');
    } finally {
      setIsLoading(false);
    }
  };

  /**
   * Opens the user details modal for a specific user.
   *
   * @param {User} user - The user to display
   *
   * @author Philipp Borkovic
   */
  const handleUserClick = (user: User): void => {
    setSelectedUser(user);
    setIsModalOpen(true);
  };

  /**
   * Closes the user details modal.
   *
   * @author Philipp Borkovic
   */
  const handleCloseModal = (): void => {
    setIsModalOpen(false);
    setSelectedUser(null);
  };

  /**
   * Opens the delete confirmation modal for a specific user.
   *
   * @param {User} user - The user to delete
   *
   * @author Philipp Borkovic
   */
  const handleDeleteClick = (user: User): void => {
    setUserToDelete(user);
    setIsDeleteModalOpen(true);
  };

  /**
   * Closes the delete confirmation modal.
   *
   * @author Philipp Borkovic
   */
  const handleCloseDeleteModal = (): void => {
    setIsDeleteModalOpen(false);
    setUserToDelete(null);
  };

  /**
   * Deletes the selected user account.
   *
   * @author Philipp Borkovic
   */
  const handleConfirmDelete = async (): Promise<void> => {
    if (!userToDelete) return;

    try {
      // TODO: Implement user deletion via UserController
      console.log('Deleting user:', userToDelete.id);
      handleCloseDeleteModal();
      loadUsers(); // Reload users after deletion
    } catch (err: any) {
      setError(err.message || 'Fehler beim Löschen des Benutzers');
    }
  };

  /**
   * Opens the edit modal for a specific user.
   *
   * @param {User} user - The user to edit
   *
   * @author Philipp Borkovic
   */
  const handleEditClick = (user: User): void => {
    setUserToEdit(user);
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
      postalCode: user.postalCode || ''
    });
    setIsEditModalOpen(true);
    setIsModalOpen(false); // Close details modal
  };

  /**
   * Closes the edit modal.
   *
   * @author Philipp Borkovic
   */
  const handleCloseEditModal = (): void => {
    setIsEditModalOpen(false);
    setUserToEdit(null);
    setEditForm({
      name: '',
      email: '',
      salutation: '',
      academicTitle: '',
      rank: '',
      birthday: undefined,
      phone: '',
      street: '',
      city: '',
      postalCode: ''
    });
  };

  /**
   * Saves the edited user information.
   *
   * @author Philipp Borkovic
   */
  const handleSaveEdit = async (): Promise<void> => {
    if (!userToEdit) return;

    try {
      const updatedUser: User = {
        ...userToEdit,
        name: editForm.name,
        email: editForm.email,
        salutation: editForm.salutation || undefined,
        academicTitle: editForm.academicTitle || undefined,
        rank: editForm.rank || undefined,
        birthday: editForm.birthday ? editForm.birthday.toISOString().split('T')[0] : undefined,
        phone: editForm.phone || undefined,
        street: editForm.street || undefined,
        city: editForm.city || undefined,
        postalCode: editForm.postalCode || undefined
      };

      await UserController.updateUser(userToEdit.id!, updatedUser);
      handleCloseEditModal();
      loadUsers(); // Reload users after update
    } catch (err: any) {
      setError(err.message || 'Fehler beim Aktualisieren des Benutzers');
    }
  };

  /**
   * Formats a date to German locale string.
   *
   * @param {string | null | undefined} dateString - The date string to format
   * @returns {string} Formatted date or 'N/A'
   *
   * @author Philipp Borkovic
   */
  const formatDate = (dateString: string | null | undefined): string => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString('de-DE');
  };

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

  return (
    <div className="flex flex-col h-full space-y-4">
      {/* Header with Search and Controls */}
      <div className="flex-shrink-0 flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-black">Mitgliederverwaltung</h2>
          <p className="text-sm text-gray-600 mt-1">
            Übersicht aller registrierten Mitglieder
          </p>
        </div>

        {/* Search Bar and Page Size Controls */}
        <div className="flex flex-col sm:flex-row gap-3 sm:ml-auto">
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
                <SelectItem value="5" className="text-black hover:bg-gray-100 focus:bg-gray-100">5</SelectItem>
                <SelectItem value="10" className="text-black hover:bg-gray-100 focus:bg-gray-100">10</SelectItem>
                <SelectItem value="25" className="text-black hover:bg-gray-100 focus:bg-gray-100">25</SelectItem>
                <SelectItem value="50" className="text-black hover:bg-gray-100 focus:bg-gray-100">50</SelectItem>
                <SelectItem value="100" className="text-black hover:bg-gray-100 focus:bg-gray-100">100</SelectItem>
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
        opened={isModalOpen}
        onOpenedChanged={(e: any) => {
          if (!e.detail.value) handleCloseModal();
        }}
        headerTitle="Benutzerdetails"
      >
        {selectedUser && (
          <div className="p-4 sm:p-6 min-w-[300px] sm:min-w-[600px] lg:min-w-[800px] max-w-[95vw]">
            {/* Header Section */}
            <div className="flex flex-col sm:flex-row items-start gap-4 sm:gap-6 pb-6 border-b">
              <div className="flex-shrink-0 bg-muted rounded-full p-4">
                <Icon icon="vaadin:user-card" className="text-foreground" style={{ width: '64px', height: '64px' }} />
              </div>
              <div className="flex-1">
                <h3 className="text-2xl font-semibold mb-1">{selectedUser.name}</h3>
                <p className="text-muted-foreground">{selectedUser.email}</p>
              </div>
            </div>

            {/* Details Grid */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-4 sm:gap-x-8 gap-y-4 py-6">
              {/* Email Verification Status */}
              <div className="space-y-1">
                <label className="text-sm font-medium text-muted-foreground">E-Mail Status</label>
                <div className="flex items-center gap-2">
                  {selectedUser.emailVerifiedAt ? (
                    <>
                      <Icon icon="vaadin:check-circle" className="text-success" style={{ width: '20px', height: '20px' }} />
                      <div>
                        <div className="text-sm font-medium">Verifiziert</div>
                        <div className="text-xs text-muted-foreground">{formatDate(selectedUser.emailVerifiedAt)}</div>
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
              {selectedUser.phone && (
                <div className="space-y-1">
                  <label className="text-sm font-medium text-muted-foreground">Telefon</label>
                  <div className="flex items-center gap-2">
                    <Icon icon="vaadin:phone" className="text-foreground" style={{ width: '20px', height: '20px' }} />
                    <div className="text-sm font-medium">{selectedUser.phone}</div>
                  </div>
                </div>
              )}

              {/* Birthday */}
              {selectedUser.birthday && (
                <div className="space-y-1">
                  <label className="text-sm font-medium text-muted-foreground">Geburtsdatum</label>
                  <div className="flex items-center gap-2">
                    <Icon icon="vaadin:cake" className="text-foreground" style={{ width: '20px', height: '20px' }} />
                    <div className="text-sm font-medium">{formatDate(selectedUser.birthday)}</div>
                  </div>
                </div>
              )}

              {/* Address */}
              {(selectedUser.street || selectedUser.city || selectedUser.postalCode) && (
                <div className="space-y-1 sm:col-span-2">
                  <label className="text-sm font-medium text-muted-foreground">Adresse</label>
                  <div className="flex items-start gap-2">
                    <Icon icon="vaadin:home" className="text-foreground mt-0.5" style={{ width: '20px', height: '20px' }} />
                    <div className="text-sm font-medium">
                      {selectedUser.street && <div>{selectedUser.street}</div>}
                      {(selectedUser.postalCode || selectedUser.city) && (
                        <div>{selectedUser.postalCode} {selectedUser.city}</div>
                      )}
                    </div>
                  </div>
                </div>
              )}

              {/* Salutation */}
              {selectedUser.salutation && (
                <div className="space-y-1">
                  <label className="text-sm font-medium text-muted-foreground">Anrede</label>
                  <div className="flex items-center gap-2">
                    <Icon icon="vaadin:user" className="text-foreground" style={{ width: '20px', height: '20px' }} />
                    <div className="text-sm font-medium">{selectedUser.salutation}</div>
                  </div>
                </div>
              )}

              {/* Academic Title */}
              {selectedUser.academicTitle && (
                <div className="space-y-1">
                  <label className="text-sm font-medium text-muted-foreground">Titel</label>
                  <div className="flex items-center gap-2">
                    <Icon icon="vaadin:academy-cap" className="text-foreground" style={{ width: '20px', height: '20px' }} />
                    <div className="text-sm font-medium">{selectedUser.academicTitle}</div>
                  </div>
                </div>
              )}

              {/* Rank */}
              {selectedUser.rank && (
                <div className="space-y-1">
                  <label className="text-sm font-medium text-muted-foreground">Rang</label>
                  <div className="flex items-center gap-2">
                    <Icon icon="vaadin:medal" className="text-foreground" style={{ width: '20px', height: '20px' }} />
                    <div className="text-sm font-medium">{selectedUser.rank}</div>
                  </div>
                </div>
              )}
            </div>

            {/* Action Buttons */}
            <div className="flex flex-col sm:flex-row justify-end gap-3 pt-6 border-t mt-6">
              <Button variant="destructive" onClick={handleCloseModal} className="text-white w-full sm:w-auto">
                <Icon icon="vaadin:close" className="mr-2" style={{ width: '16px', height: '16px', color: 'white' }} />
                Schließen
              </Button>
              <Button variant="outline" onClick={() => handleEditClick(selectedUser)} className="text-white bg-black hover:bg-gray-800 w-full sm:w-auto">
                <Icon icon="vaadin:edit" className="mr-2" style={{ width: '16px', height: '16px', color: 'white' }} />
                Bearbeiten
              </Button>
            </div>
          </div>
        )}
      </Dialog>

      {/* Delete confirmation modal */}
      <Dialog
        opened={isDeleteModalOpen}
        onOpenedChanged={(e: any) => {
          if (!e.detail.value) handleCloseDeleteModal();
        }}
        headerTitle="Benutzer löschen"
      >
        {userToDelete && (
          <div className="space-y-4 p-4 min-w-[400px]">
            <div className="flex justify-center">
              <Icon icon="vaadin:warning" className="text-destructive" style={{ width: '64px', height: '64px' }} />
            </div>

            <div className="text-center space-y-2">
              <p className="font-medium">Sind Sie sicher, dass Sie diesen Benutzer löschen möchten?</p>
              <p className="text-sm text-muted-foreground">
                Benutzer: <span className="font-semibold">{userToDelete.name}</span>
              </p>
              <p className="text-sm text-muted-foreground">
                E-Mail: <span className="font-semibold">{userToDelete.email}</span>
              </p>
              <p className="text-sm text-destructive mt-4">
                Diese Aktion kann nicht rückgängig gemacht werden.
              </p>
            </div>

            <div className="flex justify-end gap-2 pt-4">
              <Button variant="outline" onClick={handleCloseDeleteModal}>
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
        opened={isEditModalOpen}
        onOpenedChanged={(e: any) => {
          if (!e.detail.value) handleCloseEditModal();
        }}
        headerTitle="Benutzer bearbeiten"
      >
        {userToEdit && (
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
                    <label className="text-sm font-medium">Rang</label>
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
              <Button variant="destructive" onClick={handleCloseEditModal} className="text-white w-full sm:w-auto">
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
    </div>
  );
}
