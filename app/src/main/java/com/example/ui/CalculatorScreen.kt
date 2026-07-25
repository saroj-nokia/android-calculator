package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.wrapContentWidth

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.example.ui.theme.*
import com.example.viewmodel.CalculatorViewModel
import com.example.viewmodel.CalculatorMode
import com.example.viewmodel.MatrixResult
import com.example.viewmodel.HistoryItem

private val standardGrid = listOf(
    listOf(
        Triple("AC", "key_ac", AccentOrange),
        Triple("DEL", "key_del", AccentOrange),
        Triple("%", "key_percent", AccentOrange),
        Triple("÷", "key_divide", AccentOrange)
    ),
    listOf(
        Triple("7", "key_7", TextPrimary),
        Triple("8", "key_8", TextPrimary),
        Triple("9", "key_9", TextPrimary),
        Triple("×", "key_multiply", AccentOrange)
    ),
    listOf(
        Triple("4", "key_4", TextPrimary),
        Triple("5", "key_5", TextPrimary),
        Triple("6", "key_6", TextPrimary),
        Triple("−", "key_minus", AccentOrange)
    ),
    listOf(
        Triple("1", "key_1", TextPrimary),
        Triple("2", "key_2", TextPrimary),
        Triple("3", "key_3", TextPrimary),
        Triple("+", "key_plus", AccentOrange)
    ),
    listOf(
        Triple("0", "key_0", TextPrimary),
        Triple(".", "key_dot", TextPrimary),
        Triple("=", "key_equal", AccentTeal) // Accent custom equals color matches Android 16 premium ui style
    )
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun CalculatorScreen(viewModel: CalculatorViewModel) {
    val formula by viewModel.formula.collectAsStateWithLifecycle()
    val isDegrees by viewModel.isDegrees.collectAsStateWithLifecycle()
    val isAdvancedMode by viewModel.isAdvancedMode.collectAsStateWithLifecycle()
    val calculationResult by viewModel.calculationResult.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val mode by viewModel.mode.collectAsStateWithLifecycle()

    var showHistoryDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 480.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                // 1. Top Bar Options Toolbar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // History Button Toggle
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                showHistoryDialog = !showHistoryDialog
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("history_button"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🕒 History",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val memoryValue by viewModel.memoryValue.collectAsStateWithLifecycle()
                        if (memoryValue != 0.0) {
                            Text(
                                text = "M",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        }

                        // Degrees or Radians Toggle
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    viewModel.toggleDegrees()
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("deg_rad_toggle"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isDegrees) "DEG" else "RAD",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (mode == CalculatorMode.FUNCTION) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    viewModel.setMode(CalculatorMode.FUNCTION)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("function_mode_toggle"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "f(x)",
                                color = if (mode == CalculatorMode.FUNCTION) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Complex Mode toggle
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (mode == CalculatorMode.COMPLEX) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    viewModel.setMode(CalculatorMode.COMPLEX)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("complex_mode_toggle"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ℂ",
                                color = if (mode == CalculatorMode.COMPLEX) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Advanced Scientific Notation / Functions mode trigger
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isAdvancedMode) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    viewModel.toggleAdvancedMode()
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("advanced_mode_toggle"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "SCIENTIFIC",
                                color = if (isAdvancedMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 2. Interactive Calculation Screen Display Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.End
                ) {
                    when (mode) {
                        CalculatorMode.COMPLEX -> {
                            val complexOperand1 by viewModel.complexOperand1.collectAsStateWithLifecycle()
                            val complexOperand2 by viewModel.complexOperand2.collectAsStateWithLifecycle()
                            val complexOperator by viewModel.complexOperator.collectAsStateWithLifecycle()
                            val complexResult by viewModel.complexResult.collectAsStateWithLifecycle()
                            val focusedField by viewModel.focusedField.collectAsStateWithLifecycle()
                            ComplexModeContent(
                                complexOperand1 = complexOperand1,
                                complexOperand2 = complexOperand2,
                                complexOperator = complexOperator,
                                complexResult = complexResult,
                                focusedField = focusedField,
                                onSetFocusedField = { viewModel.setFocusedField(it) }
                            )
                        }
                        CalculatorMode.FUNCTION -> {
                            val functionFormula by viewModel.functionFormula.collectAsStateWithLifecycle()
                            val functionPoint by viewModel.functionPoint.collectAsStateWithLifecycle()
                            val functionPointB by viewModel.functionPointB.collectAsStateWithLifecycle()
                            val functionResult by viewModel.functionResult.collectAsStateWithLifecycle()
                            val focusedField by viewModel.focusedField.collectAsStateWithLifecycle()
                            FunctionModeContent(
                                functionFormula = functionFormula,
                                functionPoint = functionPoint,
                                functionPointB = functionPointB,
                                functionResult = functionResult,
                                focusedField = focusedField,
                                onSetFocusedField = { viewModel.setFocusedField(it) }
                            )
                        }
                        CalculatorMode.NORMAL -> {
                            NormalModeContent(
                                formula = formula,
                                calculationResult = calculationResult
                            )
                        }
                        CalculatorMode.MATRIX -> {
                            val matrixSize by viewModel.matrixSize.collectAsStateWithLifecycle()
                            val matrixA by viewModel.matrixA.collectAsStateWithLifecycle()
                            val matrixB by viewModel.matrixB.collectAsStateWithLifecycle()
                            val focusedMatrix by viewModel.focusedMatrix.collectAsStateWithLifecycle()
                            val focusedRow by viewModel.focusedMatrixRow.collectAsStateWithLifecycle()
                            val focusedCol by viewModel.focusedMatrixCol.collectAsStateWithLifecycle()
                            val matrixResult by viewModel.matrixResult.collectAsStateWithLifecycle()
                            
                            MatrixModeContent(
                                matrixSize = matrixSize,
                                matrixA = matrixA,
                                matrixB = matrixB,
                                focusedMatrix = focusedMatrix,
                                focusedRow = focusedRow,
                                focusedCol = focusedCol,
                                matrixResult = matrixResult,
                                onSetSize = { viewModel.setMatrixSize(it) },
                                onFocusCell = { m, r, c -> viewModel.setFocusedMatrixCell(m, r, c) }
                            )
                        }
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // 3. Dynamic Interactive Mechanical Keypad Layout (Styled Bento Grid container)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Scientific Operations Drawer Panel (with beautiful reveal animation)
                    AnimatedVisibility(
                        visible = isAdvancedMode,
                        enter = expandVertically(animationSpec = tween(250)) + fadeIn(),
                        exit = shrinkVertically(animationSpec = tween(220)) + fadeOut()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            // Row M Memory & Combinatorics
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CalculatorKeyButton("MC", "key_mc", modifier = Modifier.weight(1f), isSci = true) { viewModel.onMemoryClear() }
                                CalculatorKeyButton("MR", "key_mr", modifier = Modifier.weight(1f), isSci = true) { viewModel.onMemoryRecall() }
                                CalculatorKeyButton("M+", "key_m_plus", modifier = Modifier.weight(1f), isSci = true) { viewModel.onMemoryAdd() }
                                CalculatorKeyButton("M-", "key_m_minus", modifier = Modifier.weight(1f), isSci = true) { viewModel.onMemorySubtract() }
                            }
                            // Row 0 Advanced Functions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val previousFormula by viewModel.previousFormula.collectAsStateWithLifecycle()
                                val undoEnabled = previousFormula != null
                                val undoLabel = "UNDO"
                                
                                CalculatorKeyButton("nPr", "key_npr", modifier = Modifier.weight(1f), isSci = true) { viewModel.onKeyPress("nPr") }
                                CalculatorKeyButton("nCr", "key_ncr", modifier = Modifier.weight(1f), isSci = true) { viewModel.onKeyPress("nCr") }
                                CalculatorKeyButton("x", "key_x_var", modifier = Modifier.weight(1f), isSci = true) { viewModel.onKeyPress("x") }
                                CalculatorKeyButton("i", "key_i_var", modifier = Modifier.weight(1f), isSci = true) { viewModel.onKeyPress("i") }
                                CalculatorKeyButton(undoLabel, "key_undo", modifier = Modifier.weight(1f).alpha(if (undoEnabled) 1f else 0.4f), isSci = true) { if (undoEnabled) viewModel.onUndo() }
                            }
                            // Row 1 Advanced Functions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CalculatorKeyButton("sin", "key_sin", modifier = Modifier.weight(1f), isSci = true) { viewModel.onKeyPress("sin") }
                                CalculatorKeyButton("cos", "key_cos", modifier = Modifier.weight(1f), isSci = true) { viewModel.onKeyPress("cos") }
                                CalculatorKeyButton("tan", "key_tan", modifier = Modifier.weight(1f), isSci = true) { viewModel.onKeyPress("tan") }
                                CalculatorKeyButton("π", "key_pi", modifier = Modifier.weight(1f), isSci = true) { viewModel.onKeyPress("pi") }
                            }
                            // Row 2 Advanced Functions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CalculatorKeyButton("ln", "key_ln", modifier = Modifier.weight(1f), isSci = true) { viewModel.onKeyPress("ln") }
                                CalculatorKeyButton("log", "key_log", modifier = Modifier.weight(1f), isSci = true) { viewModel.onKeyPress("log") }
                                CalculatorKeyButton("^", "key_power", modifier = Modifier.weight(1f), isSci = true) { viewModel.onKeyPress("^") }
                                CalculatorKeyButton("e", "key_ee", modifier = Modifier.weight(1f), isSci = true) { viewModel.onKeyPress("EE") }
                            }
                            // Row 3 Advanced Functions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CalculatorKeyButton("√", "key_sqrt", modifier = Modifier.weight(1f), isSci = true) { viewModel.onKeyPress("sqrt") }
                                CalculatorKeyButton("x!", "key_factorial", modifier = Modifier.weight(1f), isSci = true) { viewModel.onKeyPress("x!") }
                                CalculatorKeyButton("(", "key_open_paren", modifier = Modifier.weight(1f), isSci = true) { viewModel.onKeyPress("(") }
                                CalculatorKeyButton(")", "key_close_paren", modifier = Modifier.weight(1f), isSci = true) { viewModel.onKeyPress(")") }
                            }
                        }
                    }

                    // Function Mode Action Buttons
                    AnimatedVisibility(
                        visible = mode == CalculatorMode.FUNCTION,
                        enter = expandVertically(animationSpec = tween(250)) + fadeIn(),
                        exit = shrinkVertically(animationSpec = tween(220)) + fadeOut()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CalculatorKeyButton("f(a)", "key_eval_f", modifier = Modifier.weight(1f), isSci = true) { viewModel.evaluateFunctionAtPoint() }
                            CalculatorKeyButton("f'(a)", "key_eval_deriv", modifier = Modifier.weight(1f), isSci = true) { viewModel.evaluateDerivativeAtPoint() }
                            CalculatorKeyButton("∫f(x)dx", "key_eval_int", modifier = Modifier.weight(1f), isSci = true) { viewModel.evaluateDefiniteIntegral() }
                        }
                    }

                    // Complex Mode Action Buttons
                    AnimatedVisibility(
                        visible = mode == CalculatorMode.COMPLEX,
                        enter = expandVertically(animationSpec = tween(250)) + fadeIn(),
                        exit = shrinkVertically(animationSpec = tween(220)) + fadeOut()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val op by viewModel.complexOperator.collectAsStateWithLifecycle()
                            Row(
                                modifier = Modifier
                                    .weight(1.5f)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf('+', '-', '×', '÷').forEach { o ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(24.dp))
                                            .background(if (op == o) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent)
                                            .clickable { viewModel.setComplexOperator(o) }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            o.toString(),
                                            color = if (op == o) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                    }
                                }
                            }
                            CalculatorKeyButton("|z|", "key_eval_mod", modifier = Modifier.weight(1f), isSci = true) { viewModel.evaluateModulus() }
                            CalculatorKeyButton("z̄", "key_eval_conj", modifier = Modifier.weight(1f), isSci = true) { viewModel.evaluateConjugate() }
                        }
                    }

                    standardGrid.forEach { rowSpec ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowSpec.forEach { (label, tag, color) ->
                                val weightValue = if (label == "0") 2f else 1f
                                CalculatorKeyButton(
                                    label = label,
                                    tag = tag,
                                    modifier = Modifier.weight(weightValue),
                                    textColor = color,
                                    isSci = false
                                ) {
                                    viewModel.onKeyPress(label)
                                }
                            }
                        }
                    }
                }
            }

            // 4. Overlapping Sliding Menu for History Logs Overlay
            AnimatedVisibility(
                visible = showHistoryDialog,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
                exit = shrinkVertically(animationSpec = tween(250)) + fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { showHistoryDialog = false }
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.65f)
                            .align(Alignment.TopCenter)
                            .clickable(enabled = false) {}, // do not trigger closing when clicking inside
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Calculation History",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                if (history.isNotEmpty()) {
                                    Text(
                                        text = "Clear All",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier
                                            .clickable {
                                                viewModel.clearHistory()
                                            }
                                            .padding(4.dp)
                                            .testTag("clear_history_btn")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (history.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No calculations found yet.",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        fontSize = 15.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(history, key = { it.id }) { item ->
                                        HistoryRowItem(
                                            item = item,
                                            onSelect = {
                                                viewModel.loadHistoryItem(item)
                                                showHistoryDialog = false
                                            },
                                            onDelete = {
                                                viewModel.deleteHistoryItem(item)
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Close Button
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                    .clickable {
                                        showHistoryDialog = false
                                    }
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Close History",
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalculatorKeyButton(
    label: String,
    tag: String,
    modifier: Modifier = Modifier,
    textColor: Color = TextPrimary,
    isSci: Boolean = false,
    onClick: () -> Unit
) {
    val isSystemAction = label == "AC" || label == "DEL" || label == "%" || label == "(" || label == ")"
    val isOperator = label == "÷" || label == "×" || label == "−" || label == "+"
    val isEqual = label == "="

    val (buttonBk, labelColor) = when {
        isSci -> MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.primary
        isSystemAction -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        isOperator -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        isEqual -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.onSecondary
    }

    val contrast = LocalAppContrast.current
    val haptic = LocalHapticFeedback.current

    val borderModifier = when {
        contrast > 0.01f -> {
            val borderColor = if (isSystemInDarkTheme()) Color.White else Color.Black
            Modifier.border((1.5f + contrast * 1.5f).dp, borderColor, RoundedCornerShape(28.dp))
        }
        isSci -> {
            Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(28.dp))
        }
        else -> {
            Modifier
        }
    }

    Box(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(buttonBk)
            .then(borderModifier)
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        // Exquisite custom label pairing
        val fontStyle = if (Character.isDigit(label.firstOrNull() ?: ' ')) FontFamily.Default else FontFamily.SansSerif
        val isBigOperator = label == "÷" || label == "×" || label == "−" || label == "+"
        val isPower = label == "^" || label == "√"
        
        Text(
            text = label,
            color = labelColor,
            fontSize = if (isSci) 16.sp else if (isBigOperator) 24.sp else 20.sp,
            fontWeight = if (isPower || isBigOperator || label == "=") FontWeight.Bold else FontWeight.Medium,
            fontFamily = fontStyle
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryRowItem(item: HistoryItem, onSelect: () -> Unit, onDelete: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart || it == SwipeToDismissBoxValue.StartToEnd) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                .clickable { onSelect() }
                .padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = item.formula,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 15.sp,
                textAlign = TextAlign.End,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "= " + item.result,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ComplexModeContent(
    complexOperand1: String,
    complexOperand2: String,
    complexOperator: Char,
    complexResult: String,
    focusedField: Int,
    onSetFocusedField: (Int) -> Unit
) {
    val selectedColor = MaterialTheme.colorScheme.primary
    val unselectedColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("Op 1 =", fontSize = 24.sp, color = if (focusedField == 0) selectedColor else unselectedColor, modifier = Modifier.clickable { onSetFocusedField(0) })
        Spacer(Modifier.width(8.dp))
        Text(
            text = complexOperand1.ifEmpty { "..." },
            color = if (focusedField == 0) selectedColor else unselectedColor,
            fontSize = 24.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.clickable { onSetFocusedField(0) }.weight(1f),
            textAlign = TextAlign.End
        )
    }
    Spacer(Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
        Text(
            text = complexOperator.toString(),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 24.sp
        )
    }
    Spacer(Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("Op 2 =", fontSize = 24.sp, color = if (focusedField == 1) selectedColor else unselectedColor, modifier = Modifier.clickable { onSetFocusedField(1) })
        Spacer(Modifier.width(8.dp))
        Text(
            text = complexOperand2.ifEmpty { "..." },
            color = if (focusedField == 1) selectedColor else unselectedColor,
            fontSize = 24.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.clickable { onSetFocusedField(1) }.weight(1f),
            textAlign = TextAlign.End
        )
    }
    
    Spacer(Modifier.height(16.dp))
    if (complexResult.isNotEmpty()) {
        val clipboardManager = LocalClipboardManager.current
        val context = LocalContext.current
        
        Text(
            text = complexResult,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 26.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            maxLines = 2,
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {},
                    onLongClick = {
                        clipboardManager.setText(AnnotatedString(complexResult.substringAfter("= ")))
                        Toast.makeText(context, "Result copied", Toast.LENGTH_SHORT).show()
                    }
                )
                .testTag("complex_result_display")
        )
    } else {
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FunctionModeContent(
    functionFormula: String,
    functionPoint: String,
    functionPointB: String,
    functionResult: String,
    focusedField: Int,
    onSetFocusedField: (Int) -> Unit
) {
    val selectedColor = MaterialTheme.colorScheme.primary
    val unselectedColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("f(x) =", fontSize = 24.sp, color = if (focusedField == 0) selectedColor else unselectedColor, modifier = Modifier.clickable { onSetFocusedField(0) })
        Spacer(Modifier.width(8.dp))
        Text(
            text = functionFormula.ifEmpty { "..." },
            color = if (focusedField == 0) selectedColor else unselectedColor,
            fontSize = 24.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.clickable { onSetFocusedField(0) }.weight(1f),
            textAlign = TextAlign.End
        )
    }
    Spacer(Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("a =", fontSize = 24.sp, color = if (focusedField == 1) selectedColor else unselectedColor, modifier = Modifier.clickable { onSetFocusedField(1) })
        Spacer(Modifier.width(8.dp))
        Text(
            text = functionPoint.ifEmpty { "..." },
            color = if (focusedField == 1) selectedColor else unselectedColor,
            fontSize = 24.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.clickable { onSetFocusedField(1) }.weight(1f),
            textAlign = TextAlign.End
        )
    }
    Spacer(Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("b =", fontSize = 24.sp, color = if (focusedField == 2) selectedColor else unselectedColor, modifier = Modifier.clickable { onSetFocusedField(2) })
        Spacer(Modifier.width(8.dp))
        Text(
            text = functionPointB.ifEmpty { "..." },
            color = if (focusedField == 2) selectedColor else unselectedColor,
            fontSize = 24.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.clickable { onSetFocusedField(2) }.weight(1f),
            textAlign = TextAlign.End
        )
    }
    
    Spacer(Modifier.height(16.dp))
    if (functionResult.isNotEmpty()) {
        val clipboardManager = LocalClipboardManager.current
        val context = LocalContext.current
        Text(
            text = functionResult,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 26.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            maxLines = 2,
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {},
                    onLongClick = {
                        clipboardManager.setText(AnnotatedString(functionResult))
                        Toast.makeText(context, "Result copied", Toast.LENGTH_SHORT).show()
                    }
                )
        )
    } else {
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NormalModeContent(
    formula: String,
    calculationResult: String
) {
    val fontSize = remember(formula) {
        val textLength = formula.length
        when {
            textLength > 24 -> 24.sp
            textLength > 16 -> 32.sp
            else -> 46.sp
        }
    }

    Text(
        text = formula.ifEmpty { "0" },
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = fontSize,
        fontWeight = FontWeight.Light,
        textAlign = TextAlign.End,
        maxLines = 4,
        lineHeight = fontSize * 1.25,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("formula_display")
    )

    Spacer(modifier = Modifier.height(10.dp))

    if (calculationResult.isNotEmpty()) {
        val clipboardManager = LocalClipboardManager.current
        val context = LocalContext.current
        
        Text(
            text = calculationResult,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
            fontSize = 26.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {},
                    onLongClick = {
                        clipboardManager.setText(AnnotatedString(calculationResult.removePrefix("= ")))
                        Toast.makeText(context, "Result copied", Toast.LENGTH_SHORT).show()
                    }
                )
                .testTag("result_display")
        )
    } else {
        Spacer(modifier = Modifier.height(32.dp))
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MatrixModeContent(
    matrixSize: Int,
    matrixA: List<List<String>>,
    matrixB: List<List<String>>,
    focusedMatrix: Char,
    focusedRow: Int,
    focusedCol: Int,
    matrixResult: MatrixResult,
    onSetSize: (Int) -> Unit,
    onFocusCell: (Char, Int, Int) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Size selector
        Row(
            modifier = Modifier
                .wrapContentWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            listOf(2, 3, 4).forEach { size ->
                val isSelected = matrixSize == size
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { onSetSize(size) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${size}×${size}",
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MatrixInputGrid(
                label = "Matrix A",
                matrixId = 'A',
                data = matrixA,
                size = matrixSize,
                focusedMatrix = focusedMatrix,
                focusedRow = focusedRow,
                focusedCol = focusedCol,
                onFocusCell = onFocusCell
            )
            MatrixInputGrid(
                label = "Matrix B",
                matrixId = 'B',
                data = matrixB,
                size = matrixSize,
                focusedMatrix = focusedMatrix,
                focusedRow = focusedRow,
                focusedCol = focusedCol,
                onFocusCell = onFocusCell
            )
        }
        
        Spacer(Modifier.height(16.dp))
        
        when (matrixResult) {
            is MatrixResult.Empty -> {
                Spacer(modifier = Modifier.height(32.dp))
            }
            is MatrixResult.Scalar -> {
                Text(
                    text = "= ${com.example.util.Matrix.Companion.formatMatrix(com.example.util.Matrix(2,2, listOf(listOf(matrixResult.value,0.0), listOf(0.0,0.0)))).replace("[[", "").split(",")[0]}", // Quick format hack
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            is MatrixResult.Error -> {
                Text(
                    text = matrixResult.message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            is MatrixResult.MatrixGrid -> {
                val resData = matrixResult.value.data
                Text("Result", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Column(
                    modifier = Modifier
                        .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    for (i in 0 until matrixResult.value.rows) {
                        Row {
                            for (j in 0 until matrixResult.value.cols) {
                                val value = resData[i][j]
                                val formatted = "%.4f".format(value).trimEnd('0').trimEnd('.')
                                val finalStr = if (formatted == "-0" || formatted == "") "0" else formatted
                                Text(
                                    text = finalStr,
                                    modifier = Modifier.padding(4.dp).width(50.dp),
                                    textAlign = TextAlign.Center,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MatrixInputGrid(
    label: String,
    matrixId: Char,
    data: List<List<String>>,
    size: Int,
    focusedMatrix: Char,
    focusedRow: Int,
    focusedCol: Int,
    onFocusCell: (Char, Int, Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            for (i in 0 until size) {
                Row {
                    for (j in 0 until size) {
                        val isFocused = focusedMatrix == matrixId && focusedRow == i && focusedCol == j
                        val bgColor = if (isFocused) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        val textColor = if (isFocused) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        
                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .size(if (size == 4) 36.dp else 48.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(bgColor)
                                .clickable { onFocusCell(matrixId, i, j) }
                                .border(if (isFocused) 2.dp else 1.dp, if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = data[i][j].ifEmpty { "0" },
                                color = textColor,
                                fontSize = if (size == 4) 12.sp else 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
