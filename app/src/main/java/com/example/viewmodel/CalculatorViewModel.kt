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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    private val _memoryValue = MutableStateFlow(0.0)
    val memoryValue: StateFlow<Double> = _memoryValue

    private val _previousFormula = MutableStateFlow<String?>(null)
    val previousFormula: StateFlow<String?> = _previousFormula

    private val _isFunctionMode = MutableStateFlow(false)
    val isFunctionMode: StateFlow<Boolean> = _isFunctionMode

    private val _functionFormula = MutableStateFlow("")
    val functionFormula: StateFlow<String> = _functionFormula

    private val _functionPoint = MutableStateFlow("")
    val functionPoint: StateFlow<String> = _functionPoint

    private val _functionPointB = MutableStateFlow("")
    val functionPointB: StateFlow<String> = _functionPointB

    private val _functionResult = MutableStateFlow("")
    val functionResult: StateFlow<String> = _functionResult

    private val _focusedField = MutableStateFlow(0)
    val focusedField: StateFlow<Int> = _focusedField

    private var liveEvalJob: Job? = null

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
        if (_isFunctionMode.value) {
            handleFunctionKeyPress(key)
            return
        }

        val current = _formula.value

        when (key) {
            "AC" -> {
                _formula.value = ""
                _calculationResult.value = ""
                liveEvalJob?.cancel()
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
            "nPr", "nCr" -> {
                if (current.isNotEmpty() && !isOperator(current.last()) && !current.endsWith("nPr") && !current.endsWith("nCr")) {
                    _formula.value = "$current$key"
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
        liveEvalJob?.cancel()
        liveEvalJob = viewModelScope.launch {
            delay(120)
            val expr = _formula.value.trim()
            if (expr.isEmpty()) {
                _calculationResult.value = ""
                return@launch
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
    }

    private fun evaluateAndSave() {
        liveEvalJob?.cancel()
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

            _previousFormula.value = expr

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

    fun deleteHistoryItem(item: HistoryItem) {
        val updated = _history.value.filter { it.id != item.id }
        _history.value = updated
        saveHistoryToPrefs()
    }

    fun onUndo() {
        val prev = _previousFormula.value
        if (prev != null) {
            _formula.value = prev
            _previousFormula.value = null
            triggerLiveEvaluation()
        }
    }

    fun onMemoryAdd() {
        val currentVal = evaluateCurrent()
        if (currentVal != null) {
            _memoryValue.value += currentVal
        }
    }

    fun onMemorySubtract() {
        val currentVal = evaluateCurrent()
        if (currentVal != null) {
            _memoryValue.value -= currentVal
        }
    }

    fun onMemoryRecall() {
        val memStr = formatRawResult(_memoryValue.value)
        if (memStr != "Error" && !memStr.endsWith("Infinity")) {
            val current = _formula.value
            if (current.isEmpty() || isOperator(current.last())) {
                 _formula.value = current + memStr
            } else {
                 _formula.value = current + "×" + memStr
            }
            triggerLiveEvaluation()
        }
    }

    fun onMemoryClear() {
        _memoryValue.value = 0.0
    }

    private fun evaluateCurrent(): Double? {
        if (_formula.value.isEmpty()) return 0.0
        return try {
            CalculatorEvaluator.evaluate(_formula.value, _isDegrees.value)
        } catch (e: Throwable) {
            null
        }
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
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
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

    fun toggleFunctionMode() {
        _isFunctionMode.value = !_isFunctionMode.value
    }

    fun setFocusedField(field: Int) {
        _focusedField.value = field
    }

    private fun handleFunctionKeyPress(key: String) {
        val currentFlow = when (_focusedField.value) {
            0 -> _functionFormula
            1 -> _functionPoint
            else -> _functionPointB
        }
        val current = currentFlow.value

        when (key) {
            "AC" -> {
                currentFlow.value = ""
                _functionResult.value = ""
            }
            "DEL" -> {
                if (current.isNotEmpty()) {
                    currentFlow.value = current.dropLast(1)
                }
                _functionResult.value = ""
            }
            "=" -> {
                // Ignore in function mode for normal keys, actions are triggered by dedicated buttons
            }
            "sin", "cos", "tan", "ln", "log", "sqrt" -> {
                currentFlow.value = "$current$key("
                _functionResult.value = ""
            }
            else -> {
                currentFlow.value = "$current$key"
                _functionResult.value = ""
            }
        }
    }

    fun evaluateFunctionAtPoint() {
        try {
            val expr = _functionFormula.value
            val pointExpr = _functionPoint.value
            if (expr.isEmpty() || pointExpr.isEmpty()) return
            val xVal = CalculatorEvaluator.evaluate(pointExpr, _isDegrees.value)
            val result = CalculatorEvaluator.evaluate(expr, _isDegrees.value, xVal)
            _functionResult.value = "f(" + formatResult(xVal) + ") = " + formatResult(result)
        } catch (e: Exception) {
            _functionResult.value = "Error"
        }
    }

    fun evaluateDerivativeAtPoint() {
        try {
            val expr = _functionFormula.value
            val pointExpr = _functionPoint.value
            if (expr.isEmpty() || pointExpr.isEmpty()) return
            val xVal = CalculatorEvaluator.evaluate(pointExpr, _isDegrees.value)
            val h = 1e-5 // small enough for accuracy, large enough to avoid heavy floating point cancellation
            val fPlus = CalculatorEvaluator.evaluate(expr, _isDegrees.value, xVal + h)
            val fMinus = CalculatorEvaluator.evaluate(expr, _isDegrees.value, xVal - h)
            val result = (fPlus - fMinus) / (2 * h)
            _functionResult.value = "f'(" + formatResult(xVal) + ") ≈ " + formatResult(result)
        } catch (e: Exception) {
            _functionResult.value = "Error"
        }
    }

    fun evaluateDefiniteIntegral() {
        try {
            val expr = _functionFormula.value
            val aStr = _functionPoint.value
            val bStr = _functionPointB.value
            if (expr.isEmpty() || aStr.isEmpty() || bStr.isEmpty()) return
            val aEval = CalculatorEvaluator.evaluate(aStr, _isDegrees.value)
            val bEval = CalculatorEvaluator.evaluate(bStr, _isDegrees.value)
            
            val a = kotlin.math.min(aEval, bEval)
            val b = kotlin.math.max(aEval, bEval)
            val sign = if (aEval > bEval) -1.0 else 1.0
            
            if (a == b) {
                _functionResult.value = "∫f(x)dx = 0"
                return
            }

            val n = 1000 // Fixed subintervals for Simpson's rule (must be even)
            val h = (b - a) / n
            var sum = CalculatorEvaluator.evaluate(expr, _isDegrees.value, a) +
                      CalculatorEvaluator.evaluate(expr, _isDegrees.value, b)
            
            for (i in 1 until n step 2) {
                sum += 4 * CalculatorEvaluator.evaluate(expr, _isDegrees.value, a + i * h)
            }
            for (i in 2 until n - 1 step 2) {
                sum += 2 * CalculatorEvaluator.evaluate(expr, _isDegrees.value, a + i * h)
            }
            val result = (sum * h / 3) * sign
            _functionResult.value = "∫f(x)dx ≈ " + formatResult(result)
        } catch (e: Exception) {
            _functionResult.value = "Error"
        }
    }
}
