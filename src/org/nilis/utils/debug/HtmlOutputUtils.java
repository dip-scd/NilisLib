package org.nilis.utils.debug;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.nilis.utils.data.DataPair;


public class HtmlOutputUtils {
	
	private static String mapBasedChartString(String chartType,
			Map<String, Double> input, String keyColumnNameInput, String valueColumnNameInput) {
		String keyColumnName = keyColumnNameInput;
		String valueColumnName = valueColumnNameInput;
		String divId = ""+Math.random();
		String ret = 
		        "<div id='"+divId+"'></div>" +
				"<script type='text/javascript'>"+
      "var data = new google.visualization.DataTable();"+
      "data.addColumn('string', '"+keyColumnName+"');"+
      "data.addColumn('number', '"+valueColumnName+"');";
		for(Entry<String, Double> entry : input.entrySet()) {
			ret+="data.addRow(['"+entry.getKey()+"', "+entry.getValue()+"]);";
		}
        ret+="var options = {'width':400,'height':300};"+
        "var chart = new google.visualization."+chartType+"(document.getElementById('"+divId+"'));"+
        "chart.draw(data, options);"+
        "</script>";
		
		return ret;
	}
	
	public static String pieChart(Map<String, Double> input, String keyColumnNameInput, String valueColumnNameInput) {
		return mapBasedChartString("PieChart", input, keyColumnNameInput, valueColumnNameInput);
	}
	
	public static String barChart(Map<String, Double> input, String keyColumnNameInput, String valueColumnNameInput) {
		return mapBasedChartString("BarChart", input, keyColumnNameInput, valueColumnNameInput);
	}
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("dd MMMMM, yyyy HH:mm:ss");
	public static String linearTimedChart(Map<Date, Long> input, String valueTag) {
			String divId = ""+Math.random();
			String ret = 
			        "<div id='"+divId+"'></div>" +
					"<script type='text/javascript'>"+
	      "var data = new google.visualization.DataTable();"+
	      "data.addColumn('datetime', '"+"time"+"');"+
	      "data.addColumn('number', '"+valueTag+"');";
			
			for(Entry<Date, Long> entry : input.entrySet()) {
				ret+="data.addRow([new Date('"+sdf.format(entry.getKey())+"'), "+entry.getValue()+"]);";
			}
	        ret+="var options = {'width':800,'height':600};"+
	        "var chart = new google.visualization."+"LineChart"+"(document.getElementById('"+divId+"'));"+
	        "chart.draw(data, options);"+
	        "</script>";
			
			return ret;
	}
	
	public static String image(String imageUrl) {
		return "<br><img src='"+imageUrl+"' ></img>";
	}
	
	public static String smallImage(String imageUrl) {
		return "<br><img style='width: 160px' src='"+imageUrl+"' ></img>";
	}
	
	private static String parametrizedGraph(Set<DataPair<String, String>> edges, Set<String> standaloneNodes, int type) {
		String graphTypeString = "graph";
		if(type != 0) {
			graphTypeString = "digraph";
		}
		String ret = "<br><img src='http://chart.googleapis.com/chart?cht=gv&chl="+graphTypeString+"{";
		int count = 0;
		int size = 0;
		if(edges != null) {
			count = 0;
			size = edges.size();
			for(DataPair<String, String> edge : edges) {
				ret+=edge.getTag().replaceAll("\\W", "_");
				if(type == 0) {
					ret+="--";
				} else {
					ret+="->";
				}
				ret+=edge.getData().replaceAll("\\W", "_");
				count++;
				if(count < size) {
					ret+=",";
				}
			}
		}
		if(standaloneNodes != null) {
			count = 0;
			if(size != 0 && standaloneNodes.size() != 0) {
				ret += ",";
			}
			size = standaloneNodes.size();
			
			for(String node : standaloneNodes) {
				ret+=node;
				count++;
				if(count < size) {
					ret+=",";
				}
			}
		}
		ret +="}' />";
		return ret;
	}
	
	public static String graph(Set<DataPair<String, String>> edges, Set<String> standaloneNodes) {
		return parametrizedGraph(edges, standaloneNodes, 0);
	}
	
	public static String directedGraph(Set<DataPair<String, String>> edges, Set<String> standaloneNodes) {
		return parametrizedGraph(edges, standaloneNodes, 1);
	}
}
