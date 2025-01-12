package io.github.drumber.kitsune.data.source.jsonapi.testutils

import io.github.drumber.kitsune.data.model.Image
import io.github.drumber.kitsune.data.model.ImageDimension
import io.github.drumber.kitsune.data.model.ImageDimensions
import io.github.drumber.kitsune.data.model.ImageMeta
import net.datafaker.Faker

fun image(faker: Faker) = Image(
    tiny = faker.internet().image(),
    small = faker.internet().image(),
    medium = faker.internet().image(),
    large = faker.internet().image(),
    original = faker.internet().image(),
    meta = ImageMeta(
        ImageDimensions(
            tiny = ImageDimension(
                faker.number().positive(),
                faker.number().positive()
            ),
            small = ImageDimension(
                faker.number().positive(),
                faker.number().positive()
            ),
            medium = ImageDimension(
                faker.number().positive(),
                faker.number().positive()
            ),
            large = ImageDimension(
                faker.number().positive(),
                faker.number().positive()
            )
        )
    )
)
