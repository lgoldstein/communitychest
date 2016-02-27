/*
 * 
 */
package net.community.chest.db.sql.persistence;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Nov 13, 2011 2:20:46 PM
 */
public final class PersistenceUtils {
	private PersistenceUtils ()
	{
		// no instance
	}

	public static final String resolveTableName (final Class<?> clazz)
	{
		if (clazz == null)
			return null;

		{
			final Table	tbl=clazz.getAnnotation(Table.class);
			if (tbl != null)
				return tbl.name();
		}

		{
			final Entity	entity=clazz.getAnnotation(Entity.class);
			if (entity != null)
				return entity.name();
		}

		return clazz.getSimpleName();
	}
}
