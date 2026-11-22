package com.chiu.guitarroad.ui.screens.tools

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.chiu.guitarroad.audio.AudioPitchRecorder
import com.chiu.guitarroad.audio.PitchResult
import com.chiu.guitarroad.audio.Tuning
import com.chiu.guitarroad.audio.TuningMode
import com.chiu.guitarroad.data.repository.GuitarRepository
import com.chiu.guitarroad.ui.components.EmptyState
import com.chiu.guitarroad.ui.components.SecondaryButton
import com.chiu.guitarroad.ui.components.TunerDial
import com.chiu.guitarroad.ui.theme.CreamBg
import com.chiu.guitarroad.ui.theme.DisabledGray
import com.chiu.guitarroad.ui.theme.InkPrimary
import com.chiu.guitarroad.ui.theme.InkSecondary
import com.chiu.guitarroad.ui.theme.LeafGreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

class TunerViewModel(private val repo: GuitarRepository) : ViewModel() {

    private val recorder = AudioPitchRecorder()

    private val _mode = MutableStateFlow(TuningMode.STANDARD)
    val mode: StateFlow<TuningMode> = _mode.asStateFlow()

    private val _pitch = MutableStateFlow(PitchResult.SILENT)
    val pitch: StateFlow<PitchResult> = _pitch.asStateFlow()

    // 每根弦最近一次测到的相对目标音 cents（弦号 → cents）
    private val _stringCents = MutableStateFlow<Map<Int, Float>>(emptyMap())
    val stringCents: StateFlow<Map<Int, Float>> = _stringCents.asStateFlow()

    init {
        recorder.onPitch = { result ->
            _pitch.value = result
            if (!result.isSilent) {
                val strings = Tuning.stringsFor(_mode.value)
                val target = Tuning.targetFor(result, strings)
                if (target != null) {
                    val cents = Tuning.centsToTarget(result, target)
                    _stringCents.value = _stringCents.value + (target.stringNumber to cents)
                }
            }
        }
    }

    fun setMode(newMode: TuningMode) {
        _mode.value = newMode
        _stringCents.value = emptyMap()
        _pitch.value = PitchResult.SILENT
    }

    fun startListening() {
        if (!recorder.isRunning) {
            recorder.start()
        }
    }

    fun stopListening() {
        if (recorder.isRunning) {
            recorder.stop()
        }
    }

    override fun onCleared() {
        stopListening()
        super.onCleared()
    }
}

@Composable
fun TunerScreen(nodeId: String, navController: NavController, repo: GuitarRepository) {
    val context = LocalContext.current
    val vm: TunerViewModel = viewModel(
        factory = viewModelFactory { initializer { TunerViewModel(repo) } }
    )
    val mode by vm.mode.collectAsState()
    val pitch by vm.pitch.collectAsState()
    val stringCents by vm.stringCents.collectAsState()

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var showPermissionDialog by remember { mutableStateOf(!hasPermission) }
    var permissionDenied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        permissionDenied = !granted
        if (granted) {
            vm.startListening()
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            vm.startListening()
        }
    }

    DisposableEffect(Unit) {
        onDispose { vm.stopListening() }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(text = "麦克风权限", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
            text = {
                Text(
                    text = "调音需要访问麦克风。音频仅在本机实时处理，不会上传、不会存储、不会录音。",
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionDialog = false
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }) {
                    Text(text = "知道了", fontSize = 16.sp)
                }
            }
        )
    }

    val nodeNumber = nodeId.removePrefix("node_").toIntOrNull()
    val strings = Tuning.stringsFor(mode)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "调音器",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = InkPrimary
            )
            TuningModeDropdown(mode = mode, onModeSelected = { vm.setMode(it) })
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (permissionDenied) {
            EmptyState(
                message = "调音需要麦克风权限，请在设置中开启",
                actionText = "去设置",
                onAction = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
            )
        } else {
            TunerDial(result = pitch)

            Spacer(modifier = Modifier.height(24.dp))

            // 6 弦状态条：弦号 + 目标音名 + ●/○
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (guitarString in strings) {
                    val cents = stringCents[guitarString.stringNumber]
                    val inTune = cents != null && abs(cents) <= 5f
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${guitarString.stringNumber}弦",
                            fontSize = 14.sp,
                            color = InkSecondary
                        )
                        Text(
                            text = "${guitarString.noteName}${guitarString.octave}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = InkPrimary
                        )
                        Text(
                            text = if (inTune) "●" else "○",
                            fontSize = 18.sp,
                            color = if (inTune) LeafGreen else DisabledGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "🎤 正在聆听…",
                fontSize = 16.sp,
                color = InkPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "音频仅在本机处理，不会上传",
                fontSize = 14.sp,
                color = InkSecondary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (nodeId != "none" && nodeNumber != null) {
            SecondaryButton(
                text = "返回节点 $nodeNumber ←",
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TuningModeDropdown(mode: TuningMode, onModeSelected: (TuningMode) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(text = "${mode.displayName} ▾", fontSize = 16.sp)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            for (tuningMode in TuningMode.entries) {
                DropdownMenuItem(
                    text = { Text(text = tuningMode.displayName, fontSize = 16.sp) },
                    onClick = {
                        expanded = false
                        onModeSelected(tuningMode)
                    }
                )
            }
        }
    }
}
