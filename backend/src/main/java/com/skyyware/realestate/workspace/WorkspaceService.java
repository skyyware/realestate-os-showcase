package com.skyyware.realestate.workspace;

import com.skyyware.realestate.activity.ActivityEvent;
import com.skyyware.realestate.activity.ActivityEventRepository;
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

    public WorkspaceService(
            AppUserRepository users,
            PropertyAssetRepository properties,
            OwnerUnitRepository units,
            WorkTaskRepository tasks,
            FinanceEventRepository finances,
            ActivityEventRepository activities
    ) {
        this.users = users;
        this.properties = properties;
        this.units = units;
        this.tasks = tasks;
        this.finances = finances;
        this.activities = activities;
    }

    @Transactional(readOnly = true)
    public DashboardView dashboard(UUID userId) {
        AppUser user = users.findById(userId).orElseThrow(() -> new IllegalArgumentException("Nutzer nicht gefunden."));
        List<PropertyAsset> assets = properties.findByOwnerOrderByCreatedAtAsc(user);
        PropertyAsset selected = assets.stream().findFirst().orElseThrow(() -> new IllegalArgumentException("Noch kein Workspace vorhanden."));
        List<OwnerUnit> ownerUnits = units.findByProperty(selected);
        List<WorkTask> workTasks = tasks.findTop8ByPropertyOrderByCreatedAtDesc(selected);
        List<FinanceEvent> financeEvents = finances.findTop8ByPropertyOrderByBookedOnDesc(selected);
        List<ActivityEvent> activityEvents = activities.findTop12ByUserOrderByCreatedAtDesc(user);

        BigDecimal pendingPayments = financeEvents.stream()
                .map(FinanceEvent::amount)
                .filter(amount -> amount.signum() < 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .abs();

        return new DashboardView(
                new UserView(user.email(), user.fullName(), user.organizationName()),
                assets.stream().map(WorkspaceService::toProperty).toList(),
                new PortfolioMetrics(
                        assets.size(),
                        assets.stream().mapToInt(PropertyAsset::unitCount).sum(),
                        selected.cashBalance(),
                        selected.reserveBalance(),
                        pendingPayments
                ),
                ownerUnits.stream().map(WorkspaceService::toUnit).toList(),
                workTasks.stream().map(WorkspaceService::toTask).toList(),
                financeEvents.stream().map(WorkspaceService::toFinance).toList(),
                activityEvents.stream().map(WorkspaceService::toActivity).toList()
        );
    }

    @Transactional
    public DashboardView addTask(UUID userId, CreateTaskCommand command) {
        AppUser user = users.findById(userId).orElseThrow(() -> new IllegalArgumentException("Nutzer nicht gefunden."));
        PropertyAsset property = properties.findFirstByOwnerOrderByCreatedAtAsc(user)
                .orElseThrow(() -> new IllegalArgumentException("Noch kein Workspace vorhanden."));
        tasks.save(new WorkTask(property, command.title(), command.description(), command.priority(), command.dueDate()));
        activities.save(new ActivityEvent(user, property, "TASK", "Neue Aufgabe erstellt: " + command.title()));
        return dashboard(userId);
    }

    private static PropertyView toProperty(PropertyAsset property) {
        return new PropertyView(property.id(), property.name(), property.address(), property.city(), property.unitCount(), property.cashBalance(), property.reserveBalance());
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

    private static ActivityView toActivity(ActivityEvent event) {
        return new ActivityView(event.eventType(), event.summary(), event.createdAt());
    }

    public record DashboardView(
            UserView user,
            List<PropertyView> properties,
            PortfolioMetrics metrics,
            List<UnitView> units,
            List<TaskView> tasks,
            List<FinanceView> finances,
            List<ActivityView> activity
    ) {
    }

    public record UserView(String email, String fullName, String organizationName) {
    }

    public record PropertyView(UUID id, String name, String address, String city, int unitCount, BigDecimal cashBalance, BigDecimal reserveBalance) {
    }

    public record PortfolioMetrics(int properties, int units, BigDecimal cashBalance, BigDecimal reserveBalance, BigDecimal pendingPayments) {
    }

    public record UnitView(String ownerName, String unitLabel, BigDecimal shareValue) {
    }

    public record TaskView(UUID id, String title, String description, String status, String priority, LocalDate dueDate) {
    }

    public record FinanceView(String label, BigDecimal amount, String category, LocalDate bookedOn, String status) {
    }

    public record ActivityView(String eventType, String summary, Instant createdAt) {
    }

    public record CreateTaskCommand(String title, String description, TaskPriority priority, LocalDate dueDate) {
    }
}
