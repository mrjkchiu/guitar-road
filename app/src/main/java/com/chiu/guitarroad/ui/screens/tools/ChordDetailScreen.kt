package com.chiu.guitarroad.ui.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.chiu.guitarroad.ui.components.PrimaryButton
import com.chiu.guitarroad.ui.components.SectionCard
import com.chiu.guitarroad.ui.components.SectionTitle
import com.chiu.guitarroad.ui.theme.BrickRed
import com.chiu.guitarroad.ui.theme.CreamBg
import com.chiu.guitarroad.ui.theme.InkPrimary
import com.chiu.guitarroad.ui.theme.InkSecondary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChordDetailViewModel(
    private val repo: GuitarRepository,
    private val chordId: String
) : ViewModel() {

    private val _chord = MutableStateFlow<Chord?>(null)
    val chord: StateFlow<Chord?> = _chord.asStateFlow()

    private val _nextInLibrary = MutableStateFlow(false)
    val nextInLibrary: StateFlow<Boolean> = _nextInLibrary.asStateFlow()

    init {
        viewModelScope.launch {
            val found = repo.getChord(chordId)
            _chord.value = found
            val next = found?.next
            _nextInLibrary.value = next != null && repo.getChord(next) != null
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChordDetailScreen(chordId: String, navController: NavController, repo: GuitarRepository) {
    val vm: ChordDetailViewModel = viewModel(
        factory = viewModelFactory { initializer { ChordDetailViewModel(repo, chordId) } }
    )
    val chord by vm.chord.collectAsState()
    val nextInLibrary by vm.nextInLibrary.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        val current = chord
        if (current == null) {
            EmptyState(message = "未找到该和弦")
            return@Column
        }

        Text(
            text = current.id,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = InkPrimary
        )
        Text(
            text = "${current.label} · ${chordDifficultyText(current.difficulty)}",
            fontSize = 16.sp,
            color = InkSecondary
        )
        Spacer(modifier = Modifier.height(16.dp))

        SectionCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                ChordDiagram(
                    strings = current.strings,
                    fingers = current.fingers,
                    modifier = Modifier.fillMaxWidth().height(260.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "手指编号：1 食指 / 2 中指 / 3 无名指 / 4 小指",
                    fontSize = 14.sp,
                    color = InkSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionTitle(text = "按法步骤")
        for ((index, step) in current.steps.withIndex()) {
            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                Text(
                    text = "${index + 1}. ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = InkPrimary
                )
                Text(text = step, fontSize = 16.sp, color = InkPrimary)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionTitle(text = "常见错误")
        for (mistake in current.mistakes) {
            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                Text(
                    text = "✗ ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrickRed
                )
                Text(text = mistake, fontSize = 16.sp, color = InkPrimary)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        val next = current.next
        if (next != null) {
            if (nextInLibrary) {
                PrimaryButton(
                    text = "下一个推荐：$next",
                    onClick = { navController.navigate("chord/$next") },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                PrimaryButton(
                    text = "下一个推荐：$next（暂未收录）",
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (current.nodeIds.isNotEmpty()) {
            SectionTitle(text = "相关节点")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (nodeNumber in current.nodeIds) {
                    val nodeId = "node_" + nodeNumber.toString().padStart(2, '0')
                    FilterChip(
                        selected = false,
                        onClick = { navController.navigate("task/$nodeId") },
                        label = { Text(text = "节点 $nodeNumber", fontSize = 14.sp) }
                    )
                }
            }
        }
    }
}
