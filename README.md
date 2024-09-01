# Lox Interpreter in Kotlin

This project implements an interpreter for the Lox programming language using Kotlin. Lox is a simple, dynamically-typed language designed for learning about interpreter implementation, as described in the book "Crafting Interpreters" by Robert Nystrom.

## Features

- **Full implementation of the Lox language**
- **Tokenization** of Lox source code
- **Parsing** of Lox expressions and statements
- **Interpretation and execution** of Lox programs
- Support for Lox data types: `nil`, `boolean`, `number`, and `string`
- **Variable declaration and assignment**
- **Control flow:** `if` statements and `while/for` loops
- **Functions and closures**
- **Classes and inheritance**

## Project Structure

The project consists of several Kotlin files:

- **`Main.kt`:** Contains the main entry point and command-line interface for the interpreter.
- **`Interpreter.kt`:** Implements the core interpreter functionality, including tokenization and parsing.
- **`ASTNode.kt`:** Defines the Abstract Syntax Tree (AST) nodes used to represent the Lox program structure.
- **`Operator.kt`:** Implements various operators supported by Lox.
- **`Value.kt`:** Defines the value types supported by Lox.

## Usage

The interpreter supports four main commands:

- **`tokenize`:** Tokenize the input file and print the tokens.
- **`parse`:** Parse the input file and print the AST.
- **`evaluate`:** Evaluate the expression in the input file and print the result.
- **`run`:** Execute the Lox program in the input file.

To use the interpreter, run the following command:
```sh
./your_program.sh <command> <filename>
```
Replace <command> with one of the supported commands (tokenize, parse, evaluate, or run) and <filename> with the path to your Lox source file.

##Lox Language Features
-Variable declaration and assignment: var x = 10;
-Arithmetic operations: +, -, *, /
-Comparison operations: ==, !=, <, <=, >, >=
-Logical operations: and, or, !
-Control flow: if, else, while, for
-Functions: fun name(params) { ... }
-Classes and methods: class Name { ... }
-Inheritance: class Child < Parent { ... }
-Print statements: print "Hello, world!";
-Native functions: clock()
-Example Lox Program
-Here's a simple example of a Lox program:

```lox
class Greeting {
  init(name) {
    this.name = name;
  }

  sayHello() {
    print "Hello, " + this.name + "!";
  }
}

var greeting = Greeting("Lox");
greeting.sayHello();
```
Error Handling
The interpreter includes error handling for various scenarios:

Lexical errors during tokenization
Syntax errors during parsing
Runtime errors during execution
Undefined variables or functions
Type mismatches in operations
Future Improvements
Potential areas for enhancement include:

Implementing a REPL (Read-Eval-Print Loop) for interactive use
Adding more built-in functions and standard library features
Improving error reporting with more detailed information
Optimizing the interpreter for better performance
Adding support for modules or imports
Implementing static type checking as an optional feature
Contributing
Contributions to improve the Lox interpreter are welcome. Please feel free to submit issues or pull requests on the project repository.

Acknowledgments
This interpreter is based on the Lox language designed by Robert Nystrom for his book "Crafting Interpreters". The implementation draws inspiration from the book while being adapted for Kotlin.
