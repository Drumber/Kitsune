package io.github.drumber.kitsune.domain_old.mapper

import io.github.drumber.kitsune.data.source.network.algolia.model.search.AlgoliaDimension
import io.github.drumber.kitsune.data.source.network.algolia.model.search.AlgoliaDimensions
import io.github.drumber.kitsune.data.source.network.algolia.model.search.AlgoliaImage
import io.github.drumber.kitsune.data.source.network.algolia.model.search.AlgoliaImageMeta
import io.github.drumber.kitsune.domain_old.model.database.DBDimension
import io.github.drumber.kitsune.domain_old.model.database.DBDimensions
import io.github.drumber.kitsune.domain_old.model.database.DBImage
import io.github.drumber.kitsune.domain_old.model.database.DBImageMeta
import io.github.drumber.kitsune.domain_old.model.infrastructure.image.Dimension
import io.github.drumber.kitsune.domain_old.model.infrastructure.image.Dimensions
import io.github.drumber.kitsune.domain_old.model.infrastructure.image.Image
import io.github.drumber.kitsune.domain_old.model.infrastructure.image.ImageMeta

/*======================
 * Image to DBImage
 *======================*/
fun Image.toDBImage() = DBImage(
    tiny, small, medium, large, original, meta?.toDBImageMeta()
)

fun ImageMeta.toDBImageMeta() = DBImageMeta(dimensions?.toDBDimensions())
fun Dimensions.toDBDimensions() = DBDimensions(
    tiny?.toDBDimension(), small?.toDBDimension(), medium?.toDBDimension(), large?.toDBDimension()
)

fun Dimension.toDBDimension() = DBDimension(width, height)


/*======================
 * DBImage to Image
 *======================*/
fun DBImage.toImage() = Image(
    tiny, small, medium, large, original, meta?.toImageMeta()
)

fun DBImageMeta.toImageMeta() = ImageMeta(dimensions?.toDimensions())
fun DBDimensions.toDimensions() = Dimensions(
    tiny?.toDimension(), small?.toDimension(), medium?.toDimension(), large?.toDimension()
)

fun DBDimension.toDimension() = Dimension(width, height)


/*======================
 * AlgoliaImage to Image
 *======================*/
fun AlgoliaImage.toImage() = Image(
    tiny, small, medium, large, original, meta?.toImageMeta()
)

fun AlgoliaImageMeta.toImageMeta() = ImageMeta(dimensions?.toDimensions())
fun AlgoliaDimensions.toDimensions() = Dimensions(
    tiny?.toDimension(), small?.toDimension(), medium?.toDimension(), large?.toDimension()
)

fun AlgoliaDimension.toDimension() = Dimension(width, height)
