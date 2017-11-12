package ru.spbau.mit

import ru.spbau.mit.parser.FunBaseVisitor
import ru.spbau.mit.parser.FunParser

class FunInterpreter(private val context: Context = Context(), private val out: java.io.PrintStream = System.out) :
        FunBaseVisitor<Int?>() {
    override fun visitFile(ctx: FunParser.FileContext): Int? {
        return visitBlock(ctx.block())
    }

    override fun visitBlock(ctx: FunParser.BlockContext): Int? {
        context.enterScope()
        for (child in ctx.children) {
            val res = visit(child)
            if (res != null) {
                context.leaveScope()
                return res
            }
        }
        context.leaveScope()
        return null
    }

    override fun visitBlockWithBraces(ctx: FunParser.BlockWithBracesContext): Int? {
        return visitBlock(ctx.block())
    }

    override fun visitFunction(ctx: FunParser.FunctionContext): Int? {
        try {
            context.addFunction(ctx.IDENTIFIER().text, ctx)
        } catch (e: RedefineFunctionException) {
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
        try {
            context.addVariable(name = name.text, value = value)
        } catch (e: RedefineVariableException) {
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
        try {
            context.setVariable(ctx.IDENTIFIER().text, visit(ctx.expression()))
        } catch (e: UndefinedVariableException) {
            throw UndefinedVariableException("Undefined variable", ctx.start.line)
        }
        return null
    }

    override fun visitReturnStatement(ctx: FunParser.ReturnStatementContext): Int? {
        return visit(ctx.expression())
    }

    private fun applyBinaryOperation(leftRes: Int?, rightRes: Int?, operation: String): Int? {
        if (leftRes == null || rightRes == null) {
            return null
        }
        return when (operation) {
            "||" ->
                if (leftRes != 0 || rightRes != 0) 1 else 0
            "&&" ->
                if (leftRes != 0 && rightRes != 0) 1 else 0

            "!=" ->
                if (leftRes != rightRes) 1 else 0
            "==" ->
                if (leftRes == rightRes) 1 else 0

            ">=" ->
                if (leftRes >= rightRes) {
                    1
                } else {
                    0
                }
            "<=" ->
                if (leftRes <= rightRes) 1 else 0
            ">" ->
                if (leftRes > rightRes) 1 else 0
            "<" ->
                if (leftRes < rightRes) 1 else 0
            "-" ->
                leftRes - rightRes
            "+" ->
                leftRes + rightRes
            "%" ->
                if (rightRes == 0) {
                    throw DivisionByZeroException("Division by zero")
                } else {
                    leftRes % rightRes
                }
            "/" ->
                if (rightRes == 0) {
                    throw DivisionByZeroException()
                } else {
                    leftRes % rightRes
                }
            "*" -> leftRes * rightRes
            else -> 0
        }
    }

    override fun visitExpression(ctx: FunParser.ExpressionContext): Int? {
        val leftRes = visit(ctx.expressionPriority5())
        return if (ctx.childCount == 1) {
            leftRes
        } else {
            val rightRes = visit(ctx.expression())
            applyBinaryOperation(leftRes, rightRes, ctx.BIN_OP_PRIORITY6().text)
        }
    }

    override fun visitExpressionPriority5(ctx: FunParser.ExpressionPriority5Context): Int? {
        val leftRes = visit(ctx.expressionPriority4())
        return if (ctx.childCount == 1) {
            leftRes
        } else {
            val rightRes = visit(ctx.expressionPriority5())
            applyBinaryOperation(leftRes, rightRes, ctx.BIN_OP_PRIORITY5().text)
        }
    }

    override fun visitExpressionPriority4(ctx: FunParser.ExpressionPriority4Context): Int? {
        val leftRes = visit(ctx.expressionPriority3())
        return if (ctx.childCount == 1) {
            leftRes
        } else {
            val rightRes = visit(ctx.expressionPriority4())
            applyBinaryOperation(leftRes, rightRes, ctx.BIN_OP_PRIORITY4().text)
        }
    }

    override fun visitExpressionPriority3(ctx: FunParser.ExpressionPriority3Context): Int? {
        val leftRes = visit(ctx.expressionPriority2())
        return if (ctx.childCount == 1) {
            leftRes
        } else {
            val rightRes = visit(ctx.expressionPriority3())
            applyBinaryOperation(leftRes, rightRes, ctx.BIN_OP_PRIORITY3().text)
        }
    }

    override fun visitExpressionPriority2(ctx: FunParser.ExpressionPriority2Context): Int? {
        val leftRes = visit(ctx.expressionPriority1())
        return if (ctx.childCount == 1) {
            leftRes
        } else {
            val rightRes = visit(ctx.expressionPriority2())
            applyBinaryOperation(leftRes, rightRes, ctx.BIN_OP_PRIORITY2().text)
        }
    }

    override fun visitExpressionPriority1(ctx: FunParser.ExpressionPriority1Context): Int? {
        val leftRes = visit(ctx.atomicExpression())
        return if (ctx.childCount == 1) {
            leftRes
        } else {
            val rightRes = visit(ctx.expressionPriority1())
            applyBinaryOperation(leftRes, rightRes, ctx.BIN_OP_PRIORITY1().text)
        }
    }

    override fun visitAtomicExpression(ctx: FunParser.AtomicExpressionContext): Int? {
        return when {
            ctx.functionCall() != null -> visit(ctx.functionCall())
            ctx.IDENTIFIER() != null ->
                try {
                    context.getVariable(ctx.IDENTIFIER().text)
                } catch (e: UndefinedVariableException) {
                    throw UndefinedVariableException("Undefined variable", ctx.start.line)
                }
            ctx.LITERAL() != null -> {
                try {
                    ctx.LITERAL().text.toInt()
                } catch (e: NumberFormatException) {
                    throw NumberOverflowException("Number overflow", ctx.start.line)
                }
            }
            ctx.expression() != null -> visit(ctx.expression())
            else -> null
        }
    }

    private fun evaluateFunction(function: FunParser.FunctionContext, args: MutableList<Int?>): Int? {
        val names: MutableList<String> = mutableListOf()
        function.parameterNames().IDENTIFIER().mapTo(names) { it.text }
        if (names.size != args.size) {
            throw IncorrectNumberOfArgsException("Incorrect number of arguments")
        }

        context.enterScope()
        for (i in 0 until names.size) {
            context.addVariable(names[i], args[i])
        }
        val res = visit(function.blockWithBraces())
        context.leaveScope()
        return res ?: 0
    }

    override fun visitFunctionCall(ctx: FunParser.FunctionCallContext): Int? {
        val name = ctx.IDENTIFIER().text
        val args: MutableList<Int?> = mutableListOf()
        ctx.arguments().expression().mapTo(args) { visit(it) }
        return if (name == "println") {
            out.println(args.joinToString(" "))
            null
        } else {
            try {
                val function = context.getFunction(name)
                evaluateFunction(function, args)
            } catch (e: UndefinedFunctionException) {
                throw UndefinedFunctionException("Undefined function", ctx.start.line)
            }
        }
    }
}
