/*
 *
 */
package net.community.chest.math.functions.eval;

import net.community.chest.math.NumbersFunction;
import net.community.chest.resources.PropertyAccessor;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Represents an expression evaluator code</P>
 * @author Lyor G.
 * @since May 27, 2009 3:21:41 PM
 */
public interface Evaluator extends NumbersFunction {
    /**
     * @param resolver A {@link PropertyAccessor} resolver to be used to
     * resolve values of "variables" in the expression. The
     * {@link PropertyAccessor#getProperty(Object)} is invoked with the
     * {@link String} of the variable name and is expected to return the
     * {@link Number} value of that variable. If <code>null</code> then
     * caller expects that the evaluator will not be used (otherwise a
     * {@link RuntimeException} may be thrown or <code>null</code> returned).
     * @return Evaluation result - may be <code>null</code> to indicate either
     * and error or no evaluation taking place
     * @throws RuntimeException if failed to evaluate
     */
    Number invoke (PropertyAccessor<String,? extends Number> resolver) throws RuntimeException;
}
