package interpreter
import ast.NilValue
import ast.Value
interface Context {
    fun pushScope(name: String)
    fun popScope()
    fun declareVariable(name: String)
    fun assignVariable(name: String, value: Value)
    fun getVariable(name: String): Value?
}
object NullContext : Context {
    override fun pushScope(name: String) {}
    override fun popScope() {}
    override fun declareVariable(name: String) {}
    override fun assignVariable(name: String, value: Value) {}
    override fun getVariable(name: String): Value? = null
}
class DefaultInterpreterContext : Context {
    private data class Scope(val name: String, val variables: MutableMap<String, Value>)
    private val scopes = mutableListOf(Scope("global", mutableMapOf()))
    override fun pushScope(name: String) {
        scopes.add(Scope(name, mutableMapOf()))
    }
    override fun popScope() {
        if (scopes.size == 1) {
            throw EvaluationException("Cannot pop the global scope")
        }
        scopes.removeLast()
    }
    override fun declareVariable(name: String) {
        scopes.last().variables.computeIfAbsent(name) { NilValue }
    }
    override fun assignVariable(name: String, value: Value) {
        scopes.last().variables[name] = value
    }
    override fun getVariable(name: String): Value? {
        for (scope in scopes.asReversed()) {
            val value = scope.variables[name]
            if (value != null) {
                return value
            }
        }
        return null
    }
}