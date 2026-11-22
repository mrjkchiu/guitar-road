package com.chiu.guitarroad.ui.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
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
import com.chiu.guitarroad.ui.components.EmptyState
import com.chiu.guitarroad.ui.components.SectionCard
import com.chiu.guitarroad.ui.theme.CreamBg
import com.chiu.guitarroad.ui.theme.InkPrimary
import com.chiu.guitarroad.ui.theme.InkSecondary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class TheoryViewModel(repo: GuitarRepository) : ViewModel() {
    val cards: StateFlow<List<TheoryCard>> = repo.observeTheoryCards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

private enum class TheoryFilter(val label: String, val category: String?) {
    ALL("全部", null),
    READING("读谱", "reading"),
    PITCH("音高", "pitch"),
    HARMONY("和声", "harmony"),
    RHYTHM("节奏", "rhythm")
}

fun theoryCategoryText(category: String): String = when (category) {
    "reading" -> "读谱"
    "pitch" -> "音高"
    "harmony" -> "和声"
    "rhythm" -> "节奏"
    else -> category
}

@Composable
fun TheoryScreen(navController: NavController, repo: GuitarRepository) {
    val vm: TheoryViewModel = viewModel(
        factory = viewModelFactory { initializer { TheoryViewModel(repo) } }
    )
    val cards by vm.cards.collectAsState()

    var filter by remember { mutableStateOf(TheoryFilter.ALL) }

    val filtered = if (filter.category == null) {
        cards
    } else {
        cards.filter { it.category == filter.category }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBg)
            .padding(16.dp)
    ) {
        Text(
            text = "乐理卡片",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = InkPrimary
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (option in TheoryFilter.entries) {
                FilterChip(
                    selected = filter == option,
                    onClick = { filter = option },
                    label = { Text(text = option.label, fontSize = 14.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            EmptyState(message = "这个分类下还没有卡片")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filtered, key = { it.id }) { card ->
                    SectionCard(
                        modifier = Modifier.fillMaxWidth().clickable {
                            navController.navigate("theory/${card.id}")
                        }
                    ) {
                        Column {
                            Text(
                                text = card.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = InkPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = theoryCategoryText(card.category),
                                fontSize = 14.sp,
                                color = InkSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val preview = card.content.replace("\n", " ")
                            Text(
                                text = if (preview.length > 60) preview.take(60) + "…" else preview,
                                fontSize = 16.sp,
                                color = InkPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
