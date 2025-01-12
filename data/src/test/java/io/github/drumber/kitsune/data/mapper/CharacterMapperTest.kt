package io.github.drumber.kitsune.data.mapper

import io.github.drumber.kitsune.data.mapper.CharacterMapper.toCharacter
import io.github.drumber.kitsune.data.mapper.CharacterMapper.toLocalCharacter
import io.github.drumber.kitsune.data.testutils.localCharacter
import io.github.drumber.kitsune.data.testutils.networkCharacter
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CharacterMapperTest {

    private val faker = Faker()

    @Test
    fun shouldMap_NetworkCharacter_to_LocalCharacter() {
        // given
        val networkCharacter = networkCharacter(faker)

        // when
        val localCharacter = networkCharacter.toLocalCharacter()

        // then
        assertThat(localCharacter)
            .usingRecursiveComparison()
            .isEqualTo(networkCharacter)
    }

    @Test
    fun shouldMap_NetworkCharacter_to_Character() {
        // given
        val networkCharacter = networkCharacter(faker)

        // when
        val character = networkCharacter.toCharacter()

        // then
        assertThat(character)
            .usingRecursiveComparison()
            .isEqualTo(networkCharacter)
    }

    @Test
    fun shouldMap_LocalCharacter_to_Character() {
        // given
        val localCharacter = localCharacter(faker)

        // when
        val character = localCharacter.toCharacter()

        // then
        assertThat(character)
            .usingRecursiveComparison()
            .ignoringFields("mediaCharacters")
            .isEqualTo(localCharacter)
    }
}