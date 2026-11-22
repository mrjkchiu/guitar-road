package com.chiu.guitarroad.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chiu.guitarroad.data.model.NodeProgress
import com.chiu.guitarroad.data.model.NodeStatus
import com.chiu.guitarroad.data.model.PathNode
import com.chiu.guitarroad.data.model.StageInfo
import com.chiu.guitarroad.data.repository.GuitarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar

data class HomeUiState(
    val currentNode: PathNode? = null,
    val currentNodeProgress: NodeProgress? = null,
    val totalDone: Int = 0,
    val totalNodes: Int = 30,
    val allDone: Boolean = false,
    val hasStarted: Boolean = false,
    val weekDoneCount: Int = 0,
    val weekMinutes: Int = 0,
    val streakDays: Int = 0,
    val currentStage: StageInfo? = null,
    val milestoneRemaining: Int = 0,
    val isLoading: Boolean = true
)

val HOME_STAGES = listOf(
    StageInfo(1, "让手和琴成为朋友", "第 1–2 周", "Em / Am / G 切换 1 分钟 30 次"),
    StageInfo(2, "让节奏活起来", "第 3–4 周", "60 BPM 下连续扫 8 小节不抢拍"),
    StageInfo(3, "弹出第一首歌", "第 5–7 周", "完整录一遍主歌"),
    StageInfo(4, "从“能弹”到“能弹唱”", "第 8–10 周", "完整弹唱一首歌，存入作品档案")
)

private const val HOME_PATH_ID = "path_sing_zero_3h"

class HomeViewModel(private val repo: GuitarRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(repo.observeNodes(HOME_PATH_ID), repo.observeProgress()) { nodes, progress ->
                nodes to progress
            }.collect { (nodes, progress) ->
                val sorted = nodes.sortedBy { it.orderIndex }
                val doneCount = sorted.count { progress[it.id]?.status == NodeStatus.DONE }
                val hasStarted = sorted.any {
                    val status = progress[it.id]?.status
                    status == NodeStatus.IN_PROGRESS || status == NodeStatus.DONE
                }
                val currentNode = sorted.firstOrNull {
                    val status = progress[it.id]?.status
                    status == NodeStatus.AVAILABLE || status == NodeStatus.IN_PROGRESS
                }
                val weekStart = weekStartMillis()
                val weekDone = sorted.count {
                    val completedAt = progress[it.id]?.completedAt
                    completedAt != null && completedAt >= weekStart
                }
                val currentStageNumber = currentNode?.stage
                    ?: sorted.firstOrNull { progress[it.id]?.status != NodeStatus.DONE }?.stage
                    ?: 4
                val stageInfo = HOME_STAGES.firstOrNull { it.number == currentStageNumber }
                val milestoneRemaining = sorted.count {
                    it.stage == currentStageNumber && progress[it.id]?.status != NodeStatus.DONE
                }
                val weekMinutes = runCatching { repo.getWeekMinutes() }.getOrDefault(0)
                val streak = runCatching { repo.getStreakDays() }.getOrDefault(0)
                _uiState.value = HomeUiState(
                    currentNode = currentNode,
                    currentNodeProgress = currentNode?.let { progress[it.id] },
                    totalDone = doneCount,
                    totalNodes = sorted.size.coerceAtLeast(30),
                    allDone = sorted.isNotEmpty() && doneCount == sorted.size,
                    hasStarted = hasStarted,
                    weekDoneCount = weekDone,
                    weekMinutes = weekMinutes,
                    streakDays = streak,
                    currentStage = stageInfo,
                    milestoneRemaining = milestoneRemaining,
                    isLoading = false
                )
            }
        }
    }

    private fun weekStartMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val offset = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
        calendar.add(Calendar.DAY_OF_YEAR, -offset)
        return calendar.timeInMillis
    }
}
