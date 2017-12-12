package ru.spbau.mit

open class FunException(message: String, val line: Int) : Exception(message)

class UndefinedVariableException(message: String, line: Int) : FunException(message, line)

class RedefineVariableException(message: String, line: Int) : FunException(message, line)

class RedefineFunctionException(message: String, line: Int) : FunException(message, line)

class UndefinedFunctionException(message: String, line: Int) : FunException(message, line)

class UndefinedOperationException(message: String, line: Int) : FunException(message, line)

class UninitializedVariableException(message: String, line: Int) : FunException(message, line)

class DivisionByZeroException(message: String, line: Int) : FunException(message, line)

class IncorrectNumberOfArgsException(message: String, line: Int) : FunException(message, line)

class NumberOverflowException(message: String, line: Int) : FunException(message, line)
