/*
 * 
 */
package net.community.chest.net.snmp.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;

import net.community.chest.io.FileUtil;
import net.community.chest.net.snmp.MIBAttributeEntry;
import net.community.chest.net.snmp.OIDAliasMap;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 2, 2011 12:29:32 PM
 */
public class MIBEntriesReaderTest extends Assert {
	public MIBEntriesReaderTest ()
	{
		super();
	}

	@Ignore
	@Test
	public void testMIBEntriesReader () throws IOException
	{
		final String[]	MIBS={	// NOTE: order may be important due to OID(s)
				"SNMPv2-MIB",
				"IF-MIB",
				"INET-ADDRESS-MIB",
				"TCP-MIB",
				"UDP-MIB"
			};
		OIDAliasMap	oidsMap=OIDAliasMap.getDefaultAliases();
		final MIBDefinitionResolver	testResolver=new LoggingMIBDefinitionResolver(System.out, DefaultMIBDefinitionResolver.INSTANCE);
		for (final String mibName : MIBS)
		{
			final URL	url=testResolver.lookupMIB(mibName);
			assertNotNull("Missing " + mibName + " resource", url);

			InputStream			inStream=testResolver.openMIB(mibName);
			MIBEntriesReader	r=null;
			try
			{
				r = new MIBEntriesReader(oidsMap, new InputStreamReader(inStream));
				r.setResolver(testResolver);
				
				final Collection<MIBAttributeEntry>	el=new LinkedList<MIBAttributeEntry>(), cl=r.readMIBEntries(el);
				assertSame("Mismatched entries list instance(s)", el, cl);
			}
			catch(IOException e)
			{
				fail("Failed (" + e.getClass().getName() + ") to read mib=" + mibName + ": " + e.getMessage());
				throw e;	// dead code actually...
			}
			finally
			{
				if (r != null)
					oidsMap = r.getAliases();

				FileUtil.closeAll(r, inStream);
			}
		}
	}
}
