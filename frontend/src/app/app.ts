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

  protected readonly mode = signal<'register' | 'login' | 'forgot' | 'password' | 'dashboard'>('register');
  protected readonly activeSection = signal<Section>('overview');
  protected readonly loading = signal(false);
  protected readonly error = signal('');
  protected readonly info = signal('');
  protected readonly dashboard = signal<Dashboard | null>(null);
  protected readonly registrationPreview = signal<RegistrationPreview | null>(null);
  protected readonly setupToken = signal('');
  protected readonly searchTerm = signal('');
  protected readonly selectedPropertyId = signal<string | null>(null);
  protected readonly insightHidden = signal(false);
  protected readonly financePanelTab = signal<'entry' | 'history'>('entry');
  protected readonly editingTaskId = signal<string | null>(null);
  protected readonly quickAccess = signal<Section[]>(['tasks', 'documents']);
  private applyDefaultViewOnNextDashboard = false;

  protected readonly quickAccessOptions: Array<{ section: Section; label: string }> = [
    { section: 'tasks', label: 'Neue Aufgabe' },
    { section: 'documents', label: 'Dokument ablegen' },
    { section: 'finances', label: 'Buchung erfassen' },
    { section: 'communication', label: 'Mitteilung vorbereiten' },
    { section: 'settings', label: 'Nutzer & Rechte' }
  ];
  protected readonly configuredQuickActions = computed(() =>
    this.quickAccessOptions.filter(option => this.quickAccess().includes(option.section))
  );

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
  protected readonly openDecisions = computed(() => (this.dashboard()?.decisions ?? []).filter(decision => decision.status !== 'IMPLEMENTED' && decision.status !== 'REJECTED'));
  protected readonly primaryInsight = computed(() => this.insightHidden() ? null : this.dashboard()?.insights?.[0] ?? null);
  protected readonly filteredUnits = computed(() => (this.dashboard()?.units ?? []).filter(unit =>
    this.matchesSearch(unit.unitLabel, unit.ownerName, unit.ownerEmail, unit.shareValue, unit.occupancyType)
  ));
  protected readonly filteredMembers = computed(() => (this.dashboard()?.members ?? []).filter(member =>
    this.matchesSearch(member.fullName, member.email, member.role, member.status)
  ));
  protected readonly filteredTasks = computed(() => (this.dashboard()?.tasks ?? []).filter(task =>
    this.matchesSearch(task.title, task.description, task.priority, task.status, task.assigneeRole, task.sourceType, task.dueDate, task.reminderDate)
  ));
  protected readonly filteredFinances = computed(() => (this.dashboard()?.finances ?? []).filter(item =>
    this.matchesSearch(item.label, item.category, item.status, item.bookedOn, item.amount, item.eventType, item.allocationKey, item.ownerUnitLabel, item.counterparty, item.invoiceNumber)
  ));
  protected readonly filteredAssessments = computed(() => (this.dashboard()?.houseMoneyAssessments ?? []).filter(item =>
    this.matchesSearch(item.unitLabel, item.fiscalYear, item.status, item.monthlyHouseMoney, item.monthlyReserveContribution)
  ));
  protected readonly filteredUnitBalances = computed(() => (this.dashboard()?.unitBalances ?? []).filter(item =>
    this.matchesSearch(item.unitLabel, item.ownerName, item.expectedAnnual, item.paid, item.outstanding)
  ));
  protected readonly filteredAnnualPlans = computed(() => (this.dashboard()?.annualPlans ?? []).filter(plan =>
    this.matchesSearch(plan.fiscalYear, plan.status, plan.houseMoneyBudget, plan.maintenanceBudget, plan.reserveContribution)
  ));
  protected readonly filteredDocuments = computed(() => (this.dashboard()?.documents ?? []).filter(document =>
    this.matchesSearch(document.title, document.documentType, document.fileName, document.documentDate, document.status, document.visibility, document.source, document.description, document.linkedEntityType)
  ));
  protected readonly filteredDecisions = computed(() => (this.dashboard()?.decisions ?? []).filter(decision =>
    this.matchesSearch(decision.title, decision.resolutionText, decision.meetingLocation, decision.meetingTitle, decision.agendaItem, decision.responsibleRole, decision.costImpact, decision.status, decision.meetingDate)
  ));
  protected readonly filteredMeetings = computed(() => (this.dashboard()?.meetings ?? []).filter(meeting =>
    this.matchesSearch(meeting.title, meeting.location, meeting.agenda, meeting.quorumRequirement, meeting.responseDeadline, meeting.invitationSentOn, meeting.status, meeting.meetingDate)
  ));
  protected readonly filteredMessages = computed(() => (this.dashboard()?.messages ?? []).filter(message =>
    this.matchesSearch(message.audience, message.subject, message.message, message.status, message.channel, message.sourceType, message.followUpTaskTitle, message.readyToSendOn, message.createdAt)
  ));
  protected readonly filteredActivity = computed(() => (this.dashboard()?.activity ?? []).filter(event =>
    this.matchesSearch(event.eventType, event.summary, event.createdAt)
  ));
  protected readonly filteredAudit = computed(() => (this.dashboard()?.audit ?? []).filter(event =>
    this.matchesSearch(event.actorName, event.actorRole, event.action, event.targetType, event.summary, event.occurredAt)
  ));

  protected readonly registerForm = this.fb.nonNullable.group({
    fullName: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    organizationName: ['', [Validators.required, Validators.minLength(2)]]
  });

  protected readonly loginForm = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]]
  });

  protected readonly forgotForm = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]]
  });

  protected readonly passwordForm = this.fb.nonNullable.group({
    password: ['', [Validators.required, Validators.minLength(10)]]
  });

  protected readonly propertyForm = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(180)]],
    address: ['', [Validators.required, Validators.maxLength(240)]],
    city: ['', [Validators.required, Validators.maxLength(120)]],
    unitCount: [1, [Validators.required, Validators.min(1)]],
    fiscalYear: [new Date().getFullYear(), [Validators.required, Validators.min(2020)]],
    cashBalance: [0, [Validators.required, Validators.min(0)]],
    reserveBalance: [0, [Validators.required, Validators.min(0)]],
    reserveTarget: [0, [Validators.required, Validators.min(0)]],
    shareTotal: [1000, [Validators.required, Validators.min(1)]],
    managementMode: ['SELF_MANAGED' as ManagementMode, [Validators.required]]
  });

  protected readonly unitForm = this.fb.nonNullable.group({
    ownerName: ['', [Validators.required, Validators.maxLength(180)]],
    ownerEmail: ['', [Validators.required, Validators.email, Validators.maxLength(320)]],
    unitLabel: ['', [Validators.required, Validators.maxLength(80)]],
    shareValue: [0, [Validators.required, Validators.min(0)]],
    votingWeight: [0, [Validators.required, Validators.min(0)]],
    occupancyType: ['OWNER_OCCUPIED' as OccupancyType, [Validators.required]]
  });

  protected readonly memberForm = this.fb.nonNullable.group({
    fullName: ['', [Validators.required, Validators.maxLength(180)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(320)]],
    role: ['BOARD_MEMBER' as CommunityRole, [Validators.required]]
  });

  protected readonly taskForm = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(180)]],
    description: ['', [Validators.required, Validators.maxLength(1000)]],
    priority: ['MEDIUM' as TaskPriority, [Validators.required]],
    assigneeRole: ['Verwaltung', [Validators.required, Validators.maxLength(80)]],
    sourceType: ['MANUAL' as WorkContextType, [Validators.required]],
    sourceId: [''],
    dueDate: ['', [Validators.required, germanDateValidator]],
    reminderDate: ['', [germanDateValidator]],
    status: ['OPEN' as TaskStatus, [Validators.required]]
  });

  protected readonly financeForm = this.fb.nonNullable.group({
    label: ['', [Validators.required, Validators.maxLength(180)]],
    eventType: ['EXPENSE' as FinanceEventType, [Validators.required]],
    amount: [0, [Validators.required]],
    category: ['Instandhaltung', [Validators.required, Validators.maxLength(80)]],
    allocationKey: ['MEA' as AllocationKey, [Validators.required]],
    ownerUnitId: [''],
    bookedOn: [this.today(), [Validators.required, germanDateValidator]],
    dueDate: [''],
    paidOn: [''],
    counterparty: ['', [Validators.maxLength(180)]],
    invoiceNumber: ['', [Validators.maxLength(80)]],
    documentReference: ['', [Validators.maxLength(240)]],
    status: ['BOOKED', [Validators.required, Validators.maxLength(32)]]
  });

  protected readonly houseMoneyForm = this.fb.nonNullable.group({
    unitId: ['', [Validators.required]],
    fiscalYear: [new Date().getFullYear(), [Validators.required, Validators.min(2020)]],
    monthlyHouseMoney: [0, [Validators.required, Validators.min(0)]],
    monthlyReserveContribution: [0, [Validators.required, Validators.min(0)]],
    validFrom: [`01.01.${new Date().getFullYear()}`, [Validators.required, germanDateValidator]],
    status: ['ACTIVE' as AssessmentStatus, [Validators.required]]
  });

  protected readonly annualPlanForm = this.fb.nonNullable.group({
    fiscalYear: [new Date().getFullYear(), [Validators.required, Validators.min(2020)]],
    houseMoneyBudget: [0, [Validators.required, Validators.min(0)]],
    maintenanceBudget: [0, [Validators.required, Validators.min(0)]],
    reserveContribution: [0, [Validators.required, Validators.min(0)]],
    status: ['DRAFT' as AnnualPlanStatus, [Validators.required]]
  });

  protected readonly documentForm = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(180)]],
    documentType: ['Rechnung', [Validators.required, Validators.maxLength(80)]],
    fileName: ['', [Validators.required, Validators.maxLength(240)]],
    documentDate: [this.today(), [Validators.required, germanDateValidator]],
    status: ['RECEIVED' as DocumentStatus, [Validators.required]],
    visibility: ['ALL_OWNERS' as DocumentVisibility, [Validators.required]],
    source: ['UPLOAD', [Validators.required, Validators.maxLength(80)]],
    description: ['', [Validators.maxLength(1000)]],
    linkedEntityType: ['GENERAL' as DocumentLinkType, [Validators.required]],
    linkedEntityId: ['']
  });

  protected readonly decisionForm = this.fb.nonNullable.group({
    meetingId: [''],
    title: ['', [Validators.required, Validators.maxLength(180)]],
    resolutionText: ['', [Validators.required, Validators.maxLength(1600)]],
    meetingDate: [this.today(), [Validators.required, germanDateValidator]],
    meetingLocation: ['Eigentümerversammlung', [Validators.required, Validators.maxLength(180)]],
    agendaItem: ['TOP 1', [Validators.required, Validators.maxLength(240)]],
    implementationDueDate: ['', [germanDateValidator]],
    responsibleRole: ['Verwaltung', [Validators.required, Validators.maxLength(80)]],
    costImpact: [0, [Validators.required, Validators.min(0)]],
    status: ['PASSED' as DecisionStatus, [Validators.required]],
    yesVotes: [0, [Validators.required, Validators.min(0)]],
    noVotes: [0, [Validators.required, Validators.min(0)]],
    abstentions: [0, [Validators.required, Validators.min(0)]]
  });

  protected readonly meetingForm = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(180)]],
    meetingDate: [this.today(), [Validators.required, germanDateValidator]],
    location: ['', [Validators.required, Validators.maxLength(180)]],
    agenda: ['', [Validators.required, Validators.maxLength(1800)]],
    invitationSentOn: ['', [germanDateValidator]],
    responseDeadline: ['', [germanDateValidator]],
    quorumRequirement: ['Einfache Mehrheit nach MEA', [Validators.required, Validators.maxLength(240)]],
    status: ['SCHEDULED' as MeetingStatus, [Validators.required]]
  });

  protected readonly communicationForm = this.fb.nonNullable.group({
    audience: ['Eigentümer', [Validators.required]],
    subject: ['', [Validators.required, Validators.maxLength(180)]],
    message: ['', [Validators.required, Validators.maxLength(1200)]],
    status: ['READY_TO_SEND' as MessageStatus, [Validators.required]],
    channel: ['EMAIL' as MessageChannel, [Validators.required]],
    sourceType: ['MANUAL' as WorkContextType, [Validators.required]],
    sourceId: [''],
    readyToSendOn: [this.today(), [germanDateValidator]],
    createFollowUpTask: [true],
    followUpTitle: ['Rückfrage nachhalten', [Validators.maxLength(180)]],
    followUpDescription: ['Rückmeldung prüfen und nächsten Schritt dokumentieren.', [Validators.maxLength(1000)]],
    followUpPriority: ['MEDIUM' as TaskPriority, [Validators.required]],
    followUpAssigneeRole: ['Verwaltung', [Validators.maxLength(80)]],
    followUpDueDate: ['', [germanDateValidator]],
    followUpReminderDate: ['', [germanDateValidator]]
  });

  protected readonly settingsForm = this.fb.nonNullable.group({
    emailNotifications: [true],
    taskDigest: [true],
    decisionReminders: [true],
    defaultView: ['overview' as Section, [Validators.required]]
  });

  ngOnInit(): void {
    window.addEventListener('realestate:session-expired', this.sessionExpiredHandler);
    this.restoreLocalState();
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
      this.applyDefaultViewOnNextDashboard = true;
      this.loadDashboard();
      return;
    }
    const lastEmail = localStorage.getItem('realestate.lastEmail') ?? '';
    if (lastEmail) {
      this.loginForm.controls.email.setValue(lastEmail);
      this.forgotForm.controls.email.setValue(lastEmail);
      this.mode.set('login');
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
    localStorage.setItem('realestate.lastEmail', normalizedEmail);
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

  protected requestPasswordReset(): void {
    if (this.forgotForm.invalid) {
      this.error.set('Bitte E-Mail-Adresse eingeben.');
      return;
    }
    const normalizedEmail = this.normalizeEmail(this.forgotForm.controls.email.value);
    const suggestion = this.emailSuggestion(normalizedEmail);
    if (suggestion) {
      this.error.set(`Bitte E-Mail-Adresse prüfen. Meintest du ${suggestion}?`);
      return;
    }
    this.forgotForm.controls.email.setValue(normalizedEmail);
    localStorage.setItem('realestate.lastEmail', normalizedEmail);
    this.begin();
    this.http.post<RegistrationResult>(`${API_BASE_URL}/auth/password-reset`, this.forgotForm.getRawValue())
      .subscribe({
        next: result => {
          this.loading.set(false);
          this.info.set(result.emailSent
            ? 'Wenn ein Zugang existiert, wurde ein Link zum Passwortsetzen verschickt.'
            : `Lokaler Mailversand ist deaktiviert. Reset-Link: ${result.localSetupLink}`);
          if (result.localSetupLink) {
            const token = new URL(result.localSetupLink).searchParams.get('token') ?? '';
            this.setupToken.set(token);
            this.mode.set('password');
            this.previewRegistration(token);
          } else {
            this.mode.set('login');
            this.loginForm.controls.email.setValue(normalizedEmail);
          }
        },
        error: error => this.fail(error)
      });
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
    const formValue = this.unitForm.getRawValue();
    this.submitDashboardRequest('units', {
      ...formValue,
      ownerEmail: this.normalizeEmail(formValue.ownerEmail),
      propertyId
    }, 'Einheit und Eigentümerrolle wurden angelegt.');
  }

  protected inviteMember(): void {
    if (this.memberForm.invalid) return;
    const propertyId = this.requireSelectedPropertyId();
    if (!propertyId) return;
    const formValue = this.memberForm.getRawValue();
    this.submitDashboardRequest('members', {
      ...formValue,
      email: this.normalizeEmail(formValue.email),
      propertyId
    }, 'Rolle wurde eingeladen und dokumentiert.');
  }

  protected addTask(): void {
    if (this.taskForm.invalid) return;
    const propertyId = this.requireSelectedPropertyId();
    if (!propertyId) return;
    const formValue = this.taskForm.getRawValue();
    if (formValue.sourceType !== 'MANUAL' && !formValue.sourceId) {
      this.error.set('Bitte Zielobjekt für die Aufgabe auswählen.');
      this.info.set('');
      return;
    }
    const taskPayload = {
      title: formValue.title,
      description: formValue.description,
      priority: formValue.priority,
      assigneeRole: formValue.assigneeRole,
      sourceType: formValue.sourceType,
      sourceId: formValue.sourceType === 'MANUAL' ? null : formValue.sourceId || null,
      dueDate: this.toIsoDate(formValue.dueDate),
      reminderDate: formValue.reminderDate ? this.toIsoDate(formValue.reminderDate) : null,
      status: formValue.status
    };
    if (this.editingTaskId()) {
      this.begin();
      this.http.put<Dashboard>(`${API_BASE_URL}/workspace/tasks/${this.editingTaskId()}`, taskPayload)
        .subscribe({
          next: dashboard => {
            this.loading.set(false);
            this.editingTaskId.set(null);
            this.applyDashboard(dashboard);
            this.resetTaskForm();
            this.info.set('Aufgabe wurde gespeichert.');
          },
          error: error => this.fail(error)
      });
      return;
    }
    const { status: _status, ...createPayload } = taskPayload;
    this.submitDashboardRequest('tasks', { ...createPayload, propertyId }, 'Aufgabe wurde erstellt und protokolliert.');
  }

  protected createFinance(): void {
    if (this.financeForm.invalid) return;
    const propertyId = this.requireSelectedPropertyId();
    if (!propertyId) return;
    const formValue = this.financeForm.getRawValue();
    this.submitDashboardRequest('finances', {
      ...formValue,
      ownerUnitId: formValue.ownerUnitId || null,
      bookedOn: this.toIsoDate(formValue.bookedOn),
      dueDate: formValue.dueDate ? this.toIsoDate(formValue.dueDate) : null,
      paidOn: formValue.paidOn ? this.toIsoDate(formValue.paidOn) : null,
      propertyId
    }, 'Finanzereignis wurde mit Belegkette erfasst.');
  }

  protected createHouseMoneyAssessment(): void {
    if (this.houseMoneyForm.invalid) return;
    const propertyId = this.requireSelectedPropertyId();
    if (!propertyId) return;
    const formValue = this.houseMoneyForm.getRawValue();
    this.submitDashboardRequest('house-money', {
      ...formValue,
      validFrom: this.toIsoDate(formValue.validFrom),
      propertyId
    }, 'Hausgeld-Soll wurde für die Einheit angelegt.');
  }

  protected createAnnualPlan(): void {
    if (this.annualPlanForm.invalid) return;
    const propertyId = this.requireSelectedPropertyId();
    if (!propertyId) return;
    this.submitDashboardRequest('annual-plans', { ...this.annualPlanForm.getRawValue(), propertyId }, 'Wirtschaftsplan wurde angelegt.');
  }

  protected createDocument(): void {
    if (this.documentForm.invalid) return;
    const propertyId = this.requireSelectedPropertyId();
    if (!propertyId) return;
    const formValue = this.documentForm.getRawValue();
    if (formValue.linkedEntityType !== 'GENERAL' && !formValue.linkedEntityId) {
      this.error.set('Bitte Zielobjekt für das Dokument auswählen.');
      this.info.set('');
      return;
    }
    this.submitDashboardRequest('documents', {
      ...formValue,
      documentDate: this.toIsoDate(formValue.documentDate),
      linkedEntityId: formValue.linkedEntityType === 'GENERAL' ? null : formValue.linkedEntityId || null,
      propertyId
    }, 'Dokument wurde mit Kontext abgelegt.');
  }

  protected createDecision(): void {
    if (this.decisionForm.invalid) return;
    const propertyId = this.requireSelectedPropertyId();
    if (!propertyId) return;
    const formValue = this.decisionForm.getRawValue();
    this.submitDashboardRequest('decisions', {
      ...formValue,
      meetingId: formValue.meetingId || null,
      meetingDate: this.toIsoDate(formValue.meetingDate),
      implementationDueDate: formValue.implementationDueDate ? this.toIsoDate(formValue.implementationDueDate) : null,
      propertyId
    }, 'Beschluss wurde in die Sammlung aufgenommen.');
  }

  protected createMeeting(): void {
    if (this.meetingForm.invalid) return;
    const propertyId = this.requireSelectedPropertyId();
    if (!propertyId) return;
    const formValue = this.meetingForm.getRawValue();
    this.submitDashboardRequest('meetings', {
      ...formValue,
      meetingDate: this.toIsoDate(formValue.meetingDate),
      invitationSentOn: formValue.invitationSentOn ? this.toIsoDate(formValue.invitationSentOn) : null,
      responseDeadline: formValue.responseDeadline ? this.toIsoDate(formValue.responseDeadline) : null,
      propertyId
    }, 'Eigentümerversammlung wurde vorbereitet.');
  }

  protected logout(): void {
    localStorage.removeItem('realestate.token');
    this.dashboard.set(null);
    this.selectedPropertyId.set(null);
    this.mode.set('login');
    history.replaceState({}, '', '/');
  }

  protected switchMode(mode: 'register' | 'login' | 'forgot'): void {
    this.error.set('');
    this.info.set('');
    if (mode === 'forgot') {
      this.forgotForm.controls.email.setValue(this.loginForm.controls.email.value || localStorage.getItem('realestate.lastEmail') || '');
    }
    if (mode === 'login') {
      this.loginForm.controls.email.setValue(this.forgotForm.controls.email.value || this.loginForm.controls.email.value);
    }
    this.mode.set(mode);
  }

  protected selectSection(section: Section): void {
    this.activeSection.set(section);
    this.error.set('');
    this.info.set('');
  }

  protected selectFinancePanelTab(tab: 'entry' | 'history'): void {
    this.financePanelTab.set(tab);
  }

  protected dismissInsight(): void {
    this.insightHidden.set(true);
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

  protected propertyOpenAmount(propertyId: string): number {
    return propertyId === this.selectedProperty()?.id ? this.openInvoices().reduce((sum, item) => sum + Math.abs(item.amount), 0) : 0;
  }

  protected propertyOpenCount(propertyId: string): number {
    return propertyId === this.selectedProperty()?.id ? this.openInvoices().length : 0;
  }

  protected activityLabel(eventType: string): string {
    return {
      PROPERTY: 'Immobilie',
      UNIT: 'Einheit',
      TASK: 'Aufgabe',
      FINANCE: 'Finanzen',
      DOCUMENT: 'Dokument',
      DECISION: 'Beschluss',
      PLAN: 'Wirtschaftsplan',
      MEETING: 'Versammlung',
      COMMUNICATION: 'Kommunikation',
      MEMBER: 'Rolle'
    }[eventType] ?? 'Aktivität';
  }

  protected sendCommunication(): void {
    if (this.communicationForm.invalid) {
      this.error.set('Bitte Empfänger, Betreff und Nachricht ausfüllen.');
      return;
    }
    const propertyId = this.requireSelectedPropertyId();
    if (!propertyId) return;
    const formValue = this.communicationForm.getRawValue();
    if (formValue.sourceType !== 'MANUAL' && !formValue.sourceId) {
      this.error.set('Bitte Zielobjekt für die Mitteilung auswählen.');
      this.info.set('');
      return;
    }
    if (formValue.createFollowUpTask && (!formValue.followUpTitle || !formValue.followUpDescription || !formValue.followUpAssigneeRole || !formValue.followUpDueDate)) {
      this.error.set('Bitte Folgeaufgabe mit Titel, Beschreibung, Verantwortlichkeit und Fälligkeit ausfüllen.');
      this.info.set('');
      return;
    }
    this.submitDashboardRequest('messages', {
      ...formValue,
      sourceId: formValue.sourceType === 'MANUAL' ? null : formValue.sourceId || null,
      readyToSendOn: formValue.readyToSendOn ? this.toIsoDate(formValue.readyToSendOn) : null,
      followUpDueDate: formValue.followUpDueDate ? this.toIsoDate(formValue.followUpDueDate) : null,
      followUpReminderDate: formValue.followUpReminderDate ? this.toIsoDate(formValue.followUpReminderDate) : null,
      propertyId
    }, formValue.createFollowUpTask ? 'Mitteilung und Folgeaufgabe wurden vorbereitet.' : 'Mitteilung wurde vorbereitet.');
  }

  protected saveSettings(): void {
    const email = this.preferenceEmail();
    localStorage.setItem(this.preferenceKey('workspaceSettings', email), JSON.stringify(this.settingsForm.getRawValue()));
    localStorage.setItem(this.preferenceKey('quickAccess', email), JSON.stringify(this.quickAccess()));
    this.info.set('Einstellungen wurden gespeichert.');
    this.error.set('');
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

  protected updateTaskStatusFromSelect(task: WorkTaskView, event: Event): void {
    this.updateTaskStatus(task, (event.target as HTMLSelectElement).value as TaskStatus);
  }

  protected editTask(task: WorkTaskView): void {
    this.editingTaskId.set(task.id);
    this.taskForm.reset({
      title: task.title,
      description: task.description,
      priority: task.priority,
      assigneeRole: task.assigneeRole,
      sourceType: task.sourceType,
      sourceId: task.sourceId ?? '',
      dueDate: task.dueDate ? this.toGermanDate(task.dueDate) : '',
      reminderDate: task.reminderDate ? this.toGermanDate(task.reminderDate) : '',
      status: task.status
    });
    this.activeSection.set('tasks');
    this.info.set('Aufgabe kann jetzt bearbeitet werden.');
    this.error.set('');
  }

  protected cancelTaskEdit(): void {
    this.editingTaskId.set(null);
    this.resetTaskForm();
    this.info.set('');
    this.error.set('');
  }

  protected deleteTask(task: WorkTaskView): void {
    if (!confirm(`Aufgabe "${task.title}" wirklich löschen?`)) return;
    this.begin();
    this.http.delete<Dashboard>(`${API_BASE_URL}/workspace/tasks/${task.id}`)
      .subscribe({
        next: dashboard => {
          this.loading.set(false);
          if (this.editingTaskId() === task.id) this.editingTaskId.set(null);
          this.applyDashboard(dashboard);
          this.resetTaskForm();
          this.info.set('Aufgabe wurde gelöscht.');
        },
        error: error => this.fail(error)
      });
  }

  protected updateMemberRole(member: MemberView, event: Event): void {
    this.updateMember(member, { role: (event.target as HTMLSelectElement).value as CommunityRole });
  }

  protected updateMemberStatus(member: MemberView, event: Event): void {
    this.updateMember(member, { status: (event.target as HTMLSelectElement).value as MemberStatus });
  }

  protected disableMember(member: MemberView): void {
    if (!confirm(`Zugriff für ${member.fullName} deaktivieren?`)) return;
    this.begin();
    this.http.delete<Dashboard>(`${API_BASE_URL}/workspace/members/${member.id}`)
      .subscribe({
        next: dashboard => {
          this.loading.set(false);
          this.applyDashboard(dashboard);
          this.info.set('Rolle wurde deaktiviert.');
        },
        error: error => this.fail(error)
      });
  }

  protected isQuickActionEnabled(section: Section): boolean {
    return this.quickAccess().includes(section);
  }

  protected toggleQuickAction(section: Section, event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    const current = this.quickAccess();
    const next = checked
      ? [...new Set([...current, section])]
      : current.filter(item => item !== section);
    this.quickAccess.set(next.length ? next : ['tasks']);
  }

  protected updateDecisionStatus(decision: DecisionView, status: DecisionStatus): void {
    this.begin();
    this.http.patch<Dashboard>(`${API_BASE_URL}/workspace/decisions/${decision.id}/status`, { status })
      .subscribe({
        next: dashboard => {
          this.loading.set(false);
          this.applyDashboard(dashboard);
          this.info.set(status === 'IMPLEMENTED' ? 'Beschluss als umgesetzt markiert.' : 'Beschlussstatus aktualisiert.');
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
      DONE: 'Erledigt',
      DRAFT: 'Entwurf',
      PASSED: 'Beschlossen',
      REJECTED: 'Abgelehnt',
      IMPLEMENTED: 'Umgesetzt',
      APPROVED: 'Freigegeben',
      ACTIVE: 'Aktiv',
      SUPERSEDED: 'Ersetzt',
      SCHEDULED: 'Geplant',
      INVITED: 'Eingeladen',
      COMPLETED: 'Abgeschlossen',
      PREPARED: 'Vorbereitet',
      SELF_MANAGED: 'Selbstverwaltung',
      HYBRID: 'Hybrid',
      PROFESSIONAL: 'Professionell',
      OWNER_ADMIN: 'WEG-Admin',
      SELF_MANAGER: 'Selbstverwalter',
      PROPERTY_MANAGER: 'Verwaltung',
      BOARD_MEMBER: 'Beirat',
      OWNER: 'Eigentümer',
      EXTERNAL_EXPERT: 'Externer Experte',
      OWNER_OCCUPIED: 'Selbst genutzt',
      RENTED: 'Vermietet',
      VACANT: 'Leerstand',
      DISABLED: 'Deaktiviert',
      HOUSE_MONEY_CHARGE: 'Hausgeld-Soll',
      OWNER_PAYMENT: 'Eigentümerzahlung',
      EXPENSE: 'Ausgabe',
      RESERVE_TRANSFER: 'Rücklagenbewegung',
      SPECIAL_ASSESSMENT: 'Sonderumlage',
      REFUND: 'Erstattung',
      MEA: 'MEA',
      UNIT: 'Einheit',
      CONSUMPTION: 'Verbrauch',
      EQUAL: 'Gleich verteilt',
      DIRECT: 'Direkt',
      RECEIVED: 'Eingegangen',
      ARCHIVED: 'Archiviert',
      ALL_OWNERS: 'Alle Eigentümer',
      BOARD_ONLY: 'Beirat',
      MANAGEMENT_ONLY: 'Verwaltung',
      PRIVATE: 'Privat',
      GENERAL: 'Allgemein',
      FINANCE: 'Finanzen',
      DECISION: 'Beschluss',
      MEETING: 'Versammlung',
      UPLOAD: 'Upload',
      EMAIL: 'E-Mail',
      SCAN: 'Scan',
      MANUAL: 'Manuell',
      DOCUMENT: 'Dokument',
      READY_TO_SEND: 'Versandbereit',
      SENT: 'Versendet',
      PORTAL: 'Portal'
    }[status] ?? status;
  }

  protected clearTaskSource(): void {
    this.taskForm.controls.sourceId.setValue('');
  }

  protected clearMessageSource(): void {
    this.communicationForm.controls.sourceId.setValue('');
  }

  protected taskRequiresSource(): boolean {
    return this.taskForm.controls.sourceType.value !== 'MANUAL';
  }

  protected messageRequiresSource(): boolean {
    return this.communicationForm.controls.sourceType.value !== 'MANUAL';
  }

  protected workContextOptions(sourceType: WorkContextType): Array<{ id: string; label: string }> {
    const dashboard = this.dashboard();
    if (!dashboard) return [];
    switch (sourceType) {
      case 'FINANCE':
        return dashboard.finances.map(item => ({ id: item.id, label: `${item.label} · ${this.currency(item.amount)}` }));
      case 'DOCUMENT':
        return dashboard.documents.map(item => ({ id: item.id, label: item.title }));
      case 'DECISION':
        return dashboard.decisions.map(item => ({ id: item.id, label: item.title }));
      case 'MEETING':
        return dashboard.meetings.map(item => ({ id: item.id, label: item.title }));
      default:
        return [];
    }
  }

  protected workContextLabel(sourceType: WorkContextType, sourceId?: string): string {
    if (sourceType === 'MANUAL' || !sourceId) return 'Manuell';
    const match = this.workContextOptions(sourceType).find(item => item.id === sourceId);
    return match?.label ?? this.statusLabel(sourceType);
  }

  protected clearDocumentLink(): void {
    this.documentForm.controls.linkedEntityId.setValue('');
  }

  protected documentRequiresLink(): boolean {
    return this.documentForm.controls.linkedEntityType.value !== 'GENERAL';
  }

  protected documentLinkOptions(): Array<{ id: string; label: string }> {
    const dashboard = this.dashboard();
    if (!dashboard) return [];
    switch (this.documentForm.controls.linkedEntityType.value) {
      case 'FINANCE':
        return dashboard.finances.map(item => ({ id: item.id, label: `${item.label} · ${this.currency(item.amount)}` }));
      case 'DECISION':
        return dashboard.decisions.map(item => ({ id: item.id, label: item.title }));
      case 'MEETING':
        return dashboard.meetings.map(item => ({ id: item.id, label: item.title }));
      default:
        return [];
    }
  }

  protected documentLinkLabel(document: DocumentView): string {
    if (document.linkedEntityType === 'GENERAL' || !document.linkedEntityId) return 'Allgemein';
    const dashboard = this.dashboard();
    if (!dashboard) return this.statusLabel(document.linkedEntityType);
    if (document.linkedEntityType === 'FINANCE') {
      return dashboard.finances.find(item => item.id === document.linkedEntityId)?.label ?? 'Finanzereignis';
    }
    if (document.linkedEntityType === 'DECISION') {
      return dashboard.decisions.find(item => item.id === document.linkedEntityId)?.title ?? 'Beschluss';
    }
    if (document.linkedEntityType === 'MEETING') {
      return dashboard.meetings.find(item => item.id === document.linkedEntityId)?.title ?? 'Versammlung';
    }
    return this.statusLabel(document.linkedEntityType);
  }

  protected insightSeverityLabel(severity: string): string {
    return {
      HIGH: 'Priorität',
      MEDIUM: 'Nächster Schritt',
      LOW: 'Verbesserung',
      GOOD: 'Stabil'
    }[severity] ?? severity;
  }

  private submitDashboardRequest(path: 'properties' | 'units' | 'members' | 'tasks' | 'finances' | 'house-money' | 'annual-plans' | 'documents' | 'meetings' | 'decisions' | 'messages', payload: Record<string, unknown>, success: string): void {
    this.begin();
    this.http.post<Dashboard>(`${API_BASE_URL}/workspace/${path}`, payload)
      .subscribe({
        next: dashboard => {
          this.loading.set(false);
          this.applyDashboard(dashboard);
          this.info.set(success);
          if (path === 'properties') this.propertyForm.reset({ name: '', address: '', city: '', unitCount: 1, fiscalYear: new Date().getFullYear(), cashBalance: 0, reserveBalance: 0, reserveTarget: 0, shareTotal: 1000, managementMode: 'SELF_MANAGED' });
          if (path === 'units') this.unitForm.reset({ ownerName: '', ownerEmail: '', unitLabel: '', shareValue: 0, votingWeight: 0, occupancyType: 'OWNER_OCCUPIED' });
          if (path === 'members') this.memberForm.reset({ fullName: '', email: '', role: 'BOARD_MEMBER' });
          if (path === 'tasks') this.resetTaskForm();
          if (path === 'finances') {
            this.financeForm.reset({ label: '', eventType: 'EXPENSE', amount: 0, category: 'Instandhaltung', allocationKey: 'MEA', ownerUnitId: '', bookedOn: this.today(), dueDate: '', paidOn: '', counterparty: '', invoiceNumber: '', documentReference: '', status: 'BOOKED' });
            this.financePanelTab.set('history');
          }
          if (path === 'house-money') this.houseMoneyForm.reset({ unitId: '', fiscalYear: new Date().getFullYear(), monthlyHouseMoney: 0, monthlyReserveContribution: 0, validFrom: `01.01.${new Date().getFullYear()}`, status: 'ACTIVE' });
          if (path === 'annual-plans') this.annualPlanForm.reset({ fiscalYear: new Date().getFullYear(), houseMoneyBudget: 0, maintenanceBudget: 0, reserveContribution: 0, status: 'DRAFT' });
          if (path === 'documents') this.documentForm.reset({ title: '', documentType: 'Rechnung', fileName: '', documentDate: this.today(), status: 'RECEIVED', visibility: 'ALL_OWNERS', source: 'UPLOAD', description: '', linkedEntityType: 'GENERAL', linkedEntityId: '' });
          if (path === 'meetings') this.meetingForm.reset({ title: '', meetingDate: this.today(), location: '', agenda: '', invitationSentOn: '', responseDeadline: '', quorumRequirement: 'Einfache Mehrheit nach MEA', status: 'SCHEDULED' });
          if (path === 'decisions') this.decisionForm.reset({ meetingId: '', title: '', resolutionText: '', meetingDate: this.today(), meetingLocation: 'Eigentümerversammlung', agendaItem: 'TOP 1', implementationDueDate: '', responsibleRole: 'Verwaltung', costImpact: 0, status: 'PASSED', yesVotes: 0, noVotes: 0, abstentions: 0 });
          if (path === 'messages') this.communicationForm.reset({ audience: 'Eigentümer', subject: '', message: '', status: 'READY_TO_SEND', channel: 'EMAIL', sourceType: 'MANUAL', sourceId: '', readyToSendOn: this.today(), createFollowUpTask: true, followUpTitle: 'Rückfrage nachhalten', followUpDescription: 'Rückmeldung prüfen und nächsten Schritt dokumentieren.', followUpPriority: 'MEDIUM', followUpAssigneeRole: 'Verwaltung', followUpDueDate: '', followUpReminderDate: '' });
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
    localStorage.setItem('realestate.lastEmail', session.user.email);
    this.loading.set(false);
    this.info.set(`Willkommen, ${session.user.fullName}.`);
    this.mode.set('dashboard');
    this.activeSection.set('overview');
    this.applyDefaultViewOnNextDashboard = true;
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
    this.loadUserPreferences(dashboard.user.email);
    if (this.applyDefaultViewOnNextDashboard) {
      this.activeSection.set(this.settingsForm.controls.defaultView.value as Section);
      this.applyDefaultViewOnNextDashboard = false;
    }
  }

  private restoreLocalState(): void {
    this.loadUserPreferences(localStorage.getItem('realestate.lastEmail') ?? 'default');
  }

  private loadUserPreferences(email: string): void {
    const settings = localStorage.getItem(this.preferenceKey('workspaceSettings', email)) ?? localStorage.getItem('realestate.workspaceSettings');
    if (settings) {
      try {
        this.settingsForm.patchValue(JSON.parse(settings));
      } catch {
        localStorage.removeItem(this.preferenceKey('workspaceSettings', email));
      }
    }
    const quickAccess = localStorage.getItem(this.preferenceKey('quickAccess', email));
    if (quickAccess) {
      try {
        const parsed = JSON.parse(quickAccess) as Section[];
        const allowed = parsed.filter(section => this.quickAccessOptions.some(option => option.section === section));
        if (allowed.length) this.quickAccess.set(allowed);
      } catch {
        localStorage.removeItem(this.preferenceKey('quickAccess', email));
      }
    }
  }

  private updateMember(member: MemberView, changes: Partial<Pick<MemberView, 'role' | 'status'>>): void {
    this.begin();
    this.http.patch<Dashboard>(`${API_BASE_URL}/workspace/members/${member.id}`, {
      fullName: member.fullName,
      email: member.email,
      role: changes.role ?? member.role,
      status: changes.status ?? member.status
    }).subscribe({
      next: dashboard => {
        this.loading.set(false);
        this.applyDashboard(dashboard);
        this.info.set('Rolle wurde aktualisiert.');
      },
      error: error => this.fail(error)
    });
  }

  private resetTaskForm(): void {
    this.taskForm.reset({
      title: '',
      description: '',
      priority: 'MEDIUM',
      assigneeRole: 'Verwaltung',
      sourceType: 'MANUAL',
      sourceId: '',
      dueDate: '',
      reminderDate: '',
      status: 'OPEN'
    });
  }

  private preferenceEmail(): string {
    return this.dashboard()?.user.email ?? localStorage.getItem('realestate.lastEmail') ?? 'default';
  }

  private preferenceKey(kind: 'workspaceSettings' | 'quickAccess', email: string): string {
    return `realestate.${kind}.${email}`;
  }

  private matchesSearch(...values: Array<unknown>): boolean {
    const term = this.searchTerm().trim().toLowerCase();
    if (!term) return true;
    return values.filter(value => value !== undefined && value !== null).join(' ').toLowerCase().includes(term);
  }

  private currency(amount: number): string {
    return new Intl.NumberFormat('de-DE', { style: 'currency', currency: 'EUR' }).format(amount);
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

  private toGermanDate(value: string): string {
    const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(value);
    if (!match) return value;
    const [, year, month, day] = match;
    return `${day}.${month}.${year}`;
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

type Section = 'overview' | 'properties' | 'units' | 'finances' | 'tasks' | 'documents' | 'decisions' | 'activity' | 'communication' | 'settings';
type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
type TaskStatus = 'OPEN' | 'IN_REVIEW' | 'DONE';
type WorkContextType = 'MANUAL' | 'FINANCE' | 'DOCUMENT' | 'DECISION' | 'MEETING';
type MessageStatus = 'PREPARED' | 'READY_TO_SEND' | 'SENT';
type MessageChannel = 'EMAIL' | 'PORTAL';
type DecisionStatus = 'DRAFT' | 'PASSED' | 'REJECTED' | 'IMPLEMENTED';
type AnnualPlanStatus = 'DRAFT' | 'APPROVED' | 'ACTIVE';
type MeetingStatus = 'SCHEDULED' | 'INVITED' | 'COMPLETED';
type InsightSeverity = 'HIGH' | 'MEDIUM' | 'LOW' | 'GOOD';
type ManagementMode = 'SELF_MANAGED' | 'HYBRID' | 'PROFESSIONAL';
type OccupancyType = 'OWNER_OCCUPIED' | 'RENTED' | 'VACANT';
type CommunityRole = 'OWNER_ADMIN' | 'SELF_MANAGER' | 'PROPERTY_MANAGER' | 'BOARD_MEMBER' | 'OWNER' | 'EXTERNAL_EXPERT';
type MemberStatus = 'ACTIVE' | 'INVITED' | 'DISABLED';
type FinanceEventType = 'HOUSE_MONEY_CHARGE' | 'OWNER_PAYMENT' | 'EXPENSE' | 'RESERVE_TRANSFER' | 'SPECIAL_ASSESSMENT' | 'REFUND';
type AllocationKey = 'MEA' | 'UNIT' | 'CONSUMPTION' | 'EQUAL' | 'DIRECT';
type AssessmentStatus = 'DRAFT' | 'ACTIVE' | 'SUPERSEDED';
type DocumentStatus = 'RECEIVED' | 'IN_REVIEW' | 'APPROVED' | 'ARCHIVED';
type DocumentVisibility = 'ALL_OWNERS' | 'BOARD_ONLY' | 'MANAGEMENT_ONLY' | 'PRIVATE';
type DocumentLinkType = 'GENERAL' | 'FINANCE' | 'DECISION' | 'MEETING';

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
    fiscalYear: number;
    cashBalance: number;
    reserveBalance: number;
    reserveTarget: number;
    shareTotal: number;
    managementMode: ManagementMode;
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
  units: UnitView[];
  tasks: WorkTaskView[];
  finances: FinanceView[];
  houseMoneyAssessments: AssessmentView[];
  unitBalances: UnitBalanceView[];
  annualPlans: AnnualPlanView[];
  documents: DocumentView[];
  decisions: DecisionView[];
  meetings: MeetingView[];
  messages: MessageView[];
  members: MemberView[];
  readiness: {
    shareValueTotal: number;
    missingShareValue: number;
    shareDistributionComplete: boolean;
    expectedShareTotal: number;
    expectedUnits: number;
    createdUnits: number;
    invitedMembers: number;
    activeMembers: number;
    rolesReady: boolean;
    readyForFinance: boolean;
    blockers: string[];
  };
  access: {
    role: CommunityRole;
    canAdmin: boolean;
    canCollaborate: boolean;
    allowedCommands: string[];
  };
  audit: AuditView[];
  activity: Array<{ eventType: string; summary: string; createdAt: string }>;
  insights: InsightView[];
  onboarding: {
    completion: number;
    accountActivated: boolean;
    propertyCreated: boolean;
    unitsCreated: boolean;
    sharesComplete: boolean;
    rolesInvited: boolean;
    financeCreated: boolean;
    taskCreated: boolean;
    decisionCreated: boolean;
    annualPlanCreated: boolean;
    meetingCreated: boolean;
  };
}

interface UnitView {
  id: string;
  ownerName: string;
  ownerEmail: string;
  unitLabel: string;
  shareValue: number;
  votingWeight: number;
  occupancyType: OccupancyType;
}

interface MemberView {
  id: string;
  fullName: string;
  email: string;
  role: CommunityRole;
  status: MemberStatus;
  invitedAt?: string;
  acceptedAt?: string;
}

interface FinanceView {
  id: string;
  label: string;
  eventType: FinanceEventType;
  amount: number;
  category: string;
  allocationKey: AllocationKey;
  ownerUnitId?: string;
  ownerUnitLabel?: string;
  bookedOn: string;
  dueDate?: string;
  paidOn?: string;
  counterparty?: string;
  invoiceNumber?: string;
  documentReference?: string;
  status: string;
}

interface AssessmentView {
  id: string;
  unitId: string;
  unitLabel: string;
  fiscalYear: number;
  monthlyHouseMoney: number;
  monthlyReserveContribution: number;
  validFrom: string;
  status: AssessmentStatus;
}

interface UnitBalanceView {
  unitId: string;
  unitLabel: string;
  ownerName: string;
  expectedAnnual: number;
  paid: number;
  outstanding: number;
}

interface DocumentView {
  id: string;
  title: string;
  documentType: string;
  fileName: string;
  documentDate: string;
  status: DocumentStatus;
  visibility: DocumentVisibility;
  source: string;
  description?: string;
  linkedEntityType: DocumentLinkType;
  linkedEntityId?: string;
}

interface WorkTaskView {
  id: string;
  title: string;
  description: string;
  status: TaskStatus;
  priority: TaskPriority;
  assigneeRole: string;
  sourceType: WorkContextType;
  sourceId?: string;
  dueDate?: string;
  reminderDate?: string;
  completedAt?: string;
}

interface InsightView {
  severity: InsightSeverity;
  title: string;
  description: string;
  actionSection: Section;
  actionLabel: string;
}

interface DecisionView {
  id: string;
  title: string;
  resolutionText: string;
  meetingDate: string;
  meetingLocation: string;
  meetingId?: string;
  meetingTitle?: string;
  agendaItem: string;
  implementationDueDate?: string;
  responsibleRole: string;
  costImpact: number;
  status: DecisionStatus;
  yesVotes: number;
  noVotes: number;
  abstentions: number;
}

interface AnnualPlanView {
  id: string;
  fiscalYear: number;
  houseMoneyBudget: number;
  maintenanceBudget: number;
  reserveContribution: number;
  status: AnnualPlanStatus;
}

interface MeetingView {
  id: string;
  title: string;
  meetingDate: string;
  location: string;
  agenda: string;
  invitationSentOn?: string;
  responseDeadline?: string;
  quorumRequirement: string;
  status: MeetingStatus;
}

interface MessageView {
  id: string;
  audience: string;
  subject: string;
  message: string;
  status: MessageStatus;
  channel: MessageChannel;
  sourceType: WorkContextType;
  sourceId?: string;
  followUpTaskId?: string;
  followUpTaskTitle?: string;
  readyToSendOn?: string;
  createdAt: string;
  sentAt?: string;
}

interface AuditView {
  actorName: string;
  actorRole: CommunityRole;
  action: string;
  targetType: string;
  targetId?: string;
  summary: string;
  occurredAt: string;
}
