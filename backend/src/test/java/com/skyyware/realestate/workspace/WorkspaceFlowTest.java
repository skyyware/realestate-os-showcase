package com.skyyware.realestate.workspace;

import static org.assertj.core.api.Assertions.assertThat;

import com.skyyware.realestate.identity.AppUser;
import com.skyyware.realestate.identity.AppUserRepository;
import com.skyyware.realestate.identity.AuthService;
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
import com.skyyware.realestate.property.OccupancyType;
import com.skyyware.realestate.task.TaskPriority;
import com.skyyware.realestate.task.TaskStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
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
                1,
                2026,
                new BigDecimal("125540.75"),
                new BigDecimal("256780.20"),
                new BigDecimal("300000.00"),
                new BigDecimal("1000.00"),
                ManagementMode.SELF_MANAGED
        ));
        assertThat(withProperty.properties()).hasSize(1);
        assertThat(withProperty.members()).extracting(WorkspaceService.MemberView::role).contains("OWNER_ADMIN");

        WorkspaceService.DashboardView withUnit = workspaceService.createUnit(user.id(), new WorkspaceService.CreateUnitCommand(
                withProperty.selectedPropertyId(),
                "Sascha Dobrochynskyy",
                email,
                "Einheit 07",
                new BigDecimal("1000.00"),
                new BigDecimal("1000.00"),
                OccupancyType.OWNER_OCCUPIED
        ));
        UUID unitId = withUnit.units().getFirst().id();
        WorkspaceService.DashboardView withBoard = workspaceService.inviteMember(user.id(), new WorkspaceService.InviteMemberCommand(
                withProperty.selectedPropertyId(),
                "Beirat Stuttgart",
                "beirat@example.com",
                CommunityRole.BOARD_MEMBER
        ));
        assertThat(withBoard.readiness().readyForFinance()).isTrue();
        assertThat(withBoard.members()).extracting(WorkspaceService.MemberView::role)
                .contains("OWNER_ADMIN", "BOARD_MEMBER");
        WorkspaceService.DashboardView withAssessment = workspaceService.createHouseMoneyAssessment(user.id(), new WorkspaceService.CreateHouseMoneyAssessmentCommand(
                withProperty.selectedPropertyId(),
                unitId,
                2026,
                new BigDecimal("410.00"),
                new BigDecimal("95.00"),
                LocalDate.of(2026, 1, 1),
                AssessmentStatus.ACTIVE
        ));
        assertThat(withAssessment.houseMoneyAssessments()).hasSize(1);
        assertThat(withAssessment.unitBalances().getFirst().expectedAnnual()).isEqualByComparingTo("6060.00");
        WorkspaceService.DashboardView withFinance = workspaceService.createFinance(user.id(), new WorkspaceService.CreateFinanceCommand(
                withProperty.selectedPropertyId(),
                "Rechnung Hausmeisterservice",
                FinanceEventType.EXPENSE,
                new BigDecimal("1250.00"),
                "Instandhaltung",
                AllocationKey.MEA,
                null,
                LocalDate.of(2026, 6, 5),
                LocalDate.of(2026, 6, 20),
                null,
                "Hausmeisterservice Stuttgart",
                "HM-2026-118",
                "rechnung-hm-2026-118.pdf",
                "OPEN"
        ));
        UUID financeId = withFinance.finances().getFirst().id();
        workspaceService.createAnnualPlan(user.id(), new WorkspaceService.CreateAnnualPlanCommand(
                withProperty.selectedPropertyId(),
                2026,
                new BigDecimal("64000.00"),
                new BigDecimal("18500.00"),
                new BigDecimal("12000.00"),
                AnnualPlanStatus.APPROVED
        ));
        workspaceService.createDocument(user.id(), new WorkspaceService.CreateDocumentCommand(
                withProperty.selectedPropertyId(),
                "Rechnung Hausmeisterservice",
                "Rechnung",
                "rechnung-hm-2026-118.pdf",
                LocalDate.of(2026, 6, 5),
                DocumentStatus.APPROVED,
                DocumentVisibility.ALL_OWNERS,
                "UPLOAD",
                "Geprüfter Beleg zur offenen Forderung.",
                DocumentLinkType.FINANCE,
                financeId
        ));
        workspaceService.createMeeting(user.id(), new WorkspaceService.CreateMeetingCommand(
                withProperty.selectedPropertyId(),
                "Eigentümerversammlung 2026",
                LocalDate.of(2026, 7, 10),
                "Stuttgart und digital",
                "Jahresabrechnung, Wirtschaftsplan, Treppenhaus-Sanierung",
                MeetingStatus.INVITED
        ));
        workspaceService.createMessage(user.id(), new WorkspaceService.CreateMessageCommand(
                withProperty.selectedPropertyId(),
                "Eigentümer",
                "Unterlagen zur Versammlung",
                "Die Unterlagen für die nächste Eigentümerversammlung sind vorbereitet."
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
        WorkspaceService.DashboardView withDecisionDocument = workspaceService.createDocument(user.id(), new WorkspaceService.CreateDocumentCommand(
                withProperty.selectedPropertyId(),
                "Protokoll JHV 2026",
                "Protokoll",
                "protokoll-jhv-2026.pdf",
                LocalDate.of(2026, 6, 3),
                DocumentStatus.APPROVED,
                DocumentVisibility.ALL_OWNERS,
                "UPLOAD",
                "Beschlussprotokoll zur Sanierung.",
                DocumentLinkType.DECISION,
                withDecision.decisions().getFirst().id()
        ));
        WorkspaceService.DashboardView complete = workspaceService.addTask(user.id(), new WorkspaceService.CreateTaskCommand(
                withProperty.selectedPropertyId(),
                "Versammlung vorbereiten",
                "Einladungspaket prüfen und versenden.",
                TaskPriority.HIGH,
                LocalDate.of(2026, 6, 12)
        ));

        assertThat(complete.units()).hasSize(1);
        assertThat(complete.finances()).hasSize(1);
        assertThat(complete.finances().getFirst().eventType()).isEqualTo("EXPENSE");
        assertThat(complete.finances().getFirst().amount()).isEqualByComparingTo("-1250.00");
        assertThat(complete.houseMoneyAssessments()).hasSize(1);
        assertThat(complete.unitBalances().getFirst().outstanding()).isEqualByComparingTo("6060.00");
        assertThat(complete.annualPlans()).hasSize(1);
        assertThat(withDecisionDocument.documents()).hasSize(2);
        assertThat(withDecisionDocument.documents()).extracting(WorkspaceService.DocumentView::linkedEntityType)
                .contains("FINANCE", "DECISION");
        assertThat(complete.documents()).hasSize(2);
        assertThat(complete.meetings()).hasSize(1);
        assertThat(complete.messages()).hasSize(1);
        assertThat(complete.decisions()).hasSize(1);
        assertThat(complete.tasks()).hasSize(1);
        assertThat(complete.readiness().shareDistributionComplete()).isTrue();
        assertThat(complete.readiness().rolesReady()).isTrue();
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
                1,
                2026,
                new BigDecimal("42000.00"),
                new BigDecimal("88000.00"),
                new BigDecimal("120000.00"),
                new BigDecimal("1000.00"),
                ManagementMode.SELF_MANAGED
        ));
        assertThat(secondProperty.selectedPropertyId()).isNotEqualTo(withProperty.selectedPropertyId());
        assertThat(secondProperty.units()).isEmpty();
        assertThat(secondProperty.metrics().cashBalance()).isEqualByComparingTo("42000.00");

        workspaceService.createUnit(user.id(), new WorkspaceService.CreateUnitCommand(
                secondProperty.selectedPropertyId(),
                "Beirat Stuttgart",
                "beirat@example.com",
                "Einheit 02",
                new BigDecimal("1000.00"),
                new BigDecimal("1000.00"),
                OccupancyType.OWNER_OCCUPIED
        ));

        WorkspaceService.DashboardView firstAgain = workspaceService.dashboard(user.id(), withProperty.selectedPropertyId());
        WorkspaceService.DashboardView secondAgain = workspaceService.dashboard(user.id(), secondProperty.selectedPropertyId());

        assertThat(firstAgain.units()).extracting(WorkspaceService.UnitView::unitLabel).containsExactly("Einheit 07");
        assertThat(secondAgain.units()).extracting(WorkspaceService.UnitView::unitLabel).containsExactly("Einheit 02");
    }
}
