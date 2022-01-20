package eu.javaexperience.mdsite;

import eu.javaexperience.generic.annotations.Ignore;
import eu.javaexperience.log.JavaExperienceLoggingFacility;
import eu.javaexperience.mdsite.cli.MdsiteCgiCli;
import eu.javaexperience.mdsite.cli.MdsiteCompileCli;
import eu.javaexperience.mdsite.cli.MdsiteSeedCli;
import eu.javaexperience.rpc.cli.RpcCliTools;

public class MdSiteCli
{
	@Ignore
	public static void main(String[] args)
	{
		JavaExperienceLoggingFacility.addStdErr();
		RpcCliTools.tryExecuteCommandCollectorClassOrExit(new MdSiteCli(), 1, args);
	}
	
	public static void compile(String... args) throws Throwable
	{
		MdsiteCompileCli.main(args);
	}
	
	/**
	 * Creates the necessary files for a compilation into the current directory.
	 * */
	public static void seed(String... args) throws Throwable
	{
		MdsiteSeedCli.main(args);
	}

	/**
	 * Generates single page as past of a CGI infrastructure
	 * */
	public static void cgi(String... args) throws Throwable
	{
		MdsiteCgiCli.main(args);
	}
}
