package org.nilis.nilismath;

import java.awt.BorderLayout;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;

import org.nilis.nilismath.BasicNodeCriteria.DataSimilarityCriteria;
import org.nilis.nilismath.essentials.AssotiativeAndOrderedMemory;
import org.nilis.nilismath.essentials.AssotiativeAndOrderedMemory.GraphNode;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.renderers.BasicRenderer;
import edu.uci.ics.jung.visualization.renderers.BasicVertexLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;

public class Spawnlings {

	public static void out(Object message) {
		System.out.println(message);
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
		AssotiativeAndOrderedMemory<Double> testMemory = new AssotiativeAndOrderedMemory<Double>(
				new BasicNodeCriteria<Double>(
						new NumbersSimilarityCriteria(1000)
					)
		);
		VariableWithMemory<Double> var = new VariableWithMemory<Double>(testMemory);
		for(int i=0; i<400; i++) {
			double val = Math.random() * 500;
			var.set(val);
			out(": "+val+", : "+var.get());
			//testMemory.remember(val);
		}
		//out(testMemory);
		StringsSimilarityCriteria ssc = new StringsSimilarityCriteria();
		out(ssc.howSimilar("abc", "dcd"));
//		Graph<GraphNode<Double>,  GraphNode<Double>> graph = new SparseGraph<GraphNode<Double>,  GraphNode<Double>>();
//		for(GraphNode<Double> node : testMemory.getNodes()) {
//			graph.addVertex(node);
//			for(GraphNode<Double> linkedNode : node.getLinkedNodes().keySet()) {
//				Set<GraphNode<Double>> edge = new HashSet<GraphNode<Double>>();
//				edge.add(node);
//				edge.add(linkedNode);
//				try {
//					graph.addEdge(node, edge);
//				} catch(Exception e) {
//					
//				}
//			}
//		}
//		Layout<GraphNode<Double>, GraphNode<Double>> l = new FRLayout<GraphNode<Double>, GraphNode<Double>>( graph );
//		Renderer<GraphNode<Double>, GraphNode<Double>> renderer = new BasicRenderer<GraphNode<Double>, GraphNode<Double>>();
//		BasicVertexLabelRenderer<GraphNode<Double>, GraphNode<Double>> bvlr = new BasicVertexLabelRenderer<GraphNode<Double>, GraphNode<Double>>();
//		renderer.setVertexLabelRenderer(bvlr);
//		VisualizationViewer<GraphNode<Double>, GraphNode<Double>> vv = new VisualizationViewer<GraphNode<Double>, GraphNode<Double>>(l);
//		vv.setRenderer(renderer);
//		JFrame jf = new JFrame();
//		LayoutManager la = new BorderLayout();
//		jf.setLayout(la);
//		jf.setSize(400, 400);
//		jf.getContentPane().add ( vv );
//		jf.setSize(vv.getSize());
//		jf.setVisible(true);
	}
}
