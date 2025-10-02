import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

/**
 * Utility function to merge Tailwind CSS classes.
 *
 * @description
 * Combines multiple class values and resolves Tailwind CSS conflicts using:
 * - clsx: For conditional class names and object syntax
 * - twMerge: For intelligent Tailwind class conflict resolution
 *
 * This is the standard Shadcn UI utility for managing className props.
 *
 * @example
 * ```tsx
 * // Basic usage
 * cn('px-4 py-2', 'bg-blue-500')
 * // => 'px-4 py-2 bg-blue-500'
 *
 * // Conflict resolution (last one wins)
 * cn('px-2', 'px-4')
 * // => 'px-4'
 *
 * // Conditional classes
 * cn('base-class', isActive && 'active-class')
 * // => 'base-class active-class' (if isActive is true)
 *
 * // Object syntax
 * cn({ 'text-red-500': hasError, 'text-green-500': !hasError })
 * // => 'text-red-500' or 'text-green-500'
 * ```
 *
 * @param inputs - Array of class values (strings, objects, arrays, etc.)
 * @returns Merged class string with resolved conflicts
 */
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}
