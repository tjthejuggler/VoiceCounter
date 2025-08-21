package com.example.voicecounter

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
                    VoiceCounterApp(viewModel = viewModel(factory = MainViewModelFactory(application)))
                }
            }
        }
    }
}

@Composable
fun VoiceCounterApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "counter") {
        composable("counter") {
            CounterScreen(
                viewModel = viewModel,
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("settings") {
            SettingsScreen(
                onNavigateUp = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun CounterScreen(viewModel: MainViewModel, onNavigateToSettings: () -> Unit) {
    val context = LocalContext.current
    val voiceRecognition = remember { VoiceRecognition(context) }
    val recognitionResult by voiceRecognition.recognitionResult.collectAsState()
    val words by viewModel.words.collectAsState()
    val wordRecognized by viewModel.wordRecognized.collectAsState()
    var isListening by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf<Word?>(null) }
    var showAddWordDialog by remember { mutableStateOf(false) }
    var flashColor by remember { mutableStateOf(Color.Transparent) }

    LaunchedEffect(recognitionResult) {
        viewModel.incrementWordCount(recognitionResult)
    }

    LaunchedEffect(wordRecognized) {
        wordRecognized?.let {
            flashColor = Color(android.graphics.Color.parseColor(it.backgroundColor))
            kotlinx.coroutines.delay(500)
            flashColor = Color.Transparent
            viewModel.onRecognitionComplete()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceRecognition.destroy()
        }
    }

    val animatedColor by animateColorAsState(targetValue = flashColor, animationSpec = tween(500))
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
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
                    Spacer(modifier = Modifier.height(8.dp))
                    FloatingActionButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(animatedColor)
        )
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
            onAdd = { name, words -> viewModel.addWord(name, words) }
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
                style = MaterialTheme.typography.displayLarge,
                textAlign = TextAlign.Center,
                color = Color(android.graphics.Color.parseColor(word.textColor)),
                modifier = Modifier.align(Alignment.Center)
            )
            Text(
                text = word.name,
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
fun AddWordDialog(onDismiss: () -> Unit, onAdd: (String, List<String>) -> Unit) {
    var name by remember { mutableStateOf("") }
    var words by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = words,
                    onValueChange = { words = it },
                    label = { Text("Words (comma-separated)") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    onAdd(name, words.split(",").map { it.trim() })
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
    var name by remember { mutableStateOf(word.name) }
    var words by remember { mutableStateOf(word.words.joinToString(", ")) }
    var count by remember { mutableStateOf(word.count.toString()) }
    var backgroundColor by remember { mutableStateOf(word.backgroundColor) }
    var textColor by remember { mutableStateOf(word.textColor) }
    var confidenceThreshold by remember { mutableStateOf(word.confidenceThreshold) }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = words,
                    onValueChange = { words = it },
                    label = { Text("Words (comma-separated)") }
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
                Text("Confidence Threshold: ${String.format("%.2f", confidenceThreshold)}")
                Slider(
                    value = confidenceThreshold,
                    onValueChange = { confidenceThreshold = it },
                    valueRange = 0f..1f
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Button(onClick = {
                        onSave(
                            word.copy(
                                name = name,
                                words = words.split(",").map { it.trim() },
                                count = count.toIntOrNull() ?: 0,
                                backgroundColor = backgroundColor,
                                textColor = textColor,
                                confidenceThreshold = confidenceThreshold
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
        VoiceCounterApp(viewModel = viewModel(factory = MainViewModelFactory(LocalContext.current.applicationContext as android.app.Application)))
    }
}