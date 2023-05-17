package info.dawelbeit.graphviz.dot;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

/**
 * A handler for processing the graph rendering request
 * @author omerio
 *
 */
public class HttpDotGraphMessageHandler implements HttpRequestHandler  {

	private static final String TEMP_PATH = "/tmp/graph.";

	// 3 supported types
	private static final String GRAPH_TYPE_PNG = "png";
	private static final String GRAPH_TYPE_PDF = "pdf";
	private static final String GRAPH_TYPE_SVG = "svg";

	private static final String CONTENT_TYPE_PNG = "image/png";
	private static final String CONTENT_TYPE_PDF = "application/pdf";
	private static final String CONTENT_TYPE_SVG = "application/svg+xml";

	private static final Logger log = Logger.getLogger(HttpDotGraphMessageHandler.class.getName());


	/**
	 *
	 * @param url
	 * @param proxyUser
	 * @param proxyPass
	 */
	public HttpDotGraphMessageHandler() {
		super();
	}

	/**
	 * Handle the HTTP Requests
	 */
	public void handle(
			final HttpRequest request,
			final HttpResponse response,
			final HttpContext context) throws HttpException, IOException {

		String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);

		if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
			throw new MethodNotSupportedException(method + " method not supported");
		}

		log.info(request.toString());

		String target = request.getRequestLine().getUri();

		response.setStatusCode(HttpStatus.SC_OK);

		if (request instanceof HttpEntityEnclosingRequest) {
			HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
			byte[] entityContent = EntityUtils.toByteArray(entity);
			log.info("Incoming entity content (bytes): " + entityContent.length);
			String dot = new String(entityContent);
			log.debug("message contents\n" + dot);

			// only respond if we have a valid dot
			if(StringUtils.isNotBlank(dot) && GraphViz.isValidDotText(dot)) {

				target = (StringUtils.isNotBlank(target) ?
						StringUtils.remove((URLDecoder.decode(target, "UTF-8")).trim().toLowerCase(), '/') : null);

				log.info("requesting graph type: " + target);

				HttpEntity graph = this.generateGraph(dot, target);

				response.setEntity(graph);
			}

		}

		log.info("Responded with Success");

	}

	/**
	 *
	 * @param dot
	 * @return
	 */
	private HttpEntity generateGraph(String dot, String target) {

		GraphViz gv = new GraphViz();
		gv.readString(dot);
		log.info(gv.getDotSource());

		ContentType contentType;
		String graphType;

		if(GRAPH_TYPE_SVG.equals(target)) {

			graphType = GRAPH_TYPE_SVG;
			// contentType = ContentType.APPLICATION_SVG_XML;
			contentType = ContentType.create(CONTENT_TYPE_SVG, (Charset) null);

		} else if(GRAPH_TYPE_PDF.equals(target)) {
			graphType = GRAPH_TYPE_PDF;
			contentType = ContentType.create(CONTENT_TYPE_PDF, (Charset) null);

		} else {
			// default png
			graphType = GRAPH_TYPE_PNG;
			contentType = ContentType.create(CONTENT_TYPE_PNG, (Charset) null);
		}

		//	    String type = "dot";
		//	    String type = "fig";    // open with xfig
		//	    String type = "pdf";
		//	    String type = "ps";
		//	    String type = "svg";    // open with inkscape
		//	    String type = "png";
		//	      String type = "plain";
		File out = new File(TEMP_PATH + graphType);   // Linux

		gv.writeGraphToFile( gv.getGraph( gv.getDotSource(), graphType ), out );

		FileEntity body = new FileEntity(out, contentType);
		//FileEntity body = new FileEntity(out, "application/svg+xml; charset=utf-8");

		return body;

	}

}