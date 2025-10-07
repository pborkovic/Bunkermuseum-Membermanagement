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
          <div className="p-6 min-w-[800px]">
            {/* Header Section */}
            <div className="flex items-start gap-6 pb-6 border-b">
              <div className="flex-shrink-0 bg-primary/10 rounded-full p-4">
                <Icon icon="vaadin:user-card" className="text-primary" style={{ width: '64px', height: '64px' }} />
              </div>
              <div className="flex-1">
                <h3 className="text-2xl font-semibold mb-1">{selectedUser.name}</h3>
                <p className="text-muted-foreground">{selectedUser.email}</p>
              </div>
            </div>

            {/* Details Grid */}
            <div className="grid grid-cols-2 gap-6 py-6">
              {/* Email Verification Status */}
              <div className="space-y-2">
                <label className="text-sm font-medium text-muted-foreground">E-Mail Status</label>
                <div className="flex items-center gap-2 p-3 bg-muted/50 rounded-lg">
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

              {/* Account Created */}
              <div className="space-y-2">
                <label className="text-sm font-medium text-muted-foreground">Konto erstellt</label>
                <div className="flex items-center gap-2 p-3 bg-muted/50 rounded-lg">
                  <Icon icon="vaadin:calendar" className="text-primary" style={{ width: '20px', height: '20px' }} />
                  <div className="text-sm font-medium">{formatDate(selectedUser.createdAt)}</div>
                </div>
              </div>

              {/* Google Connection */}
              <div className="space-y-2">
                <label className="text-sm font-medium text-muted-foreground">Google Verknüpfung</label>
                <div className="flex items-center gap-2 p-3 bg-muted/50 rounded-lg">
                  {selectedUser.googleId ? (
                    <>
                      <Icon icon="vaadin:check-circle-o" className="text-success" style={{ width: '20px', height: '20px' }} />
                      <div className="text-sm font-medium">Verknüpft</div>
                    </>
                  ) : (
                    <>
                      <Icon icon="vaadin:minus-circle-o" className="text-muted-foreground" style={{ width: '20px', height: '20px' }} />
                      <div className="text-sm font-medium text-muted-foreground">Nicht verknüpft</div>
                    </>
                  )}
                </div>
              </div>

              {/* Microsoft Connection */}
              <div className="space-y-2">
                <label className="text-sm font-medium text-muted-foreground">Microsoft Verknüpfung</label>
                <div className="flex items-center gap-2 p-3 bg-muted/50 rounded-lg">
                  {selectedUser.microsoftId ? (
                    <>
                      <Icon icon="vaadin:check-circle-o" className="text-success" style={{ width: '20px', height: '20px' }} />
                      <div className="text-sm font-medium">Verknüpft</div>
                    </>
                  ) : (
                    <>
                      <Icon icon="vaadin:minus-circle-o" className="text-muted-foreground" style={{ width: '20px', height: '20px' }} />
                      <div className="text-sm font-medium text-muted-foreground">Nicht verknüpft</div>
                    </>
                  )}
                </div>
              </div>
            </div>

            {/* Last Updated */}
            <div className="pt-4 border-t">
              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <Icon icon="vaadin:clock" style={{ width: '16px', height: '16px' }} />
                <span>Zuletzt aktualisiert: {formatDate(selectedUser.updatedAt)}</span>
              </div>
            </div>

            {/* Action Buttons */}
            <div className="flex justify-end gap-3 pt-6 border-t mt-6">
              <Button variant="outline" onClick={handleCloseModal}>Schließen</Button>
              <Button variant="default" onClick={() => {
                // TODO: Edit functionality
                console.log('Edit user:', selectedUser.id);
              }}>
                <Icon icon="vaadin:edit" className="mr-2" style={{ width: '16px', height: '16px' }} />
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
    </div>
  );
}
