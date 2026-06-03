package com.skyyware.realestate.workspace;

import com.skyyware.realestate.security.CurrentUser;
import com.skyyware.realestate.task.TaskPriority;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
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

    @PostMapping("/tasks")
    WorkspaceService.DashboardView addTask(@Valid @RequestBody CreateTaskRequest request) {
        return workspaceService.addTask(CurrentUser.require().userId(), new WorkspaceService.CreateTaskCommand(
                request.title(),
                request.description(),
                request.priority(),
                request.dueDate()
        ));
    }

    public record CreateTaskRequest(
            @NotBlank @Size(max = 180) String title,
            @NotBlank @Size(max = 1000) String description,
            @NotNull TaskPriority priority,
            LocalDate dueDate
    ) {
    }
}
