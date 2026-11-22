package com.chiu.guitarroad.ui.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.chiu.guitarroad.data.model.TheoryCard
import com.chiu.guitarroad.data.repository.GuitarRepository
import com.chiu.guitarroad.data.settings.SettingsStore
import com.chiu.guitarroad.ui.components.EmptyState
import com.chiu.guitarroad.ui.components.PrimaryButton
import com.chiu.guitarroad.ui.components.SecondaryButton
import com.chiu.guitarroad.ui.components.SectionCard
import com.chiu.guitarroad.ui.components.SectionTitle
import com.chiu.guitarroad.ui.theme.CreamBg
import com.chiu.guitarroad.ui.theme.InkPrimary
import com.chiu.guitarroad.ui.theme.InkSecondary
import com.chiu.guitarroad.ui.theme.LeafGreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TheoryCardViewModel(
    private val repo: GuitarRepository,
    private val cardId: String,
    private val settings: SettingsStore,
) : ViewModel() {

    private val _card = MutableStateFlow<TheoryCard?>(null)
    val card: StateFlow<TheoryCard?> = _card.asStateFlow()

    private val _allCardIds = MutableStateFlow<List<String>>(emptyList())
    val allCardIds: StateFlow<List<String>> = _allCardIds.asStateFlow()

    init {
        viewModelScope.launch {
            _card.value = repo.getTheoryCard(cardId)
        }
        viewModelScope.launch {
            repo.observeTheoryCards().collect { cards ->
                _allCardIds.value = cards.map { it.id }.sorted()
            }
        }
    }

    /**
     * 答对小题：同一张卡每天只加一次乐理分。
     * 先查 SettingsStore 的 quiz_bonus_days（"cardId:yyyy-MM-dd" 的 StringSet），
     * 当天该卡已加过就不重复加分；未加过才调 repo.addTheoryPoint() 并 mark。
     * Repository 接口签名不改。
     */
    fun onAnsweredCorrect() {
        viewModelScope.launch {
            if (settings.hasQuizBonusToday(cardId)) return@launch
            repo.addTheoryPoint()
            settings.markQuizBonusToday(cardId)
        }
    }
}

@Composable
fun TheoryCardScreen(
    cardId: String,
    navController: NavController,
    repo: GuitarRepository,
    settings: SettingsStore,
) {
    val vm: TheoryCardViewModel = viewModel(
        factory = viewModelFactory { initializer { TheoryCardViewModel(repo, cardId, settings) } }
    )
    val card by vm.card.collectAsState()
    val allCardIds by vm.allCardIds.collectAsState()

    // 换卡时重置答题状态
    var selectedOption by remember(cardId) { mutableStateOf<Int?>(null) }
    var answeredCorrect by remember(cardId) { mutableStateOf<Boolean?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        val current = card
        if (current == null) {
            EmptyState(message = "未找到这张乐理卡")
            return@Column
        }

        Text(
            text = current.title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = InkPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = theoryCategoryText(current.category),
            fontSize = 14.sp,
            color = InkSecondary
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 正文：按 \n 分段，"- " 开头的行显示为圆点列表
        for (paragraph in current.content.split("\n")) {
            if (paragraph.isBlank()) continue
            if (paragraph.startsWith("- ")) {
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text(text = "• ", fontSize = 16.sp, color = InkPrimary)
                    Text(
                        text = paragraph.removePrefix("- "),
                        fontSize = 16.sp,
                        color = InkPrimary
                    )
                }
            } else {
                Text(
                    text = paragraph,
                    fontSize = 16.sp,
                    color = InkPrimary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 小测验
        SectionCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                SectionTitle(text = "小测验")
                Text(
                    text = current.quizQuestion,
                    fontSize = 16.sp,
                    color = InkPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                for ((index, option) in current.quizOptions.withIndex()) {
                    SecondaryButton(
                        text = option,
                        onClick = {
                            if (selectedOption != null) return@SecondaryButton
                            selectedOption = index
                            val correct = index == current.quizAnswer
                            answeredCorrect = correct
                            if (correct) {
                                vm.onAnsweredCorrect()
                            }
                        },
                        enabled = selectedOption == null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                when (answeredCorrect) {
                    true -> {
                        Text(
                            text = "答对了 ✓ 乐理 +1",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LeafGreen
                        )
                    }
                    false -> {
                        Text(
                            text = "正确答案是：${current.quizOptions[current.quizAnswer]}",
                            fontSize = 16.sp,
                            color = InkPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "再读一遍卡片内容",
                            fontSize = 16.sp,
                            color = InkSecondary
                        )
                    }
                    null -> {}
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 上一张 / 下一张：按 card_01…card_20 顺序，首尾禁用对应按钮
        val sortedIds = allCardIds.ifEmpty { listOf(cardId) }
        val currentIndex = sortedIds.indexOf(cardId).coerceAtLeast(0)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SecondaryButton(
                text = "上一张",
                onClick = {
                    val prevId = sortedIds.getOrNull(currentIndex - 1) ?: return@SecondaryButton
                    navController.navigate("theory/$prevId")
                },
                enabled = currentIndex > 0,
                modifier = Modifier.weight(1f)
            )
            PrimaryButton(
                text = "下一张",
                onClick = {
                    val nextId = sortedIds.getOrNull(currentIndex + 1) ?: return@PrimaryButton
                    navController.navigate("theory/$nextId")
                },
                enabled = currentIndex < sortedIds.size - 1,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
