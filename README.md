graphviz-server
===============

Graphviz-server is a a lightweight Java based HTTP server that invokes the [Graphviz](http://www.graphviz.org/) dot binary installed locally. You simply submit a HTTP POST with the dot graph as the request body and the server returns back a graph in SVG, PDF or PNG format. Graphviz-server uses the [Graphviz Java API](https://github.com/jabbalaci/graphviz-java-api), a Java wrapper that invokes the dot binary using Runtime.exec.

![graphviz-server](http://omerio.com/wp-content/uploads/2013/11/dot_server.png "graphviz-server")

**Source**: [github.com/omerio/graphviz-server](https://github.com/omerio/graphviz-server)

**Author**: [Omer Dawelbeit](http://omerio.com)

## Motivation
Graphviz is a great open source graph visualization and layout tool, unfortunately no runtime exists for Java, so the only option is to invoke the dot binary from a running Java application. This option might not be possible or practical:

* If you are running your app in a Platform as a Service (PaaS) environment like [Google App Engine](https://cloud.google.com/appengine/) where you can't install extra software.
* You can't install the dot binaries in the server running your code, or you need to access Graphviz from more than one server.

In the cases mentioned above it does make sense to setup one graphviz-server in a separate environment, then access it from all your applications that require graphs to be generated.

## Live Demos

Here is a demo running on Google App Engine [http://dot-graphics1.appspot.com/](http://dot-graphics1.appspot.com/). 

The source code for the demo is [here](https://github.com/omerio/graphviz-appengine). The graphviz-server is installed on a [Google Compute Engine](https://cloud.google.com/compute/) VM (see documentation section below).


## Jump start

1. Clone the git repository - `git clone https://github.com/omerio/graphviz-server`
2. A executable jar with dependencies is included in the dist folder. The port on which the server listens can be configured as a command line parameter to the jar. To change the default port (8080) edit the DotGraphics.sh in the dist directory:
```
#!/bin/sh
java -jar DotGraphics.jar 8080 > /dev/null 2>&1 &
exit 0
```


To use the Graphviz server simply submit a HTTP POST with the dot graph script set as the request body. Optionally an output type can be specified on the URL for example:

* Post to http://localhost:8080/svg to render the graph as SVG
* Post to http://localhost:8080/pdf to render the graph as PDF
* Post to http://localhost:8080 to render the graph as PNG (default)


## Documentation

For more details on the implementation of graphviz-server and a detailed guide on how to set it up on Google Compute Engine VM, see this blog post:
[http://omerio.com/2013/11/03/running-a-graphviz-server-on-google-compute-engine/](http://omerio.com/2013/11/03/running-a-graphviz-server-on-google-compute-engine/).

