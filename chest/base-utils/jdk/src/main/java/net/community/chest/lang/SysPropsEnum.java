/*
 *
 */
package net.community.chest.lang;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * <P>Encapsulates the default provided {@link System#getProperties()} values as an {@link Enum}</P>
 * @author Lyor G.
 * @since Oct 28, 2010 8:02:33 AM
 */
public enum SysPropsEnum {
    /**
     * Java Runtime Environment version
     */
    JAVAVERSION("java.version", "Java Runtime Environment version"),
    /**
     * Java Runtime Environment vendor
     */
    JAVAVENDOR("java.vendor", "Java Runtime Environment vendor"),
    /**
     * Java vendor URL
     */
    JAVAVENDORURL("java.vendor.url", "Java vendor URL"),
    /**
     * Java installation directory
     */
    JAVAHOME("java.home", "Java installation directory"),
    /**
     * Java Virtual Machine specification version
     */
    JVMSPECVERSION("java.vm.specification.version", "Java Virtual Machine specification version"),
    /**
     * Java Virtual Machine specification vendor
     */
    JVMSPECVENDOR("java.vm.specification.vendor", "Java Virtual Machine specification vendor"),
    /**
     * Java Virtual Machine specification name
     */
    JVMSPECNAME("java.vm.specification.name", "Java Virtual Machine specification name"),
    /**
     * Java Virtual Machine implementation version
     */
    JVMVERSION("java.vm.version", "Java Virtual Machine implementation version"),
    /**
     * Java Virtual Machine implementation vendor
     */
    JVMVENDOR("java.vm.vendor", "Java Virtual Machine implementation vendor"),
    /**
     * Java Virtual Machine implementation name
     */
    JVMNAME("java.vm.name", "Java Virtual Machine implementation name"),
    /**
     * Java Runtime Environment specification version
     */
    JAVASPECVERSION("java.specification.version ", "Java Runtime Environment specification version"),
    /**
     * Java Runtime Environment specification vendor
     */
    JAVASPECVENDOR("java.specification.vendor", "Java Runtime Environment specification vendor"),
    /**
     * Java Runtime Environment specification name
     */
    JAVASPECNAME("java.specification.name", "Java Runtime Environment specification name"),
    /**
     * Java class format version number
     */
    JAVACLASSVERSION("java.class.version", "Java class format version number"),
    /**
     * Java class path
     */
    JAVACLASSPATH("java.class.path", "Java class path"),
    /**
     * List of paths to search when loading libraries
     */
    JAVALIBPATH("java.library.path", "List of paths to search when loading libraries"),
    /**
     * Default temp file path
     */
    JAVAIOTMPDIR("java.io.tmpdir", "Default temp file path"),
    /**
     * Name of JIT compiler to use
     */
    JAVACOMPILER("java.compiler", "Name of JIT compiler to use"),
    /**
     * Path of extension directory or directories
     */
    JAVAEXTDIRS("java.ext.dirs", "Path of extension directory or directories"),
    /**
     * Operating system name
     */
    OSNAME("os.name", "Operating system name"),
    /**
     * Operating system architecture
     */
    OSARCH("os.arch", "Operating system architecture"),
    /**
     * Operating system version
     */
    OSVERSION("os.version", "Operating system version"),
    /**
     * File separator (&quot;/&quot; on UNIX)
     */
    FILESEP("file.separator", "File separator (\"/\" on UNIX)"),
    /**
     * Path separator (&quot;:&quot; on UNIX)
     */
    PATHSEP("path.separator", "Path separator (\":\" on UNIX)"),
    /**
     * Line separator (&quot;\n&quot; on UNIX)
     */
    LINESEP("line.separator", "Line separator (\"\n\" on UNIX)"),
    /**
     * User's account name
     */
    USERNAME("user.name", "User's account name"),
    /**
     * User's home directory
     */
    USERHOME("user.home", "User's home directory"),
    /**
     * User's current working directory
     */
    USERDIR("user.dir", "User's current working directory");

    private final String    _propName;
    public final String getPropertyName ()
    {
        return _propName;
    }

    public final String getPropertyValue ()
    {
        return System.getProperty(getPropertyName());
    }

    public final String getPropertyValue (String defValue)
    {
        return System.getProperty(getPropertyName(), defValue);
    }

    private final String    _description;
    public final String getDescription ()
    {
        return _description;
    }

    SysPropsEnum (String propName, String description)
    {
        _propName = propName;
        _description = description;
    }

    public static final List<SysPropsEnum>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final SysPropsEnum fromName (String s)
    {
        return EnumUtil.fromName(VALUES, s, false);
    }

    public static final SysPropsEnum fromProperty (String p)
    {
        if ((p == null) || (p.length() <= 0))
            return null;

        for (final SysPropsEnum v : VALUES)
        {
            final String    n=(v == null) ? null : v.getPropertyName();
            if (p.equalsIgnoreCase(n))
                return v;
        }

        return null;
    }

    public static final Map<SysPropsEnum,String> toSysPropsMap ()
    {
        final Map<SysPropsEnum,String>    retMap=new EnumMap<SysPropsEnum,String>(SysPropsEnum.class);
        for (final SysPropsEnum key : VALUES)
        {
            final String    value=(key == null) ? null : key.getPropertyValue();
            if ((value == null) || (value.length() <= 0))
                continue;

            final String    prev=retMap.put(key, value);
            if (prev != null)    // should not happen
                continue;
        }

        return retMap;
    }
}
