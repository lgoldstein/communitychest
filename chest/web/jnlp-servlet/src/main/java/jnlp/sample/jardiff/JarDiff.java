/*
 * @(#)JarDiff.java	1.7 05/11/17
 * 
 * Copyright (c) 2006 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * -Redistribution of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 *
 * -Redistribution in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */

package jnlp.sample.jardiff;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;


/**
 * JarDiff is able to create a jar file containing the delta between two
 * jar files (old and new). The delta jar file can then be applied to the
 * old jar file to reconstruct the new jar file.
 * <p>
 * Refer to the JNLP spec for details on how this is done.
 *
 * @version 1.13, 06/26/03
 */
public class JarDiff implements JarDiffConstants {
    private static final int DEFAULT_READ_SIZE = 2048;
    protected static final byte[] newBytes = new byte[DEFAULT_READ_SIZE];
    protected static final byte[] oldBytes = new byte[DEFAULT_READ_SIZE];
    private static ResourceBundle _resources = null;

    // The JARDiff.java is the stand-alone jardiff.jar tool. Thus, we do not
    // depend on Globals.java and other stuff here. Instead, we use an explicit
    // _debug flag.
    private static boolean _debugMode;
    public static final boolean isDebugMode ()
    {
    	return _debugMode;
    }

    public static ResourceBundle getResources ()
    {
    	if (_resources == null)
    		_resources = ResourceBundle.getBundle("jnlp/sample/jardiff/resources/strings");
    	return _resources;
    }
    /*
     * Creates a patch from the two passed in files, writing the result
     * to <code>os</code>.
     */
    public static void createPatch (String oldPath, String newPath, OutputStream os, boolean minimal) throws IOException
    {
        final JarFile2 oldJar=new JarFile2(oldPath), newJar=new JarFile2(newPath);
      
        try
        {
        	final Map<String,String> moved=new TreeMap<String,String>();
        	final Collection<String> implicit=new TreeSet<String>();
        	final Collection<String> moveSrc=new TreeSet<String>();
        	final Collection<String> newEntries=new TreeSet<String>();


          // FIRST PASS
          // Go through the entries in new jar and
          // determine which files are candidates for implicit moves
          // ( files that has the same filename and same content in old.jar
          // and new.jar )
          // and for files that cannot be implicitly moved, we will either
          // find out whether it is moved or new (modified)
          for (final Iterator<JarEntry> entries = newJar.getJarEntries(); (entries != null) && entries.hasNext(); )
          {
        	  final JarEntry	newEntry=entries.next(); 
        	  final String 		newname=newEntry.getName();
        	  // Return best match of contents, will return a name match if possible 
        	  String oldname=oldJar.getBestMatch(newJar, newEntry);
        	  if ((oldname == null) || (oldname.length() <= 0))
        	  {
                    // New or modified entry
                    if (isDebugMode())
                        System.out.println("NEW: "+ newname); 
                    newEntries.add(newname);
        	  }
        	  else
        	  {
                    // Content already exist - need to do a move
		    
                    // Should do implicit move? Yes, if names are the same, and
                    // no move command already exist from oldJar
                    if (oldname.equals(newname) && !moveSrc.contains(oldname))
                    {
                        if (isDebugMode()) 
                            System.out.println(newname + " added to implicit set!");

                        implicit.add(newname);
		            }
                    else
                    {
	                    // The 1.0.1/1.0 JarDiffPatcher cannot handle
	                    // multiple MOVE command with same src.
	                    // The work around here is if we are going to generate
	                    // a MOVE command with duplicate src, we will
	                    // instead add the target as a new file.  This way
	                    // the jardiff can be applied by 1.0.1/1.0
	                    // JarDiffPatcher also.
                        if (!minimal && (implicit.contains(oldname) || moveSrc.contains(oldname)))
                        {
                            // generate non-minimal jardiff
                            // for backward compatibility
                            if (isDebugMode())
				    
                                System.out.println("NEW: "+ newname); 
                            newEntries.add(newname);			
                        }
                        else
                        {
                            // Use newname as key, since they are unique
                            if (isDebugMode())
                                System.err.println("moved.put " + newname + " " + oldname); 
                            moved.put(newname, oldname);
                            moveSrc.add(oldname);
                        }
                        // Check if this disables an implicit 'move <oldname> <oldname>'
                        if (implicit.contains(oldname) && minimal)
                        { 
                           if (isDebugMode())
                           {
                              System.err.println("implicit.remove " + oldname);
                              System.err.println("moved.put " + oldname + " " + oldname); 
                           }

                           implicit.remove(oldname);
                           moved.put(oldname, oldname);
                           moveSrc.add(oldname);		     
                        }
                    } 
                }
          } // for loop
	
          // SECOND PASS: <deleted files> = <oldjarnames> - <implicitmoves> - 
          // <source of move commands> - <new or modified entries>
          Collection<String> deleted = new LinkedList<String>();
          for (Iterator<JarEntry> entries=oldJar.getJarEntries(); (entries != null) && entries.hasNext(); )
          {
        	  JarEntry oldEntry = entries.next();
        	  String oldName = oldEntry.getName();
        	  if (!implicit.contains(oldName)
        	   && !moveSrc.contains(oldName)
        	   && !newEntries.contains(oldName))
        	  {
        		  if (isDebugMode())
        			  System.err.println("deleted.add " + oldName);
        		  deleted.add(oldName);
              }
          }
	
          //DEBUG
          if (isDebugMode())
          {
              //DEBUG:  print out moved map
        	  {
	              final Collection<? extends Map.Entry<String,String>>	entries=moved.entrySet();
	              if ((entries != null) && (entries.size() > 0))
	              {
	                  System.out.println("MOVED MAP!!!");
	                  for (final Map.Entry<String,String> ee : entries)
	                  {
	                      final String 	newName=(null == ee) ? null : ee.getKey(),
	                    		  		oldName=(null == ee) ? null : ee.getValue();	
	                      System.out.println("key is " + newName + " value is " + oldName);
	                  }
	              }
        	  }
	    
              //DEBUG:  print out IMOVE map
        	  {
        		  final Iterator<String>	entries=implicit.iterator();
        		  if (entries != null)
        		  {
        			  System.out.println("IMOVE MAP!!!");
        			  while (entries.hasNext())
        			  {
        				  final String newName=entries.next();		 
        				  System.out.println("key is " + newName);
        			  }
        		  }
        	  }
          }

          JarOutputStream jos = new JarOutputStream(os);

          // Write out all the MOVEs and REMOVEs
          createIndex(jos, deleted, moved);

          // Put in New and Modified entries
          for (final String newName : newEntries)
          {
        	  if (isDebugMode())
        		  System.out.println("New File: " + newName);
        	  writeEntry(jos, newJar.getEntryByName(newName), newJar);
          }
     
    
          jos.finish();
          jos.close();

        }
        catch (IOException ioE)
        {
        	throw ioE;
        }
        finally
        {
	          try
	          { 
	              oldJar.getJarFile().close();
	          }
	          catch (IOException e1)
	          {
	              //ignore
	          }
	          try
	          { 
	              newJar.getJarFile().close();
	          }
	          catch (IOException e1)
	          {
	            //ignore
	          }
        } // finally
    }

    /**
     * Writes the index file out to <code>jos</code>.
     * <code>oldEntries</code> gives the names of the files that were removed,
     * <code>movedMap</code> maps from the new name to the old name.
     */
    private static void createIndex (JarOutputStream jos, Collection<String> oldEntries, Map<String,String> movedMap) throws IOException
    {
        final StringWriter writer=new StringWriter()
        			.append(VERSION_HEADER)
        			.append("\r\n")
        			;

        // Write out entries that have been removed
        for (final String name : oldEntries)
        {
            writer.append(REMOVE_COMMAND)
            	  .append(' ')
            	  ;
            writeEscapedString(writer, name)
            	.append("\r\n")
            	;
        }

        // And those that have moved
        final Collection<? extends Map.Entry<String,String>>	entries=movedMap.entrySet();
        if ((entries != null) && (entries.size() > 0))
        {
            for (final Map.Entry<String,String> ee : entries)
            {
                final String 	newName=(null == ee) ? null : ee.getKey(),
              		  			oldName=(null == ee) ? null : ee.getValue();	
                writer.append(MOVE_COMMAND)
                	  .append(' ')
                	  ;
                writeEscapedString(writer, oldName)
                	.append(' ')
                	;
                writeEscapedString(writer, newName)
                	.append("\r\n")
                	;
            }
        }

        final JarEntry	je=new JarEntry(INDEX_NAME);
        final String	js=writer.toString();
        final byte[]	bytes=js.getBytes("UTF-8");
        writer.close();
        jos.putNextEntry(je);
        jos.write(bytes, 0, bytes.length);
    }

    private static final <W extends Writer> W writeEscapedString (W writer, String string) throws IOException
    {
    	if ((null == string) || (string.length() <= 0))
    		return writer;

    	int index = 0;
        int last = 0;
        char[] chars = null;

        while ((index=string.indexOf(' ', index)) >= 0)
        {
            if (last != index)
            {
                if (chars == null)
                    chars = string.toCharArray();
                writer.write(chars, last, index - last);
            }

            last = index;
            index++;
            writer.write('\\');
        }

        if (last != 0)
            writer.write(chars, last, chars.length - last);
        else // no spaces
            writer.write(string);
        return writer;
    }

    private static void writeEntry(JarOutputStream jos, JarEntry entry,
                            JarFile2 file) throws IOException {
        writeEntry(jos, entry, file.getJarFile().getInputStream(entry));
    }

    private static void writeEntry(JarOutputStream jos, JarEntry entry,
                            InputStream data) throws IOException {
        jos.putNextEntry(entry);

        try {
            // Read the entry
            int size = data.read(newBytes);

            while (size != -1) {
                jos.write(newBytes, 0, size);
                size = data.read(newBytes);
            }
        } catch(IOException ioE) {
            throw ioE;
        } finally {
            try {
                data.close();
            } catch(IOException e){
                //Ignore
            }

        }
    }
    /*
     * JarFile2 wraps a JarFile providing some convenience methods.
     */
    private static class JarFile2 {
        private final JarFile _jar;
        private final List<JarEntry> _entries;
        private final Map<String,JarEntry> _nameToEntryMap;
        private final Map<Long,Collection<JarEntry>> _crcToEntryMap;
            
        public JarFile2 (String path) throws IOException
        {
            _jar = new JarFile(new File(path));
            _nameToEntryMap = new TreeMap<String,JarEntry>();
            _entries = new ArrayList<JarEntry>();
            _crcToEntryMap = new HashMap<Long,Collection<JarEntry>>();

            index();
        }

        public JarFile getJarFile () { return _jar; }
        public Iterator<JarEntry> getJarEntries() { return _entries.iterator(); }
        public JarEntry getEntryByName(String name) { return _nameToEntryMap.get(name); }
		/*
		 * Returns true if the two InputStreams differ.
		 */
		private static boolean differs (InputStream oldIS, InputStream newIS) throws IOException
		{
			int newSize = 0;
			int oldSize;
			int total = 0;
    
			try
			{
				while (newSize != -1)
				{
					newSize = newIS.read(newBytes);
					oldSize = oldIS.read(oldBytes);
		
					if (newSize != oldSize)
					{
						if (isDebugMode())
							System.out.println("\tread sizes differ: " + newSize + " " + oldSize + " total " + total);
						return true;
					}

					if (newSize > 0)
					{
						while (--newSize >= 0)
						{
							total++;

							if (newBytes[newSize] != oldBytes[newSize])
							{
								if (isDebugMode())
									System.out.println("\tbytes differ at " + total);
								return true;
							}
						}
					}
				}
			}
			finally
			{
				try
				{
					oldIS.close();
				}
				catch(IOException e)
				{
					//Ignore
				}
				try
				{
					newIS.close();
				}
				catch(IOException e)
				{
					//Ignore
				}
			}

			return false;
		}

		public String getBestMatch (JarFile2 file, JarEntry entry) throws IOException
		{
			// check for same name and same content, return name if found
			if (contains(file, entry))
				return entry.getName();

			// return name of same content file or null
			return hasSameContent(file,entry);
		}
	
		public boolean contains (JarFile2 f, JarEntry e) throws IOException
		{
			final JarEntry thisEntry=(null == e) ? null : getEntryByName(e.getName());
			// Look up name in 'this' Jar2File - if not exist return false
			if (thisEntry == null)
				return false;

			// Check CRC - if no match - return false
			if (thisEntry.getCrc() != e.getCrc())
				return false;

			// Check contents - if no match - return false
			InputStream oldIS = getJarFile().getInputStream(thisEntry);
			InputStream newIS = f.getJarFile().getInputStream(e);
			return (!differs(oldIS, newIS));
		}

		public String hasSameContent (JarFile2 file, JarEntry entry) throws IOException
		{
			final Long crcL=(null == entry) ? Long.valueOf(0L) : Long.valueOf(entry.getCrc());
			final Collection<? extends JarEntry>	ll=_crcToEntryMap.get(crcL);
			// check if this jar contains files with the passed in entry's crc
			if ((ll != null) && (ll.size() > 0))
			{
	            for (final JarEntry thisEntry : ll)
	            {
		            // check for content match
	                InputStream oldIS = getJarFile().getInputStream(thisEntry);
	                InputStream newIS = file.getJarFile().getInputStream(entry);		
			    
	                if (!differs(oldIS, newIS))
	                    return thisEntry.getName();
	            }
		    }
	 
			return null;
		}

		private void index ()
        {
            if (isDebugMode())
                System.out.println("indexing: " + _jar.getName());

            for (final Enumeration<JarEntry> entries=_jar.entries(); (entries != null) && entries.hasMoreElements(); )
            {
            	final JarEntry	entry=entries.nextElement();
            	if (null == entry)
            		continue;

            	final long		crc=entry.getCrc(); 
            	final Long		crcL=Long.valueOf(crc);
            	final String	en=entry.getName();
            	if (isDebugMode())
            		System.out.println("\t" + en + " CRC " + crc);
		 
            	_nameToEntryMap.put(en, entry);
            	_entries.add(entry);

            	// generate the CRC to entries map
            	Collection<JarEntry>	ll=_crcToEntryMap.get(crcL);
                if (ll == null)
                {
                	ll = new LinkedList<JarEntry>();
                	// create the new entry in the map
                	_crcToEntryMap.put(crcL, ll);
                }

                ll.add(entry);
            }
        }
    }

    private static void showHelp ()
    {
        System.out.println("JarDiff: [-nonminimal (for backward compatibility with 1.0.1/1.0] [-creatediff | -applydiff] [-output file] old.jar new.jar");
    }

    // -creatediff -applydiff -debug -output file
    public static void main (String[] args)
    {
    	boolean diff = true, minimal = true;
        String outputFile = "out.jardiff";

        for (int counter = 0; counter < args.length; counter++)
        {
		    // for backward compatibility with 1.0.1/1.0
		    if (args[counter].equals("-nonminimal") || args[counter].equals("-n"))
		    	minimal = false;
		    else if (args[counter].equals("-creatediff") || args[counter].equals("-c"))
                diff = true;
            else if (args[counter].equals("-applydiff") || args[counter].equals("-a"))
                diff = false;
            else if (args[counter].equals("-debug") || args[counter].equals("-d"))
                _debugMode = true;
            else if (args[counter].equals("-output") || args[counter].equals("-o"))
            {
                if (++counter < args.length)
                    outputFile = args[counter];
            }
            else if (args[counter].equals("-applydiff") || args[counter].equals("-a"))
                diff = false;
            else
            {
                if ((counter + 2) != args.length)
                {
                    showHelp();
                    System.exit(0);
                }

                if (diff)
                {
                    try
                    {
                        OutputStream os = new FileOutputStream(outputFile);
                        try
                        {
                        	JarDiff.createPatch(args[counter], args[counter + 1], os, minimal);
                        }
                        finally
                        {
                        	os.close();
                        }
                    }
                    catch (IOException ioe)
                    {
                    	try
                    	{
                    		System.out.println(getResources().getString("jardiff.error.create") + " " + ioe);
                    	}
                    	catch (MissingResourceException mre)
                    	{
                    		// ignored
                    	}
                    }
                }
                else
                {
                    try
                    {
                    	OutputStream os=new FileOutputStream(outputFile);
                    	try
                    	{
                    		new JarDiffPatcher().applyPatch(null, args[counter], args[counter + 1], os);
                    	}
                    	finally
                    	{
                    		os.close();
                    	}
                    }
                    catch (IOException ioe)
                    {
                    	try
                    	{
                    		System.out.println(getResources().getString("jardiff.error.apply") + " " + ioe);
                    	}
                    	catch (MissingResourceException mre)
                    	{
                    		// ignored
                    	}
                    }
                }
                System.exit(0);
            }
        }
        showHelp();
    }
}
