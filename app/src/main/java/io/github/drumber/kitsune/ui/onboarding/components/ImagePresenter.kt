package io.github.drumber.kitsune.ui.onboarding.components

interface ImagePresenter {
    fun hasNextImage(): Boolean
    fun getNextImage(): String?
}

object EmptyImagePresenter : ImagePresenter {
    override fun hasNextImage(): Boolean {
        return false
    }

    override fun getNextImage(): String? {
        return null
    }
}

class RandomImagePresenter(private val imageUrls: List<String>) : ImagePresenter {
    private val lastShownImages = LinkedHashSet<String>()

    override fun hasNextImage(): Boolean {
        return imageUrls.isNotEmpty()
    }

    override fun getNextImage(): String? {
        if (imageUrls.isEmpty()) {
            return null
        }
        if (imageUrls.size == 1) {
            return imageUrls.first()
        }

        val imagePool = imageUrls - lastShownImages
        if (imagePool.isEmpty()) {
            val lastImageUrl = lastShownImages.last()
            lastShownImages.clear()
            lastShownImages.add(lastImageUrl)
            return getNextImage()
        }

        val image = imagePool.random()
        lastShownImages.add(image)
        return image
    }
}