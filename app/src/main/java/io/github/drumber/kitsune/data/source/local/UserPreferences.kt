package io.github.drumber.kitsune.data.source.local

import android.content.Context
import androidx.core.content.edit
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.source.local.user.UserLocalDataSource
import io.github.drumber.kitsune.data.source.local.user.model.LocalUser
import io.github.drumber.kitsune.shared.logD

class UserPreferences(context: Context, private val objectMapper: ObjectMapper) : UserLocalDataSource {

    private val sharedPreferences = context.getSharedPreferences(
        context.getString(R.string.user_preference_file_key),
        Context.MODE_PRIVATE
    )

    override fun loadUser(): LocalUser? {
        val jsonString = sharedPreferences.getString(KEY_USER_MODEL, null)
        return if (!jsonString.isNullOrBlank()) {
            logD("Parse and return user model stored as json string.")
            return objectMapper.readValue(jsonString)
        } else {
            logD("No user model stored.")
            null
        }
    }

    override fun storeUser(user: LocalUser) {
        logD("Storing user model in shared preferences.")
        val jsonString = objectMapper.writeValueAsString(user)
        sharedPreferences.edit {
            putString(KEY_USER_MODEL, jsonString)
        }
    }

    override fun clearUser() {
        logD("Deleting user model from shared preferences.")
        sharedPreferences.edit {
            remove(KEY_USER_MODEL)
        }
    }

    companion object {
        const val KEY_USER_MODEL = "user_model"
    }

}