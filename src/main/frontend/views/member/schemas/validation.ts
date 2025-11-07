/**
 * @fileoverview Zod validation schemas for member profile and settings forms.
 *
 * This module provides comprehensive validation schemas with German error messages
 * for all member settings forms including profile information and password changes.
 *
 * @module member/schemas/validation
 */

import { z } from 'zod';

/**
 * Zod validation schema for profile information form.
 *
 * This schema provides type-safe, comprehensive validation for all profile fields
 * with localized German error messages. It validates:
 * - Name must be non-empty and between 2-100 characters
 * - Email must be valid email format
 * - Phone must be valid format (optional)
 * - Postal code must be valid format (optional)
 * - All other fields are optional
 *
 * @constant
 * @type {z.ZodObject}
 *
 * @author Philipp Borkovic
 */
export const profileFormSchema = z.object({
  name: z
    .string()
    .min(2, 'Der Name muss mindestens 2 Zeichen lang sein.')
    .max(100, 'Der Name darf maximal 100 Zeichen lang sein.')
    .refine(
      (val) => val.trim().length >= 2,
      'Der Name darf nicht nur aus Leerzeichen bestehen.'
    ),

  email: z
    .string()
    .min(1, 'Bitte geben Sie eine E-Mail-Adresse ein.')
    .email('Bitte geben Sie eine gültige E-Mail-Adresse ein.')
    .max(255, 'Die E-Mail-Adresse darf maximal 255 Zeichen lang sein.'),

  salutation: z
    .string()
    .optional(),

  academicTitle: z
    .string()
    .max(50, 'Der akademische Titel darf maximal 50 Zeichen lang sein.')
    .optional(),

  rank: z
    .string()
    .max(50, 'Der Dienstgrad darf maximal 50 Zeichen lang sein.')
    .optional(),

  birthday: z
    .date()
    .optional()
    .refine(
      (date) => {
        if (!date) return true;
        const today = new Date();
        const age = today.getFullYear() - date.getFullYear();
        return age >= 0 && age <= 150;
      },
      'Bitte geben Sie ein gültiges Geburtsdatum ein.'
    ),

  phone: z
    .string()
    .optional()
    .refine(
      (val) => {
        if (!val || val.trim().length === 0) return true;
        const phoneRegex = /^[\d\s\+\-\(\)\/]+$/;
        return phoneRegex.test(val);
      },
      'Bitte geben Sie eine gültige Telefonnummer ein.'
    ),

  street: z
    .string()
    .max(200, 'Die Straße darf maximal 200 Zeichen lang sein.')
    .optional(),

  city: z
    .string()
    .max(100, 'Die Stadt darf maximal 100 Zeichen lang sein.')
    .optional(),

  postalCode: z
    .string()
    .optional()
    .refine(
      (val) => {
        if (!val || val.trim().length === 0) return true;
        const postalCodeRegex = /^\d{4,}$/;
        return postalCodeRegex.test(val.trim());
      },
      'Bitte geben Sie eine gültige Postleitzahl ein (mindestens 4 Ziffern).'
    ),

  country: z
    .string()
    .max(100, 'Das Land darf maximal 100 Zeichen lang sein.')
    .optional(),
});

/**
 * TypeScript type inferred from the profile form schema.
 * Ensures type safety between schema and form state.
 *
 * @typedef {z.infer<typeof profileFormSchema>} ProfileFormSchemaType
 *
 * @author Philipp Borkovic
 */
export type ProfileFormSchemaType = z.infer<typeof profileFormSchema>;

/**
 * Zod validation schema for password change form.
 *
 * This schema provides type-safe validation for password changes with:
 * - Current password must be non-empty
 * - New password must meet strength requirements (min 8 chars)
 * - Confirm password must match new password
 * - German error messages for all validation failures
 *
 * @constant
 * @type {z.ZodObject}
 *
 * @author Philipp Borkovic
 */
export const passwordChangeSchema = z
  .object({
    currentPassword: z
      .string()
      .min(1, 'Bitte geben Sie Ihr aktuelles Passwort ein.'),

    newPassword: z
      .string()
      .min(8, 'Das neue Passwort muss mindestens 8 Zeichen lang sein.')
      .max(128, 'Das neue Passwort darf maximal 128 Zeichen lang sein.')
      .refine(
        (val) => /[A-Za-z]/.test(val),
        'Das Passwort muss mindestens einen Buchstaben enthalten.'
      )
      .refine(
        (val) => /[0-9]/.test(val),
        'Das Passwort muss mindestens eine Zahl enthalten.'
      ),

    confirmPassword: z
      .string()
      .min(1, 'Bitte bestätigen Sie Ihr neues Passwort.'),
  })
  .refine(
    (data) => data.newPassword === data.confirmPassword,
    {
      message: 'Die Passwörter stimmen nicht überein.',
      path: ['confirmPassword'],
    }
  )
  .refine(
    (data) => data.currentPassword !== data.newPassword,
    {
      message: 'Das neue Passwort muss sich vom aktuellen Passwort unterscheiden.',
      path: ['newPassword'],
    }
  );

/**
 * TypeScript type inferred from the password change schema.
 * Ensures type safety between schema and form state.
 *
 * @typedef {z.infer<typeof passwordChangeSchema>} PasswordChangeSchemaType
 *
 * @author Philipp Borkovic
 */
export type PasswordChangeSchemaType = z.infer<typeof passwordChangeSchema>;
