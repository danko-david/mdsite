package eu.javaexperience.mdsite.cli;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import eu.javaexperience.cli.CliEntry;
import eu.javaexperience.cli.CliTools;
import eu.javaexperience.io.IOTools;
import eu.javaexperience.io.file.FileTools;
import eu.javaexperience.log.JavaExperienceLoggingFacility;
import eu.javaexperience.log.LogLevel;
import eu.javaexperience.log.Loggable;
import eu.javaexperience.log.Logger;
import eu.javaexperience.log.LoggingTools;
import eu.javaexperience.mdsite.MdRenderContext;
import eu.javaexperience.text.Format;
import eu.javaexperience.text.StringTools;

import static eu.javaexperience.mdsite.cli.MdSiteCliCommons.*;

public class MdsiteCompileCli
{
	protected static final Logger LOG = JavaExperienceLoggingFacility.getLogger(new Loggable("MdsiteCompileCli"));
	
	protected static final CliEntry[] COMPILE_CLI_ARGS = new CliEntry[]
	{
		CLI_DIR,
		CLI_TARGET,
		CLI_CONFIG,
		CLI_FORCE
	};
	
	/**
	 * default: incremental build
	 * */
	public static void main(String... args) throws Throwable
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
		
		
		String from = CLI_DIR.tryParse(opts);
		if(null == from)
		{
			System.err.println("Source directory (-d) not specified!");
			fail = true;
		}
		
		String to = CLI_TARGET.tryParse(opts);
		
		if(null == to)
		{
			System.err.println("Target directory (-t) not specified!");
			fail = true;
		}
		
		if(fail)
		{
			CliTools.printHelpAndExit("MdSite.compile", 2, COMPILE_CLI_ARGS);
		}
		
		String cfg = CLI_CONFIG.tryParseOrDefault(opts, "main.mds");
		
		File cfgFile = new File(from+"/"+cfg);
		if(!cfgFile.exists())
		{
			System.err.println("main.mds file doesn't exist at: "+cfgFile);
			System.exit(1);
		}
		
		Properties prop = MdRenderContext.loadProperties(cfgFile.toString());
		
		MdRenderContext ctx = MdRenderContext.createContext(from+"/", cfg, prop);
		
		String extension = ctx.getTargetFileExtension();
		
		boolean forceCompile = CLI_FORCE.hasOption(opts);
		
		FileTools.translateFiles(true, ctx.getSourceDir(), new File(to), (f, t)->
		{
			if(f.isFile() && f.toString().endsWith(".md"))
			{
				try
				{
					LoggingTools.tryLogFormat(LOG, LogLevel.DEBUG, "Visiting source file: `%s`", f);
					String dst = StringTools.getSubstringBeforeLastString(t.toString(), ".md")+(null == extension?"":"."+extension);
					t = new File(dst);
					long srcMod = Math.max(ctx.lastModify, f.lastModified());
					
					if(LOG.mayLog(LogLevel.DEBUG))
					{
						LoggingTools.tryLogFormat
						(
							LOG,
							LogLevel.DEBUG,
							"Source last modified: `%s`, Destination file `%s` last modified: `%s`, force recompile: %s",
							Format.SQL_TIMESTAMP.format(new Date(srcMod)),
							dst,
							0 == t.lastModified()?"0":Format.SQL_TIMESTAMP.format(new Date(t.lastModified())),
							forceCompile
						);
					}
					
					if(!t.exists() || srcMod > t.lastModified() || forceCompile)
					{
						IOTools.putFileContent(dst, ctx.renderContent(IOTools.getFileContents(f)).getBytes());
						t.setLastModified(srcMod);
					}
				}
				catch(Exception e)
				{
					System.err.println("Exception while processing file: "+f);
					e.printStackTrace();
				}
			}
		});
	}
}
