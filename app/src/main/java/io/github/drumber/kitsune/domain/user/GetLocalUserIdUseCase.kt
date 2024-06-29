package io.github.drumber.kitsune.domain.user

import io.github.drumber.kitsune.data.repository.UserRepository

class GetLocalUserIdUseCase(
    private val userRepository: UserRepository
) {

    operator fun invoke(): String? {
        return userRepository.localUser.value?.id
    }

}