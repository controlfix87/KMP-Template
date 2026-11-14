package com.cmptemplate.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cmptemplate.core.designsystem.ThemeMode
import com.cmptemplate.core.designsystem.generated.resources.Res
import com.cmptemplate.core.designsystem.generated.resources.language
import com.cmptemplate.core.designsystem.generated.resources.theme
import com.cmptemplate.core.designsystem.generated.resources.theme_dark
import com.cmptemplate.core.designsystem.generated.resources.theme_light
import com.cmptemplate.core.designsystem.generated.resources.theme_system
import com.cmptemplate.core.designsystem.i18n.AppLocale
import org.jetbrains.compose.resources.stringResource

/**
 * Language as flag cards. Same component in onboarding and About, so the two cannot drift.
 * Languages show as autonyms (never translated): see LanguagePickerDialog.
 */
@Composable
fun LanguageChoices(current: AppLocale, onSelect: (AppLocale) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel(stringResource(Res.string.language), AppIcons.Language)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppLocale.entries.forEach { locale ->
                OptionCard(
                    selected = locale == current,
                    label = locale.displayName,
                    onClick = { if (locale != current) onSelect(locale) },
                    modifier = Modifier.weight(1f).testTag("language_${locale.code}"),
                ) { Text(locale.flag, fontSize = 26.sp, modifier = Modifier.clearAndSetSemantics {}) }
            }
        }
    }
}

/** Follow the device, light or dark. */
@Composable
fun ThemeChoices(current: ThemeMode, onSelect: (ThemeMode) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel(stringResource(Res.string.theme), AppIcons.ThemeAuto)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                Triple(ThemeMode.SYSTEM, Res.string.theme_system, AppIcons.ThemeAuto),
                Triple(ThemeMode.LIGHT, Res.string.theme_light, AppIcons.LightMode),
                Triple(ThemeMode.DARK, Res.string.theme_dark, AppIcons.DarkMode),
            ).forEach { (mode, label, icon) ->
                OptionCard(
                    selected = mode == current,
                    label = stringResource(label),
                    onClick = { onSelect(mode) },
                    modifier = Modifier.weight(1f).testTag("theme_${mode.name.lowercase()}"),
                ) { Icon(icon, contentDescription = null, Modifier.size(28.dp)) }
            }
        }
    }
}
