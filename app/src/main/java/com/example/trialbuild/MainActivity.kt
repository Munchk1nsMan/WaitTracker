package com.example.trialbuild

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.trialbuild.ui.GraphScreen
import com.example.trialbuild.ui.InputScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                val viewModel: WeightViewModel = viewModel()

                NavHost(navController = navController, startDestination = "input") {
                    composable("input") {
                        InputScreen(
                            viewModel = viewModel,
                            onNavigateToGraph = { navController.navigate("graph") }
                        )
                    }
                    composable("graph") {
                        GraphScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}