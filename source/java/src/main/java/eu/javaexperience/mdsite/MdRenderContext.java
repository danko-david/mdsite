package eu.javaexperience.mdsite;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import eu.javaexperience.collection.map.MapTools;
import eu.javaexperience.collection.map.SmallMap;
import eu.javaexperience.interfaces.simple.getBy.GetBy1;
import eu.javaexperience.io.IOTools;
import eu.javaexperience.log.JavaExperienceLoggingFacility;
import eu.javaexperience.log.LogLevel;
import eu.javaexperience.log.Loggable;
import eu.javaexperience.log.Logger;
import eu.javaexperience.log.LoggingTools;
import eu.javaexperience.reflect.CastTo;
import eu.javaexperience.text.Format;
import eu.javaexperience.text.StringTools;

public class MdRenderContext implements Cloneable
{
	protected static final Logger LOG = JavaExperienceLoggingFacility.getLogger(new Loggable("MdRenderContext"));
	
	public String rootDir;
	public Properties props;
	public GetBy1<String, String> render; 
	public List<GetBy1<String, String>> processors = new ArrayList<>();
	public long lastModify = 0;
	public String cfgFile;
	
	//copy constructor
	public MdRenderContext(MdRenderContext ctx)
	{
		rootDir = ctx.rootDir;
		props = new Properties(ctx.props);
		render = ctx.render;
		processors.addAll(ctx.processors);
		lastModify = ctx.lastModify;
		cfgFile = ctx.cfgFile;
	}
	
	public void init() throws FileNotFoundException, IOException
	{
		String wrapperFile = StringTools.toStringOrNull(props.get("wrapper"));
		
		if(null != wrapperFile)
		{
			String file = getSourceDir()+"/"+StringTools.getSubstringBeforeFirstString(wrapperFile, "?", wrapperFile);
			String _replace = StringTools.getSubstringAfterFirstString(wrapperFile, "?", null);
			
			if(null == _replace)
			{
				throw new RuntimeException("No replacement variable specified at the wrapper in the `"+cfgFile+"`. Use this form: wrapper=index.html?body_content");
			}
			
			String replace = "$"+_replace;
			
			File wf = new File(file);
			if(!wf.exists())
			{
				LoggingTools.tryLogFormat
				(
					LOG,
					LogLevel.WARNING,
					"Specified wrapper file `%s` doesn't exists",
					file
				);
			}
			else
			{
				lastModify = wf.lastModified();
				LoggingTools.tryLogFormat
				(
					LOG,
					LogLevel.DEBUG,
					"Wrapper file last modification date: %s", Format.SQL_TIMESTAMP.format(new Date(lastModify))
				);
				
				String wrapper = IOTools.getFileContents(file);
				processors.add(src->StringTools.replaceAllStrings(wrapper, replace, src));
			}
		}
		else
		{
			LoggingTools.tryLogFormat
			(
				LOG,
				LogLevel.INFO,
				"No wrapper file specified",
				wrapperFile
			);
		}
	}
	
	public MdRenderContext(){}

	public MdRenderContext clone()
	{
		return new MdRenderContext(this);
	}
	
	public static GetBy1<String, String> createDefaultRenderer()
	{
		List<Extension> extensions = new ArrayList<>();
		extensions.add(TablesExtension.create());
		extensions.add(StrikethroughExtension.create());
		
		Parser parser = Parser.builder()
			.extensions(extensions)
			.build();
		
		HtmlRenderer renderer = HtmlRenderer.builder()
			.extensions(extensions)
			.build();
		
		return a->renderer.render(parser.parse(a));
	}

	public String renderContent(String content)
	{
		String ret = render.getBy(content);
		for(GetBy1<String, String> p:processors)
		{
			ret = p.getBy(ret);
		}
		return ret;
	}

	public File getSourceDir() throws IOException
	{
		String add = StringTools.toStringOrNull(props.get("md_root_dir"));
		if(null == add)
		{
			return new File(rootDir).getCanonicalFile();
		}
		
		return new File(rootDir+add).getCanonicalFile();
	}
	
	private static final ThreadLocal<MdRenderContext> THREAD_CONTEXT = new ThreadLocal<>();
	
	public static MdRenderContext getCurrentRenderContext()
	{
		return THREAD_CONTEXT.get();
	}
	
	public static MdRenderContext ensureGetCurrentRpcSession()
	{
		MdRenderContext ret = getCurrentRenderContext();
		if(null == ret)
		{
			throw new RuntimeException("No MdRenderContext associated with the current processor thread.");
		}
		
		return ret;
	}
	
	public static void setCurrentRenderContext(MdRenderContext ctx)
	{
		THREAD_CONTEXT.set(ctx);
	}

	public String getTargetFileExtension()
	{
		String extension = props.getProperty("target_file_extension");
		if(null == extension)
		{
			extension = "md";
		}
		else if(extension.length() == 0)
		{
			extension = "";
		}
		
		return extension;
	}
	
	public static MdRenderContext createContext(String root, String cfgFile, Properties prop) throws IOException
	{
		MdRenderContext ctx = new MdRenderContext();
		ctx.render = ctx.createRenderer();
		ctx.cfgFile = cfgFile;
		ctx.props = prop;
		ctx.rootDir = root;
		
		ctx.init();
		
		return ctx;
	}
	
	public boolean isRewriteInternalLinkHrefs()
	{
		return Boolean.TRUE == CastTo.Boolean.cast(props.get("rewrite_internal_link_href"));
	}
	
	protected static final Pattern FIND_HREFS = Pattern.compile("href\\s*=\\s*\\\"(?<url>[^\"]+)\\\"");
	protected static final Pattern FIND_EXTERNSION = Pattern.compile("(?<file>.*)(?<ext>\\.md)(?<post>($|#|\\?).*)", Pattern.CASE_INSENSITIVE);
	
	public static boolean isFullUrl(String str)
	{
		try
		{
			new URL(str);
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}
	
	public GetBy1<String, String> createRenderer()
	{
		GetBy1<String, String> def = MdRenderContext.createDefaultRenderer();
		return s->
		{
			s = def.getBy(s);
			if(isRewriteInternalLinkHrefs())
			{
				String te = getTargetFileExtension();
				if(!"md".equals(te))
				{
					Map<String, String> ft = new SmallMap<>();
					
					Matcher m = FIND_HREFS.matcher(s);
					while(m.find())
					{
						String url = m.group("url");
						if(!isFullUrl(url))
						{
							if(LOG.mayLog(LogLevel.DEBUG))
							{
								LoggingTools.tryLogFormat(LOG, LogLevel.DEBUG, "Link found: `%s`", url);
							}
							Matcher em = FIND_EXTERNSION.matcher(url);
							if(em.find())
							{
								ft.put(em.group(0), em.group("file")+"."+te+em.group("post"));
							}
						}
					}
					
					if(LOG.mayLog(LogLevel.DEBUG))
					{
						LoggingTools.tryLogFormat(LOG, LogLevel.DEBUG, "Rewriting urls: %s", MapTools.toStringMultiline(ft));
					}
					s = StringTools.multiReplaceAllString(s, ft);
				}
			}
			return s;
		};
	}

	public static Properties loadProperties(String propFile) throws IOException
	{
		try
		(
			FileInputStream fis = new FileInputStream(propFile);
			InputStreamReader reader = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(reader);
		)
		{
			Properties prop = new Properties();
			prop.load(reader);
			return prop;
		}
	}
}
