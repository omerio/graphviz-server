graphviz-server
===============

Java based GraphViz Http Server. A executable jar with dependencies is included in the dist folder. The port on which the server listens can be configured as a command line parameter to the jar. To change the default port (8080) edit the DotGraphics.sh in the dist directory:

```
#!/bin/sh
java -jar DotGraphics.jar 8080 > /dev/null 2>&1 &
exit 0
```

To use the Graphviz server simply submit a HTTP POST with the dot graph script set as the request body. Optionally an output type can be specified on the URL for example:

* Post to http://localhost:8080/svg to render the graph as SVG
* Post to http://localhost:8080/pdf to render the graph as PDF
* Post to http://localhost:8080 to render the graph as PNG (default)

