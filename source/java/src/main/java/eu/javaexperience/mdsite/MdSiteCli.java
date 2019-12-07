package eu.javaexperience.mdsite;

import eu.javaexperience.generic.annotations.Ignore;
import eu.javaexperience.mdsite.cli.MdsiteCompileCli;
import eu.javaexperience.rpc.cli.RpcCliTools;

public class MdSiteCli
{
	@Ignore
	public static void main(String[] args)
	{
		RpcCliTools.tryExecuteCommandCollectorClassOrExit(new MdSiteCli(), 1, args);
	}
	
	//reindex: refresh site pages dependency
	
	
	public static void compile(String... args) throws Throwable
	{
		MdsiteCompileCli.main(args);
	}
}
