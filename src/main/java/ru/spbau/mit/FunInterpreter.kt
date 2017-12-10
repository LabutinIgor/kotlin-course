package ru.spbau.mit

import ru.spbau.mit.parser.FunBaseVisitor
import ru.spbau.mit.parser.FunParser

class FunInterpreter(private val context: Context = Context(), private val out: java.io.PrintStream = System.out) :
        FunBaseVisitor<Int?>() {
    override fun visitFile(ctx: FunParser.FileContext): Int? = visitBlock(ctx.block())

    override fun visitBlock(ctx: FunParser.BlockContext): Int? {
        context.enterScope()
        for (statement in ctx.statement()) {
            val res = visit(statement)
            if (res != null) {
                context.leaveScope()
                return res
            }
        }
        context.leaveScope()
        return null
    }

    override fun visitExpressionStatement(ctx: FunParser.ExpressionStatementContext): Int? {
        visit(ctx.expression())
        return null
    }

    override fun visitBlockWithBraces(ctx: FunParser.BlockWithBracesContext): Int? = visit(ctx.block())

    override fun visitFunction(ctx: FunParser.FunctionContext): Int? {
        try {
            context.addFunction(ctx.IDENTIFIER().text, ctx)
        } catch (e: FunException) {
            throw FunException(e.message, ctx.start.line)
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
        try {
            context.addVariable(name = name.text, value = value)
        } catch (e: FunException) {
            throw FunException(e.message, ctx.start.line)
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
        try {
            context.setVariable(ctx.IDENTIFIER().text, visit(ctx.expression()))
        } catch (e: FunException) {
            throw FunException(e.message, ctx.start.line)
        }
        return null
    }

    override fun visitReturnStatement(ctx: FunParser.ReturnStatementContext): Int? = visit(ctx.expression())

    private fun applyBinaryOperation(leftRes: Int, rightRes: Int, operation: String, line: Int): Int {
        return when (operation) {
            "||" -> if (leftRes != 0 || rightRes != 0) 1 else 0
            "&&" -> if (leftRes != 0 && rightRes != 0) 1 else 0
            "!=" -> if (leftRes != rightRes) 1 else 0
            "==" -> if (leftRes == rightRes) 1 else 0
            ">=" -> if (leftRes >= rightRes) 1 else 0
            "<=" -> if (leftRes <= rightRes) 1 else 0
            ">" -> if (leftRes > rightRes) 1 else 0
            "<" -> if (leftRes < rightRes) 1 else 0
            "-" -> leftRes - rightRes
            "+" -> leftRes + rightRes
            "%" ->
                if (rightRes == 0) {
                    throw DivisionByZeroException("Division by zero", line)
                } else {
                    leftRes % rightRes
                }
            "/" ->
                if (rightRes == 0) {
                    throw DivisionByZeroException("Division by zero", line)
                } else {
                    leftRes / rightRes
                }
            "*" -> leftRes * rightRes
            else -> 0
        }
    }

    override fun visitExpression(ctx: FunParser.ExpressionContext): Int {
        return if (ctx.atomicExpression() != null) {
            visitAtomicExpression(ctx.atomicExpression())
        } else {
            val leftRes = visitExpression(ctx.expression(0))
            val rightRes = visitExpression(ctx.expression(1))
            applyBinaryOperation(leftRes, rightRes, ctx.BIN_OP().text, ctx.start.line)
        }
    }

    override fun visitAtomicExpression(ctx: FunParser.AtomicExpressionContext): Int = when {
        ctx.functionCall() != null -> visitFunctionCall(ctx.functionCall())
        ctx.IDENTIFIER() != null ->
            try {
                context.getVariable(ctx.IDENTIFIER().text)
            } catch (e: FunException) {
                throw FunException(e.message, ctx.start.line)
            }
        ctx.LITERAL() != null -> {
            try {
                ctx.LITERAL().text.toInt()
            } catch (e: NumberFormatException) {
                throw NumberOverflowException("Numeric overflow", ctx.start.line)
            }
        }
        ctx.expression() != null -> visitExpression(ctx.expression())
        else -> throw FunException("Undefined expression", ctx.start.line)
    }

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
        return if (name == "println") {
            out.println(args.joinToString(" "))
            0
        } else {
            try {
                val function = context.getFunction(name)
                evaluateFunction(function, args, ctx.start.line)
            } catch (e: FunException) {
                throw FunException(e.message, ctx.start.line)
            }
        }
    }
}
