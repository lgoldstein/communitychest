/*
 *
 */
package net.community.chest.dom.impl;

import org.w3c.dom.DOMException;
import org.w3c.dom.ProcessingInstruction;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 26, 2009 9:47:57 AM
 */
public class StandaloneProcessingInstructionImpl
        extends BaseNodeImpl<StandaloneProcessingInstructionImpl>
        implements ProcessingInstruction {
    public StandaloneProcessingInstructionImpl (String tgtName, String tgtData)
    {
        super(StandaloneProcessingInstructionImpl.class, tgtName, tgtData);
    }

    public StandaloneProcessingInstructionImpl (String tgtName)
    {
        this(tgtName, null);
    }

    public StandaloneProcessingInstructionImpl ()
    {
        this(null);
    }
    /*
     * @see org.w3c.dom.Node#getNodeType()
     */
    @Override
    public final short getNodeType ()
    {
        return PROCESSING_INSTRUCTION_NODE;
    }
    /*
     * @see org.w3c.dom.ProcessingInstruction#getTarget()
     */
    @Override
    public String getTarget ()
    {
        return getNodeName();
    }

    public void setTarget (String tgt)
    {
        setNodeName(tgt);
    }
    /*
     * @see org.w3c.dom.ProcessingInstruction#getData()
     */
    @Override
    public String getData ()
    {
        return getNodeValue();
    }
    /*
     * @see org.w3c.dom.ProcessingInstruction#setData(java.lang.String)
     */
    @Override
    public void setData (String data) throws DOMException
    {
        setNodeValue(data);
    }
}
