package org.nilis.utils.debug;

import java.util.Map;
import java.util.Map.Entry;


public class HtmlOutputUtils {
	public static String pieChart(Map<String, Double> input, String keyColumnNameInput, String valueColumnNameInput) {
		String keyColumnName = keyColumnNameInput;
		String valueColumnName = valueColumnNameInput;
		String ret = "<script type='text/javascript' src='https://www.google.com/jsapi'></script>"+
    "<script type='text/javascript'>"+
     "google.load('visualization', '1.0', {'packages':['corechart']});"+
      "google.setOnLoadCallback(drawChart);"+
      "function drawChart() {"+
      "var data = new google.visualization.DataTable();"+
      "data.addColumn('string', '"+keyColumnName+"');"+
      "data.addColumn('number', '"+valueColumnName+"');";
		for(Entry<String, Double> entry : input.entrySet()) {
			ret+="data.addRow(['"+entry.getKey()+"', "+entry.getValue()+"]);";
		}
        ret+="var options = {'width':400,'height':300};"+
        "var chart = new google.visualization.PieChart(document.getElementById('chart_div'));"+
        "chart.draw(data, options);}</script>"+
        "<div id=\"chart_div\"></div>";
		
		return ret;
	}
}
