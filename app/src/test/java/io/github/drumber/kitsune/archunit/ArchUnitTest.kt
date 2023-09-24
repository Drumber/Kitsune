package io.github.drumber.kitsune.archunit

import androidx.lifecycle.ViewModel
import androidx.room.Entity
import com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage
import com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackage
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

}