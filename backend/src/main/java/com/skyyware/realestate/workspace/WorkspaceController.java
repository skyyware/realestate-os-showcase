package com.skyyware.realestate.workspace;

import com.skyyware.realestate.common.WorkContextType;
import com.skyyware.realestate.communication.MessageChannel;
import com.skyyware.realestate.communication.MessageStatus;
import com.skyyware.realestate.decision.DecisionStatus;
import com.skyyware.realestate.document.DocumentLinkType;
import com.skyyware.realestate.document.DocumentStatus;
import com.skyyware.realestate.document.DocumentVisibility;
import com.skyyware.realestate.finance.AllocationKey;
import com.skyyware.realestate.finance.AssessmentStatus;
import com.skyyware.realestate.finance.FinanceEventType;
import com.skyyware.realestate.meeting.MeetingStatus;
import com.skyyware.realestate.planning.AnnualPlanStatus;
import com.skyyware.realestate.property.CommunityRole;
import com.skyyware.realestate.property.ManagementMode;
import com.skyyware.realestate.property.MemberStatus;
import com.skyyware.realestate.property.OccupancyType;
import com.skyyware.realestate.security.CurrentUser;
import com.skyyware.realestate.task.TaskPriority;
import com.skyyware.realestate.task.TaskStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspace")
public class WorkspaceController {
    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @GetMapping("/dashboard")
    WorkspaceService.DashboardView dashboard(@RequestParam(required = false) UUID propertyId) {
        return workspaceService.dashboard(CurrentUser.require().userId(), propertyId);
    }

    @PostMapping("/properties")
    @PreAuthorize("hasAnyRole('OWNER_ADMIN','PROPERTY_MANAGER')")
    WorkspaceService.DashboardView createProperty(@Valid @RequestBody CreatePropertyRequest request) {
        return workspaceService.createProperty(CurrentUser.require().userId(), new WorkspaceService.CreatePropertyCommand(
                request.name(),
                request.address(),
                request.city(),
                request.unitCount(),
                request.fiscalYear(),
                request.cashBalance(),
                request.reserveBalance(),
                request.reserveTarget(),
                request.shareTotal(),
                request.managementMode()
        ));
    }

    @PostMapping("/units")
    @PreAuthorize("hasAnyRole('OWNER_ADMIN','PROPERTY_MANAGER')")
    WorkspaceService.DashboardView createUnit(@Valid @RequestBody CreateUnitRequest request) {
        return workspaceService.createUnit(CurrentUser.require().userId(), new WorkspaceService.CreateUnitCommand(
                request.propertyId(),
                request.ownerName(),
                request.ownerEmail(),
                request.unitLabel(),
                request.shareValue(),
                request.votingWeight(),
                request.occupancyType()
        ));
    }

    @PostMapping("/members")
    @PreAuthorize("hasAnyRole('OWNER_ADMIN','PROPERTY_MANAGER')")
    WorkspaceService.DashboardView inviteMember(@Valid @RequestBody InviteMemberRequest request) {
        return workspaceService.inviteMember(CurrentUser.require().userId(), new WorkspaceService.InviteMemberCommand(
                request.propertyId(),
                request.fullName(),
                request.email(),
                request.role()
        ));
    }

    @PatchMapping("/members/{memberId}")
    @PreAuthorize("hasAnyRole('OWNER_ADMIN','PROPERTY_MANAGER')")
    WorkspaceService.DashboardView updateMember(@PathVariable UUID memberId, @Valid @RequestBody UpdateMemberRequest request) {
        return workspaceService.updateMember(CurrentUser.require().userId(), memberId, new WorkspaceService.UpdateMemberCommand(
                request.fullName(),
                request.email(),
                request.role(),
                request.status()
        ));
    }

    @DeleteMapping("/members/{memberId}")
    @PreAuthorize("hasAnyRole('OWNER_ADMIN','PROPERTY_MANAGER')")
    WorkspaceService.DashboardView disableMember(@PathVariable UUID memberId) {
        return workspaceService.disableMember(CurrentUser.require().userId(), memberId);
    }

    @PostMapping("/tasks")
    @PreAuthorize("hasAnyRole('OWNER_ADMIN','PROPERTY_MANAGER','BOARD_MEMBER')")
    WorkspaceService.DashboardView addTask(@Valid @RequestBody CreateTaskRequest request) {
        return workspaceService.addTask(CurrentUser.require().userId(), new WorkspaceService.CreateTaskCommand(
                request.propertyId(),
                request.title(),
                request.description(),
                request.priority(),
                request.assigneeRole(),
                request.sourceType(),
                request.sourceId(),
                request.dueDate(),
                request.reminderDate()
        ));
    }

    @PutMapping("/tasks/{taskId}")
    @PreAuthorize("hasAnyRole('OWNER_ADMIN','PROPERTY_MANAGER','BOARD_MEMBER')")
    WorkspaceService.DashboardView updateTask(@PathVariable UUID taskId, @Valid @RequestBody UpdateTaskRequest request) {
        return workspaceService.updateTask(CurrentUser.require().userId(), taskId, new WorkspaceService.UpdateTaskCommand(
                request.title(),
                request.description(),
                request.priority(),
                request.assigneeRole(),
                request.sourceType(),
                request.sourceId(),
                request.dueDate(),
                request.reminderDate(),
                request.status()
        ));
    }

    @PatchMapping("/tasks/{taskId}/status")
    @PreAuthorize("hasAnyRole('OWNER_ADMIN','PROPERTY_MANAGER','BOARD_MEMBER')")
    WorkspaceService.DashboardView updateTaskStatus(@PathVariable UUID taskId, @Valid @RequestBody UpdateTaskStatusRequest request) {
        return workspaceService.updateTaskStatus(CurrentUser.require().userId(), taskId, request.status());
    }

    @DeleteMapping("/tasks/{taskId}")
    @PreAuthorize("hasAnyRole('OWNER_ADMIN','PROPERTY_MANAGER','BOARD_MEMBER')")
    WorkspaceService.DashboardView deleteTask(@PathVariable UUID taskId) {
        return workspaceService.deleteTask(CurrentUser.require().userId(), taskId);
    }

    @PostMapping("/finances")
    @PreAuthorize("hasAnyRole('OWNER_ADMIN','PROPERTY_MANAGER')")
    WorkspaceService.DashboardView createFinance(@Valid @RequestBody CreateFinanceRequest request) {
        return workspaceService.createFinance(CurrentUser.require().userId(), new WorkspaceService.CreateFinanceCommand(
                request.propertyId(),
                request.label(),
                request.eventType(),
                request.amount(),
                request.category(),
                request.allocationKey(),
                request.ownerUnitId(),
                request.bookedOn(),
                request.dueDate(),
                request.paidOn(),
                request.counterparty(),
                request.invoiceNumber(),
                request.documentReference(),
                request.status()
        ));
    }

    @PostMapping("/house-money")
    @PreAuthorize("hasAnyRole('OWNER_ADMIN','PROPERTY_MANAGER')")
    WorkspaceService.DashboardView createHouseMoneyAssessment(@Valid @RequestBody CreateHouseMoneyAssessmentRequest request) {
        return workspaceService.createHouseMoneyAssessment(CurrentUser.require().userId(), new WorkspaceService.CreateHouseMoneyAssessmentCommand(
                request.propertyId(),
                request.unitId(),
                request.fiscalYear(),
                request.monthlyHouseMoney(),
                request.monthlyReserveContribution(),
                request.validFrom(),
                request.status()
        ));
    }

    @PostMapping("/annual-plans")
    @PreAuthorize("hasAnyRole('OWNER_ADMIN','PROPERTY_MANAGER')")
    WorkspaceService.DashboardView createAnnualPlan(@Valid @RequestBody CreateAnnualPlanRequest request) {
        return workspaceService.createAnnualPlan(CurrentUser.require().userId(), new WorkspaceService.CreateAnnualPlanCommand(
                request.propertyId(),
                request.fiscalYear(),
                request.houseMoneyBudget(),
                request.maintenanceBudget(),
                request.reserveContribution(),
                request.status()
        ));
    }

    @PostMapping("/documents")
    @PreAuthorize("hasAnyRole('OWNER_ADMIN','PROPERTY_MANAGER','BOARD_MEMBER')")
    WorkspaceService.DashboardView createDocument(@Valid @RequestBody CreateDocumentRequest request) {
        return workspaceService.createDocument(CurrentUser.require().userId(), new WorkspaceService.CreateDocumentCommand(
                request.propertyId(),
                request.title(),
                request.documentType(),
                request.fileName(),
                request.documentDate(),
                request.status(),
                request.visibility(),
                request.source(),
                request.description(),
                request.linkedEntityType(),
                request.linkedEntityId()
        ));
    }

    @PostMapping("/meetings")
    @PreAuthorize("hasAnyRole('OWNER_ADMIN','PROPERTY_MANAGER')")
    WorkspaceService.DashboardView createMeeting(@Valid @RequestBody CreateMeetingRequest request) {
        return workspaceService.createMeeting(CurrentUser.require().userId(), new WorkspaceService.CreateMeetingCommand(
                request.propertyId(),
                request.title(),
                request.meetingDate(),
                request.location(),
                request.agenda(),
                request.invitationSentOn(),
                request.responseDeadline(),
                request.quorumRequirement(),
                request.status()
        ));
    }

    @PostMapping("/messages")
    @PreAuthorize("hasAnyRole('OWNER_ADMIN','PROPERTY_MANAGER','BOARD_MEMBER')")
    WorkspaceService.DashboardView createMessage(@Valid @RequestBody CreateMessageRequest request) {
        return workspaceService.createMessage(CurrentUser.require().userId(), new WorkspaceService.CreateMessageCommand(
                request.propertyId(),
                request.audience(),
                request.subject(),
                request.message(),
                request.status(),
                request.channel(),
                request.sourceType(),
                request.sourceId(),
                request.readyToSendOn(),
                request.createFollowUpTask(),
                request.followUpTitle(),
                request.followUpDescription(),
                request.followUpPriority(),
                request.followUpAssigneeRole(),
                request.followUpDueDate(),
                request.followUpReminderDate()
        ));
    }

    @PostMapping("/decisions")
    @PreAuthorize("hasAnyRole('OWNER_ADMIN','PROPERTY_MANAGER')")
    WorkspaceService.DashboardView createDecision(@Valid @RequestBody CreateDecisionRequest request) {
        return workspaceService.createDecision(CurrentUser.require().userId(), new WorkspaceService.CreateDecisionCommand(
                request.propertyId(),
                request.meetingId(),
                request.title(),
                request.resolutionText(),
                request.meetingDate(),
                request.meetingLocation(),
                request.agendaItem(),
                request.implementationDueDate(),
                request.responsibleRole(),
                request.costImpact(),
                request.status(),
                request.yesVotes(),
                request.noVotes(),
                request.abstentions()
        ));
    }

    @PatchMapping("/decisions/{decisionId}/status")
    @PreAuthorize("hasAnyRole('OWNER_ADMIN','PROPERTY_MANAGER')")
    WorkspaceService.DashboardView updateDecisionStatus(@PathVariable UUID decisionId, @Valid @RequestBody UpdateDecisionStatusRequest request) {
        return workspaceService.updateDecisionStatus(CurrentUser.require().userId(), decisionId, request.status());
    }

    public record CreatePropertyRequest(
            @NotBlank @Size(max = 180) String name,
            @NotBlank @Size(max = 240) String address,
            @NotBlank @Size(max = 120) String city,
            @Min(1) int unitCount,
            @Min(2020) int fiscalYear,
            @NotNull @DecimalMin("0.00") BigDecimal cashBalance,
            @NotNull @DecimalMin("0.00") BigDecimal reserveBalance,
            @NotNull @DecimalMin("0.00") BigDecimal reserveTarget,
            @NotNull @DecimalMin("1.00") BigDecimal shareTotal,
            @NotNull ManagementMode managementMode
    ) {
    }

    public record CreateUnitRequest(
            UUID propertyId,
            @NotBlank @Size(max = 180) String ownerName,
            @NotBlank @Email @Size(max = 320) String ownerEmail,
            @NotBlank @Size(max = 80) String unitLabel,
            @NotNull @DecimalMin("0.00") BigDecimal shareValue,
            @NotNull @DecimalMin("0.00") BigDecimal votingWeight,
            @NotNull OccupancyType occupancyType
    ) {
    }

    public record InviteMemberRequest(
            UUID propertyId,
            @NotBlank @Size(max = 180) String fullName,
            @NotBlank @Email @Size(max = 320) String email,
            @NotNull CommunityRole role
    ) {
    }

    public record UpdateMemberRequest(
            @NotBlank @Size(max = 180) String fullName,
            @NotBlank @Email @Size(max = 320) String email,
            @NotNull CommunityRole role,
            @NotNull MemberStatus status
    ) {
    }

    public record CreateTaskRequest(
            UUID propertyId,
            @NotBlank @Size(max = 180) String title,
            @NotBlank @Size(max = 1000) String description,
            @NotNull TaskPriority priority,
            @NotBlank @Size(max = 80) String assigneeRole,
            @NotNull WorkContextType sourceType,
            UUID sourceId,
            LocalDate dueDate,
            LocalDate reminderDate
    ) {
    }

    public record UpdateTaskRequest(
            @NotBlank @Size(max = 180) String title,
            @NotBlank @Size(max = 1000) String description,
            @NotNull TaskPriority priority,
            @NotBlank @Size(max = 80) String assigneeRole,
            @NotNull WorkContextType sourceType,
            UUID sourceId,
            LocalDate dueDate,
            LocalDate reminderDate,
            @NotNull TaskStatus status
    ) {
    }

    public record UpdateTaskStatusRequest(@NotNull TaskStatus status) {
    }

    public record CreateFinanceRequest(
            UUID propertyId,
            @NotBlank @Size(max = 180) String label,
            @NotNull FinanceEventType eventType,
            @NotNull BigDecimal amount,
            @NotBlank @Size(max = 80) String category,
            @NotNull AllocationKey allocationKey,
            UUID ownerUnitId,
            @NotNull LocalDate bookedOn,
            LocalDate dueDate,
            LocalDate paidOn,
            @Size(max = 180) String counterparty,
            @Size(max = 80) String invoiceNumber,
            @Size(max = 240) String documentReference,
            @NotBlank @Size(max = 32) String status
    ) {
    }

    public record CreateHouseMoneyAssessmentRequest(
            UUID propertyId,
            @NotNull UUID unitId,
            @Min(2020) int fiscalYear,
            @NotNull @DecimalMin("0.00") BigDecimal monthlyHouseMoney,
            @NotNull @DecimalMin("0.00") BigDecimal monthlyReserveContribution,
            @NotNull LocalDate validFrom,
            @NotNull AssessmentStatus status
    ) {
    }

    public record CreateAnnualPlanRequest(
            UUID propertyId,
            @Min(2020) int fiscalYear,
            @NotNull @DecimalMin("0.00") BigDecimal houseMoneyBudget,
            @NotNull @DecimalMin("0.00") BigDecimal maintenanceBudget,
            @NotNull @DecimalMin("0.00") BigDecimal reserveContribution,
            @NotNull AnnualPlanStatus status
    ) {
    }

    public record CreateDocumentRequest(
            UUID propertyId,
            @NotBlank @Size(max = 180) String title,
            @NotBlank @Size(max = 80) String documentType,
            @NotBlank @Size(max = 240) String fileName,
            @NotNull LocalDate documentDate,
            @NotNull DocumentStatus status,
            @NotNull DocumentVisibility visibility,
            @NotBlank @Size(max = 80) String source,
            @Size(max = 1000) String description,
            @NotNull DocumentLinkType linkedEntityType,
            UUID linkedEntityId
    ) {
    }

    public record CreateMeetingRequest(
            UUID propertyId,
            @NotBlank @Size(max = 180) String title,
            @NotNull LocalDate meetingDate,
            @NotBlank @Size(max = 180) String location,
            @NotBlank @Size(max = 1800) String agenda,
            LocalDate invitationSentOn,
            LocalDate responseDeadline,
            @NotBlank @Size(max = 240) String quorumRequirement,
            @NotNull MeetingStatus status
    ) {
    }

    public record CreateMessageRequest(
            UUID propertyId,
            @NotBlank @Size(max = 120) String audience,
            @NotBlank @Size(max = 180) String subject,
            @NotBlank @Size(max = 1200) String message,
            @NotNull MessageStatus status,
            @NotNull MessageChannel channel,
            @NotNull WorkContextType sourceType,
            UUID sourceId,
            LocalDate readyToSendOn,
            boolean createFollowUpTask,
            @Size(max = 180) String followUpTitle,
            @Size(max = 1000) String followUpDescription,
            TaskPriority followUpPriority,
            @Size(max = 80) String followUpAssigneeRole,
            LocalDate followUpDueDate,
            LocalDate followUpReminderDate
    ) {
    }

    public record CreateDecisionRequest(
            UUID propertyId,
            UUID meetingId,
            @NotBlank @Size(max = 180) String title,
            @NotBlank @Size(max = 1600) String resolutionText,
            @NotNull LocalDate meetingDate,
            @NotBlank @Size(max = 180) String meetingLocation,
            @NotBlank @Size(max = 240) String agendaItem,
            LocalDate implementationDueDate,
            @NotBlank @Size(max = 80) String responsibleRole,
            @NotNull @DecimalMin("0.00") BigDecimal costImpact,
            @NotNull DecisionStatus status,
            @Min(0) int yesVotes,
            @Min(0) int noVotes,
            @Min(0) int abstentions
    ) {
    }

    public record UpdateDecisionStatusRequest(@NotNull DecisionStatus status) {
    }
}
