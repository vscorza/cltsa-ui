package ar.uba.dc.lafhis.experiments.jung;

import edu.uci.ics.jung.graph.Graph;

/**
 * RadialGraphLayout extended for LTS graphs
 * @author Cédric Delforge
 * @author Charles Pecheur - fixes, support resizing
 */
public class ExperimentJUNGRadialLayout extends ExperimentJUNGRadialGraphLayout<ExperimentJUNGStateVertex,ExperimentJUNGTransitionEdge> {
	public ExperimentJUNGRadialLayout(Graph<ExperimentJUNGStateVertex,ExperimentJUNGTransitionEdge> g) {
		super(g);
    }
    
    /*
     * (non-Javadoc)
     * @see jung.TreeLikeGraphLayout#setRoot()
     * The root is chosen as the "0" state instead of at random
     */
    @Override
    protected void setRoot() {
    	if (graph.getVertices().size() > 0) {
    		for (ExperimentJUNGStateVertex v: graph.getVertices()) {
    			if (v.toString().equals("0")) {
    				this.root = v; //initial LTS state
    				return;
    			}
    		}
    		this.root = graph.getVertices().iterator().next(); //random vertex if no initial state found
    		return;
    	}
    	this.root = null; //empty graph
    }
}
