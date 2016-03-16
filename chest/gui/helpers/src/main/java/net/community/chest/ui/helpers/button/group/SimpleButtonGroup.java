package net.community.chest.ui.helpers.button.group;

import javax.swing.JButton;

/**
 * Copyright 2007 as per GPLv2
 *
 * @param <B> The {@link JButton} derived button class
 * @author Lyor G.
 * @since Jul 16, 2007 3:32:45 PM
 */
public class SimpleButtonGroup<B extends JButton> extends TypedButtonGroup<B> {
    /**
     *
     */
    private static final long serialVersionUID = -4291475949817998537L;

    public SimpleButtonGroup (Class<B> btnClass)
    {
        super(btnClass);
    }

    public static class JButtonGroup extends SimpleButtonGroup<JButton> {
        /**
         *
         */
        private static final long serialVersionUID = -1218129869241721605L;

        public JButtonGroup ()
        {
            super(JButton.class);
        }
    }
}
