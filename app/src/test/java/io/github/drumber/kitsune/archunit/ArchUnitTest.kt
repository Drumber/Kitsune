package io.github.drumber.kitsune.archunit

import androidx.lifecycle.ViewModel
import androidx.room.Entity
import com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage
import com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackage
import com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleNameEndingWith
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices
import io.github.drumber.kitsune.KitsuneApplication
import org.junit.Ignore
import org.junit.Test

class ArchUnitTest {

    companion object {
        private val ALL_CLASSES =
            ClassFileImporter().importPackagesOf(KitsuneApplication::class.java)

        private val NON_TEST_CLASSES = ClassFileImporter().withImportOption {
            !it.matches(".*/(debug|release)UnitTest/.*".toPattern())
        }.importPackagesOf(KitsuneApplication::class.java)

        private val TEST_CLASSES = ClassFileImporter().withImportOption {
            it.matches(".*/(debug|release)UnitTest/.*".toPattern())
        }.importPackagesOf(KitsuneApplication::class.java)
    }

    @Test
    fun classesShouldNotUseStandardStreams() {
        NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS.check(NON_TEST_CLASSES)
    }

    @Test
    fun uiClassesShouldNotAccessDatabaseEntities() {
        noClasses()
            .that()
            .resideInAPackage("..kitsune.ui..")
            .and()
            .areNotAssignableTo(ViewModel::class.java)
            .and()
            .areNotAnonymousClasses()
            .should()
            .accessClassesThat()
            .areAnnotatedWith(Entity::class.java)
            .because("UI classes should not depend on the database")
            .check(NON_TEST_CLASSES)
    }

    @Ignore("Deactivated during recode of domain logic")
    @Test
    fun differentModelLayersShouldNotDependOnEachOther() {
        slices()
            .matching("..domain.model.(*)..")
            .should()
            .notDependOnEachOther()
            // any classes outside 'common' are allowed to depend on 'common'
            .ignoreDependency(
                resideOutsideOfPackage("..common.."),
                resideInAPackage("..common..")
            )
            // classes in 'ui' are allowed to depend on 'infrastructure' classes
            .ignoreDependency(
                resideInAPackage("..ui.."),
                resideInAPackage("..infrastructure..")
            )
            .because("different model layers should be independent from each other")
            .check(NON_TEST_CLASSES)
    }

    @Test
    fun local_and_network_data_sources_should_not_depend_on_each_other() {
        slices()
            .matching("..data.source.(*)..")
            .should()
            .notDependOnEachOther()
            .because("local and network data sources should be independent from each other")
            .check(NON_TEST_CLASSES)
    }

    @Test
    fun other_layers_should_not_access_data_sources_directly() {
        noClasses()
            .that()
            .resideOutsideOfPackages("..data.source..", "..data.repository..", "..di..")
            .should()
            .accessClassesThat(
                resideInAPackage("..data.source..")
                    .and(simpleNameEndingWith("DataSource"))
            )
            .because("data sources should only be accessed by the repositories")
            .check(NON_TEST_CLASSES)
    }

    @Test
    fun presentation_model_classes_should_not_depend_on_data_sources() {
        noClasses()
            .that()
            .resideInAPackage("..data.presentation.model..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..data.source..")
            .because("presentation model classes should not depend on data sources")
            .check(NON_TEST_CLASSES)
    }

    @Test
    fun common_classes_should_not_depend_on_data_sources() {
        noClasses()
            .that()
            .resideInAPackage("..data.common..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..data.source..")
            .because("common classes should not depend on data sources")
            .check(NON_TEST_CLASSES)
    }

    @Test
    fun data_sources_should_not_depend_on_presentation_classes() {
        noClasses()
            .that()
            .resideInAPackage("..data.source..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..data.presentation..")
            .because("data sources classes should not depend on presentation classes")
            .check(NON_TEST_CLASSES)
    }

}