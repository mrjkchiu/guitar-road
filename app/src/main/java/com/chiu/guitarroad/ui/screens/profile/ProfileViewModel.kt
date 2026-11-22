package com.chiu.guitarroad.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chiu.guitarroad.data.model.WorkItem
import com.chiu.guitarroad.data.repository.GuitarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val nickname: String = "吉他手",
    val works: List<WorkItem> = emptyList(),
    val showAddWorkDialog: Boolean = false,
    val showClearDialog: Boolean = false,
    val cleared: Boolean = false
)

class ProfileViewModel(private val repo: GuitarRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.observeWorks().collect { works ->
                _uiState.value = _uiState.value.copy(works = works)
            }
        }
    }

    fun showAddWorkDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showAddWorkDialog = show)
    }

    fun showClearDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showClearDialog = show)
    }

    fun addWork(title: String, durationMin: Int, note: String?) {
        viewModelScope.launch {
            repo.addWork(
                title = title,
                mediaPath = "",
                durationSec = durationMin * 60,
                linkedNodeId = null,
                note = note?.takeIf { it.isNotBlank() }
            )
            _uiState.value = _uiState.value.copy(showAddWorkDialog = false)
        }
    }

    suspend fun exportAllData(): String {
        return repo.exportAllData()
    }

    fun clearAllLearningData() {
        viewModelScope.launch {
            repo.clearAllLearningData()
            _uiState.value = _uiState.value.copy(showClearDialog = false, cleared = true)
        }
    }
}
