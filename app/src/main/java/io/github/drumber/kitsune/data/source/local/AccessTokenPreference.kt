package io.github.drumber.kitsune.data.source.local

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.source.local.auth.AccessTokenLocalDataSource
import io.github.drumber.kitsune.data.source.local.auth.model.LocalAccessToken
import io.github.drumber.kitsune.util.logI

class AccessTokenPreference(
    context: Context,
    private val objectMapper: ObjectMapper
): AccessTokenLocalDataSource {

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

    override fun storeAccessToken(accessToken: LocalAccessToken) {
        logI("Converting access token to json and storing it in encrypted shared preferences.")
        val jsonString = objectMapper.writeValueAsString(accessToken)
        sharedPreferences.edit(commit = true) {
            putString(KEY_ACCESS_TOKEN, jsonString)
        }
    }

    override fun clearAccessToken() {
        logI("Deleting access token from encrypted shared preferences.")
        sharedPreferences.edit(commit = true) {
            remove(KEY_ACCESS_TOKEN)
        }
    }

    override fun loadAccessToken(): LocalAccessToken? {
        val jsonString = sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
        return if (!jsonString.isNullOrBlank()) {
            logI("Parse and return access token stored as json.")
            objectMapper.readValue(jsonString)
        } else {
            logI("No access token stored.")
            null
        }
    }

    companion object {
        const val KEY_ACCESS_TOKEN = "access_token"
    }
}