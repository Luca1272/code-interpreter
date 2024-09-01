package ast

sealed interface Value {
    fun format(): String
}

data class StringValue(val value: String) : Value {
    override fun format(): String {
        return value
    }
}

data class RealValue(val value: Double) : Value {
    override fun format(): String {
        return value.toString()
    }
}

data class BooleanValue(val value: Boolean) : Value {
    override fun format(): String {
        return value.toString()
    }
}

object NilValue : Value {
    override fun format(): String {
        return "nil"
    }
}