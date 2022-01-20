package eu.javaexperience.mdsite.cli;

import static eu.javaexperience.mdsite.cli.MdSiteCliCommons.CLI_CONFIG;
import static eu.javaexperience.mdsite.cli.MdSiteCliCommons.CLI_DIR;
import static eu.javaexperience.mdsite.cli.MdSiteCliCommons.CLI_FORCE;
import static eu.javaexperience.mdsite.cli.MdSiteCliCommons.CLI_TARGET;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import eu.javaexperience.cli.CliEntry;
import eu.javaexperience.cli.CliTools;
import eu.javaexperience.exceptions.CheckedIllegalArgumentException;
import eu.javaexperience.io.IOTools;
import eu.javaexperience.io.file.FileTools;
import eu.javaexperience.log.JavaExperienceLoggingFacility;
import eu.javaexperience.log.LogLevel;
import eu.javaexperience.log.Loggable;
import eu.javaexperience.log.Logger;
import eu.javaexperience.log.LoggingTools;
import eu.javaexperience.mdsite.MdRenderContext;
import eu.javaexperience.text.Format;
import eu.javaexperience.text.StringFunctions;
import eu.javaexperience.text.StringTools;

public class MdsiteCgiCli
{
	protected static final Logger LOG = JavaExperienceLoggingFacility.getLogger(new Loggable("MdsiteCgiCli"));
	
	public static final CliEntry<String> CLI_CGI_HEADER = CliEntry.createFirstArgParserEntry
	(
		StringFunctions.PASS_TROUGHT,
		"Print headers before content eg.: (-h \"Content-type: text/html;charset=utf-8\\nGenerated-with: MdSite\")",
		"h", "-cgi-headers"
	);
	
	public static final CliEntry<String> CLI_FILE = CliEntry.createFirstArgParserEntry
	(
		StringFunctions.PASS_TROUGHT,
		"The file to compile. This is the last CLI switch when using as CGI."
		+ "Specify shebang like this: `#!/usr/bin/env -S MDSITE_REWRITE_HREF=false mdsite cgi -h \"Content-type: text/html;charset=utf-8\\n\" -f`",
		"f", "-file"
	);
	
	protected static final CliEntry[] COMPILE_CLI_ARGS = new CliEntry[]
	{
		CLI_CGI_HEADER,
		CLI_CONFIG,
		CLI_FILE
	};
	
	protected static File findConfig(String anchor, String cfg) throws IOException
	{
		File f = new File(anchor).getCanonicalFile();
		if(null == f)
		{
			return null;
		}
		
		f = FileTools.getDirectory(f);
		File c = null;
		
		do
		{
			if((c = new File(f+"/"+cfg)).exists())
			{
				return c;
			}
		}
		while(null != (f = f.getParentFile()));
		
		return null;
	}
	
	public static void main(String[] args) throws Throwable
	{
		//-d directory where main.mds located
		//-t target_directory
		Map<String, List<String>> opts = CliTools.storeCliOptions(true, args);

		boolean fail = false;
		
		{
			String sw = CliTools.getFirstUnknownParam(opts, COMPILE_CLI_ARGS);
			if(null != sw)
			{
				System.err.println("Unknown Cli option: "+sw);
				fail = true;
			}
		}
		
		String file = CLI_FILE.tryParse(opts);
		if(null == file)
		{
			System.err.println("No source file specified!");
			fail = true;
		}
		
		String header = CLI_CGI_HEADER.tryParseOrDefault(opts, null);
		
		if(fail)
		{
			CliTools.printHelpAndExit("MdSite.cgi", 2, COMPILE_CLI_ARGS);
		}
		
		//TODO look for this file pwards
		String cfg = CLI_CONFIG.tryParseOrDefault(opts, "main.mds");
		
		File cfgFile = findConfig(file, cfg);
		if(null == cfgFile || !cfgFile.exists())
		{
			System.err.println("main.mds file doesn't exist at: "+cfgFile);
			System.exit(1);
		}
		
		if(null != header)
		{
			System.out.println(header);
		}
		
		Properties prop = MdRenderContext.loadProperties(cfgFile.toString());
		
		MdRenderContext ctx = MdRenderContext.createContext(cfgFile.getParent()+"/", cfg, prop);
		
		System.out.println(ctx.renderContent(IOTools.getFileContents(file)));
	}
}
