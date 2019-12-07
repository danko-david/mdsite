package eu.javaexperience.mdsite.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;
import org.commonmark.parser.delimiter.DelimiterProcessor;
import org.commonmark.parser.delimiter.DelimiterRun;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlNodeRendererFactory;
import org.commonmark.renderer.html.HtmlRenderer;

import eu.javaexperience.collection.set.NullSet;
import eu.javaexperience.io.IOTools;


public class InstructionExtension implements Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension
{
	public static void main(String[] args) throws Exception
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
		
		Node document = parser.parse(IOTools.getFileContents("/home/szupervigyor/projektek/electronics/uartbus/README.md"));
		System.out.println(renderer.render(document));  // "<p>This is <em>Sparta</em></p>\n"
	}


	@Override
	public void extend(Parser.Builder parserBuilder)
	{
		parserBuilder.customDelimiterProcessor(new DelimiterProcessor()
		{
			@Override
			public char getOpeningCharacter()
			{
				return '{';
			}
	
			@Override
			public char getClosingCharacter()
			{
				return '}';
			}

			@Override
			public int getMinLength()
			{
				return 2;
			}

			@Override
			public int getDelimiterUse(DelimiterRun opener, DelimiterRun closer)
			{
				return 0;
			}

			@Override
			public void process(Text opener, Text closer, int delimiterUse)
			{
				
			}
		});
	}

	@Override
	public void extend(HtmlRenderer.Builder rendererBuilder)
	{
		rendererBuilder.nodeRendererFactory(new HtmlNodeRendererFactory()
		{
			@Override
			public NodeRenderer create(HtmlNodeRendererContext context)
			{
				return new NodeRenderer()
				{
					@Override
					public void render(Node node)
					{
						//TODO 
					}
					
					@Override
					public Set<Class<? extends Node>> getNodeTypes()
					{
						return NullSet.instance;
					}
				};
			}
		});
	}
}
