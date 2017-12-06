package ru.spbau.mit

open class FunException(override val message: String = "", open val line: Int? = null) : Exception(message)

class UndefinedVariableException(message: String = "", line: Int? = null) : FunException(message, line)

class RedefineVariableException(message: String = "", line: Int? = null) : FunException(message, line)

class RedefineFunctionException(message: String = "", line: Int? = null) : FunException(message, line)

class UndefinedFunctionException(message: String = "", line: Int? = null) : FunException(message, line)

class UninitializedVariableException(message: String = "", line: Int? = null) : FunException(message, line)

class DivisionByZeroException(message: String = "", line: Int? = null) : FunException(message, line)

class IncorrectNumberOfArgsException(message: String = "", line: Int? = null) : FunException(message, line)

class NumberOverflowException(message: String = "", line: Int? = null) : FunException(message, line)
