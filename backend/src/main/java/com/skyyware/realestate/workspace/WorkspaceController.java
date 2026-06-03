package com.skyyware.realestate.workspace;

import com.skyyware.realestate.security.CurrentUser;
import com.skyyware.realestate.task.TaskPriority;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspace")
public class WorkspaceController {
    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @GetMapping("/dashboard")
    WorkspaceService.DashboardView dashboard() {
        return workspaceService.dashboard(CurrentUser.require().userId());
    }

    @PostMapping("/properties")
    WorkspaceService.DashboardView createProperty(@Valid @RequestBody CreatePropertyRequest request) {
        return workspaceService.createProperty(CurrentUser.require().userId(), new WorkspaceService.CreatePropertyCommand(
                request.name(),
                request.address(),
                request.city(),
                request.unitCount(),
                request.cashBalance(),
                request.reserveBalance()
        ));
    }

    @PostMapping("/units")
    WorkspaceService.DashboardView createUnit(@Valid @RequestBody CreateUnitRequest request) {
        return workspaceService.createUnit(CurrentUser.require().userId(), new WorkspaceService.CreateUnitCommand(
                request.propertyId(),
                request.ownerName(),
                request.unitLabel(),
                request.shareValue()
        ));
    }

    @PostMapping("/tasks")
    WorkspaceService.DashboardView addTask(@Valid @RequestBody CreateTaskRequest request) {
        return workspaceService.addTask(CurrentUser.require().userId(), new WorkspaceService.CreateTaskCommand(
                request.propertyId(),
                request.title(),
                request.description(),
                request.priority(),
                request.dueDate()
        ));
    }

    @PostMapping("/finances")
    WorkspaceService.DashboardView createFinance(@Valid @RequestBody CreateFinanceRequest request) {
        return workspaceService.createFinance(CurrentUser.require().userId(), new WorkspaceService.CreateFinanceCommand(
                request.propertyId(),
                request.label(),
                request.amount(),
                request.category(),
                request.bookedOn(),
                request.status()
        ));
    }

    @PostMapping("/documents")
    WorkspaceService.DashboardView createDocument(@Valid @RequestBody CreateDocumentRequest request) {
        return workspaceService.createDocument(CurrentUser.require().userId(), new WorkspaceService.CreateDocumentCommand(
                request.propertyId(),
                request.title(),
                request.documentType(),
                request.fileName(),
                request.documentDate()
        ));
    }

    public record CreatePropertyRequest(
            @NotBlank @Size(max = 180) String name,
            @NotBlank @Size(max = 240) String address,
            @NotBlank @Size(max = 120) String city,
            @Min(1) int unitCount,
            @NotNull @DecimalMin("0.00") BigDecimal cashBalance,
            @NotNull @DecimalMin("0.00") BigDecimal reserveBalance
    ) {
    }

    public record CreateUnitRequest(
            UUID propertyId,
            @NotBlank @Size(max = 180) String ownerName,
            @NotBlank @Size(max = 80) String unitLabel,
            @NotNull @DecimalMin("0.00") BigDecimal shareValue
    ) {
    }

    public record CreateTaskRequest(
            UUID propertyId,
            @NotBlank @Size(max = 180) String title,
            @NotBlank @Size(max = 1000) String description,
            @NotNull TaskPriority priority,
            LocalDate dueDate
    ) {
    }

    public record CreateFinanceRequest(
            UUID propertyId,
            @NotBlank @Size(max = 180) String label,
            @NotNull BigDecimal amount,
            @NotBlank @Size(max = 80) String category,
            @NotNull LocalDate bookedOn,
            @NotBlank @Size(max = 32) String status
    ) {
    }

    public record CreateDocumentRequest(
            UUID propertyId,
            @NotBlank @Size(max = 180) String title,
            @NotBlank @Size(max = 80) String documentType,
            @NotBlank @Size(max = 240) String fileName,
            @NotNull LocalDate documentDate
    ) {
    }
}
