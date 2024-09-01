package ast
import interpreter.Context
import interpreter.EvaluationException
import java.sql.Statement
sealed interface ASTNode {
    fun format(): String
    fun evaluate(context: Context): Value
}
data class UnaryOperatorNode(val operator: UnaryOperator, val operand: ASTNode) : ASTNode {
    override fun format(): String {
        return "(${operator.format()} ${operand.format()})"
    }
    override fun evaluate(context: Context): Value {
        return operator.evaluate(operand.evaluate(context))
    }
}
data class BinaryOperatorNode(val operator: BinaryOperator, val left: ASTNode, val right: ASTNode) : ASTNode {
    override fun format(): String {
        return "(${operator.format()} ${left.format()} ${right.format()})"
    }
    override fun evaluate(context: Context): Value {
        return operator.evaluate(left.evaluate(context), right.evaluate(context))
    }
}
data class AssignmentOperatorNode(val variable: String, val value: ASTNode) : ASTNode {
    override fun format(): String {
        return "$variable = ${value.format()}"
    }
    override fun evaluate(context: Context): Value {
        val value = value.evaluate(context)
        context.assignVariable(variable, value)
        return value
    }
}
interface StatementNode: ASTNode
data class SingleStatementNode(val node: ASTNode) : StatementNode {
    override fun format(): String = "${node.format()};"
    override fun evaluate(context: Context): Value {
        return node.evaluate(context)
    }
}
data class BlockStatementNode(val statements: List<StatementNode>) : StatementNode {
    override fun format(): String {
        val statementsString = statements.joinToString("\n") { it.format() }
        return "{\n${statementsString.lines().joinToString("\n") { "  $it" }}\n}"
    }
    override fun evaluate(context: Context): Value {
        context.pushScope("")
        var result: Value = NilValue
        for (statement in statements) {
            result = statement.evaluate(context)
        }
        context.popScope()
        return result
    }
}
data class CallableNode(
    val name: String,
    val arguments: List<ASTNode>,
    val operation: (context: Context, List<Value>) -> Value
) : ASTNode {
    override fun format(): String {
        return "$name(${arguments.joinToString(", ") { it.format() }})"
    }
    override fun evaluate(context: Context): Value {
        return operation(context, arguments.map { it.evaluate(context) })
    }
}
data class ValueNode(val value: Value) : ASTNode {
    override fun format() = value.format()
    override fun evaluate(context: Context): Value {
        return value
    }
}
data class VariableNode(val name: String) : ASTNode {
    override fun format() = name
    override fun evaluate(context: Context): Value {
        return context.getVariable(name) ?: throw EvaluationException("Variable $name is not defined")
    }
}