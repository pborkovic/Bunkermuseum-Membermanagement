import { useState, useCallback } from 'react';
import type { ModalState } from '../types';

/**
 * Extended modal state with data management.
 *
 * @template T - The type of data associated with the modal
 * @interface ModalStateWithData
 * @extends {ModalState}
 * @property {T | null} data - The data associated with the modal
 * @property {(data: T) => void} openWith - Open the modal with specific data
 */
export interface ModalStateWithData<T> extends ModalState {
  data: T | null;
  openWith: (data: T) => void;
}

/**
 * Custom hook for managing modal state.
 * Provides a clean API for opening, closing, and toggling modals.
 *
 * @param {boolean} [initialState=false] - Initial open state of the modal
 * @returns {ModalState} Modal state and control functions
 *
 * @author Philipp Borkovic
 */
export function useModal(initialState = false): ModalState {
  const [isOpen, setIsOpen] = useState<boolean>(initialState);

  /**
   * Opens the modal.
   */
  const open = useCallback((): void => {
    setIsOpen(true);
  }, []);

  /**
   * Closes the modal.
   */
  const close = useCallback((): void => {
    setIsOpen(false);
  }, []);

  /**
   * Toggles the modal state.
   */
  const toggle = useCallback((): void => {
    setIsOpen((prev) => !prev);
  }, []);

  return {
    isOpen,
    open,
    close,
    toggle,
  };
}

/**
 * Custom hook for managing modal state with associated data.
 * Useful for modals that display or edit specific items.
 *
 * @template T - The type of data associated with the modal
 * @returns {ModalStateWithData<T>} Modal state with data and control functions
 *
 * @author Philipp Borkovic
 */
export function useModalWithData<T>(): ModalStateWithData<T> {
  const [isOpen, setIsOpen] = useState<boolean>(false);
  const [data, setData] = useState<T | null>(null);

  /**
   * Opens the modal.
   */
  const open = useCallback((): void => {
    setIsOpen(true);
  }, []);

  /**
   * Closes the modal and clears the data.
   */
  const close = useCallback((): void => {
    setIsOpen(false);
    setData(null);
  }, []);

  /**
   * Toggles the modal state.
   */
  const toggle = useCallback((): void => {
    setIsOpen((prev) => !prev);
  }, []);

  /**
   * Opens the modal with specific data.
   */
  const openWith = useCallback((newData: T): void => {
    setData(newData);
    setIsOpen(true);
  }, []);

  return {
    isOpen,
    data,
    open,
    close,
    toggle,
    openWith,
  };
}
