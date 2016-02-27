/*
 * 
 */
package net.community.chest.apache.httpclient.methods;

import org.apache.commons.httpclient.methods.EntityEnclosingMethod;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Provides the ability to attach an entity to the GET request</P<
 * @author Lyor G.
 * @since Aug 21, 2008 4:15:33 PM
 */
public class EntityEnclosingGetMethod extends EntityEnclosingMethod {
    /*
     * @see org.apache.commons.httpclient.HttpMethodBase#getName()
     */
    @Override
	public String getName ()
    {
    	return "GET";
    }

    public EntityEnclosingGetMethod ()
    {
        super();
    }

    public EntityEnclosingGetMethod (String uri)
    {
        super(uri);
    }
}
