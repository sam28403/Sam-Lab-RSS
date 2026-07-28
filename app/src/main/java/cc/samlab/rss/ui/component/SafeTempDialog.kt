package cc.samlab.rss.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cc.samlab.rss.R
import cc.samlab.rss.ui.component.base.RYDialog
import kotlin.math.roundToInt

@Composable
fun SafeTempDialog(
    visible: Boolean,
    currentMaxTemp: Int,
    currentDeviceTemp: Float,
    onConfirm: (Int) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var tempValue by remember(currentMaxTemp) { mutableStateOf(currentMaxTemp.toFloat()) }
    val hapticFeedback = LocalHapticFeedback.current

    RYDialog(
        visible = visible,
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = stringResource(R.string.safe_temperature_settings))
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                Text(
                    text = stringResource(R.string.temperature_threshold, tempValue.roundToInt()),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Slider(
                    value = tempValue,
                    onValueChange = {
                        if (it.roundToInt() != tempValue.roundToInt()) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                        }
                        tempValue = it
                    },
                    valueRange = 35f..45f,
                    steps = 9
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.current_temperature, currentDeviceTemp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(tempValue.roundToInt()) }) {
                Text(text = stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}
