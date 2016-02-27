/*
 * 
 */
package net.community.chest.aspectj.test;

import javax.persistence.Id;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Mar 31, 2011 8:29:18 AM
 */
public interface Identifiable {
	public void setId (String id);
	public String getId ();

	static aspect Impl {
		@Id
		private String Identifiable._id;
		public void Identifiable.setId (String id)
		{
			this._id = id;
		}

		public String Identifiable.getId ()
		{
			return this._id;
		}
	}
}
