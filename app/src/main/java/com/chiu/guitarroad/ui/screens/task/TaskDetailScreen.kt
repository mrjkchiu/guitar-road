package com.chiu.guitarroad.ui.screens.task

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.chiu.guitarroad.data.model.NodeTool
import com.chiu.guitarroad.data.model.SelfRating
import com.chiu.guitarroad.data.repository.GuitarRepository
import com.chiu.guitarroad.ui.components.PrimaryButton
import com.chiu.guitarroad.ui.components.SecondaryButton
import com.chiu.guitarroad.ui.components.SectionCard
import com.chiu.guitarroad.ui.components.SectionTitle
import com.chiu.guitarroad.ui.theme.Amber
import com.chiu.guitarroad.ui.theme.BrickRed
import com.chiu.guitarroad.ui.theme.CreamBg
import com.chiu.guitarroad.ui.theme.InkPrimary
import com.chiu.guitarroad.ui.theme.InkSecondary
import com.chiu.guitarroad.ui.theme.LeafGreen
import com.chiu.guitarroad.ui.theme.Wood
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.heightIn

@Composable
fun TaskDetailScreen(nodeId: String, navController: NavController, repo: GuitarRepository) {
    val vm: TaskDetailViewModel = viewModel(
        factory = viewModelFactory { initializer { TaskDetailViewModel(nodeId, repo) } }
    )
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.navigateTo) {
        state.navigateTo?.let { route ->
            if (route == "main") {
                navController.navigate("main") {
                    popUpTo("main") { inclusive = true }
                }
            } else {
                navController.navigate(route)
            }
        }
    }

    val node = state.node

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { navController.popBackStack() }) {
                Text(text = "← 返回", fontSize = 16.sp, color = Wood)
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = if (node != null) "节点 ${state.nodeNumber} / 共 30" else "",
                fontSize = 14.sp,
                color = InkSecondary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (node == null) {
            Text(text = "正在加载任务…", fontSize = 16.sp, color = InkSecondary)
            return@Column
        }

        Text(text = node.title, fontSize = 22.sp, color = InkPrimary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "预计 ${node.minutes} 分钟", fontSize = 14.sp, color = InkSecondary)
        Spacer(modifier = Modifier.height(16.dp))

        // 为什么学这个
        SectionCard(modifier = Modifier.fillMaxWidth()) {
            SectionTitle(text = "为什么学这个")
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = node.why, fontSize = 16.sp, color = InkPrimary)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 怎么练
        SectionCard(modifier = Modifier.fillMaxWidth()) {
            SectionTitle(text = "怎么练")
            Spacer(modifier = Modifier.height(8.dp))
            node.steps.forEachIndexed { index, step ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(text = "${index + 1}. ", fontSize = 16.sp, color = Wood)
                    Text(
                        text = step,
                        fontSize = 16.sp,
                        color = InkPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 本任务工具
        if (node.tools.isNotEmpty()) {
            SectionCard(modifier = Modifier.fillMaxWidth()) {
                SectionTitle(text = "本任务工具")
                Spacer(modifier = Modifier.height(8.dp))
                node.tools.forEach { tool ->
                    ToolRow(tool = tool, nodeId = nodeId, navController = navController)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 别人怎么讲
        SectionCard(modifier = Modifier.fillMaxWidth()) {
            SectionTitle(text = "别人怎么讲")
            Spacer(modifier = Modifier.height(8.dp))
            if (state.resourceLinks.isEmpty() && state.userResources.isEmpty()) {
                Text(text = "这个任务还没有收录视频", fontSize = 16.sp, color = InkSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    node.resourceTags.forEach { tag ->
                        Text(text = tag, fontSize = 14.sp, color = InkSecondary)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                SecondaryButton(
                    text = "添加我的链接",
                    onClick = { vm.showAddResourceDialog(true) }
                )
            } else {
                state.resourceLinks.forEach { link ->
                    ResourceCard(
                        platformLabel = when (link.platform) {
                            "bilibili" -> "B站"
                            "douyin" -> "抖音"
                            else -> link.platform
                        },
                        platformColor = when (link.platform) {
                            "bilibili" -> Color(0xFF00A1D6)
                            "douyin" -> Color.Black
                            else -> Wood
                        },
                        title = link.title,
                        author = link.author,
                        durationText = formatDuration(link.durationSec),
                        difficulty = link.difficulty,
                        sourceNote = link.sourceNote,
                        usefulCount = link.usefulCount,
                        openLabel = when (link.platform) {
                            "douyin" -> "去抖音看 →"
                            else -> "去 B站看 →"
                        },
                        showActions = true,
                        onOpen = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(link.url))
                            )
                        },
                        onUseful = { vm.markResourceUseful(link.id) },
                        onReport = { vm.reportResourceBroken(link.id) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                state.userResources.forEach { item ->
                    ResourceCard(
                        platformLabel = "我的链接",
                        platformColor = Wood,
                        title = item.title,
                        author = "",
                        durationText = "",
                        difficulty = "",
                        sourceNote = "由我添加",
                        usefulCount = 0,
                        openLabel = "打开链接 →",
                        showActions = false,
                        onOpen = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(item.url))
                            )
                        },
                        onUseful = {},
                        onReport = {}
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                SecondaryButton(
                    text = "添加我的链接",
                    onClick = { vm.showAddResourceDialog(true) }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 验收标准
        SectionCard(modifier = Modifier.fillMaxWidth()) {
            SectionTitle(text = "验收标准")
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = node.criteria, fontSize = 16.sp, color = InkPrimary)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 自评
        SectionCard(modifier = Modifier.fillMaxWidth()) {
            SectionTitle(text = "练完感觉怎么样？")
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RatingChip("太简单", SelfRating.TOO_EASY, state.rating) { vm.selectRating(it) }
                RatingChip("刚好", SelfRating.JUST_RIGHT, state.rating) { vm.selectRating(it) }
                RatingChip("太难", SelfRating.TOO_HARD, state.rating) { vm.selectRating(it) }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        PrimaryButton(
            text = "我练好了 ✓",
            onClick = { vm.completeNode() },
            enabled = state.theoryGatePassed,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        if ((state.progress?.attempts ?: 0) >= 3) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                TextButton(onClick = { vm.skipNode() }) {
                    Text(text = "跳过", fontSize = 16.sp, color = Wood)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }

    // 乐理卡自动弹出
    if (state.showTheoryDialog && state.theoryCard != null) {
        val card = state.theoryCard!!
        AlertDialog(
            onDismissRequest = { vm.dismissTheoryDialog() },
            title = { Text(text = card.title, fontSize = 18.sp, color = InkPrimary) },
            text = {
                Column {
                    Text(text = card.content, fontSize = 16.sp, color = InkPrimary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = card.quizQuestion, fontSize = 16.sp, color = InkPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    card.quizOptions.forEachIndexed { index, option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = state.quizSelected == index,
                                    onClick = { vm.selectQuizOption(index) },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = state.quizSelected == index,
                                onClick = { vm.selectQuizOption(index) }
                            )
                            Text(text = option, fontSize = 16.sp, color = InkPrimary)
                        }
                    }
                    if (state.quizAnswered) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (state.quizCorrect) {
                                "答对了 ✓ 乐理 +1"
                            } else {
                                "答错了，正确答案是：${card.quizOptions[card.quizAnswer]}，再读一遍卡片内容"
                            },
                            fontSize = 14.sp,
                            color = if (state.quizCorrect) LeafGreen else BrickRed
                        )
                    }
                }
            },
            confirmButton = {
                if (!state.quizAnswered) {
                    TextButton(
                        onClick = { vm.submitQuizAnswer() },
                        enabled = state.quizSelected != null
                    ) {
                        Text(text = "提交答案", color = Wood)
                    }
                } else {
                    TextButton(onClick = { vm.dismissTheoryDialog() }) {
                        Text(text = "知道了", color = Wood)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.dismissTheoryDialog() }) {
                    Text(text = "稍后看", color = InkSecondary)
                }
            }
        )
    }

    // 添加我的链接
    if (state.showAddResourceDialog) {
        var title by remember { mutableStateOf("") }
        var url by remember { mutableStateOf("") }
        var platform by remember { mutableStateOf("bilibili") }
        AlertDialog(
            onDismissRequest = { vm.showAddResourceDialog(false) },
            title = { Text(text = "添加我的链接", fontSize = 18.sp, color = InkPrimary) },
            text = {
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(text = "标题") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        label = { Text(text = "链接") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Row(
                            modifier = Modifier.selectable(
                                selected = platform == "bilibili",
                                onClick = { platform = "bilibili" },
                                role = Role.RadioButton
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = platform == "bilibili",
                                onClick = { platform = "bilibili" }
                            )
                            Text(text = "B站", fontSize = 16.sp)
                        }
                        Row(
                            modifier = Modifier.selectable(
                                selected = platform == "douyin",
                                onClick = { platform = "douyin" },
                                role = Role.RadioButton
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = platform == "douyin",
                                onClick = { platform = "douyin" }
                            )
                            Text(text = "抖音", fontSize = 16.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { vm.addUserResource(title.trim(), url.trim(), platform) },
                    enabled = title.isNotBlank() && url.isNotBlank()
                ) {
                    Text(text = "保存", color = Wood)
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.showAddResourceDialog(false) }) {
                    Text(text = "取消", color = InkSecondary)
                }
            }
        )
    }
}

@Composable
private fun ToolRow(tool: NodeTool, nodeId: String, navController: NavController) {
    val (label, route) = when (tool.type) {
        "tuner" -> "打开调音器" to "tuner?nodeId=$nodeId"
        "metronome" -> "打开节拍器 ${tool.bpm ?: 60}BPM" to
            "metronome?nodeId=$nodeId&bpm=${tool.bpm ?: 60}"
        "chord" -> "查看 ${tool.target ?: ""} 指板图" to "chord/${tool.target ?: ""}"
        "theory" -> "查看乐理卡" to "theory/${tool.target ?: ""}"
        else -> return
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate(route) }
            .heightIn(min = 48.dp)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 16.sp, color = Wood)
        Text(text = "→", fontSize = 16.sp, color = Wood)
    }
}

@Composable
private fun ResourceCard(
    platformLabel: String,
    platformColor: Color,
    title: String,
    author: String,
    durationText: String,
    difficulty: String,
    sourceNote: String,
    usefulCount: Int,
    openLabel: String,
    showActions: Boolean,
    onOpen: () -> Unit,
    onUseful: () -> Unit,
    onReport: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = platformLabel,
                fontSize = 14.sp,
                color = Color.White,
                modifier = Modifier
                    .background(platformColor, shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
            Spacer(modifier = Modifier.padding(4.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                color = InkPrimary,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        val meta = listOf(author, durationText, difficulty).filter { it.isNotBlank() }
        if (meta.isNotEmpty()) {
            Text(text = meta.joinToString(" · "), fontSize = 14.sp, color = InkSecondary)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = sourceNote, fontSize = 14.sp, color = InkSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = openLabel,
                fontSize = 14.sp,
                color = Wood,
                modifier = Modifier.clickable { onOpen() }
            )
            if (showActions) {
                Text(
                    text = "⭐ 有用" + if (usefulCount > 0) " $usefulCount" else "",
                    fontSize = 14.sp,
                    color = Amber,
                    modifier = Modifier.clickable { onUseful() }
                )
                Text(
                    text = "举报失效",
                    fontSize = 14.sp,
                    color = BrickRed,
                    modifier = Modifier.clickable { onReport() }
                )
            }
        }
    }
}

@Composable
private fun RatingChip(
    label: String,
    rating: SelfRating,
    selected: SelfRating,
    onSelect: (SelfRating) -> Unit
) {
    val isSelected = rating == selected
    Text(
        text = label,
        fontSize = 16.sp,
        color = if (isSelected) Wood else InkSecondary,
        modifier = Modifier
            .clickable { onSelect(rating) }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    )
}

private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return "%d:%02d".format(minutes, secs)
}
