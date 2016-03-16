/**
 * <P>Copyright GPLv2</P>
 *
 * <P>This class cannot be imported outside the default package</P>
 * @author Lyor G.
 * @since Jun 15, 2009 12:56:28 PM
 */
public final class NonImportableExample {
    private NonImportableExample ()
    {
        // do nothing
    }

    public static final void importMe ()
    {
        System.out.println("I was imported");
    }
}
