package net.community.chest.ui.helpers.button.group;

import javax.swing.JCheckBox;

/**
 * Copyright 2007 as per GPLv2
 *
 * @param <V> The {@link JCheckBox} derived button type
 * @author Lyor G.
 * @since Jul 16, 2007 3:34:36 PM
 */
public class CheckboxButtonGroup<V extends JCheckBox> extends ToggleButtonGroup<V> {
    /**
     *
     */
    private static final long serialVersionUID = 164321855174309259L;

    public CheckboxButtonGroup (Class<V> cbClass)
    {
        super(cbClass);
    }

    public static class JCheckBoxGroup extends CheckboxButtonGroup<JCheckBox> {
        /**
         *
         */
        private static final long serialVersionUID = 4152388448287266212L;

        public JCheckBoxGroup ()
        {
            super(JCheckBox.class);
        }
    }
}
