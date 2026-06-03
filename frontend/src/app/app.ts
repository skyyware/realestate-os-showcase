import { CommonModule, CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, computed, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { API_BASE_URL } from './core/api';

@Component({
  selector: 'app-root',
  imports: [CommonModule, CurrencyPipe, DatePipe, DecimalPipe, ReactiveFormsModule],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App implements OnInit, OnDestroy {
  private readonly http = inject(HttpClient);
  private readonly fb = inject(FormBuilder);
  private readonly sessionExpiredHandler = () => this.handleSessionExpired();

  protected readonly mode = signal<'register' | 'login' | 'password' | 'dashboard'>('register');
  protected readonly activeSection = signal<Section>('overview');
  protected readonly loading = signal(false);
  protected readonly error = signal('');
  protected readonly info = signal('');
  protected readonly dashboard = signal<Dashboard | null>(null);
  protected readonly registrationPreview = signal<RegistrationPreview | null>(null);
  protected readonly setupToken = signal('');
  protected readonly searchTerm = signal('');
  protected readonly selectedPropertyId = signal<string | null>(null);

  protected readonly selectedProperty = computed(() => {
    const dashboard = this.dashboard();
    if (!dashboard) return undefined;
    const propertyId = this.selectedPropertyId() ?? dashboard.selectedPropertyId;
    return dashboard.properties.find(item => item.id === propertyId) ?? dashboard.properties[0];
  });
  protected readonly hasWorkspace = computed(() => (this.dashboard()?.properties.length ?? 0) > 0);
  protected readonly filteredProperties = computed(() => {
    const term = this.searchTerm().trim().toLowerCase();
    const properties = this.dashboard()?.properties ?? [];
    if (!term) return properties;
    return properties.filter(property =>
      `${property.name} ${property.address} ${property.city}`.toLowerCase().includes(term)
    );
  });
  protected readonly openInvoices = computed(() => (this.dashboard()?.finances ?? []).filter(item => item.amount < 0));
  protected readonly dueTasks = computed(() => (this.dashboard()?.tasks ?? []).filter(task => task.status !== 'DONE'));

  protected readonly registerForm = this.fb.nonNullable.group({
    fullName: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    organizationName: ['', [Validators.required, Validators.minLength(2)]]
  });

  protected readonly loginForm = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]]
  });

  protected readonly passwordForm = this.fb.nonNullable.group({
    password: ['', [Validators.required, Validators.minLength(10)]]
  });

  protected readonly propertyForm = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(180)]],
    address: ['', [Validators.required, Validators.maxLength(240)]],
    city: ['', [Validators.required, Validators.maxLength(120)]],
    unitCount: [1, [Validators.required, Validators.min(1)]],
    cashBalance: [0, [Validators.required, Validators.min(0)]],
    reserveBalance: [0, [Validators.required, Validators.min(0)]]
  });

  protected readonly unitForm = this.fb.nonNullable.group({
    ownerName: ['', [Validators.required, Validators.maxLength(180)]],
    unitLabel: ['', [Validators.required, Validators.maxLength(80)]],
    shareValue: [0, [Validators.required, Validators.min(0)]]
  });

  protected readonly taskForm = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(180)]],
    description: ['', [Validators.required, Validators.maxLength(1000)]],
    priority: ['MEDIUM' as TaskPriority, [Validators.required]],
    dueDate: ['', [Validators.required, germanDateValidator]]
  });

  protected readonly financeForm = this.fb.nonNullable.group({
    label: ['', [Validators.required, Validators.maxLength(180)]],
    amount: [0, [Validators.required]],
    category: ['Hausgeld', [Validators.required, Validators.maxLength(80)]],
    bookedOn: [this.today(), [Validators.required, germanDateValidator]],
    status: ['BOOKED', [Validators.required, Validators.maxLength(32)]]
  });

  protected readonly documentForm = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(180)]],
    documentType: ['PDF', [Validators.required, Validators.maxLength(80)]],
    fileName: ['', [Validators.required, Validators.maxLength(240)]],
    documentDate: [this.today(), [Validators.required, germanDateValidator]]
  });

  ngOnInit(): void {
    window.addEventListener('realestate:session-expired', this.sessionExpiredHandler);
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

  ngOnDestroy(): void {
    window.removeEventListener('realestate:session-expired', this.sessionExpiredHandler);
  }

  protected register(): void {
    if (this.registerForm.invalid) {
      this.error.set('Bitte Name, gültige E-Mail-Adresse und Organisation ausfüllen.');
      return;
    }
    const normalizedEmail = this.normalizeEmail(this.registerForm.controls.email.value);
    const suggestion = this.emailSuggestion(normalizedEmail);
    if (suggestion) {
      this.error.set(`Bitte E-Mail-Adresse prüfen. Meintest du ${suggestion}?`);
      return;
    }
    this.registerForm.controls.email.setValue(normalizedEmail);
    this.begin();
    this.http.post<RegistrationResult>(`${API_BASE_URL}/auth/register`, this.registerForm.getRawValue())
      .subscribe({
        next: result => {
          this.loading.set(false);
          this.info.set(result.emailSent
            ? 'Aktivierungslink wurde per E-Mail verschickt.'
            : `Lokaler Mailversand ist deaktiviert. Setup-Link: ${result.localSetupLink}`);
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
    if (this.loginForm.invalid) {
      this.error.set('Bitte E-Mail-Adresse und Passwort eingeben.');
      return;
    }
    const normalizedEmail = this.normalizeEmail(this.loginForm.controls.email.value);
    const suggestion = this.emailSuggestion(normalizedEmail);
    if (suggestion) {
      this.error.set(`Bitte E-Mail-Adresse prüfen. Meintest du ${suggestion}?`);
      return;
    }
    this.loginForm.controls.email.setValue(normalizedEmail);
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

  protected createProperty(): void {
    if (this.propertyForm.invalid) return;
    this.submitDashboardRequest('properties', this.propertyForm.getRawValue(), 'Immobilie wurde angelegt.');
  }

  protected createUnit(): void {
    if (this.unitForm.invalid) return;
    const propertyId = this.requireSelectedPropertyId();
    if (!propertyId) return;
    this.submitDashboardRequest('units', { ...this.unitForm.getRawValue(), propertyId }, 'Einheit wurde angelegt.');
  }

  protected addTask(): void {
    if (this.taskForm.invalid) return;
    const propertyId = this.requireSelectedPropertyId();
    if (!propertyId) return;
    const formValue = this.taskForm.getRawValue();
    this.submitDashboardRequest('tasks', { ...formValue, dueDate: this.toIsoDate(formValue.dueDate), propertyId }, 'Aufgabe wurde erstellt und protokolliert.');
  }

  protected createFinance(): void {
    if (this.financeForm.invalid) return;
    const propertyId = this.requireSelectedPropertyId();
    if (!propertyId) return;
    const formValue = this.financeForm.getRawValue();
    this.submitDashboardRequest('finances', { ...formValue, bookedOn: this.toIsoDate(formValue.bookedOn), propertyId }, 'Finanzereignis wurde erfasst.');
  }

  protected createDocument(): void {
    if (this.documentForm.invalid) return;
    const propertyId = this.requireSelectedPropertyId();
    if (!propertyId) return;
    const formValue = this.documentForm.getRawValue();
    this.submitDashboardRequest('documents', { ...formValue, documentDate: this.toIsoDate(formValue.documentDate), propertyId }, 'Dokument wurde abgelegt.');
  }

  protected logout(): void {
    localStorage.removeItem('realestate.token');
    this.dashboard.set(null);
    this.selectedPropertyId.set(null);
    this.mode.set('login');
    history.replaceState({}, '', '/');
  }

  protected switchMode(mode: 'register' | 'login'): void {
    this.error.set('');
    this.info.set('');
    this.mode.set(mode);
  }

  protected selectSection(section: Section): void {
    this.activeSection.set(section);
    this.error.set('');
    this.info.set('');
  }

  protected updateSearch(value: Event): void {
    this.searchTerm.set((value.target as HTMLInputElement).value);
  }

  protected selectProperty(event: Event): void {
    this.selectPropertyId((event.target as HTMLSelectElement).value);
  }

  protected selectPropertyId(propertyId: string): void {
    if (!propertyId || propertyId === this.selectedProperty()?.id) return;
    this.selectedPropertyId.set(propertyId);
    this.loadDashboard(propertyId);
  }

  protected openInsight(insight: InsightView): void {
    this.selectSection(insight.actionSection);
  }

  protected updateTaskStatus(task: WorkTaskView, status: TaskStatus): void {
    this.begin();
    this.http.patch<Dashboard>(`${API_BASE_URL}/workspace/tasks/${task.id}/status`, { status })
      .subscribe({
        next: dashboard => {
          this.loading.set(false);
          this.applyDashboard(dashboard);
          this.info.set(status === 'DONE' ? 'Aufgabe erledigt.' : 'Aufgabe ist in Prüfung.');
        },
        error: error => this.fail(error)
      });
  }

  protected priorityLabel(priority: string): string {
    return {
      LOW: 'Niedrig',
      MEDIUM: 'Mittel',
      HIGH: 'Hoch',
      URGENT: 'Dringend'
    }[priority] ?? priority;
  }

  protected statusLabel(status: string): string {
    return {
      BOOKED: 'Gebucht',
      REVIEW: 'Prüfung',
      OPEN: 'Offen',
      OPEN_TASK: 'Offen',
      IN_REVIEW: 'In Prüfung',
      DONE: 'Erledigt'
    }[status] ?? status;
  }

  protected insightSeverityLabel(severity: string): string {
    return {
      HIGH: 'Priorität',
      MEDIUM: 'Nächster Schritt',
      LOW: 'Verbesserung',
      GOOD: 'Stabil'
    }[severity] ?? severity;
  }

  private submitDashboardRequest(path: 'properties' | 'units' | 'tasks' | 'finances' | 'documents', payload: Record<string, unknown>, success: string): void {
    this.begin();
    this.http.post<Dashboard>(`${API_BASE_URL}/workspace/${path}`, payload)
      .subscribe({
        next: dashboard => {
          this.loading.set(false);
          this.applyDashboard(dashboard);
          this.info.set(success);
          if (path === 'properties') this.propertyForm.reset({ name: '', address: '', city: '', unitCount: 1, cashBalance: 0, reserveBalance: 0 });
          if (path === 'units') this.unitForm.reset({ ownerName: '', unitLabel: '', shareValue: 0 });
          if (path === 'tasks') this.taskForm.reset({ title: '', description: '', priority: 'MEDIUM', dueDate: '' });
          if (path === 'finances') this.financeForm.reset({ label: '', amount: 0, category: 'Hausgeld', bookedOn: this.today(), status: 'BOOKED' });
          if (path === 'documents') this.documentForm.reset({ title: '', documentType: 'PDF', fileName: '', documentDate: this.today() });
        },
        error: error => this.fail(error)
      });
  }

  private previewRegistration(token: string): void {
    this.http.get<RegistrationPreview>(`${API_BASE_URL}/auth/registration/${token}`)
      .subscribe({
        next: preview => this.registrationPreview.set(preview),
        error: () => this.error.set('Der Aktivierungslink ist ungültig oder abgelaufen.')
      });
  }

  private loadDashboard(propertyId = this.selectedPropertyId() ?? undefined): void {
    this.begin();
    const url = propertyId
      ? `${API_BASE_URL}/workspace/dashboard?propertyId=${encodeURIComponent(propertyId)}`
      : `${API_BASE_URL}/workspace/dashboard`;
    this.http.get<Dashboard>(url)
      .subscribe({ next: dashboard => { this.loading.set(false); this.applyDashboard(dashboard); }, error: error => this.fail(error) });
  }

  private acceptSession(session: AuthSession): void {
    localStorage.setItem('realestate.token', session.accessToken);
    this.loading.set(false);
    this.info.set(`Willkommen, ${session.user.fullName}.`);
    this.mode.set('dashboard');
    this.activeSection.set('overview');
    history.replaceState({}, '', '/');
    this.loadDashboard();
  }

  private begin(): void {
    this.loading.set(true);
    this.error.set('');
    this.info.set('');
  }

  private fail(error: { status?: number; error?: { message?: string } }): void {
    if (error.status === 401 && this.mode() === 'dashboard') {
      this.handleSessionExpired();
      return;
    }
    this.loading.set(false);
    this.error.set(error.error?.message ?? 'Der Vorgang konnte nicht abgeschlossen werden.');
  }

  private applyDashboard(dashboard: Dashboard): void {
    this.dashboard.set(dashboard);
    this.selectedPropertyId.set(dashboard.selectedPropertyId ?? dashboard.properties[0]?.id ?? null);
  }

  private requireSelectedPropertyId(): string | null {
    const propertyId = this.selectedProperty()?.id;
    if (propertyId) return propertyId;
    this.error.set('Bitte zuerst eine Immobilie hinzufügen.');
    this.activeSection.set('properties');
    return null;
  }

  private handleSessionExpired(): void {
    this.loading.set(false);
    this.dashboard.set(null);
    this.selectedPropertyId.set(null);
    this.mode.set('login');
    this.error.set('Ihre Sitzung ist abgelaufen. Bitte erneut einloggen.');
    this.info.set('');
    history.replaceState({}, '', '/');
  }

  private normalizeEmail(email: string): string {
    return email.trim().toLowerCase();
  }

  private emailSuggestion(email: string): string {
    const suffixFixes: Record<string, string> = {
      '.cpm': '.com',
      '.cmo': '.com',
      '.con': '.com',
      '.vom': '.com',
      '.deu': '.de'
    };
    const typo = Object.keys(suffixFixes).find(suffix => email.endsWith(suffix));
    return typo ? email.slice(0, -typo.length) + suffixFixes[typo] : '';
  }

  private today(): string {
    const date = new Date();
    return `${String(date.getDate()).padStart(2, '0')}.${String(date.getMonth() + 1).padStart(2, '0')}.${date.getFullYear()}`;
  }

  private toIsoDate(value: string): string {
    const match = /^(\d{2})\.(\d{2})\.(\d{4})$/.exec(value);
    if (!match) return value;
    const [, day, month, year] = match;
    return `${year}-${month}-${day}`;
  }
}

function germanDateValidator(control: AbstractControl): ValidationErrors | null {
  const value = String(control.value ?? '');
  if (!value) return null;
  const match = /^(\d{2})\.(\d{2})\.(\d{4})$/.exec(value);
  if (!match) return { germanDate: true };
  const [, dayValue, monthValue, yearValue] = match;
  const day = Number(dayValue);
  const month = Number(monthValue);
  const year = Number(yearValue);
  const date = new Date(Date.UTC(year, month - 1, day));
  return date.getUTCFullYear() === year && date.getUTCMonth() === month - 1 && date.getUTCDate() === day
    ? null
    : { germanDate: true };
}

type Section = 'overview' | 'properties' | 'units' | 'finances' | 'tasks' | 'documents' | 'activity';
type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
type TaskStatus = 'OPEN' | 'IN_REVIEW' | 'DONE';
type InsightSeverity = 'HIGH' | 'MEDIUM' | 'LOW' | 'GOOD';

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
  selectedPropertyId?: string;
  properties: Array<{
    id: string;
    name: string;
    address: string;
    city: string;
    unitCount: number;
    cashBalance: number;
    reserveBalance: number;
    status: string;
  }>;
  metrics: {
    properties: number;
    units: number;
    cashBalance: number;
    reserveBalance: number;
    pendingPayments: number;
    openTasks: number;
    onboardingCompletion: number;
  };
  units: Array<{ ownerName: string; unitLabel: string; shareValue: number }>;
  tasks: WorkTaskView[];
  finances: Array<{ label: string; amount: number; category: string; bookedOn: string; status: string }>;
  documents: Array<{ id: string; title: string; documentType: string; fileName: string; documentDate: string }>;
  activity: Array<{ eventType: string; summary: string; createdAt: string }>;
  insights: InsightView[];
  onboarding: {
    completion: number;
    accountActivated: boolean;
    propertyCreated: boolean;
    unitsCreated: boolean;
    financeCreated: boolean;
    taskCreated: boolean;
  };
}

interface WorkTaskView {
  id: string;
  title: string;
  description: string;
  status: TaskStatus;
  priority: TaskPriority;
  dueDate?: string;
}

interface InsightView {
  severity: InsightSeverity;
  title: string;
  description: string;
  actionSection: Section;
  actionLabel: string;
}
