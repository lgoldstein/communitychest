/*
 * 
 */
package net.community.chest.spring.test.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.community.chest.spring.test.entities.NodeEntity;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jan 5, 2011 11:19:15 AM
 */
@Service
@Transactional
public class TestTreeServiceImpl implements TestTreeService {
	private final Random	_randomizer=new Random(System.nanoTime());
	private final NodeEntityDao	_dao;
	protected final NodeEntityDao getNodeEntityDao ()
	{
		return _dao;
	}

	@Inject
	public TestTreeServiceImpl (final NodeEntityDao dao)
	{
		_dao = dao;
	}
	/*
	 * @see net.community.chest.spring.test.beans.TestTreeService#listNodes()
	 */
	@Override
	public List<NodeEntity> listNodes ()
	{
		final NodeEntityDao	dao=getNodeEntityDao();
		return dao.findAll();
	}
	/*
	 * @see net.community.chest.spring.test.beans.TestTreeService#listRoots()
	 */
	@Override
	public List<NodeEntity> listRoots ()
	{
		final NodeEntityDao	dao=getNodeEntityDao();
		return dao.findByCriteria(true, Restrictions.isNull("parent"));
	}
	/*
	 * @see net.community.chest.spring.test.beans.TestTreeService#createNewTree(int, int)
	 */
	@Override
	public NodeEntity createNewTree (int depth, int fanOut)
	{
		final NodeEntityDao	dao=getNodeEntityDao();
		final NodeEntity	tree=createNewTree(createNewNode(null, depth, 0), depth - 1, fanOut);
		dao.create(tree);
		return tree;
	}
	/*
	 * @see net.community.chest.spring.test.beans.TestTreeService#deleteTree(java.lang.Long)
	 */
	@Override
	public NodeEntity deleteTree (Long id)
	{
		final NodeEntityDao	dao=getNodeEntityDao();
		final NodeEntity	root=dao.findById(id);
		if (root == null)
			return null;

		if (!root.isRoot())
			throw new UnsupportedOperationException("Node ID=" + id + " is not a root");

		dao.delete(root);
		return root;
	}

	private NodeEntity createNewTree (NodeEntity parent, int depth, int fanOut)
	{
		if (depth <= 0)
			return parent;

		final int				numChildren=1 + (_randomizer.nextInt(Short.MAX_VALUE) % fanOut);
		final List<NodeEntity>	children=new ArrayList<NodeEntity>();
		for (int	childIndex=1; childIndex <= numChildren; childIndex++)
		{
			final NodeEntity	child=createNewTree(createNewNode(parent, depth, childIndex), depth - 1, fanOut);
			children.add(child);
		}

		parent.setChildren(children);
		return parent;
	}

	private NodeEntity createNewNode (NodeEntity parent, int depth, int nodeIndex)
	{
		final NodeEntity	node=
			new NodeEntity(null, String.valueOf(System.nanoTime())+String.valueOf(_randomizer.nextDouble()), depth + "." + nodeIndex);
		if (parent != null)
			node.setParent(parent);
		return node;
	}
}
