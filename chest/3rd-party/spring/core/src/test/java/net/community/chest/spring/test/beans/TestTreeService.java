/*
 * 
 */
package net.community.chest.spring.test.beans;

import java.util.List;

import net.community.chest.spring.test.entities.NodeEntity;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jan 5, 2011 11:04:24 AM
 */
public interface TestTreeService {
	List<NodeEntity> listNodes ();
	List<NodeEntity> listRoots ();
	NodeEntity createNewTree (int depth, int fanOut);
	NodeEntity deleteTree (Long id);
}
