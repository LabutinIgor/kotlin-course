package ru.spbau.mit

import ru.spbau.mit.parser.FunParser

class Context {
    private var scopeContexts: MutableList<ScopeContext> = arrayListOf(ScopeContext())

    fun enterScope() {
        scopeContexts.add(ScopeContext())
    }

    fun leaveScope() {
        scopeContexts.removeAt(scopeContexts.size - 1)
    }

    fun addVariable(name: String, value: Int? = null) {
        scopeContexts.last().addVariable(name, value)
    }

    fun addFunction(name: String, value: FunParser.FunctionContext) {
        scopeContexts.last().addFunction(name, value)
    }

    fun setVariable(name: String, value: Int?) {
        for (context in scopeContexts.asReversed()) {
            if (context.containsVariable(name)) {
                context.setVariable(name, value)
                return
            }
        }
        throw UndefinedVariableException("Undefined variable")
    }

    fun getVariable(name: String): Int {
        for (context in scopeContexts.asReversed()) {
            if (context.containsVariable(name)) {
                return context.getVariable(name) ?: throw UninitializedVariableException("Uninitialized variable")
            }
        }
        throw UndefinedVariableException("Undefined variable")
    }

    fun getFunction(name: String): FunParser.FunctionContext {
        for (context in scopeContexts.asReversed()) {
            if (context.containsFunction(name)) {
                return context.getFunction(name)
            }
        }
        throw UndefinedFunctionException("Undefined function")
    }

    class ScopeContext {
        private var variables: MutableMap<String, Int?> = mutableMapOf()
        private var functions: MutableMap<String, FunParser.FunctionContext> = mutableMapOf()

        fun addVariable(name: String, value: Int?) {
            if (containsVariable(name)) {
                throw RedefineVariableException("Redefine variable")
            }
            variables.put(name, value)
        }

        fun addFunction(name: String, value: FunParser.FunctionContext) {
            if (containsFunction(name)) {
                throw RedefineFunctionException("Redefine function")
            }
            functions.put(name, value)
        }

        fun setVariable(name: String, value: Int?) {
            variables[name] = value
        }

        fun containsVariable(name: String): Boolean {
            return variables.containsKey(name)
        }

        fun getVariable(name: String): Int? {
            return variables[name]
        }

        fun containsFunction(name: String): Boolean {
            return functions.containsKey(name)
        }

        fun getFunction(name: String): FunParser.FunctionContext {
            return functions[name]!!
        }
    }
}
