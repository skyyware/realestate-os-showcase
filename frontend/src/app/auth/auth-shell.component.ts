import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';

export type AuthMode = 'register' | 'login' | 'forgot' | 'password' | 'dashboard';

interface RegistrationPreviewView {
  email: string;
  fullName: string;
  organizationName: string;
}

@Component({
  selector: 'app-auth-shell',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './auth-shell.component.html',
  styleUrl: './auth-shell.component.scss'
})
export class AuthShellComponent {
  @Input({ required: true }) mode!: AuthMode;
  @Input({ required: true }) loading = false;
  @Input({ required: true }) error = '';
  @Input({ required: true }) info = '';
  @Input({ required: true }) registerForm!: FormGroup;
  @Input({ required: true }) loginForm!: FormGroup;
  @Input({ required: true }) forgotForm!: FormGroup;
  @Input({ required: true }) passwordForm!: FormGroup;
  @Input() registrationPreview: RegistrationPreviewView | null = null;

  @Output() modeChange = new EventEmitter<'register' | 'login' | 'forgot'>();
  @Output() registerRequested = new EventEmitter<void>();
  @Output() loginRequested = new EventEmitter<void>();
  @Output() passwordResetRequested = new EventEmitter<void>();
  @Output() passwordSetRequested = new EventEmitter<void>();

  protected switchMode(mode: 'register' | 'login' | 'forgot'): void {
    this.modeChange.emit(mode);
  }
}
