import { useState, useEffect } from 'react';
import { Grid } from '@vaadin/react-components/Grid';
import { GridColumn } from '@vaadin/react-components/GridColumn';
import { Dialog } from '@vaadin/react-components/Dialog';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { Icon } from '@vaadin/react-components';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { UserController } from 'Frontend/generated/endpoints';
import type User from 'Frontend/generated/com/bunkermuseum/membermanagement/model/User';

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
  const [userToDelete, setUserToDelete] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  /**
   * Loads all users from the backend on component mount.
   */
  useEffect(() => {
    loadUsers();
  }, []);

  /**
   * Fetches all users from the UserController.
   *
   * @author Philipp Borkovic
   */
  const loadUsers = async (): Promise<void> => {
    try {
      setIsLoading(true);
      setError('');
      const allUsers = await UserController.getAllUsers();
      setUsers((allUsers || []).filter((user): user is User => user !== undefined));
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

  return (
    <div className="space-y-4">
      {/* Header */}
      <div className="mb-4">
        <h2 className="text-xl font-semibold">Benutzerverwaltung</h2>
        <p className="text-sm text-muted-foreground">
          Übersicht aller registrierten Benutzer
        </p>
      </div>

      {/* Error message */}
      {error && (
        <div className="rounded-md bg-destructive/10 p-3 text-sm text-destructive">
          {error}
        </div>
      )}

      {/* Users grid */}
      <div className="bg-white rounded-lg p-4">
        {isLoading ? (
          <div className="flex items-center justify-center py-12">
            <div className="text-center">
              <Icon icon="vaadin:spinner" className="animate-spin text-primary mb-2" style={{ width: '32px', height: '32px' }} />
              <p className="text-sm text-muted-foreground">Lädt Benutzer...</p>
            </div>
          </div>
        ) : users.length === 0 ? (
          <div className="flex items-center justify-center py-12">
            <div className="text-center">
              <Icon icon="vaadin:users" className="text-muted-foreground mb-2" style={{ width: '48px', height: '48px' }} />
              <p className="text-sm text-muted-foreground">Keine Benutzer gefunden</p>
            </div>
          </div>
        ) : (
          <Grid
            items={users}
            className="cursor-pointer"
          >
            <GridColumn path="name" header="Name" autoWidth />
            <GridColumn path="email" header="E-Mail" autoWidth />
            <GridColumn
              path="createdAt"
              header="Erstellt am"
              autoWidth
              renderer={({ item }: any) => formatDate(item.createdAt)}
            />
            <GridColumn
              header="Aktionen"
              autoWidth
              renderer={({ item }: any) => (
                <DropdownMenu>
                  <DropdownMenuTrigger asChild>
                    <button
                      onClick={(e) => e.stopPropagation()}
                      className="p-1 hover:bg-muted rounded"
                    >
                      <Icon icon="vaadin:ellipsis-dots-v" style={{ width: '20px', height: '20px' }} />
                    </button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent align="end">
                    <DropdownMenuItem onClick={() => handleUserClick(item)} className="text-foreground">
                      <Icon icon="vaadin:eye" className="mr-2" style={{ width: '16px', height: '16px' }} />
                      View Details
                    </DropdownMenuItem>
                    <DropdownMenuItem
                      onClick={() => handleDeleteClick(item)}
                      className="text-destructive focus:text-destructive"
                    >
                      <Icon icon="vaadin:trash" className="mr-2" style={{ width: '16px', height: '16px' }} />
                      Delete Account
                    </DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>
              )}
            />
          </Grid>
        )}
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
          <div className="space-y-4 p-4 min-w-[600px]">
            {/* User icon */}
            <div className="flex justify-center">
              <Icon icon="vaadin:user-card" className="text-primary" style={{ width: '64px', height: '64px' }} />
            </div>

            {/* User information */}
            <div className="space-y-3">
              <div className="grid grid-cols-3 gap-2">
                <span className="font-medium">Name:</span>
                <span className="col-span-2">{selectedUser.name}</span>
              </div>

              <div className="grid grid-cols-3 gap-2">
                <span className="font-medium">E-Mail:</span>
                <span className="col-span-2">{selectedUser.email}</span>
              </div>

              <div className="grid grid-cols-3 gap-2">
                <span className="font-medium">ID:</span>
                <span className="col-span-2 font-mono text-sm">{selectedUser.id}</span>
              </div>

              <div className="grid grid-cols-3 gap-2">
                <span className="font-medium">E-Mail verifiziert:</span>
                <span className="col-span-2">
                  {selectedUser.emailVerifiedAt ? (
                    <span className="text-success">
                      <Icon icon="vaadin:check-circle" className="inline mr-1" style={{ width: '16px', height: '16px' }} />
                      {formatDate(selectedUser.emailVerifiedAt)}
                    </span>
                  ) : (
                    <span className="text-destructive">
                      <Icon icon="vaadin:close-circle" className="inline mr-1" style={{ width: '16px', height: '16px' }} />
                      Nicht verifiziert
                    </span>
                  )}
                </span>
              </div>

              <div className="grid grid-cols-3 gap-2">
                <span className="font-medium">Google verknüpft:</span>
                <span className="col-span-2">
                  {selectedUser.googleId ? (
                    <Icon icon="vaadin:check" className="text-success" style={{ width: '16px', height: '16px' }} />
                  ) : (
                    <Icon icon="vaadin:close" className="text-muted-foreground" style={{ width: '16px', height: '16px' }} />
                  )}
                </span>
              </div>

              <div className="grid grid-cols-3 gap-2">
                <span className="font-medium">Microsoft verknüpft:</span>
                <span className="col-span-2">
                  {selectedUser.microsoftId ? (
                    <Icon icon="vaadin:check" className="text-success" style={{ width: '16px', height: '16px' }} />
                  ) : (
                    <Icon icon="vaadin:close" className="text-muted-foreground" style={{ width: '16px', height: '16px' }} />
                  )}
                </span>
              </div>

              <div className="grid grid-cols-3 gap-2">
                <span className="font-medium">Erstellt am:</span>
                <span className="col-span-2">{formatDate(selectedUser.createdAt)}</span>
              </div>

              <div className="grid grid-cols-3 gap-2">
                <span className="font-medium">Aktualisiert am:</span>
                <span className="col-span-2">{formatDate(selectedUser.updatedAt)}</span>
              </div>
            </div>

            {/* Close button */}
            <div className="flex justify-end pt-4">
              <Button onClick={handleCloseModal}>Schließen</Button>
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
    </div>
  );
}
