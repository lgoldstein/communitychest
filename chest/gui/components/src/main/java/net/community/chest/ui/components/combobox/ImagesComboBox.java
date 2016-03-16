/*
 *
 */
package net.community.chest.ui.components.combobox;

import java.awt.Image;

import net.community.chest.ui.helpers.combobox.TypedComboBox;
import net.community.chest.ui.helpers.combobox.TypedComboBoxModel;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <I> The type of {@link Image} being attached to each row
 * @author Lyor G.
 * @since Dec 18, 2008 9:49:40 AM
 */
public class ImagesComboBox<I extends Image> extends TypedComboBox<I> {
    /**
     *
     */
    private static final long serialVersionUID = -2277437915014029707L;

    public ImagesComboBox (Class<I> valsClass)
    {
        super(valsClass);
    }

    public ImagesComboBox (TypedComboBoxModel<I> model)
    {
        super(model);
    }

    public static class SimpleImagesComboBox extends ImagesComboBox<Image> {
        /**
         *
         */
        private static final long serialVersionUID = 8702603810057542670L;

        public SimpleImagesComboBox ()
        {
            super(Image.class);
        }

        public SimpleImagesComboBox (TypedComboBoxModel<Image> model)
        {
            super(model);
        }
    }
}
