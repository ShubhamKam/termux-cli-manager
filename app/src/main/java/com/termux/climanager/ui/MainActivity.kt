package com.termux.climanager.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.termux.climanager.ui.screens.CLIManagerScreen
import com.termux.climanager.ui.screens.FileSystemScreen
import com.termux.climanager.ui.screens.SessionsScreen
import com.termux.climanager.ui.theme.TermuxCLIManagerTheme
import com.termux.climanager.ui.viewmodel.CLIManagerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            TermuxCLIManagerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val viewModel: CLIManagerViewModel = hiltViewModel()
    
    NavHost(
        navController = navController,
        startDestination = "cli_manager"
    ) {
        composable("cli_manager") {
            CLIManagerScreen(
                viewModel = viewModel,
                onNavigateToSessions = { navController.navigate("sessions") },
                onNavigateToFileSystem = { navController.navigate("filesystem") }
            )
        }
        
        composable("sessions") {
            SessionsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable("filesystem") {
            FileSystemScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}