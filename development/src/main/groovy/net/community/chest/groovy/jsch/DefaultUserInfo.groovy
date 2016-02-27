/*
 * 
 */
package net.community.chest.groovy.jsch

import com.jcraft.jsch.UserInfo;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Dec 23, 2010 12:19:00 PM
 */
class DefaultUserInfo implements UserInfo {
	public DefaultUserInfo () {
		super()
	}

	public String	passphrase
	/*
	 * @see com.jcraft.jsch.UserInfo#getPassphrase()
	 */
	@Override
	public String getPassphrase ()	{
		return passphrase
	}

	public String	password
	/*
	 * @see com.jcraft.jsch.UserInfo#getPassword()
	 */
	@Override
	public String getPassword () {
		return password
	}

	public DefaultUserInfo (String passphrase, String password) {
		this.passphrase = passphrase
		this.password = password
	}

	public DefaultUserInfo (String password) {
		this.password = password
	}
	/*
	 * @see com.jcraft.jsch.UserInfo#promptPassword(java.lang.String)
	 */
	@Override
	public boolean promptPassword (String message) {
		return true
	}
	/*
	 * @see com.jcraft.jsch.UserInfo#promptPassphrase(java.lang.String)
	 */
	@Override
	public boolean promptPassphrase (String message) {
		return true
	}
	/*
	 * @see com.jcraft.jsch.UserInfo#promptYesNo(java.lang.String)
	 */
	@Override
	public boolean promptYesNo (String message)	{
		return true
	}
	/*
	 * @see com.jcraft.jsch.UserInfo#showMessage(java.lang.String)
	 */
	@Override
	public void showMessage (String message) {
		// ignored
	}
}
