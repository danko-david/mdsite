package eu.javaexperience.mdsite;

import eu.javaexperience.generic.annotations.Ignore;
import eu.javaexperience.log.JavaExperienceLoggingFacility;
import eu.javaexperience.mdsite.cli.MdsiteCompileCli;
import eu.javaexperience.mdsite.cli.MdsiteSeedCli;
import eu.javaexperience.rpc.cli.RpcCliTools;

public class MdSiteCli
{
	@Ignore
	public static void main(String[] args)
	{
		JavaExperienceLoggingFacility.addStdOut();
		RpcCliTools.tryExecuteCommandCollectorClassOrExit(new MdSiteCli(), 1, args);
	}
	
	//reindex: refresh site pages dependency
	
	
	public static void compile(String... args) throws Throwable
	{
		MdsiteCompileCli.main(args);
	}
	
	/**
	 * Creates the necessary files to a compilation to the current directory.
	 * */
	public static void seed(String... args) throws Throwable
	{
		MdsiteSeedCli.main(args);
	}
}
