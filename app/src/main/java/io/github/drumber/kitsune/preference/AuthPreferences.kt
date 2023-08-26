package io.github.drumber.kitsune.preference

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.domain.model.infrastructure.auth.AccessToken
import io.github.drumber.kitsune.util.logD

class AuthPreferences(context: Context, private val objectMapper: ObjectMapper) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPrefsFile = context.getString(R.string.auth_preference_file_key)
    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        sharedPrefsFile,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun storeAccessToken(accessToken: AccessToken) {
        logD("Converting access token to json and storing it in encrypted shared preferences.")
        val jsonString = objectMapper.writeValueAsString(accessToken)
        sharedPreferences.edit {
            putString(KEY_ACCESS_TOKEN, jsonString)
        }
    }

    fun clearAccessToken() {
        logD("Deleting access token from encrypted shared preferences.")
        sharedPreferences.edit {
            remove(KEY_ACCESS_TOKEN)
        }
    }

    fun getStoredAccessToken(): AccessToken? {
        val jsonString = sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
        return if (!jsonString.isNullOrBlank()) {
            logD("Parse and return access token stored as json.")
            objectMapper.readValue(jsonString)
        } else {
            logD("No access token stored.")
            null
        }
    }

    companion object {
        const val KEY_ACCESS_TOKEN = "access_token"
    }

}