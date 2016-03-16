/*
 *
 */
package net.community.chest.math;

import java.util.List;

import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.util.compare.AbstractComparator;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 27, 2009 3:12:44 PM
 */
public class MathConstantEmbedder implements NumbersFunction, PubliclyCloneable<MathConstantEmbedder> {
    private Number    _n;
    public Number getNumber ()
    {
        return _n;
    }

    public void setNumber (Number n)
    {
        if (!AbstractComparator.compareObjects(n, _n))
            _n = n;    // debug breakpoints
    }

    public MathConstantEmbedder (Number n)
    {
        _n = n;
    }

    public MathConstantEmbedder ()
    {
        this(null);
    }
    /*
     * @see net.community.chest.math.NumbersFunction#getFloatingPointExecutionState()
     */
    @Override
    public Boolean getFloatingPointExecutionState ()
    {
        return null;
    }
    /*
     * @see net.community.chest.math.NumbersFunction#invoke(java.util.List)
     */
    @Override
    public Number invoke (List<? extends Number> args)
            throws IllegalArgumentException, ClassCastException
    {
        return getNumber();
    }
    /*
     * @see net.community.chest.math.NumbersFunction#invoke(java.lang.Number[])
     */
    @Override
    public Number invoke (Number... args)
        throws IllegalArgumentException, ClassCastException
    {
        return getNumber();
    }
    /*
     * @see net.community.chest.math.FunctionInterface#getName()
     */
    @Override
    public String getName ()
    {
        final Number    n=getNumber();
        return String.valueOf(n);
    }
    /*
     * @see net.community.chest.math.FunctionInterface#getNumArguments()
     */
    @Override
    public int getNumArguments ()
    {
        return 0;
    }
    /*
     * @see net.community.chest.math.FunctionInterface#getSymbol()
     */
    @Override
    public String getSymbol ()
    {
        return getName();
    }
    /*
     * @see java.lang.Object#clone()
     */
    @Override
    public MathConstantEmbedder clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }

    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals (Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof MathConstantEmbedder))
            return false;

        return AbstractComparator.compareObjects(getNumber(), ((MathConstantEmbedder) obj).getNumber());
    }
    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode ()
    {
        return ClassUtil.getObjectHashCode(getNumber());
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        return String.valueOf(getNumber());
    }
}
