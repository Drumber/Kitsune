package io.github.drumber.kitsune.testutils

import io.github.drumber.kitsune.data.common.ImageDimension
import io.github.drumber.kitsune.data.common.ImageDimensions
import net.datafaker.Faker

fun image(faker: Faker) = io.github.drumber.kitsune.data.common.Image(
    tiny = faker.internet().image(),
    small = faker.internet().image(),
    medium = faker.internet().image(),
    large = faker.internet().image(),
    original = faker.internet().image(),
    meta = io.github.drumber.kitsune.data.common.ImageMeta(
        ImageDimensions(
            tiny = ImageDimension(faker.number().positive(), faker.number().positive()),
            small = ImageDimension(faker.number().positive(), faker.number().positive()),
            medium = ImageDimension(faker.number().positive(), faker.number().positive()),
            large = ImageDimension(faker.number().positive(), faker.number().positive())
        )
    )
)
