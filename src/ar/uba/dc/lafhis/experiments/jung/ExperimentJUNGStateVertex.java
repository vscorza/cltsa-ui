package ar.uba.dc.lafhis.experiments.jung;

/**
 * A state of a LTS, for use as a JUNG vertex in LTSGraph
 * @author Cédric Delforge
 */
public class ExperimentJUNGStateVertex {
	private String graphName;
	private int stateIndex;
	private String stateValue;
	
	public int getStateIndex() {
		return stateIndex;
	}

	public String getStateValue() {
		return stateValue;
	}
	
	public String getGraphName() {
		return graphName;
	}
	
	public ExperimentJUNGStateVertex(int index, String value, String graph) {
		stateIndex 	= index;
		stateValue	= value;
		graphName 	= graph;
	}
	
	public String toString() {
		return "<html><center><small>" + String.valueOf(stateIndex) + "<p style=\"margin-top: -5;color:#555;\">" + stateValue + "</p></small></center></html>";
	}
}
