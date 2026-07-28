package com.eyevoicecoach.data.preferences

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import androidx.compose.animation.core.animateFloat
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column

private const val KEYSTORE_NAME = "AndroidKeyStore"
private const val KEY_ALIAS = "eye_body_voice_settings_key"
private const val TRANSFORMATION = "AES/GCM/NoPadding"
private const val IV_BYTES = 12
private const val TAG_BITS = 128
private val Context.securePreferencesDataStore by preferencesDataStore(name = "encrypted_settings")

/**
 * Encrypts DataStore preference values with an AES-GCM key stored solely in Android Keystore.
 * The DataStore file contains ciphertext and never readable preference values.
 */
class EncryptedPreferenceDataStore(private val context: Context) {
    private val crypto = KeystoreCipher()

    /** Streams a decrypted setting, returning [default] when unavailable or corrupt. */
    fun stringFlow(name: String, default: String): Flow<String> =
        context.securePreferencesDataStore.data
            .map { preferences -> preferences[stringPreferencesKey(name)]?.let(crypto::decrypt) ?: default }
            .catch { emit(default) }

    /** Removes every encrypted setting from the private DataStore. */
    suspend fun clear() {
        context.securePreferencesDataStore.edit { preferences -> preferences.clear() }
    }

    /** Encrypts and persists one setting in the private DataStore. */
    suspend fun putString(name: String, value: String) {
        context.securePreferencesDataStore.edit { preferences ->
            preferences[stringPreferencesKey(name)] = crypto.encrypt(value)
        }
    }
}

/** AES-GCM cipher backed by a non-exportable Android Keystore key. */
private class KeystoreCipher {
    private fun key(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_NAME).apply { load(null) }
        val existing = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existing != null) return existing
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_NAME).apply {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build(),
            )
        }.generateKey()
    }

    fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, key()) }
        val encoded = cipher.iv + cipher.doFinal(value.toByteArray(StandardCharsets.UTF_8))
        return Base64.encodeToString(encoded, Base64.NO_WRAP)
    }

    fun decrypt(encrypted: String): String {
        val bytes = Base64.decode(encrypted, Base64.NO_WRAP)
        require(bytes.size > IV_BYTES) { "قيمة إعدادات غير صالحة" }
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(TAG_BITS, bytes.copyOfRange(0, IV_BYTES)))
        }
        return String(cipher.doFinal(bytes.copyOfRange(IV_BYTES, bytes.size)), StandardCharsets.UTF_8)
    }
}
