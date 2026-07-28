package cc.samlab.rss.ui.component

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import cc.samlab.rss.R
import cc.samlab.rss.domain.model.general.SyncWarning
import cc.samlab.rss.ui.component.base.RYDialog

@Composable
fun SyncWarningDialog(
    syncWarning: SyncWarning,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    if (syncWarning == SyncWarning.None) return

    val message = when (syncWarning) {
        SyncWarning.Metered -> stringResource(R.string.sync_metered_network_warning)
        SyncWarning.NotCharging -> stringResource(R.string.sync_not_charging_warning)
        SyncWarning.Overheat -> stringResource(R.string.sync_overheat_warning)
        SyncWarning.MeteredNotCharging -> stringResource(R.string.sync_metered_and_not_charging_warning)
        SyncWarning.MeteredOverheat -> stringResource(R.string.sync_metered_and_overheat_warning)
        SyncWarning.NotChargingOverheat -> stringResource(R.string.sync_not_charging_and_overheat_warning)
        SyncWarning.All -> stringResource(R.string.sync_all_warning)
        else -> ""
    }

    RYDialog(
        visible = true,
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = stringResource(R.string.synchronous))
        },
        text = {
            Text(text = message)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
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
