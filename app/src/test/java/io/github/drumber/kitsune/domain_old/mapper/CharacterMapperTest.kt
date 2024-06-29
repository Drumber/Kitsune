package io.github.drumber.kitsune.domain_old.mapper

import io.github.drumber.kitsune.domain_old.model.infrastructure.algolia.search.AlgoliaDimension
import io.github.drumber.kitsune.domain_old.model.infrastructure.algolia.search.AlgoliaDimensions
import io.github.drumber.kitsune.domain_old.model.infrastructure.algolia.search.AlgoliaImage
import io.github.drumber.kitsune.domain_old.model.infrastructure.algolia.search.AlgoliaImageMeta
import io.github.drumber.kitsune.domain_old.model.infrastructure.algolia.search.CharacterSearchResult
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CharacterMapperTest {

    private val faker = Faker()

    @Test
    fun shouldMapCharacterSearchResultToCharacter() {
        // given
        val characterSearchResult = CharacterSearchResult(
            id = faker.number().randomNumber(),
            slug = faker.lorem().word(),
            canonicalName = faker.lorem().word(),
            image = AlgoliaImage(
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
            ),
            primaryMedia = faker.lorem().word()
        )

        // when
        val character = characterSearchResult.toCharacter()

        // then
        assertThat(character).usingRecursiveComparison()
            .ignoringFields("name", "description", "malId", "names", "otherNames", "mediaCharacters")
            .withEqualsForFields({ a, b -> a.toString() == b.toString() }, "id")
            .isEqualTo(characterSearchResult)
    }

}