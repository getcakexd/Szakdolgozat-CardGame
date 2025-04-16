import { Injectable } from '@angular/core';
import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

@Injectable({
  providedIn: 'root'
})
export class PasswordValidatorService {

  createPasswordStrengthValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const value = control.value;

      if (!value) {
        return null;
      }

      const hasUpperCase = /[A-Z]/.test(value);
      const hasLowerCase = /[a-z]/.test(value);
      const hasNumeric = /[0-9]/.test(value);
      const hasSpecialChar = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(value);
      const minLength = value.length >= 8;

      const passwordValid = hasUpperCase && hasLowerCase && hasNumeric && hasSpecialChar && minLength;

      return !passwordValid ? {
        passwordStrength: {
          hasUpperCase,
          hasLowerCase,
          hasNumeric,
          hasSpecialChar,
          minLength
        }
      } : null;
    };
  }


  getPasswordErrorMessage(errors: any): string {
    if (!errors) return '';

    if (errors['required']) return 'Password is required';

    if (errors['passwordStrength']) {
      const strength = errors['passwordStrength'];
      const messages = [];

      if (!strength.minLength) messages.push('at least 8 characters');
      if (!strength.hasUpperCase) messages.push('at least one uppercase letter');
      if (!strength.hasLowerCase) messages.push('at least one lowercase letter');
      if (!strength.hasNumeric) messages.push('at least one number');
      if (!strength.hasSpecialChar) messages.push('at least one special character');

      return `Password must contain ${messages.join(', ')}`;
    }

    return 'Invalid password';
  }
}
