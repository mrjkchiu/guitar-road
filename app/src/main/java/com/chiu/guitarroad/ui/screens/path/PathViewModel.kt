package com.chiu.guitarroad.ui.screens.path

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chiu.guitarroad.data.model.NodeProgress
import com.chiu.guitarroad.data.model.NodeStatus
import com.chiu.guitarroad.data.model.PathNode
import com.chiu.guitarroad.data.model.StageInfo
import com.chiu.guitarroad.data.repository.GuitarRepository
import com.chiu.guitarroad.ui.screens.home.HOME_STAGES
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

enum class StageStatus { DONE, IN_PROGRESS, LOCKED }

data class StageUiState(
    val info: StageInfo,
    val status: StageStatus,
    val doneCount: Int,
    val totalCount: Int,
    val nodes: List<PathNode>
)

data class PathUiState(
    val pathName: String = "零基础弹唱之路",
    val totalDone: Int = 0,
    val totalNodes: Int = 30,
    val stages: List<StageUiState> = emptyList(),
    val progress: Map<String, NodeProgress> = emptyMap(),
    val isLoading: Boolean = true
)

private const val PATH_PATH_ID = "path_sing_zero_3h"

class PathViewModel(private val repo: GuitarRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PathUiState())
    val uiState: StateFlow<PathUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(repo.observeNodes(PATH_PATH_ID), repo.observeProgress()) { nodes, progress ->
                nodes to progress
            }.collect { (nodes, progress) ->
                val sorted = nodes.sortedBy { it.orderIndex }
                val doneCount = sorted.count { progress[it.id]?.status == NodeStatus.DONE }
                val currentStageNumber = sorted.firstOrNull {
                    progress[it.id]?.status != NodeStatus.DONE
                }?.stage ?: 4
                val stages = HOME_STAGES.map { stage ->
                    val stageNodes = sorted.filter { it.stage == stage.number }
                    val stageDone = stageNodes.count { progress[it.id]?.status == NodeStatus.DONE }
                    val status = when {
                        stageNodes.isNotEmpty() && stageDone == stageNodes.size -> StageStatus.DONE
                        stage.number == currentStageNumber -> StageStatus.IN_PROGRESS
                        stage.number < currentStageNumber -> StageStatus.DONE
                        else -> StageStatus.LOCKED
                    }
                    StageUiState(
                        info = stage,
                        status = status,
                        doneCount = stageDone,
                        totalCount = stageNodes.size,
                        nodes = stageNodes
                    )
                }
                _uiState.value = PathUiState(
                    pathName = "零基础弹唱之路",
                    totalDone = doneCount,
                    totalNodes = sorted.size.coerceAtLeast(30),
                    stages = stages,
                    progress = progress,
                    isLoading = false
                )
            }
        }
    }
}
