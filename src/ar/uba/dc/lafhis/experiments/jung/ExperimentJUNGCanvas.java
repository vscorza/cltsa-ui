package ar.uba.dc.lafhis.experiments.jung;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

import edu.uci.ics.jung.algorithms.layout.AggregateLayout;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.layout.ObservableCachingLayout;
import edu.uci.ics.jung.visualization.picking.PickedState;

/**
 * Canvas for JUNG graph manipulation
 * @author Cédric Delforge
 */
@SuppressWarnings("serial")
public class ExperimentJUNGCanvas extends JPanel {
    public static enum EnumLayout {
    	KamadaKawai("Force-directed KK"), 
    	FruchtermanReingold("Force-directed FR"), 
    	ISOM("Space-filling ISOM"), 
    	Circle("Circle"), 
    	TreeLikeLTS("Top-down tree"), 
    	RadialLTS("Radial tree"),
    	Aggregate("Aggregate");
    	
    	private String name;
    	
    	private EnumLayout(String name) {
    		this.name = name;
    	}

    	@Override
    	public String toString() {
    		return name;
    	};
    }
	public static enum EnumMode {Edit, Activate};
	public static enum LayoutOptions {KK_length_factor, KK_distance, KK_max_iterations,
										FR_attraction, FR_repulsion, FR_max_iterations,
										Tree_distX, Tree_distY, Radial_distX, Radial_distY};
    
    private ExperimentJUNGViewer view;
    private GraphZoomScrollPane scrollView; //the scrollview holding the view
    private ArrayList<ExperimentJUNGGraph> graphs;
    private Map<ExperimentJUNGGraph,EnumLayout> enumLayouts;
    private Map<ExperimentJUNGStateVertex,Point2D> savedPositions;
    private Set<ExperimentJUNGTransitionEdge> selectedTrans;

	public void setSelectedStates(Set<ExperimentJUNGStateVertex> selectedStates) {
		this.selectedStates = selectedStates;
	}

	private Set<ExperimentJUNGStateVertex> selectedStates;
    
    private boolean singleMode = true;
    private boolean colorSCC = false;
    
    public static float edgeCurve = 20.f;
    public static double KK_length_factor = 0.9;
    public static double KK_distance = 0.5;
    public static int KK_max_iterations = 500;
    public static double FR_attraction = 0.75;
    public static double FR_repulsion = 0.75;
    public static int FR_max_iterations = 500;
    public static int Tree_distX = 100;
    public static int Tree_distY = 100;
    public static int Radial_distX = 100;
    public static int Radial_distY = 100;
    
    public ExperimentJUNGCanvas() {
    	super();
		cleanUp();
		addComponentListener(new ComponentAdapter() {
			public final void componentResized(ComponentEvent e) {
				if (view != null) {
					view.setPreferredSize(getSize());
					view.setLocation(0, 0);
					remove();
					display();
					//view.scaleToLayout(new CrossoverScalingControl());
				}
	        }			
		});
    }
    
	private void cleanUp() {
		view = null;
		scrollView = null;
		savedPositions = null;
		graphs = new ArrayList<ExperimentJUNGGraph>();
		enumLayouts = new HashMap<ExperimentJUNGGraph, EnumLayout>();
		selectedTrans = new HashSet<ExperimentJUNGTransitionEdge>();
		selectedStates = new HashSet<ExperimentJUNGStateVertex>();
	}
	
//-----------------------------------------------------------------------------
// Parameter setters
//-----------------------------------------------------------------------------
	public void setMode(boolean mode) {
		if (mode != singleMode)
			clear();
		singleMode = mode;	
	}
	public boolean shouldColorSCC() {
		return colorSCC;
	}
	public void colorSCC(boolean color) {
		colorSCC = color;
		if (view != null) {
			view.refresh();
		}
	}
	public float getCurve() {
		return edgeCurve;
	}
	public void setCurve(float curve) {
		edgeCurve = curve;
		if (view != null) {
			view.refresh();
		}
	}
	public void next() {
		if (view != null) {
			view.selectNext();
		}
	}
	public void reachable() {
		if (view != null) {
			view.selectReachable();
		}
	}
	public void previous() {
		if (view != null) {
			view.selectPrevious();
		}
	}
	public void reaching() {
		if (view != null) {
			view.selectReaching();
		}
	}
	public void zoomIn() {
		if (view != null) {
			final ScalingControl scaler = new CrossoverScalingControl();
			scaler.scale(view, 1.1f, view.getCenter());
		}
	}
	public void zoomOut() {
		if (view != null) {
			final ScalingControl scaler = new CrossoverScalingControl();
			scaler.scale(view, 1/1.1f, view.getCenter());
		}
	}
	public void setInteraction(EnumMode m) {
		if (view != null) {
			view.setInteraction(m);
		}
	}
	public Set<ExperimentJUNGTransitionEdge> getSelectedTransitions() {
		return Collections.unmodifiableSet(selectedTrans);
	}
	public Set<ExperimentJUNGStateVertex> getSelectedVertices() {
		return Collections.unmodifiableSet(selectedStates);
	}
//-----------------------------------------------------------------------------
// Interactions
//-----------------------------------------------------------------------------
	/*
	 * Draws graph on the canvas under the given layout
	 */
	public void draw(final ExperimentJUNGGraph graph, EnumLayout layout)
	{
		Layout<ExperimentJUNGStateVertex,ExperimentJUNGTransitionEdge> layoutToDisplay;
		
		if (singleMode || graphs.size() == 0)
		{
			graphs.clear();
			enumLayouts.clear();
			
			graphs.add(new ExperimentJUNGGraph());
			enumLayouts.put(graphs.get(0), EnumLayout.Aggregate);
			
			if (!singleMode) graphs.get(0).mergeWith(graph);
			
			graphs.add(graph);
			enumLayouts.put(graph, layout);
			
			layoutToDisplay = getLayout(graph,EnumLayout.Aggregate,this.getSize());
		}
		else
		{
			graphs.get(0).mergeWith(graph);
			graphs.add(graph);
			enumLayouts.put(graph, layout);
			
			layoutToDisplay = getLayout(graphs.get(0),EnumLayout.Aggregate,this.getSize());
		}
		
		remove();
		renew(layoutToDisplay);
		display();
	}
	
	private ExperimentJUNGGraph aggregate() {
		return graphs.get(singleMode ? 1 : 0);
	}
	
	/*
	 * Adds a layout for a subgraph defined as the set of picked states
	 */
	public void addLayout(final PickedState<ExperimentJUNGStateVertex> picked, EnumLayout layout) {
		ExperimentJUNGGraph graph = new ExperimentJUNGGraph(picked,aggregate());
		CenteredArea ca = new CenteredArea(picked);
		Point2D center = ca.getCenter();
		Dimension dimension = ca.getDimension();
		
		Layout<ExperimentJUNGStateVertex,ExperimentJUNGTransitionEdge> graphLayout = getLayout(graph, layout, dimension);
		AggregateLayout<ExperimentJUNGStateVertex,ExperimentJUNGTransitionEdge> oldAggregate;
		if ((view.getGraphLayout() instanceof ObservableCachingLayout
				&& ((ObservableCachingLayout<ExperimentJUNGStateVertex,ExperimentJUNGTransitionEdge>)view.getGraphLayout()).getDelegate() instanceof AggregateLayout)
				|| (view.getGraphLayout() instanceof AggregateLayout)) {
			if (view.getGraphLayout() instanceof AggregateLayout)
				oldAggregate = ((AggregateLayout<ExperimentJUNGStateVertex,ExperimentJUNGTransitionEdge>)view.getGraphLayout());
			else
				oldAggregate = ((AggregateLayout<ExperimentJUNGStateVertex,ExperimentJUNGTransitionEdge>)((ObservableCachingLayout<ExperimentJUNGStateVertex,ExperimentJUNGTransitionEdge>)view.getGraphLayout()).getDelegate());
			
			AggregateLayout<ExperimentJUNGStateVertex,ExperimentJUNGTransitionEdge> newAggregate = (AggregateLayout<ExperimentJUNGStateVertex,ExperimentJUNGTransitionEdge>) getLayout((ExperimentJUNGGraph) oldAggregate.getGraph(), EnumLayout.Aggregate, oldAggregate.getSize());
			newAggregate.setInitializer(oldAggregate);
			newAggregate.put(graphLayout, center);
			
			
			view.setGraphLayout(newAggregate);
			view.repaint();
		}
	}
	
	public void cluster(final PickedState<ExperimentJUNGStateVertex> picked) {
		CenteredArea ca = new CenteredArea(picked);
		Point2D center = ca.getCenter();

		AggregateLayout<ExperimentJUNGStateVertex,ExperimentJUNGTransitionEdge> oldAggregate;
		
		if ((view.getGraphLayout() instanceof ObservableCachingLayout
				&& ((ObservableCachingLayout<ExperimentJUNGStateVertex,ExperimentJUNGTransitionEdge>)view.getGraphLayout()).getDelegate() instanceof AggregateLayout)
				|| (view.getGraphLayout() instanceof AggregateLayout)) {
			if (view.getGraphLayout() instanceof AggregateLayout)
				oldAggregate = ((AggregateLayout<ExperimentJUNGStateVertex,ExperimentJUNGTransitionEdge>)view.getGraphLayout());
			else
				oldAggregate = ((AggregateLayout<ExperimentJUNGStateVertex,ExperimentJUNGTransitionEdge>)((ObservableCachingLayout<ExperimentJUNGStateVertex,ExperimentJUNGTransitionEdge>)view.getGraphLayout()).getDelegate());
			
			ExperimentJUNGGraph inGraph = (ExperimentJUNGGraph) oldAggregate.getGraph();
			ExperimentJUNGGraph clusterGraph = new ExperimentJUNGGraph(picked,aggregate());

            inGraph.separateFrom(clusterGraph);
            
            ExperimentJUNGStateVertex cluster = makeVertex(clusterGraph);
            
            inGraph.addVertex(cluster);
			oldAggregate.setGraph(inGraph);
			oldAggregate.setLocation(cluster, center);
			view.getPickedEdgeState().clear();
			view.repaint();
		}
	}
	
	/*
	 * Refreshes all the graphs visualized with the given layout
	 */
	public void refresh(EnumLayout layout) {
		Layout<ExperimentJUNGStateVertex,ExperimentJUNGTransitionEdge> layoutToDisplay;
		
		if (graphs.size() > 0) {
			for (ExperimentJUNGGraph g : enumLayouts.keySet()) {
				if (enumLayouts.get(g) != EnumLayout.Aggregate) {
					enumLayouts.put(g, layout);
				}
			}

			layoutToDisplay = getLayout(aggregate(),EnumLayout.Aggregate,this.getSize());
			
			remove();
			renew(layoutToDisplay);
			display();	
		}
	}
	
	/*
	 * Stops the rendering
	 */
	public void stop() {
		if (view != null) {
			view.getModel().getRelaxer().stop();
		}
	}
	
	/*
	 * Clears a graph from the canvas
	 */
	public void clear(final ExperimentJUNGGraph graph) {
		if (singleMode || graphs.size() <= 2) {
			clear();
		} else {
			graphs.remove(graph);
			enumLayouts.remove(graph);
			aggregate().separateFrom(graph);

			remove();
			renew(getLayout(aggregate(),EnumLayout.Aggregate,this.getSize()));
			display();
		}
	}
	
	/*
	 * Clears the canvas
	 */
	public void clear() {
		remove();
		cleanUp();
		display();
	}
	
    public void savePositions() {
    	if (graphs != null && graphs.size() > 0 && view != null && view.getGraphLayout() != null) {
    		savedPositions = new HashMap<ExperimentJUNGStateVertex,Point2D>();
    		
    		for (ExperimentJUNGStateVertex v: aggregate().getVertices()) {
    			savedPositions.put(v, view.getGraphLayout().transform(v));
    		}
    	}
    }
    
    public void loadPositions() {
    	if (view != null && savedPositions != null) {
    		for (ExperimentJUNGStateVertex v: savedPositions.keySet()) {
    			view.getGraphLayout().setLocation(v, savedPositions.get(v));
    		}
    	}
    }
    
    public void select(Set<ExperimentJUNGTransitionEdge> trans, Set<ExperimentJUNGStateVertex> states) {
    	selectedTrans = trans;
    	selectedStates = states;
    	if (view != null) {
    		view.refresh();
    	}
    }
//-----------------------------------------------------------------------------
// Utils
//-----------------------------------------------------------------------------	
	public Dimension getSize() {
		return new Dimension(super.getSize().width-20,super.getSize().height-20);
	}

	private void display() {
		if (view != null) {
			scrollView = new GraphZoomScrollPane(view);
	        add(scrollView);
		}
		setVisible(true);
	}
	
	private void renew(Layout<ExperimentJUNGStateVertex,ExperimentJUNGTransitionEdge> l) {			
		//how many squares will be needed to display all the graphs, x horizontally and y vertically
		int x = (int) Math.ceil(Math.sqrt(graphs.size()-1));
		int y = x == 0 ? 0 : (int) Math.ceil((double)(graphs.size()-1)/x);
		
		//allocated width and height of each square in the total canvas size
		int width = x == 0 ? 0 : this.getSize().width/x;
		int height = y == 0 ? 0 : this.getSize().height/y;
		
		//size of each square
		Dimension dimension = new Dimension(width,height);
		
		int x_i= 0, y_i = 0;

		for (int i = 1; i < graphs.size(); ++i) {
			final Point2D center = new Point2D.Double();
			//centered on the width and height of each square
			center.setLocation(width*x_i+width/2, height*y_i+height/2);
			
			//make a new sublayout of the size of a square and centered on one, add it to the aggregate layout
			((AggregateLayout<ExperimentJUNGStateVertex,ExperimentJUNGTransitionEdge>) l).put(getLayout(graphs.get(i),enumLayouts.get(graphs.get(i)),dimension), center);
			
			x_i += 1;
			if (x_i == x) {
				x_i = 0;
				y_i += 1;
			}
		}
		
		view = new ExperimentJUNGViewer(l,this.getSize(),this);
	}
	
	private void remove() {
		if (view != null) {
			this.remove(scrollView);
		}
		setVisible(false);
	}
	
	private Layout<ExperimentJUNGStateVertex,ExperimentJUNGTransitionEdge> getLayout(final ExperimentJUNGGraph graph, final EnumLayout layout, Dimension dms) {
	    	switch (layout) {
	    	case KamadaKawai: {
	    		 final KKLayout<ExperimentJUNGStateVertex,ExperimentJUNGTransitionEdge> out = new KKLayout<ExperimentJUNGStateVertex,ExperimentJUNGTransitionEdge>(graph);
	    		 out.setSize(dms);
	    		 out.setLengthFactor(KK_length_factor);
	    		 out.setDisconnectedDistanceMultiplier(KK_distance);
	    		 out.setMaxIterations(KK_max_iterations);
	    		 return out;
	    	}
	    	case FruchtermanReingold: {
	    		final FRLayout<ExperimentJUNGStateVertex,ExperimentJUNGTransitionEdge> out = new FRLayout<ExperimentJUNGStateVertex,ExperimentJUNGTransitionEdge>(graph);
	    		 out.setSize(dms);
	    		 out.setAttractionMultiplier(FR_attraction);
	    		 out.setRepulsionMultiplier(FR_repulsion);
	    		 out.setMaxIterations(FR_max_iterations);
	    		 return out;
	    	}
	    	case Circle: {
	    		 final CircleLayout<ExperimentJUNGStateVertex,ExperimentJUNGTransitionEdge> out = new CircleLayout<ExperimentJUNGStateVertex,ExperimentJUNGTransitionEdge>(graph);
	    		 out.setSize(dms);
	    		 return out;
	    	}
	    	case ISOM: {
	    		 final ISOMLayout<ExperimentJUNGStateVertex,ExperimentJUNGTransitionEdge> out = new ISOMLayout<ExperimentJUNGStateVertex,ExperimentJUNGTransitionEdge>(graph);
	    		 out.setSize(dms);
	    		 return out;
	    	}
	    	case TreeLikeLTS: {
	    		final ExperimentJUNGTreeLikeLayout out = new ExperimentJUNGTreeLikeLayout(graph);
	    		out.setSize(dms);
    			return out;
	    	}
	    	case RadialLTS: {
	    		final ExperimentJUNGRadialLayout out = new ExperimentJUNGRadialLayout(graph);
	    		out.setSize(dms);
    			return out;
	    	}
	    	case Aggregate: {
	    		 final StaticLayout<ExperimentJUNGStateVertex,ExperimentJUNGTransitionEdge> out = new StaticLayout<ExperimentJUNGStateVertex,ExperimentJUNGTransitionEdge>(graph);
	    		 out.setSize(dms);
	    		return new AggregateLayout<ExperimentJUNGStateVertex,ExperimentJUNGTransitionEdge>(out);
	    	}
	    	default: {
	    		 final KKLayout<ExperimentJUNGStateVertex,ExperimentJUNGTransitionEdge> out = new KKLayout<ExperimentJUNGStateVertex,ExperimentJUNGTransitionEdge>(graph);
	    		 out.setSize(dms);
	    		 return out;
	    	}
	    	}
    }
    
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#print(java.awt.Graphics)
	 */
    public void print(Graphics g) {
    	//view.setBackground(new Color(255,255,255,0));
    	if (view != null)
    		view.print(g);  	
    }
    
    /*
     * Returns the buffered image of the current viewer
     */
    public BufferedImage getImage() { 
    	if (view != null)
    		return view.getImage();
    	else
    		return null;
    }
    
    /*
     * Turns a clustered Graph into a vertex, incomplete
     */
	private ExperimentJUNGStateVertex makeVertex(ExperimentJUNGGraph g) {
		HashSet<String> graphNames = new HashSet<String>();
		HashSet<String> statesValues = new HashSet<String>();

		boolean isFirstState	= true;
		int firstState			= -1;
		
		for (ExperimentJUNGStateVertex j : g.getVertices()) {
			if(isFirstState) {
				isFirstState	 = false;
				firstState		= j.getStateIndex();
			}
			graphNames.add(j.getGraphName());
			statesValues.add(j.getStateValue());
		}
		
		String name = "";
		String value = "";
		boolean firstValue = true;
		for (String s : graphNames) {
			name += s + " ";
		}
		for (String v : statesValues) {
			if(firstValue) {
				firstValue	= false;
			}else {
				value += ",";
			}
			value += v;
		}
		return new ExperimentJUNGStateVertex(firstState, value,name.trim());
	}
    
	/*
	 * Creates the smallest area such that it contains all the vertices in ps
	 */
    private class CenteredArea {
    	Dimension dimension;
    	Point2D center;
    	
    	@SuppressWarnings("unused")
		public CenteredArea(Dimension d, Point2D c) {
    		dimension = d;
    		center = c;
    	}
    	public CenteredArea(PickedState<ExperimentJUNGStateVertex> ps) {
    		center = new Point2D.Double();
    		double x = 0;
    		double y = 0;
    		double min_x = -1;
    		double min_y = -1;
    		double max_x = -1;
    		double max_y = -1;
    		for(ExperimentJUNGStateVertex vertex : ps.getPicked()) {
    			Point2D p = view.getGraphLayout().transform(vertex);
    			x += p.getX();
    			y += p.getY();
    			if (min_x  == -1 || min_x > p.getX())
    				min_x = p.getX();
    			if (min_y  == -1 || min_y > p.getY())
    				min_y = p.getY();
    			if (max_x  == -1 || max_x < p.getX())
    				max_x = p.getX();
    			if (max_y  == -1 || max_y < p.getY())
    				max_y = p.getY();	
    		}
    		x /= ps.getPicked().size();
    		y /= ps.getPicked().size();
    		center.setLocation(x,y);
    		dimension = new Dimension((int)(max_x - min_x),(int)(max_y - min_y));
    	}
    	public Dimension getDimension() {
    		return dimension;
    	}
    	public Point2D getCenter() {
    		return center;
    	}
    }
}
