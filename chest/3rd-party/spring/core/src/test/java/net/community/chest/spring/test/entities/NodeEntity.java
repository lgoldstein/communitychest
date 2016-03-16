/*
 *
 */
package net.community.chest.spring.test.entities;

import java.util.Collection;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import net.community.chest.io.EOLStyle;

/**
 * Example of mapping a tree-like structure
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jan 5, 2011 10:53:29 AM
 */
@Entity
@Table(name="node_entity")
@XmlRootElement(name="NodeEntity")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class NodeEntity extends AbstractBaseEntity {
    /**
     *
     */
    private static final long serialVersionUID = -8026682391583315191L;
    public NodeEntity ()
    {
        this(null);
    }

    public NodeEntity (Long id)
    {
        this(id, null, null);
    }

    public NodeEntity (Long id, String name, String desc)
    {
        super(id, name, desc);
    }

    private NodeEntity _parent;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="parent_id",insertable=false,updatable=false)
    @XmlTransient    // so we do not create a cyclic reference
    public NodeEntity getParent ()
    {
        return _parent;
    }

    public void setParent (NodeEntity parent)
    {
        _parent = parent;
    }

    @Transient
    @XmlTransient
    public boolean isRoot ()
    {
        return (getParent() == null);
    }

    private List<NodeEntity>    _children;
    @OneToMany(cascade={CascadeType.ALL})
    @OrderColumn(name="child_index")
    @JoinColumn(name="parent_id")
    public List<NodeEntity> getChildren ()
    {
        return _children;
    }

    public void setChildren (List<NodeEntity> children)
    {
        _children = children;
    }

    @Transient
    @XmlTransient
    public String getNodeName ()
    {
        return getName() + "[" + getDescription() + "]";
    }

    private static StringBuilder appendChildren (StringBuilder sb, StringBuilder indent, Collection<? extends NodeEntity> nodes)
    {
        final int    numChildren=(nodes == null) ? 0 : nodes.size();
        if (numChildren <= 0)
            return sb;

        final int    curIndent=indent.length();
        indent.append('\t');    // prepare for the children
        for (final NodeEntity n : nodes) {
            sb.append(indent)
              .append(n.getId())
              .append('@')
              .append(n.getNodeName())
              .append(EOLStyle.LOCAL.getStyleString())
              ;
            appendChildren(sb, indent, n.getChildren());
        }
        indent.setLength(curIndent);    // restore previous indent
        return sb;
    }
    /*
     * @see net.community.chest.spring.test.entities.AbstractBaseEntity#toString()
     */
    @Override
    public String toString ()
    {
        final Collection<? extends NodeEntity>    nodes=getChildren();
        final int                                numChildren=(nodes == null) ? 0 : nodes.size();
        final String                            thisName=getNodeName(), thisId=String.valueOf(getId());
        return appendChildren(
                new StringBuilder(thisId.length() + 4 + numChildren * 32 + thisName.length())
                        .append(thisId)
                        .append('@')
                        .append(thisName)
                        .append(EOLStyle.LOCAL.getStyleString()),
                new StringBuilder(numChildren + 2),
                nodes)
            .toString()
            ;
    }
}
