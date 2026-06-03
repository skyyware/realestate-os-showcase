import { CommonModule, CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { API_BASE_URL } from './core/api';

@Component({
  selector: 'app-root',
  imports: [CommonModule, CurrencyPipe, DatePipe, DecimalPipe, ReactiveFormsModule],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly fb = inject(FormBuilder);

  protected readonly mode = signal<'register' | 'login' | 'password' | 'dashboard'>('register');
  protected readonly loading = signal(false);
  protected readonly error = signal('');
  protected readonly info = signal('');
  protected readonly dashboard = signal<Dashboard | null>(null);
  protected readonly registrationPreview = signal<RegistrationPreview | null>(null);
  protected readonly setupToken = signal('');
  protected readonly firstProperty = computed(() => this.dashboard()?.properties[0]);

  protected readonly registerForm = this.fb.nonNullable.group({
    fullName: ['Sascha Dobrochynskyy', [Validators.required, Validators.minLength(2)]],
    email: ['sascha@skyyware.com', [Validators.required, Validators.email]],
    organizationName: ['SKYYWARE Product Engineering', [Validators.required, Validators.minLength(2)]]
  });

  protected readonly loginForm = this.fb.nonNullable.group({
    email: ['sascha@skyyware.com', [Validators.required, Validators.email]],
    password: ['', [Validators.required]]
  });

  protected readonly passwordForm = this.fb.nonNullable.group({
    password: ['', [Validators.required, Validators.minLength(10)]]
  });

  protected readonly taskForm = this.fb.nonNullable.group({
    title: ['Angebot für Dachwartung prüfen', [Validators.required, Validators.maxLength(180)]],
    description: ['Eigentümerbeirat soll Angebot, Budget und Dringlichkeit in einem Vorgang bewerten.', [Validators.required]],
    priority: ['HIGH' as TaskPriority, [Validators.required]],
    dueDate: [this.toGermanDate(new Date(Date.now() + 5 * 86400000)), [
      Validators.required,
      Validators.pattern(/^\d{2}\.\d{2}\.\d{4}$/)
    ]]
  });

  ngOnInit(): void {
    const params = new URLSearchParams(location.search);
    const token = params.get('token');
    if (location.pathname.includes('set-password') && token) {
      this.setupToken.set(token);
      this.mode.set('password');
      this.previewRegistration(token);
      return;
    }
    if (localStorage.getItem('realestate.token')) {
      this.mode.set('dashboard');
      this.loadDashboard();
    }
  }

  protected register(): void {
    if (this.registerForm.invalid) return;
    this.begin();
    this.http.post<RegistrationResult>(`${API_BASE_URL}/auth/register`, this.registerForm.getRawValue())
      .subscribe({
        next: result => {
          this.loading.set(false);
          this.info.set(result.emailSent
            ? 'Aktivierungslink wurde per E-Mail verschickt.'
            : `Lokaler Mailversand ist deaktiviert. Demo-Link: ${result.localSetupLink}`);
          if (result.localSetupLink) {
            const token = new URL(result.localSetupLink).searchParams.get('token') ?? '';
            this.setupToken.set(token);
            this.mode.set('password');
            this.previewRegistration(token);
          }
        },
        error: error => this.fail(error)
      });
  }

  protected login(): void {
    if (this.loginForm.invalid) return;
    this.begin();
    this.http.post<AuthSession>(`${API_BASE_URL}/auth/login`, this.loginForm.getRawValue())
      .subscribe({ next: session => this.acceptSession(session), error: error => this.fail(error) });
  }

  protected setPassword(): void {
    if (this.passwordForm.invalid || !this.setupToken()) return;
    this.begin();
    this.http.post<AuthSession>(`${API_BASE_URL}/auth/password`, {
      token: this.setupToken(),
      password: this.passwordForm.controls.password.value
    }).subscribe({ next: session => this.acceptSession(session), error: error => this.fail(error) });
  }

  protected addTask(): void {
    if (this.taskForm.invalid) return;
    this.begin();
    const formValue = this.taskForm.getRawValue();
    this.http.post<Dashboard>(`${API_BASE_URL}/workspace/tasks`, {
      ...formValue,
      dueDate: this.toIsoDate(formValue.dueDate)
    })
      .subscribe({
        next: dashboard => {
          this.loading.set(false);
          this.dashboard.set(dashboard);
          this.info.set('Aufgabe wurde erstellt und im Aktivitätsfeed protokolliert.');
        },
        error: error => this.fail(error)
      });
  }

  protected logout(): void {
    localStorage.removeItem('realestate.token');
    this.dashboard.set(null);
    this.mode.set('login');
  }

  protected switchMode(mode: 'register' | 'login'): void {
    this.error.set('');
    this.info.set('');
    this.mode.set(mode);
  }

  private previewRegistration(token: string): void {
    this.http.get<RegistrationPreview>(`${API_BASE_URL}/auth/registration/${token}`)
      .subscribe({
        next: preview => this.registrationPreview.set(preview),
        error: () => this.error.set('Der Aktivierungslink ist ungültig oder abgelaufen.')
      });
  }

  private loadDashboard(): void {
    this.begin();
    this.http.get<Dashboard>(`${API_BASE_URL}/workspace/dashboard`)
      .subscribe({ next: dashboard => { this.loading.set(false); this.dashboard.set(dashboard); }, error: error => this.fail(error) });
  }

  private acceptSession(session: AuthSession): void {
    localStorage.setItem('realestate.token', session.accessToken);
    this.loading.set(false);
    this.info.set(`Willkommen, ${session.user.fullName}.`);
    this.mode.set('dashboard');
    history.replaceState({}, '', '/');
    this.loadDashboard();
  }

  private begin(): void {
    this.loading.set(true);
    this.error.set('');
    this.info.set('');
  }

  private fail(error: { error?: { message?: string } }): void {
    this.loading.set(false);
    this.error.set(error.error?.message ?? 'Der Vorgang konnte nicht abgeschlossen werden.');
  }

  private toGermanDate(date: Date): string {
    return new Intl.DateTimeFormat('de-DE', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    }).format(date);
  }

  private toIsoDate(value: string): string {
    const match = /^(\d{2})\.(\d{2})\.(\d{4})$/.exec(value);
    return match ? `${match[3]}-${match[2]}-${match[1]}` : value;
  }
}

type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';

interface RegistrationResult {
  emailSent: boolean;
  localSetupLink?: string;
}

interface RegistrationPreview {
  email: string;
  fullName: string;
  organizationName: string;
}

interface AuthSession {
  accessToken: string;
  user: {
    email: string;
    fullName: string;
    organizationName: string;
    role: string;
  };
}

interface Dashboard {
  user: { email: string; fullName: string; organizationName: string };
  properties: Array<{
    id: string;
    name: string;
    address: string;
    city: string;
    unitCount: number;
    cashBalance: number;
    reserveBalance: number;
  }>;
  metrics: {
    properties: number;
    units: number;
    cashBalance: number;
    reserveBalance: number;
    pendingPayments: number;
  };
  units: Array<{ ownerName: string; unitLabel: string; shareValue: number }>;
  tasks: Array<{ id: string; title: string; description: string; status: string; priority: string; dueDate?: string }>;
  finances: Array<{ label: string; amount: number; category: string; bookedOn: string; status: string }>;
  activity: Array<{ eventType: string; summary: string; createdAt: string }>;
}
