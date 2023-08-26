package io.github.drumber.kitsune.preference

import android.content.Context
import androidx.core.content.edit
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.domain.model.infrastructure.user.User
import io.github.drumber.kitsune.util.logD

class UserPreferences(context: Context, private val objectMapper: ObjectMapper) {

    private val sharedPreferences = context.getSharedPreferences(
        context.getString(R.string.user_preference_file_key),
        Context.MODE_PRIVATE
    )

    fun storeUserModel(user: User) {
        logD("Storing user model in shared preferences.")
        val jsonString = objectMapper.writeValueAsString(user)
        sharedPreferences.edit {
            putString(KEY_USER_MODEL, jsonString)
        }
    }

    fun clearUserModel() {
        logD("Deleting user model from shared preferences.")
        sharedPreferences.edit {
            remove(KEY_USER_MODEL)
        }
    }

    fun getStoredUserModel(): User? {
        val jsonString = sharedPreferences.getString(KEY_USER_MODEL, null)
        return if (!jsonString.isNullOrBlank()) {
            logD("Parse and return user model stored as json string.")
            return objectMapper.readValue(jsonString)
        } else {
            logD("No user model stored.")
            null
        }
    }

    companion object {
        const val KEY_USER_MODEL = "user_model"
    }

}