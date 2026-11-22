package com.chiu.guitarroad.ui.screens.tools

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.chiu.guitarroad.data.repository.GuitarRepository
import com.chiu.guitarroad.ui.components.SectionCard
import com.chiu.guitarroad.ui.theme.CreamBg
import com.chiu.guitarroad.ui.theme.InkPrimary
import com.chiu.guitarroad.ui.theme.InkSecondary
import androidx.compose.foundation.background

@Composable
fun ToolsScreen(navController: NavController, repo: GuitarRepository) {
    val vm: ToolsViewModel = viewModel(
        factory = viewModelFactory { initializer { ToolsViewModel(repo) } }
    )
    val chordCount by vm.chordCount.collectAsState()
    val theoryCardCount by vm.theoryCardCount.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "工具",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = InkPrimary
        )
        Spacer(modifier = Modifier.height(16.dp))

        ToolEntryCard(
            icon = "🎵",
            name = "调音器",
            description = "让每根弦都准到 ±5 cents",
            onClick = { navController.navigate("tuner?nodeId=none") }
        )
        Spacer(modifier = Modifier.height(12.dp))
        ToolEntryCard(
            icon = "⏱️",
            name = "节拍器",
            description = "稳住节奏，从 60 BPM 开始",
            onClick = { navController.navigate("metronome?nodeId=none&bpm=60") }
        )
        Spacer(modifier = Modifier.height(12.dp))
        ToolEntryCard(
            icon = "🎼",
            name = "和弦谱库",
            description = if (chordCount > 0) "共 $chordCount 个常用和弦指板图" else "常用和弦指板图",
            onClick = { navController.navigate("chords") }
        )
        Spacer(modifier = Modifier.height(12.dp))
        ToolEntryCard(
            icon = "📖",
            name = "乐理卡片",
            description = if (theoryCardCount > 0) "共 $theoryCardCount 张卡，一次一张，3 分钟懂一个概念" else "一次一张卡，3 分钟懂一个概念",
            onClick = { navController.navigate("theory") }
        )
    }
}

@Composable
private fun ToolEntryCard(icon: String, name: String, description: String, onClick: () -> Unit) {
    SectionCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 32.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = InkPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = InkSecondary
                )
            }
        }
    }
}
