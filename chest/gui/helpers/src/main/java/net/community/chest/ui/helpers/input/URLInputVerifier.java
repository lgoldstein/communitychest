/*
 * 
 */
package net.community.chest.ui.helpers.input;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 31, 2009 1:44:03 PM
 */
public class URLInputVerifier extends TextInputVerifier {
	public URLInputVerifier ()
	{
		super();
	}
	/*
	 * @see net.community.chest.ui.helpers.input.TextInputVerifier#verifyText(java.lang.String)
	 */
	@Override
	public boolean verifyText (String text)
	{
		if (!super.verifyText(text))
			return false;

		try
		{
			final URI	uri=new URI(text);
			if (null == uri.toString())
				return false;
			return true;
		}
		catch(URISyntaxException e)
		{
			return false;
		}
	}

	public static final URLInputVerifier	URL=new URLInputVerifier();
}
