/**
 * @fileoverview Searchable member picker for admin views.
 *
 * Searches members on the server (fuzzy name/email search via
 * UserController.getUsersPage) as the admin types, debounced.
 *
 * @module MemberComboBox
 * @author Philipp Borkovic
 */

import {useEffect, useRef, useState} from 'react';
import {ComboBox} from '@vaadin/react-components/ComboBox';
import {UserController} from 'Frontend/generated/endpoints';
import type UserDTO from 'Frontend/generated/com/bunkermuseum/membermanagement/dto/UserDTO';
import {getErrorMessage} from '../../../types/vaadin';

/**
 * Maximum number of users fetched per search request.
 */
const USER_SEARCH_PAGE_SIZE = 20;

/**
 * Debounce delay in milliseconds before a search request is sent.
 */
const USER_SEARCH_DEBOUNCE_MS = 250;

/**
 * A member search result with a precomputed display label for the ComboBox.
 */
export type MemberOption = UserDTO & { label: string };

/**
 * Converts a user into a ComboBox option labelled "Name (E-Mail)".
 */
const toMemberOption = (user: UserDTO): MemberOption => ({ ...user, label: `${user.name} (${user.email})` });

interface MemberComboBoxProps {
  value: MemberOption | null;
  onChange: (member: MemberOption | null) => void;
  status?: 'active' | 'deleted' | 'all';
  onError?: (message: string) => void;
  placeholder?: string;
  disabled?: boolean;
  className?: string;
}

/**
 * MemberComboBox - searchable dropdown for picking a single member.
 *
 * @component
 * @author Philipp Borkovic
 */
export default function MemberComboBox({
  value,
  onChange,
  status = 'active',
  onError,
  placeholder = 'Name oder E-Mail suchen...',
  disabled = false,
  className = 'w-full',
}: MemberComboBoxProps): JSX.Element {
  const [options, setOptions] = useState<MemberOption[]>([]);
  const [isSearching, setIsSearching] = useState(false);

  /**
   * Pending debounce timer and sequence number of the latest search,
   * so that slower, outdated responses are ignored.
   */
  const searchTimeoutRef = useRef<ReturnType<typeof setTimeout>>();
  const searchSeqRef = useRef(0);

  /**
   * Searches members, debounced.
   *
   * @param {string} query - The text typed into the picker
   */
  const search = (query: string): void => {
    clearTimeout(searchTimeoutRef.current);

    searchTimeoutRef.current = setTimeout(async () => {
      const seq = ++searchSeqRef.current;
      setIsSearching(true);

      try {
        const page = await UserController.getUsersPage(0, USER_SEARCH_PAGE_SIZE, query.trim() || undefined, status);
        if (seq !== searchSeqRef.current) return;

        const results = (page?.content ?? [])
          .filter((user): user is UserDTO => !!user)
          .map(toMemberOption);
        if (value && !results.some((user) => user.id === value.id)) {
          results.unshift(value);
        }
        setOptions(results);
      } catch (e: unknown) {
        if (seq === searchSeqRef.current) {
          onError?.(getErrorMessage(e) || 'Fehler beim Laden der Mitglieder');
        }
      } finally {
        if (seq === searchSeqRef.current) {
          setIsSearching(false);
        }
      }
    }, USER_SEARCH_DEBOUNCE_MS);
  };

  useEffect(() => {
    search('');

    return () => clearTimeout(searchTimeoutRef.current);
  }, [status]);

  return (
    <ComboBox<MemberOption>
      className={className}
      filteredItems={options}
      itemIdPath="id"
      itemLabelPath="label"
      selectedItem={value ?? undefined}
      onSelectedItemChanged={(e) => onChange(e.detail.value ?? null)}
      onFilterChanged={(e) => search(e.detail.value)}
      loading={isSearching}
      placeholder={placeholder}
      clearButtonVisible
      disabled={disabled}
    />
  );
}
