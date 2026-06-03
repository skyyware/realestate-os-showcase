package com.skyyware.realestate.workspace;

import com.skyyware.realestate.activity.ActivityEvent;
import com.skyyware.realestate.activity.ActivityEventRepository;
import com.skyyware.realestate.document.PropertyDocument;
import com.skyyware.realestate.document.PropertyDocumentRepository;
import com.skyyware.realestate.finance.FinanceEvent;
import com.skyyware.realestate.finance.FinanceEventRepository;
import com.skyyware.realestate.identity.AppUser;
import com.skyyware.realestate.identity.AppUserRepository;
import com.skyyware.realestate.property.OwnerUnit;
import com.skyyware.realestate.property.OwnerUnitRepository;
import com.skyyware.realestate.property.PropertyAsset;
import com.skyyware.realestate.property.PropertyAssetRepository;
import com.skyyware.realestate.task.TaskPriority;
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

    public WorkspaceService(
            AppUserRepository users,
            PropertyAssetRepository properties,
            OwnerUnitRepository units,
            WorkTaskRepository tasks,
            FinanceEventRepository finances,
            ActivityEventRepository activities,
            PropertyDocumentRepository documents
    ) {
        this.users = users;
        this.properties = properties;
        this.units = units;
        this.tasks = tasks;
        this.finances = finances;
        this.activities = activities;
        this.documents = documents;
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
                    activityEvents.stream().map(WorkspaceService::toActivity).toList(),
                    new OnboardingView(20, true, false, false, false, false)
            );
        }

        PropertyAsset selected = selectedPropertyId == null ? assets.getFirst() : propertyFor(user, selectedPropertyId);
        List<OwnerUnit> ownerUnits = units.findByProperty(selected);
        List<WorkTask> workTasks = tasks.findTop8ByPropertyOrderByCreatedAtDesc(selected);
        List<FinanceEvent> financeEvents = finances.findTop8ByPropertyOrderByBookedOnDesc(selected);
        List<PropertyDocument> documentViews = documents.findTop8ByPropertyOrderByDocumentDateDesc(selected);

        BigDecimal pendingPayments = financeEvents.stream()
                .map(FinanceEvent::amount)
                .filter(amount -> amount.signum() < 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .abs();

        OnboardingView onboarding = onboarding(!assets.isEmpty(), !ownerUnits.isEmpty(), !financeEvents.isEmpty(), !workTasks.isEmpty());
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
                        workTasks.size(),
                        onboarding.completion()
                ),
                ownerUnits.stream().map(WorkspaceService::toUnit).toList(),
                workTasks.stream().map(WorkspaceService::toTask).toList(),
                financeEvents.stream().map(WorkspaceService::toFinance).toList(),
                documentViews.stream().map(WorkspaceService::toDocument).toList(),
                activityEvents.stream().map(WorkspaceService::toActivity).toList(),
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
        return dashboard(userId, property.id());
    }

    @Transactional
    public DashboardView createUnit(UUID userId, CreateUnitCommand command) {
        AppUser user = user(userId);
        PropertyAsset property = propertyFor(user, command.propertyId());
        units.save(new OwnerUnit(property, command.ownerName(), command.unitLabel(), command.shareValue()));
        activities.save(new ActivityEvent(user, property, "UNIT", "Einheit hinzugefügt: " + command.unitLabel()));
        return dashboard(userId, property.id());
    }

    @Transactional
    public DashboardView addTask(UUID userId, CreateTaskCommand command) {
        AppUser user = user(userId);
        PropertyAsset property = propertyFor(user, command.propertyId());
        tasks.save(new WorkTask(property, command.title(), command.description(), command.priority(), command.dueDate()));
        activities.save(new ActivityEvent(user, property, "TASK", "Neue Aufgabe erstellt: " + command.title()));
        return dashboard(userId, property.id());
    }

    @Transactional
    public DashboardView createFinance(UUID userId, CreateFinanceCommand command) {
        AppUser user = user(userId);
        PropertyAsset property = propertyFor(user, command.propertyId());
        finances.save(new FinanceEvent(property, command.label(), command.amount(), command.category(), command.bookedOn(), command.status()));
        activities.save(new ActivityEvent(user, property, "FINANCE", "Finanzereignis erfasst: " + command.label()));
        return dashboard(userId, property.id());
    }

    @Transactional
    public DashboardView createDocument(UUID userId, CreateDocumentCommand command) {
        AppUser user = user(userId);
        PropertyAsset property = propertyFor(user, command.propertyId());
        documents.save(new PropertyDocument(property, command.title(), command.documentType(), command.fileName(), command.documentDate()));
        activities.save(new ActivityEvent(user, property, "DOCUMENT", "Dokument abgelegt: " + command.title()));
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

    private static OnboardingView onboarding(boolean hasProperty, boolean hasUnits, boolean hasFinance, boolean hasTasks) {
        int completed = 1 + (hasProperty ? 1 : 0) + (hasUnits ? 1 : 0) + (hasFinance ? 1 : 0) + (hasTasks ? 1 : 0);
        return new OnboardingView(completed * 20, true, hasProperty, hasUnits, hasFinance, hasTasks);
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

    private static DocumentView toDocument(PropertyDocument document) {
        return new DocumentView(document.id(), document.title(), document.documentType(), document.fileName(), document.documentDate());
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
            List<DocumentView> documents,
            List<ActivityView> activity,
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

    public record DocumentView(UUID id, String title, String documentType, String fileName, LocalDate documentDate) {
    }

    public record ActivityView(String eventType, String summary, Instant createdAt) {
    }

    public record OnboardingView(int completion, boolean accountActivated, boolean propertyCreated, boolean unitsCreated, boolean financeCreated, boolean taskCreated) {
    }

    public record CreatePropertyCommand(String name, String address, String city, int unitCount, BigDecimal cashBalance, BigDecimal reserveBalance) {
    }

    public record CreateUnitCommand(UUID propertyId, String ownerName, String unitLabel, BigDecimal shareValue) {
    }

    public record CreateTaskCommand(UUID propertyId, String title, String description, TaskPriority priority, LocalDate dueDate) {
    }

    public record CreateFinanceCommand(UUID propertyId, String label, BigDecimal amount, String category, LocalDate bookedOn, String status) {
    }

    public record CreateDocumentCommand(UUID propertyId, String title, String documentType, String fileName, LocalDate documentDate) {
    }
}
