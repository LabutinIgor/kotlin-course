package ru.spbau.mit

import ru.spbau.mit.parser.FunBaseVisitor
import ru.spbau.mit.parser.FunParser

class FunInterpreter(private val context: Context = Context(), out: java.io.PrintStream = System.out) :
        FunBaseVisitor<Int?>() {
    private val exprVisitor = ExpressionsVisitor(this, context, out)

    override fun visitBlock(ctx: FunParser.BlockContext): Int? {
        return context.withScope({
            for (statement in ctx.statement()) {
                val res = visit(statement)
                if (res != null) {
                    return@withScope res
                }
            }
            return@withScope null
        })
    }

    override fun visitExpressionStatement(ctx: FunParser.ExpressionStatementContext): Int? {
        exprVisitor.visit(ctx.expression())
        return null
    }

    override fun visitBlockWithBraces(ctx: FunParser.BlockWithBracesContext): Int? = visit(ctx.block())

    override fun visitFunction(ctx: FunParser.FunctionContext): Int? {
        if (!context.addFunction(ctx.IDENTIFIER().text, ctx)) {
            throw RedefineFunctionException("Function redefined", ctx.start.line)
        }
        return null
    }

    override fun visitVariable(ctx: FunParser.VariableContext): Int? {
        val name = ctx.IDENTIFIER()
        val value = if (ctx.expression() != null) {
            exprVisitor.visit(ctx.expression())
        } else {
            null
        }
        if (!context.addVariable(name.text, value)) {
            throw RedefineVariableException("Variable redefined", ctx.start.line)
        }
        return null
    }

    override fun visitWhileStatement(ctx: FunParser.WhileStatementContext): Int? {
        while (exprVisitor.visit(ctx.expression()) != 0) {
            val res = visit(ctx.blockWithBraces())
            if (res != null) {
                return res
            }
        }
        return null
    }

    override fun visitIfStatement(ctx: FunParser.IfStatementContext): Int? {
        if (exprVisitor.visit(ctx.expression()) != 0) {
            return visit(ctx.blockWithBraces(0))
        } else {
            if (ctx.blockWithBraces().size == 2) {
                return visit(ctx.blockWithBraces(1))
            }
        }
        return null
    }

    override fun visitAssignment(ctx: FunParser.AssignmentContext): Int? {
        if (!context.setVariable(ctx.IDENTIFIER().text, exprVisitor.visit(ctx.expression()))) {
            throw UndefinedVariableException("Undefined variable", ctx.start.line)
        }
        return null
    }

    override fun visitReturnStatement(ctx: FunParser.ReturnStatementContext): Int? =
            exprVisitor.visit(ctx.expression())

    fun evaluateFunction(function: FunParser.FunctionContext, args: List<Int?>, line: Int): Int {
        val names = function.parameterNames().IDENTIFIER().map { it.text }.toList()
        if (names.size != args.size) {
            throw IncorrectNumberOfArgsException("Incorrect number of arguments", line)
        }

        context.enterScope()
        for (i in 0 until names.size) {
            if (!context.addVariable(names[i], args[i])) {
                throw RedefineVariableException("Variable redefined", line)
            }
        }
        val res = visit(function.blockWithBraces())
        context.leaveScope()
        return res ?: 0
    }
}
