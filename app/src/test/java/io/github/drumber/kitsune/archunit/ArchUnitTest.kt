package io.github.drumber.kitsune.archunit

import com.tngtech.archunit.base.DescribedPredicate.not
import com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage
import com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage
import com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleNameEndingWith
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices
import io.github.drumber.kitsune.KitsuneApplication
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
    fun classes_should_not_use_standard_streams() {
        NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS.check(ALL_CLASSES)
    }

    @Test
    fun ui_should_not_depend_on_data_sources() {
        noClasses()
            .that()
            .resideInAPackage("..kitsune.ui..")
            .should()
            .dependOnClassesThat(
                resideInAPackage("..data.source..")
                    .and(
                        not(
                            // exclusion: local user and character models are allowed in the UI
                            resideInAnyPackage(
                                "..data.source.local.user.model..",
                                "..data.source.local.character..",
                                // temporary exclusion: Algolia character search is used in EditProfileFragment
                                "..data.source.network.algolia.model.search.."
                            )
                        )
                    )
            )
            .because("UI classes should not depend on data sources directly")
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
    fun other_layers_should_not_depend_on_data_sources_directly() {
        noClasses()
            .that()
            .resideOutsideOfPackages("..data.source..", "..data.repository..", "..di..")
            .should()
            .dependOnClassesThat(
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