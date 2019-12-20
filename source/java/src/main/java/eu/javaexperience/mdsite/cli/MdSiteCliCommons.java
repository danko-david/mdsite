package eu.javaexperience.mdsite.cli;

import eu.javaexperience.cli.CliEntry;
import eu.javaexperience.text.StringFunctions;

public class MdSiteCliCommons
{
	private MdSiteCliCommons() {}
	
	public static final CliEntry<String> CLI_DIR = CliEntry.createFirstArgParserEntry
	(
		StringFunctions.PASS_TROUGHT,
		"Source directory (main.mds location)",
		"d", "-source-directory"
	);
	
	public static final CliEntry<String> CLI_TARGET = CliEntry.createFirstArgParserEntry
	(
		StringFunctions.PASS_TROUGHT,
		"Destination directory",
		"t", "-target-directory"
	);
	
	public static final CliEntry<String> CLI_CONFIG = CliEntry.createFirstArgParserEntry
	(
		StringFunctions.PASS_TROUGHT,
		"The config file in the sourcer directory (default: main.mds)",
		"c", "-config-file"
	);
	
	public static final CliEntry<Boolean> CLI_FORCE = CliEntry.createFirstArgParserEntry
	(
		e->true,
		"Force to (re)compile markdowns",
		"f", "-force"
	);
}
