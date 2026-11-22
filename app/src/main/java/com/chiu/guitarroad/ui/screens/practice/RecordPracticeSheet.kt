package com.chiu.guitarroad.ui.screens.practice

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chiu.guitarroad.data.model.PathNode
import com.chiu.guitarroad.data.model.SelfRating
import com.chiu.guitarroad.ui.components.PrimaryButton
import com.chiu.guitarroad.ui.components.SectionTitle
import com.chiu.guitarroad.ui.theme.InkPrimary
import com.chiu.guitarroad.ui.theme.InkSecondary
import com.chiu.guitarroad.ui.theme.Wood

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordPracticeSheet(
    nodes: List<PathNode>,
    onDismiss: () -> Unit,
    onSave: (nodeId: String?, durationMin: Int, rating: SelfRating?) -> Unit
) {
    var selectedNodeId by remember { mutableStateOf<String?>(null) }
    var durationText by remember { mutableStateOf("15") }
    var rating by remember { mutableStateOf(SelfRating.JUST_RIGHT) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            SectionTitle(text = "记一次练习")
            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "练了哪个任务", fontSize = 16.sp, color = InkPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            NodeOption(
                label = "自由练习",
                selected = selectedNodeId == null,
                onClick = { selectedNodeId = null }
            )
            nodes.forEach { node ->
                NodeOption(
                    label = node.title,
                    selected = selectedNodeId == node.id,
                    onClick = { selectedNodeId = node.id }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "练了多久", fontSize = 16.sp, color = InkPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = durationText,
                onValueChange = { durationText = it.filter { ch -> ch.isDigit() } },
                label = { Text(text = "时长（分钟）") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "感觉怎么样", fontSize = 16.sp, color = InkPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                RatingOption("太简单", SelfRating.TOO_EASY, rating) { rating = it }
                RatingOption("刚好", SelfRating.JUST_RIGHT, rating) { rating = it }
                RatingOption("太难", SelfRating.TOO_HARD, rating) { rating = it }
            }

            Spacer(modifier = Modifier.height(24.dp))
            PrimaryButton(
                text = "保存",
                onClick = {
                    val minutes = durationText.toIntOrNull()?.takeIf { it > 0 } ?: 15
                    onSave(selectedNodeId, minutes, rating)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun NodeOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text = label, fontSize = 16.sp, color = InkPrimary)
    }
}

@Composable
private fun RatingOption(
    label: String,
    value: SelfRating,
    selected: SelfRating,
    onSelect: (SelfRating) -> Unit
) {
    Text(
        text = label,
        fontSize = 16.sp,
        color = if (value == selected) Wood else InkSecondary,
        modifier = Modifier
            .clickable { onSelect(value) }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}
