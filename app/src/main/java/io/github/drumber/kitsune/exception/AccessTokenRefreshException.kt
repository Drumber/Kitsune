package io.github.drumber.kitsune.exception

class AccessTokenRefreshException(message: String, cause: Throwable?) : Exception(message, cause)

/** Access token was not refreshed, because it is not expired. */
class AccessTokenNotRefreshedException(message: String) : Exception(message)
