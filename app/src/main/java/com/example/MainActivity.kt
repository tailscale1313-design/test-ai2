package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShortText
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShortText
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.AIViewModel
import com.example.ui.ActiveScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PlaygroundScreen
import com.example.ui.screens.SentimentScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SummarizerScreen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: AIViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(viewModel: AIViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentScreen) {
                            ActiveScreen.HOME -> "Local AI Studio"
                            ActiveScreen.SUMMARIZER -> "Text Summarizer"
                            ActiveScreen.SENTIMENT -> "Sentiment Analysis"
                            ActiveScreen.PROMPT_PLAYGROUND -> "Mistral AI Prompt"
                            ActiveScreen.HISTORY -> "History & Logs"
                            ActiveScreen.SERVER_SETTINGS -> "Backend Config"
                        },
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("main_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = currentScreen == ActiveScreen.HOME,
                    onClick = { viewModel.setScreen(ActiveScreen.HOME) },
                    icon = {
                        Icon(
                            if (currentScreen == ActiveScreen.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = "Home"
                        )
                    },
                    label = { Text("Home") },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = CyberCyan.copy(alpha = 0.2f)),
                    modifier = Modifier.testTag("nav_home")
                )
                NavigationBarItem(
                    selected = currentScreen == ActiveScreen.SUMMARIZER,
                    onClick = { viewModel.setScreen(ActiveScreen.SUMMARIZER) },
                    icon = {
                        Icon(
                            if (currentScreen == ActiveScreen.SUMMARIZER) Icons.Filled.ShortText else Icons.Outlined.ShortText,
                            contentDescription = "Summarizer"
                        )
                    },
                    label = { Text("Summarize") },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = CyberCyan.copy(alpha = 0.2f)),
                    modifier = Modifier.testTag("nav_summarizer")
                )
                NavigationBarItem(
                    selected = currentScreen == ActiveScreen.SENTIMENT,
                    onClick = { viewModel.setScreen(ActiveScreen.SENTIMENT) },
                    icon = {
                        Icon(
                            if (currentScreen == ActiveScreen.SENTIMENT) Icons.Filled.Psychology else Icons.Outlined.Psychology,
                            contentDescription = "Sentiment"
                        )
                    },
                    label = { Text("Sentiment") },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = CyberCyan.copy(alpha = 0.2f)),
                    modifier = Modifier.testTag("nav_sentiment")
                )
                NavigationBarItem(
                    selected = currentScreen == ActiveScreen.PROMPT_PLAYGROUND,
                    onClick = { viewModel.setScreen(ActiveScreen.PROMPT_PLAYGROUND) },
                    icon = {
                        Icon(
                            if (currentScreen == ActiveScreen.PROMPT_PLAYGROUND) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                            contentDescription = "Playground"
                        )
                    },
                    label = { Text("Prompt") },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = CyberCyan.copy(alpha = 0.2f)),
                    modifier = Modifier.testTag("nav_playground")
                )
                NavigationBarItem(
                    selected = currentScreen == ActiveScreen.HISTORY,
                    onClick = { viewModel.setScreen(ActiveScreen.HISTORY) },
                    icon = {
                        Icon(
                            if (currentScreen == ActiveScreen.HISTORY) Icons.Filled.History else Icons.Outlined.History,
                            contentDescription = "History"
                        )
                    },
                    label = { Text("History") },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = CyberCyan.copy(alpha = 0.2f)),
                    modifier = Modifier.testTag("nav_history")
                )
                NavigationBarItem(
                    selected = currentScreen == ActiveScreen.SERVER_SETTINGS,
                    onClick = { viewModel.setScreen(ActiveScreen.SERVER_SETTINGS) },
                    icon = {
                        Icon(
                            if (currentScreen == ActiveScreen.SERVER_SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                            contentDescription = "Settings"
                        )
                    },
                    label = { Text("Server") },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = CyberCyan.copy(alpha = 0.2f)),
                    modifier = Modifier.testTag("nav_settings")
                )
            }
        }
    ) { innerPadding ->
        when (currentScreen) {
            ActiveScreen.HOME -> HomeScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            ActiveScreen.SUMMARIZER -> SummarizerScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            ActiveScreen.SENTIMENT -> SentimentScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            ActiveScreen.PROMPT_PLAYGROUND -> PlaygroundScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            ActiveScreen.HISTORY -> HistoryScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            ActiveScreen.SERVER_SETTINGS -> SettingsScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
