package ru.spbau.mit

import ru.spbau.mit.parser.FunParser

class Context {
    var scopeContexts: MutableList<ScopeContext> = arrayListOf()

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

    fun getVariable(name: String): Int? {
        (scopeContexts.size - 1..0)
                .asSequence()
                .filter { scopeContexts[it].containsVariable(name) }
                .forEach { return scopeContexts[it].getVariable(name) }
        throw UndefinedVariableException()
    }

    fun getFunction(name: String): FunParser.FunctionContext {
        val f = (scopeContexts.size - 1..0)
                .firstOrNull { scopeContexts[it].containsFunction(name) }
                ?.let { scopeContexts[it].getFunction(name) }
        if (f == null) {
            throw UndefinedFunctionException()
        } else {
            return f
        }
    }

    class ScopeContext {
        private var variables: MutableMap<String, Int?> = mutableMapOf()
        private var functions: MutableMap<String, FunParser.FunctionContext> = mutableMapOf()

        fun addVariable(name: String, value: Int?) {
            variables.put(name, value)
        }

        fun addFunction(name: String, value: FunParser.FunctionContext) {
            functions.put(name, value)
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
