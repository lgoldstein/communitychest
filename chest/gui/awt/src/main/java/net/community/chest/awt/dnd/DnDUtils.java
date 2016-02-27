/*
 * 
 */
package net.community.chest.awt.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Provides useful utilities for drag-and-drop</P>
 * @author Lyor G.
 * @since Sep 25, 2008 10:01:24 AM
 */
public final class DnDUtils {
	private DnDUtils ()
	{
		// no instance
	}
	/**
	 * Looks for a {@link DataFlavor} that is a list of files or a {@link String}
	 * and returns it as a {@link List} of {@link File}-s
	 * @param dtde The received {@link DropTargetDropEvent} instance
	 * @return The extracted {@link File}-s {@link List} - null/empty if no match found.
	 * <B>Note:</B> caller must call {@link DropTargetDropEvent#dropComplete(boolean)}
	 * to indicate end of handling
	 * @throws IOException If failed to retrieve dragged data
	 * @throws UnsupportedFlavorException  If unknown flavor encountered
	 */
	public static final List<File> getFiles (final DropTargetDropEvent dtde)
		throws UnsupportedFlavorException, IOException
	{
		// get the dropped object and try to figure out what it is
	    final Transferable	tr=(null == dtde) ? null : dtde.getTransferable();
	    final DataFlavor[]	flavors=(null == tr) ? null : tr.getTransferDataFlavors();
	    if ((null == flavors) || (flavors.length <= 0))
	    	return null;

	    for (final DataFlavor df : flavors)
    	{
    		if (null == df)
    			continue;

    		final boolean	hndlIt=
	    			df.isFlavorTextType()
	    		 || df.isFlavorJavaFileListType()
	    		 ;
    		if (!hndlIt)
    			continue;

    		// must be called PRIOR to retrieving data
    		dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

    		final Object	tdo=tr.getTransferData(df);
    		if (tdo instanceof List<?>)
    		{
    			final List<?>	cc=(List<?>) tdo;
    			final Object	fo=cc.get(0);
    			if (!(fo instanceof File))
    				throw new UnsupportedFlavorException(df);

    			@SuppressWarnings("unchecked")
    			final List<File>	fl=(List<File>) cc;
    			return fl;
    		}
    		else
    			return (null == tdo) /* should not happen */ ? null : Arrays.asList(new File(tdo.toString()));
    	}

	    return null;
	}
	/**
	 * Looks for a {@link DataFlavor} that is either a file list or a
	 * text and returns it as a {@link File}
	 * @param dtde The received {@link DropTargetDropEvent} instance
	 * @return The extracted {@link File} - null/empty if no match found.
	 * <B>Note:</B> caller must call {@link DropTargetDropEvent#dropComplete(boolean)}
	 * to indicate end of handling
	 * @throws IllegalStateException If more than one file was dragged
	 * @throws IOException If failed to retrieve dragged data
	 * @throws UnsupportedFlavorException  If unknown flavor encountered
	 */
	public static final File getFilePath (final DropTargetDropEvent dtde)
		throws IllegalStateException, UnsupportedFlavorException, IOException
	{
		final List<? extends File>	fl=getFiles(dtde);
		final int					numFiles=(null == fl) ? 0 : fl.size();
		if (numFiles <= 0)
			return null;
		if (numFiles != 1)
			throw new IllegalStateException("Only one file may be dragged and dropped instead of " + numFiles);

		return fl.get(0);
	}
}
