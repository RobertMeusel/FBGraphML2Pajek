package de.unimannheim.informatik.wifo5.webmining.parser;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReader;

/**
 * Small parser which converts the .graphml file provided by the NameGenWeb
 * Application of the Oxofor Internet Institute, University of Oxford into Pajek
 * readable files. The general network structure will be stored in the .net
 * file. Vertex labels as gender, relationship_status and locale will be stored
 * in the partion files *.clu. The number of likes and number of friends of each
 * vertex will be stored in the vector file (*.vec).
 * 
 * @author Robert Meusel (robert.meusel@freenet.de)
 * 
 */
// TODO add age (calculated from birthday)
// TODO allow conversion of anonymized files.
public class FBGraphMLParser {

	private static final String XML_FILE = "profile.graphml";

	public static void main(String[] args) {
		boolean ano = false;
		try {
			if (args != null && args[0] != null && args[0].equals("true")) {
				ano = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {

			Graph graph = new TinkerGraph();
			GraphMLReader graphReader = new GraphMLReader(graph);

			InputStream is = new BufferedInputStream(new FileInputStream(
					XML_FILE));
			graphReader.inputGraph(is);

			Iterable<Vertex> vertices = graph.getVertices();
			Iterator<Vertex> verticesIterator = vertices.iterator();

			List<Vertex> verticesList = new ArrayList<Vertex>();
			// add numbering and put into list
			int regNum = 0;
			List<String> locals = new ArrayList<String>();
			List<String> rStatus = new ArrayList<String>();
			Set<String> localsSet = new HashSet<String>();
			Set<String> rStatusSet = new HashSet<String>();
			while (verticesIterator.hasNext()) {
				regNum++;
				Vertex vertex = verticesIterator.next();
				vertex.setProperty("regNum", regNum);
				// add null locale
				localsSet.add("unknown");
				if (vertex.getProperty("locale") != null) {
					localsSet.add(vertex.getProperty("locale").toString());
				} else {
					// set property if null
					vertex.setProperty("locale", "unknown");
				}
				rStatusSet.add("unknown");
				if (vertex.getProperty("relationship_status") != null) {
					rStatusSet.add(vertex.getProperty("relationship_status")
							.toString());
				} else {
					// set property if null
					vertex.setProperty("relationship_status", "unknown");
				}
				verticesList.add(vertex);
			}
			locals.addAll(localsSet);
			Collections.sort(locals);

			rStatus.addAll(rStatusSet);
			Collections.sort(rStatus);

			// write files for pajek
			BufferedWriter netWriter = new BufferedWriter(new FileWriter(
					XML_FILE.replace("graphml", "net")));
			netWriter.write("*Vertices " + regNum + "\n");
			BufferedWriter sexCluWriter = new BufferedWriter(new FileWriter(
					XML_FILE.replace(".graphml", "_sex.clu")));
			sexCluWriter.write("*Vertices " + regNum + "\n");
			BufferedWriter localCluWriter = new BufferedWriter(new FileWriter(
					XML_FILE.replace(".graphml", "_local.clu")));
			localCluWriter.write("*Vertices " + regNum + "\n");
			BufferedWriter statusCluWriter = new BufferedWriter(new FileWriter(
					XML_FILE.replace(".graphml", "_rs.clu")));
			statusCluWriter.write("*Vertices " + regNum + "\n");
			BufferedWriter likesCountVecWriter = new BufferedWriter(
					new FileWriter(XML_FILE.replace(".graphml", "_lc.vec")));
			likesCountVecWriter.write("*Vertices " + regNum + "\n");
			BufferedWriter friendsCountVecWriter = new BufferedWriter(
					new FileWriter(XML_FILE.replace(".graphml", "_fc.vec")));
			friendsCountVecWriter.write("*Vertices " + regNum + "\n");
			// writing vertex
			for (Vertex vertex : verticesList) {
				if (ano) {
					netWriter.write(vertex.getProperty("regNum") + " \""
							+ vertex.getProperty("regNum") + "\"\n");
				} else {
					netWriter.write(vertex.getProperty("regNum") + " \""
							+ vertex.getProperty("Label") + "\"\n");
				}
				int sex = 2;
				if (vertex.getProperty("sex") != null) {
					if (vertex.getProperty("sex").equals("female")) {
						sex = 0;
					} else if (vertex.getProperty("sex").equals("male")) {
						sex = 1;
					}
				}
				sexCluWriter.write(" " + sex + "\n");
				// locale cannot be null here
				localCluWriter.write(" "
						+ locals.indexOf(vertex.getProperty("locale")
								.toString()) + "\n");
				// relationship status cannot be null here
				statusCluWriter.write(" "
						+ rStatus.indexOf(vertex.getProperty(
								"relationship_status").toString()) + "\n");

				if (vertex.getProperty("friend_count") != null) {
					friendsCountVecWriter.write(" "
							+ vertex.getProperty("friend_count") + "\n");
				} else {
					friendsCountVecWriter.write(" 1" + "\n");
				}
				if (vertex.getProperty("likes_count") != null) {
					likesCountVecWriter.write(" "
							+ vertex.getProperty("likes_count") + "\n");
				} else {
					likesCountVecWriter.write(" 1" + "\n");
				}
			}
			// writing edges
			netWriter.write("*Edges :2 \"Friends\"\n");
			for (Vertex vertex : verticesList) {
				Iterable<Vertex> edgedVertices = vertex
						.getVertices(Direction.IN);
				Iterator<Vertex> edgedVerticesIterator = edgedVertices
						.iterator();
				while (edgedVerticesIterator.hasNext()) {
					Vertex edgedVertex = edgedVerticesIterator.next();
					netWriter.write(vertex.getProperty("regNum") + " "
							+ edgedVertex.getProperty("regNum") + " 1\n");
				}
			}

			// write README
			BufferedWriter rmWriter = new BufferedWriter(new FileWriter("log"));
			rmWriter.write("* Files where created using profile.graphml\n*\n");
			rmWriter.write("* Currently supported attributes:\n");
			rmWriter.write("** sex\n");
			rmWriter.write("** locale\n");
			rmWriter.write("** friend count\n");
			rmWriter.write("** like count\n");
			rmWriter.write("*\n*\n");
			rmWriter.write("* GENDER:\n** 0 = female\n** 1 = male\n** 2 = unknown/no information");
			rmWriter.write("\n*\n");
			rmWriter.write("* LOCALE: \n");
			for (String s : locals) {
				rmWriter.write("** " + locals.indexOf(s) + " = " + s + "\n");
			}
			rmWriter.write("*\n");
			rmWriter.write("* RELATIONSHIP STATUS: \n");
			for (String s : rStatus) {
				rmWriter.write("** " + rStatus.indexOf(s) + " = " + s + "\n");
			}

			// Close all writer
			rmWriter.flush();
			rmWriter.close();

			netWriter.flush();
			netWriter.close();

			sexCluWriter.flush();
			sexCluWriter.close();

			localCluWriter.flush();
			localCluWriter.close();

			statusCluWriter.flush();
			statusCluWriter.close();

			likesCountVecWriter.flush();
			likesCountVecWriter.close();

			friendsCountVecWriter.flush();
			friendsCountVecWriter.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
