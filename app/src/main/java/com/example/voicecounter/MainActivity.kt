package com.example.voicecounter

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.voicecounter.ui.theme.VoiceCounterTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your app.
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        setContent {
            VoiceCounterTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    CounterScreen(viewModel = viewModel(factory = MainViewModelFactory(application)))
                }
            }
        }
    }
}

@Composable
fun CounterScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val voiceRecognition = remember { VoiceRecognition(context) }
    val recognizedText by voiceRecognition.recognizedText.collectAsState()
    val words by viewModel.words.collectAsState()
    var isListening by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf<Word?>(null) }
    var showAddWordDialog by remember { mutableStateOf(false) }

    LaunchedEffect(recognizedText) {
        if (recognizedText.isNotBlank()) {
            viewModel.incrementWordCount(recognizedText)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceRecognition.destroy()
        }
    }

    Scaffold(
        floatingActionButton = {
            Column {
                FloatingActionButton(onClick = { showAddWordDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Word")
                }
                Spacer(modifier = Modifier.height(8.dp))
                FloatingActionButton(onClick = { viewModel.resetAllCounts() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                words.forEach { word ->
                    Box(modifier = Modifier.weight(1f)) {
                        WordCard(word = word, onSettingsClick = { showSettingsDialog = word })
                    }
                }
            }
            Button(
                onClick = {
                    isListening = !isListening
                    if (isListening) {
                        voiceRecognition.startListening()
                    } else {
                        voiceRecognition.stopListening()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(if (isListening) "Stop Listening" else "Start Listening")
            }
        }
    }

    if (showSettingsDialog != null) {
        SettingsDialog(
            word = showSettingsDialog!!,
            onDismiss = { showSettingsDialog = null },
            onSave = { viewModel.updateWord(it) },
            onDelete = { viewModel.deleteWord(it) }
        )
    }

    if (showAddWordDialog) {
        AddWordDialog(
            onDismiss = { showAddWordDialog = false },
            onAdd = { viewModel.addWord(it) }
        )
    }
}

@Composable
fun WordCard(word: Word, onSettingsClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(android.graphics.Color.parseColor(word.backgroundColor)))
        ) {
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
            Text(
                text = word.count.toString(),
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                color = Color(android.graphics.Color.parseColor(word.textColor)),
                modifier = Modifier.align(Alignment.Center)
            )
            Text(
                text = word.text,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Color(android.graphics.Color.parseColor(word.textColor)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
            )
        }
    }
}

@Composable
fun AddWordDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Word") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    onAdd(text)
                    onDismiss()
                }) {
                    Text("Add")
                }
            }
        }
    }
}

@Composable
fun SettingsDialog(word: Word, onDismiss: () -> Unit, onSave: (Word) -> Unit, onDelete: (Word) -> Unit) {
    var text by remember { mutableStateOf(word.text) }
    var count by remember { mutableStateOf(word.count.toString()) }
    var backgroundColor by remember { mutableStateOf(word.backgroundColor) }
    var textColor by remember { mutableStateOf(word.textColor) }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Word") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = count,
                    onValueChange = { count = it },
                    label = { Text("Count") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                ColorPicker(
                    title = "Background Color",
                    color = backgroundColor,
                    onColorChange = { backgroundColor = it }
                )
                Spacer(modifier = Modifier.height(8.dp))
                ColorPicker(
                    title = "Text Color",
                    color = textColor,
                    onColorChange = { textColor = it }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Button(onClick = {
                        onSave(
                            word.copy(
                                text = text,
                                count = count.toIntOrNull() ?: 0,
                                backgroundColor = backgroundColor,
                                textColor = textColor
                            )
                        )
                        onDismiss()
                    }) {
                        Text("Save")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = {
                        onDelete(word)
                        onDismiss()
                    }) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}

@Composable
fun ColorPicker(title: String, color: String, onColorChange: (String) -> Unit) {
    val colors = listOf("#FFFFFF", "#FF0000", "#00FF00", "#0000FF", "#FFFF00", "#000000")
    Column {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        Row {
            colors.forEach { colorHex ->
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .padding(4.dp)
                        .background(Color(android.graphics.Color.parseColor(colorHex)))
                        .clickable { onColorChange(colorHex) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    VoiceCounterTheme {
        CounterScreen(viewModel = viewModel(factory = MainViewModelFactory(LocalContext.current.applicationContext as android.app.Application)))
    }
}