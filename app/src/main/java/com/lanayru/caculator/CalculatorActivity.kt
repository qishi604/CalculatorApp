package com.lanayru.caculator

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.lanayru.caculator.ui.theme.CaculatorTheme

@OptIn(ExperimentalMaterial3Api::class)
class CalculatorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CaculatorTheme {
                // A surface container using the 'background' color from the theme
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//
//
//                }
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    Log.d("Calculator", "PaddingValues: $it")
                    Calculator()
                }
            }
        }
    }
}
