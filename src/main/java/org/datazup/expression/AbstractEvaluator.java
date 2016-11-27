package org.datazup.expression;


import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.datazup.pathextractor.AbstractVariableSet;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by ninel on 3/14/16.
 */
public abstract class AbstractEvaluator<T> {
    private final Tokenizer tokenizer;
    private final Map<String, Function> functions;
    private final Map<String, List<Operator>> operators;
    private final Map<String, Constant> constants;
    private final String functionArgumentSeparator;
    private final Map<String, BracketPair> functionBrackets;
    private final Map<String, BracketPair> expressionBrackets;

    protected AbstractEvaluator(Parameters parameters) {
        ArrayList tokenDelimitersBuilder = new ArrayList();
        this.functions = new HashMap();
        this.operators = new HashMap();
        this.constants = new HashMap();
        this.functionBrackets = new HashMap();
        Iterator needFunctionSeparator = parameters.getFunctionBrackets().iterator();

        BracketPair i$;
        while (needFunctionSeparator.hasNext()) {
            i$ = (BracketPair) needFunctionSeparator.next();
            this.functionBrackets.put(i$.getOpen(), i$);
            this.functionBrackets.put(i$.getClose(), i$);
            tokenDelimitersBuilder.add(i$.getOpen());
            tokenDelimitersBuilder.add(i$.getClose());
        }

        this.expressionBrackets = new HashMap();
        needFunctionSeparator = parameters.getExpressionBrackets().iterator();

        while (needFunctionSeparator.hasNext()) {
            i$ = (BracketPair) needFunctionSeparator.next();
            this.expressionBrackets.put(i$.getOpen(), i$);
            this.expressionBrackets.put(i$.getClose(), i$);
            tokenDelimitersBuilder.add(i$.getOpen());
            tokenDelimitersBuilder.add(i$.getClose());
        }

        if (this.operators != null) {
            needFunctionSeparator = parameters.getOperators().iterator();

            while (needFunctionSeparator.hasNext()) {
                Operator i$1 = (Operator) needFunctionSeparator.next();
                tokenDelimitersBuilder.add(i$1.getSymbol());
                List constant = (List) this.operators.get(i$1.getSymbol());
                if (constant == null) {
                    constant = new ArrayList();
                    this.operators.put(i$1.getSymbol(), constant);
                }

                ((List) constant).add(i$1);
                if (((List) constant).size() > 1) {
                    this.validateHomonyms((List) constant);
                }
            }
        }

        boolean needFunctionSeparator1 = false;
        Iterator i$2;
        if (parameters.getFunctions() != null) {
            i$2 = parameters.getFunctions().iterator();

            while (i$2.hasNext()) {
                Function constant1 = (Function) i$2.next();
                this.functions.put(parameters.getTranslation(constant1.getName()), constant1);
                if (constant1.getMaximumArgumentCount() > 1) {
                    needFunctionSeparator1 = true;
                }
            }
        }

        if (parameters.getConstants() != null) {
            i$2 = parameters.getConstants().iterator();

            while (i$2.hasNext()) {
                Constant constant2 = (Constant) i$2.next();
                this.constants.put(parameters.getTranslation(constant2.getName()), constant2);
            }
        }

        this.functionArgumentSeparator = parameters.getFunctionArgumentSeparator();
        if (needFunctionSeparator1) {
            tokenDelimitersBuilder.add(this.functionArgumentSeparator);
        }

        this.tokenizer = new Tokenizer(tokenDelimitersBuilder);
    }

    protected void validateHomonyms(List<Operator> operators) {
        if (operators.size() > 2) {
            throw new IllegalArgumentException();
        }
    }

    protected Operator guessOperator(Token previous, List<Operator> candidates) {
        int argCount = previous == null || !previous.isCloseBracket() && (!previous.isLiteral() || !previous.isLiteralValue()) ? 1 : 2;
        Iterator i$ = candidates.iterator();

        Operator operator;
        do {
            if (!i$.hasNext()) {
                return null;
            }

            operator = (Operator) i$.next();
        } while (operator.getOperandCount() != argCount);

        return operator;
    }

    private void output(Deque<T> values, Token token, Object evaluationContext) {
        if (token.isLiteral()) {
            String operator = token.getLiteral();
            Constant ct = (Constant) this.constants.get(operator);
            T value = ct == null ? null : this.evaluate(ct, evaluationContext);
            /*if (value == null && evaluationContext != null && evaluationContext instanceof AbstractVariableSet) {
                AbstractVariableSet abstractVariableSet = (AbstractVariableSet) evaluationContext;
                value = (T) abstractVariableSet.get(operator);
            }*/

            values.push(value != null ? value : this.toValue(operator, evaluationContext));
        } else if (token.isLookupLiteral()){
            String operator = token.getLookupLiteralValue();
            Constant ct = (Constant) this.constants.get(operator);
            T value = ct == null ? null : this.evaluate(ct, evaluationContext);
            if (value == null && evaluationContext != null && evaluationContext instanceof AbstractVariableSet) {
                AbstractVariableSet abstractVariableSet = (AbstractVariableSet) evaluationContext;
                value = (T) abstractVariableSet.get(operator);
            }
                if (value instanceof String){
                String strVal = (String)value;
                if (StringUtils.isNotEmpty(strVal) && NumberUtils.isNumber(strVal)){
                    value = (T) NumberUtils.createNumber(strVal);
                }
            }

            values.push(value != null ? value : (T) new NullObject());
        }
        else if (token.isLiteralValue()){
            String value =  token.getLiteralValue();
            value = value.replaceAll("'","").trim();
            values.push((T)value);
        } else if (token.isNumber()){
            Number value = token.getNumber();
            values.push((T)value);
        } else {
            if (!token.isOperator()) {
                throw new IllegalArgumentException();
            }

            Operator operator1 = token.getOperator();
            values.push(this.evaluate(operator1, this.getArguments(values, operator1.getOperandCount()), evaluationContext));
        }

    }

    protected T evaluate(Constant constant, Object evaluationContext) {
        throw new RuntimeException("evaluate(Constant) is not implemented for " + constant.getName());
    }

    protected T evaluate(Operator operator, Iterator<T> operands, Object evaluationContext) {
        throw new RuntimeException("evaluate(Operator, Iterator) is not implemented for " + operator.getSymbol());
    }

    protected T evaluate(Function function, Iterator<T> arguments, Deque<Token> argumentList, Object evaluationContext) {
        throw new RuntimeException("evaluate(Function, Iterator) is not implemented for " + function.getName());
    }

    private void doFunction(Deque<T> values, Function function, int argCount,Deque<Token> argumentList, Object evaluationContext) {
        if (function.getMinimumArgumentCount() <= argCount && function.getMaximumArgumentCount() >= argCount) {
            try {
                values.push(this.evaluate(function, this.getArguments(values, argCount), argumentList, evaluationContext));
            }catch (Exception e){
                throw e;
            }
        } else {
            throw new IllegalArgumentException("Invalid argument count for " + function.getName());
        }
    }

    /*private Iterator<Token> getArgumentTokens(Deque<Token> values, int argCount) {
        if (values.size() < argCount) {
            throw new IllegalArgumentException();
        } else {
            LinkedList<Token> result = new LinkedList();

            for (int i = 0; i < argCount; ++i) {
                result.addFirst(values.pop());
            }

            return result.iterator();
        }
    }*/

    private Iterator<T> getArguments(Deque<T> values, int nb) {
        if (values.size() < nb) {
            throw new IllegalArgumentException();
        } else {
            LinkedList result = new LinkedList();

            for (int i = 0; i < nb; ++i) {
                result.addFirst(values.pop());
            }

            return result.iterator();
        }
    }

    protected abstract T toValue(String var1, Object var2);

   /* public T evaluate(String expression) {
        return this.evaluate((String) expression, (Object) null);
    }
*/


    /*private boolean checkKeySplitters(String expression){
        boolean contains = false;
        if (expression.contains("$")){
            contains = true;
        }
        if (expression.contains("<%")){
            contains = true;
        }
        return contains;
    }*/

    public T evaluate(String expression, Object evaluationContext) {

        if (StringUtils.isEmpty(expression)){
            return (T) Boolean.TRUE;
        }
        if (expression.startsWith("'") && expression.endsWith("'")){
           try {
               expression = expression.substring(1, expression.length()-1);
               T res = (T) expression;
               return  res;
           }catch (Exception e){
               e.printStackTrace();
               return null;
           }
        }

        if (Pattern.matches("[a-zA-Z0-9 _]+", expression)){ //match string without special characters as string not expression
            T res = (T) expression;
            return res;
        }

        ArrayDeque<T> values = new ArrayDeque();
        ArrayDeque<Token> argumentTokens = new ArrayDeque();
        ArrayDeque stack = new ArrayDeque();
        ArrayDeque previousValuesSize = this.functions.isEmpty() ? null : new ArrayDeque();
        Iterator tokens = this.tokenize(expression);
      //  List<Token> functionArgumentList = null;

        Token token;
        for (Token previous = null; tokens.hasNext(); previous = token) {
            String sc = (String) tokens.next();
            token = this.toToken(previous, sc);
            if (token.isOpenBracket()) {
                stack.push(token);
                if (previous != null && previous.isFunction()) {
                    if (!this.functionBrackets.containsKey(token.getBrackets().getOpen())) {
                        throw new IllegalArgumentException("Invalid bracket after function: " + sc);
                    }
                } else if (!this.expressionBrackets.containsKey(token.getBrackets().getOpen())) {
                    throw new IllegalArgumentException("Invalid bracket in expression: " + sc);
                }
            } else if (token.isCloseBracket()) {
                if (previous == null) {
                    throw new IllegalArgumentException("expression can\'t start with a close bracket");
                }

                if (previous.isFunctionArgumentSeparator()) {
                    throw new IllegalArgumentException("argument is missing");
                }

                BracketPair sc1 = token.getBrackets();
                boolean openBracketFound = false;



                while (!stack.isEmpty()) {
                    Token argCount = (Token) stack.pop();

                    if (argCount.isOpenBracket()) {
                        if (!argCount.getBrackets().equals(sc1)) {
                            throw new IllegalArgumentException("Invalid parenthesis match " + argCount.getBrackets().getOpen() + sc1.getClose());
                        }

                        openBracketFound = true;
                        break;
                    }

                    this.output(values, argCount, evaluationContext);
                    argumentTokens.push(argCount);
                }

                if (!openBracketFound) {
                    throw new IllegalArgumentException("Parentheses mismatched");
                }

                if (!stack.isEmpty() && ((Token) stack.peek()).isFunction()) {
                    try {
                        int argCount1 = values.size() - ((Integer) previousValuesSize.pop()).intValue();
                        Token tkn = (Token) stack.pop();
                        this.doFunction(values, (tkn).getFunction(), argCount1, argumentTokens, evaluationContext);
                        argumentTokens.push(tkn);
                    }catch (Exception e){
                        throw e;
                    }

                }
            } else if (token.isFunctionArgumentSeparator()) {
                if (previous == null) {
                    throw new IllegalArgumentException("expression can\'t start with a function argument separator");
                }

                if (previous.isOpenBracket() || previous.isFunctionArgumentSeparator()) {
                    throw new IllegalArgumentException("argument is missing");
                }

                boolean sc3 = false;

                while (!stack.isEmpty()) {
                    if (((Token) stack.peek()).isOpenBracket()) {
                        sc3 = true;
                        break;
                    }

                    Token aToken = (Token) stack.pop();
                    argumentTokens.push(aToken);

                    this.output(values, aToken, evaluationContext);
                }

                if (!sc3) {
                    throw new IllegalArgumentException("Separator or parentheses mismatched");
                }
            } else if (token.isFunction()) {
                stack.push(token);
                previousValuesSize.push(Integer.valueOf(values.size()));
            } else if (!token.isOperator()) {
                if (previous != null && previous.isLiteral()) {
                    throw new IllegalArgumentException("A literal can\'t follow another literal");
                }
                argumentTokens.push(token);
                this.output(values, token, evaluationContext);
            } else {
                while (!stack.isEmpty()) {
                    Token sc4 = (Token) stack.peek();
                    if (!sc4.isOperator() || (!token.getAssociativity().equals(Operator.Associativity.LEFT) || token.getPrecedence() > sc4.getPrecedence()) && token.getPrecedence() >= sc4.getPrecedence()) {
                        break;
                    }
                    Token t = (Token) stack.pop();
                    argumentTokens.push(t);
                    this.output(values, t, evaluationContext);
                }

                stack.push(token);
            }
        }

        while (!stack.isEmpty()) {
            Token sc2 = (Token) stack.pop();
            if (sc2.isOpenBracket() || sc2.isCloseBracket()) {
                throw new IllegalArgumentException("Parentheses mismatched");
            }
            argumentTokens.push(sc2);
            this.output(values, sc2, evaluationContext);
        }

        if (values.size() != 1) {
            throw new IllegalArgumentException();
        } else {
            return values.pop();
        }
    }

    private Token toToken(Token previous, String token) {
        if (token.equals(this.functionArgumentSeparator)) {
            return Token.FUNCTION_ARG_SEPARATOR;
        } else if (this.functions.containsKey(token)) {
            return Token.buildFunction((Function) this.functions.get(token));
        } else if (this.operators.containsKey(token)) {
            List brackets1 = (List) this.operators.get(token);
            return brackets1.size() == 1 ? Token.buildOperator((Operator) brackets1.get(0)) : Token.buildOperator(this.guessOperator(previous, brackets1));
        } else {
            BracketPair brackets = this.getBracketPair(token);
            if (brackets != null)
                return (brackets.getOpen().equals(token) ? Token.buildOpenToken(brackets) : Token.buildCloseToken(brackets));
            else if (token.startsWith("'") && token.endsWith("'")){
                return Token.buildLiteralValue(token);
            }
            else if (NumberUtils.isNumber(token)){
                Number number = NumberUtils.createDouble(token);// NumberUtils.createNumber(token);
                return Token.buildNumber(number);
            }else{
                return Token.buildLookupLiteral(token); // we want clean tokens such as: person.name or person.items[] or person.items[0].title
            }
            /*else if (token.startsWith("$") && token.endsWith("$")){
                return Token.buildLookupLiteral(token);
            }
            else {
                return Token.buildLiteral(token);
            }*/
        }
    }

    private BracketPair getBracketPair(String token) {
        BracketPair result = (BracketPair) this.expressionBrackets.get(token);
        return result == null ? (BracketPair) this.functionBrackets.get(token) : result;
    }

    public Collection<Operator> getOperators() {
        ArrayList result = new ArrayList();
        Collection values = this.operators.values();
        Iterator i$ = values.iterator();

        while (i$.hasNext()) {
            List list = (List) i$.next();
            result.addAll(list);
        }

        return result;
    }

    public Collection<Function> getFunctions() {
        return this.functions.values();
    }

    public Collection<Constant> getConstants() {
        return this.constants.values();
    }

    protected Iterator<String> tokenize(String expression) {
        return this.tokenizer.tokenize(expression);
    }


}