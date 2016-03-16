package net.community.chest.dom.proxy;

import java.lang.reflect.Field;
import java.util.Map;

import net.community.chest.reflect.FieldsAccessor;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <V> Type of accessed object(s)
 * @author Lyor G.
 * @since Jan 24, 2008 2:26:15 PM
 */
public class ReflectiveFieldsProxy<V> extends AbstractReflectiveProxy<V,Field> {
    private final Class<?>    _baseClass;
    public final Class<?> getBaseClass ()
    {
        return _baseClass;
    }

    public ReflectiveFieldsProxy (Class<V> valsClass, Class<?> baseClass) throws IllegalArgumentException
    {
        super(valsClass, Field.class);

        if (null == (_baseClass=baseClass))
            throw new IllegalArgumentException("No base class specified");
    }

    public ReflectiveFieldsProxy (Class<V> valsClass) throws IllegalArgumentException
    {
        this(valsClass, Object.class);
    }
    /**
     * @return Are fields mapped as case sensitive or not attributes (default
     * is FALSE - i.e., case <U>insensitive</U> mapping)
     */
    public boolean isCaseSensitive ()
    {
        return false;
    }
    /**
     * @return TRUE=ignore if same field found in other super-classes (in
     * which case the top-most field is the accessed one). FALSE=throw an
     * {@link Exception} if duplicates found (depending on the {@link #isCaseSensitive()}
     * mapping) - default=TRUE (i.e., ignore...)
     */
    public boolean isIgnoreDuplicates ()
    {
        return true;
    }
    /*
     * @see net.community.chest.dom.transform.AbstractReflectiveProxy#extractAccessorsMap(java.lang.Class)
     */
    @Override
    protected Map<String,Field> extractAccessorsMap (Class<V> valsClass)
    {
        return FieldsAccessor.getDeclaredFieldsMap(valsClass, getBaseClass(), isCaseSensitive(), isIgnoreDuplicates());
    }
    /*
     * @see net.community.chest.dom.transform.AbstractReflectiveProxy#resolveAccessorType(java.lang.reflect.AccessibleObject)
     */
    @Override
    public Class<?> resolveAccessorType (Field acc)
    {
        return (null == acc) ? null : acc.getType();
    }
    /*
     * @see net.community.chest.dom.transform.AbstractReflectiveProxy#getAttributeRetriever(java.lang.Object, java.lang.String, java.lang.Class)
     */
    @Override
    public Field getAttributeRetriever (V src, String attrName, Class<?> aType) throws Exception
    {
        if ((null == attrName) || (attrName.length() <= 0) || (null == aType))
            return null;

        final Map<String,? extends Field>    fm=getAccessorsMap();
        final Field                            f=((null == fm) || (fm.size() <= 0)) ? null : fm.get(attrName);
        if (null == f)
            return null;

        final Class<?>    fType=resolveAccessorType(f);
        if ((null == fType) || (!aType.isAssignableFrom(fType)))
            throw new NoSuchFieldException("getAttributeRetriever(" + attrName + ") mismatched types: expect=" + aType.getName() + ";got=" + ((null == fType) ? null : fType.getName()));

        return f;
    }
    /*
     * @see net.community.chest.dom.transform.AbstractReflectiveProxy#getAttributeValue(java.lang.Object, java.lang.String, java.lang.Class)
     */
    @Override
    public Object getAttributeValue (V src, String attrName, Class<?> type) throws Exception
    {
        final Field        f=getAttributeRetriever(src, attrName, type);
        final Object    o=f.get(src);
        return o;
    }
    /*
     * @see net.community.chest.dom.transform.AbstractReflectiveProxy#updateObjectAttribute(java.lang.Object, java.lang.String, java.lang.String, java.lang.reflect.AccessibleObject)
     */
    @Override
    protected V updateObjectAttribute (V src, String aName, String aValue, Field setter) throws Exception
    {
        final Class<?>    fType=resolveAccessorType(setter);
        final Object    objValue=getObjectAttributeValue(src, aName, aValue, fType);
        setter.set(src, objValue);
        return src;
    }
}
