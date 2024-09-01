package ast
import interpreter.Context
import interpreter.EvaluationException
sealed interface Operator
sealed interface UnaryOperator : Operator {
    fun format(): String
    fun evaluate(operand: Value): Value
}
data object Positive : UnaryOperator {
    override fun format(): String = "+"
    override fun evaluate(operand: Value): Value = when (operand) {
        is RealValue -> operand
        else -> throw EvaluationException("Cannot convert $operand to a number")
    }
}
data object Negative : UnaryOperator {
    override fun format(): String = "-"
    override fun evaluate(operand: Value): Value = when (operand) {
        is RealValue -> RealValue(-operand.value)
        else -> throw EvaluationException("Cannot convert $operand to a number")
    }
}
data object LogicalNot : UnaryOperator {
    override fun format(): String = "!"
    override fun evaluate(operand: Value): Value = when (operand) {
        is BooleanValue -> BooleanValue(!operand.value)
        is RealValue -> BooleanValue(operand.value == 0.0)
        is NilValue -> BooleanValue(true)
        else -> throw EvaluationException("Cannot convert $operand to a boolean")
    }
}
data object Group : UnaryOperator {
    override fun format(): String = "group"
    override fun evaluate(operand: Value): Value = operand
}
sealed interface BinaryOperator : Operator {
    fun format(): String
    fun isLeftAssociative(): Boolean = true
    fun evaluate(left: Value, right: Value): Value
}
data object Plus : BinaryOperator {
    override fun format(): String = "+"
    override fun evaluate(left: Value, right: Value): Value {
        if (left is StringValue && right is StringValue) {
            return StringValue(left.value + right.value)
        }
        if (left is RealValue && right is RealValue) {
            return RealValue(left.value + right.value)
        }
        throw EvaluationException("Cannot add $left and $right")
    }
}
data object Minus : BinaryOperator {
    override fun format(): String = "-"
    override fun evaluate(left: Value, right: Value): Value {
        if (left is RealValue && right is RealValue) {
            return RealValue(left.value - right.value)
        }
        throw EvaluationException("Cannot subtract $right from $left")
    }
}
data object Times : BinaryOperator {
    override fun format(): String = "*"
    override fun evaluate(left: Value, right: Value): Value {
        if (left is RealValue && right is RealValue) {
            return RealValue(left.value * right.value)
        }
        throw EvaluationException("Cannot multiply $left and $right")
    }
}
data object Div : BinaryOperator {
    override fun format(): String = "/"
    override fun evaluate(left: Value, right: Value): Value {
        if (left is RealValue && right is RealValue) {
            return RealValue(left.value / right.value)
        }
        throw EvaluationException("Cannot divide $left by $right")
    }
}
data object EqualTo : BinaryOperator {
    override fun format(): String = "=="
    override fun evaluate(left: Value, right: Value): Value {
        return BooleanValue(left == right)
    }
}
data object NotEqualTo : BinaryOperator {
    override fun format() = "!="
    override fun evaluate(left: Value, right: Value): Value {
        return BooleanValue(left != right)
    }
}
data object LessThan : BinaryOperator {
    override fun format(): String = "<"
    override fun evaluate(left: Value, right: Value): Value {
        if (left is RealValue && right is RealValue) {
            return BooleanValue(left.value < right.value)
        }
        throw EvaluationException("Cannot compare $left < $right")
    }
}
data object GreaterThan : BinaryOperator {
    override fun format(): String = ">"
    override fun evaluate(left: Value, right: Value): Value {
        if (left is RealValue && right is RealValue) {
            return BooleanValue(left.value > right.value)
        }
        throw EvaluationException("Cannot compare $left > $right")
    }
}
data object LessThanOrEqualTo : BinaryOperator {
    override fun format(): String = "<="
    override fun evaluate(left: Value, right: Value): Value {
        if (left is RealValue && right is RealValue) {
            return BooleanValue(left.value <= right.value)
        }
        throw EvaluationException("Cannot compare $left <= $right")
    }
}
data object GreaterThanOrEqualTo : BinaryOperator {
    override fun format(): String = ">="
    override fun evaluate(left: Value, right: Value): Value {
        if (left is RealValue && right is RealValue) {
            return BooleanValue(left.value >= right.value)
        }
        throw EvaluationException("Cannot compare $left >= $right")
    }
}
data object LogicalOr : BinaryOperator {
    override fun format(): String = "||"
    override fun evaluate(left: Value, right: Value): Value {
        if (left is BooleanValue && right is BooleanValue) {
            return BooleanValue(left.value || right.value)
        }
        throw EvaluationException("Cannot apply || to $left and $right")
    }
}
data object LogicalAnd : BinaryOperator {
    override fun format(): String = "&&"
    override fun evaluate(left: Value, right: Value): Value {
        if (left is BooleanValue && right is BooleanValue) {
            return BooleanValue(left.value && right.value)
        }
        throw EvaluationException("Cannot apply && to $left and $right")
    }
}
data object AssignmentOperator : Operator {
    fun format(): String = "="
    fun evaluate(context: Context, variable: String, value: Value): Value {
        context.assignVariable(variable, value)
        return value
    }
}