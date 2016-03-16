/*
 *
 */
package net.community.chest.spring.test.beans;

import java.util.List;

import net.community.chest.spring.test.entities.EmbeddingEntity;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jan 20, 2011 10:31:15 AM
 */
public interface TestEmbeddingService {
    List<EmbeddingEntity> list ();
    Long create (EmbeddingEntity entity);
}
