package io.github.drumber.kitsune.testutils

import io.github.drumber.kitsune.domain.model.database.DBDimension
import io.github.drumber.kitsune.domain.model.database.DBDimensions
import io.github.drumber.kitsune.domain.model.database.DBImage
import io.github.drumber.kitsune.domain.model.database.DBImageMeta
import io.github.drumber.kitsune.domain.model.infrastructure.image.Dimension
import io.github.drumber.kitsune.domain.model.infrastructure.image.Dimensions
import io.github.drumber.kitsune.domain.model.infrastructure.image.Image
import io.github.drumber.kitsune.domain.model.infrastructure.image.ImageMeta
import net.datafaker.Faker

fun image(faker: Faker) = Image(
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
fun dbImage(faker: Faker) = DBImage(
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

