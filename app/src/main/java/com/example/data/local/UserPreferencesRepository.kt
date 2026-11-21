package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.CaptionLanguage
import com.example.data.model.ExportPlatform
import com.example.data.model.SubscriptionPlan
import com.example.data.model.UserProfileSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPrefsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "viral_clip_user_prefs"
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val HANDLE = stringPreferencesKey("handle")
        val PLAN = stringPreferencesKey("subscription_plan")
        val CLIPS_THIS_MONTH = intPreferencesKey("clips_this_month")
        val DEFAULT_LANGUAGE = stringPreferencesKey("default_language")
        val DEFAULT_PLATFORM = stringPreferencesKey("default_platform")
        val FULL_HD_EXPORT = booleanPreferencesKey("full_hd_export")
        val AUTO_REMOVE_SILENCE = booleanPreferencesKey("auto_remove_silence")
        val DEFAULT_EMOJI_CAPTIONS = booleanPreferencesKey("default_emoji_captions")
    }

    val userSettingsFlow: Flow<UserProfileSettings> = context.userPrefsDataStore.data.map { prefs ->
        UserProfileSettings(
            displayName = prefs[Keys.DISPLAY_NAME] ?: "Creator Studio",
            handle = prefs[Keys.HANDLE] ?: "@viralcreator",
            plan = SubscriptionPlan.entries.firstOrNull { it.name == prefs[Keys.PLAN] }
                ?: SubscriptionPlan.FREE,
            clipsGeneratedThisMonth = prefs[Keys.CLIPS_THIS_MONTH] ?: 0,
            defaultLanguage = CaptionLanguage.entries.firstOrNull { it.name == prefs[Keys.DEFAULT_LANGUAGE] }
                ?: CaptionLanguage.HINGLISH,
            defaultPlatform = ExportPlatform.entries.firstOrNull { it.name == prefs[Keys.DEFAULT_PLATFORM] }
                ?: ExportPlatform.INSTAGRAM_REELS,
            fullHdExport = prefs[Keys.FULL_HD_EXPORT] ?: true,
            autoRemoveSilence = prefs[Keys.AUTO_REMOVE_SILENCE] ?: true,
            defaultEmojiCaptions = prefs[Keys.DEFAULT_EMOJI_CAPTIONS] ?: true
        )
    }

    suspend fun updateProfile(displayName: String, handle: String) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[Keys.DISPLAY_NAME] = displayName.trim().ifBlank { "Creator Studio" }
            val cleanHandle = handle.trim().ifBlank { "@viralcreator" }
            prefs[Keys.HANDLE] = if (cleanHandle.startsWith("@")) cleanHandle else "@$cleanHandle"
        }
    }

    suspend fun updatePlan(plan: SubscriptionPlan) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[Keys.PLAN] = plan.name
        }
    }

    suspend fun incrementClipsGenerated(count: Int) {
        context.userPrefsDataStore.edit { prefs ->
            val current = prefs[Keys.CLIPS_THIS_MONTH] ?: 0
            prefs[Keys.CLIPS_THIS_MONTH] = current + count.coerceAtLeast(0)
        }
    }

    suspend fun resetMonthlyUsage() {
        context.userPrefsDataStore.edit { prefs ->
            prefs[Keys.CLIPS_THIS_MONTH] = 0
        }
    }

    suspend fun updateDefaultLanguage(language: CaptionLanguage) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[Keys.DEFAULT_LANGUAGE] = language.name
        }
    }

    suspend fun updateDefaultPlatform(platform: ExportPlatform) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[Keys.DEFAULT_PLATFORM] = platform.name
        }
    }

    suspend fun updateFullHdExport(enabled: Boolean) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[Keys.FULL_HD_EXPORT] = enabled
        }
    }

    suspend fun updateAutoRemoveSilence(enabled: Boolean) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[Keys.AUTO_REMOVE_SILENCE] = enabled
        }
    }

    suspend fun updateDefaultEmojiCaptions(enabled: Boolean) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[Keys.DEFAULT_EMOJI_CAPTIONS] = enabled
        }
    }
}
