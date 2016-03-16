/*
 *
 */
package net.community.chest.spring.test.entities;

import javax.xml.bind.annotation.XmlRegistry;

/**
 * @author Lyor G.
 * @since Jul 26, 2010 1:23:26 PM
 */
@XmlRegistry
public final class ObjectFactory {
    public ObjectFactory ()
    {
        super();
    }

    public DateTimeEntity createDateTimeEntity ()
    {
        return new DateTimeEntity();
    }

    public NodeEntity createNodeEntity ()
    {
        return new NodeEntity();
    }
}
