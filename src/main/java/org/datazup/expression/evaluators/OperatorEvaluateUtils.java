package org.datazup.expression.evaluators;

import org.datazup.expression.Operator;
import org.datazup.expression.context.ContextWrapper;
import org.datazup.expression.context.ExecutionContext;
import org.datazup.expression.exceptions.ExpressionValidationException;
import org.datazup.pathextractor.AbstractResolverHelper;
import org.datazup.utils.Tuple;
import org.datazup.utils.TypeUtils;

import java.util.Iterator;

public class OperatorEvaluateUtils extends EvaluatorBase{

    public OperatorEvaluateUtils(AbstractResolverHelper mapListResolver, ExecutionContext executionContext) {
        super(mapListResolver, executionContext);
    }


    public ContextWrapper getPow(Operator operator, Iterator<ContextWrapper> operands, Object evaluationContext) {
        Tuple<Number, Number> numberTuple = getNumberTuple(operands);

        if (null != numberTuple.getKey() && null != numberTuple.getValue()) {
            return wrap(Math.pow(numberTuple.getKey().doubleValue(), numberTuple.getValue().doubleValue()));
        }
        throw new ExpressionValidationException("Values cannot be null type");
    }

    private Boolean getEqualBool(Operator operator, Iterator<ContextWrapper> operands, Object evaluationContext) {
        ContextWrapper leftC = operands.next();
        ContextWrapper rightC = operands.next();

        Object left = leftC.get();
        Object right = rightC.get();
        if (null == left || right == null) {
            return false; // left.equals(right);
        }
        if (null == left && null == right) {
            return true;
        }
        if (left instanceof Number && right instanceof Number) {
            return ((Number) left).doubleValue() == ((Number) right).doubleValue();
        } else {
            if (left instanceof Boolean || right instanceof Boolean) {
                Boolean l = Boolean.parseBoolean(left.toString());
                Boolean r = Boolean.parseBoolean(right.toString());
                return l.equals(r);
            } else if (left instanceof String || right instanceof String) {
                return left.toString().equalsIgnoreCase(right.toString());
            } else {
                return left.equals(right);
            }
        }
    }



    protected ContextWrapper wrap(Object object){
        return executionContext.create(object);
    }

    private Tuple<Number, Number> getNumberTuple(Iterator<ContextWrapper> operands) {
        ContextWrapper leftC = operands.next();
        ContextWrapper rightC = operands.next();
        Object left = leftC.get();
        Object right = rightC.get();

        Number lN = TypeUtils.resolveNumber(left);// left;
        Number rN = TypeUtils.resolveNumber(right);//right;

        return new Tuple<>(lN, rN);
    }

    public ContextWrapper getModulo(Operator operator, Iterator<ContextWrapper> operands, Object evaluationContext) {
        Tuple<Number, Number> numberTuple = getNumberTuple(operands);

        // TODO - write test ases for Modulo
        if (null != numberTuple.getKey() && null != numberTuple.getValue()) {
            return wrap(numberTuple.getKey().doubleValue() % numberTuple.getValue().doubleValue());
        }
        return wrap(0);
    }

    public ContextWrapper getDivide(Operator operator, Iterator<ContextWrapper> operands, Object evaluationContext) {
        ContextWrapper leftC = operands.next();
        ContextWrapper rightC = operands.next();

        Object left = leftC.get();
        Object right = rightC.get();

        Number lN = TypeUtils.resolveNumber(left);// left;
        Number rN = TypeUtils.resolveNumber(right);//right;
        if (null != lN && null != rN) {
            return wrap(lN.doubleValue() / rN.doubleValue());
        }
        return wrap(0);
    }

    public ContextWrapper getMultiply(Operator operator, Iterator<ContextWrapper> operands, Object evaluationContext) {
        ContextWrapper leftC = operands.next();
        ContextWrapper rightC = operands.next();

        Object left = leftC.get();
        Object right = rightC.get();

        Number lN = TypeUtils.resolveNumber(left);// left;
        Number rN = TypeUtils.resolveNumber(right);//right;

        if (null != lN && null != rN) {
            return wrap(lN.doubleValue() * rN.doubleValue());
        }
        return wrap(0);
    }

    public ContextWrapper getNegate(Operator operator, Iterator<ContextWrapper> operands, Object evaluationContext) {
        ContextWrapper nextC = operands.next();
        Object next = nextC.get();

        if (next == null) {
            return wrap(false);
        }
        Boolean nextB = TypeUtils.resolveBoolean(next);
        if (null != nextB) {
            return wrap(!nextB);
        } else {
            return wrap(!true);
        }
    }

    public ContextWrapper getNotEqual(Operator operator, Iterator<ContextWrapper> operands, Object evaluationContext) {
        Boolean booleanRes = getEqualBool(operator, operands, evaluationContext);
        return wrap(!booleanRes);
    }

    public ContextWrapper getEqual(Operator operator, Iterator<ContextWrapper> operands, Object evaluationContext) {
        Boolean booleanRes = getEqualBool(operator, operands, evaluationContext);
        return wrap(booleanRes);
    }

    public ContextWrapper getLowerThenOrEqual(Operator operator, Iterator<ContextWrapper> operands, Object evaluationContext) {
        ContextWrapper leftC = operands.next();
        ContextWrapper rightC = operands.next();

        Object left = leftC.get();
        Object right = rightC.get();
        if (null == left || null == right) {
            return wrap(false);
        }
        Number l = TypeUtils.resolveNumber(left);// left;
        Number r = TypeUtils.resolveNumber(right);//right;
        if (null == l || null == r)
            return wrap(null);
        return wrap(l.doubleValue() <= r.doubleValue());
    }

    public ContextWrapper getGreaterThenOrEqual(Operator operator, Iterator<ContextWrapper> operands, Object evaluationContext) {

        ContextWrapper leftC = operands.next();
        ContextWrapper rightC = operands.next();

        Object left = leftC.get();
        Object right = rightC.get();


        if (null == left || null == right) {
            return wrap(false);
        }

        Number l = TypeUtils.resolveNumber(left);// left;
        Number r = TypeUtils.resolveNumber(right);//right;
        if (null == l || null == r)
            return wrap(null);

        return wrap(l.doubleValue() >= r.doubleValue());
    }

    public ContextWrapper getGreaterThen(Operator operator, Iterator<ContextWrapper> operands, Object evaluationContext) {
        ContextWrapper leftC = operands.next();
        ContextWrapper rightC = operands.next();

        Object left = leftC.get();
        Object right = rightC.get();

        if (null == left || null == right) {
            return wrap(false);
        }

        // check this:
        // http://stackoverflow.com/questions/2683202/comparing-the-values-of-two-generic-numbers

        Number l = TypeUtils.resolveNumber(left);// left;
        Number r = TypeUtils.resolveNumber(right);//right;
        if (null == l || null == r)
            return wrap(null);


        return wrap(l.doubleValue() > r.doubleValue());
    }

    public ContextWrapper getLowerThen(Operator operator, Iterator<ContextWrapper> operands, Object evaluationContext) {
        ContextWrapper leftC = operands.next();
        ContextWrapper rightC = operands.next();

        Object left = leftC.get();
        Object right = rightC.get();


        if (null == left || null == right) {
            return wrap(false);
        }
        // check this:
        // http://stackoverflow.com/questions/2683202/comparing-the-values-of-two-generic-numbers
        Number l = TypeUtils.resolveNumber(left);// left;
        Number r = TypeUtils.resolveNumber(right);//right;
        if (null == l || null == r)
            return wrap(null);
        // if (null!=l && null!=r)
        return wrap(l.doubleValue() < r.doubleValue());
        // else return false;
    }

    public ContextWrapper getAnd(Operator operator, Iterator<ContextWrapper> operands, Object evaluationContext) {
        ContextWrapper leftC = operands.next();
        ContextWrapper rightC = operands.next();

        Object left = leftC.get();
        Object right = rightC.get();

        if (null == left || null == right) {
            return wrap(false);
        }

        boolean l = TypeUtils.resolveBoolean(left);
        boolean r = TypeUtils.resolveBoolean(right);
        return wrap(l && r); // Boolean.logicalAnd(l,r);
    }

    public ContextWrapper getOr(Operator operator, Iterator<ContextWrapper> operands, Object evaluationContext) {
        ContextWrapper leftC = operands.next();
        ContextWrapper rightC = operands.next();

        Object left = leftC.get();
        Object right = rightC.get();

        if (null == left || null == right) {
            return wrap(false);
        }

        boolean l = TypeUtils.resolveBoolean(left);
        boolean r = TypeUtils.resolveBoolean(right);
        return wrap(l || r); // Boolean.logicalOr(l,r);
    }

    public ContextWrapper getPlus(Operator operator, Iterator<ContextWrapper> operands, Object evaluationContext) {
        ContextWrapper leftC = operands.next();
        ContextWrapper rightC = operands.next();

        Object left = leftC.get();
        Object right = rightC.get();


        if (left instanceof String || right instanceof String) {
            String l, r;
            if (left instanceof String) {
                l = (String) left;
            } else {
                l = (null == left ? "" : left.toString());
            }
            if (right instanceof String) {
                r = (String) right;
            } else {
                r = (null == right ? "" : right.toString());
            }
            return wrap(l + r);
        } else {
            Number l = TypeUtils.resolveNumber(left);
            if (null == l)
                l = 0;
            Number r = TypeUtils.resolveNumber(right);
            if (null == r)
                r = 0;
            return wrap(l.doubleValue() + r.doubleValue());
        }
    }

    public ContextWrapper getMinus(Operator operator, Iterator<ContextWrapper> operands, Object evaluationContext) {
        ContextWrapper leftC = operands.next();
        Object left = leftC.get();
        if (null == left)
            return wrap(null);

        ContextWrapper rightC = operands.next();
        Object right = rightC.get();

        if (null == right)
            return wrap(null);
        Number l = TypeUtils.resolveNumber(left);// left;
        Number r = TypeUtils.resolveNumber(right);//right;

        if (null == l || null == r)
            return wrap(null);

        return wrap(l.doubleValue() - r.doubleValue());
    }
}
