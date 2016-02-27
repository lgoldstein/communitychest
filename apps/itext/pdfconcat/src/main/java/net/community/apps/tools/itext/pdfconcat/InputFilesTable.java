/*
 * 
 */
package net.community.apps.tools.itext.pdfconcat;

import java.io.File;
import java.util.Collection;

import net.community.chest.ui.helpers.table.TypedTable;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 30, 2009 12:29:38 PM
 */
public class InputFilesTable extends TypedTable<File> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6901209894720663586L;

	public InputFilesTable (InputFilesModel m)
	{
		super(m);
	}

	public InputFilesTable ()
	{
		super(new InputFilesModel());
	}

	public Collection<? extends File> getInputFiles ()
	{
		return getTypedModel();
	}
}
