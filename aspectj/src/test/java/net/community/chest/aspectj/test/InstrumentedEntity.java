/*
 * 
 */
package net.community.chest.aspectj.test;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.UUID;

import javax.persistence.Entity;

import net.community.chest.test.TestBase;

/**
 * <P>Copyright as per GPLv2</P>
 * The default implementation of the {@link Nameable} and {@link Identifiable}
 * interfaces is provided by their respective aspects
 * 
 * @author Lyor G.
 * @since Mar 31, 2011 8:31:05 AM
 */
@Entity
public class InstrumentedEntity extends TestBase implements Nameable, Identifiable {
	public InstrumentedEntity ()
	{
		super();
	}

	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		return "id=" + getId() + ";name=" + getName();
	}

	//////////////////////////////////////////////////////////////////////////

	protected static final void testInstrumentedEntity (
			final BufferedReader in, final PrintStream out, final String ... args)
	{
		final int					numArgs=(args == null) ? 0 : args.length;
		final InstrumentedEntity	entity=new InstrumentedEntity();
		for (int	aIndex=0; ; aIndex++)
		{
			final String	name=
				(aIndex < numArgs) ? args[aIndex] : getval(out, in, "name (or Quit)");
			if ((name == null) || (name.length() <= 0))
				continue;
			if (isQuit(name))
				break;

			entity.setId(UUID.randomUUID().toString());
			entity.setName(name);
			out.println(entity);
		}
	}

	public static final void showEntityContents (final PrintStream out, final Class<?> clazz)
	{
		final Field[]	fields=(clazz == null) ? null : clazz.getDeclaredFields();
		if ((fields != null) && (fields.length > 0))
		{
			out.append(clazz.getName()).println(" fields:");
			for (final Field f : fields)
			{
				out.append('\t');

				final Annotation[]	anns=(f == null) ? null : f.getAnnotations();
				if ((anns != null) && (anns.length > 0))
				{
					for (final Annotation  a : anns)
					{
						out.append(a.toString()).println();
						out.append('\t');
					}
				}

				out.println(f.getName());
			}
		}
	}

	public static final void main (String[] args)
	{
		showEntityContents(System.out, InstrumentedEntity.class);
		testInstrumentedEntity(getStdin(), System.out, args);
	}
}
