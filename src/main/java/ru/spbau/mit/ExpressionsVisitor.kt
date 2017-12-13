package ru.spbau.mit

import ru.spbau.mit.parser.FunBaseVisitor
import ru.spbau.mit.parser.FunParser

class ExpressionsVisitor(private val funInterpreter: FunInterpreter, private val context: Context,
                         private val out: java.io.PrintStream) :
        FunBaseVisitor<Int>() {
    override fun visitExprPriority1(ctx: FunParser.ExprPriority1Context): Int {
        val leftRes = visit(ctx.expression(0))
        val rightRes = visit(ctx.expression(1))
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
        val leftRes = visit(ctx.expression(0))
        val rightRes = visit(ctx.expression(1))
        return when (ctx.op.text) {
            "-" -> leftRes - rightRes
            "+" -> leftRes + rightRes
            else -> throw UndefinedOperationException("Undefined operation", ctx.start.line)
        }
    }

    override fun visitExprPriority3(ctx: FunParser.ExprPriority3Context): Int {
        val leftRes = visit(ctx.expression(0))
        val rightRes = visit(ctx.expression(1))
        return when (ctx.op.text) {
            ">=" -> if (leftRes >= rightRes) 1 else 0
            "<=" -> if (leftRes <= rightRes) 1 else 0
            ">" -> if (leftRes > rightRes) 1 else 0
            "<" -> if (leftRes < rightRes) 1 else 0
            else -> throw UndefinedOperationException("Undefined operation", ctx.start.line)
        }
    }

    override fun visitExprPriority4(ctx: FunParser.ExprPriority4Context): Int {
        val leftRes = visit(ctx.expression(0))
        val rightRes = visit(ctx.expression(1))
        return when (ctx.op.text) {
            "!=" -> if (leftRes != rightRes) 1 else 0
            "==" -> if (leftRes == rightRes) 1 else 0
            else -> throw UndefinedOperationException("Undefined operation", ctx.start.line)
        }
    }

    override fun visitExprPriority5(ctx: FunParser.ExprPriority5Context): Int {
        val leftRes = visit(ctx.expression(0))
        val rightRes = visit(ctx.expression(1))
        return when (ctx.op.text) {
            "&&" -> if (leftRes != 0 && rightRes != 0) 1 else 0
            else -> throw UndefinedOperationException("Undefined operation", ctx.start.line)
        }
    }

    override fun visitExprPriority6(ctx: FunParser.ExprPriority6Context): Int {
        val leftRes = visit(ctx.expression(0))
        val rightRes = visit(ctx.expression(1))
        return when (ctx.op.text) {
            "||" -> if (leftRes != 0 || rightRes != 0) 1 else 0
            else -> throw UndefinedOperationException("Undefined operation", ctx.start.line)
        }
    }

    override fun visitExprUnaryMinus(ctx: FunParser.ExprUnaryMinusContext): Int = -visit(ctx.expression())

    override fun visitExprUnaryPlus(ctx: FunParser.ExprUnaryPlusContext): Int = -visit(ctx.expression())

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

    override fun visitBracketedExpression(ctx: FunParser.BracketedExpressionContext): Int = visit(ctx.expression())

    override fun visitFunctionCall(ctx: FunParser.FunctionCallContext): Int {
        val name = ctx.IDENTIFIER().text
        val args = ctx.arguments().expression().map { visit(it) }
        val func = context.getFunction(name)
        return when {
            name == "println" -> {
                out.println(args.joinToString(" "))
                0
            }
            func != null -> funInterpreter.evaluateFunction(func, args, ctx.start.line)
            else -> throw UndefinedFunctionException("Undefined function", ctx.start.line)
        }
    }
}