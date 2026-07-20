package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.CalculatorEvaluator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import kotlin.math.abs

data class HistoryItem(
    val id: Long,
    val formula: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
)

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPrefs: SharedPreferences = application.getSharedPreferences("calculator_prefs", Context.MODE_PRIVATE)

    private val _formula = MutableStateFlow("")
    val formula: StateFlow<String> = _formula

    private val _isDegrees = MutableStateFlow(true)
    val isDegrees: StateFlow<Boolean> = _isDegrees

    private val _isAdvancedMode = MutableStateFlow(false)
    val isAdvancedMode: StateFlow<Boolean> = _isAdvancedMode

    private val _calculationResult = MutableStateFlow("")
    val calculationResult: StateFlow<String> = _calculationResult

    private val _history = MutableStateFlow<List<HistoryItem>>(emptyList())
    val history: StateFlow<List<HistoryItem>> = _history

    init {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            loadHistory()
        }
    }

    fun toggleDegrees() {
        _isDegrees.value = !_isDegrees.value
        triggerLiveEvaluation()
    }

    fun toggleAdvancedMode() {
        _isAdvancedMode.value = !_isAdvancedMode.value
    }

    fun onKeyPress(key: String) {
        val current = _formula.value

        when (key) {
            "AC" -> {
                _formula.value = ""
                _calculationResult.value = ""
            }
            "DEL" -> {
                if (current.isNotEmpty()) {
                    // Smart delete for multi-char functions or tokens
                    val functionsToCheck = listOf("sin(", "cos(", "tan(", "ln(", "log(", "sqrt(", "asin(", "acos(", "atan(")
                    var deleted = false
                    for (func in functionsToCheck) {
                        if (current.endsWith(func)) {
                            _formula.value = current.substring(0, current.length - func.length)
                            deleted = true
                            break
                        }
                    }
                    if (!deleted) {
                        _formula.value = current.substring(0, current.length - 1)
                    }
                }
                triggerLiveEvaluation()
            }
            "=" -> {
                evaluateAndSave()
            }
            "%" -> {
                if (current.isNotEmpty() && !isOperator(current.last()) && current.last() != '%') {
                    _formula.value = "$current%"
                }
                triggerLiveEvaluation()
            }
            "x!" -> {
                if (current.isNotEmpty() && !isOperator(current.last()) && current.last() != '!') {
                    _formula.value = "$current!"
                }
                triggerLiveEvaluation()
            }
            "sin", "cos", "tan", "ln", "log", "sqrt" -> {
                _formula.value = "$current$key("
                triggerLiveEvaluation()
            }
            "pi" -> {
                _formula.value = "${current}π"
                triggerLiveEvaluation()
            }
            "EE" -> {
                _formula.value = "${current}e"
                triggerLiveEvaluation()
            }
            else -> {
                // Note: The UI layer (CalculatorScreen.kt) sends unicode symbols (×, ÷, −) directly,
                // so ASCII remap here is technically dead code, but kept defensively.
                val formattedKey = when (key) {
                    "*" -> "×"
                    "/" -> "÷"
                    "-" -> "−"
                    else -> key
                }
                _formula.value = "$current$formattedKey"
                triggerLiveEvaluation()
            }
        }
    }

    private fun isOperator(c: Char): Boolean {
        return c == '+' || c == '−' || c == '×' || c == '÷' || c == '^' || c == '('
    }

    private fun triggerLiveEvaluation() {
        val expr = _formula.value.trim()
        if (expr.isEmpty()) {
            _calculationResult.value = ""
            return
        }

        // Only evaluate if it contains digits, constants or closing parenthesis (ends beautifully)
        try {
            val result = CalculatorEvaluator.evaluate(expr, _isDegrees.value)
            if (!result.isNaN() && !result.isInfinite()) {
                _calculationResult.value = "= " + formatResult(result)
            } else {
                _calculationResult.value = ""
            }
        } catch (e: Throwable) {
            // Live evaluation shouldn't pollute screen with error during typing
            _calculationResult.value = ""
        }
    }

    private fun evaluateAndSave() {
        val expr = _formula.value.trim()
        if (expr.isEmpty()) return

        try {
            val result = CalculatorEvaluator.evaluate(expr, _isDegrees.value)
            val rawFormatted = formatRawResult(result)
            val displayFormatted = formatResult(result)

            if (rawFormatted == "Error" || rawFormatted.endsWith("Infinity")) {
                _calculationResult.value = "Error"
                return
            }

            // Save to internal history
            val item = HistoryItem(
                id = System.nanoTime(),
                formula = expr,
                result = displayFormatted
            )
            val updated = listOf(item) + _history.value
            _history.value = updated.take(50) // Keep last 50 items
            saveHistoryToPrefs()

            // Reset current input to output for rolling calculation
            _formula.value = rawFormatted
            _calculationResult.value = ""
        } catch (e: Throwable) {
            _calculationResult.value = "Error"
        }
    }

    fun clearHistory() {
        _history.value = emptyList()
        saveHistoryToPrefs()
    }

    fun loadHistoryItem(item: HistoryItem) {
        _formula.value = item.formula
        _calculationResult.value = "= " + item.result
    }

    private fun formatRawResult(value: Double): String {
        if (value.isNaN()) return "Error"
        if (value.isInfinite()) return if (value < 0.0) "-Infinity" else "Infinity"

        // Round tiny numbers close to zero (often precision errors with sine/cosine)
        var roundedValue = value
        if (abs(value) < 1e-14) {
            roundedValue = 0.0
        }

        if (roundedValue == roundedValue.toLong().toDouble()) {
            val longVal = roundedValue.toLong()
            if (abs(longVal) < 100_000_000_000L) {
                return longVal.toString()
            }
        }

        val absValue = abs(roundedValue)
        if (absValue > 0 && (absValue >= 1e11 || absValue < 1e-6)) {
            val df = DecimalFormat("0.######E0")
            return df.format(roundedValue)
        }

        val df = DecimalFormat("#.##########")
        return df.format(roundedValue)
    }

    private fun formatResult(value: Double): String {
        val raw = formatRawResult(value)
        if (raw == "Error" || raw.endsWith("Infinity")) return raw
        if (raw.contains("E")) return convertExponentToSuperscript(raw)
        return raw
    }

    private fun convertExponentToSuperscript(input: String): String {
        val superscripts = mapOf(
            '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴',
            '5' to '⁵', '6' to '⁶', '7' to '⁷', '8' to '⁸', '9' to '⁹',
            '-' to '⁻', '+' to '⁺'
        )
        val parts = input.split("E")
        if (parts.size != 2) return input
        val base = parts[0]
        val exp = parts[1]
        val sb = java.lang.StringBuilder()
        for (char in exp) {
            sb.append(superscripts[char] ?: char)
        }
        return "$base × 10$sb"
    }

    private fun saveHistoryToPrefs() {
        viewModelScope.launch {
            try {
                // simple record separation using custom encoding
                // Item: id|||formula|||result
                // Delimiter: \n\n
                val serialized = _history.value.joinToString("\n\n") {
                    "${it.id}|||${it.formula}|||${it.result}|||${it.timestamp}"
                }
                sharedPrefs.edit().putString("calc_history", serialized).apply()
            } catch (e: Throwable) {
                Log.e("CalculatorVM", "Failed to save history", e)
            }
        }
    }

    private fun loadHistory() {
        try {
            val serialized = sharedPrefs.getString("calc_history", "") ?: ""
            if (serialized.isNotEmpty()) {
                var fallbackId = System.nanoTime()
                val list = serialized.split("\n\n").mapNotNull { block ->
                    val parts = block.split("|||")
                    if (parts.size >= 3) {
                        HistoryItem(
                            id = parts[0].toLongOrNull() ?: (fallbackId++),
                            formula = parts[1],
                            result = parts[2],
                            timestamp = if (parts.size >= 4) parts[3].toLongOrNull() ?: System.currentTimeMillis() else System.currentTimeMillis()
                        )
                    } else null
                }
                _history.value = list
            }
        } catch (e: Throwable) {
            Log.e("CalculatorVM", "Failed to load history", e)
        }
    }
}
