package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ui.CalculatorScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.CalculatorViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: CalculatorViewModel by viewModels {
    object : ViewModelProvider.Factory {
      @Suppress("UNCHECKED_CAST")
      override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CalculatorViewModel(applicationContext) as T
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        CalculatorScreen(viewModel)
      }
    }
  }
}

