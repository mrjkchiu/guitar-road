package com.chiu.guitarroad.ui.screens.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chiu.guitarroad.data.repository.GuitarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** 工具 Tab 轻量 ViewModel：持 repo，暴露和弦库与乐理卡数量。 */
class ToolsViewModel(private val repo: GuitarRepository) : ViewModel() {

    private val _chordCount = MutableStateFlow(0)
    val chordCount: StateFlow<Int> = _chordCount.asStateFlow()

    private val _theoryCardCount = MutableStateFlow(0)
    val theoryCardCount: StateFlow<Int> = _theoryCardCount.asStateFlow()

    init {
        viewModelScope.launch {
            repo.observeChords().collect { chords ->
                _chordCount.value = chords.size
            }
        }
        viewModelScope.launch {
            repo.observeTheoryCards().collect { cards ->
                _theoryCardCount.value = cards.size
            }
        }
    }
}
