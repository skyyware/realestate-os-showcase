package com.skyyware.realestate.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.skyyware.realestate")
class ArchitectureBoundaryTest {
    @ArchTest
    static final ArchRule domain_modules_do_not_depend_on_workspace_api =
            noClasses()
                    .that().resideInAnyPackage(
                            "..activity..",
                            "..audit..",
                            "..communication..",
                            "..decision..",
                            "..document..",
                            "..finance..",
                            "..identity..",
                            "..meeting..",
                            "..planning..",
                            "..property..",
                            "..task.."
                    )
                    .should().dependOnClassesThat().resideInAPackage("..workspace..");

    @ArchTest
    static final ArchRule domain_modules_do_not_depend_on_delivery_infrastructure =
            noClasses()
                    .that().resideInAnyPackage(
                            "..decision..",
                            "..document..",
                            "..finance..",
                            "..communication..",
                            "..meeting..",
                            "..planning..",
                            "..property..",
                            "..task.."
                    )
                    .should().dependOnClassesThat().resideInAnyPackage("..mail..", "..security..");
}
