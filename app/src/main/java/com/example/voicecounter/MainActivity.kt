package com.example.voicecounter

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
                    CounterScreen()
                }
            }
        }
    }
}

@Composable
fun CounterScreen(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val voiceRecognition = remember { VoiceRecognition(context) }
    val recognizedText by voiceRecognition.recognizedText.collectAsState()
    val words by viewModel.words.collectAsState()
    var newWord by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                value = newWord,
                onValueChange = { newWord = it },
                label = { Text("New word") }
            )
            Button(onClick = {
                if (newWord.isNotBlank()) {
                    viewModel.addWord(newWord)
                    newWord = ""
                }
            }) {
                Text("Add")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(words) { word ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = word.text, style = MaterialTheme.typography.headlineMedium)
                    Text(text = word.count.toString(), style = MaterialTheme.typography.headlineMedium)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                isListening = !isListening
                if (isListening) {
                    voiceRecognition.startListening()
                } else {
                    voiceRecognition.stopListening()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isListening) "Stop Listening" else "Start Listening")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    VoiceCounterTheme {
        CounterScreen()
    }
}