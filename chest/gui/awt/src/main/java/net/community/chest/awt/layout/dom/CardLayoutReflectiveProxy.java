/*
 *
 */
package net.community.chest.awt.layout.dom;

import java.awt.CardLayout;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <L> The reflected {@link CardLayout}
 * @author Lyor G.
 * @since Aug 20, 2008 1:42:10 PM
 */
public class CardLayoutReflectiveProxy<L extends CardLayout> extends AbstractLayoutManager2ReflectiveProxy<L> {
    public CardLayoutReflectiveProxy (Class<L> objClass) throws IllegalArgumentException
    {
        super(objClass);
    }

    public static final CardLayoutReflectiveProxy<CardLayout>    CARD=
            new CardLayoutReflectiveProxy<CardLayout>(CardLayout.class);
}
