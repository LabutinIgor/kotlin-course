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
        if (scopeContexts.last().containsVariable(name)) {
            throw RedefineVariableException()
        } else {
            scopeContexts.last().addVariable(name, value)
        }
    }

    fun addFunction(name: String, value: FunParser.FunctionContext) {
        if (scopeContexts.last().containsFunction(name)) {
            throw RedefineFunctionException()
        } else {
            scopeContexts.last().addFunction(name, value)
        }
    }

    fun setVariable(name: String, value: Int?) {
        for (i in scopeContexts.size - 1 downTo 0) {
            if (scopeContexts[i].containsVariable(name)) {
                scopeContexts[i].setVariable(name, value)
                return
            }
        }
        throw UndefinedVariableException()
    }

    fun getVariable(name: String): Int? {
//        System.err.println("get " + name + " " + scopeContexts.size)
//        System.err.println(scopeContexts.last().containsVariable(name))
        for (i in scopeContexts.size - 1 downTo 0) {
            if (scopeContexts[i].containsVariable(name)) {
                return scopeContexts[i].getVariable(name)
            }
        }
        throw UndefinedVariableException()
    }

    fun getFunction(name: String): FunParser.FunctionContext {
        val f = (scopeContexts.size - 1 downTo 0)
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
