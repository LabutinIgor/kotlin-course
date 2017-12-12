package ru.spbau.mit

import ru.spbau.mit.parser.FunParser

class Context {
    private val scopeContexts: MutableList<ScopeContext> = arrayListOf(ScopeContext())

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

    fun setVariable(name: String, value: Int) {
        for (context in scopeContexts.asReversed()) {
            if (context.containsVariable(name)) {
                context.setVariable(name, value)
                return
            }
        }
    }

    fun isVariableDefined(name: String): Boolean {
        return scopeContexts.any { it.containsVariable(name) }
    }

    fun isFunctionDefined(name: String): Boolean {
        return scopeContexts.any { it.containsFunction(name) }
    }

    fun getVariable(name: String): Int? {
        for (context in scopeContexts.asReversed()) {
            if (context.containsVariable(name)) {
                return context.getVariable(name)
            }
        }
        return null
    }

    fun getFunction(name: String): FunParser.FunctionContext? {
        for (context in scopeContexts.asReversed()) {
            if (context.containsFunction(name)) {
                return context.getFunction(name)
            }
        }
        return null
    }

    class ScopeContext {
        private val variables: MutableMap<String, Int?> = mutableMapOf()
        private val functions: MutableMap<String, FunParser.FunctionContext> = mutableMapOf()

        fun addVariable(name: String, value: Int?) {
            variables.put(name, value)
        }

        fun addFunction(name: String, value: FunParser.FunctionContext) {
            functions.put(name, value)
        }

        fun setVariable(name: String, value: Int) {
            variables[name] = value
        }

        fun containsVariable(name: String): Boolean = variables.containsKey(name)

        fun getVariable(name: String): Int? = variables[name]

        fun containsFunction(name: String): Boolean = functions.containsKey(name)

        fun getFunction(name: String): FunParser.FunctionContext = functions[name]!!
    }
}
