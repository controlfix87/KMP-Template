package com.kmptemplate.core.designsystem.i18n

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.kmptemplate.core.designsystem.generated.resources.Res
import com.kmptemplate.core.designsystem.generated.resources.language
import com.kmptemplate.core.designsystem.generated.resources.language_close
import org.jetbrains.compose.resources.stringResource

/**
 * The only supported way to change the app's language. Everything else about the locale — the
 * platform default, the layout direction, the resource directory — follows from what this writes,
 * so keeping it the single entry point is what makes "the language never changes on its own" true
 * rather than aspirational.
 */
@Composable
fun LanguagePickerDialog(
    current: AppLocale,
    onSelect: (AppLocale) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = { Text(stringResource(Res.string.language)) },
        confirmButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(Res.string.language_close)) }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                AppLocale.entries.forEach { locale ->
                    LanguageRow(
                        locale = locale,
                        selected = locale == current,
                        onClick = { onSelect(locale) },
                    )
                }
            }
        },
    )
}

@Composable
private fun LanguageRow(locale: AppLocale, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            // The whole row is the target, not just the radio button: a 20dp control is below the
            // 48dp minimum and this dialog is the one screen a user who cannot read the current
            // language has to operate.
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(selected = selected, onClick = null)
        // Screen readers announce a flag emoji as its country name, in the reader's own language —
        // noise in front of the only label that matters here.
        Text(locale.flag, modifier = Modifier.clearAndSetSemantics {})
        Text(locale.displayName, style = MaterialTheme.typography.bodyLarge)
    }
}
