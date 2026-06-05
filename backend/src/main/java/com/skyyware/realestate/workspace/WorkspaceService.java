package com.skyyware.realestate.workspace;

import com.skyyware.realestate.activity.ActivityEvent;
import com.skyyware.realestate.activity.ActivityEventRepository;
import com.skyyware.realestate.audit.AuditLog;
import com.skyyware.realestate.audit.AuditService;
import com.skyyware.realestate.common.WorkContextType;
import com.skyyware.realestate.communication.CommunityMessage;
import com.skyyware.realestate.communication.CommunityMessageRepository;
import com.skyyware.realestate.communication.MessageChannel;
import com.skyyware.realestate.communication.MessageStatus;
import com.skyyware.realestate.config.RealEstateProperties;
import com.skyyware.realestate.decision.CommunityDecision;
import com.skyyware.realestate.decision.CommunityDecisionRepository;
import com.skyyware.realestate.decision.DecisionStatus;
import com.skyyware.realestate.document.DocumentLinkType;
import com.skyyware.realestate.document.DocumentStatus;
import com.skyyware.realestate.document.DocumentVisibility;
import com.skyyware.realestate.document.PropertyDocument;
import com.skyyware.realestate.document.PropertyDocumentRepository;
import com.skyyware.realestate.finance.AllocationKey;
import com.skyyware.realestate.finance.AssessmentStatus;
import com.skyyware.realestate.finance.FinanceEvent;
import com.skyyware.realestate.finance.FinanceEventRepository;
import com.skyyware.realestate.finance.FinanceEventType;
import com.skyyware.realestate.finance.HouseMoneyAssessment;
import com.skyyware.realestate.finance.HouseMoneyAssessmentRepository;
import com.skyyware.realestate.identity.AppUser;
import com.skyyware.realestate.identity.AppUserRepository;
import com.skyyware.realestate.mail.TransactionalMailService;
import com.skyyware.realestate.meeting.MeetingStatus;
import com.skyyware.realestate.meeting.OwnerMeeting;
import com.skyyware.realestate.meeting.OwnerMeetingRepository;
import com.skyyware.realestate.planning.AnnualPlan;
import com.skyyware.realestate.planning.AnnualPlanRepository;
import com.skyyware.realestate.planning.AnnualPlanStatus;
import com.skyyware.realestate.property.CommunityMember;
import com.skyyware.realestate.property.CommunityMemberRepository;
import com.skyyware.realestate.property.CommunityRole;
import com.skyyware.realestate.property.ManagementMode;
import com.skyyware.realestate.property.MemberStatus;
import com.skyyware.realestate.property.OccupancyType;
import com.skyyware.realestate.property.OwnerUnit;
import com.skyyware.realestate.property.OwnerUnitRepository;
import com.skyyware.realestate.property.PropertyAsset;
import com.skyyware.realestate.property.PropertyAssetRepository;
import com.skyyware.realestate.storage.DocumentStorage;
import com.skyyware.realestate.task.TaskPriority;
import com.skyyware.realestate.task.TaskStatus;
import com.skyyware.realestate.task.WorkTask;
import com.skyyware.realestate.task.WorkTaskRepository;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkspaceService {
    private final AppUserRepository users;
    private final PropertyAssetRepository properties;
    private final OwnerUnitRepository units;
    private final CommunityMemberRepository members;
    private final WorkTaskRepository tasks;
    private final FinanceEventRepository finances;
    private final HouseMoneyAssessmentRepository houseMoneyAssessments;
    private final ActivityEventRepository activities;
    private final PropertyDocumentRepository documents;
    private final CommunityDecisionRepository decisions;
    private final AnnualPlanRepository annualPlans;
    private final OwnerMeetingRepository ownerMeetings;
    private final CommunityMessageRepository messages;
    private final AuditService audit;
    private final TransactionalMailService mailService;
    private final RealEstateProperties realEstateProperties;
    private final DocumentStorage documentStorage;

    public WorkspaceService(
            AppUserRepository users,
            PropertyAssetRepository properties,
            OwnerUnitRepository units,
            CommunityMemberRepository members,
            WorkTaskRepository tasks,
            FinanceEventRepository finances,
            HouseMoneyAssessmentRepository houseMoneyAssessments,
            ActivityEventRepository activities,
            PropertyDocumentRepository documents,
            CommunityDecisionRepository decisions,
            AnnualPlanRepository annualPlans,
            OwnerMeetingRepository ownerMeetings,
            CommunityMessageRepository messages,
            AuditService audit,
            TransactionalMailService mailService,
            RealEstateProperties realEstateProperties,
            DocumentStorage documentStorage
    ) {
        this.users = users;
        this.properties = properties;
        this.units = units;
        this.members = members;
        this.tasks = tasks;
        this.finances = finances;
        this.houseMoneyAssessments = houseMoneyAssessments;
        this.activities = activities;
        this.documents = documents;
        this.decisions = decisions;
        this.annualPlans = annualPlans;
        this.ownerMeetings = ownerMeetings;
        this.messages = messages;
        this.audit = audit;
        this.mailService = mailService;
        this.realEstateProperties = realEstateProperties;
        this.documentStorage = documentStorage;
    }

    @Transactional(readOnly = true)
    public DashboardView dashboard(UUID userId) {
        return dashboard(userId, null);
    }

    @Transactional(readOnly = true)
    public DashboardView dashboard(UUID userId, UUID selectedPropertyId) {
        AppUser user = user(userId);
        List<PropertyAsset> assets = accessibleProperties(user);
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
                    List.of(),
                    List.of(),
                    List.of(),
                    ReadinessView.empty(),
                    AccessView.empty(),
                    List.of(),
                    activityEvents.stream().map(WorkspaceService::toActivity).toList(),
                    List.of(new InsightView("HIGH", "Immobilie anlegen", "Lege die erste WEG mit Kontostand, Rücklage und Einheiten an.", "properties", "Immobilie starten")),
                    new OnboardingView(10, true, false, false, false, false, false, false, false, false, false)
            );
        }

        PropertyAsset selected = selectedPropertyId == null ? assets.getFirst() : propertyForView(user, selectedPropertyId);
        List<OwnerUnit> ownerUnits = units.findByProperty(selected);
        List<CommunityMember> communityMembers = members.findByPropertyOrderByCreatedAtAsc(selected);
        List<WorkTask> workTasks = tasks.findTop8ByPropertyOrderByCreatedAtDesc(selected);
        List<FinanceEvent> financeEvents = finances.findTop8ByPropertyOrderByBookedOnDesc(selected);
        List<FinanceEvent> allFinanceEvents = finances.findByProperty(selected);
        List<HouseMoneyAssessment> assessments = houseMoneyAssessments.findTop8ByPropertyOrderByFiscalYearDescCreatedAtDesc(selected);
        List<AnnualPlan> planViews = annualPlans.findTop4ByPropertyOrderByFiscalYearDesc(selected);
        List<PropertyDocument> documentViews = documents.findTop8ByPropertyOrderByDocumentDateDesc(selected);
        List<CommunityDecision> communityDecisions = decisions.findTop8ByPropertyOrderByMeetingDateDesc(selected);
        List<OwnerMeeting> meetingViews = ownerMeetings.findTop6ByPropertyOrderByMeetingDateDesc(selected);
        List<CommunityMessage> messageViews = messages.findTop8ByPropertyOrderByCreatedAtDesc(selected);
        List<AuditLog> auditViews = audit.findForProperty(selected);

        BigDecimal pendingPayments = allFinanceEvents.stream()
                .map(FinanceEvent::amount)
                .filter(amount -> amount.signum() < 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .abs();
        int openTaskCount = (int) workTasks.stream().filter(task -> task.status() != TaskStatus.DONE).count();

        ReadinessView readiness = readiness(selected, ownerUnits, communityMembers);
        OnboardingView onboarding = onboarding(
                !assets.isEmpty(),
                !ownerUnits.isEmpty(),
                readiness.shareDistributionComplete(),
                readiness.rolesReady(),
                !financeEvents.isEmpty(),
                !workTasks.isEmpty(),
                !communityDecisions.isEmpty(),
                !planViews.isEmpty(),
                !meetingViews.isEmpty()
        );
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
                assessments.stream().map(WorkspaceService::toAssessment).toList(),
                unitBalances(ownerUnits, assessments, allFinanceEvents),
                planViews.stream().map(WorkspaceService::toAnnualPlan).toList(),
                documentViews.stream().map(WorkspaceService::toDocument).toList(),
                communityDecisions.stream().map(WorkspaceService::toDecision).toList(),
                meetingViews.stream().map(WorkspaceService::toMeeting).toList(),
                messageViews.stream().map(WorkspaceService::toMessage).toList(),
                communityMembers.stream().map(WorkspaceService::toMember).toList(),
                readiness,
                accessView(user, selected),
                auditViews.stream().map(WorkspaceService::toAudit).toList(),
                activityEvents.stream().map(WorkspaceService::toActivity).toList(),
                insights(selected, ownerUnits, communityMembers, workTasks, financeEvents, planViews, documentViews, communityDecisions, meetingViews, pendingPayments, readiness),
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
                command.fiscalYear(),
                command.cashBalance(),
                command.reserveBalance(),
                command.reserveTarget(),
                command.shareTotal(),
                command.managementMode()
        ));
        members.save(CommunityMember.active(property, user, CommunityRole.OWNER_ADMIN));
        activities.save(new ActivityEvent(user, property, "PROPERTY", "Immobilie hinzugefügt: " + property.name()));
        audit.record(user, property, "property.create", "property_asset", property.id(), "Immobilie angelegt: " + property.name());
        audit.record(user, property, "member.create", "community_member", property.id(), "Eigene Administratorrolle für WEG angelegt.");
        return dashboard(userId, property.id());
    }

    @Transactional
    public DashboardView createUnit(UUID userId, CreateUnitCommand command) {
        AppUser user = user(userId);
        PropertyAsset property = propertyForAdmin(user, command.propertyId());
        OwnerUnit unit = units.save(new OwnerUnit(
                property,
                command.ownerName(),
                command.ownerEmail(),
                command.unitLabel(),
                command.shareValue(),
                command.votingWeight(),
                command.occupancyType()
        ));
        upsertMemberFromUnit(property, user, command.ownerName(), command.ownerEmail());
        activities.save(new ActivityEvent(user, property, "UNIT", "Einheit hinzugefügt: " + command.unitLabel()));
        audit.record(user, property, "unit.create", "owner_unit", unit.id(), "Einheit angelegt: " + command.unitLabel());
        return dashboard(userId, property.id());
    }

    @Transactional
    public DashboardView inviteMember(UUID userId, InviteMemberCommand command) {
        AppUser user = user(userId);
        PropertyAsset property = propertyForAdmin(user, command.propertyId());
        String email = normalizeEmail(command.email());
        CommunityMember member = members.findByPropertyAndEmailIgnoreCase(property, email)
                .map(existing -> {
                    existing.updateInvite(command.fullName(), email, command.role());
                    return existing;
                })
                .orElseGet(() -> members.save(CommunityMember.invited(property, command.fullName(), email, command.role())));
        mailService.sendCommunityInvitation(email, command.fullName(), property.name(), roleLabel(command.role()), workspaceUrl());
        activities.save(new ActivityEvent(user, property, "MEMBER", "Rolle eingeladen: " + command.fullName() + " als " + roleLabel(command.role())));
        audit.record(user, property, "member.invite", "community_member", member.id(), "Mitglied eingeladen: " + email + " als " + command.role().name());
        return dashboard(userId, property.id());
    }

    @Transactional
    public DashboardView updateMember(UUID userId, UUID memberId, UpdateMemberCommand command) {
        AppUser user = user(userId);
        CommunityMember member = members.findById(memberId).orElseThrow(() -> new IllegalArgumentException("Mitglied nicht gefunden."));
        PropertyAsset property = member.property();
        requireAdmin(user, property);
        if (property.owner().email().equalsIgnoreCase(member.email()) && command.status() == MemberStatus.DISABLED) {
            throw new IllegalArgumentException("Die primäre Administratorrolle kann nicht deaktiviert werden.");
        }
        member.updateAccess(command.fullName(), command.email(), command.role(), command.status());
        activities.save(new ActivityEvent(user, property, "MEMBER", "Rolle aktualisiert: " + member.fullName() + " als " + roleLabel(member.role())));
        audit.record(user, property, "member.update", "community_member", member.id(), "Mitglied aktualisiert: " + member.email() + " als " + member.role().name());
        return dashboard(userId, property.id());
    }

    @Transactional
    public DashboardView disableMember(UUID userId, UUID memberId) {
        AppUser user = user(userId);
        CommunityMember member = members.findById(memberId).orElseThrow(() -> new IllegalArgumentException("Mitglied nicht gefunden."));
        PropertyAsset property = member.property();
        requireAdmin(user, property);
        if (property.owner().email().equalsIgnoreCase(member.email())) {
            throw new IllegalArgumentException("Die primäre Administratorrolle kann nicht deaktiviert werden.");
        }
        member.disable();
        activities.save(new ActivityEvent(user, property, "MEMBER", "Rolle deaktiviert: " + member.fullName()));
        audit.record(user, property, "member.disable", "community_member", member.id(), "Mitglied deaktiviert: " + member.email());
        return dashboard(userId, property.id());
    }

    @Transactional
    public DashboardView addTask(UUID userId, CreateTaskCommand command) {
        AppUser user = user(userId);
        PropertyAsset property = propertyForCollaborator(user, command.propertyId());
        validateWorkContext(property, command.sourceType(), command.sourceId());
        WorkTask task = tasks.save(new WorkTask(
                property,
                command.title(),
                command.description(),
                command.priority(),
                command.assigneeRole(),
                command.sourceType(),
                command.sourceId(),
                command.dueDate(),
                command.reminderDate()
        ));
        activities.save(new ActivityEvent(user, property, "TASK", "Neue Aufgabe erstellt: " + command.title()));
        audit.record(user, property, "task.create", "work_task", task.id(), "Aufgabe angelegt: " + command.title());
        return dashboard(userId, property.id());
    }

    @Transactional
    public DashboardView updateTaskStatus(UUID userId, UUID taskId, TaskStatus status) {
        AppUser user = user(userId);
        WorkTask task = tasks.findById(taskId).orElseThrow(() -> new IllegalArgumentException("Aufgabe nicht gefunden."));
        PropertyAsset property = task.property();
        requireCollaborator(user, property);
        task.transitionTo(status);
        activities.save(new ActivityEvent(user, property, "TASK", "Aufgabe aktualisiert: " + task.title() + " ist " + taskStatusLabel(status) + "."));
        audit.record(user, property, "task.status", "work_task", task.id(), "Aufgabenstatus gesetzt: " + status.name());
        return dashboard(userId, property.id());
    }

    @Transactional
    public DashboardView updateTask(UUID userId, UUID taskId, UpdateTaskCommand command) {
        AppUser user = user(userId);
        WorkTask task = tasks.findById(taskId).orElseThrow(() -> new IllegalArgumentException("Aufgabe nicht gefunden."));
        PropertyAsset property = task.property();
        requireCollaborator(user, property);
        validateWorkContext(property, command.sourceType(), command.sourceId());
        task.updateDetails(
                command.title(),
                command.description(),
                command.priority(),
                command.assigneeRole(),
                command.sourceType(),
                command.sourceId(),
                command.dueDate(),
                command.reminderDate(),
                command.status()
        );
        activities.save(new ActivityEvent(user, property, "TASK", "Aufgabe bearbeitet: " + task.title()));
        audit.record(user, property, "task.update", "work_task", task.id(), "Aufgabe bearbeitet: " + task.title());
        return dashboard(userId, property.id());
    }

    @Transactional
    public DashboardView deleteTask(UUID userId, UUID taskId) {
        AppUser user = user(userId);
        WorkTask task = tasks.findById(taskId).orElseThrow(() -> new IllegalArgumentException("Aufgabe nicht gefunden."));
        PropertyAsset property = task.property();
        requireCollaborator(user, property);
        String title = task.title();
        tasks.delete(task);
        activities.save(new ActivityEvent(user, property, "TASK", "Aufgabe gelöscht: " + title));
        audit.record(user, property, "task.delete", "work_task", taskId, "Aufgabe gelöscht: " + title);
        return dashboard(userId, property.id());
    }

    @Transactional
    public DashboardView createFinance(UUID userId, CreateFinanceCommand command) {
        AppUser user = user(userId);
        PropertyAsset property = propertyForAdmin(user, command.propertyId());
        OwnerUnit unit = command.ownerUnitId() == null ? null : unitFor(property, command.ownerUnitId());
        FinanceEvent finance = finances.save(new FinanceEvent(
                property,
                command.label(),
                command.eventType(),
                normalizedFinanceAmount(command.eventType(), command.amount()),
                command.category(),
                command.allocationKey(),
                unit,
                command.bookedOn(),
                command.dueDate(),
                command.paidOn(),
                command.counterparty(),
                command.invoiceNumber(),
                command.documentReference(),
                command.status()
        ));
        activities.save(new ActivityEvent(user, property, "FINANCE", "Finanzereignis erfasst: " + command.label()));
        audit.record(user, property, "finance.create", "finance_event", finance.id(), "Finanzereignis erfasst: " + command.label());
        return dashboard(userId, property.id());
    }

    @Transactional
    public DashboardView createHouseMoneyAssessment(UUID userId, CreateHouseMoneyAssessmentCommand command) {
        AppUser user = user(userId);
        PropertyAsset property = propertyForAdmin(user, command.propertyId());
        OwnerUnit unit = unitFor(property, command.unitId());
        HouseMoneyAssessment assessment = houseMoneyAssessments.save(new HouseMoneyAssessment(
                property,
                unit,
                command.fiscalYear(),
                command.monthlyHouseMoney(),
                command.monthlyReserveContribution(),
                command.validFrom(),
                command.status()
        ));
        activities.save(new ActivityEvent(user, property, "FINANCE", "Hausgeld-Soll angelegt: " + unit.unitLabel() + " " + command.fiscalYear()));
        audit.record(user, property, "house_money.create", "house_money_assessment", assessment.id(), "Hausgeld-Soll angelegt: " + unit.unitLabel());
        return dashboard(userId, property.id());
    }

    @Transactional
    public DashboardView createAnnualPlan(UUID userId, CreateAnnualPlanCommand command) {
        AppUser user = user(userId);
        PropertyAsset property = propertyForAdmin(user, command.propertyId());
        AnnualPlan plan = annualPlans.save(new AnnualPlan(
                property,
                command.fiscalYear(),
                command.houseMoneyBudget(),
                command.maintenanceBudget(),
                command.reserveContribution(),
                command.status()
        ));
        activities.save(new ActivityEvent(user, property, "PLAN", "Wirtschaftsplan erfasst: " + command.fiscalYear()));
        audit.record(user, property, "annual_plan.create", "annual_plan", plan.id(), "Wirtschaftsplan angelegt: " + command.fiscalYear());
        return dashboard(userId, property.id());
    }

    @Transactional
    public DashboardView createDocument(UUID userId, CreateDocumentCommand command) {
        AppUser user = user(userId);
        PropertyAsset property = propertyForCollaborator(user, command.propertyId());
        validateDocumentLink(property, command.linkedEntityType(), command.linkedEntityId());
        PropertyDocument document = documents.save(new PropertyDocument(
                property,
                command.title(),
                command.documentType(),
                command.fileName(),
                command.documentDate(),
                command.status(),
                command.visibility(),
                command.source(),
                command.description(),
                command.linkedEntityType(),
                command.linkedEntityId()
        ));
        activities.save(new ActivityEvent(user, property, "DOCUMENT", "Dokument abgelegt: " + command.title()));
        audit.record(user, property, "document.create", "property_document", document.id(), "Dokument abgelegt: " + command.title());
        return dashboard(userId, property.id());
    }

    @Transactional
    public DashboardView uploadDocument(UUID userId, UploadDocumentCommand command) {
        AppUser user = user(userId);
        PropertyAsset property = propertyForCollaborator(user, command.propertyId());
        validateDocumentLink(property, command.linkedEntityType(), command.linkedEntityId());
        PropertyDocument document = new PropertyDocument(
                property,
                command.title(),
                command.documentType(),
                command.originalFileName(),
                command.documentDate(),
                command.status(),
                command.visibility(),
                command.source(),
                command.description(),
                command.linkedEntityType(),
                command.linkedEntityId()
        );
        DocumentStorage.StoredDocument stored = documentStorage.store(
                document.id(),
                command.originalFileName(),
                command.contentType(),
                command.fileSizeBytes(),
                command.inputStream()
        );
        document.attachFile(stored.storageKey(), stored.contentType(), stored.fileSizeBytes(), stored.sha256Checksum());
        documents.save(document);
        activities.save(new ActivityEvent(user, property, "DOCUMENT", "Datei hochgeladen: " + command.title()));
        audit.record(user, property, "document.upload", "property_document", document.id(), "Datei hochgeladen: " + command.originalFileName());
        return dashboard(userId, property.id());
    }

    @Transactional(readOnly = true)
    public DocumentDownload downloadDocument(UUID userId, UUID documentId) {
        AppUser user = user(userId);
        PropertyDocument document = documents.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Dokument nicht gefunden."));
        requireDocumentView(user, document);
        if (!document.hasFile()) {
            throw new IllegalArgumentException("Für dieses Dokument ist keine Datei hinterlegt.");
        }
        DocumentStorage.StoredDocumentFile stored = documentStorage.load(document.storageKey());
        return new DocumentDownload(
                document.fileName(),
                document.contentType() == null ? "application/octet-stream" : document.contentType(),
                stored.fileSizeBytes(),
                stored.resource()
        );
    }

    @Transactional
    public DashboardView createMeeting(UUID userId, CreateMeetingCommand command) {
        AppUser user = user(userId);
        PropertyAsset property = propertyForAdmin(user, command.propertyId());
        OwnerMeeting meeting = ownerMeetings.save(new OwnerMeeting(
                property,
                command.title(),
                command.meetingDate(),
                command.location(),
                command.agenda(),
                command.invitationSentOn(),
                command.responseDeadline(),
                command.quorumRequirement(),
                command.status()
        ));
        activities.save(new ActivityEvent(user, property, "MEETING", "Eigentümerversammlung geplant: " + meeting.title()));
        audit.record(user, property, "meeting.create", "owner_meeting", meeting.id(), "Eigentümerversammlung angelegt: " + meeting.title());
        return dashboard(userId, property.id());
    }

    @Transactional
    public DashboardView createMessage(UUID userId, CreateMessageCommand command) {
        AppUser user = user(userId);
        PropertyAsset property = propertyForCollaborator(user, command.propertyId());
        validateWorkContext(property, command.sourceType(), command.sourceId());
        WorkTask followUpTask = null;
        if (command.createFollowUpTask()) {
            validateFollowUp(command);
            followUpTask = tasks.save(new WorkTask(
                    property,
                    command.followUpTitle(),
                    command.followUpDescription(),
                    command.followUpPriority(),
                    command.followUpAssigneeRole(),
                    command.sourceType(),
                    command.sourceId(),
                    command.followUpDueDate(),
                    command.followUpReminderDate()
            ));
            activities.save(new ActivityEvent(user, property, "TASK", "Folgeaufgabe aus Mitteilung erstellt: " + followUpTask.title()));
            audit.record(user, property, "task.create", "work_task", followUpTask.id(), "Folgeaufgabe aus Mitteilung angelegt: " + followUpTask.title());
        }
        CommunityMessage message = messages.save(new CommunityMessage(
                property,
                command.audience(),
                command.subject(),
                command.message(),
                command.status(),
                command.channel(),
                command.sourceType(),
                command.sourceId(),
                followUpTask,
                command.readyToSendOn()
        ));
        activities.save(new ActivityEvent(user, property, "COMMUNICATION", "Mitteilung vorbereitet: " + message.subject() + " an " + message.audience()));
        audit.record(user, property, "message.create", "community_message", message.id(), "Mitteilung vorbereitet: " + message.subject());
        return dashboard(userId, property.id());
    }

    private static void validateFollowUp(CreateMessageCommand command) {
        if (isBlank(command.followUpTitle()) || isBlank(command.followUpDescription()) || isBlank(command.followUpAssigneeRole())
                || command.followUpPriority() == null || command.followUpDueDate() == null) {
            throw new IllegalArgumentException("Folgeaufgabe braucht Titel, Beschreibung, Priorität, Verantwortlichkeit und Fälligkeit.");
        }
    }

    @Transactional
    public DashboardView createDecision(UUID userId, CreateDecisionCommand command) {
        AppUser user = user(userId);
        PropertyAsset property = propertyForAdmin(user, command.propertyId());
        OwnerMeeting meeting = command.meetingId() == null ? null : meetingFor(property, command.meetingId());
        CommunityDecision decision = decisions.save(new CommunityDecision(
                property,
                command.title(),
                command.resolutionText(),
                command.meetingDate(),
                command.meetingLocation(),
                meeting,
                command.agendaItem(),
                command.implementationDueDate(),
                command.responsibleRole(),
                command.costImpact(),
                command.status(),
                command.yesVotes(),
                command.noVotes(),
                command.abstentions()
        ));
        activities.save(new ActivityEvent(user, property, "DECISION", "Beschluss dokumentiert: " + decision.title()));
        audit.record(user, property, "decision.create", "community_decision", decision.id(), "Beschluss dokumentiert: " + decision.title());
        return dashboard(userId, property.id());
    }

    @Transactional
    public DashboardView updateDecisionStatus(UUID userId, UUID decisionId, DecisionStatus status) {
        AppUser user = user(userId);
        CommunityDecision decision = decisions.findById(decisionId).orElseThrow(() -> new IllegalArgumentException("Beschluss nicht gefunden."));
        PropertyAsset property = decision.property();
        requireAdmin(user, property);
        decision.transitionTo(status);
        activities.save(new ActivityEvent(user, property, "DECISION", "Beschluss aktualisiert: " + decision.title() + " ist " + decisionStatusLabel(status) + "."));
        audit.record(user, property, "decision.status", "community_decision", decision.id(), "Beschlussstatus gesetzt: " + status.name());
        return dashboard(userId, property.id());
    }

    private AppUser user(UUID userId) {
        return users.findById(userId).orElseThrow(() -> new IllegalArgumentException("Nutzer nicht gefunden."));
    }

    private List<PropertyAsset> accessibleProperties(AppUser user) {
        Map<UUID, PropertyAsset> result = new LinkedHashMap<>();
        properties.findByOwnerOrderByCreatedAtAsc(user).forEach(property -> result.put(property.id(), property));
        members.findByEmailIgnoreCaseOrderByCreatedAtAsc(user.email()).stream()
                .filter(member -> member.status() != MemberStatus.DISABLED)
                .map(CommunityMember::property)
                .forEach(property -> result.putIfAbsent(property.id(), property));
        return List.copyOf(result.values());
    }

    private PropertyAsset propertyForView(AppUser user, UUID requestedPropertyId) {
        if (requestedPropertyId != null) {
            PropertyAsset property = properties.findById(requestedPropertyId)
                    .orElseThrow(() -> new IllegalArgumentException("Immobilie nicht gefunden."));
            if (!canView(user, property)) {
                throw new IllegalArgumentException("Immobilie gehört nicht zu diesem Workspace.");
            }
            return property;
        }
        return accessibleProperties(user).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Bitte zuerst eine Immobilie hinzufügen."));
    }

    private PropertyAsset propertyForAdmin(AppUser user, UUID requestedPropertyId) {
        PropertyAsset property = propertyForView(user, requestedPropertyId);
        requireAdmin(user, property);
        return property;
    }

    private PropertyAsset propertyForCollaborator(AppUser user, UUID requestedPropertyId) {
        PropertyAsset property = propertyForView(user, requestedPropertyId);
        requireCollaborator(user, property);
        return property;
    }

    private OwnerUnit unitFor(PropertyAsset property, UUID unitId) {
        OwnerUnit unit = units.findById(unitId)
                .orElseThrow(() -> new IllegalArgumentException("Einheit nicht gefunden."));
        if (!unit.property().id().equals(property.id())) {
            throw new IllegalArgumentException("Einheit gehört nicht zu dieser WEG.");
        }
        return unit;
    }

    private OwnerMeeting meetingFor(PropertyAsset property, UUID meetingId) {
        OwnerMeeting meeting = ownerMeetings.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("Versammlung nicht gefunden."));
        if (!meeting.property().id().equals(property.id())) {
            throw new IllegalArgumentException("Versammlung gehört nicht zu dieser WEG.");
        }
        return meeting;
    }

    private void validateDocumentLink(PropertyAsset property, DocumentLinkType linkType, UUID linkedEntityId) {
        if (linkType == DocumentLinkType.GENERAL) {
            if (linkedEntityId != null) {
                throw new IllegalArgumentException("Allgemeine Dokumente dürfen kein Zielobjekt setzen.");
            }
            return;
        }
        if (linkedEntityId == null) {
            throw new IllegalArgumentException("Bitte ein Zielobjekt für das Dokument auswählen.");
        }
        boolean sameProperty = switch (linkType) {
            case FINANCE -> finances.findById(linkedEntityId)
                    .map(finance -> finance.property().id().equals(property.id()))
                    .orElse(false);
            case DECISION -> decisions.findById(linkedEntityId)
                    .map(decision -> decision.property().id().equals(property.id()))
                    .orElse(false);
            case MEETING -> ownerMeetings.findById(linkedEntityId)
                    .map(meeting -> meeting.property().id().equals(property.id()))
                    .orElse(false);
            case GENERAL -> true;
        };
        if (!sameProperty) {
            throw new IllegalArgumentException("Dokument-Zuordnung gehört nicht zu dieser WEG.");
        }
    }

    private void validateWorkContext(PropertyAsset property, WorkContextType sourceType, UUID sourceId) {
        if (sourceType == WorkContextType.MANUAL) {
            if (sourceId != null) {
                throw new IllegalArgumentException("Manuelle Vorgänge dürfen kein Zielobjekt setzen.");
            }
            return;
        }
        if (sourceId == null) {
            throw new IllegalArgumentException("Bitte ein Zielobjekt für den Vorgang auswählen.");
        }
        boolean sameProperty = switch (sourceType) {
            case FINANCE -> finances.findById(sourceId)
                    .map(finance -> finance.property().id().equals(property.id()))
                    .orElse(false);
            case DOCUMENT -> documents.findById(sourceId)
                    .map(document -> document.property().id().equals(property.id()))
                    .orElse(false);
            case DECISION -> decisions.findById(sourceId)
                    .map(decision -> decision.property().id().equals(property.id()))
                    .orElse(false);
            case MEETING -> ownerMeetings.findById(sourceId)
                    .map(meeting -> meeting.property().id().equals(property.id()))
                    .orElse(false);
            case MANUAL -> true;
        };
        if (!sameProperty) {
            throw new IllegalArgumentException("Vorgangs-Zuordnung gehört nicht zu dieser WEG.");
        }
    }

    private void requireAdmin(AppUser user, PropertyAsset property) {
        if (!canAdmin(user, property)) {
            throw new IllegalArgumentException("Für diese WEG fehlt eine Verwaltungsrolle.");
        }
    }

    private void requireCollaborator(AppUser user, PropertyAsset property) {
        if (!canCollaborate(user, property)) {
            throw new IllegalArgumentException("Für diese WEG fehlt eine Bearbeitungsrolle.");
        }
    }

    private void requireDocumentView(AppUser user, PropertyDocument document) {
        PropertyAsset property = document.property();
        if (!canView(user, property)) {
            throw new IllegalArgumentException("Dokument gehört nicht zu diesem Workspace.");
        }
        boolean allowed = switch (document.visibility()) {
            case ALL_OWNERS -> true;
            case BOARD_ONLY -> canCollaborate(user, property);
            case MANAGEMENT_ONLY, PRIVATE -> canAdmin(user, property);
        };
        if (!allowed) {
            throw new IllegalArgumentException("Für dieses Dokument fehlt die Berechtigung.");
        }
    }

    private boolean canView(AppUser user, PropertyAsset property) {
        return property.owner().id().equals(user.id())
                || members.findByPropertyAndEmailIgnoreCase(property, user.email())
                .filter(member -> member.status() != MemberStatus.DISABLED)
                .isPresent();
    }

    private boolean canAdmin(AppUser user, PropertyAsset property) {
        if (property.owner().id().equals(user.id())) {
            return true;
        }
        return members.findByPropertyAndEmailIgnoreCase(property, user.email())
                .filter(member -> member.status() != MemberStatus.DISABLED)
                .map(member -> member.role() == CommunityRole.OWNER_ADMIN
                        || member.role() == CommunityRole.SELF_MANAGER
                        || member.role() == CommunityRole.PROPERTY_MANAGER)
                .orElse(false);
    }

    private boolean canCollaborate(AppUser user, PropertyAsset property) {
        if (canAdmin(user, property)) {
            return true;
        }
        return members.findByPropertyAndEmailIgnoreCase(property, user.email())
                .filter(member -> member.status() != MemberStatus.DISABLED)
                .map(member -> member.role() == CommunityRole.BOARD_MEMBER)
                .orElse(false);
    }

    private void upsertMemberFromUnit(PropertyAsset property, AppUser actor, String fullName, String email) {
        String normalizedEmail = normalizeEmail(email);
        members.findByPropertyAndEmailIgnoreCase(property, normalizedEmail)
                .ifPresentOrElse(existing -> {
                    if (existing.status() != MemberStatus.ACTIVE || !existing.email().equalsIgnoreCase(actor.email())) {
                        existing.updateInvite(fullName, normalizedEmail, existing.role());
                    }
                }, () -> {
                    CommunityMember member = actor.email().equalsIgnoreCase(normalizedEmail)
                            ? CommunityMember.active(property, actor, CommunityRole.OWNER_ADMIN)
                            : CommunityMember.invited(property, fullName, normalizedEmail, CommunityRole.OWNER);
                    members.save(member);
                });
    }

    private ReadinessView readiness(PropertyAsset property, List<OwnerUnit> ownerUnits, List<CommunityMember> communityMembers) {
        BigDecimal shareTotal = ownerUnits.stream()
                .map(OwnerUnit::shareValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal missingShare = property.shareTotal().subtract(shareTotal);
        boolean unitCountComplete = ownerUnits.size() == property.unitCount();
        boolean shareDistributionComplete = unitCountComplete && missingShare.compareTo(BigDecimal.ZERO) == 0;
        boolean everyUnitHasMember = ownerUnits.stream()
                .allMatch(unit -> communityMembers.stream()
                        .anyMatch(member -> member.email().equalsIgnoreCase(unit.ownerEmail())));
        int invitedMembers = (int) communityMembers.stream().filter(member -> member.status() == MemberStatus.INVITED).count();
        int activeMembers = (int) communityMembers.stream().filter(member -> member.status() == MemberStatus.ACTIVE).count();
        boolean rolesReady = !ownerUnits.isEmpty() && everyUnitHasMember;
        List<String> blockers = new java.util.ArrayList<>();
        if (!unitCountComplete) {
            blockers.add("Es fehlen " + (property.unitCount() - ownerUnits.size()) + " Einheiten.");
        }
        if (missingShare.compareTo(BigDecimal.ZERO) != 0) {
            blockers.add("Die MEA-Summe weicht um " + missingShare.abs() + " vom Zielwert ab.");
        }
        if (!rolesReady) {
            blockers.add("Nicht alle Einheiten haben eine zugeordnete Rolle.");
        }
        return new ReadinessView(
                shareTotal,
                missingShare,
                shareDistributionComplete,
                property.shareTotal(),
                property.unitCount(),
                ownerUnits.size(),
                invitedMembers,
                activeMembers,
                rolesReady,
                shareDistributionComplete && rolesReady,
                blockers
        );
    }

    private static OnboardingView onboarding(boolean hasProperty, boolean hasUnits, boolean sharesComplete, boolean rolesInvited, boolean hasFinance, boolean hasTasks, boolean hasDecisions, boolean hasAnnualPlan, boolean hasMeeting) {
        int completed = 1
                + (hasProperty ? 1 : 0)
                + (hasUnits ? 1 : 0)
                + (sharesComplete ? 1 : 0)
                + (rolesInvited ? 1 : 0)
                + (hasFinance ? 1 : 0)
                + (hasTasks ? 1 : 0)
                + (hasDecisions ? 1 : 0)
                + (hasAnnualPlan ? 1 : 0)
                + (hasMeeting ? 1 : 0);
        return new OnboardingView(Math.round(completed * 100f / 10f), true, hasProperty, hasUnits, sharesComplete, rolesInvited, hasFinance, hasTasks, hasDecisions, hasAnnualPlan, hasMeeting);
    }

    private static List<InsightView> insights(
            PropertyAsset property,
            List<OwnerUnit> ownerUnits,
            List<CommunityMember> communityMembers,
            List<WorkTask> workTasks,
            List<FinanceEvent> financeEvents,
            List<AnnualPlan> annualPlans,
            List<PropertyDocument> documents,
            List<CommunityDecision> decisions,
            List<OwnerMeeting> meetings,
            BigDecimal pendingPayments,
            ReadinessView readiness
    ) {
        List<InsightView> result = new java.util.ArrayList<>();
        if (ownerUnits.isEmpty()) {
            result.add(new InsightView("HIGH", "Eigentümerstruktur fehlt", "Einheiten und Miteigentumsanteile sind die Grundlage für Umlagen und Beschlüsse.", "units", "Einheiten anlegen"));
        }
        if (!ownerUnits.isEmpty() && !readiness.shareDistributionComplete()) {
            result.add(new InsightView("HIGH", "MEA-Verteilung prüfen", "Die angelegten Miteigentumsanteile ergeben noch nicht " + property.shareTotal().stripTrailingZeros().toPlainString() + ". Finanzen und Beschlüsse brauchen eine belastbare Verteilung.", "units", "MEA prüfen"));
        }
        if (!ownerUnits.isEmpty() && !readiness.rolesReady()) {
            result.add(new InsightView("HIGH", "Rollen vervollständigen", "Jede Einheit braucht eine zugeordnete Eigentümerrolle, damit Einladungen und Beschlüsse nachvollziehbar bleiben.", "units", "Rollen einladen"));
        }
        if (pendingPayments.signum() > 0) {
            result.add(new InsightView("HIGH", "Offene Forderungen klären", "Negative Buchungen sollten geprüft, gebucht oder aktiv nachverfolgt werden.", "finances", "Finanzen prüfen"));
        }
        if (!communityMembers.isEmpty() && readiness.invitedMembers() > 0) {
            result.add(new InsightView("MEDIUM", "Einladungen nachhalten", readiness.invitedMembers() + " Rollen sind eingeladen und sollten vor der ersten Versammlung bestätigt werden.", "units", "Einladungen prüfen"));
        }
        boolean hasOpenTask = workTasks.stream().anyMatch(task -> task.status() != TaskStatus.DONE);
        LocalDate today = LocalDate.now();
        boolean hasOverdueTask = workTasks.stream()
                .filter(task -> task.status() != TaskStatus.DONE)
                .anyMatch(task -> task.dueDate() != null && task.dueDate().isBefore(today));
        boolean hasUpcomingReminder = workTasks.stream()
                .filter(task -> task.status() != TaskStatus.DONE)
                .anyMatch(task -> task.reminderDate() != null && !task.reminderDate().isAfter(today.plusDays(7)));
        boolean hasOpenDecision = decisions.stream().anyMatch(decision -> decision.status() == DecisionStatus.PASSED || decision.status() == DecisionStatus.DRAFT);
        if (hasOverdueTask) {
            result.add(new InsightView("HIGH", "Frist überfällig", "Mindestens eine Aufgabe ist überfällig und sollte Eigentümern oder Beirat transparent nachgehalten werden.", "tasks", "Fristen prüfen"));
        } else if (hasUpcomingReminder) {
            result.add(new InsightView("MEDIUM", "Wiedervorlage steht an", "Eine Aufgabe erreicht bald die Wiedervorlage. Verantwortlichkeit und nächste Kommunikation prüfen.", "tasks", "Aufgaben prüfen"));
        } else if (hasOpenTask) {
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
        return result.stream().limit(4).toList();
    }

    private String workspaceUrl() {
        String baseUrl = realEstateProperties.publicBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            return "https://realestate.stage.dev";
        }
        return baseUrl;
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String roleLabel(CommunityRole role) {
        return switch (role) {
            case OWNER_ADMIN -> "WEG-Admin";
            case SELF_MANAGER -> "Selbstverwalter";
            case PROPERTY_MANAGER -> "Verwaltung";
            case BOARD_MEMBER -> "Beirat";
            case OWNER -> "Eigentümer";
            case EXTERNAL_EXPERT -> "Externer Experte";
        };
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
        return new PropertyView(
                property.id(),
                property.name(),
                property.address(),
                property.city(),
                property.unitCount(),
                property.fiscalYear(),
                property.cashBalance(),
                property.reserveBalance(),
                property.reserveTarget(),
                property.shareTotal(),
                property.managementMode().name(),
                "Aktiv"
        );
    }

    private static UnitView toUnit(OwnerUnit unit) {
        return new UnitView(unit.id(), unit.ownerName(), unit.ownerEmail(), unit.unitLabel(), unit.shareValue(), unit.votingWeight(), unit.occupancyType().name());
    }

    private static TaskView toTask(WorkTask task) {
        return new TaskView(
                task.id(),
                task.title(),
                task.description(),
                task.status().name(),
                task.priority().name(),
                task.assigneeRole(),
                task.sourceType().name(),
                task.sourceId(),
                task.dueDate(),
                task.reminderDate(),
                task.completedAt()
        );
    }

    private static FinanceView toFinance(FinanceEvent finance) {
        OwnerUnit unit = finance.ownerUnit();
        return new FinanceView(
                finance.id(),
                finance.label(),
                finance.eventType().name(),
                finance.amount(),
                finance.category(),
                finance.allocationKey().name(),
                unit == null ? null : unit.id(),
                unit == null ? null : unit.unitLabel(),
                finance.bookedOn(),
                finance.dueDate(),
                finance.paidOn(),
                finance.counterparty(),
                finance.invoiceNumber(),
                finance.documentReference(),
                finance.status()
        );
    }

    private static AssessmentView toAssessment(HouseMoneyAssessment assessment) {
        return new AssessmentView(
                assessment.id(),
                assessment.unit().id(),
                assessment.unit().unitLabel(),
                assessment.fiscalYear(),
                assessment.monthlyHouseMoney(),
                assessment.monthlyReserveContribution(),
                assessment.validFrom(),
                assessment.status().name()
        );
    }

    private static List<UnitBalanceView> unitBalances(List<OwnerUnit> ownerUnits, List<HouseMoneyAssessment> assessments, List<FinanceEvent> financeEvents) {
        return ownerUnits.stream().map(unit -> {
            BigDecimal expectedMonthly = assessments.stream()
                    .filter(assessment -> assessment.unit().id().equals(unit.id()))
                    .filter(assessment -> assessment.status() == AssessmentStatus.ACTIVE)
                    .map(assessment -> assessment.monthlyHouseMoney().add(assessment.monthlyReserveContribution()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal expectedAnnual = expectedMonthly.multiply(BigDecimal.valueOf(12));
            BigDecimal paid = financeEvents.stream()
                    .filter(finance -> finance.ownerUnit() != null && finance.ownerUnit().id().equals(unit.id()))
                    .filter(finance -> finance.eventType() == FinanceEventType.OWNER_PAYMENT)
                    .map(FinanceEvent::amount)
                    .filter(amount -> amount.signum() > 0)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal outstanding = expectedAnnual.subtract(paid);
            if (outstanding.signum() < 0) {
                outstanding = BigDecimal.ZERO;
            }
            return new UnitBalanceView(unit.id(), unit.unitLabel(), unit.ownerName(), expectedAnnual, paid, outstanding);
        }).toList();
    }

    private static BigDecimal normalizedFinanceAmount(FinanceEventType eventType, BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        return switch (eventType) {
            case EXPENSE, REFUND -> amount.abs().negate();
            case OWNER_PAYMENT -> amount.abs();
            default -> amount;
        };
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
        return new DocumentView(
                document.id(),
                document.title(),
                document.documentType(),
                document.fileName(),
                document.documentDate(),
                document.status().name(),
                document.visibility().name(),
                document.source(),
                document.description(),
                document.linkedEntityType().name(),
                document.linkedEntityId(),
                document.hasFile(),
                document.contentType(),
                document.fileSizeBytes(),
                document.sha256Checksum(),
                document.uploadedAt()
        );
    }

    private static DecisionView toDecision(CommunityDecision decision) {
        OwnerMeeting meeting = decision.meeting();
        return new DecisionView(
                decision.id(),
                decision.title(),
                decision.resolutionText(),
                decision.meetingDate(),
                decision.meetingLocation(),
                meeting == null ? null : meeting.id(),
                meeting == null ? null : meeting.title(),
                decision.agendaItem(),
                decision.implementationDueDate(),
                decision.responsibleRole(),
                decision.costImpact(),
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
                meeting.invitationSentOn(),
                meeting.responseDeadline(),
                meeting.quorumRequirement(),
                meeting.status().name()
        );
    }

    private static MessageView toMessage(CommunityMessage message) {
        WorkTask followUpTask = message.followUpTask();
        return new MessageView(
                message.id(),
                message.audience(),
                message.subject(),
                message.message(),
                message.status().name(),
                message.channel().name(),
                message.sourceType().name(),
                message.sourceId(),
                followUpTask == null ? null : followUpTask.id(),
                followUpTask == null ? null : followUpTask.title(),
                message.readyToSendOn(),
                message.createdAt(),
                message.sentAt()
        );
    }

    private static MemberView toMember(CommunityMember member) {
        return new MemberView(member.id(), member.fullName(), member.email(), member.role().name(), member.status().name(), member.invitedAt(), member.acceptedAt());
    }

    private static ActivityView toActivity(ActivityEvent event) {
        return new ActivityView(event.eventType(), event.summary(), event.createdAt());
    }

    private static AuditView toAudit(AuditLog log) {
        return new AuditView(
                log.actor().fullName(),
                log.actor().role().name(),
                log.action(),
                log.targetType(),
                log.targetId(),
                log.summary(),
                log.occurredAt()
        );
    }

    private AccessView accessView(AppUser user, PropertyAsset property) {
        boolean admin = canAdmin(user, property);
        boolean collaborator = canCollaborate(user, property);
        String role = propertyRole(user, property);
        List<String> commands = admin
                ? List.of("WEG verwalten", "Einheiten pflegen", "Finanzen steuern", "Beschlüsse führen", "Rollen einladen", "Dokumente ablegen", "Kommunikation und Aufgaben")
                : collaborator
                ? List.of("Dokumente ablegen", "Kommunikation vorbereiten", "Aufgaben steuern")
                : List.of("Workspace lesen");
        return new AccessView(role, admin, collaborator, commands);
    }

    private String propertyRole(AppUser user, PropertyAsset property) {
        if (property.owner().id().equals(user.id())) {
            return "OWNER_ADMIN";
        }
        return members.findByPropertyAndEmailIgnoreCase(property, user.email())
                .map(member -> member.role().name())
                .orElse(user.role().name());
    }

    public record DashboardView(
            UserView user,
            UUID selectedPropertyId,
            List<PropertyView> properties,
            PortfolioMetrics metrics,
            List<UnitView> units,
            List<TaskView> tasks,
            List<FinanceView> finances,
            List<AssessmentView> houseMoneyAssessments,
            List<UnitBalanceView> unitBalances,
            List<AnnualPlanView> annualPlans,
            List<DocumentView> documents,
            List<DecisionView> decisions,
            List<MeetingView> meetings,
            List<MessageView> messages,
            List<MemberView> members,
            ReadinessView readiness,
            AccessView access,
            List<AuditView> audit,
            List<ActivityView> activity,
            List<InsightView> insights,
            OnboardingView onboarding
    ) {
    }

    public record UserView(String email, String fullName, String organizationName) {
    }

    public record PropertyView(UUID id, String name, String address, String city, int unitCount, int fiscalYear, BigDecimal cashBalance, BigDecimal reserveBalance, BigDecimal reserveTarget, BigDecimal shareTotal, String managementMode, String status) {
    }

    public record PortfolioMetrics(int properties, int units, BigDecimal cashBalance, BigDecimal reserveBalance, BigDecimal pendingPayments, int openTasks, int onboardingCompletion) {
        static PortfolioMetrics empty() {
            return new PortfolioMetrics(0, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0, 33);
        }
    }

    public record UnitView(UUID id, String ownerName, String ownerEmail, String unitLabel, BigDecimal shareValue, BigDecimal votingWeight, String occupancyType) {
    }

    public record TaskView(UUID id, String title, String description, String status, String priority, String assigneeRole, String sourceType, UUID sourceId, LocalDate dueDate, LocalDate reminderDate, Instant completedAt) {
    }

    public record FinanceView(UUID id, String label, String eventType, BigDecimal amount, String category, String allocationKey, UUID ownerUnitId, String ownerUnitLabel, LocalDate bookedOn, LocalDate dueDate, LocalDate paidOn, String counterparty, String invoiceNumber, String documentReference, String status) {
    }

    public record AssessmentView(UUID id, UUID unitId, String unitLabel, int fiscalYear, BigDecimal monthlyHouseMoney, BigDecimal monthlyReserveContribution, LocalDate validFrom, String status) {
    }

    public record UnitBalanceView(UUID unitId, String unitLabel, String ownerName, BigDecimal expectedAnnual, BigDecimal paid, BigDecimal outstanding) {
    }

    public record AnnualPlanView(UUID id, int fiscalYear, BigDecimal houseMoneyBudget, BigDecimal maintenanceBudget, BigDecimal reserveContribution, String status) {
    }

    public record DocumentView(UUID id, String title, String documentType, String fileName, LocalDate documentDate, String status, String visibility, String source, String description, String linkedEntityType, UUID linkedEntityId, boolean hasFile, String contentType, Long fileSizeBytes, String sha256Checksum, Instant uploadedAt) {
    }

    public record DecisionView(UUID id, String title, String resolutionText, LocalDate meetingDate, String meetingLocation, UUID meetingId, String meetingTitle, String agendaItem, LocalDate implementationDueDate, String responsibleRole, BigDecimal costImpact, String status, int yesVotes, int noVotes, int abstentions) {
    }

    public record MeetingView(UUID id, String title, LocalDate meetingDate, String location, String agenda, LocalDate invitationSentOn, LocalDate responseDeadline, String quorumRequirement, String status) {
    }

    public record MessageView(UUID id, String audience, String subject, String message, String status, String channel, String sourceType, UUID sourceId, UUID followUpTaskId, String followUpTaskTitle, LocalDate readyToSendOn, Instant createdAt, Instant sentAt) {
    }

    public record MemberView(UUID id, String fullName, String email, String role, String status, Instant invitedAt, Instant acceptedAt) {
    }

    public record ReadinessView(BigDecimal shareValueTotal, BigDecimal missingShareValue, boolean shareDistributionComplete, BigDecimal expectedShareTotal, int expectedUnits, int createdUnits, int invitedMembers, int activeMembers, boolean rolesReady, boolean readyForFinance, List<String> blockers) {
        static ReadinessView empty() {
            return new ReadinessView(BigDecimal.ZERO, BigDecimal.ZERO, false, BigDecimal.ZERO, 0, 0, 0, 0, false, false, List.of("Bitte zuerst eine WEG anlegen."));
        }
    }

    public record AccessView(String role, boolean canAdmin, boolean canCollaborate, List<String> allowedCommands) {
        static AccessView empty() {
            return new AccessView("OWNER_ADMIN", false, false, List.of("Immobilie anlegen"));
        }
    }

    public record AuditView(String actorName, String actorRole, String action, String targetType, UUID targetId, String summary, Instant occurredAt) {
    }

    public record ActivityView(String eventType, String summary, Instant createdAt) {
    }

    public record InsightView(String severity, String title, String description, String actionSection, String actionLabel) {
    }

    public record OnboardingView(int completion, boolean accountActivated, boolean propertyCreated, boolean unitsCreated, boolean sharesComplete, boolean rolesInvited, boolean financeCreated, boolean taskCreated, boolean decisionCreated, boolean annualPlanCreated, boolean meetingCreated) {
    }

    public record CreatePropertyCommand(String name, String address, String city, int unitCount, int fiscalYear, BigDecimal cashBalance, BigDecimal reserveBalance, BigDecimal reserveTarget, BigDecimal shareTotal, ManagementMode managementMode) {
    }

    public record CreateUnitCommand(UUID propertyId, String ownerName, String ownerEmail, String unitLabel, BigDecimal shareValue, BigDecimal votingWeight, OccupancyType occupancyType) {
    }

    public record InviteMemberCommand(UUID propertyId, String fullName, String email, CommunityRole role) {
    }

    public record CreateTaskCommand(UUID propertyId, String title, String description, TaskPriority priority, String assigneeRole, WorkContextType sourceType, UUID sourceId, LocalDate dueDate, LocalDate reminderDate) {
    }

    public record UpdateMemberCommand(String fullName, String email, CommunityRole role, MemberStatus status) {
    }

    public record UpdateTaskCommand(String title, String description, TaskPriority priority, String assigneeRole, WorkContextType sourceType, UUID sourceId, LocalDate dueDate, LocalDate reminderDate, TaskStatus status) {
    }

    public record CreateFinanceCommand(UUID propertyId, String label, FinanceEventType eventType, BigDecimal amount, String category, AllocationKey allocationKey, UUID ownerUnitId, LocalDate bookedOn, LocalDate dueDate, LocalDate paidOn, String counterparty, String invoiceNumber, String documentReference, String status) {
    }

    public record CreateHouseMoneyAssessmentCommand(UUID propertyId, UUID unitId, int fiscalYear, BigDecimal monthlyHouseMoney, BigDecimal monthlyReserveContribution, LocalDate validFrom, AssessmentStatus status) {
    }

    public record CreateAnnualPlanCommand(UUID propertyId, int fiscalYear, BigDecimal houseMoneyBudget, BigDecimal maintenanceBudget, BigDecimal reserveContribution, AnnualPlanStatus status) {
    }

    public record CreateDocumentCommand(UUID propertyId, String title, String documentType, String fileName, LocalDate documentDate, DocumentStatus status, DocumentVisibility visibility, String source, String description, DocumentLinkType linkedEntityType, UUID linkedEntityId) {
    }

    public record UploadDocumentCommand(UUID propertyId, String title, String documentType, LocalDate documentDate, DocumentStatus status, DocumentVisibility visibility, String source, String description, DocumentLinkType linkedEntityType, UUID linkedEntityId, String originalFileName, String contentType, long fileSizeBytes, InputStream inputStream) {
    }

    public record DocumentDownload(String fileName, String contentType, long fileSizeBytes, Resource resource) {
    }

    public record CreateMeetingCommand(UUID propertyId, String title, LocalDate meetingDate, String location, String agenda, LocalDate invitationSentOn, LocalDate responseDeadline, String quorumRequirement, MeetingStatus status) {
    }

    public record CreateMessageCommand(UUID propertyId, String audience, String subject, String message, MessageStatus status, MessageChannel channel, WorkContextType sourceType, UUID sourceId, LocalDate readyToSendOn, boolean createFollowUpTask, String followUpTitle, String followUpDescription, TaskPriority followUpPriority, String followUpAssigneeRole, LocalDate followUpDueDate, LocalDate followUpReminderDate) {
    }

    public record CreateDecisionCommand(UUID propertyId, UUID meetingId, String title, String resolutionText, LocalDate meetingDate, String meetingLocation, String agendaItem, LocalDate implementationDueDate, String responsibleRole, BigDecimal costImpact, DecisionStatus status, int yesVotes, int noVotes, int abstentions) {
    }
}
