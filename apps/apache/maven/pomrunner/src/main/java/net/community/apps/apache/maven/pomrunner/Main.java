package net.community.apps.apache.maven.pomrunner;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.SwingUtilities;

import net.community.apps.common.BaseMain;
import net.community.chest.CoVariantReturn;
import net.community.chest.lang.SysPropsEnum;
import net.community.chest.resources.SystemPropertiesResolver;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 8, 2007 10:32:47 AM
 */
public final class Main extends BaseMain {
	private Main (final String ... args)
	{
		super(args);
	}

	public static final char	CMD_ARGS_SEP=' ', TARGETS_SEP=' ';
	private static final MainFrame processMainArgs (MainFrame frame, String ...args)
	{
		final int					numArgs=(args == null) ? 0 : args.length;
		final Map<String,String>	procArgs=new TreeMap<String,String>();
		for (int	aIndex=0; aIndex < numArgs; aIndex++)
		{
			final String	opt=args[aIndex];
			if ("--pom".equalsIgnoreCase(opt))
			{
				aIndex = addExtraArgument(opt, procArgs, aIndex, args);

				final String	argVal=procArgs.get(opt),
								filePath=SystemPropertiesResolver.SYSTEM.format(argVal);
				frame.loadFile(new File(filePath), opt, null);
			}
			else if ("--mvnhome".equalsIgnoreCase(opt))
			{
				aIndex = addExtraArgument(opt, procArgs, aIndex, args);

				final String	argVal=procArgs.get(opt),
								filePath=SystemPropertiesResolver.SYSTEM.format(argVal);
				frame.setMavenHome(filePath);
			}
			else if ("--mvncmd".equalsIgnoreCase(opt))
			{
				aIndex = addExtraArgument(opt, procArgs, aIndex, args);
				frame.setMavenCommand(procArgs.get(opt));
			}
			else if ("--cwd".equalsIgnoreCase(opt))
			{
				aIndex = addExtraArgument(opt, procArgs, aIndex, args);
				frame.setWorkingDirectory(procArgs.get(opt));
			}
			else if ("--targets".equalsIgnoreCase(opt))
			{
				aIndex = collectExtraArguments(opt, procArgs, aIndex, '-', TARGETS_SEP, args);
				frame.setTargets(procArgs.get(opt));
			}
			else if ("--mvnargs".equalsIgnoreCase(opt))
			{
				aIndex = collectExtraArguments(opt, procArgs, aIndex, '-', CMD_ARGS_SEP, args);
				frame.setExtraArguments(procArgs.get(opt));
			}
			else
				throw new IllegalArgumentException("Unknown option: " + opt);
		}

		// if no POM file specified by the user then try to load the CWD one (if any)
		final String	curdir=SysPropsEnum.USERDIR.getPropertyValue();
		if ((curdir != null) && (curdir.length() > 0))
		{
			final String	filePath=frame.getCurrentFilePath();
			if ((filePath == null) || (filePath.length() <= 0))
				frame.loadFile(new File(curdir), SysPropsEnum.USERDIR.getPropertyName(), null);

			final String	workDir=frame.getWorkingDirectory();
			if ((workDir == null) || (workDir.length() <= 0))
				frame.setWorkingDirectory(curdir);
		}

		return frame;
	}
	/*
	 * @see net.community.apps.common.BaseMain#createMainFrameInstance()
	 */
	@Override
	@CoVariantReturn
	protected MainFrame createMainFrameInstance () throws Exception
	{
		return processMainArgs(new MainFrame(), getMainArguments());
	}

	//////////////////////////////////////////////////////////////////////////

	public static void main (final String[] args)
	{
		SwingUtilities.invokeLater(new Main(args));
	}
}
