/*
 * 
 */
package net.community.chest.apache.ant.helpers;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.condition.Condition;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * <P>Base class for {@link org.apache.tools.ant.Task}-s that are also
 * {@link Condition}-s that may set a property with their result
 * @author Lyor G.
 * @since Jul 12, 2009 9:39:02 AM
 */
public abstract class AbstractPropConditionTask extends ExtendedTask implements Condition {
	protected AbstractPropConditionTask ()
	{
		super();
	}

    private String	_property;
    /**
     * Set the name of the property which will be set if the particular resource
     * is available.
     * @param property the name of the property to set.
     */
    public void setProperty (String property)
    {
        _property = property;
    }

    public String getProperty ()
    {
    	return _property;
    }

    private String	_valueTrue="true";
    /**
     * Set the value to be given to the property if the desired resource is
     * available.
     * @param value the value to be given (default="true").
     */
    public void setValue (String value)
    {
        _valueTrue = value;
    }

    public String getValue ()
    {
    	return _valueTrue;
    }

    private String _valueFalse /* =NULL */;
    /**
     * @param value value to be set if desired resource is NOT available
     * (default=null - i.e., no value set)
     */
    public void setElse (String value)
    {
        _valueFalse = value;
    }

    public String getElse ()
    {
    	return _valueFalse;
    }
    /*
     * @see org.apache.tools.ant.Task#execute()
     */
    @Override
	public void execute () throws BuildException
	{
    	final String	propName=getProperty();
        if ((null == propName) || (propName.length() <= 0))
            throw new BuildException("property attribute is required", getLocation());

        final Project	p=getProject();
        final String	propVal=eval() ? getValue() : getElse();
        if (propVal != null)
            p.setProperty(propName, propVal);
	}
}
