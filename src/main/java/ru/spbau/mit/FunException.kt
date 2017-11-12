package ru.spbau.mit

open class FunException(override val message: String = "", open val line: Int? = null) : Exception(message)

class UndefinedVariableException(override val message: String = "", override val line: Int? = null) :
        FunException(message, line)

class RedefineVariableException(override val message: String = "", override val line: Int? = null) :
        FunException(message, line)

class RedefineFunctionException(override val message: String = "", override val line: Int? = null) :
        FunException(message, line)

class UndefinedFunctionException(override val message: String = "", override val line: Int? = null) :
        FunException(message, line)

class DivisionByZeroException(override val message: String = "", override val line: Int? = null) :
        FunException(message, line)

class IncorrectNumberOfArgsException(override val message: String = "", override val line: Int? = null) :
        FunException(message, line)

class NumberOverflowException(override val message: String = "", override val line: Int? = null) :
        FunException(message, line)
