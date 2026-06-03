package com.skyyware.realestate.workspace;

import static org.assertj.core.api.Assertions.assertThat;

import com.skyyware.realestate.identity.AppUser;
import com.skyyware.realestate.identity.AppUserRepository;
import com.skyyware.realestate.identity.AuthService;
import com.skyyware.realestate.decision.DecisionStatus;
import com.skyyware.realestate.task.TaskPriority;
import com.skyyware.realestate.task.TaskStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("ci")
@SpringBootTest
class WorkspaceFlowTest {
    @Autowired
    private AuthService authService;

    @Autowired
    private AppUserRepository users;

    @Autowired
    private WorkspaceService workspaceService;

    @Test
    void freshAccountStartsEmptyAndCanBuildWorkspace() {
        String email = "ship-ready@example.com";
        AuthService.RegistrationResult registration = authService.register(email, "Ship Ready", "WEG Verwaltung GmbH");
        String rawToken = registration.localSetupLink().substring(registration.localSetupLink().indexOf("token=") + 6);

        authService.setPassword(rawToken, "Ready2ship-password");
        AppUser user = users.findByEmail(email).orElseThrow();

        WorkspaceService.DashboardView empty = workspaceService.dashboard(user.id());
        assertThat(empty.properties()).isEmpty();
        assertThat(empty.metrics().cashBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(empty.onboarding().propertyCreated()).isFalse();

        WorkspaceService.DashboardView withProperty = workspaceService.createProperty(user.id(), new WorkspaceService.CreatePropertyCommand(
                "Musterstraße 12",
                "Musterstraße 12",
                "Stuttgart",
                16,
                new BigDecimal("125540.75"),
                new BigDecimal("256780.20")
        ));
        assertThat(withProperty.properties()).hasSize(1);

        workspaceService.createUnit(user.id(), new WorkspaceService.CreateUnitCommand(
                withProperty.selectedPropertyId(),
                "Sascha Dobrochynskyy",
                "Einheit 07",
                new BigDecimal("84.50")
        ));
        workspaceService.createFinance(user.id(), new WorkspaceService.CreateFinanceCommand(
                withProperty.selectedPropertyId(),
                "Rechnung Hausmeisterservice",
                new BigDecimal("-1250.00"),
                "Instandhaltung",
                LocalDate.of(2026, 6, 5),
                "OPEN"
        ));
        workspaceService.createDocument(user.id(), new WorkspaceService.CreateDocumentCommand(
                withProperty.selectedPropertyId(),
                "Protokoll JHV 2026",
                "PDF",
                "protokoll-jhv-2026.pdf",
                LocalDate.of(2026, 6, 3)
        ));
        WorkspaceService.DashboardView withDecision = workspaceService.createDecision(user.id(), new WorkspaceService.CreateDecisionCommand(
                withProperty.selectedPropertyId(),
                "Sanierung Treppenhaus beauftragen",
                "Die Eigentümergemeinschaft beschließt, die Sanierung des Treppenhauses auf Basis des Angebots Nr. 24-118 zu beauftragen.",
                LocalDate.of(2026, 6, 3),
                "Eigentümerversammlung",
                DecisionStatus.PASSED,
                14,
                1,
                1
        ));
        assertThat(withDecision.decisions()).hasSize(1);
        WorkspaceService.DashboardView complete = workspaceService.addTask(user.id(), new WorkspaceService.CreateTaskCommand(
                withProperty.selectedPropertyId(),
                "Versammlung vorbereiten",
                "Einladungspaket prüfen und versenden.",
                TaskPriority.HIGH,
                LocalDate.of(2026, 6, 12)
        ));

        assertThat(complete.units()).hasSize(1);
        assertThat(complete.finances()).hasSize(1);
        assertThat(complete.documents()).hasSize(1);
        assertThat(complete.decisions()).hasSize(1);
        assertThat(complete.tasks()).hasSize(1);
        assertThat(complete.metrics().pendingPayments()).isEqualByComparingTo("1250.00");
        assertThat(complete.metrics().openTasks()).isEqualTo(1);
        assertThat(complete.onboarding().completion()).isEqualTo(100);
        assertThat(complete.insights()).extracting(WorkspaceService.InsightView::title)
                .contains("Offene Forderungen klären", "Nächste Aufgabe steuern", "Beschluss umsetzen");

        WorkspaceService.DashboardView decisionDone = workspaceService.updateDecisionStatus(
                user.id(),
                complete.decisions().getFirst().id(),
                DecisionStatus.IMPLEMENTED
        );
        assertThat(decisionDone.decisions().getFirst().status()).isEqualTo("IMPLEMENTED");

        WorkspaceService.DashboardView inReview = workspaceService.updateTaskStatus(
                user.id(),
                complete.tasks().getFirst().id(),
                TaskStatus.IN_REVIEW
        );
        assertThat(inReview.tasks().getFirst().status()).isEqualTo("IN_REVIEW");
        assertThat(inReview.metrics().openTasks()).isEqualTo(1);

        WorkspaceService.DashboardView taskDone = workspaceService.updateTaskStatus(
                user.id(),
                complete.tasks().getFirst().id(),
                TaskStatus.DONE
        );
        assertThat(taskDone.tasks().getFirst().status()).isEqualTo("DONE");
        assertThat(taskDone.metrics().openTasks()).isZero();
        assertThat(taskDone.insights()).extracting(WorkspaceService.InsightView::title)
                .doesNotContain("Nächste Aufgabe steuern");

        WorkspaceService.DashboardView secondProperty = workspaceService.createProperty(user.id(), new WorkspaceService.CreatePropertyCommand(
                "Neckarblick 4",
                "Neckarblick 4",
                "Stuttgart",
                8,
                new BigDecimal("42000.00"),
                new BigDecimal("88000.00")
        ));
        assertThat(secondProperty.selectedPropertyId()).isNotEqualTo(withProperty.selectedPropertyId());
        assertThat(secondProperty.units()).isEmpty();
        assertThat(secondProperty.metrics().cashBalance()).isEqualByComparingTo("42000.00");

        workspaceService.createUnit(user.id(), new WorkspaceService.CreateUnitCommand(
                secondProperty.selectedPropertyId(),
                "Beirat Stuttgart",
                "Einheit 02",
                new BigDecimal("92.00")
        ));

        WorkspaceService.DashboardView firstAgain = workspaceService.dashboard(user.id(), withProperty.selectedPropertyId());
        WorkspaceService.DashboardView secondAgain = workspaceService.dashboard(user.id(), secondProperty.selectedPropertyId());

        assertThat(firstAgain.units()).extracting(WorkspaceService.UnitView::unitLabel).containsExactly("Einheit 07");
        assertThat(secondAgain.units()).extracting(WorkspaceService.UnitView::unitLabel).containsExactly("Einheit 02");
    }
}
