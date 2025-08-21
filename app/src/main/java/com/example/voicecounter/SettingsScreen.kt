package com.example.voicecounter

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    val extraMaxResults by viewModel.extraMaxResults.collectAsState()
    var showInfoDialog by remember { mutableStateOf<String?>(null) }

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
            SettingSwitch(
                title = "Speak recognized words",
                checked = speakWords,
                onCheckedChange = { viewModel.setSpeakWords(it) },
                onInfoClick = { showInfoDialog = "When enabled, the app will speak the name of the counter when a word is recognized." }
            )
            SettingSwitch(
                title = "Enable partial results",
                checked = extraPartialResults,
                onCheckedChange = { viewModel.setExtraPartialResults(it) },
                onInfoClick = { showInfoDialog = "When enabled, the app will receive and process recognition results as you speak. This can improve reliability but may also lead to more false positives." }
            )
            SettingSlider(
                title = "Complete silence length",
                value = extraSpeechInputCompleteSilenceLengthMillis,
                onValueChange = { viewModel.setExtraSpeechInputCompleteSilenceLengthMillis(it) },
                range = 0..5000,
                steps = 50,
                onInfoClick = { showInfoDialog = "The amount of time in milliseconds that the recognizer will wait for speech to end before considering it complete." }
            )
            SettingSlider(
                title = "Possibly complete silence length",
                value = extraSpeechInputPossiblyCompleteSilenceLengthMillis,
                onValueChange = { viewModel.setExtraSpeechInputPossiblyCompleteSilenceLengthMillis(it) },
                range = 0..5000,
                steps = 50,
                onInfoClick = { showInfoDialog = "The amount of time in milliseconds that the recognizer will wait after a pause in speech before considering it possibly complete." }
            )
            SettingSlider(
                title = "Max results",
                value = extraMaxResults,
                onValueChange = { viewModel.setExtraMaxResults(it) },
                range = 1..10,
                steps = 9,
                onInfoClick = { showInfoDialog = "The maximum number of recognition results to return. A higher number may improve reliability but will also increase processing." }
            )
        }
    }

    if (showInfoDialog != null) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = null },
            title = { Text("Info") },
            text = { Text(showInfoDialog!!) },
            confirmButton = {
                Button(onClick = { showInfoDialog = null }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun SettingSwitch(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, onInfoClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = title, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
        IconButton(onClick = onInfoClick) {
            Icon(Icons.Filled.Info, contentDescription = "Info")
        }
    }
}

@Composable
fun SettingSlider(title: String, value: Int, onValueChange: (Int) -> Unit, range: IntRange, steps: Int, onInfoClick: () -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "$title: $value", modifier = Modifier.weight(1f))
            IconButton(onClick = onInfoClick) {
                Icon(Icons.Filled.Info, contentDescription = "Info")
            }
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = steps
        )
    }
}