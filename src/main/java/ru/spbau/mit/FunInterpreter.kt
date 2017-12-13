package ru.spbau.mit

import ru.spbau.mit.parser.FunBaseVisitor
import ru.spbau.mit.parser.FunParser

class FunInterpreter(private val context: Context = Context(), private val out: java.io.PrintStream = System.out) :
        FunBaseVisitor<Int?>() {
    override fun visitBlock(ctx: FunParser.BlockContext): Int? {
        context.enterScope()
        var res: Int? = null
        for (statement in ctx.statement()) {
            res = visit(statement)
            if (res != null) {
                break
            }
        }
        context.leaveScope()
        return res
    }

    override fun visitExpressionStatement(ctx: FunParser.ExpressionStatementContext): Int? {
        visit(ctx.expression())
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
            visit(ctx.expression())
        } else {
            null
        }
        if (!context.addVariable(name.text, value)) {
            throw RedefineVariableException("Variable redefined", ctx.start.line)
        }
        return null
    }

    override fun visitWhileStatement(ctx: FunParser.WhileStatementContext): Int? {
        while (visit(ctx.expression()) != 0) {
            val res = visit(ctx.blockWithBraces())
            if (res != null) {
                return res
            }
        }
        return null
    }

    override fun visitIfStatement(ctx: FunParser.IfStatementContext): Int? {
        if (visit(ctx.expression()) != 0) {
            return visit(ctx.blockWithBraces(0))
        } else {
            if (ctx.blockWithBraces().size == 2) {
                return visit(ctx.blockWithBraces(1))
            }
        }
        return null
    }

    override fun visitAssignment(ctx: FunParser.AssignmentContext): Int? {
        if (!context.setVariable(ctx.IDENTIFIER().text, visit(ctx.expression())!!)) {
            throw UndefinedVariableException("Undefined variable", ctx.start.line)
        }
        return null
    }

    override fun visitReturnStatement(ctx: FunParser.ReturnStatementContext): Int? = visit(ctx.expression())

    override fun visitExprPriority1(ctx: FunParser.ExprPriority1Context): Int {
        val leftRes = visit(ctx.expression(0))!!
        val rightRes = visit(ctx.expression(1))!!
        return when (ctx.op.text) {
            "%" ->
                if (rightRes == 0) {
                    throw DivisionByZeroException("Division by zero", ctx.start.line)
                } else {
                    leftRes % rightRes
                }
            "/" ->
                if (rightRes == 0) {
                    throw DivisionByZeroException("Division by zero", ctx.start.line)
                } else {
                    leftRes / rightRes
                }
            "*" -> leftRes * rightRes
            else -> throw UndefinedOperationException("Undefined operation", ctx.start.line)
        }
    }

    override fun visitExprPriority2(ctx: FunParser.ExprPriority2Context): Int {
        val leftRes = visit(ctx.expression(0))!!
        val rightRes = visit(ctx.expression(1))!!
        return when (ctx.op.text) {
            "-" -> leftRes - rightRes
            "+" -> leftRes + rightRes
            else -> throw UndefinedOperationException("Undefined operation", ctx.start.line)
        }
    }

    override fun visitExprPriority3(ctx: FunParser.ExprPriority3Context): Int {
        val leftRes = visit(ctx.expression(0))!!
        val rightRes = visit(ctx.expression(1))!!
        return when (ctx.op.text) {
            ">=" -> if (leftRes >= rightRes) 1 else 0
            "<=" -> if (leftRes <= rightRes) 1 else 0
            ">" -> if (leftRes > rightRes) 1 else 0
            "<" -> if (leftRes < rightRes) 1 else 0
            else -> throw UndefinedOperationException("Undefined operation", ctx.start.line)
        }
    }

    override fun visitExprPriority4(ctx: FunParser.ExprPriority4Context): Int {
        val leftRes = visit(ctx.expression(0))!!
        val rightRes = visit(ctx.expression(1))!!
        return when (ctx.op.text) {
            "!=" -> if (leftRes != rightRes) 1 else 0
            "==" -> if (leftRes == rightRes) 1 else 0
            else -> throw UndefinedOperationException("Undefined operation", ctx.start.line)
        }
    }

    override fun visitExprPriority5(ctx: FunParser.ExprPriority5Context): Int {
        val leftRes = visit(ctx.expression(0))!!
        val rightRes = visit(ctx.expression(1))!!
        return when (ctx.op.text) {
            "&&" -> if (leftRes != 0 && rightRes != 0) 1 else 0
            else -> throw UndefinedOperationException("Undefined operation", ctx.start.line)
        }
    }

    override fun visitExprPriority6(ctx: FunParser.ExprPriority6Context): Int {
        val leftRes = visit(ctx.expression(0))!!
        val rightRes = visit(ctx.expression(1))!!
        return when (ctx.op.text) {
            "||" -> if (leftRes != 0 || rightRes != 0) 1 else 0
            else -> throw UndefinedOperationException("Undefined operation", ctx.start.line)
        }
    }

    override fun visitExprUnaryMinus(ctx: FunParser.ExprUnaryMinusContext): Int = -visit(ctx.expression())!!

    override fun visitExprUnaryPlus(ctx: FunParser.ExprUnaryPlusContext): Int = -visit(ctx.expression())!!

    override fun visitIdentifierExpression(ctx: FunParser.IdentifierExpressionContext): Int {
        if (context.isVariableDefined(ctx.IDENTIFIER().text)) {
            val res = context.getVariable(ctx.IDENTIFIER().text)
            if (res == null) {
                throw UninitializedVariableException("Uninitialized variable", ctx.start.line)
            } else {
                return res
            }
        } else {
            throw UndefinedVariableException("Undefined variable", ctx.start.line)
        }
    }

    override fun visitLiteralExpression(ctx: FunParser.LiteralExpressionContext): Int {
        try {
            return ctx.LITERAL().text.toInt()
        } catch (e: NumberFormatException) {
            throw NumberOverflowException("Numeric overflow", ctx.start.line)
        }
    }

    override fun visitBracketedExpression(ctx: FunParser.BracketedExpressionContext): Int = visit(ctx.expression())!!

    private fun evaluateFunction(function: FunParser.FunctionContext, args: List<Int?>, line: Int): Int {
        val names = function.parameterNames().IDENTIFIER().map { it.text }.toList()
        if (names.size != args.size) {
            throw IncorrectNumberOfArgsException("Incorrect number of arguments", line)
        }

        context.enterScope()
        for (i in 0 until names.size) {
            context.addVariable(names[i], args[i])
        }
        val res = visit(function.blockWithBraces())
        context.leaveScope()
        return res ?: 0
    }

    override fun visitFunctionCall(ctx: FunParser.FunctionCallContext): Int {
        val name = ctx.IDENTIFIER().text
        val args = ctx.arguments().expression().map { visit(it) }
        val func = context.getFunction(name)
        return when {
            name == "println" -> {
                out.println(args.joinToString(" "))
                0
            }
            func != null -> evaluateFunction(func, args, ctx.start.line)
            else -> throw UndefinedFunctionException("Undefined function", ctx.start.line)
        }
    }
}
