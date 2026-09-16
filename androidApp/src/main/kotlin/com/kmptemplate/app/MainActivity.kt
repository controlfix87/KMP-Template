package com.kmptemplate.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.kmptemplate.core.designsystem.KMPTemplateTheme
import com.kmptemplate.feature.example.navigation.ExampleRoute
import com.kmptemplate.feature.example.navigation.exampleNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KMPTemplateTheme {
                KMPTemplateApp()
            }
        }
    }
}

@Composable
private fun KMPTemplateApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = ExampleRoute.List) {
        exampleNavGraph(onNavigateToDetail = { /* wire a detail screen here when one exists */ })
    }
}
