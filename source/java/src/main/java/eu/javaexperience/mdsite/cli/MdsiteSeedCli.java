package eu.javaexperience.mdsite.cli;

import java.io.File;

import eu.javaexperience.io.IOTools;

public class MdsiteSeedCli
{
	public static void main(String[] args) throws Throwable
	{
		File main = new File("./main.mds");
		if(main.exists())
		{
			System.err.println("File main.mds already exists in this directory. Skip seeding.");
			System.exit(1);
		}
		
		IOTools.writeStringToFile
		(
			"md_root_dir=.\nwrapper=wrapper.html?body_content\ntarget_file_extension=md\rewrite_internal_link_href=true",
			"./main.mds"
		);

		IOTools.writeStringToFile
		(
			"<!DOCTYPE html>\n" + 
			"<html lang=\"en-US\">\n" + 
			"	<head>\n" + 
			"		<meta charset=\"UTF-8\"/>\n" + 
			"		<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"/>\n" + 
			"	</head>\n" + 
			"	<body>\n" + 
			"		<div class=\"markdown-body\">\n" + 
			"			$body_content\n" + 
			"		</div>\n" + 
			"	</body>\n" + 
			"</html>",
			"./wrapper.html"
		);
		
		IOTools.writeStringToFile
		(
			"# Index page",
			"./index.md"
		);
	}
}
