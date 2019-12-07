package eu.javaexperience.mdsite;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import eu.javaexperience.interfaces.simple.getBy.GetBy1;
import eu.javaexperience.io.IOTools;
import eu.javaexperience.text.StringTools;

public class MdRenderContext implements Cloneable
{
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
		//TODO implement, create, add extra processor for {} and {{}}
		
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

	public File getSourceDir()
	{
		String add = StringTools.toStringOrNull(props.get("md_root_dir"));
		if(null == add)
		{
			return new File(rootDir);
		}
		
		return new File(rootDir+add);
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
		ctx.render = MdRenderContext.createDefaultRenderer();
		ctx.cfgFile = cfgFile;
		ctx.props = prop;
		ctx.rootDir = root;
		
		String wrapperFile = StringTools.toStringOrNull(prop.get("wrapper"));
		if(null != wrapperFile)
		{
			String file = StringTools.getSubstringBeforeFirstString(wrapperFile, "?", null);
			String _replace = StringTools.getSubstringAfterFirstString(wrapperFile, "?", null);
			
			if(null == _replace)
			{
				throw new RuntimeException("No replacement variable specified at the wrapper in the `"+ctx.cfgFile+"`. Use this form: wrapper=index.html?body_content");
			}
			
			String replace = "$"+_replace;
			
			ctx.lastModify = Math.max(ctx.lastModify, new File(file).lastModified());
			
			String wrapper = IOTools.getFileContents(ctx.getSourceDir()+"/"+file);
			ctx.processors.add(src->StringTools.replaceAllStrings(wrapper, replace, src));
		}
		
		return ctx;
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
