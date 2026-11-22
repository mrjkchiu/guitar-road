package com.chiu.guitarroad.ui.screens.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chiu.guitarroad.data.model.NodeProgress
import com.chiu.guitarroad.data.model.PathNode
import com.chiu.guitarroad.data.model.ResourceLink
import com.chiu.guitarroad.data.model.SelfRating
import com.chiu.guitarroad.data.model.TheoryCard
import com.chiu.guitarroad.data.model.UserResourceItem
import com.chiu.guitarroad.data.repository.GuitarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class TaskUiState(
    val node: PathNode? = null,
    val nodeNumber: Int = 0,
    val progress: NodeProgress? = null,
    val nextNodeId: String? = null,
    val resourceLinks: List<ResourceLink> = emptyList(),
    val userResources: List<UserResourceItem> = emptyList(),
    val theoryCard: TheoryCard? = null,
    val showTheoryDialog: Boolean = false,
    val theoryGatePassed: Boolean = false,
    val quizSelected: Int? = null,
    val quizAnswered: Boolean = false,
    val quizCorrect: Boolean = false,
    val rating: SelfRating = SelfRating.JUST_RIGHT,
    val showAddResourceDialog: Boolean = false,
    val navigateTo: String? = null,
    val isLoading: Boolean = true
)

private const val TASK_PATH_ID = "path_sing_zero_3h"

class TaskDetailViewModel(
    private val nodeId: String,
    private val repo: GuitarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    // startNode 只调用一次：首次进入时按进入前那一刻的 attempts 快照判断是否弹乐理卡，
    // 之后 progress 的每次变化都不再重复调用，避免 attempts 被循环累加。
    private var started = false
    private var theoryEvaluated = false

    init {
        viewModelScope.launch {
            combine(repo.observeNodes(TASK_PATH_ID), repo.observeProgress()) { nodes, progress ->
                nodes to progress
            }.collect { (nodes, progress) ->
                val sorted = nodes.sortedBy { it.orderIndex }
                val current = sorted.firstOrNull { it.id == nodeId }
                    ?: repo.getNode(nodeId)
                    ?: return@collect
                val nodeProgress = progress[nodeId]
                val nextNode = sorted.firstOrNull { it.orderIndex > current.orderIndex }

                if (!theoryEvaluated) {
                    theoryEvaluated = true
                    val needsTheory =
                        (nodeProgress?.attempts ?: 0) == 0 && current.theoryCardIds.isNotEmpty()
                    if (needsTheory) {
                        val card = repo.getTheoryCard(current.theoryCardIds.first())
                        _uiState.value = _uiState.value.copy(
                            theoryCard = card,
                            showTheoryDialog = card != null,
                            theoryGatePassed = card == null
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(theoryGatePassed = true)
                    }
                }

                _uiState.value = _uiState.value.copy(
                    node = current,
                    nodeNumber = current.orderIndex,
                    progress = nodeProgress,
                    nextNodeId = nextNode?.id,
                    isLoading = false
                )

                if (!started) {
                    started = true
                    repo.startNode(nodeId)
                }
            }
        }
        viewModelScope.launch {
            combine(
                repo.observeResourceLinks(nodeId),
                repo.observeUserResources(nodeId)
            ) { links, userResources ->
                links to userResources
            }.collect { (links, userResources) ->
                _uiState.value = _uiState.value.copy(
                    resourceLinks = links,
                    userResources = userResources
                )
            }
        }
    }

    fun selectQuizOption(index: Int) {
        _uiState.value = _uiState.value.copy(quizSelected = index)
    }

    fun submitQuizAnswer() {
        val state = _uiState.value
        val card = state.theoryCard ?: return
        val selected = state.quizSelected ?: return
        val correct = selected == card.quizAnswer
        _uiState.value = state.copy(quizAnswered = true, quizCorrect = correct)
        if (correct) {
            viewModelScope.launch { repo.addTheoryPoint() }
        }
    }

    fun dismissTheoryDialog() {
        _uiState.value = _uiState.value.copy(
            showTheoryDialog = false,
            theoryGatePassed = true
        )
    }

    fun selectRating(rating: SelfRating) {
        _uiState.value = _uiState.value.copy(rating = rating)
    }

    fun showAddResourceDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showAddResourceDialog = show)
    }

    fun addUserResource(title: String, url: String, platform: String) {
        viewModelScope.launch {
            repo.addUserResource(nodeId, title, url, platform)
            _uiState.value = _uiState.value.copy(showAddResourceDialog = false)
        }
    }

    fun markResourceUseful(id: Long) {
        viewModelScope.launch { repo.markResourceUseful(id) }
    }

    fun reportResourceBroken(id: Long) {
        viewModelScope.launch { repo.reportResourceBroken(id) }
    }

    fun completeNode() {
        val state = _uiState.value
        val node = state.node ?: return
        if (!state.theoryGatePassed) return
        viewModelScope.launch {
            repo.completeNode(nodeId, state.rating, node.minutes)
            _uiState.value = _uiState.value.copy(
                navigateTo = state.nextNodeId?.let { "task/$it" } ?: "main"
            )
        }
    }

    fun skipNode() {
        _uiState.value = _uiState.value.copy(navigateTo = "main")
    }
}
