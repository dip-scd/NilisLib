package org.nilis.utils.debug;

import java.util.Map;
import java.util.Map.Entry;


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
	
	public static String image(String imageUrl) {
		return "<br><img src='"+imageUrl+"' ></img>";
	}
	
	public static String smallImage(String imageUrl) {
		return "<br><img style='width: 160px' src='"+imageUrl+"' ></img>";
	}
}
