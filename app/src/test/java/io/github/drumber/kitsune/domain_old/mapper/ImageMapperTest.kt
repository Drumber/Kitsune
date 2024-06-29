package io.github.drumber.kitsune.domain_old.mapper

import io.github.drumber.kitsune.domain_old.model.database.DBDimension
import io.github.drumber.kitsune.domain_old.model.database.DBDimensions
import io.github.drumber.kitsune.domain_old.model.database.DBImage
import io.github.drumber.kitsune.domain_old.model.database.DBImageMeta
import io.github.drumber.kitsune.domain_old.model.infrastructure.algolia.search.AlgoliaDimension
import io.github.drumber.kitsune.domain_old.model.infrastructure.algolia.search.AlgoliaDimensions
import io.github.drumber.kitsune.domain_old.model.infrastructure.algolia.search.AlgoliaImage
import io.github.drumber.kitsune.domain_old.model.infrastructure.algolia.search.AlgoliaImageMeta
import io.github.drumber.kitsune.domain_old.model.infrastructure.image.Dimension
import io.github.drumber.kitsune.domain_old.model.infrastructure.image.Dimensions
import io.github.drumber.kitsune.domain_old.model.infrastructure.image.Image
import io.github.drumber.kitsune.domain_old.model.infrastructure.image.ImageMeta
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ImageMapperTest {

    private val faker = Faker()

    @Test
    fun shouldMapImageToDBImage() {
        // given
        val image = Image(
            tiny = faker.internet().image(),
            small = faker.internet().image(),
            medium = faker.internet().image(),
            large = faker.internet().image(),
            original = faker.internet().image(),
            meta = ImageMeta(
                Dimensions(
                    tiny = Dimension(faker.number().positive(), faker.number().positive()),
                    small = Dimension(faker.number().positive(), faker.number().positive()),
                    medium = Dimension(faker.number().positive(), faker.number().positive()),
                    large = Dimension(faker.number().positive(), faker.number().positive())
                )
            )
        )

        // when
        val dbImage = image.toDBImage()

        // then
        assertThat(dbImage).usingRecursiveComparison().isEqualTo(image)
    }

    @Test
    fun shouldMapDBImageToImage() {
        // given
        val dbImage = DBImage(
            tiny = faker.internet().image(),
            small = faker.internet().image(),
            medium = faker.internet().image(),
            large = faker.internet().image(),
            original = faker.internet().image(),
            meta = DBImageMeta(
                DBDimensions(
                    tiny = DBDimension(faker.number().positive(), faker.number().positive()),
                    small = DBDimension(faker.number().positive(), faker.number().positive()),
                    medium = DBDimension(faker.number().positive(), faker.number().positive()),
                    large = DBDimension(faker.number().positive(), faker.number().positive())
                )
            )
        )

        // when
        val image = dbImage.toImage()

        // then
        assertThat(image).usingRecursiveComparison().isEqualTo(dbImage)
    }

    @Test
    fun shouldMapAlgoliaImageToImage() {
        // given
        val algoliaImage = AlgoliaImage(
            tiny = faker.internet().image(),
            small = faker.internet().image(),
            medium = faker.internet().image(),
            large = faker.internet().image(),
            original = faker.internet().image(),
            meta = AlgoliaImageMeta(
                AlgoliaDimensions(
                    tiny = AlgoliaDimension(faker.number().positive(), faker.number().positive()),
                    small = AlgoliaDimension(faker.number().positive(), faker.number().positive()),
                    medium = AlgoliaDimension(faker.number().positive(), faker.number().positive()),
                    large = AlgoliaDimension(faker.number().positive(), faker.number().positive())
                )
            )
        )

        // when
        val image = algoliaImage.toImage()

        // then
        assertThat(image).usingRecursiveComparison().isEqualTo(algoliaImage)
    }

}