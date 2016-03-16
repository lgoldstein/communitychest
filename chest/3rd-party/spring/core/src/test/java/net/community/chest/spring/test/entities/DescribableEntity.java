/*
 *
 */
package net.community.chest.spring.test.entities;

/**
 * @author Lyor G.
 * @since Jul 21, 2010 8:48:51 AM
 */
public interface DescribableEntity {
    static final int MAX_DESCRIPTION_LENGTH=80;

    String getDescription ();
    void setDescription (String d);
}
