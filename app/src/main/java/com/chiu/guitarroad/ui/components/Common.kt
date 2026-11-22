package com.chiu.guitarroad.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chiu.guitarroad.ui.theme.CardWhite
import com.chiu.guitarroad.ui.theme.DisabledGray
import com.chiu.guitarroad.ui.theme.InkPrimary
import com.chiu.guitarroad.ui.theme.InkSecondary
import com.chiu.guitarroad.ui.theme.LineDivider
import com.chiu.guitarroad.ui.theme.Wood

/** 木色实心主按钮：白字、高 48dp、圆角 12dp、16sp Medium。 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Wood,
            contentColor = Color.White,
            disabledContainerColor = DisabledGray,
            disabledContentColor = Color.White
        )
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

/** 木色描边次按钮：木色 1dp 描边、木色字，同尺寸。 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Wood),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Wood,
            disabledContentColor = DisabledGray
        )
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

/** 白卡：圆角 16dp、内边距 16dp、1px 边框 #E3DCD2、2dp 轻微阴影。 */
@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = CardWhite,
        border = BorderStroke(1.dp, LineDivider),
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            content()
        }
    }
}

/** 区块标题：18sp SemiBold + 下方 4dp 间距（标题到内容要比区块间距紧）。 */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            color = InkPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
    }
}

/** 空状态：文案 + 可选行动按钮。 */
@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            fontSize = 16.sp,
            color = InkSecondary,
            textAlign = TextAlign.Center
        )
        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(16.dp))
            PrimaryButton(text = actionText, onClick = onAction)
        }
    }
}
