# MdSite

Utility to genereate/update site from a bunch of md files.

## Execution as command

Compiled and packaged exetuable available here: [Javaexperience MdSite repository](http://maven.javaexperience.eu/javaexperience/MdSite/)  
Download the MdSite-\*-jar-with-dependencies.jar file from a version directory.  
Then you can execute it directly:
`java -jar MdSite-*-jar-with-dependencies.jar`

It's advised to create a wrapper script for this command like:
> #!/bin/bash  
> #Location eg.: ~/bin/mdsite (assuming ~/bin is in your $PATH varaible)  
> java -jar /path/to/MdSite-*-jar-with-dependencies.jar "$@"  

If you do this, the command `mdsite` will be available in you shell.

## Seed a new site template

There is a built in subcommand `mdsite seed` that creates the initial files in the
current working directory. The `main.mds` contains the instruction for compilation:
the target extenstion, URL rewrite option, the main wrapper and other features for
the next releases.

The `wrapper.html` is a wrapper for every single page, this file is used as a frame for
every single compiled md document. The `$body_content` will be replaced with the actual
rendered md content.

To compile: `mdsite compile -d path/to/source -t path/to/destination/dir`  
The specified files should be directories, the source directory is where the `main.mds` located.

This basically do an incremental build, when an md file modified in the source, only that file
will be recompiled. If the wrapper changed, all md file will be recompiled.

If you want to force all page to recompile specify the `-f` cli option.

## Working example

### [UARTBus](https://www.uartbus.eu/)

The whole site is compile from this repository: [https://github.com/danko-david/uartbus](https://github.com/danko-david/uartbus)  
The `main.mds` is located in [./doc/main.mds](https://github.com/danko-david/uartbus/blob/master/doc/main.mds)  

Incremental compilation is made by a script in [./scripts/mdsite_compile.sh](https://github.com/danko-david/uartbus/blob/master/scripts/mdsite_compile.sh)

This script is invoked by the Git's post-receive hook in the server.
So every time i push a new commit to the server where this website hosted,
this inremental build script invoked and the site will be updated.

### [DankoDavid.hu](https://www.dankodavid.hu/)

Likely the same as UARTBus, but the repository is not public.

## Compilation from source

You can simply compile the project from source:
> git clone https://github.com/danko-david/mdsite.git  
> cd mdsite  
> mvn package  

After the compilation you can find the direct executable in the `target/` directory


