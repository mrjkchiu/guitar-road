package com.chiu.guitarroad.ui.screens.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chiu.guitarroad.data.model.PathNode
import com.chiu.guitarroad.data.model.PracticeRecord
import com.chiu.guitarroad.data.model.RadarScore
import com.chiu.guitarroad.data.model.SelfRating
import com.chiu.guitarroad.data.repository.GuitarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class PracticeUiState(
    val radarScore: RadarScore = RadarScore(0, 0, 0, 0, 0, 0),
    val weekMinutes: Int = 0,
    val streakDays: Int = 0,
    val last7Days: List<LocalDate> = emptyList(),
    val practicedDates: Set<LocalDate> = emptySet(),
    val recentRecords: List<PracticeRecord> = emptyList(),
    val nodeTitles: Map<String, String> = emptyMap(),
    val allNodes: List<PathNode> = emptyList(),
    val showRecordSheet: Boolean = false
)

private const val PRACTICE_PATH_ID = "path_sing_zero_3h"

class PracticeViewModel(private val repo: GuitarRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PracticeUiState())
    val uiState: StateFlow<PracticeUiState> = _uiState.asStateFlow()

    init {
        val today = LocalDate.now()
        val last7 = (6 downTo 0).map { today.minusDays(it.toLong()) }
        viewModelScope.launch {
            combine(
                repo.observeRadarScore(),
                repo.observePracticeRecords(),
                repo.observeNodes(PRACTICE_PATH_ID)
            ) { radar, records, nodes ->
                Triple(radar, records, nodes)
            }.collect { (radar, records, nodes) ->
                val zone = ZoneId.systemDefault()
                val practiced = records.map {
                    Instant.ofEpochMilli(it.recordedAt).atZone(zone).toLocalDate()
                }.toSet()
                val weekMinutes = runCatching { repo.getWeekMinutes() }.getOrDefault(0)
                val streak = runCatching { repo.getStreakDays() }.getOrDefault(0)
                _uiState.value = _uiState.value.copy(
                    radarScore = radar,
                    weekMinutes = weekMinutes,
                    streakDays = streak,
                    last7Days = last7,
                    practicedDates = practiced,
                    recentRecords = records.take(10),
                    nodeTitles = nodes.associate { it.id to it.title },
                    allNodes = nodes.sortedBy { it.orderIndex }
                )
            }
        }
    }

    fun showRecordSheet(show: Boolean) {
        _uiState.value = _uiState.value.copy(showRecordSheet = show)
    }

    fun recordPractice(nodeId: String?, durationMin: Int, rating: SelfRating?) {
        viewModelScope.launch {
            repo.recordPractice(nodeId, durationMin, rating, note = null, source = "manual")
            _uiState.value = _uiState.value.copy(showRecordSheet = false)
        }
    }
}
