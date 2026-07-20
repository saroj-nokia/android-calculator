package com.example.util

import kotlin.math.*

object CalculatorEvaluator {
    fun evaluate(expression: String, isDegrees: Boolean): Double {
        val sanitized = sanitize(expression)
        val tokens = tokenize(sanitized)
        return Parser(tokens, isDegrees).parse()
    }

    private fun sanitize(expr: String): String {
        return expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", "pi")
    }

    private fun tokenize(expr: String): List<String> {
        val result = mutableListOf<String>()
        var i = 0
        val s = expr.replace(" ", "")

        while (i < s.length) {
            val c = s[i]
            when {
                c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || c == ')' || c == '^' || c == '!' || c == '%' -> {
                    result.add(c.toString())
                    i++
                }
                c == 'p' && i + 1 < s.length && s[i + 1] == 'i' -> {
                    result.add("pi")
                    i += 2
                }
                c.isDigit() || c == '.' -> {
                    val sb = StringBuilder()
                    while (i < s.length && (s[i].isDigit() || s[i] == '.' || s[i] == 'E' || s[i] == 'e')) {
                        if (s[i] == 'E' || s[i] == 'e') {
                            sb.append(s[i])
                            i++
                            // Handing signed exponents like E-5, E+12
                            if (i < s.length && (s[i] == '+' || s[i] == '-')) {
                                sb.append(s[i])
                                i++
                            }
                        } else {
                            sb.append(s[i])
                            i++
                        }
                    }
                    result.add(sb.toString())
                }
                c.isLetter() -> {
                    val sb = StringBuilder()
                    while (i < s.length && s[i].isLetter()) {
                        sb.append(s[i])
                        i++
                    }
                    val word = sb.toString()
                    result.add(word)
                }
                else -> {
                    i++
                }
            }
        }
        return result
    }

    private class Parser(private val tokens: List<String>, private val isDegrees: Boolean) {
        private var idx = 0

        private fun peek(): String? {
            return if (idx < tokens.size) tokens[idx] else null
        }

        private fun consume(): String {
            return tokens[idx++]
        }

        fun parse(): Double {
            if (tokens.isEmpty()) return 0.0
            val res = parseExpression()
            if (idx < tokens.size) {
                throw IllegalArgumentException("Unexpected token: ${tokens[idx]}")
            }
            return res
        }

        // Expression = Term (('+' | '-') Term)*
        private fun parseExpression(): Double {
            var val1 = parseTerm(null)
            while (true) {
                val p = peek()
                if (p == "+" || p == "-") {
                    val op = consume()
                    val val2 = parseTerm(val1)
                    if (op == "+") {
                        val1 += val2
                    } else {
                        val1 -= val2
                    }
                } else {
                    break
                }
            }
            return val1
        }

        // Term = Unary (('*' | '/') Unary)*
        private fun parseTerm(addBase: Double?): Double {
            var val1 = parseUnary()
            var p = peek()
            while (p == "%") {
                consume()
                if (addBase != null) {
                    val1 = addBase * (val1 / 100.0)
                } else {
                    val1 /= 100.0
                }
                p = peek()
            }
            while (true) {
                p = peek()
                if (p == "*" || p == "/") {
                    val op = consume()
                    var val2 = parseUnary()
                    var p2 = peek()
                    while (p2 == "%") {
                        consume()
                        val2 /= 100.0
                        p2 = peek()
                    }
                    if (op == "*") {
                        val1 *= val2
                    } else {
                        if (val2 == 0.0) throw ArithmeticException("Division by zero")
                        val1 /= val2
                    }
                } else {
                    // Implicit multiplication, e.g., 2pi, 2(3+5), 3ln(e), (4)(5)
                    if (p != null && (p == "(" || p == "pi" || p == "e" || isFunction(p) || p.toDoubleOrNull() != null)) {
                        var val2 = parseUnary()
                        var p2 = peek()
                        while (p2 == "%") {
                            consume()
                            val2 /= 100.0
                            p2 = peek()
                        }
                        val1 *= val2
                    } else {
                        break
                    }
                }
            }
            return val1
        }

        private fun parseUnary(): Double {
            val p = peek() ?: throw IllegalArgumentException("Unexpected end of formula")
            if (p == "-") {
                consume()
                return -parseUnary()
            }
            if (p == "+") {
                consume()
                return parseUnary()
            }
            return parseFactor()
        }

        private fun isFunction(name: String): Boolean {
            return name == "sin" || name == "cos" || name == "tan" || name == "asin" || name == "acos" || name == "atan" || name == "ln" || name == "log" || name == "sqrt"
        }

        // Factor = Base ('!')* ('^' Unary)*
        private fun parseFactor(): Double {
            var val1 = parseBase()
            // Postfix operators like !
            while (true) {
                val p = peek()
                if (p == "!") {
                    consume()
                    val1 = factorial(val1)
                } else {
                    break
                }
            }
            if (peek() == "^") {
                consume()
                val val2 = parseUnary()
                val1 = val1.pow(val2)
            }
            return val1
        }

        // Base = Number | Constant | Function | '(' Expression ')'
        private fun parseBase(): Double {
            val p = peek() ?: throw IllegalArgumentException("Unexpected end of formula")

            if (p == "(") {
                consume()
                val res = parseExpression()
                if (peek() == ")") {
                    consume()
                } else {
                    throw IllegalArgumentException("Missing closing parenthesis")
                }
                return res
            }

            if (p == "pi") {
                consume()
                return PI
            }

            if (p == "e") {
                consume()
                return E
            }

            if (isFunction(p)) {
                val func = consume()
                val arg = if (peek() == "(") {
                    consume()
                    val res = parseExpression()
                    if (peek() == ")") {
                        consume()
                    } else {
                        throw IllegalArgumentException("Missing closing parenthesis")
                    }
                    res
                } else {
                    parseUnary()
                }

                return when (func) {
                    "sin" -> if (isDegrees) sin(Math.toRadians(arg)) else sin(arg)
                    "cos" -> if (isDegrees) cos(Math.toRadians(arg)) else cos(arg)
                    "tan" -> if (isDegrees) tan(Math.toRadians(arg)) else tan(arg)
                    "asin" -> if (isDegrees) Math.toDegrees(asin(arg)) else asin(arg)
                    "acos" -> if (isDegrees) Math.toDegrees(acos(arg)) else acos(arg)
                    "atan" -> if (isDegrees) Math.toDegrees(atan(arg)) else atan(arg)
                    "ln" -> ln(arg)
                    "log" -> log10(arg)
                    "sqrt" -> sqrt(arg)
                    else -> throw IllegalArgumentException("Unknown function: $func")
                }
            }

            val numStr = consume()
            val cleanStr = numStr.replace("e", "E")
            return cleanStr.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid number: $numStr")
        }

        private fun factorial(n: Double): Double {
            if (n < 0.0) return Double.NaN
            val intN = n.toInt()
            if (intN.toDouble() != n) return Double.NaN
            if (intN > 170) return Double.POSITIVE_INFINITY
            var result = 1.0
            for (i in 1..intN) {
                result *= i
            }
            return result
        }
    }
}
