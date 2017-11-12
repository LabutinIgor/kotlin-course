package ru.spbau.mit

open class FunException(override val message: String = "") : Exception(message) {
}

class UndefinedVariableException(override val message: String = "") : FunException(message) {
}

class UndefinedFunctionException(override val message: String = "") : FunException(message) {
}
