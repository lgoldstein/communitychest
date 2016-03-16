/*
 *
 */
package net.community.apps.tools.filesync;

import java.awt.Component;
import java.awt.GridLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.swing.JPanel;

import net.community.chest.awt.attributes.Selectible;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 15, 2009 3:55:26 PM
 */
public class FilesList extends JPanel {
    /**
     *
     */
    private static final long serialVersionUID = -1924307368319314876L;

    public FilesList ()
    {
        super(new GridLayout(0, 1));
    }

    public Collection<Map.Entry<File,File>> getFilePairs (boolean selOnly)
    {
        final Component[]                        ca=getComponents();
        final int                                numComps=(null == ca) ? 0 : ca.length;
        final Collection<Map.Entry<File,File>>    pl=(numComps <= 0) ? null : new ArrayList<Map.Entry<File,File>>(numComps);
        if (numComps > 0)
        {
            for (final Component c : ca)
            {
                if (!(c instanceof Map.Entry<?,?>))
                    continue;

                if (selOnly)
                {
                    if (!(c instanceof Selectible))
                        continue;
                    if (!((Selectible) c).isSelected())
                        continue;
                }

                final Map.Entry<?,?>    fp=(Map.Entry<?,?>) c;
                final Object            k=fp.getKey(), v=fp.getValue();
                if ((k instanceof File) && (v instanceof File))
                {
                    @SuppressWarnings("unchecked")
                    final Map.Entry<File,File>    ff=(Map.Entry<File,File>) fp;
                    pl.add(ff);
                }
            }
        }

        return pl;
    }
}
