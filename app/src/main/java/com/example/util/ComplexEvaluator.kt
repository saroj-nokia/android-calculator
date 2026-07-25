package com.example.util

import kotlin.math.sqrt
import kotlin.math.abs

// v1: supports two-operand expressions only, no operator chaining/precedence yet
data class Complex(val re: Double, val im: Double) {
    operator fun plus(other: Complex) = Complex(re + other.re, im + other.im)
    operator fun minus(other: Complex) = Complex(re - other.re, im - other.im)
    operator fun times(other: Complex) = Complex(
        re * other.re - im * other.im,
        re * other.im + im * other.re
    )
    operator fun div(other: Complex): Complex {
        val denominator = other.re * other.re + other.im * other.im
        if (denominator == 0.0) throw ArithmeticException("Division by zero")
        return Complex(
            (re * other.re + im * other.im) / denominator,
            (im * other.re - re * other.im) / denominator
        )
    }

    fun modulus(): Double = sqrt(re * re + im * im)
    fun conjugate(): Complex = Complex(re, -im)
}

object ComplexEvaluator {
    fun parseLiteral(s: String): Complex {
        val str = s.trim().replace(" ", "")
        if (str.isEmpty()) throw IllegalArgumentException("Empty string")
        
        if (str == "i") return Complex(0.0, 1.0)
        if (str == "-i") return Complex(0.0, -1.0)

        var splitIdx = -1
        for (i in str.indices.reversed()) {
            val c = str[i]
            if (c == '+' || c == '-') {
                if (i == 0) continue
                if (str[i - 1] == 'e' || str[i - 1] == 'E') continue
                splitIdx = i
                break
            }
        }

        if (splitIdx != -1) {
            val part1 = str.substring(0, splitIdx)
            val part2 = str.substring(splitIdx)
            
            if (part2.endsWith("i")) {
                val re = part1.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid real part: $part1")
                val imStr = part2.dropLast(1)
                val im = if (imStr == "+" || imStr.isEmpty()) 1.0 else if (imStr == "-") -1.0 else imStr.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid imag part: $imStr")
                return Complex(re, im)
            } else {
                throw IllegalArgumentException("Invalid complex literal: $str")
            }
        } else {
            if (str.endsWith("i")) {
                val imStr = str.dropLast(1)
                val im = if (imStr.isEmpty() || imStr == "+") 1.0 else if (imStr == "-") -1.0 else imStr.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid imag part: $imStr")
                return Complex(0.0, im)
            } else {
                val re = str.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid real part: $str")
                return Complex(re, 0.0)
            }
        }
    }

    fun evaluate(expression: String): Complex {
        val sanitized = expression.replace("×", "*").replace("÷", "/").trim().replace(" ", "")
        if (sanitized.isEmpty()) throw IllegalArgumentException("Empty expression")
        
        // Try * and / first
        for (op in listOf("*", "/")) {
            val idx = sanitized.indexOf(op)
            if (idx != -1) {
                val left = sanitized.substring(0, idx)
                val right = sanitized.substring(idx + 1)
                try {
                    val c1 = parseLiteral(left)
                    val c2 = parseLiteral(right)
                    return if (op == "*") c1 * c2 else c1 / c2
                } catch (e: IllegalArgumentException) {
                    // Try next operator
                }
            }
        }

        // Try + and - (find operators connecting two valid literals)
        for (i in 1 until sanitized.length - 1) {
            val c = sanitized[i]
            if (c == '+' || c == '-') {
                if (sanitized[i - 1].equals('e', true)) continue
                
                val leftStr = sanitized.substring(0, i)
                val rightStr = sanitized.substring(i + 1)
                
                try {
                    val c1 = parseLiteral(leftStr)
                    val c2 = parseLiteral(rightStr)
                    return if (c == '+') c1 + c2 else c1 - c2
                } catch (e: IllegalArgumentException) {
                    // Try next operator
                }
            }
        }

        // Single literal
        return parseLiteral(sanitized)
    }

    fun formatComplex(c: Complex): String {
        val re = c.re
        val im = c.im

        fun formatNum(num: Double): String {
            if (num.isNaN()) return "NaN"
            if (num.isInfinite()) return if (num < 0) "-∞" else "∞"
            val s = num.toString()
            return if (s.endsWith(".0")) s.dropLast(2) else s
        }

        val isReZero = abs(re) < 1e-12
        val isImZero = abs(im) < 1e-12

        if (isReZero && isImZero) return "0"
        
        if (isReZero) {
            if (abs(im - 1.0) < 1e-12) return "i"
            if (abs(im + 1.0) < 1e-12) return "-i"
            return "${formatNum(im)}i"
        }
        
        if (isImZero) {
            return formatNum(re)
        }

        val sign = if (im < 0) " - " else " + "
        val absIm = abs(im)
        
        val imStr = if (abs(absIm - 1.0) < 1e-12) "i" else "${formatNum(absIm)}i"
        
        return "${formatNum(re)}$sign$imStr"
    }
}
