package io.github.janmalch.pocpic.ui.sources

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.janmalch.pocpic.R
import io.github.janmalch.pocpic.ui.theme.Typography

@Composable
fun LabelInput(value: String, onChange: (String) -> Unit) {
    val labelIsError = remember(value) { value.isBlank() }

    OutlinedTextField(
        value = value,
        onValueChange = { onChange(it) },
        label = { Text(text = stringResource(id = R.string.required_label_input)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        isError = labelIsError
    )
}

@Composable
fun WeightInput(value: Int, onChange: (Int) -> Unit) {
    OutlinedTextField(
        value = value.toString(10),
        onValueChange = { onChange(it.trim().toIntOrNull(10)?.takeIf { n -> n > 0 } ?: value) },
        label = { Text(text = stringResource(id = R.string.required_weight_input)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
    Spacer(Modifier.height(4.dp))
    Text(stringResource(R.string.weight_explanation), style = Typography.labelSmall)
}

