package com.chiu.guitarroad.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chiu.guitarroad.data.repository.GuitarRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnboardingOption(val value: String, val label: String)

class OnboardingViewModel(private val repo: GuitarRepository) : ViewModel() {

    val goalOptions = listOf(
        OnboardingOption("sing", "弹唱流行歌"),
        OnboardingOption("fingerstyle", "指弹独奏"),
        OnboardingOption("classical", "古典入门"),
        OnboardingOption("accompany", "给唱歌伴奏"),
        OnboardingOption("casual", "纯粹兴趣，随便玩玩")
    )

    val levelOptions = listOf(
        OnboardingOption("zero", "零基础"),
        OnboardingOption("basic", "会 2–3 个和弦"),
        OnboardingOption("some", "能弹简单歌"),
        OnboardingOption("stuck", "弹了一阵，卡住了")
    )

    val hoursOptions = listOf(
        OnboardingOption("2", "每周不到 2 小时"),
        OnboardingOption("5", "每周 2–5 小时"),
        OnboardingOption("10", "每周 5 小时以上")
    )

    private val _step = MutableStateFlow(1)
    val step: StateFlow<Int> = _step.asStateFlow()

    private val _selectedGoal = MutableStateFlow<String?>(null)
    val selectedGoal: StateFlow<String?> = _selectedGoal.asStateFlow()

    private val _selectedLevel = MutableStateFlow<String?>(null)
    val selectedLevel: StateFlow<String?> = _selectedLevel.asStateFlow()

    private val _selectedHours = MutableStateFlow<Int?>(null)
    val selectedHours: StateFlow<Int?> = _selectedHours.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _navigateToMain = MutableStateFlow(false)
    val navigateToMain: StateFlow<Boolean> = _navigateToMain.asStateFlow()

    fun selectGoal(value: String) {
        _selectedGoal.value = value
    }

    fun selectLevel(value: String) {
        _selectedLevel.value = value
    }

    fun selectHours(hours: Int) {
        _selectedHours.value = hours
    }

    fun goBack() {
        if (_step.value > 1) {
            _step.update { it - 1 }
        }
    }

    fun next() {
        val current = _step.value
        if (current < 3) {
            _step.update { it + 1 }
            return
        }
        submit()
    }

    fun canGoNext(): Boolean {
        return when (_step.value) {
            1 -> _selectedGoal.value != null
            2 -> _selectedLevel.value != null
            3 -> _selectedHours.value != null
            else -> false
        }
    }

    private fun submit() {
        val goal = _selectedGoal.value ?: return
        val level = _selectedLevel.value ?: return
        val hours = _selectedHours.value ?: return
        if (_isGenerating.value) return
        _isGenerating.value = true
        viewModelScope.launch {
            delay(1500)
            repo.saveOnboarding(goal, level, hours)
            _navigateToMain.value = true
        }
    }
}
