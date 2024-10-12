package io.github.janmalch.pocpic.ui

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingScreen(
    onDirectorySelected: (Uri) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pickDirectory = rememberDirectoryPicker { uri ->
        if (uri != null) {
            onDirectorySelected(uri)
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(48.dp),
        ) {

            Text("Welcome!", style = MaterialTheme.typography.displayMedium)
            Spacer(Modifier.height(32.dp))

            Text("PocPic has just one feature:", textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Text(
                "A home screen widget that displays random pictures from the directory of your choice.",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "So get started by selecting a directory from your phone!",
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = { pickDirectory() },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Text("Select Directory", fontSize = 20.sp)
            }
        }
    }
}