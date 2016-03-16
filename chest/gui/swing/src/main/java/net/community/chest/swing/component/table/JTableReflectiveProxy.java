/*
 *
 */
package net.community.chest.swing.component.table;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.dom.transform.XmlValueInstantiator;
import net.community.chest.swing.component.JComponentReflectiveProxy;
import net.community.chest.swing.component.list.ListSelectionModeValueStringInstantiator;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <T> Type of reflected {@link JTable}
 * @author Lyor G.
 * @since Aug 14, 2008 2:37:10 PM
 */
public class JTableReflectiveProxy<T extends JTable> extends JComponentReflectiveProxy<T> {
    public JTableReflectiveProxy (Class<T> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    protected JTableReflectiveProxy (Class<T> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }
    // some special attributes of interest
    public static final String    MODEL_ATTR="model",
                                SELECTION_MODE_ATTR="selectionMode",
                                AUTO_RESIZE_MODE_ATTR="autoResizeMode";
    /*
     * @see net.community.chest.awt.dom.UIReflectiveAttributesProxy#resolveAttributeInstantiator(java.lang.String, java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected <C> ValueStringInstantiator<C> resolveAttributeInstantiator (String name, Class<C> type) throws Exception
    {
        if (SELECTION_MODE_ATTR.equalsIgnoreCase(name))
            return (ValueStringInstantiator<C>) ListSelectionModeValueStringInstantiator.DEFAULT;
        else if (AUTO_RESIZE_MODE_ATTR.equalsIgnoreCase(name))
            return (ValueStringInstantiator<C>) TableAutoResizeModeValueStringInstantiator.DEFAULT;
        return super.resolveAttributeInstantiator(name, type);
    }

    public boolean isModelElement (final Element elem, final String tagName)
    {
        return isMatchingElement(elem, tagName, MODEL_ATTR);
    }

    public XmlValueInstantiator<? extends TableModel> getModelInstantiator (final Element elem) throws Exception
    {
        if (null == elem)
            throw new IllegalArgumentException("No element provided");

        return null;
    }

    public TableModel setModel (T src, Element elem) throws Exception
    {
        final XmlValueInstantiator<? extends TableModel>    inst=getModelInstantiator(elem);
        final TableModel                                    m;
        if (null == inst)
        {
            m = src.getModel();
            if (m instanceof XmlConvertible<?>)
                ((XmlConvertible<?>) m).fromXml(elem);
            else
                throw new UnsupportedOperationException("setModel(" + DOMUtils.toString(elem) + ") N/A");
        }
        else
            m = inst.fromXml(elem);

        return m;
    }

    public XmlValueInstantiator<? extends JTableHeader> getTableHeaderInstantiator (final Element elem) throws Exception
    {
        return (null == elem) ? null : JTableHeaderReflectiveProxy.TBLHDR;
    }

    public static final String    TBL_HDR_ELEM_NAME="header";
    public boolean isTableHeaderElement (final Element elem, final String tagName)
    {
        return isMatchingElement(elem, tagName, TBL_HDR_ELEM_NAME);
    }

    public JTableHeader setTableHeader (T src, Element elem) throws Exception
    {
        final XmlValueInstantiator<? extends JTableHeader>    inst=getTableHeaderInstantiator(elem);
        final JTableHeader                                    h=inst.fromXml(elem);
        if (h != null)
        {
            h.setTable(src);
            src.setTableHeader(h);
        }

        return h;
    }
    /*
     * @see net.community.chest.swing.component.JComponentReflectiveProxy#fromXmlChild(javax.swing.JComponent, org.w3c.dom.Element)
     */
    @Override
    public T fromXmlChild (T src, Element elem) throws Exception
    {
        final String tagName=elem.getTagName();
        if (isModelElement(elem, tagName))
        {
            setModel(src, elem);
            return src;
        }
        else if (isTableHeaderElement(elem, tagName))
        {
            setTableHeader(src, elem);
            return src;
        }

        return super.fromXmlChild(src, elem);
    }

    public static final JTableReflectiveProxy<JTable>    TBL=
            new JTableReflectiveProxy<JTable>(JTable.class, true);
}
