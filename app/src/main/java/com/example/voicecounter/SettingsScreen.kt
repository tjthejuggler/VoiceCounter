package com.example.voicecounter

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateUp: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(LocalContext.current.applicationContext as android.app.Application))
) {
    val speakWords by viewModel.speakWords.collectAsState()
    val extraPartialResults by viewModel.extraPartialResults.collectAsState()
    val extraSpeechInputCompleteSilenceLengthMillis by viewModel.extraSpeechInputCompleteSilenceLengthMillis.collectAsState()
    val extraSpeechInputPossiblyCompleteSilenceLengthMillis by viewModel.extraSpeechInputPossiblyCompleteSilenceLengthMillis.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.navigate_up)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Speak recognized words", modifier = Modifier.weight(1f))
                Switch(
                    checked = speakWords,
                    onCheckedChange = { viewModel.setSpeakWords(it) }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Enable partial results", modifier = Modifier.weight(1f))
                Switch(
                    checked = extraPartialResults,
                    onCheckedChange = { viewModel.setExtraPartialResults(it) }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column {
                Text(text = "Complete silence length: $extraSpeechInputCompleteSilenceLengthMillis ms")
                Slider(
                    value = extraSpeechInputCompleteSilenceLengthMillis.toFloat(),
                    onValueChange = { viewModel.setExtraSpeechInputCompleteSilenceLengthMillis(it.toInt()) },
                    valueRange = 0f..5000f,
                    steps = 50
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column {
                Text(text = "Possibly complete silence length: $extraSpeechInputPossiblyCompleteSilenceLengthMillis ms")
                Slider(
                    value = extraSpeechInputPossiblyCompleteSilenceLengthMillis.toFloat(),
                    onValueChange = { viewModel.setExtraSpeechInputPossiblyCompleteSilenceLengthMillis(it.toInt()) },
                    valueRange = 0f..5000f,
                    steps = 50
                )
            }
        }
    }
}