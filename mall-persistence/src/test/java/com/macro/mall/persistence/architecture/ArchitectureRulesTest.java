package com.macro.mall.persistence.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.macro.mall.persistence.PersistenceModule;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

class ArchitectureRulesTest {

  private static final String ROOT_PACKAGE = "com.macro.mall.persistence";

  @Test
  void persistenceClassesRespectModuleAndReservedLayerBoundaries() {
    JavaClasses imported = importProductionClasses();
    assertMarkerWasImported(imported);

    noClasses()
        .that()
        .resideInAPackage(ROOT_PACKAGE + "..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "com.macro.mall.security..",
            "com.macro.mall.admin..",
            "com.macro.mall.portal..",
            "com.macro.mall.search..")
        .check(imported);

    checkReservedLayerRules(imported);
  }

  private static JavaClasses importProductionClasses() {
    return new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages(ROOT_PACKAGE);
  }

  private static void assertMarkerWasImported(JavaClasses imported) {
    assertTrue(
        imported.stream()
            .map(JavaClass::getName)
            .anyMatch(PersistenceModule.class.getName()::equals),
        "Architecture rules must inspect the persistence module marker");
  }

  private static void checkReservedLayerRules(JavaClasses imported) {
    noClasses()
        .that()
        .resideInAPackage("..domain..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage("..api..", "..infrastructure..")
        .allowEmptyShould(true)
        .check(imported);
    noClasses()
        .that()
        .resideInAPackage("..application..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("..api..")
        .allowEmptyShould(true)
        .check(imported);
    noClasses()
        .that()
        .resideInAPackage("..api..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("com.macro.mall.persistence.infrastructure..")
        .allowEmptyShould(true)
        .check(imported);
    classes()
        .that()
        .haveSimpleNameEndingWith("Controller")
        .should()
        .resideInAPackage("..api..")
        .allowEmptyShould(true)
        .check(imported);
    classes()
        .that()
        .haveSimpleNameEndingWith("Repository")
        .or()
        .haveSimpleNameEndingWith("Mapper")
        .or()
        .haveSimpleNameEndingWith("Configuration")
        .should()
        .resideInAPackage("..infrastructure..")
        .allowEmptyShould(true)
        .check(imported);
  }
}
