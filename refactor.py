import re

with open('app/src/main/java/com/example/ui/CalculatorScreen.kt', 'r') as f:
    content = f.read()

# 1. Add CalculatorMode import
content = content.replace(
    'import com.example.viewmodel.CalculatorViewModel\nimport com.example.viewmodel.HistoryItem',
    'import com.example.viewmodel.CalculatorViewModel\nimport com.example.viewmodel.CalculatorMode\nimport com.example.viewmodel.HistoryItem'
)

# 2. Replace state collections
content = content.replace(
    'val isFunctionMode by viewModel.isFunctionMode.collectAsStateWithLifecycle()\n    val isComplexMode by viewModel.isComplexMode.collectAsStateWithLifecycle()',
    'val mode by viewModel.mode.collectAsStateWithLifecycle()'
)

# 3. Replace toggle functions
content = content.replace(
    'if (isFunctionMode) MaterialTheme',
    'if (mode == CalculatorMode.FUNCTION) MaterialTheme'
)
content = content.replace(
    'viewModel.toggleFunctionMode()',
    'viewModel.setMode(CalculatorMode.FUNCTION)'
)

content = content.replace(
    'if (isComplexMode) MaterialTheme',
    'if (mode == CalculatorMode.COMPLEX) MaterialTheme'
)
content = content.replace(
    'viewModel.toggleComplexMode()',
    'viewModel.setMode(CalculatorMode.COMPLEX)'
)

# 4. Replace the AnimatedVisibility conditions
content = content.replace(
    'visible = isFunctionMode,',
    'visible = mode == CalculatorMode.FUNCTION,'
)
content = content.replace(
    'visible = isComplexMode,',
    'visible = mode == CalculatorMode.COMPLEX,'
)

# 5. We need to extract the display areas into separate Composables.
# Let's find the Interactive Calculation Screen Display Area
display_start = content.find('                // 2. Interactive Calculation Screen Display Area\n                Column(')
display_end = content.find('                HorizontalDivider(', display_start)

# We want to replace the whole content inside the Column from display_start up to display_end.
# The internal part starts at:
#                     horizontalAlignment = Alignment.End
#                 ) {
col_open_end = content.find(') {\n', display_start) + 4
col_close = content.rfind('                }\n\n', display_start, display_end) + 18

display_inner = content[col_open_end:col_close]

new_display_inner = """                    when (mode) {
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
                    }
                }
"""

content = content[:col_open_end] + new_display_inner + content[col_close:]

# Now we append the extracted composables at the end of the file
extracted = """

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
"""

content = content + extracted

with open('app/src/main/java/com/example/ui/CalculatorScreen.kt', 'w') as f:
    f.write(content)

