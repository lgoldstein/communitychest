/*
 * 
 */
package net.community.chest.itext.core.test;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PRAcroForm;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.SimpleBookmark;

import net.community.chest.test.TestBase;
import net.community.chest.util.set.SetsUtils;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 30, 2009 11:23:19 AM
 */
public class PDFTester extends TestBase {
	public static final void testPDFConcatenate (
			final PrintStream out, final BufferedReader in, final String oFile, final Collection<String> iFiles)
	{
		String	outFile=oFile;
		while ((null == outFile) || (outFile.length() <= 0))
			outFile = getval(out, in, "output file path (or Quit)");
		if (isQuit(outFile)) return;

		Collection<String>	inFiles=iFiles;
		while ((null == inFiles) || (inFiles.size() <= 0))
		{
			for (int	fIndex=1; ; fIndex++)
			{
				final String	f=getval(out, in, "input file #" + fIndex + " path [ENTER=end/Quit]");
				if ((null == f) || (f.length() <= 0))
					break;
				if (isQuit(f)) return;
			}
		}

		int 			pageOffset=0;
		Document		document=null;
		PdfCopy			writer=null;
		List<Object>	master=new ArrayList<Object>();
		for (final String f : inFiles)
		{
			out.println("\tProcessing input file=" + f);
			try
			{
	            // we create a reader for a certain document
	            final PdfReader reader=new PdfReader(f);
	            reader.consolidateNamedDestinations();
	            // we retrieve the total number of pages
	            int n = reader.getNumberOfPages();
	            List<?> bookmarks = SimpleBookmark.getBookmark(reader);
	            if (bookmarks != null)
	            {
	                if (pageOffset != 0)
	                    SimpleBookmark.shiftPageNumbers(bookmarks, pageOffset, null);
	                master.addAll(bookmarks);
	            }
	            pageOffset += n;
            
	            if (null == document)
	            {
	                // step 1: creation of a document-object
	                document = new Document(reader.getPageSizeWithRotation(1));
	                // step 2: we create a writer that listens to the document
	                writer = new PdfCopy(document, new FileOutputStream(outFile));
	                // step 3: we open the document
	                document.open();
	                out.println("\tOpened output=" + outFile);
	            }

	            // step 4: we add content
	            for (int i = 1; i <= n; i++)
	            {
	                final PdfImportedPage page=writer.getImportedPage(reader, i);
	                writer.addPage(page);
	            }

	            final PRAcroForm form=reader.getAcroForm();
	            if (form != null)
	            	writer.copyAcroForm(reader);
			}
			catch(Exception e)
			{
				System.err.println(e.getClass().getName() + " while handle input=" + f + ": " + e.getMessage());
			}
		}

		try
		{
	        if (!master.isEmpty())
	            writer.setOutlines(master);
	        // step 5: we close the document
	        document.close();
	        out.println("\tClosing output=" + outFile);
		}
		catch(Exception e)
		{
			System.err.println(e.getClass().getName() + " while finalize output: " + e.getMessage());
		}
	}

	public static final void testPDFConcatenate (
			final PrintStream out, final BufferedReader in, final String oFile, final String ... iFiles)
	{
		testPDFConcatenate(out, in, oFile, SetsUtils.setOf(String.CASE_INSENSITIVE_ORDER, iFiles));
	}

	public static final void main (String[] args)
	{
		testPDFConcatenate(System.out, getStdin(), null, args);
	}
}
