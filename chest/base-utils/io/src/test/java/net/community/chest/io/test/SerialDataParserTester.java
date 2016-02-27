/*
 * 
 */
package net.community.chest.io.test;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;

import net.community.chest.io.IOCopier;
import net.community.chest.io.serial.FieldTypeDescriptor;
import net.community.chest.io.serial.SerialDataParser;
import net.community.chest.io.serial.SerialFieldDescriptor;
import net.community.chest.io.serial.SerializedClassDescriptor;
import net.community.chest.test.TestBase;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 22, 2012 10:15:21 AM
 *
 */
public class SerialDataParserTester extends TestBase {
	public SerialDataParserTester ()
	{
		super();
	}

	/////////////////////////////////////////////////////////////////////////
	
	static class SerialDataParserHandler extends SerialDataParser {
		private final PrintStream	out;
		SerialDataParserHandler (File file, PrintStream outStream) throws IOException {
			super(new BufferedInputStream(new FileInputStream(file), IOCopier.DEFAULT_COPY_SIZE));

			if ((out=outStream) == null) {
				throw new IllegalStateException("No output stream");
			}
		}

		@Override
		protected boolean startParsing (short magic, short version) throws IOException
		{
			out.println("startParsing(" + magic + "/" + version + ")");
			return super.startParsing(magic, version);
		}

		@Override
		protected boolean endParsing (short magic, short version) throws IOException
		{
			out.println("endParsing(" + magic + "/" + version + ")");
			return super.endParsing(magic, version);
		}

		@Override
		protected boolean startContent () throws IOException
		{
			out.println("startContent(" + getCurrentContentId() + ")");
			return super.startContent();
		}

		@Override
		protected boolean handleBlockData (byte[] buf, int offset, int len) throws IOException {
			out.println("handleBlockData(" + offset + " - " + (offset+len) + ")"); 
			return super.handleBlockData(buf, offset, len);
		}

		@Override
		protected boolean endContent () throws IOException
		{
			out.println("endContent(" + getCurrentContentId() + ")");
			return super.endContent();
		}

		@Override
		protected boolean startClassDescriptor () throws IOException
		{
			out.println("startClassDescriptor()");
			return true;
		}

		@Override
		protected boolean endClassDescriptor () throws IOException
		{
			out.println("endClassDescriptor()");
			return true;
		}

		@Override
		protected boolean startClassInfo (SerializedClassDescriptor info) throws IOException
		{
			out.println("startClassInfo(" + info + ")");
			return true;
		}

		@Override
		protected boolean endClassInfo (SerializedClassDescriptor info) throws IOException
		{
			out.println("endClassInfo(" + info + ")");
			return true;
		}

		@Override
		protected boolean startClassFields (SerializedClassDescriptor info, int numFields) throws IOException
		{
			out.println("startClassFields(" + info + ")[" + numFields + "]");
			return true;
		}

		@Override
		protected boolean handleFieldInfo (SerializedClassDescriptor info, int numFields, SerialFieldDescriptor field) throws IOException
		{
			out.println("handleFieldInfo(" + info + ")[" + field + "]");
			return true;
		}

		@Override
		protected boolean startCompoundFieldValue (String className, String fieldName, String fieldType, FieldTypeDescriptor typeDescriptor) throws IOException
		{
			out.println("startCompoundFieldValue(" + className + "#" + fieldName + ")[" + fieldType + "/" + typeDescriptor + ")");
			return super.startCompoundFieldValue(className, fieldName, fieldType, typeDescriptor);
		}

		@Override
		protected boolean endCompoundFieldValue (String className, String fieldName, String fieldType, FieldTypeDescriptor typeDescriptor) throws IOException
		{
			out.println("endCompoundFieldValue(" + className + "#" + fieldName + ")[" + fieldType + "/" + typeDescriptor + ")");
			return super.endCompoundFieldValue(className, fieldName, fieldType, typeDescriptor);
		}

		@Override
		protected boolean handleFieldValue (String className, String fieldName, FieldTypeDescriptor typeDescriptor, Serializable value) throws IOException
		{
			out.println("handleFieldValue(" + className + "#" + fieldName + ")[" + typeDescriptor + "]=" + value);
			return true;
		}

		@Override
		protected boolean endClassFields (SerializedClassDescriptor info, int numFields) throws IOException
		{
			out.println("endClassFields(" + info + ")[" + numFields + "]");
			return true;
		}

		@Override
		protected boolean startObject () throws IOException
		{
			out.println("startObject()");
			return true;
		}

		@Override
		protected boolean endObject () throws IOException
		{
			out.println("endObject()");
			return true;
		}
	}

	public static final void testSerialDataParser (final PrintStream out, final BufferedReader in, final File file) {
		for ( ; ; ) {
			out.append(file.getAbsolutePath()).println(':');

			try(SerialDataParserHandler	handler=new SerialDataParserHandler(file, out)) {
				handler.parse();
			} catch(IOException e) {
				System.err.println(e.getClass().getName() + " while parsing " + file.getAbsolutePath() + ": " + e.getMessage());
			}

			final String	ans=getval(out, in, "again [y]/n");
			if ((ans != null) && (ans.length() > 0) && (Character.toLowerCase(ans.charAt(0)) != 'y')) {
				return;
			}
		}
	}

	// args[i] path of a serialized Java object file
	public static final int testSerialDataParser (final PrintStream out, final BufferedReader in, final String ... args)
	{
		final int	numArgs=(null == args) ? 0 : args.length;
		for (int	aIndex=0; ; aIndex++)
		{
			final String	filePath=
				(aIndex < numArgs) ? args[aIndex] : getval(out, in, "file path (or Quit)");
			if ((null == filePath) || (filePath.length() <= 0))
				continue;
			if (isQuit(filePath))
				break;

			try {
				testSerialDataParser(out, in, new File(filePath));
			} catch(Exception e) {
				System.err.println(e.getClass().getName() + " while parsing " + filePath + ": " + e.getMessage());
			}
		}

		return 0;
	}

	//////////////////////////////////////////////////////////////////////////

	public static void main (String[] args)
	{
		final BufferedReader	in=getStdin();
		final int				nErr=testSerialDataParser(System.out, in, args);
		if (nErr != 0)
			System.err.println("test failed (err=" + nErr + ")");
		else
			System.out.println("OK");
	}
}
