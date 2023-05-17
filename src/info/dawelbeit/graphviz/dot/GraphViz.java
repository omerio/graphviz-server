package info.dawelbeit.graphviz.dot;
// GraphViz.java - a simple API to call dot from Java programs

/*$Id$*/
/*
 ******************************************************************************
 *                                                                            *
 *              (c) Copyright 2003 Laszlo Szathmary                           *
 *                                                                            *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms of the GNU Lesser General Public License as published by   *
 * the Free Software Foundation; either version 2.1 of the License, or        *
 * (at your option) any later version.                                        *
 *                                                                            *
 * This program is distributed in the hope that it will be useful, but        *
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY *
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public    *
 * License for more details.                                                  *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public License   *
 * along with this program; if not, write to the Free Software Foundation,    *
 * Inc., 675 Mass Ave, Cambridge, MA 02139, USA.                              *
 *                                                                            *
 ******************************************************************************
 */

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <dl>
 * <dt>Purpose: GraphViz Java API
 * <dd>
 *
 * <dt>Description:
 * <dd> With this Java class you can simply call dot
 *      from your Java programs
 * <dt>Example usage:
 * <dd>
 * <pre>
 *    GraphViz gv = new GraphViz();
 *    gv.addln(gv.start_graph());
 *    gv.addln("A -> B;");
 *    gv.addln("A -> C;");
 *    gv.addln(gv.end_graph());
 *    System.out.println(gv.getDotSource());
 *
 *    String type = "gif";
 *    File out = new File("out." + type);   // out.gif in this example
 *    gv.writeGraphToFile( gv.getGraph( gv.getDotSource(), type ), out );
 * </pre>
 * </dd>
 *
 * </dl>
 *
 * @version v0.4, 2011/02/05 (February) -- Patch of Keheliya Gallaba is added. Now you
 * can specify the type of the output file: gif, dot, fig, pdf, ps, svg, png, etc.
 * @version v0.3, 2010/11/29 (November) -- Windows support + ability
 * to read the graph from a text file
 * @version v0.2, 2010/07/22 (July) -- bug fix
 * @version v0.1, 2003/12/04 (December) -- first release
 * @author  Laszlo Szathmary (<a href="jabba.laci@gmail.com">jabba.laci@gmail.com</a>)
 */
public class GraphViz
{

	private static final Logger log = Logger.getLogger(GraphViz.class.getName());
	/**
	 * The dir. where temporary files will be created.
	 */
	private static String TEMP_DIR = "/tmp";	// Linux
	//  private static String TEMP_DIR = "c:/temp";	// Windows

	/**
	 * Where is your dot program located? It will be called externally.
	 */
	//private static String DOT = "/usr/local/bin/dot"; // MAC
	//private static String DOT = "/usr/bin/dot";	// Linux
	private static String DOT = "C:/Program Files/Graphviz/bin/dot.exe";	// Windows

	public static final String GRAPH_START = "digraph G {";

	public static final String GRAPH_END = "}";

	/**
	 * The source of the graph written in dot language.
	 */
	private StringBuilder graph = new StringBuilder();

	/**
	 * Constructor: creates a new GraphViz object that will contain
	 * a graph.
	 */
	public GraphViz() {
	}

	/**
	 * Returns the graph's source description in dot language.
	 * @return Source of the graph in dot language.
	 */
	public String getDotSource() {
		return graph.toString();
	}

	/**
	 * Adds a string to the graph's source (without newline).
	 */
	public void add(String line) {
		graph.append(line);
	}

	/**
	 * Adds a string to the graph's source (with newline).
	 */
	public void addln(String line) {
		graph.append(line + "\n");
	}

	/**
	 * Adds a newline to the graph's source.
	 */
	public void addln() {
		graph.append('\n');
	}

	/**
	 * Returns the graph as an image in binary format.
	 * @param dot_source Source of the graph to be drawn.
	 * @param type Type of the output image to be produced, e.g.: gif, dot, fig, pdf, ps, svg, png.
	 * @return A byte array containing the image of the graph.
	 */
	public byte[] getGraph(String dot_source, String type)
	{
		File dot;
		byte[] img_stream = null;

		try {
			dot = writeDotSourceToFile(dot_source);
			if (dot != null)
			{
				img_stream = get_img_stream(dot, type);
				if (dot.delete() == false)
					log.warn("Warning: " + dot.getAbsolutePath() + " could not be deleted!");
				return img_stream;
			}
			return null;
		} catch (java.io.IOException ioe) { return null; }
	}

	/**
	 * Writes the graph's image in a file.
	 * @param img   A byte array containing the image of the graph.
	 * @param file  Name of the file to where we want to write.
	 * @return Success: 1, Failure: -1
	 */
	public int writeGraphToFile(byte[] img, String file)
	{
		File to = new File(file);
		return writeGraphToFile(img, to);
	}

	/**
	 * Writes the graph's image in a file.
	 * @param img   A byte array containing the image of the graph.
	 * @param to    A File object to where we want to write.
	 * @return Success: 1, Failure: -1
	 */
	public int writeGraphToFile(byte[] img, File to)
	{
		try {
			FileOutputStream fos = new FileOutputStream(to);
			fos.write(img);
			fos.close();
		} catch (java.io.IOException ioe) { return -1; }
		return 1;
	}

	/**
	 * It will call the external dot program, and return the image in
	 * binary format.
	 * @param dot Source of the graph (in dot language).
	 * @param type Type of the output image to be produced, e.g.: gif, dot, fig, pdf, ps, svg, png.
	 * @return The image of the graph in .gif format.
	 */
	private byte[] get_img_stream(File dot, String type)
	{
		File img;
		byte[] img_stream = null;

		try {
			img = File.createTempFile("graph_", "."+type, new File(GraphViz.TEMP_DIR));
			Runtime rt = Runtime.getRuntime();

			// patch by Mike Chenault
			String[] args = {DOT, "-T"+type, dot.getAbsolutePath(), "-o", img.getAbsolutePath()};
			Process p = rt.exec(args);

			p.waitFor();

			FileInputStream in = new FileInputStream(img.getAbsolutePath());
			img_stream = new byte[in.available()];
			in.read(img_stream);
			// Close it if we need to
			if( in != null ) in.close();

			if (img.delete() == false)
				log.warn("Warning: " + img.getAbsolutePath() + " could not be deleted!");
		}
		catch (java.io.IOException ioe) {
			log.warn("Error:    in I/O processing of tempfile in dir " + GraphViz.TEMP_DIR+"\n");
			log.warn("       or in calling external command");
			log.error("stacktrace", ioe);
		}
		catch (java.lang.InterruptedException ie) {
			log.error("Error: the execution of the external program was interrupted", ie);
		}

		return img_stream;
	}

	/**
	 * Writes the source of the graph in a file, and returns the written file
	 * as a File object.
	 * @param str Source of the graph (in dot language).
	 * @return The file (as a File object) that contains the source of the graph.
	 */
	private File writeDotSourceToFile(String str) throws java.io.IOException
	{
		File temp;
		try {
			temp = File.createTempFile("graph_", ".dot.tmp", new File(GraphViz.TEMP_DIR));
			FileWriter fout = new FileWriter(temp);
			fout.write(str);
			fout.close();
		}
		catch (Exception e) {
			log.error("Error: I/O error while writing the dot source to temp file!", e);
			return null;
		}
		return temp;
	}

	/**
	 * Returns a string that is used to start a graph.
	 * @return A string to open a graph.
	 */
	public String start_graph() {
		return GRAPH_START;
	}

	/**
	 * Returns a string that is used to end a graph.
	 * @return A string to close a graph.
	 */
	public String end_graph() {
		return GRAPH_END;
	}

	/**
	 * Read a DOT graph from a text file.
	 *
	 * @param input Input text file containing the DOT graph
	 * source.
	 */
	public void readSource(String input)
	{
		StringBuilder sb = new StringBuilder();

		try
		{
			FileInputStream fis = new FileInputStream(input);
			DataInputStream dis = new DataInputStream(fis);
			BufferedReader br = new BufferedReader(new InputStreamReader(dis));
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			dis.close();
		}
		catch (Exception e) {
			log.error("Error: ", e);
		}

		this.graph = sb;
	}

	/**
	 * read from a string
	 * @param dot
	 */
	public void readString(String dot) {
		this.graph = new StringBuilder(dot);
	}

	/**
	 * is a valid dot source
	 * @param dot
	 * @return
	 */
	public static boolean isValidDotText(String dot) {
		// return StringUtils.isNotBlank(dot) && (dot.indexOf(GRAPH_START) > -1);
		return StringUtils.isNotBlank(dot) && (checkGraphStart(dot));
	}

	private static boolean checkGraphStart(String dot) {
		boolean ret_val;

		// "digraph" 다음에 오는 단어를 찾는 정규 표현식 패턴
        String patternString = "digraph\\s+(\\w+)\\s*\\{";

        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(dot);

        if (matcher.find()) {
            ret_val = true;
        } else {
            ret_val = false;
        }
		return ret_val;
	}

} // end of class GraphViz

