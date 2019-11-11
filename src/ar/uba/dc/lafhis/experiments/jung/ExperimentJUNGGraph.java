package ar.uba.dc.lafhis.experiments.jung;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ar.uba.dc.lafhis.experiments.exchange.JSONAwareMultiGraph;
import ar.uba.dc.lafhis.henos.report.ReportAutomaton;
import ar.uba.dc.lafhis.henos.report.ReportContext;
import ar.uba.dc.lafhis.henos.report.ReportTransition;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.picking.PickedState;

/**
 * Graph structure for a directed graph of type LTS
 * @author Cédric Delforge
 */
@SuppressWarnings("serial")
public class ExperimentJUNGGraph extends DirectedSparseMultigraph<ExperimentJUNGStateVertex, ExperimentJUNGTransitionEdge> {
	private final String name;
	private boolean mixed; //a graph is mixed when it holds multiple single graphs besides itself
	private Set<ExperimentJUNGGraph> innerGraphs;
	private Map<ExperimentJUNGGraph,List<Set<ExperimentJUNGStateVertex>>> SCCs;
	
	public class ExperimentJUNGNavigator {
		private Set<ExperimentJUNGStateVertex> reached;
		private Set<ExperimentJUNGStateVertex> current;
		
		public ExperimentJUNGNavigator() {
			this(getInitials());
		}
		public ExperimentJUNGNavigator(final ExperimentJUNGStateVertex v) {
			this(new HashSet<ExperimentJUNGStateVertex>(){{add(v);}});
		}
		public ExperimentJUNGNavigator(Set<ExperimentJUNGStateVertex> vs) {
			reached = new HashSet<ExperimentJUNGStateVertex>();
			current = vs;
		}
		/*
		 * Adds the destination state to the set of current states, and removes those that can reach it
		 */
		public void navigateTo(ExperimentJUNGStateVertex to) {
			Set<ExperimentJUNGStateVertex> froms = new HashSet<ExperimentJUNGStateVertex>(getPredecessors(to));
			froms.retainAll(current);
			for (ExperimentJUNGStateVertex from: froms) {
				if (current.contains(from) && getSuccessors(from).contains(to)) {
					current.remove(from);
					current.add(to);
					
					reached.add(from);
				}
			}
		}
		public Set<ExperimentJUNGStateVertex> getCurrent() {
			return Collections.unmodifiableSet(current);
		}
		public Set<ExperimentJUNGStateVertex> getNext(ExperimentJUNGStateVertex v) {
			return new HashSet<ExperimentJUNGStateVertex>(getSuccessors(v));
		}
		public Set<ExperimentJUNGStateVertex> getPrevious(ExperimentJUNGStateVertex v) {
			return new HashSet<ExperimentJUNGStateVertex>(getPredecessors(v));
		}
		public Set<ExperimentJUNGStateVertex> getNext() {
			Set<ExperimentJUNGStateVertex> r = new HashSet<ExperimentJUNGStateVertex>();
			for (ExperimentJUNGStateVertex v: current) {
				r.addAll(getSuccessors(v));
			}
			return r;
		}
		public Set<ExperimentJUNGStateVertex> getPrevious() {
			Set<ExperimentJUNGStateVertex> r = new HashSet<ExperimentJUNGStateVertex>();
			for (ExperimentJUNGStateVertex v: current) {
				r.addAll(getPredecessors(v));
			}
			return r;
		}
		public Set<ExperimentJUNGTransitionEdge> getPath(ExperimentJUNGStateVertex v) {
			return new HashSet<ExperimentJUNGTransitionEdge>(getOutEdges(v));
		}
		public Set<ExperimentJUNGTransitionEdge> getPath() {
			Set<ExperimentJUNGTransitionEdge> r = new HashSet<ExperimentJUNGTransitionEdge>();
			for (ExperimentJUNGStateVertex v: current) {
				r.addAll(getOutEdges(v));
			}
			return r;		
		}
		public Set<ExperimentJUNGStateVertex> getReachable() {
			Set<ExperimentJUNGStateVertex> r = new HashSet<ExperimentJUNGStateVertex>();
			for (ExperimentJUNGStateVertex v: current) {
				r.addAll(getReachable(v));
			}
			return r;
		}
		public Set<ExperimentJUNGStateVertex> getReaching() {
			Set<ExperimentJUNGStateVertex> r = new HashSet<ExperimentJUNGStateVertex>();
			for (ExperimentJUNGStateVertex v: current) {
				r.addAll(getReaching(v));
			}
			return r;
		}
		public Set<ExperimentJUNGStateVertex> getReachable(ExperimentJUNGStateVertex v) {
			return new HashSet<ExperimentJUNGStateVertex>(getReachableStates(v));
		}
		public Set<ExperimentJUNGStateVertex> getReaching(ExperimentJUNGStateVertex v) {
			return new HashSet<ExperimentJUNGStateVertex>(getReachingStates(v));
		}
		public Set<ExperimentJUNGStateVertex> getReached() {
			return Collections.unmodifiableSet(reached);
		}
	}
	
	public String getName() {
		return name;
	}
	
	public ExperimentJUNGNavigator getNavigator() {
		return new ExperimentJUNGNavigator();
	}
	public ExperimentJUNGNavigator getNavigator(ExperimentJUNGStateVertex v) {
		return new ExperimentJUNGNavigator(v);
	}
	public ExperimentJUNGNavigator getNavigator(Set<ExperimentJUNGStateVertex> vs) {
		return new ExperimentJUNGNavigator(vs);
	}
	
	public ExperimentJUNGGraph() {
		this("aggregate");
	}
	public ExperimentJUNGGraph(String n) {
		super();
		name = n;
		mixed = true;
		innerGraphs = new HashSet<ExperimentJUNGGraph>();
		SCCs = new HashMap<ExperimentJUNGGraph,List<Set<ExperimentJUNGStateVertex>>>();
	}
	

	
	public ExperimentJUNGGraph(ReportAutomaton automaton) {
		this(automaton.getName());
		mixed = false;
		
		
		ExperimentJUNGStateVertex currentNode				= null;
		ExperimentJUNGTransitionEdge currentEdge			= null;
		
		List<ExperimentJUNGGameFluent> currentFluents		= null;
		
		Map<Integer, ExperimentJUNGStateVertex> game2Graph 	= new HashMap<Integer, ExperimentJUNGStateVertex>(automaton.getTransitions().size());
		ReportContext ctx			= automaton.getContext();
		List<String> fluentNames	= ctx.getfluents();
		List<String> livenessNames	= ctx.getLivenessNames();
		//add nodes
		List<Integer> processedStates	= new ArrayList<Integer>();
		int i;
		int state;
		for(ReportTransition transition: automaton.getTransitions()){
			state	= transition.getFromState();
			int count	= 0;
			
			while(count < 2) {
				if(!processedStates.contains(state)) {
					currentFluents	= new ArrayList<ExperimentJUNGGameFluent>();
					for(i = 0; i < fluentNames.size(); i++) {
						if(automaton.getFluentValuations().size() > (state + 1) && automaton.getFluentValuations().get(state).size() > (i + 1))
							currentFluents.add(new ExperimentJUNGGameFluent(fluentNames.get(i), automaton.getFluentValuations().get(state).get(i)));
					}
					for(i = 0; i < livenessNames.size(); i++) {
						if(automaton.getLivenessValuations().size() > (state + 1) && automaton.getLivenessValuations().get(state).size() > (i + 1))
							currentFluents.add(new ExperimentJUNGGameFluent(livenessNames.get(i), automaton.getLivenessValuations().get(state).get(i)));
					}
					String valuationsString = null;
					for(ExperimentJUNGGameFluent fluent: currentFluents){
						
						if(valuationsString == null)
							valuationsString = "";
						valuationsString += fluent.getValue()? "1" : "0";// fluent.toJSONString();
					}
					if(valuationsString != null)
						valuationsString += "";
					else
						valuationsString = "";
					
					currentNode 	= new ExperimentJUNGStateVertex(state, valuationsString
							, automaton.getName());
					
					addVertex(currentNode);
					game2Graph.put(state, currentNode);
					if(count == 0) {
						count++;
						state	= transition.getToState();
					}else if(count == 1) {
						count++;
					}
				}
			}
		}
		
		String label;
		//add edges
		processedStates	= new ArrayList<Integer>();
		boolean firstLabel;
		List<String> labels;
		String currentLabel;
		
		for(ReportTransition transition1: automaton.getTransitions()){
			state	= transition1.getFromState();
			if(processedStates.contains(state))
				continue;
			
			label = (transition1.getLabels().size() > 1) ? "<" : "";
			firstLabel	= true;
			labels	= new ArrayList<String>();
			for(int j : transition1.getLabels()) {
				if(firstLabel) {firstLabel = false;} else {label += ",";}
				currentLabel	= automaton.getLabel(j);
				label 			+= currentLabel;
				labels.add(currentLabel);
			}
			label += (transition1.getLabels().size() > 1) ? ">" : "";
			currentEdge	= new ExperimentJUNGTransitionEdge(label, state, transition1.getToState());
			addEdge(currentEdge, game2Graph.get(state), game2Graph.get(transition1.getToState()));
		}
		
		List<ExperimentJUNGStateVertex> verticesToRemove	= new ArrayList<ExperimentJUNGStateVertex>();
		for(ExperimentJUNGStateVertex v : this.getVertices()) {
			if(getInEdges(v).size() == 0 && getOutEdges(v).size() == 0)
				verticesToRemove.add(v);
		}
		
		for(ExperimentJUNGStateVertex v : verticesToRemove)
			removeVertex(v);
		
	}

	/*
	 * Creates a new graph from a set of picked vertices and an aggregated graph holding them
	 */
	public ExperimentJUNGGraph (PickedState<ExperimentJUNGStateVertex> ps, ExperimentJUNGGraph aggregate) {
		this("picked");
		
		for(ExperimentJUNGStateVertex vertex : ps.getPicked()) {
			addVertex(vertex);
			Collection<ExperimentJUNGTransitionEdge> incidentEdges = aggregate.getOutEdges(vertex);
			if (incidentEdges != null) {
				for(ExperimentJUNGTransitionEdge edge : incidentEdges) {
					Pair<ExperimentJUNGStateVertex> endpoints = aggregate.getEndpoints(edge);
					if(ps.getPicked().containsAll(endpoints)) {
						addEdge(edge, endpoints.getFirst(), endpoints.getSecond());
					}
				}
			}
		}
	}
	
	/*
	 * Gets the states of the LTS containing the state "v"
	 */
	public Set<ExperimentJUNGStateVertex> getAutomatonFromState(ExperimentJUNGStateVertex v) {
		HashSet<ExperimentJUNGStateVertex> vertices = new HashSet<ExperimentJUNGStateVertex>();
		ExperimentJUNGGraph lts = getExperimentJUNGGraphFromState(v);
		
		if (lts != null) {
			for (ExperimentJUNGStateVertex s: lts.getVertices()) {
				vertices.add(s);
			}
		}
		return vertices;
	}
	
	/*
	 * Gets the LTS containing the state "v"
	 */
	private ExperimentJUNGGraph getExperimentJUNGGraphFromState(ExperimentJUNGStateVertex v) {
		ExperimentJUNGGraph lts = null;
		if (mixed) {
			for (ExperimentJUNGGraph g: innerGraphs) {
				if (g.containsVertex(v)) {
					lts = g;
				}
			}
		} else {
			if (containsVertex(v)) {
				lts = this;
			}
		}
		return lts;
	}
	
	/*
	 * Gets the SCC containing the state "v"
	 */
	public Set<ExperimentJUNGStateVertex> getSCCFromState(ExperimentJUNGStateVertex v) {
		ExperimentJUNGGraph lts = getExperimentJUNGGraphFromState(v);
		if (lts != null) {
			List<Set<ExperimentJUNGStateVertex>> scc = SCCs.get(lts);
			
			if (scc == null) { //lazy intialization
				computeSCC(lts);
				scc = SCCs.get(lts);
			}
	
			for (Set<ExperimentJUNGStateVertex> s: scc) {
				if (s.contains(v))
					return s;
			}
		}
		return new HashSet<ExperimentJUNGStateVertex>();
	}
	
	/*
	 * Incrementally numbers each SCC of a graph,
	 * then returns the number of the SCC the "v" state belongs to
	 */
	public int numberSCC(ExperimentJUNGStateVertex v) {
		ExperimentJUNGGraph lts = getExperimentJUNGGraphFromState(v);
		List<Set<ExperimentJUNGStateVertex>> scc = SCCs.get(lts);
		
		if (scc == null) { //lazy intialization
			computeSCC(lts);
			scc = SCCs.get(lts);
		}
		
		for (int set = 0; set < scc.size(); set++) {
			if (scc.get(set).contains(v))
				return set+1;
		}
		return 0;
	}
	
	
	private void computeSCC(ExperimentJUNGGraph lts) {
		ExperimentJUNGStronglyConnectedComponentClusterer<ExperimentJUNGStateVertex,ExperimentJUNGTransitionEdge> clusterer = new ExperimentJUNGStronglyConnectedComponentClusterer<ExperimentJUNGStateVertex,ExperimentJUNGTransitionEdge>();
		
		SCCs.put(lts,clusterer.transform(lts));
	}
	
	/*
	 * Returns the transition corresponding to the given label and origin and dest states
	 */
	public ExperimentJUNGTransitionEdge getTransitionFromLabel(String label, int origin, int dest) {
		for (ExperimentJUNGTransitionEdge e: getEdges()) {
			if (e.hasLabel(label) && e.getOriginState() == origin && e.getDestinationState() == dest) {
				return e;
			}
		}
		return null;
	}
	
	/*
	 * Returns state with given number
	 */
	public ExperimentJUNGStateVertex getStateFromNumber(int n) {
		for (ExperimentJUNGStateVertex v : getVertices()) {
			if (v.getStateIndex() == n) return v;
		}
		return null;
	}
	
	/*
	 * Merges this with g
	 */
	public void mergeWith(ExperimentJUNGGraph g) {
		innerGraphs.add(g);
		final Iterator<ExperimentJUNGStateVertex> newVertices = g.getVertices().iterator();
		while (newVertices.hasNext()) {
			addVertex(newVertices.next());
		}
		
		final Iterator<ExperimentJUNGTransitionEdge> newEdges = g.getEdges().iterator();
		while (newEdges.hasNext()) {
			final ExperimentJUNGTransitionEdge e = newEdges.next();
			addEdge(e, g.getSource(e), g.getDest(e));
		
		}
	}
	
	/*
	 * Removes g from this
	 */
	public void separateFrom(ExperimentJUNGGraph g) {
		innerGraphs.remove(g);
		final Iterator<ExperimentJUNGStateVertex> newVertices = g.getVertices().iterator();
		while (newVertices.hasNext()) {
			removeVertex(newVertices.next());
		}
		
		final Iterator<ExperimentJUNGTransitionEdge> newEdges = g.getEdges().iterator();
		while (newEdges.hasNext()) {
			final ExperimentJUNGTransitionEdge e = newEdges.next();
			removeEdge(e);
		}
	}
	
	/*
	 * Returns all the states labeled "0"
	 */
	public Set<ExperimentJUNGStateVertex> getInitials() {
		Set<ExperimentJUNGStateVertex> initials = new HashSet<ExperimentJUNGStateVertex>();
		if (mixed) {
			for (ExperimentJUNGGraph g: innerGraphs) {
				for (ExperimentJUNGStateVertex v: g.getVertices()) {
					if (v.getStateIndex() == 0) {
						initials.add(v);
						break;
					}
				}
			}
			return initials;
		} else {
			for (ExperimentJUNGStateVertex v: getVertices()) {
				if (v.getStateIndex() == 0) {
					initials.add(v);
					break;
				}					
			}
			return initials;
		}
	}
	
	/*
	 * Returns a set of all the states reachable from v
	 */
	public Set<ExperimentJUNGStateVertex> getReachableStates(ExperimentJUNGStateVertex v) {
		Set<ExperimentJUNGStateVertex> reachables = new HashSet<ExperimentJUNGStateVertex>();
    	LinkedList<ExperimentJUNGStateVertex> queue = new LinkedList<ExperimentJUNGStateVertex>();
    	queue.add(v);
    	
    	while (!queue.isEmpty()) {
    		ExperimentJUNGStateVertex s = queue.removeLast();
    		
    		for (ExperimentJUNGStateVertex successor: getSuccessors(s)) {
    			if (!reachables.contains(successor)) {
    				reachables.add(successor);
    				queue.add(successor);
    			}
    		}
    	}
    	
    	return reachables;
	}
	
	/*
	 * Returns a set of all the states able to reach v
	 */
	public Set<ExperimentJUNGStateVertex> getReachingStates(ExperimentJUNGStateVertex v) {
		Set<ExperimentJUNGStateVertex> reachings = new HashSet<ExperimentJUNGStateVertex>();
    	LinkedList<ExperimentJUNGStateVertex> queue = new LinkedList<ExperimentJUNGStateVertex>();
    	queue.add(v);
    	
    	while (!queue.isEmpty()) {
    		ExperimentJUNGStateVertex s = queue.removeLast();
    		
    		for (ExperimentJUNGStateVertex predecessor: getPredecessors(s)) {
    			if (!reachings.contains(predecessor)) {
    				reachings.add(predecessor);
    				queue.add(predecessor);
    			}
    		}
    	}
    	
    	return reachings;
	}
}
