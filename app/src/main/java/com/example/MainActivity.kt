package com.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ui.CalculatorScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.CalculatorViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: CalculatorViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    // Check if we came from a crash/exception redirect
    val crashExtra = intent.getStringExtra("crash_extra")
    if (crashExtra != null) {
      renderErrorScreen("Startup Exception Context", crashExtra)
      return
    }

    try {
      setContent {
        MyApplicationTheme {
          CalculatorScreen(viewModel)
        }
      }
    } catch (e: Throwable) {
      android.util.Log.e("CRASH_LOGGER", "Crashed inside main onCreate execution flow", e)
      renderErrorScreen("Main Thread Lifecycle Crash", e.stackTraceToString())
    }
  }

  private fun renderErrorScreen(title: String, stacktrace: String) {
    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.errorContainer
        ) {
          LazyColumn(
            modifier = Modifier
              .fillMaxSize()
              .padding(24.dp)
              .systemBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            item {
              Text(
                text = "⚠️ Application Recovery Console",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
              )
              Spacer(modifier = Modifier.padding(vertical = 4.dp))
              Text(
                text = "The app encountered an exception on SDK 35/36 execution environment:",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onErrorContainer
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = stacktrace,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier
                  .background(MaterialTheme.colorScheme.surfaceVariant)
                  .padding(12.dp)
              )
            }
          }
        }
      }
    }
  }
}

