package eu.javaexperience.mdsite.cli;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import eu.javaexperience.cli.CliEntry;
import eu.javaexperience.cli.CliTools;
import eu.javaexperience.io.IOTools;
import eu.javaexperience.io.file.FileTools;
import eu.javaexperience.mdsite.MdRenderContext;
import eu.javaexperience.text.StringTools;

import static eu.javaexperience.mdsite.cli.MdSiteCliCommons.*;

public class MdsiteCompileCli
{
	protected static final CliEntry[] COMPILE_CLI_ARGS = new CliEntry[]
	{
		CLI_DIR,
		CLI_TARGET,
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
		
		
		FileTools.translateFiles(true, ctx.getSourceDir(), new File(to), (f, t)->
		{
			if(f.isFile() && f.toString().endsWith(".md"))
			{
				try
				{
					String dst = StringTools.getSubstringBeforeLastString(t.toString(), ".md")+(null == extension?"":"."+extension);
					long srcMod = Math.max(ctx.lastModify, f.lastModified());
					if(!t.exists() || srcMod > t.lastModified())
					{
						IOTools.putFileContent(dst, ctx.renderContent(IOTools.getFileContents(f)).getBytes());
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
