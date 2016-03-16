/*
 *
 */
package net.community.chest.reflect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Encapsulates access to an attribute as an {@link InvocationHandler}
 * that interprets <U>any</U> <code>get/setXXX</code> invocation as a
 * <code>get/set</code> of the attribute whose {@link AttributeAccessor} was
 * provided</P>
 *
 * @author Lyor G.
 * @since Dec 30, 2008 9:08:08 AM
 */
public class AttributeAccessorInvocationHandler implements InvocationHandler {
    private AttributeAccessor    _aa;
    public AttributeAccessor getAttributeAccessor ()
    {
        return _aa;
    }

    public void setAttributeAccessor (AttributeAccessor aa)
    {
        _aa = aa;
    }

    private Object _inst;
    public Object getInvocationTarget ()
    {
        return _inst;
    }

    public void setInvocationTarget (Object inst)
    {
        _inst = inst;
    }

    public AttributeAccessorInvocationHandler (Object inst, AttributeAccessor aa)
    {
        _inst = inst;
        _aa = aa;
    }

    public AttributeAccessorInvocationHandler ()
    {
        this(null, null);
    }

    public Object invoke (AttributeMethodType aType, Object ... args) throws Throwable
    {
        final AttributeAccessor    aa=getAttributeAccessor();
        if (null == aa)
            throw new IllegalStateException("invoke(" + aType + ") no accessor");

        final Object    inst=getInvocationTarget();
        if (null == inst)
            throw new IllegalStateException("invoke(" + aa.getName() + ") no invocation target");

        if (AttributeMethodType.GETTER.equals(aType)
         || AttributeMethodType.PREDICATE.equals(aType))
        {
            final Method    gm=aa.getGetter();
            if (null == gm)
                throw new NoSuchMethodException("invoke(" + aa.getName() + ") no getter");

            return gm.invoke(inst, args);
        }
        else if (AttributeMethodType.SETTER.equals(aType))
        {
            final Method    sm=aa.getSetter();
            if (null == sm)
                throw new NoSuchMethodException("invoke(" + aa.getName() + ") no setter");

            return sm.invoke(inst, args);
        }

        throw new NoSuchMethodException("invoke(" + aa.getName() + ") bad method type: " + aType);
    }
    /*
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    @Override
    public Object invoke (Object proxy, Method method, Object[] args) throws Throwable
    {
        final AttributeMethodType    aType=AttributeMethodType.classifyAttributeMethod(method);
        return invoke(aType, args);
    }
}
