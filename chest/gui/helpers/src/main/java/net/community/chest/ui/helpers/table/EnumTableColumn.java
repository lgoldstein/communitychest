package net.community.chest.ui.helpers.table;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import net.community.chest.CoVariantReturn;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.ReflectiveAttributesProxy;
import net.community.chest.lang.TypedValuesContainer;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.swing.component.table.BaseTableColumn;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Provides a {@link javax.swing.table.TableColumn} implementation
 * with {@link Enum}-s as the column "model index" identifier (actually
 * the {@link Enum#ordinal()}). It also uses the {@link #getIdentifier()}
 * to return the assigned enumeration value</P>
 *
 * @param <E> The {@link Enum} used to identify the column
 * @author Lyor G.
 * @since Aug 6, 2007 8:25:02 AM
 */
public class EnumTableColumn<E extends Enum<E>> extends BaseTableColumn implements TypedValuesContainer<E> {
    /**
     *
     */
    private static final long serialVersionUID = -4486580386474994907L;
    private final Class<E>    _colClass;
    /*
     * @see net.community.chest.lang.TypedValuesContainer#getValuesClass()
     */
    @Override
    public final /* no cheating */ Class<E> getValuesClass ()
    {
        return _colClass;
    }
    /*
     * @see javax.swing.table.TableColumn#getIdentifier()
     */
    @Override
    @CoVariantReturn
    public final E getIdentifier ()
    {
        return getValuesClass().cast(super.getIdentifier());
    }
    /*
     * @see javax.swing.table.TableColumn#getModelIndex()
     */
    @Override
    public int getModelIndex ()
    {
        final E    colId=getIdentifier();
        if (colId != null)
            return colId.ordinal();

        return super.getModelIndex();
    }
    /* Do not allow changing it - ignore if same value re-set
     * @see javax.swing.table.TableColumn#setIdentifier(java.lang.Object)
     */
    @Override
    public final void setIdentifier (final Object colId)
    {
        final Object    curId=super.getIdentifier();
        if ((null == colId)
         || ((curId != null) && (!colId.equals(curId))))
            throw new UnsupportedOperationException("setIdentifier(" + colId + ") not allowed for " + EnumTableColumn.class.getName());
        super.setIdentifier(colId);
        setModelIndex(getValuesClass().cast(colId).ordinal());
    }

    public E getColumnValue ()
    {
        return getIdentifier();
    }

    public void setColumnValue (E colValue)
    {
        setIdentifier(colValue);
    }

    public EnumTableColumn (Class<E> colClass, E colIndex, int colWidth, TableCellRenderer colRenderer, TableCellEditor colEditor) throws IllegalArgumentException
    {
        super((null == colIndex) ? 0 : colIndex.ordinal(), colWidth, colRenderer, colEditor);

        if ((null == (_colClass=colClass)) || (null == colIndex))
            throw new IllegalArgumentException("Incomplete " + EnumTableColumn.class.getName() + " specification");

        super.setIdentifier(colIndex);
    }

    public EnumTableColumn (Class<E> colClass, E colIndex, int colWidth)
    {
        this(colClass, colIndex, colWidth, null, null);
    }

    public EnumTableColumn (Class<E> colClass, E colIndex)
    {
        this(colClass, colIndex, DEFAULT_WIDTH);
    }

    private Class<?>    _colValueClass=Object.class;
    /**
     * @return expected column value class - default is {@link Object}
     */
    public Class<?> getColumnValueClass ()
    {
        return _colValueClass;
    }
    /**
     * @param colValueClass column value class - ignored if null
     */
    public void setColumnValueClass (final Class<?> colValueClass)
    {
        if (colValueClass != null)
            _colValueClass = colValueClass;
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        final Class<?>    vClass=getValuesClass();
        return ((null == vClass) ? null : vClass.getName()) + "[" + getIdentifier() + "]";
    }
    // returns the set class (same as current if null/empty string)
    public Class<?> setColumnValueClass (final String colClass) throws Exception
    {
        final Class<?>    colValClass;
        if ((colClass != null) && (colClass.length() > 0))
        {
            colValClass = ClassUtil.loadClassByName(colClass);
            setColumnValueClass(colValClass);
        }
        else
            colValClass = getColumnValueClass();

        return colValClass;
    }
    // OK if no {@link #CLASS_ATTR} specified - returns null if value not found
    public Class<?> setColumnValueClass (final Element elem) throws Exception
    {
        final String    colClass=elem.getAttribute(ReflectiveAttributesProxy.CLASS_ATTR);
        if ((colClass != null) && (colClass.length() > 0))
            return setColumnValueClass(colClass);

        return null;
    }
    /*
     * @see net.community.chest.swing.component.table.BaseTableColumn#fromXml(org.w3c.dom.Element)
     */
    @Override
    @CoVariantReturn
    public EnumTableColumn<E> fromXml (final Element elem) throws Exception
    {
        if (super.fromXml(elem) != this)
            throw new IllegalStateException("fromXml(" + DOMUtils.toString(elem) + " mismatched initialization instances");

        // separate handling since default reflective proxy is abstract
        setColumnValueClass(elem);
        return this;
    }

    public EnumTableColumn (Class<E> colClass, Element elem) throws Exception
    {
        if (null == (_colClass=colClass))
            throw new IllegalArgumentException("No column class specified for XML restore");

        final EnumTableColumn<E>    inst=fromXml(elem);
        if (inst != this)
            throw new IllegalStateException(ClassUtil.getConstructorExceptionLocation(getClass()) + " mismatched restored XML instances");

        final E            colIndex=getIdentifier();
        final Class<?>    idxClass=(null == colIndex) ? null : colIndex.getClass();
        if ((null == idxClass) || (!colClass.isAssignableFrom(idxClass)))
            throw new IllegalStateException(ClassUtil.getConstructorExceptionLocation(getClass()) + " Mismatched column index classes after XML restore: got=" + ((null == idxClass) ? null : idxClass.getName()) + "/expected=" + colClass.getName());
    }
}
