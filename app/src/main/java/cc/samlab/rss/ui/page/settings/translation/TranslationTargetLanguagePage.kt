package cc.samlab.rss.ui.page.settings.translation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cc.samlab.rss.R
import cc.samlab.rss.infrastructure.preference.LocalTranslateTargetLanguage
import cc.samlab.rss.infrastructure.preference.TranslateTargetLanguagePreference
import cc.samlab.rss.ui.component.base.DisplayText
import cc.samlab.rss.ui.component.base.FeedbackIconButton
import cc.samlab.rss.ui.component.base.RYScaffold
import cc.samlab.rss.ui.page.settings.SettingItem
import cc.samlab.rss.ui.theme.palette.onLight

@Composable
fun TranslationTargetLanguagePage(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val currentPreference = LocalTranslateTargetLanguage.current
    val scope = rememberCoroutineScope()

    RYScaffold(
        containerColor = MaterialTheme.colorScheme.surface onLight MaterialTheme.colorScheme.inverseOnSurface,
        navigationIcon = {
            FeedbackIconButton(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = onBack
            )
        },
        content = {
            LazyColumn {
                item {
                    DisplayText(text = stringResource(R.string.translate_target_language), desc = "")
                }
                items(TranslateTargetLanguagePreference.values) { preference ->
                    SettingItem(
                        title = preference.toDesc(),
                        onClick = {
                            preference.put(context, scope)
                        },
                    ) {
                        RadioButton(
                            selected = preference == currentPreference,
                            onClick = {
                                preference.put(context, scope)
                            }
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        }
    )
}
