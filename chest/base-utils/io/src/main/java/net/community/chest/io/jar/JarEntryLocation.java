package net.community.chest.io.jar;

import java.util.Map.Entry;
import java.util.jar.JarEntry;

import net.community.chest.util.map.entries.StringMapEntry;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 * 
 * <P>Used to hold a "pair" specifying the {@link JarEntry} and the path of
 * the JAR from which it has been extracted</P>
 * 
 * @author Lyor G.
 * @since Oct 22, 2007 11:01:19 AM
 */
public class JarEntryLocation extends StringMapEntry<JarEntry> {
	public JarEntryLocation ()
	{
		super();
	}

	public JarEntryLocation (Entry<String,? extends JarEntry> e)
	{
		super(e);
	}

	public JarEntryLocation (String key, JarEntry value)
	{
		super(key, value);
	}

	public JarEntryLocation (String key)
	{
		this(key, null);
	}
}
