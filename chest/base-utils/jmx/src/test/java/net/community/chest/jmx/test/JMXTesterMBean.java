package net.community.chest.jmx.test;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 14, 2007 10:53:37 AM
 */
public interface JMXTesterMBean extends BaseMBeanInterface {
	/**
	 * @return current time
	 */
	long getCurrentTime ();
	/**
	 * Checks the "position" of the current time when compared with the
	 * supplied time-stamp
	 * @param tStamp time stamp value to check for
	 * @return <P>The difference between the time stamp and the current
	 * time:</P></BR>
	 * <UL>
	 * 		<LI>0 - equal</LI>
	 * 		<LI>&gt;0 if time stamp is greater than current time</LI>
	 * 		<LI>&lt;0 if time stamp is less than current time</LI>
	 * </UL>
	 */
	long compareCurrentTime (long tStamp);
	/**
	 * Converts argument to lowercase
	 * @param v original value
	 * @return input value as lower-case
	 */
	String toLowerCase (String v);
}
