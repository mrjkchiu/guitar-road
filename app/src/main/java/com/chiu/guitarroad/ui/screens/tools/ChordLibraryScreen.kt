package com.chiu.guitarroad.ui.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.chiu.guitarroad.data.model.Chord
import com.chiu.guitarroad.data.repository.GuitarRepository
import com.chiu.guitarroad.ui.components.ChordDiagram
import com.chiu.guitarroad.ui.components.EmptyState
import com.chiu.guitarroad.ui.components.SectionCard
import com.chiu.guitarroad.ui.theme.CreamBg
import com.chiu.guitarroad.ui.theme.InkPrimary
import com.chiu.guitarroad.ui.theme.InkSecondary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ChordLibraryViewModel(repo: GuitarRepository) : ViewModel() {
    val chords: StateFlow<List<Chord>> = repo.observeChords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

private enum class ChordFilter(val label: String) {
    ALL("全部"), BEGINNER("入门"), INTERMEDIATE("初级"), ADVANCED("进阶"), BARRE("横按")
}

fun chordDifficultyText(difficulty: String): String = when (difficulty) {
    "beginner" -> "入门"
    "intermediate" -> "初级"
    "advanced" -> "进阶"
    else -> difficulty
}

@Composable
fun ChordLibraryScreen(navController: NavController, repo: GuitarRepository) {
    val vm: ChordLibraryViewModel = viewModel(
        factory = viewModelFactory { initializer { ChordLibraryViewModel(repo) } }
    )
    val chords by vm.chords.collectAsState()

    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(ChordFilter.ALL) }

    val filtered = chords.filter { chord ->
        val matchesQuery = query.isBlank() ||
            chord.id.contains(query, ignoreCase = true) ||
            chord.label.contains(query, ignoreCase = true)
        val matchesFilter = when (filter) {
            ChordFilter.ALL -> true
            ChordFilter.BEGINNER -> chord.difficulty == "beginner"
            ChordFilter.INTERMEDIATE -> chord.difficulty == "intermediate"
            ChordFilter.ADVANCED -> chord.difficulty == "advanced"
            ChordFilter.BARRE -> chord.barre
        }
        matchesQuery && matchesFilter
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBg)
            .padding(16.dp)
    ) {
        Text(
            text = "和弦谱库",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = InkPrimary
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = "搜索和弦", fontSize = 16.sp) },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 筛选 chips 横向滚动：窄屏上最后一个（横按）也不会被切掉。
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (option in ChordFilter.entries) {
                FilterChip(
                    selected = filter == option,
                    onClick = { filter = option },
                    label = { Text(text = option.label, fontSize = 14.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            EmptyState(message = "没有找到匹配的和弦，换个关键词试试")
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.id }) { chord ->
                    SectionCard(
                        modifier = Modifier.clickable {
                            navController.navigate("chord/${chord.id}")
                        }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            ChordDiagram(
                                strings = chord.strings,
                                fingers = chord.fingers,
                                modifier = Modifier.fillMaxWidth().height(140.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = chord.id,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = InkPrimary
                            )
                            Text(
                                text = chordDifficultyText(chord.difficulty),
                                fontSize = 14.sp,
                                color = InkSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
