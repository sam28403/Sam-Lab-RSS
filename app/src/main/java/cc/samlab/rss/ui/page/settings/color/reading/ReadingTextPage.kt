package cc.samlab.rss.ui.page.settings.color.reading

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cc.samlab.rss.R
import cc.samlab.rss.infrastructure.preference.LocalReadingTextLetterSpacing
import cc.samlab.rss.infrastructure.preference.LocalReadingTextAlign
import cc.samlab.rss.infrastructure.preference.LocalReadingTextBold
import cc.samlab.rss.infrastructure.preference.LocalReadingTextFontSize
import cc.samlab.rss.infrastructure.preference.LocalReadingTextHorizontalPadding
import cc.samlab.rss.infrastructure.preference.LocalReadingTextLineHeight
import cc.samlab.rss.infrastructure.preference.LocalReadingTheme
import cc.samlab.rss.infrastructure.preference.ReadingTextLetterSpacingPreference
import cc.samlab.rss.infrastructure.preference.ReadingTextAlignPreference
import cc.samlab.rss.infrastructure.preference.ReadingTextFontSizePreference
import cc.samlab.rss.infrastructure.preference.ReadingTextHorizontalPaddingPreference
import cc.samlab.rss.infrastructure.preference.ReadingTextLineHeightPreference
import cc.samlab.rss.infrastructure.preference.ReadingTextLineHeightPreference.coerceToRange
import cc.samlab.rss.infrastructure.preference.ReadingThemePreference
import cc.samlab.rss.infrastructure.preference.not
import cc.samlab.rss.ui.component.base.DisplayText
import cc.samlab.rss.ui.component.base.FeedbackIconButton
import cc.samlab.rss.ui.component.base.RYScaffold
import cc.samlab.rss.ui.component.base.RYSwitch
import cc.samlab.rss.ui.component.base.RadioDialog
import cc.samlab.rss.ui.component.base.RadioDialogOption
import cc.samlab.rss.ui.component.base.Subtitle
import cc.samlab.rss.ui.component.base.TextFieldDialog
import cc.samlab.rss.ui.page.settings.SettingItem
import cc.samlab.rss.ui.theme.palette.onLight

@Composable
fun ReadingTextPage(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val readingTheme = LocalReadingTheme.current
    val fontSize = LocalReadingTextFontSize.current
    val lineHeight = LocalReadingTextLineHeight.current
    val letterSpacing = LocalReadingTextLetterSpacing.current
    val horizontalPadding = LocalReadingTextHorizontalPadding.current
    val align = LocalReadingTextAlign.current
    val bold = LocalReadingTextBold.current

    var fontSizeDialogVisible by remember { mutableStateOf(false) }
    var lineHeightDialogVisible by remember { mutableStateOf(false) }
    var letterSpacingDialogVisible by remember { mutableStateOf(false) }
    var horizontalPaddingDialogVisible by remember { mutableStateOf(false) }
    var alignDialogVisible by remember { mutableStateOf(false) }

    var fontSizeValue: Int? by remember { mutableStateOf(fontSize) }
    var letterSpacingValue: String? by remember { mutableStateOf(letterSpacing.toString()) }
    var lineHeightMultipleValue: String by remember(lineHeight) { mutableStateOf(lineHeight.toString()) }
    var horizontalPaddingValue: Int? by remember { mutableStateOf(horizontalPadding) }

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
                    DisplayText(text = stringResource(R.string.text), desc = "")
                }

                // Preview
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                MaterialTheme.colorScheme.inverseOnSurface
                                        onLight MaterialTheme.colorScheme.surface.copy(0.7f)
                            )
                            .clickable { },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TitleAndTextPreview()
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Text
                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(R.string.text)
                    )
                    SettingItem(
                        title = stringResource(R.string.font_size),
                        desc = "${fontSize}sp",
                        onClick = { fontSizeDialogVisible = true },
                    ) {}
                    SettingItem(
                        title = stringResource(R.string.bold),
                        onClick = {
                            (!bold).put(context, scope)
                            ReadingThemePreference.Custom.put(context, scope)
                        },
                    ) {
                        RYSwitch(activated = bold.value) {
                            (!bold).put(context, scope)
                            ReadingThemePreference.Custom.put(context, scope)
                        }
                    }
                    SettingItem(
                        title = stringResource(R.string.letter_spacing),
                        desc = "${letterSpacing}sp",
                        onClick = { letterSpacingDialogVisible = true },
                    ) {}
                    SettingItem(
                        title = stringResource(R.string.line_height_multiple),
                        desc = lineHeightMultipleValue,
                        onClick = { lineHeightDialogVisible = true },
                    ) {}
                    SettingItem(
                        title = stringResource(R.string.horizontal_padding),
                        desc = "${horizontalPadding}dp",
                        onClick = { horizontalPaddingDialogVisible = true },
                    ) {}
                    SettingItem(
                        title = stringResource(R.string.alignment),
                        desc = align.toDesc(context),
                        onClick = { alignDialogVisible = true },
                    ) {}
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        }
    )

    TextFieldDialog(
        visible = fontSizeDialogVisible,
        title = stringResource(R.string.font_size),
        value = (fontSizeValue ?: "").toString(),
        placeholder = stringResource(R.string.value),
        onValueChange = {
            fontSizeValue = it.filter { it.isDigit() }.toIntOrNull()
        },
        onDismissRequest = {
            fontSizeDialogVisible = false
        },
        onConfirm = {
            ReadingTextFontSizePreference.put(context, scope, fontSizeValue ?: 0)
            ReadingThemePreference.Custom.put(context, scope)
            fontSizeDialogVisible = false
        }
    )

    TextFieldDialog(
        visible = lineHeightDialogVisible,
        title = stringResource(R.string.line_height_multiple),
        value = lineHeightMultipleValue,
        placeholder = stringResource(R.string.value),
        onValueChange = {
            lineHeightMultipleValue = it
        },
        onDismissRequest = {
            lineHeightDialogVisible = false
        },
        onConfirm = {
            ReadingTextLineHeightPreference.put(
                context,
                scope,
                (lineHeightMultipleValue.toFloatOrNull()
                    ?: ReadingTextLineHeightPreference.default).coerceToRange()
            )
            ReadingThemePreference.Custom.put(context, scope)
            lineHeightDialogVisible = false
        }
    )

    TextFieldDialog(
        visible = letterSpacingDialogVisible,
        title = stringResource(R.string.letter_spacing),
        value = (letterSpacingValue ?: "").toString(),
        placeholder = stringResource(R.string.value),
        onValueChange = {
            letterSpacingValue = it
        },
        onDismissRequest = {
            letterSpacingDialogVisible = false
        },
        onConfirm = {
            ReadingTextLetterSpacingPreference.put(
                context,
                scope,
                letterSpacingValue?.toFloatOrNull() ?: 0F
            )
            ReadingThemePreference.Custom.put(context, scope)
            letterSpacingDialogVisible = false
        }
    )

    TextFieldDialog(
        visible = horizontalPaddingDialogVisible,
        title = stringResource(R.string.horizontal_padding),
        value = (horizontalPaddingValue ?: "").toString(),
        placeholder = stringResource(R.string.value),
        onValueChange = {
            horizontalPaddingValue = it.filter { it.isDigit() }.toIntOrNull()
        },
        onDismissRequest = {
            horizontalPaddingDialogVisible = false
        },
        onConfirm = {
            ReadingTextHorizontalPaddingPreference.put(context, scope, horizontalPaddingValue ?: 0)
            ReadingThemePreference.Custom.put(context, scope)
            horizontalPaddingDialogVisible = false
        }
    )

    RadioDialog(
        visible = alignDialogVisible,
        title = stringResource(R.string.alignment),
        options = ReadingTextAlignPreference.values.map {
            RadioDialogOption(
                text = it.toDesc(context),
                selected = it == align,
            ) {
                it.put(context, scope)
                ReadingThemePreference.Custom.put(context, scope)
            }
        }
    ) {
        alignDialogVisible = false
    }
}
