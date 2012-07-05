package org.nilis.nilismath;

import org.nilis.utils.data.DataPair;
import org.nilis.utils.debug.D;
import org.nilis.utils.debug.HtmlOutputUtils;


import java.awt.BorderLayout;
import java.awt.DisplayMode;
import java.awt.LayoutManager;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.JFrame;

//import org.apache.commons.collections15.CollectionUtils;
import org.nilis.data.managers.FileStoreDataManager.KeyToFilenameDataConverter;
import org.nilis.data.managers.helpers.ByteArrayToFileConverter;
import org.nilis.nilismath.essentials.AssotiativeAndOrderedMemory;
import org.nilis.nilismath.essentials.AssotiativeAndOrderedMemory.GraphNode;
//
//import edu.uci.ics.jung.algorithms.layout.BalloonLayout;
//import edu.uci.ics.jung.algorithms.layout.CircleLayout;
//import edu.uci.ics.jung.algorithms.layout.DAGLayout;
//import edu.uci.ics.jung.algorithms.layout.FRLayout;
//import edu.uci.ics.jung.algorithms.layout.FRLayout2;
//import edu.uci.ics.jung.algorithms.layout.KKLayout;
//import edu.uci.ics.jung.algorithms.layout.Layout;
//import edu.uci.ics.jung.algorithms.layout.SpringLayout;
//import edu.uci.ics.jung.algorithms.layout.SpringLayout2;
//import edu.uci.ics.jung.graph.DirectedSparseGraph;
//import edu.uci.ics.jung.graph.Graph;
//import edu.uci.ics.jung.graph.SparseGraph;
//import edu.uci.ics.jung.visualization.VisualizationViewer;
//import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
//
public class Spawnlings {
	public static void out(Object message) {
		System.out.println(message);
		D.i(message.toString());
	}
	
	public static class A extends JFrame {
		public String f() {
			return toString();
		}
		
		@Override
		public String toString() {
			return "FUCK "+getClass().getCanonicalName();
		}
	}
	
	public static void main(String[] args) {
		D.enableDebug();
		D.enableRemoteLogging("Spawnlings", "dsctp");
		AssotiativeAndOrderedMemory<String> testMemory = new AssotiativeAndOrderedMemory<String>(
				new BasicNodeCriteria<String>(
						new StringsSimilarityCriteria(0,0.7)
					)
		);
		VariableWithMemory<String> var = new VariableWithMemory<String>(testMemory);
		File f = new File("/Users/oleg/text.txt");
		ByteArrayToFileConverter<Object> converter = new ByteArrayToFileConverter<Object>(new KeyToFilenameDataConverter<Object>(".") {

			@Override
			public String keyToFilename(Object key) {
				return null;
			}

			@Override
			public Object filenameToKey(String filename) {
				return null;
			}
		});
		String words[] = new String[0];
		String separators[] = new String[0];
		String inputString = "";
		if(f.exists()) {
			byte input[] = converter.backwardConvert(f);
			try {
				inputString = new String(input, "UTF8");
				words = inputString.split("\\W+");
				separators = inputString.split("\\w+"); 
				for(String word : words) {
					var.set(word.toLowerCase());
				}
				for(String separator : separators) {
					var.set(separator.toLowerCase());
				}
			}
			catch (Exception exception) {
				exception.printStackTrace();
			}
		}
		String newText = "";
		String lastSeparator = "";
		String currWord = "";
		for(int i=0; i < words.length; i++) {
			currWord = testMemory.getAssociated(words[i]);
			if(( lastSeparator.contains(".") ||
					lastSeparator.contains("?") ||
					lastSeparator.contains("!"))&& currWord.length() > 0) {
				currWord = 
				currWord.replaceFirst(Character.valueOf(currWord.charAt(0)).toString(), 
						Character.valueOf(Character.toUpperCase(currWord.charAt(0))).toString());
			}
			newText += currWord;
			if(separators.length - 1 >= i) {
				lastSeparator = separators[i];//testMemory.getAssociated(separators[i]);
				newText += lastSeparator;
			}
		}
		StringCollectionAtomFinder scaf = new StringCollectionAtomFinder();
		scaf.putWords(new HashSet<String>(Arrays.asList(words)));
		out(inputString);
		out(newText);
		Set<DataPair<String, String>> edges = new HashSet<DataPair<String,String>>();
		int count = 0;
		for(GraphNode<String> node : scaf.getNodes()) {
			for(GraphNode<String> linked : node.getLinkedNodes().keySet()) {
				edges.add(new DataPair<String, String>(node.getValue(), linked.getValue()));
			}
			count++;
			if(count>10) {
				break;
			}
		}
		D.i(HtmlOutputUtils.directedGraph(edges, null));
//		Graph<String,  GraphNode<String>> graph = new DirectedSparseGraph<String,  GraphNode<String>>();
//		for(GraphNode<String> node : scaf.getNodes()) {
//			out("Node "+node.toString()+", linked with nodes with values:");
//			for(Entry<GraphNode<String>, Double> nodeEntry : node.getLinkedNodes().entrySet()) {
//				out("      [ "+nodeEntry.getKey().getValue()+", power: "+nodeEntry.getValue()+"] ");
//			}
//			//out(node);
//			
//			graph.addVertex(node.toString());
//			for(GraphNode<String> linkedNode : node.getLinkedNodes().keySet()) {
//				Set<String> edge = new HashSet<String>();
//				edge.add(node.toString());
//				edge.add(linkedNode.toString());
//				try {
//					graph.addEdge(node, edge);
//				} catch(Exception e) {
//					
//				}
//			}
//		}
//		Layout<String, GraphNode<String>> l = new SpringLayout2<String, GraphNode<String>>(graph);
//		VisualizationViewer<String, GraphNode<String>> vv = new VisualizationViewer<String, GraphNode<String>>(l);
//		vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<String>());
//		vv.setSize(2000,2000);
//		l.setSize(vv.getSize());
//		JFrame jf = new JFrame();
//		LayoutManager la = new BorderLayout();
//		jf.setLayout(la);
//		jf.setSize(vv.getSize());
//		jf.getContentPane().add ( vv );
//		jf.setSize(vv.getSize());
//		jf.setVisible(true);
		out("done");
	}
}
