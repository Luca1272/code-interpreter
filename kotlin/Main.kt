import ast.RealValue
import interpreter.*
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.system.exitProcess
fun main(args: Array<String>) {
    if (args.size < 2) {
        System.err.println("Usage: ./your_program.sh tokenize <filename>")
        exitProcess(1)
    }
    val command = args[0]
    val filename = args[1]
    when (command) {
        "tokenize" -> {
            val fileContents = File(filename).readText()
            var returnValue = 0
            if (fileContents.isNotEmpty()) {
                interpret(fileContents) { _, _, message ->
                    System.err.println(message)
                    returnValue = 65
                }.forEach { token ->
                    println("${token.type} ${token.lexeme} ${token.literal}")
                }
            } else {
                println("EOF  null")
            }
            exitProcess(returnValue)
        }
        "parse" -> {
            val fileContents = File(filename).readText()
            try {
                val expression = interpret(fileContents).iterator().peeking().nextExpression()
                println(expression.format())
            } catch (e: Exception) {
                System.err.println(e.message)
                exitProcess(65)
            }
        }
        "evaluate" -> {
            val fileContents = File(filename).readText()
            try {
                val expression = interpret(fileContents).iterator().peeking().nextExpression()
                println(
                    when (val value = expression.evaluate(NullContext)) {
                        is RealValue -> {
                            BigDecimal(value.value).setScale(10, RoundingMode.HALF_UP).stripTrailingZeros()
                                .toPlainString()
                        }
                        else -> {
                            value.format()
                        }
                    }
                )
            } catch (e: ParseException) {
                System.err.println(e.message)
                exitProcess(65)
            } catch (e: EvaluationException) {
                System.err.println(e.message)
                exitProcess(70)
            }
        }
        "run" -> {
            val fileContents = File(filename).readText()
            try {
                val context = DefaultInterpreterContext()
                interpret(fileContents).parse().forEach { statement ->
                    statement.evaluate(context)
                }
            } catch (e: ParseException) {
                System.err.println(e.message)
                exitProcess(65)
            } catch (e: EvaluationException) {
                System.err.println(e.message)
                exitProcess(70)
            }
        }
        else -> {
            System.err.println("Unknown command: $command")
            exitProcess(1)
        }
    }
}