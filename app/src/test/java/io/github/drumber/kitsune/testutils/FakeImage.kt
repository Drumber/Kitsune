package io.github.drumber.kitsune.testutils

import io.github.drumber.kitsune.data.common.Image
import io.github.drumber.kitsune.data.common.ImageDimension
import io.github.drumber.kitsune.data.common.ImageDimensions
import io.github.drumber.kitsune.data.source.network.NetworkImage
import io.github.drumber.kitsune.data.source.network.NetworkImageDimension
import io.github.drumber.kitsune.data.source.network.NetworkImageDimensions
import io.github.drumber.kitsune.data.source.network.NetworkImageMeta
import net.datafaker.Faker

fun image(faker: Faker) = Image(
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

fun networkImage(faker: Faker) = NetworkImage(
    tiny = faker.internet().image(),
    small = faker.internet().image(),
    medium = faker.internet().image(),
    large = faker.internet().image(),
    original = faker.internet().image(),
    meta = NetworkImageMeta(
        NetworkImageDimensions(
            tiny = NetworkImageDimension(faker.number().positive(), faker.number().positive()),
            small = NetworkImageDimension(faker.number().positive(), faker.number().positive()),
            medium = NetworkImageDimension(faker.number().positive(), faker.number().positive()),
            large = NetworkImageDimension(faker.number().positive(), faker.number().positive())
        )
    )
)
