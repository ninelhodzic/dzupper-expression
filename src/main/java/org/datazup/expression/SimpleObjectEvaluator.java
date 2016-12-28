package org.datazup.expression;


import org.apache.commons.lang3.StringUtils;
import org.datazup.expression.exceptions.NotSupportedExpressionException;
import org.datazup.pathextractor.AbstractVariableSet;
import org.datazup.pathextractor.PathExtractor;
import org.datazup.utils.DateTimeUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ninel on 3/14/16.
 */
public class SimpleObjectEvaluator extends AbstractEvaluator<Object> {
    /** The negate unary operator.*/
    public final static Operator NEGATE = new Operator("!", 1, Operator.Associativity.RIGHT, 3);
    /** The logical AND operator.*/
    private static final Operator AND = new Operator("&&", 2, Operator.Associativity.LEFT, 2);
    /** The logical OR operator.*/
    public final static Operator OR = new Operator("||", 2, Operator.Associativity.LEFT, 1);

    public final static Operator NOT_EQUAL = new Operator("!=", 2, Operator.Associativity.LEFT, 4);
    public final static Operator EQUAL = new Operator("==", 2, Operator.Associativity.LEFT, 5);
    public final static Operator GREATER_THEN = new Operator(">", 2, Operator.Associativity.LEFT, 6);
    public final static Operator LOWER_THEN = new Operator("<", 2, Operator.Associativity.LEFT, 7);

    public final static Operator PLUS = new Operator("+", 2, Operator.Associativity.LEFT, 8);
    public final static Operator MINUS = new Operator("-", 2, Operator.Associativity.LEFT, 9);


    public final static Function IS_NULL = new Function("IS_NULL", 1);
    public final static Function SET_NULL = new Function("SET_NULL", 1);
    public final static Function SIZE_OF = new Function("SIZE_OF", 1);

    // date functions
    public final static Function NOW = new Function("NOW", 0);
    public final static Function STR_TO_DATE_TIMESTAMP = new Function("STR_TO_DATE_TIMESTAMP", 2);

    public final static Function MINUTE = new Function("MINUTE", 1);
    public final static Function HOUR = new Function("HOUR", 1);
    public final static Function DAY = new Function("DAY", 1);
    public final static Function WEEK = new Function("WEEK", 1);
    public final static Function MONTH = new Function("MONTH", 1);
    public final static Function YEAR = new Function("YEAR", 1);
    public final static Function REGEX_MATCH= new Function("REGEX_MATCH", 2);
    public final static Function REGEX_EXTRACT= new Function("REGEX_EXTRACT",2,3);

   // public final static Function DATE = new Function("DATE", 2);


    protected static final Parameters PARAMETERS;

    static {
        // Create the evaluator's parameters
        PARAMETERS = new Parameters();
        // Add the supported operators
        PARAMETERS.add(AND);
        PARAMETERS.add(OR);
        PARAMETERS.add(NEGATE);

        PARAMETERS.add(NOT_EQUAL);
        PARAMETERS.add(EQUAL);
        PARAMETERS.add(GREATER_THEN);
        PARAMETERS.add(LOWER_THEN);

        PARAMETERS.add(PLUS);
        PARAMETERS.add(MINUS);

        PARAMETERS.addExpressionBracket(BracketPair.PARENTHESES);
        PARAMETERS.addFunctionBracket(BracketPair.PARENTHESES);
        PARAMETERS.add(NOW);
        PARAMETERS.add(IS_NULL);
        PARAMETERS.add(SET_NULL);
        PARAMETERS.add(SIZE_OF);

        PARAMETERS.add(STR_TO_DATE_TIMESTAMP);

        PARAMETERS.add(MINUTE);
        PARAMETERS.add(HOUR);
        PARAMETERS.add(DAY);
        PARAMETERS.add(WEEK);
        PARAMETERS.add(MONTH);
        PARAMETERS.add(YEAR);
        PARAMETERS.add(REGEX_MATCH);
        PARAMETERS.add(REGEX_EXTRACT);
   }

    public SimpleObjectEvaluator() {
        super(PARAMETERS);
    }

    @Override
    protected Object toValue(String literal, Object evaluationContext) {
        if (evaluationContext instanceof AbstractVariableSet){
            AbstractVariableSet abs = (AbstractVariableSet)evaluationContext;
            Object o = abs.get(literal);
            if (null==o){
                return new NullObject();
            }else{
                return o;
            }
        }
        return literal;
    }

    @Override
    protected Object evaluate(Function function, Iterator<Object> operands, Deque<Token> argumentList, Object evaluationContext){
        if (function==STR_TO_DATE_TIMESTAMP){
            return strToDateTimeStamp(function, operands, argumentList, evaluationContext);
        }else
        if (function==MINUTE){
            Object op1 = operands.next();
            argumentList.pop();
            DateTime dt = DateTimeUtils.resolve(op1);
            return dt.getMinuteOfDay();
        }else if (function==HOUR){
            Object op1 = operands.next();
            argumentList.pop();
            DateTime dt = DateTimeUtils.resolve(op1);
            return dt.getHourOfDay();
        }else  if (function==DAY){
            Object op1 = operands.next();
            argumentList.pop();
            DateTime dt = DateTimeUtils.resolve(op1);
            return dt.getDayOfMonth();
        }else  if (function==WEEK){
            Object op1 = operands.next();
            argumentList.pop();
            DateTime dt = DateTimeUtils.resolve(op1);
            Integer res =  dt.getDayOfMonth() % 7;
            return  res;
        }else  if (function==MONTH){
            Object op1 = operands.next();
            argumentList.pop();
            DateTime dt = DateTimeUtils.resolve(op1);
            return dt.getMonthOfYear();
        }else
        if (function==YEAR){
            Object op1 = operands.next();
            argumentList.pop();
            DateTime dt = DateTimeUtils.resolve(op1);
            return dt.getYear();
        }else  if (function==NOW){
            return System.currentTimeMillis();
        }else if (function==SET_NULL){
            Object op1 = operands.next();
            argumentList.pop();
            return null; //getNullObject();
        }
        else if (function==IS_NULL){
            Object op1 = operands.next();
            Token token = argumentList.pop();
            if (null== op1 || op1 instanceof NullObject || op1.toString()==token.getContent().toString()){
                return true;
            }
            return op1==null;
        }else if (function==SIZE_OF){
            Object op1 = operands.next();
            argumentList.pop();
            if (null!=op1 && !(op1 instanceof NullObject)){
                if (op1 instanceof Collection){
                    Collection c = (Collection)op1;
                    return c.size();
                }else if (op1 instanceof Map){
                    Map map = (Map)op1;
                    return map.size();
                }
                else if (op1 instanceof String){
                    return ((String)op1).length();
                }else{
                    throw new NotSupportedExpressionException("SizeOf function not supported for instance of \""+op1.getClass()+"\"");
                }
            }
            return 0;
        }
        else if (function==REGEX_MATCH){
            
            return regexMatch(function, operands, argumentList, evaluationContext);
        }
        else if (function==REGEX_EXTRACT){
            
            return regexExtract(function, operands, argumentList, evaluationContext);
        }
        else {
            return nextFunctionEvaluate(function, operands, argumentList, evaluationContext);
        }
    }

    private Object regexExtract(Function function, Iterator<Object> operands, Deque<Token> argumentList,
			Object evaluationContext) {
		
    	String regexFieldValue = operands.next().toString();
        Token token = argumentList.pop();

        String regexPattern = operands.next().toString();
        Token token1 = argumentList.pop();
        
        Object group = null;
        if(operands.hasNext()){
        	group = operands.next();
            Token token2 = argumentList.pop();
        }
        
        regexPattern = regexPattern.replace("#", "");
        Pattern r = Pattern.compile(regexPattern);
        Matcher matcher = r.matcher(regexFieldValue);

        int pos;
        String result = "";
        	
        for(pos = 0; matcher.find(); pos = matcher.end()) {
        	if("" != result){
        		result+=", ";
        	}
        	
        	if( null != group){
        		result +=  matcher.group(((Double)group).intValue());
        	}
        	else{
        		result +=  regexFieldValue.substring(matcher.start(),matcher.end());
        	}       	
        }
        
        return result;
	}

	private Object regexMatch(Function function, Iterator<Object> operands, Deque<Token> argumentList,
			Object evaluationContext) {
    	 String regexFieldValue = operands.next().toString();
         Token token = argumentList.pop();

         String regexPattern = operands.next().toString();
         Token token1 = argumentList.pop();
         
         Pattern r = Pattern.compile(regexPattern);
         Matcher matcher = r.matcher(regexFieldValue);
         
         boolean matches = matcher.matches();
         
         return matches;
	}

	private Object strToDateTimeStamp(Function function, Iterator<Object> operands, Deque<Token> argumentList, Object evaluationContext) {
        String dateString = operands.next().toString().replace("#", "");
        Token token = argumentList.pop();

        String stringFormat = operands.next().toString().replace("#", "");
        Token token1 = argumentList.pop();

        if (stringFormat instanceof String && dateString instanceof String) {
            try {
                DateTimeFormatter df = DateTimeFormat.forPattern(stringFormat);
                DateTime dt = DateTime.parse(dateString, df);
                return dt.getMillis();
            }catch (Exception e){
                System.out.println("Parse date error for date: "+dateString+" and format: "+stringFormat+" - "+e.getMessage());
            }
        }

        return null;
    }

    protected Object nextFunctionEvaluate(Function function, Iterator<Object> operands, Deque<Token> argumentList, Object evaluationContext){
        return super.evaluate(function, operands, argumentList, evaluationContext);
    }

    protected Object superFunctionEvaluate(Function function, Iterator<Object> operands,Deque<Token> argumentList, Object evaluationContext){
        return super.evaluate(function, operands, argumentList, evaluationContext);
    }

    @Override
    protected Object evaluate(Operator operator, Iterator<Object> operands, Object evaluationContext) {
        if (operator==NEGATE){
            Object next = operands.next();
            if (next==null){
                return false;
            }
            if (next instanceof Boolean){
                return !((Boolean)next);
            }
        }else if (operator==NOT_EQUAL){
            Object left = operands.next();
            Object right = operands.next();
            if (left instanceof Number && right instanceof Number){
                return ((Number)left).doubleValue()!=((Number)right).doubleValue();
            }else {
                return !left.equals(right);
            }
        }else if (operator==EQUAL){
            Object left = operands.next();
            Object right = operands.next();
            if (left instanceof Number && right instanceof Number){
                return ((Number)left).doubleValue()==((Number)right).doubleValue();
            }else {
                return left.equals(right);
            }
        }
        else if (operator==GREATER_THEN){
            Object left = operands.next();
            Object right = operands.next();
            // check this: http://stackoverflow.com/questions/2683202/comparing-the-values-of-two-generic-numbers
            Number l = (Number)left;
            Number r = (Number)right;
         //   if (null!=l && null!=r)
            return l.doubleValue() > r.doubleValue();
         //   else return false;
        }
        else if (operator==LOWER_THEN){
            Object left = operands.next();
            Object right = operands.next();
        // check this: http://stackoverflow.com/questions/2683202/comparing-the-values-of-two-generic-numbers
            Number l = (Number)left;
            Number r = (Number)right;
           // if (null!=l && null!=r)
            return l.doubleValue() < r.doubleValue();
           // else return false;
        }
        else if (operator==AND){
            Object left = operands.next();
            Object right = operands.next();
            boolean l = (Boolean)left;
            boolean r = (Boolean)right;
            return l && r; //Boolean.logicalAnd(l,r);
        }
        else if (operator==OR){
            Object left = operands.next();
            Object right = operands.next();
            boolean l = (Boolean)left;
            boolean r = (Boolean)right;
            return l || r; //Boolean.logicalOr(l,r);
        }
        else if (operator==PLUS){
            Object left = operands.next();
            Object right = operands.next();
            Number l = (Number)left;
            Number r = (Number)right;
            //if (null!=l && null!=r)
                return l.doubleValue()+r.doubleValue();

        }else if (operator==MINUS){
            Object left = operands.next();
            Object right = operands.next();
            Number l = (Number)left;
            Number r = (Number)right;
           // if (null!=l && null!=r)
                return l.doubleValue()-r.doubleValue();
        }
        else {
            return nextOperatorEvaluate(operator, operands, evaluationContext);
        }
        return false;
    }

    protected Object nextOperatorEvaluate(Operator operator, Iterator<Object> operands, Object evaluationContext){
        return super.evaluate(operator, operands, evaluationContext);
    }
    protected Object superOperatorEvaluate(Operator operator, Iterator<Object> operands, Object evaluationContext){
        return super.evaluate(operator, operands, evaluationContext);
    }

    protected String normalizeTokenName(String tokenName) {
        if(tokenName.startsWith("$") && tokenName.endsWith("$")){
            tokenName = tokenName.substring(1,tokenName.length()-1);
        }
        if (tokenName.contains(".")){
            StringBuilder builder = new StringBuilder();
            String[] splitted = tokenName.split("\\.");
            int counter = 0;
            for (String s: splitted){
                if (counter==0) {
                    builder.append(s);
                }else{
                    String sc = StringUtils.capitalize(s);
                    builder.append(sc);
                }
                counter++;
            }
            tokenName = builder.toString();
        }
        return tokenName;
    }

    public static void main(String[] args) {

        Map<String,Object> child = new HashMap<>();
        child.put("name","child");
        Map<String , Object> parent = new HashMap<>();
        List<Object> list = new ArrayList<>();
        list.add("Hello");
        list.add("Hi");
        List<Object> list1 = new ArrayList<>();
        list1.add((new HashMap<>().put("n", "n")));

        child.put("list",list);


        parent.put("child", child);
        parent.put("list",list1);
        parent.put("number",5.30);

        String path = "child.name";
        String listParentPath = "list";
        String listChildPath = "child.list";

        PathExtractor pathExtractor = new PathExtractor(parent);

        SimpleObjectEvaluator evaluator = new SimpleObjectEvaluator();
        String expression = "(child.name == 'child' && number==5.30 && 6.30-1 == number) && (NOW() < NOW() || NOW()==NOW())";
        //String expression = "now() < now() || now()==now()";
       // String expression = "6.30-1 +0 + (1-1) == number";
        //String expression = "number==5";
        System.out.println(expression + " = " + evaluator.evaluate(expression, pathExtractor));

        Long start = System.currentTimeMillis();
        for (int i=0;i<10000;i++) {
            evaluator.evaluate(expression, pathExtractor);
            //System.out.println(expression + " = " + );
        }
        System.out.println("total time executed: "+(System.currentTimeMillis()-start)+" ms");
       /* expression = "true || false";
        System.out.println (expression+" = "+evaluator.evaluate(expression));
        expression = "!true";
        System.out.println (expression+" = "+evaluator.evaluate(expression));*/
    }

}
