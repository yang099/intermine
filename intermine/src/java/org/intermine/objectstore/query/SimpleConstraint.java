package org.flymine.objectstore.query;

/*
 * Copyright (C) 2002-2003 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.math.BigDecimal;
import java.util.Date;
import java.util.Arrays;
import java.util.List;
import org.flymine.util.Util;

/**
 * Represents a constraint between two QueryEvaluable types.  These are query elements
 * that can be resolved to a value - fields, expressions, aggregate functions and
 * constants.  Constraint type can be standard numeric comparison, IS_NULL, and also MATCHES
 * for simple string pattern matching.
 *
 * @author Richard Smith
 * @author Mark Woodbridge
 */
public class SimpleConstraint extends Constraint
{
    protected QueryEvaluable qe1, qe2;
    protected ConstraintOp type;

    /**
     * Construct a Constraint.  Check that java types of QueryEvaluables are compatible with the
     * constraint type selected.
     *
     * @param qe1 first QueryEvaluable for comparison
     * @param type define type of comparison
     * @param qe2 second QueryEvaluable for comparison
     * @throws IllegalArgumentException if type does not correspond to a defined operation
     */
    public SimpleConstraint(QueryEvaluable qe1, ConstraintOp type, QueryEvaluable qe2) {
        this(qe1, type, qe2, false);
    }

    /**
     * Construct a Constraint.  Check that java types of QueryEvaluables are compatible with the
     * constraint type selected.
     *
     * @param qe1 first QueryEvaluable for comparison
     * @param type define type of comparison
     * @param qe2 second QueryEvaluable for comparison
     * @param negated reverse constraint logic if true
     * @throws IllegalArgumentException if type does not correspond to a defined operation
     */
    public SimpleConstraint(QueryEvaluable qe1, ConstraintOp type, QueryEvaluable qe2,
                            boolean negated) {
        if (qe1 == null) {
            throw new NullPointerException("qe1 cannot be null");
        }

        if (type == null) {
            throw new NullPointerException("type cannot be null");
        }

        if (qe2 == null) {
            throw new NullPointerException("qe2 cannot be null");
        }

        if (qe1.getType().equals(UnknownTypeValue.class)
                && (!(qe2.getType().equals(UnknownTypeValue.class)))) {
            qe1.youAreType(qe2.getType());
        }
        if (qe2.getType().equals(UnknownTypeValue.class)
                && (!(qe1.getType().equals(UnknownTypeValue.class)))) {
            qe2.youAreType(qe1.getType());
        }
        if (!validComparison(qe1.getType(), type, qe2.getType())) {
            throw new IllegalArgumentException("Invalid constraint: "
                                               + qe1.getType().getName()
                                               + " " + type
                                               + " " + qe2.getType().getName());
        }

        this.qe1 = qe1;
        this.type = type;
        this.qe2 = qe2;
        this.negated = negated;
    }

    /**
     * Construct a Constraint.  Check that correct type of constraint is selected for
     * single QueryEvaluable constructor
     *
     * @param qe1 first QueryEvaluable for comparison
     * @param type define type of comparison
     * @throws IllegalArgumentException if type does not correspond to a defined operation
     */
    public SimpleConstraint(QueryEvaluable qe1, ConstraintOp type) {
        this(qe1, type, false);
    }

    /**
     * Construct a Constraint.  Check that correct type of constraint is selected for
     * single QueryEvaluable constructor
     *
     * @param qe1 first QueryEvaluable for comparison
     * @param type define type of comparison
     * @param negated reverse constraint logic if true
     * @throws IllegalArgumentException if type does not correspond to a defined operation
     */
    public SimpleConstraint(QueryEvaluable qe1, ConstraintOp type, boolean negated) {
        if (qe1 == null) {
            throw new NullPointerException("qe1 cannot be null");
        }

        if (type == null) {
            throw new NullPointerException("type cannot be null");
        }

        if (!validComparison(qe1.getType(), type, null)) {
            throw new IllegalArgumentException("Invalid constraint: "
                                               + qe1.getType().getName()
                                               + " " + type);
        }

        this.qe1 = qe1;
        this.type = type;
        this.negated = negated;
    }

    /**
     * Get type of constraint
     *
     * @return integer value of operation type
     */
    public ConstraintOp getType() {
        return type;
    }
        
    /**
     * Get type of constraint, taking negated into account.
     *
     * @return integer value of operation type
     */
    public ConstraintOp getRealType() {
        return (negated ? negate(type) : type);
    }

    /**
     * Returns the left argument of the constraint.
     *
     * @return the left-hand argument
     */
    public QueryEvaluable getArg1() {
        return qe1;
    }

    /**
     * Returns the right argument of the constraint.
     *
     * @return the right-hand argument
     */
    public QueryEvaluable getArg2() {
        return qe2;
    }

    /**
     * Returns the String representation of the operation.
     *
     * @return String representation
     */
    public String getOpString() {
        return getRealType().toString();
    }

    /**
     * Test whether two SimpleConstraints are equal, overrides Object.equals()
     *
     * @param obj the object to compare with
     * @return true if objects are equal
     */
    public boolean equals(Object obj) {
        if (obj instanceof SimpleConstraint) {
            SimpleConstraint sc = (SimpleConstraint) obj;
            return qe1.equals(sc.qe1)
                    && type == sc.type
                    && negated == sc.negated
                    && Util.equals(qe2, sc.qe2);
        }
        return false;
    }

    /**
     * Get the hashCode for this object overrides Object.hashCode()
     *
     * @return the hashCode
     */
    public int hashCode() {
        return qe1.hashCode()
            + 3 * type.hashCode() 
            + 5 * (negated ? 1 : 0)
            + 7 * Util.hashCode(qe2);
    }

    //-------------------------------------------------------------------------
    
    /**
     * require that the two arguments are either equal numerically or are identical strings
     */
    protected static final ConstraintOp EQUALS = ConstraintOp.EQUALS;

    /**
     * require that the two arguments are not equal numerically or not identical strings
     */
    protected static final ConstraintOp NOT_EQUALS = ConstraintOp.NOT_EQUALS;

    /**
     * require that the first argument is less than the second (numeric only)
     */
    protected static final ConstraintOp LESS_THAN = ConstraintOp.LESS_THAN;

    /**
     * require that the first argument is less than or equal to the second (numeric only)
     */
    protected static final ConstraintOp LESS_THAN_EQUALS = ConstraintOp.LESS_THAN_EQUALS;

    /**
     * require that the first argument is greater than the second (numeric only)
     */
    protected static final ConstraintOp GREATER_THAN = ConstraintOp.GREATER_THAN;

    /**
     * require that the first argument is greater than or equal to the second (numeric only)
     */
    protected static final ConstraintOp GREATER_THAN_EQUALS = ConstraintOp.GREATER_THAN_EQUALS;

    /**
     * require that the first argument is a substring of the second (string only)
     */
    protected static final ConstraintOp MATCHES = ConstraintOp.MATCHES;

    /**
     * require that the first argument is not a substring of the second (string only)
     */
    protected static final ConstraintOp DOES_NOT_MATCH = ConstraintOp.DOES_NOT_MATCH;

    /**
     * require that the specified argument is null
     */
    protected static final ConstraintOp IS_NULL = ConstraintOp.IS_NULL;

    /**
     * require that the specified argument is not null
     */
    protected static final ConstraintOp IS_NOT_NULL = ConstraintOp.IS_NOT_NULL;

    protected static final ConstraintOp[] NUMBER_OPS = {
        EQUALS,
        NOT_EQUALS,
        LESS_THAN,
        LESS_THAN_EQUALS,
        GREATER_THAN,
        GREATER_THAN_EQUALS};

    protected static final ConstraintOp[] DATE_OPS = NUMBER_OPS;

    protected static final ConstraintOp[] STRING_OPS = {
        EQUALS,
        NOT_EQUALS,
        MATCHES,
        DOES_NOT_MATCH};

    protected static final ConstraintOp[] BOOLEAN_OPS = {
        EQUALS,
        NOT_EQUALS};

    protected static final ConstraintOp[] ALL_OPS = {
        EQUALS,
        NOT_EQUALS,
        LESS_THAN,
        LESS_THAN_EQUALS,
        GREATER_THAN,
        GREATER_THAN_EQUALS,
        MATCHES,
        DOES_NOT_MATCH};

    /**
     * Get type of constraint, taking negated into account.
     *
     * @param op the operator to negate
     * @return integer value of operation type
     */
    protected static ConstraintOp negate(ConstraintOp op) {
        if (op == EQUALS) {
            return NOT_EQUALS;
        } else if (op == NOT_EQUALS) {
            return EQUALS;
        } else if (op == LESS_THAN) {
            return GREATER_THAN_EQUALS;
        } else if (op == GREATER_THAN_EQUALS) {
            return LESS_THAN;
        } else if (op == GREATER_THAN) {
            return LESS_THAN_EQUALS;
        } else if (op == LESS_THAN_EQUALS) {
            return GREATER_THAN;
        } else if (op == MATCHES) {
            return DOES_NOT_MATCH;
        } else if (op == DOES_NOT_MATCH) {
            return MATCHES;
        } else if (op == IS_NULL) {
            return IS_NOT_NULL;
        } else if (op == IS_NOT_NULL) {
            return IS_NULL;
        }
        return op;
    }

    /**
     * Check whether a comparison is valid i.e. the arguments are comparable types and the
     * the operator is permitted for those types
     * @param arg1 the first argument
     * @param op how to compare the arguments
     * @param arg2 the second argument
     * @return whether the comparison is valid
     */
    public static boolean validComparison(Class arg1, ConstraintOp op, Class arg2) {
        if (arg2 == null) {
            return op == IS_NULL || op == IS_NOT_NULL;
        }
        if (comparable(arg1, arg2)) {
            return validOps(arg1).contains(op);
        }
        return false;
    }

    /**
     * Check whether the two arguments are of comparable types i.e. they are of similiar type
     * and we know how to handle that type
     * @param arg1 the first argument
     * @param arg2 the second argument
     * @return whether the types are comparable
     */
    public static boolean comparable(Class arg1, Class arg2) {
        if (arg1.equals(arg2)
                && (arg1.equals(String.class) || arg1.equals(Boolean.class)
                    || arg1.equals(Date.class) || arg1.equals(Short.class)
                    || arg1.equals(Integer.class) || arg1.equals(Long.class)
                    || arg1.equals(Float.class) || arg1.equals(Double.class)
                    || arg1.equals(BigDecimal.class) || arg1.equals(UnknownTypeValue.class))) {
            return true;
        }
        return false;
    }

    /**
     * Return the list of valid (binary) operator codes given arguments of a specified type
     * @param arg the argument type
     * @return an array of character codes
     */
    public static List validOps(Class arg) {
        if (Number.class.isAssignableFrom(arg)) {
            return Arrays.asList(NUMBER_OPS);
        } else if (String.class.equals(arg)) {
            return Arrays.asList(STRING_OPS);
        } else if (Boolean.class.equals(arg)) {
            return Arrays.asList(BOOLEAN_OPS);
        } else if (Date.class.equals(arg)) {
            return Arrays.asList(DATE_OPS);
        } else if (UnknownTypeValue.class.equals(arg)) {
            return Arrays.asList(ALL_OPS);
        }
        return null;
    }
}
