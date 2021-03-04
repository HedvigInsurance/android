package com.hedvig.onboarding.createoffer.computedvalues

object TemplateExpressionCalculator {

    private val VOID_EXPR = Regex("^\\s+")
    private val BIN_OPERATOR_EXPR = Regex("^(-|\\+\\+|\\+)")
    private val STORE_KEY_EXPR = Regex("^([a-zA-Z][\\w\\d]*)")
    private val NUMBER_CONSTANT_EXPR = Regex("^(\\d+(\\.\\d+)?)")
    private val DOUBLE_QUOTE_STRING_CONSTANT_EXPR = Regex("^\"([^\"]*)\"")
    private val SINGLE_QUOTE_STRING_CONSTANT_EXPR = Regex("^'([^']*)'")

    private val tokenCheckers: List<Pair<TokenType, Regex>> = listOf(
        TokenType.BINARY_OPERATOR to BIN_OPERATOR_EXPR,
        TokenType.STORE_KEY to STORE_KEY_EXPR,
        TokenType.NUMBER_CONSTANT to NUMBER_CONSTANT_EXPR,
        TokenType.STRING_CONSTANT to DOUBLE_QUOTE_STRING_CONSTANT_EXPR,
        TokenType.STRING_CONSTANT to SINGLE_QUOTE_STRING_CONSTANT_EXPR,
    )

    fun evaluateTemplateExpression(expression: String, store: HashMap<String, String>): String = try {
        val tokenStream = parseTokenStream(expression)
        val abstractExpression = parseAbstractExpression(tokenStream)
        evaluateAbstractExpression(abstractExpression, store)
    } catch (e: Exception) {
        e.message.toString()
    }

    private fun parseTokenStream(expression: String): List<Token> {
        var cursor = 0
        val tokenStream = mutableListOf<Token>()

        while (cursor < expression.length) {
            val subExpr = expression.substring(cursor, expression.length)
            if (VOID_EXPR.containsMatchIn(subExpr)) {
                cursor += VOID_EXPR.find(subExpr)?.groups?.get(0)?.value?.length ?: subExpr.length
                continue
            }

            val matchingTokenChecker = tokenCheckers.find {
                val regex = it.second
                regex.containsMatchIn(subExpr)
            } ?: throw NoValidTokenCheckerException("Invalid expression from $subExpr")

            val matchResult = matchingTokenChecker.second.find(subExpr)

            tokenStream.add(Token(
                matchingTokenChecker.first,
                payload = matchResult?.value ?: ""
            ))

            cursor += matchResult?.groups?.get(0)?.value?.length ?: subExpr.length
        }

        return tokenStream
    }

    private fun parseAbstractExpression(tokenStream: List<Token>): Expression {
        var tokenStreamCursor = 0
        var abstractExpression: Expression? = null

        while (tokenStreamCursor < tokenStream.size) {
            val token = tokenStream[tokenStreamCursor]

            when (token.type) {
                TokenType.BINARY_OPERATOR -> {
                    if (abstractExpression == null) {
                        throw UnexpectedTokenOperator("Unexpected token operator ${token.payload}")
                    }

                    abstractExpression = Expression.BinaryOperatorExpression(
                        operatorField = token.payload,
                        left = abstractExpression,
                        right = null
                    )
                }
                TokenType.STORE_KEY -> {
                    val storeKeyExpression = Expression.StoreKeyExpression(key = token.payload)

                    abstractExpression = if (abstractExpression != null) {
                        if (abstractExpression is Expression.BinaryOperatorExpression) {
                            abstractExpression.copy(right = storeKeyExpression)
                        } else {
                            throw UnexpectedTokenOperator("Unexpected token ${token.payload}")
                        }
                    } else {
                        storeKeyExpression
                    }
                }
                TokenType.NUMBER_CONSTANT -> {
                    val numberExpression = Expression.NumberConstantExpression(token.payload.toFloat())

                    abstractExpression = if (abstractExpression != null) {
                        if (abstractExpression is Expression.BinaryOperatorExpression) {
                            abstractExpression.copy(right = numberExpression)
                        } else {
                            throw UnexpectedTokenOperator("Unexpected number constant \"${token.payload}\"")
                        }
                    } else {
                        numberExpression
                    }
                }
                TokenType.STRING_CONSTANT -> {
                    val stringConstantExpression = Expression.StringConstantExpression(constant = token.payload)

                    abstractExpression = if (abstractExpression != null) {
                        if (abstractExpression is Expression.BinaryOperatorExpression) {
                            abstractExpression.copy(right = stringConstantExpression)
                        } else {
                            throw UnexpectedTokenOperator("Unexpected string constant ${token.payload}")
                        }
                    } else {
                        stringConstantExpression
                    }
                }
            }

            tokenStreamCursor += 1
        }

        return abstractExpression
            ?: throw NoExpressionMatch("Could not create an abstract expression from $tokenStream")
    }

    private fun evaluateAbstractExpression(expression: Expression, store: HashMap<String, String>): String = when (expression) {
        is Expression.StoreKeyExpression -> store[expression.key] ?: ""
        is Expression.NumberConstantExpression -> expression.constant.toBigDecimal().stripTrailingZeros().toString()
        is Expression.StringConstantExpression -> expression.constant
        is Expression.BinaryOperatorExpression -> {
            if (expression.left == null || expression.right == null) {
                throw InvalidOperator("Invalid use of operator \"${expression.operatorField}\", must have expressions on both sides")
            } else {
                val left: Expression = expression.left
                val right: Expression = expression.right
                when (expression.operatorField) {
                    "+" -> (evaluateAbstractExpression(left, store).toFloat() + evaluateAbstractExpression(right, store).toFloat())
                        .toBigDecimal()
                        .stripTrailingZeros()
                        .toString()
                    "-" -> (evaluateAbstractExpression(left, store).toFloat() - evaluateAbstractExpression(right, store).toFloat())
                        .toBigDecimal()
                        .stripTrailingZeros()
                        .toString()
                    "++" -> (evaluateAbstractExpression(left, store) + evaluateAbstractExpression(right, store))
                        .replace("\"", "")
                        .replace("\'", "")
                    else -> throw InvalidOperator(expression.operatorField)
                }
            }
        }
    }

    enum class TokenType {
        BINARY_OPERATOR, STORE_KEY, STRING_CONSTANT, NUMBER_CONSTANT
    }

    data class Token(
        val type: TokenType,
        val payload: String
    )

    sealed class Expression {

        data class BinaryOperatorExpression(
            val left: Expression?,
            val right: Expression?,
            val operatorField: String
        ) : Expression()

        data class StringConstantExpression(
            val constant: String
        ) : Expression()

        data class NumberConstantExpression(
            val constant: Float
        ) : Expression()

        data class StoreKeyExpression(
            val key: String
        ) : Expression()
    }

    class NoValidTokenCheckerException(override val message: String) : Exception(message)
    class UnexpectedTokenOperator(override val message: String) : Exception(message)
    class InvalidOperator(override val message: String) : Exception(message)
    class NoExpressionMatch(override val message: String) : Exception(message)
}
