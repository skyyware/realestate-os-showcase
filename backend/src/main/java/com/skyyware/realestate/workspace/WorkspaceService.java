package com.skyyware.realestate.workspace;

import com.skyyware.realestate.activity.ActivityEvent;
import com.skyyware.realestate.activity.ActivityEventRepository;
import com.skyyware.realestate.audit.AuditService;
import com.skyyware.realestate.communication.CommunityMessage;
import com.skyyware.realestate.communication.CommunityMessageRepository;
import com.skyyware.realestate.decision.CommunityDecision;
import com.skyyware.realestate.decision.CommunityDecisionRepository;
import com.skyyware.realestate.decision.DecisionStatus;
import com.skyyware.realestate.document.PropertyDocument;
import com.skyyware.realestate.document.PropertyDocumentRepository;
import com.skyyware.realestate.finance.FinanceEvent;
import com.skyyware.realestate.finance.FinanceEventRepository;
import com.skyyware.realestate.identity.AppUser;
import com.skyyware.realestate.identity.AppUserRepository;
import com.skyyware.realestate.meeting.MeetingStatus;
import com.skyyware.realestate.meeting.OwnerMeeting;
import com.skyyware.realestate.meeting.OwnerMeetingRepository;
import com.skyyware.realestate.planning.AnnualPlan;
import com.skyyware.realestate.planning.AnnualPlanRepository;
import com.skyyware.realestate.planning.AnnualPlanStatus;
import com.skyyware.realestate.property.OwnerUnit;
import com.skyyware.realestate.property.OwnerUnitRepository;
import com.skyyware.realestate.property.PropertyAsset;
import com.skyyware.realestate.property.PropertyAssetRepository;
import com.skyyware.realestate.task.TaskPriority;
import com.skyyware.realestate.task.TaskStatus;
import com.skyyware.realestate.task.WorkTask;
import com.skyyware.realestate.task.WorkTaskRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkspaceService {
    private final AppUserRepository users;
    private final PropertyAssetRepository properties;
    private final OwnerUnitRepository units;
    private final WorkTaskRepository tasks;
    private final FinanceEventRepository finances;
    private final ActivityEventRepository activities;
    private final PropertyDocumentRepository documents;
    private final CommunityDecisionRepository decisions;
    private final AnnualPlanRepository annualPlans;
    private final OwnerMeetingRepository ownerMeetings;
    private final CommunityMessageRepository messages;
    private final AuditService audit;

    public WorkspaceService(
            AppUserRepository users,
            PropertyAssetRepository properties,
            OwnerUnitRepository units,
            WorkTaskRepository tasks,
            FinanceEventRepository finances,
            ActivityEventRepository activities,
            PropertyDocumentRepository documents,
            CommunityDecisionRepository decisions,
            AnnualPlanRepository annualPlans,
            OwnerMeetingRepository ownerMeetings,
            CommunityMessageRepository messages,
            AuditService audit
    ) {
        this.users = users;
        this.properties = properties;
        this.units = units;
        this.tasks = tasks;
        this.finances = finances;
        this.activities = activities;
        this.documents = documents;
        this.decisions = decisions;
        this.annualPlans = annualPlans;
        this.ownerMeetings = ownerMeetings;
        this.messages = messages;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public DashboardView dashboard(UUID userId) {
        return dashboard(userId, null);
    }

    @Transactional(readOnly = true)
    public DashboardView dashboard(UUID userId, UUID selectedPropertyId) {
        AppUser user = user(userId);
        List<PropertyAsset> assets = properties.findByOwnerOrderByCreatedAtAsc(user);
        List<ActivityEvent> activityEvents = activities.findTop12ByUserOrderByCreatedAtDesc(user);
        if (assets.isEmpty()) {
            return new DashboardView(
                    new UserView(user.email(), user.fullName(), user.organizationName()),
                    null,
                    List.of(),
                    PortfolioMetrics.empty(),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    activityEvents.stream().map(WorkspaceService::toActivity).toList(),
                    List.of(new InsightView("HIGH", "Immobilie anlegen", "Lege die erste WEG mit Kontostand, Rücklage und Einheiten an.", "properties", "Immobilie starten")),
                    new OnboardingView(13, true, false, false, false, false, false, false, false)
            );
        }

        PropertyAsset selected = selectedPropertyId == null ? assets.getFirst() : propertyFor(user, selectedPropertyId);
        List<OwnerUnit> ownerUnits = units.findByProperty(selected);
        List<WorkTask> workTasks = tasks.findTop8ByPropertyOrderByCreatedAtDesc(selected);
        List<FinanceEvent> financeEvents = finances.findTop8ByPropertyOrderByBookedOnDesc(selected);
        List<AnnualPlan> planViews = annualPlans.findTop4ByPropertyOrderByFiscalYearDesc(selected);
        List<PropertyDocument> documentViews = documents.findTop8ByPropertyOrderByDocumentDateDesc(selected);
        List<CommunityDecision> communityDecisions = decisions.findTop8ByPropertyOrderByMeetingDateDesc(selected);
        List<OwnerMeeting> meetingViews = ownerMeetings.findTop6ByPropertyOrderByMeetingDateDesc(selected);
        List<CommunityMessage> messageViews = messages.findTop8ByPropertyOrderByCreatedAtDesc(selected);

        BigDecimal pendingPayments = financeEvents.stream()
                .map(FinanceEvent::amount)
                .filter(amount -> amount.signum() < 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .abs();
        int openTaskCount = (int) workTasks.stream().filter(task -> task.status() != TaskStatus.DONE).count();

        OnboardingView onboarding = onboarding(!assets.isEmpty(), !ownerUnits.isEmpty(), !financeEvents.isEmpty(), !workTasks.isEmpty(), !communityDecisions.isEmpty(), !planViews.isEmpty(), !meetingViews.isEmpty());
        return new DashboardView(
                new UserView(user.email(), user.fullName(), user.organizationName()),
                selected.id(),
                assets.stream().map(WorkspaceService::toProperty).toList(),
                new PortfolioMetrics(
                        assets.size(),
                        assets.stream().mapToInt(PropertyAsset::unitCount).sum(),
                        selected.cashBalance(),
                        selected.reserveBalance(),
                        pendingPayments,
                        openTaskCount,
                        onboarding.completion()
                ),
                ownerUnits.stream().map(WorkspaceService::toUnit).toList(),
                workTasks.stream().map(WorkspaceService::toTask).toList(),
                financeEvents.stream().map(WorkspaceService::toFinance).toList(),
                planViews.stream().map(WorkspaceService::toAnnualPlan).toList(),
                documentViews.stream().map(WorkspaceService::toDocument).toList(),
                communityDecisions.stream().map(WorkspaceService::toDecision).toList(),
                meetingViews.stream().map(WorkspaceService::toMeeting).toList(),
                messageViews.stream().map(WorkspaceService::toMessage).toList(),
                activityEvents.stream().map(WorkspaceService::toActivity).toList(),
                insights(ownerUnits, workTasks, financeEvents, planViews, documentViews, communityDecisions, meetingViews, pendingPayments),
                onboarding
        );
    }

    @Transactional
    public DashboardView createProperty(UUID userId, CreatePropertyCommand command) {
        AppUser user = user(userId);
        PropertyAsset property = properties.save(new PropertyAsset(
                user,
                command.name(),
                command.address(),
                command.city(),
                command.unitCount(),
                command.cashBalance(),
                command.reserveBalance()
        ));
        activities.save(new ActivityEvent(user, property, "PROPERTY", "Immobilie hinzugefügt: " + property.name()));
        audit.record(user, "property.create", "property_asset", property.id(), "Immobilie angelegt: " + property.name());
        return dashboard(userId, property.id());
    }

    @Transactional
    public DashboardView createUnit(UUID userId, CreateUnitCommand command) {
        AppUser user = user(userId);
        PropertyAsset property = propertyFor(user, command.propertyId());
        OwnerUnit unit = units.save(new OwnerUnit(property, command.ownerName(), command.unitLabel(), command.shareValue()));
        activities.save(new ActivityEvent(user, property, "UNIT", "Einheit hinzugefügt: " + command.unitLabel()));
        audit.record(user, "unit.create", "owner_unit", unit.id(), "Einheit angelegt: " + command.unitLabel());
        return dashboard(userId, property.id());
    }

    @Transactional
    public DashboardView addTask(UUID userId, CreateTaskCommand command) {
        AppUser user = user(userId);
        PropertyAsset property = propertyFor(user, command.propertyId());
        WorkTask task = tasks.save(new WorkTask(property, command.title(), command.description(), command.priority(), command.dueDate()));
        activities.save(new ActivityEvent(user, property, "TASK", "Neue Aufgabe erstellt: " + command.title()));
        audit.record(user, "task.create", "work_task", task.id(), "Aufgabe angelegt: " + command.title());
        return dashboard(userId, property.id());
    }

    @Transactional
    public DashboardView updateTaskStatus(UUID userId, UUID taskId, TaskStatus status) {
        AppUser user = user(userId);
        WorkTask task = tasks.findById(taskId).orElseThrow(() -> new IllegalArgumentException("Aufgabe nicht gefunden."));
        PropertyAsset property = task.property();
        if (!property.owner().id().equals(user.id())) {
            throw new IllegalArgumentException("Aufgabe gehört nicht zu diesem Workspace.");
        }
        task.transitionTo(status);
        activities.save(new ActivityEvent(user, property, "TASK", "Aufgabe aktualisiert: " + task.title() + " ist " + taskStatusLabel(status) + "."));
        audit.record(user, "task.status", "work_task", task.id(), "Aufgabenstatus gesetzt: " + status.name());
        return dashboard(userId, property.id());
    }

    @Transactional
    public DashboardView createFinance(UUID userId, CreateFinanceCommand command) {
        AppUser user = user(userId);
        PropertyAsset property = propertyFor(user, command.propertyId());
        FinanceEvent finance = finances.save(new FinanceEvent(property, command.label(), command.amount(), command.category(), command.bookedOn(), command.status()));
        activities.save(new ActivityEvent(user, property, "FINANCE", "Finanzereignis erfasst: " + command.label()));
        audit.record(user, "finance.create", "finance_event", finance.id(), "Finanzereignis erfasst: " + command.label());
        return dashboard(userId, property.id());
    }

    @Transactional
    public DashboardView createAnnualPlan(UUID userId, CreateAnnualPlanCommand command) {
        AppUser user = user(userId);
        PropertyAsset property = propertyFor(user, command.propertyId());
        AnnualPlan plan = annualPlans.save(new AnnualPlan(
                property,
                command.fiscalYear(),
                command.houseMoneyBudget(),
                command.maintenanceBudget(),
                command.reserveContribution(),
                command.status()
        ));
        activities.save(new ActivityEvent(user, property, "PLAN", "Wirtschaftsplan erfasst: " + command.fiscalYear()));
        audit.record(user, "annual_plan.create", "annual_plan", plan.id(), "Wirtschaftsplan angelegt: " + command.fiscalYear());
        return dashboard(userId, property.id());
    }

    @Transactional
    public DashboardView createDocument(UUID userId, CreateDocumentCommand command) {
        AppUser user = user(userId);
        PropertyAsset property = propertyFor(user, command.propertyId());
        PropertyDocument document = documents.save(new PropertyDocument(property, command.title(), command.documentType(), command.fileName(), command.documentDate()));
        activities.save(new ActivityEvent(user, property, "DOCUMENT", "Dokument abgelegt: " + command.title()));
        audit.record(user, "document.create", "property_document", document.id(), "Dokument abgelegt: " + command.title());
        return dashboard(userId, property.id());
    }

    @Transactional
    public DashboardView createMeeting(UUID userId, CreateMeetingCommand command) {
        AppUser user = user(userId);
        PropertyAsset property = propertyFor(user, command.propertyId());
        OwnerMeeting meeting = ownerMeetings.save(new OwnerMeeting(
                property,
                command.title(),
                command.meetingDate(),
                command.location(),
                command.agenda(),
                command.status()
        ));
        activities.save(new ActivityEvent(user, property, "MEETING", "Eigentümerversammlung geplant: " + meeting.title()));
        audit.record(user, "meeting.create", "owner_meeting", meeting.id(), "Eigentümerversammlung angelegt: " + meeting.title());
        return dashboard(userId, property.id());
    }

    @Transactional
    public DashboardView createMessage(UUID userId, CreateMessageCommand command) {
        AppUser user = user(userId);
        PropertyAsset property = propertyFor(user, command.propertyId());
        CommunityMessage message = messages.save(new CommunityMessage(
                property,
                command.audience(),
                command.subject(),
                command.message(),
                "PREPARED"
        ));
        activities.save(new ActivityEvent(user, property, "COMMUNICATION", "Mitteilung vorbereitet: " + message.subject()));
        audit.record(user, "message.create", "community_message", message.id(), "Mitteilung vorbereitet: " + message.subject());
        return dashboard(userId, property.id());
    }

    @Transactional
    public DashboardView createDecision(UUID userId, CreateDecisionCommand command) {
        AppUser user = user(userId);
        PropertyAsset property = propertyFor(user, command.propertyId());
        CommunityDecision decision = decisions.save(new CommunityDecision(
                property,
                command.title(),
                command.resolutionText(),
                command.meetingDate(),
                command.meetingLocation(),
                command.status(),
                command.yesVotes(),
                command.noVotes(),
                command.abstentions()
        ));
        activities.save(new ActivityEvent(user, property, "DECISION", "Beschluss dokumentiert: " + decision.title()));
        audit.record(user, "decision.create", "community_decision", decision.id(), "Beschluss dokumentiert: " + decision.title());
        return dashboard(userId, property.id());
    }

    @Transactional
    public DashboardView updateDecisionStatus(UUID userId, UUID decisionId, DecisionStatus status) {
        AppUser user = user(userId);
        CommunityDecision decision = decisions.findById(decisionId).orElseThrow(() -> new IllegalArgumentException("Beschluss nicht gefunden."));
        PropertyAsset property = decision.property();
        if (!property.owner().id().equals(user.id())) {
            throw new IllegalArgumentException("Beschluss gehört nicht zu diesem Workspace.");
        }
        decision.transitionTo(status);
        activities.save(new ActivityEvent(user, property, "DECISION", "Beschluss aktualisiert: " + decision.title() + " ist " + decisionStatusLabel(status) + "."));
        audit.record(user, "decision.status", "community_decision", decision.id(), "Beschlussstatus gesetzt: " + status.name());
        return dashboard(userId, property.id());
    }

    private AppUser user(UUID userId) {
        return users.findById(userId).orElseThrow(() -> new IllegalArgumentException("Nutzer nicht gefunden."));
    }

    private PropertyAsset propertyFor(AppUser user, UUID requestedPropertyId) {
        if (requestedPropertyId != null) {
            PropertyAsset property = properties.findById(requestedPropertyId)
                    .orElseThrow(() -> new IllegalArgumentException("Immobilie nicht gefunden."));
            if (!property.owner().id().equals(user.id())) {
                throw new IllegalArgumentException("Immobilie gehört nicht zu diesem Workspace.");
            }
            return property;
        }
        return properties.findFirstByOwnerOrderByCreatedAtAsc(user)
                .orElseThrow(() -> new IllegalArgumentException("Bitte zuerst eine Immobilie hinzufügen."));
    }

    private static OnboardingView onboarding(boolean hasProperty, boolean hasUnits, boolean hasFinance, boolean hasTasks, boolean hasDecisions, boolean hasAnnualPlan, boolean hasMeeting) {
        int completed = 1
                + (hasProperty ? 1 : 0)
                + (hasUnits ? 1 : 0)
                + (hasFinance ? 1 : 0)
                + (hasTasks ? 1 : 0)
                + (hasDecisions ? 1 : 0)
                + (hasAnnualPlan ? 1 : 0)
                + (hasMeeting ? 1 : 0);
        return new OnboardingView(Math.round(completed * 100f / 8f), true, hasProperty, hasUnits, hasFinance, hasTasks, hasDecisions, hasAnnualPlan, hasMeeting);
    }

    private static List<InsightView> insights(
            List<OwnerUnit> ownerUnits,
            List<WorkTask> workTasks,
            List<FinanceEvent> financeEvents,
            List<AnnualPlan> annualPlans,
            List<PropertyDocument> documents,
            List<CommunityDecision> decisions,
            List<OwnerMeeting> meetings,
            BigDecimal pendingPayments
    ) {
        List<InsightView> result = new java.util.ArrayList<>();
        if (ownerUnits.isEmpty()) {
            result.add(new InsightView("HIGH", "Eigentümerstruktur fehlt", "Einheiten und Miteigentumsanteile sind die Grundlage für Umlagen und Beschlüsse.", "units", "Einheiten anlegen"));
        }
        if (pendingPayments.signum() > 0) {
            result.add(new InsightView("HIGH", "Offene Forderungen klären", "Negative Buchungen sollten geprüft, gebucht oder aktiv nachverfolgt werden.", "finances", "Finanzen prüfen"));
        }
        boolean hasOpenTask = workTasks.stream().anyMatch(task -> task.status() != TaskStatus.DONE);
        boolean hasOpenDecision = decisions.stream().anyMatch(decision -> decision.status() == DecisionStatus.PASSED || decision.status() == DecisionStatus.DRAFT);
        if (hasOpenTask) {
            result.add(new InsightView("MEDIUM", "Nächste Aufgabe steuern", "Offene Vorgänge brauchen einen klaren Status, damit Beirat und Eigentümer folgen können.", "tasks", "Aufgaben öffnen"));
        }
        if (decisions.isEmpty()) {
            result.add(new InsightView("MEDIUM", "Beschluss-Sammlung starten", "Gefasste Beschlüsse brauchen Datum, Ort, Wortlaut und Abstimmungsergebnis.", "decisions", "Beschluss erfassen"));
        } else if (hasOpenDecision) {
            result.add(new InsightView("MEDIUM", "Beschluss umsetzen", "Gefasste Beschlüsse sollten nachvollziehbar in operative Umsetzung übergehen.", "decisions", "Beschlüsse prüfen"));
        }
        if (financeEvents.isEmpty()) {
            result.add(new InsightView("MEDIUM", "Finanzlage erfassen", "Kontostand und Rücklage werden wertvoller, sobald die ersten Buchungen im Verlauf sichtbar sind.", "finances", "Buchung erfassen"));
        }
        if (annualPlans.isEmpty()) {
            result.add(new InsightView("MEDIUM", "Wirtschaftsplan anlegen", "Hausgeld, Instandhaltung und Rücklage brauchen eine beschlossene Jahresplanung.", "finances", "Wirtschaftsplan erfassen"));
        }
        if (meetings.isEmpty()) {
            result.add(new InsightView("LOW", "Versammlung vorbereiten", "Einladung, Tagesordnung und Beschlussvorlagen sollten früh an einer Stelle liegen.", "decisions", "Versammlung planen"));
        }
        if (documents.isEmpty()) {
            result.add(new InsightView("LOW", "Beschlüsse dokumentieren", "Protokolle, Wirtschaftspläne und Rechnungen sollten direkt am Objekt auffindbar sein.", "documents", "Dokument ablegen"));
        }
        if (result.isEmpty()) {
            result.add(new InsightView("GOOD", "Workspace ist operativ sauber", "Die wichtigsten Daten liegen vor. Nächster Hebel: regelmäßige Zahlungs- und Aufgabenprüfung.", "activity", "Audit ansehen"));
        }
        return result.stream().limit(3).toList();
    }

    private static String taskStatusLabel(TaskStatus status) {
        return switch (status) {
            case OPEN -> "offen";
            case IN_REVIEW -> "in Prüfung";
            case DONE -> "erledigt";
        };
    }

    private static String decisionStatusLabel(DecisionStatus status) {
        return switch (status) {
            case DRAFT -> "in Vorbereitung";
            case PASSED -> "beschlossen";
            case REJECTED -> "abgelehnt";
            case IMPLEMENTED -> "umgesetzt";
        };
    }

    private static PropertyView toProperty(PropertyAsset property) {
        return new PropertyView(property.id(), property.name(), property.address(), property.city(), property.unitCount(), property.cashBalance(), property.reserveBalance(), "Aktiv");
    }

    private static UnitView toUnit(OwnerUnit unit) {
        return new UnitView(unit.ownerName(), unit.unitLabel(), unit.shareValue());
    }

    private static TaskView toTask(WorkTask task) {
        return new TaskView(task.id(), task.title(), task.description(), task.status().name(), task.priority().name(), task.dueDate());
    }

    private static FinanceView toFinance(FinanceEvent finance) {
        return new FinanceView(finance.label(), finance.amount(), finance.category(), finance.bookedOn(), finance.status());
    }

    private static AnnualPlanView toAnnualPlan(AnnualPlan plan) {
        return new AnnualPlanView(
                plan.id(),
                plan.fiscalYear(),
                plan.houseMoneyBudget(),
                plan.maintenanceBudget(),
                plan.reserveContribution(),
                plan.status().name()
        );
    }

    private static DocumentView toDocument(PropertyDocument document) {
        return new DocumentView(document.id(), document.title(), document.documentType(), document.fileName(), document.documentDate());
    }

    private static DecisionView toDecision(CommunityDecision decision) {
        return new DecisionView(
                decision.id(),
                decision.title(),
                decision.resolutionText(),
                decision.meetingDate(),
                decision.meetingLocation(),
                decision.status().name(),
                decision.yesVotes(),
                decision.noVotes(),
                decision.abstentions()
        );
    }

    private static MeetingView toMeeting(OwnerMeeting meeting) {
        return new MeetingView(
                meeting.id(),
                meeting.title(),
                meeting.meetingDate(),
                meeting.location(),
                meeting.agenda(),
                meeting.status().name()
        );
    }

    private static MessageView toMessage(CommunityMessage message) {
        return new MessageView(message.id(), message.audience(), message.subject(), message.message(), message.status(), message.createdAt());
    }

    private static ActivityView toActivity(ActivityEvent event) {
        return new ActivityView(event.eventType(), event.summary(), event.createdAt());
    }

    public record DashboardView(
            UserView user,
            UUID selectedPropertyId,
            List<PropertyView> properties,
            PortfolioMetrics metrics,
            List<UnitView> units,
            List<TaskView> tasks,
            List<FinanceView> finances,
            List<AnnualPlanView> annualPlans,
            List<DocumentView> documents,
            List<DecisionView> decisions,
            List<MeetingView> meetings,
            List<MessageView> messages,
            List<ActivityView> activity,
            List<InsightView> insights,
            OnboardingView onboarding
    ) {
    }

    public record UserView(String email, String fullName, String organizationName) {
    }

    public record PropertyView(UUID id, String name, String address, String city, int unitCount, BigDecimal cashBalance, BigDecimal reserveBalance, String status) {
    }

    public record PortfolioMetrics(int properties, int units, BigDecimal cashBalance, BigDecimal reserveBalance, BigDecimal pendingPayments, int openTasks, int onboardingCompletion) {
        static PortfolioMetrics empty() {
            return new PortfolioMetrics(0, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0, 33);
        }
    }

    public record UnitView(String ownerName, String unitLabel, BigDecimal shareValue) {
    }

    public record TaskView(UUID id, String title, String description, String status, String priority, LocalDate dueDate) {
    }

    public record FinanceView(String label, BigDecimal amount, String category, LocalDate bookedOn, String status) {
    }

    public record AnnualPlanView(UUID id, int fiscalYear, BigDecimal houseMoneyBudget, BigDecimal maintenanceBudget, BigDecimal reserveContribution, String status) {
    }

    public record DocumentView(UUID id, String title, String documentType, String fileName, LocalDate documentDate) {
    }

    public record DecisionView(UUID id, String title, String resolutionText, LocalDate meetingDate, String meetingLocation, String status, int yesVotes, int noVotes, int abstentions) {
    }

    public record MeetingView(UUID id, String title, LocalDate meetingDate, String location, String agenda, String status) {
    }

    public record MessageView(UUID id, String audience, String subject, String message, String status, Instant createdAt) {
    }

    public record ActivityView(String eventType, String summary, Instant createdAt) {
    }

    public record InsightView(String severity, String title, String description, String actionSection, String actionLabel) {
    }

    public record OnboardingView(int completion, boolean accountActivated, boolean propertyCreated, boolean unitsCreated, boolean financeCreated, boolean taskCreated, boolean decisionCreated, boolean annualPlanCreated, boolean meetingCreated) {
    }

    public record CreatePropertyCommand(String name, String address, String city, int unitCount, BigDecimal cashBalance, BigDecimal reserveBalance) {
    }

    public record CreateUnitCommand(UUID propertyId, String ownerName, String unitLabel, BigDecimal shareValue) {
    }

    public record CreateTaskCommand(UUID propertyId, String title, String description, TaskPriority priority, LocalDate dueDate) {
    }

    public record CreateFinanceCommand(UUID propertyId, String label, BigDecimal amount, String category, LocalDate bookedOn, String status) {
    }

    public record CreateAnnualPlanCommand(UUID propertyId, int fiscalYear, BigDecimal houseMoneyBudget, BigDecimal maintenanceBudget, BigDecimal reserveContribution, AnnualPlanStatus status) {
    }

    public record CreateDocumentCommand(UUID propertyId, String title, String documentType, String fileName, LocalDate documentDate) {
    }

    public record CreateMeetingCommand(UUID propertyId, String title, LocalDate meetingDate, String location, String agenda, MeetingStatus status) {
    }

    public record CreateMessageCommand(UUID propertyId, String audience, String subject, String message) {
    }

    public record CreateDecisionCommand(UUID propertyId, String title, String resolutionText, LocalDate meetingDate, String meetingLocation, DecisionStatus status, int yesVotes, int noVotes, int abstentions) {
    }
}
