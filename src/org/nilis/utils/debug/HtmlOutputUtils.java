package org.nilis.utils.debug;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.nilis.utils.data.DataPair;


public class HtmlOutputUtils {
	
	private static final String SCRIPT = "</script>";
	private static final String CHART_DRAW_DATA_OPTIONS = "chart.draw(data, options);";
	private static final String VAR_CHART_NEW_GOOGLE_VISUALIZATION = "var chart = new google.visualization.";
	private static final String VAR_OPTIONS_WIDTH_400_HEIGHT_300 = "var options = {'width':400,'height':300};";
	private static final String STRING12 = "', ";
	private static final String DATA_ADD_ROW2 = "data.addRow(['";
	private static final String STRING11 = "');";
	private static final String DATA_ADD_COLUMN_NUMBER = "data.addColumn('number', '";
	private static final String DATA_ADD_COLUMN_STRING = "data.addColumn('string', '";
	private static final String VAR_DATA_NEW_GOOGLE_VISUALIZATION_DATA_TABLE = "var data = new google.visualization.DataTable();";
	private static final String STRING10 = "}' />";
	private static final String STRING9 = ",";
	private static final String STRING8 = "->";
	private static final String STRING7 = "--";
	private static final String _ = "_";
	private static final String W = "\\W";
	private static final String STRING6 = "{";
	private static final String GRAPH = "graph";
	private static final String DIGRAPH = "digraph";
	private static final String BR_IMG_SRC_HTTP_CHART_GOOGLEAPIS_COM_CHART_CHT_GV_CHL = "<br><img src='http://chart.googleapis.com/chart?cht=gv&chl=";
	private static final String BR_IMG_STYLE_WIDTH_160PX_SRC = "<br><img style='width: 160px' src='";
	private static final String IMG = "' ></img>";
	private static final String BR_IMG_SRC = "<br><img src='";
	private static final String CHART_DRAW_DATA_OPTIONS_SCRIPT = "chart'].draw(data, options);};</script>";
	private static final String CHART_UNDEFINED_WINDOW2 = "chart'] != undefined) { window['";
	private static final String STRING5 = "};";
	private static final String STRING4 = "'));";
	private static final String DOCUMENT_GET_ELEMENT_BY_ID = "(document.getElementById('";
	private static final String LINE_CHART = "LineChart";
	private static final String CHART_NEW_GOOGLE_VISUALIZATION = "chart'] = new google.visualization.";
	private static final String WINDOW = "window['";
	private static final String CHART_NULL = "chart'] == null) {";
	private static final String CHART_UNDEFINED_WINDOW = "chart'] == undefined || window['";
	private static final String IF_WINDOW = "if(window['";
	private static final String VAR_OPTIONS_WIDTH_600_HEIGHT_300 = "var options = {'width':600,'height':300};";
	private static final String DATA_SORT_COLUMN_0 = "data.sort([{column: 0}]);";
	private static final String WHILE_DATA_GET_NUMBER_OF_ROWS_1000_DATA_REMOVE_ROW_0 = "while(data.getNumberOfRows() > 1000) {data.removeRow(0);};";
	private static final String STRING3 = "]);";
	private static final String DATA_ADD_ROW = "data.addRow([";
	private static final String STRING2 = "'];";
	private static final String VAR_DATA_WINDOW_DATA_TABLE = "var data = window['dataTable";
	private static final String STRING = "');}";
	private static final String ADD_COLUMN_NUMBER = "'].addColumn('number', '";
	private static final String ADD_COLUMN_NUMBER_TIME = "'].addColumn('number', 'time');";
	private static final String WINDOW_DATA_TABLE2 = "window['dataTable";
	private static final String NEW_GOOGLE_VISUALIZATION_DATA_TABLE = "']=new google.visualization.DataTable();";
	private static final String WINDOW_DATA_TABLE = ") {window['dataTable";
	private static final String UNDEFINED = "'] == undefined || ";
	private static final String IF_WINDOW_DATA_TABLE = "if(window['dataTable";
	private static final String SCRIPT_TYPE_TEXT_JAVASCRIPT = "<script type='text/javascript'>";
	private static final String SPAN_SCRIPT = "'></span\"); }</script>";
	private static final String $_STATIC_AREA_APPEND_SPAN_ID = "$('#static_area').append(\"<span id='";
	private static final String SIZE_0 = "').size() <= 0) {";
	private static final String SCRIPT_IF_$ = "<script>if($('#";
	private static final String DIV = "'></div>";
	private static final String DIV_ID = "<div id='";
	private static String mapBasedChartString(String chartType,
			Map<String, Double> input, String keyColumnNameInput, String valueColumnNameInput) {
		String keyColumnName = keyColumnNameInput;
		String valueColumnName = valueColumnNameInput;
		String divId = String.valueOf(Math.random());
		String ret = 
		        DIV_ID+divId+DIV +
				SCRIPT_TYPE_TEXT_JAVASCRIPT+
      VAR_DATA_NEW_GOOGLE_VISUALIZATION_DATA_TABLE+
      DATA_ADD_COLUMN_STRING+keyColumnName+STRING11+
      DATA_ADD_COLUMN_NUMBER+valueColumnName+STRING11;
		for(Entry<String, Double> entry : input.entrySet()) {
			ret+=DATA_ADD_ROW2+entry.getKey()+STRING12+entry.getValue()+STRING3;
		}
        ret+=VAR_OPTIONS_WIDTH_400_HEIGHT_300+
        VAR_CHART_NEW_GOOGLE_VISUALIZATION+chartType+DOCUMENT_GET_ELEMENT_BY_ID+divId+STRING4+
        CHART_DRAW_DATA_OPTIONS+
        SCRIPT;
		
		return ret;
	}
	
	public static String pieChart(Map<String, Double> input, String keyColumnNameInput, String valueColumnNameInput) {
		return mapBasedChartString("PieChart", input, keyColumnNameInput, valueColumnNameInput);
	}
	
	public static String barChart(Map<String, Double> input, String keyColumnNameInput, String valueColumnNameInput) {
		return mapBasedChartString("BarChart", input, keyColumnNameInput, valueColumnNameInput);
	}
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("dd MMMMM, yyyy HH:mm:ss");
	public static String linearTimedChart(Map<Date, Long> input, String valueTag, String chartTag, boolean clearChart) {
		String divId = "";
		if(chartTag != null) {
			divId = chartTag;
		} else {
			divId+=Math.round(Math.random()*10000000);
		}
			String ret = "";
			if(chartTag == null) {
			        ret+=DIV_ID+divId+DIV;
			} else {
				ret+=SCRIPT_IF_$+divId+SIZE_0;
				ret+=$_STATIC_AREA_APPEND_SPAN_ID+divId+SPAN_SCRIPT;
			}
	      ret+=SCRIPT_TYPE_TEXT_JAVASCRIPT;
	      ret+=IF_WINDOW_DATA_TABLE+divId+UNDEFINED+clearChart+WINDOW_DATA_TABLE+divId+NEW_GOOGLE_VISUALIZATION_DATA_TABLE;
	      ret+=WINDOW_DATA_TABLE2+divId+ADD_COLUMN_NUMBER_TIME+
	    	    WINDOW_DATA_TABLE2+divId+ADD_COLUMN_NUMBER+valueTag+STRING;
	      ret+=VAR_DATA_WINDOW_DATA_TABLE+divId+STRING2;
			
			for(Entry<Date, Long> entry : input.entrySet()) {
				ret+=DATA_ADD_ROW+entry.getKey().getTime()
			/*"new Date('"+sdf.format(entry.getKey())+"')"*/+", "+entry.getValue()+STRING3;
			}
			ret+=WHILE_DATA_GET_NUMBER_OF_ROWS_1000_DATA_REMOVE_ROW_0;
			ret+=DATA_SORT_COLUMN_0;
	        ret+=VAR_OPTIONS_WIDTH_600_HEIGHT_300;
        	ret+=IF_WINDOW+chartTag+CHART_UNDEFINED_WINDOW+chartTag+CHART_NULL;
        	ret+=WINDOW+chartTag+CHART_NEW_GOOGLE_VISUALIZATION+LINE_CHART+DOCUMENT_GET_ELEMENT_BY_ID+divId+STRING4;
        	ret+=STRING5;
	        ret+=IF_WINDOW+chartTag+CHART_UNDEFINED_WINDOW2+chartTag+CHART_DRAW_DATA_OPTIONS_SCRIPT;
			
			return ret;
	}
	
	
	private static Map<String, Map<Date, Long>> valueDeltas = new LinkedHashMap<String, Map<Date,Long>>();

	private static long lastMeasuredValue = 0;
	public static void logValueForLinearChart(String memoryLogId, long value) {
		logValueForLinearChart(memoryLogId, value, true);
	}
	
	private static String valueLegentPrefix = "";
	private static String valueLegentPostfix = "";
	public static void logValueForLinearChart(String memoryLogId, long value, boolean submitLog) {
		lastMeasuredValue = value;
		boolean clearChart = false;
		if(!valueDeltas.containsKey(memoryLogId)) {
			clearChart = true;
			valueDeltas.put(memoryLogId, new LinkedHashMap<Date, Long>());
		}
		valueDeltas.get(memoryLogId).put(new Date(), lastMeasuredValue);
		if(submitLog || valueDeltas.get(memoryLogId).size() > 60) {
			D.i(HtmlOutputUtils.linearTimedChart(valueDeltas.get(memoryLogId), valueLegentPrefix+memoryLogId+valueLegentPostfix,
					memoryLogId, clearChart));
			valueDeltas.get(memoryLogId).clear();
		}
	}
	
	public static String image(String imageUrl) {
		return BR_IMG_SRC+imageUrl+IMG;
	}
	
	public static String smallImage(String imageUrl) {
		return BR_IMG_STYLE_WIDTH_160PX_SRC+imageUrl+IMG;
	}
	
	private static String parametrizedGraph(Set<DataPair<String, String>> edges, Set<String> standaloneNodes, int type) {
		String graphTypeString = GRAPH;
		if(type != 0) {
			graphTypeString = DIGRAPH;
		}
		String ret = BR_IMG_SRC_HTTP_CHART_GOOGLEAPIS_COM_CHART_CHT_GV_CHL+graphTypeString+STRING6;
		int count = 0;
		int size = 0;
		if(edges != null) {
			count = 0;
			size = edges.size();
			for(DataPair<String, String> edge : edges) {
				ret+=edge.getTag().replaceAll(W, _);
				if(type == 0) {
					ret+=STRING7;
				} else {
					ret+=STRING8;
				}
				ret+=edge.getData().replaceAll(W, _);
				count++;
				if(count < size) {
					ret+=STRING9;
				}
			}
		}
		if(standaloneNodes != null) {
			count = 0;
			if(size != 0 && standaloneNodes.size() != 0) {
				ret += STRING9;
			}
			size = standaloneNodes.size();
			
			for(String node : standaloneNodes) {
				ret+=node;
				count++;
				if(count < size) {
					ret+=STRING9;
				}
			}
		}
		ret +=STRING10;
		return ret;
	}
	
	public static String graph(Set<DataPair<String, String>> edges, Set<String> standaloneNodes) {
		return parametrizedGraph(edges, standaloneNodes, 0);
	}
	
	public static String directedGraph(Set<DataPair<String, String>> edges, Set<String> standaloneNodes) {
		return parametrizedGraph(edges, standaloneNodes, 1);
	}
	
	public static String coloredSpan(String input, String color) {
		return "<span style=\" color: "+color+";\" >"+input+"</span>";
	}
}
