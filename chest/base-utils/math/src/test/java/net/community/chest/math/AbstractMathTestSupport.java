/*
 *
 */
package net.community.chest.math;

import java.util.Random;

import net.community.chest.AbstractTestSupport;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 30, 2012 3:32:54 PM
 */
public abstract class AbstractMathTestSupport extends AbstractTestSupport {
    protected final Random    _randomizer;

    protected AbstractMathTestSupport ()
    {
        _randomizer = new Random(System.nanoTime());
    }

}
