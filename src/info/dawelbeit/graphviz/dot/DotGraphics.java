/**
 * 
 */
package info.dawelbeit.graphviz.dot;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpConnectionFactory;
import org.apache.http.HttpException;
import org.apache.http.HttpServerConnection;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.impl.DefaultBHttpServerConnectionFactory;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.apache.http.protocol.UriHttpRequestHandlerMapper;
import org.apache.log4j.Logger;

/**
 * Bare-bones HTTP server to listen for graph rendering requests and call Dot command line tool
 * 
 * @author omerio
 *
 */
public class DotGraphics {
	
	private static final Logger log = Logger.getLogger(DotGraphics.class.getName());

	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		int port = Integer.parseInt(System.getenv("PORT"));

		// Set up the HTTP protocol processor
		HttpProcessor httpproc = HttpProcessorBuilder.create()
				.add(new ResponseDate())
				.add(new ResponseServer("DotGraphics/1.1"))
				.add(new ResponseContent())
				.add(new ResponseConnControl()).build();

		// Set up request handlers
		UriHttpRequestHandlerMapper reqistry = new UriHttpRequestHandlerMapper();
		reqistry.register("*", new HttpDotGraphMessageHandler());

		// Set up the HTTP service
		HttpService httpService = new HttpService(httpproc, NoConnectionReuseStrategy.INSTANCE, null, reqistry, null);
	
		Thread t = new RequestListenerThread(port, httpService);
		t.setDaemon(false);
		t.start();
	}

	/**
	 * A request listener thread that listens for incoming requests
	 * When a request is recieved it forks a WorkerThread to handle it
	 * @author omerio
	 *
	 */
	static class RequestListenerThread extends Thread {

		private final HttpConnectionFactory<DefaultBHttpServerConnection> connFactory;
		private final ServerSocket serversocket;
		private final HttpService httpService;

		/**
		 * 
		 * @param port
		 * @param httpService
		 * @throws IOException
		 */
		public RequestListenerThread(
				final int port,
				final HttpService httpService) throws IOException {
			this.connFactory = DefaultBHttpServerConnectionFactory.INSTANCE;
			this.serversocket = new ServerSocket(port);
			this.httpService = httpService;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			log.info("Listening on port " + this.serversocket.getLocalPort());
			while (!Thread.interrupted()) {
				try {
					// Set up HTTP connection
					Socket socket = this.serversocket.accept();
					log.info("Incoming connection from " + socket.getInetAddress());
					HttpServerConnection conn = this.connFactory.createConnection(socket);

					// Start worker thread
					Thread t = new WorkerThread(this.httpService, conn);
					t.setDaemon(true);
					t.start();
					
				} catch (InterruptedIOException ex) {
					break;
				} catch (IOException e) {
					log.error("I/O error initialising connection thread: " + e.getMessage(), e);
					break;
				}
			}
		}
	}

	/**
	 * WorkerThread that runs as a daemon and handles incoming HTTP requests
	 * @author omerio
	 *
	 */
	static class WorkerThread extends Thread {

		private final HttpService httpservice;
		private final HttpServerConnection conn;

		/**
		 * 
		 * @param httpservice
		 * @param conn
		 */
		public WorkerThread(
				final HttpService httpservice,
				final HttpServerConnection conn) {
			super();
			this.httpservice = httpservice;
			this.conn = conn;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			log.info("New connection thread");
			HttpContext context = new BasicHttpContext(null);
			try {
				while (!Thread.interrupted() && this.conn.isOpen()) {
					this.httpservice.handleRequest(this.conn, context);
				}
			} catch (ConnectionClosedException ex) {
				log.error("Client closed connection", ex);
				
			} catch (IOException ex) {
				log.error("I/O error: " + ex.getMessage(), ex);
				
			} catch (HttpException ex) {
				log.error("Unrecoverable HTTP protocol violation: " + ex.getMessage(), ex);
				
			} finally {
				try {
					this.conn.shutdown();
				} catch (IOException ignore) {}
			}
		}

	}


}
