package com.chiu.guitarroad.ui.screens.profile

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.chiu.guitarroad.data.repository.GuitarRepository
import com.chiu.guitarroad.ui.components.PrimaryButton
import com.chiu.guitarroad.ui.components.SecondaryButton
import com.chiu.guitarroad.ui.components.SectionCard
import com.chiu.guitarroad.ui.components.SectionTitle
import com.chiu.guitarroad.ui.theme.CreamBg
import com.chiu.guitarroad.ui.theme.InkPrimary
import com.chiu.guitarroad.ui.theme.InkSecondary
import com.chiu.guitarroad.ui.theme.Wood
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(navController: NavController, repo: GuitarRepository) {
    val vm: ProfileViewModel = viewModel(factory = viewModelFactory { initializer { ProfileViewModel(repo) } })
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    var pendingExportJson by remember { mutableStateOf<String?>(null) }
    val createDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            val json = pendingExportJson ?: return@let
            runCatching {
                context.contentResolver.openOutputStream(it)?.use { stream ->
                    stream.write(json.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(context, "已导出", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, "导出失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(state.cleared) {
        if (state.cleared) {
            Toast.makeText(context, "已清除", Toast.LENGTH_SHORT).show()
            navController.navigate("onboarding") {
                popUpTo("main") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(text = "我的", fontSize = 22.sp, color = InkPrimary)
        Spacer(modifier = Modifier.height(16.dp))

        // 昵称（只读显示）
        SectionCard(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = "昵称", fontSize = 16.sp, color = InkSecondary)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = state.nickname, fontSize = 16.sp, color = InkPrimary)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 作品档案
        SectionCard(modifier = Modifier.fillMaxWidth()) {
            SectionTitle(text = "作品档案")
            Spacer(modifier = Modifier.height(8.dp))
            if (state.works.isEmpty()) {
                Text(
                    text = "录下你的第一段弹唱，3 个月后回听会惊讶",
                    fontSize = 16.sp,
                    color = InkSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                SecondaryButton(
                    text = "去录音",
                    onClick = { vm.showAddWorkDialog(true) }
                )
            } else {
                state.works.forEach { work ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Text(
                            text = work.title,
                            fontSize = 16.sp,
                            color = InkPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = dateFormat.format(Date(work.createdAt)),
                            fontSize = 14.sp,
                            color = InkSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                SecondaryButton(
                    text = "去录音",
                    onClick = { vm.showAddWorkDialog(true) }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 数据导出
        SectionCard(modifier = Modifier.fillMaxWidth()) {
            SectionTitle(text = "数据导出")
            Spacer(modifier = Modifier.height(8.dp))
            SecondaryButton(
                text = "导出全部数据为 JSON",
                onClick = {
                    scope.launch {
                        pendingExportJson = vm.exportAllData()
                        createDocumentLauncher.launch("guitar-road-backup.json")
                    }
                }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 清除学习记录
        SectionCard(modifier = Modifier.fillMaxWidth()) {
            SectionTitle(text = "学习记录")
            Spacer(modifier = Modifier.height(8.dp))
            SecondaryButton(
                text = "清除所有学习记录",
                onClick = { vm.showClearDialog(true) }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 关于
        SectionCard(modifier = Modifier.fillMaxWidth()) {
            SectionTitle(text = "关于")
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "吉他提升之路", fontSize = 16.sp, color = InkPrimary)
            Text(text = "版本 1.0.0", fontSize = 14.sp, color = InkSecondary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "调音与录音仅在本机处理，不上传、不存储",
                fontSize = 14.sp,
                color = InkSecondary
            )
            Text(text = "无账号，数据仅存本机", fontSize = 14.sp, color = InkSecondary)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }

    // 添加作品记录
    if (state.showAddWorkDialog) {
        var title by remember { mutableStateOf("") }
        var durationText by remember { mutableStateOf("") }
        var note by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { vm.showAddWorkDialog(false) },
            title = { Text(text = "添加作品记录", fontSize = 18.sp, color = InkPrimary) },
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
                        value = durationText,
                        onValueChange = { durationText = it.filter { ch -> ch.isDigit() } },
                        label = { Text(text = "时长（分钟）") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text(text = "备注（可选）") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val minutes = durationText.toIntOrNull()?.takeIf { it > 0 } ?: 0
                        vm.addWork(title.trim(), minutes, note)
                    },
                    enabled = title.isNotBlank()
                ) {
                    Text(text = "保存", color = Wood)
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.showAddWorkDialog(false) }) {
                    Text(text = "取消", color = InkSecondary)
                }
            }
        )
    }

    // 清除二次确认
    if (state.showClearDialog) {
        AlertDialog(
            onDismissRequest = { vm.showClearDialog(false) },
            title = { Text(text = "清除所有学习记录", fontSize = 18.sp, color = InkPrimary) },
            text = {
                Text(
                    text = "将清除全部学习进度、练习记录、雷达分数和作品档案，静态课程内容不受影响。确定要清除吗？",
                    fontSize = 16.sp,
                    color = InkPrimary
                )
            },
            confirmButton = {
                TextButton(onClick = { vm.clearAllLearningData() }) {
                    Text(text = "确定清除", color = Wood)
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.showClearDialog(false) }) {
                    Text(text = "取消", color = InkSecondary)
                }
            }
        )
    }
}
