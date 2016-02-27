/*
 * 
 */
package net.community.apps.eclipse.cp2pom;

import java.util.Collection;
import java.util.List;

import net.community.chest.CoVariantReturn;
import net.community.chest.ui.helpers.table.TypedTable;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 27, 2009 10:37:19 AM
 */
public class RepositoryEntriesTable extends TypedTable<RepositoryEntry> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8267857099080171492L;

	public RepositoryEntriesTable (final RepositoryEntriesModel model)
	{
		super(model);
	}

	public RepositoryEntriesTable ()
	{
		this(new RepositoryEntriesModel());
	}
	/*
	 * @see net.community.chest.ui.helpers.table.TypedTable#getTypedModel()
	 */
	@Override
	@CoVariantReturn
	public RepositoryEntriesModel getTypedModel ()
	{
		return RepositoryEntriesModel.class.cast(super.getTypedModel());
	}

	public List<? extends RepositoryEntry> getEntries ()
	{
		return getTypedModel();
	}

	public void setEntries (final Collection<? extends RepositoryEntry> rl)
	{
		final RepositoryEntriesModel	model=getTypedModel();
		final int						curItems=model.size(),
										numDeps=(null == rl) ? 0 : rl.size();
		model.setEntries(rl);

		// signal change only if had some items or have some new ones
		if ((curItems > 0) || (numDeps > 0))
			model.fireTableDataChanged();
	}
}
