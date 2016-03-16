/*
 *
 */
package net.community.chest.spring.test.entities;

import java.util.List;

import net.community.chest.lang.math.LongsComparator;
import net.community.chest.util.compare.AbstractComparator;

/**
 * @author Lyor G.
 * @since Jul 22, 2010 3:21:31 PM
 *
 */
public class IdableEntityComparator extends AbstractComparator<IdableEntity> {
    /**
     *
     */
    private static final long serialVersionUID = -9193612091935670482L;
    public IdableEntityComparator (boolean ascending) throws IllegalArgumentException
    {
        super(IdableEntity.class, !ascending);
    }
    /*
     * @see net.community.chest.util.compare.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compareValues (IdableEntity v1, IdableEntity v2)
    {
        final Long    i1=(null == v1) ? null : v1.getId(),
                    i2=(null == v2) ? null : v2.getId();

        return LongsComparator.ASCENDING.compare(i1, i2);
    }

    public static final IdableEntityComparator    DEFAULT=new IdableEntityComparator(true);
    public static final int indexOf (final Long id, final List<? extends IdableEntity> entities)
    {
        final int    numEntities=(entities == null) ? 0 : entities.size();
        if ((id == null) || (numEntities <= 0))
            return (-1);

        for (int    eIndex=0; eIndex < numEntities; eIndex++)
        {
            final IdableEntity     e=entities.get(eIndex);
            final Long            eId=(e == null) ? null : e.getId();
            if (id.equals(eId))
                return eIndex;
        }

        return (-1);
    }
}
