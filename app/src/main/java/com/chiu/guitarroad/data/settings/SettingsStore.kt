package com.chiu.guitarroad.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "guitar_road_settings",
)

/**
 * DataStore Preferences（文件 "guitar_road_settings"）。
 * quiz_bonus_days：StringSet，元素格式为 "cardId:yyyy-MM-dd"，用于
 * "同一张乐理卡每天只加一次乐理分"的判定。
 */
class SettingsStore(private val context: Context) {

    private object Keys {
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val USER_ID = stringPreferencesKey("user_id")
        val SEEDED_VERSION = intPreferencesKey("seeded_version")
        val QUIZ_BONUS_DAYS = stringSetPreferencesKey("quiz_bonus_days")
    }

    val onboardingDone: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.ONBOARDING_DONE] ?: false }

    val userId: Flow<String?> =
        context.dataStore.data.map { it[Keys.USER_ID] }

    val seededVersion: Flow<Int> =
        context.dataStore.data.map { it[Keys.SEEDED_VERSION] ?: 0 }

    val quizBonusDays: Flow<Set<String>> =
        context.dataStore.data.map { it[Keys.QUIZ_BONUS_DAYS] ?: emptySet() }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_DONE] = done }
    }

    suspend fun setUserId(id: String) {
        context.dataStore.edit { it[Keys.USER_ID] = id }
    }

    suspend fun setSeededVersion(version: Int) {
        context.dataStore.edit { it[Keys.SEEDED_VERSION] = version }
    }

    suspend fun addQuizBonusDay(entry: String) {
        context.dataStore.edit {
            val current = it[Keys.QUIZ_BONUS_DAYS] ?: emptySet()
            it[Keys.QUIZ_BONUS_DAYS] = current + entry
        }
    }

    /** 今天是否已因该卡获得过乐理加分。 */
    suspend fun hasQuizBonusToday(cardId: String): Boolean {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        return quizBonusDays.first().contains("$cardId:$today")
    }

    /** 记录今天因该卡获得乐理加分。 */
    suspend fun markQuizBonusToday(cardId: String) {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        addQuizBonusDay("$cardId:$today")
    }
}
