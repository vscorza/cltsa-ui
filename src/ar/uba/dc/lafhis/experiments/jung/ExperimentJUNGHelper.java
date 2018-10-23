package ar.uba.dc.lafhis.experiments.jung;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ar.uba.dc.lafhis.experiments.exchange.JSONAwareMultiGraph;
import ar.uba.dc.lafhis.henos.report.ReportAutomaton;
import ar.uba.dc.lafhis.henos.report.ReportContext;
import ar.uba.dc.lafhis.henos.report.ReportTransition;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;

public class ExperimentJUNGHelper {

	private static ExperimentJUNGHelper instance;

	public static ExperimentJUNGHelper getInstance() {
		if (instance == null)
			instance = new ExperimentJUNGHelper();
		return instance;
	}
	
	protected String getLabel(ReportAutomaton automaton, int labelLocalIndex) {
		return automaton.getContext().getAlphabet().getSignals().get(automaton.getLocalAlphabet().get(labelLocalIndex)).getName();
	}
	
	public JSONAwareMultiGraph<ExperimentJUNGGameNodeValue<Integer>, ExperimentJUNGGameEdgeValue<String,Integer,String>> getReportAutomatonGraph(ReportAutomaton automaton){
		
		JSONAwareMultiGraph<ExperimentJUNGGameNodeValue<Integer>, ExperimentJUNGGameEdgeValue<String,Integer,String>> returnGraph = new JSONAwareMultiGraph<ExperimentJUNGGameNodeValue<Integer>, ExperimentJUNGGameEdgeValue<String,Integer,String>>();
		
		ExperimentJUNGGameNodeValue<Integer> currentNode				= null;
		ExperimentJUNGGameEdgeValue<String,Integer,String> currentEdge	= null;
		
		List<ExperimentJUNGGameFluent> currentFluents				= null;
		
		Map<Integer, ExperimentJUNGGameNodeValue<Integer>> game2Graph 	= new HashMap<Integer, ExperimentJUNGGameNodeValue<Integer>>(automaton.getTransitions().size());
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
						currentFluents.add(new ExperimentJUNGGameFluent(fluentNames.get(i), automaton.getFluentValuations().get(state).get(i)));
					}
					for(i = 0; i < livenessNames.size(); i++) {
						currentFluents.add(new ExperimentJUNGGameFluent(livenessNames.get(i), automaton.getLivenessValuations().get(state).get(i)));
					}
					currentNode 	= new ExperimentJUNGGameNodeValue<Integer>(state, currentFluents
							, true, automaton.getInitialStates().contains(state));
					
					returnGraph.addVertex(currentNode);
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
				currentLabel	= getLabel(automaton, j);
				label 			+= currentLabel;
				labels.add(currentLabel);
			}
			label += (transition1.getLabels().size() > 1) ? ">" : "";
			currentEdge	= new ExperimentJUNGGameEdgeValue<String,Integer,String>(label, state, transition1.getToState(), !transition1.getIsInput(), labels);
			if(!returnGraph.addEdge(currentEdge, game2Graph.get(state), game2Graph.get(transition1.getToState()), EdgeType.DIRECTED))
				System.out.println("graph is not being modified\n");
		}


		return returnGraph;
	}	
	/*
	public JSONAwareMultiGraph<ExperimentJUNGGameNodeValue<Long>, ExperimentJUNGGameEdgeValue<String,Long>> getLTSGraph(LTS<Long, String> lts
			, Set<String> controllableActions){
		
		JSONAwareMultiGraph<ExperimentJUNGGameNodeValue<Long>, ExperimentJUNGGameEdgeValue<String,Long>> returnGraph = new JSONAwareMultiGraph<ExperimentJUNGGameNodeValue<Long>, ExperimentJUNGGameEdgeValue<String,Long>>();
		
		ExperimentJUNGGameNodeValue<Long> currentNode		= null;
		ExperimentJUNGGameEdgeValue<String,Long> currentEdge		= null;
		
		List<ExperimentJUNGGameFluent> currentFluents		= null;
		
		Map<Long, ExperimentJUNGGameNodeValue<Long>> game2Graph = new HashMap<Long, ExperimentJUNGGameNodeValue<Long>>(lts.getStates().size());
		
		//add nodes
		for(Long state: lts.getStates()){
			currentFluents	= new ArrayList<ExperimentJUNGGameFluent>();
			currentNode 	= new ExperimentJUNGGameNodeValue<Long>(state, currentFluents
					, true, lts.getInitialState() == state);
			
			returnGraph.addVertex(currentNode);
			
			game2Graph.put(state, currentNode);
		}
		
		
		
		String label;
		//add edges
		for(Long state: lts.getStates()){
			for(Pair<String, Long> neighbour: lts.getTransitions(state)){
				label = neighbour.getFirst();
				currentEdge	= new ExperimentJUNGGameEdgeValue<String,Long>(label, state, neighbour.getSecond(), controllableActions.contains(label));
				if(!returnGraph.addEdge(currentEdge, game2Graph.get(state), game2Graph.get(neighbour.getSecond()), EdgeType.DIRECTED))
					System.out.println("graph is not being modified\n");
			}
		}
		return returnGraph;
	}	
	
	public JSONAwareMultiGraph<ExperimentJUNGGameNodeValue<Long>, ExperimentJUNGGameEdgeValue<String,Long>> getGameGraph(LabelledGameSolver<Long, String, Integer> gameSolver, GRGoal<Long> grGoal, Set<Long> problemWinningStates){
	
		LabelledGame<Long, String> game						= gameSolver.getLabelledGame();
		
		JSONAwareMultiGraph<ExperimentJUNGGameNodeValue<Long>, ExperimentJUNGGameEdgeValue<String,Long>> returnGraph = new JSONAwareMultiGraph<ExperimentJUNGGameNodeValue<Long>, ExperimentJUNGGameEdgeValue<String,Long>>();
		
		ExperimentJUNGGameNodeValue<Long> currentNode		= null;
		ExperimentJUNGGameEdgeValue<String,Long> currentEdge		= null;
		
		List<ExperimentJUNGGameFluent> currentFluents		= null;
		
		Set<Long> initialStates								= game.getInitialStates();
		Set<Long> winningStates								= problemWinningStates;
		
		Map<Long, ExperimentJUNGGameNodeValue<Long>> game2Graph = new HashMap<Long, ExperimentJUNGGameNodeValue<Long>>(game.getStates().size());
		
		int assSize		= grGoal.getAssumptionsQuantity();
		int goalSize	= grGoal.getGuaranteesQuantity();
		
		//add nodes
		for(Long state: game.getStates()){
			currentFluents	= new ArrayList<ExperimentJUNGGameFluent>(assSize + goalSize);
			for(int i = 0; i < assSize; i++){
				currentFluents.add(i,new ExperimentJUNGGameFluent("As " + i
						,grGoal.getAssumption(i+1).contains(state)? true : false));
			}
			for(int i = 0; i < goalSize; i++){
				currentFluents.add(i + assSize,new ExperimentJUNGGameFluent("G " + i
						,grGoal.getGuarantee(i+1).contains(state)? true : false));
			}
			currentNode 	= new ExperimentJUNGGameNodeValue<Long>(state, currentFluents
					, winningStates.contains(state), initialStates.contains(state));
			
			returnGraph.addVertex(currentNode);
			
			game2Graph.put(state, currentNode);
		}
		
		//add edges
		for(Long state: game.getStates()){
			for(Long goodNeighbour: game.getControllableSuccessors(state)){
				currentEdge	= new ExperimentJUNGGameEdgeValue<String,Long>(game.getLabel(state, goodNeighbour), state, goodNeighbour, true);
				returnGraph.addEdge(currentEdge, game2Graph.get(state), game2Graph.get(goodNeighbour), EdgeType.DIRECTED);
			}
			for(Long badNeighbour: game.getUncontrollableSuccessors(state)){
				currentEdge	= new ExperimentJUNGGameEdgeValue<String,Long>(game.getLabel(state, badNeighbour), state, badNeighbour, false);
				if(game2Graph.get(badNeighbour) != null)
					returnGraph.addEdge(currentEdge, game2Graph.get(state), game2Graph.get(badNeighbour), EdgeType.DIRECTED);
				else
					System.out.println("error on "+badNeighbour.toString());
			}			
		}
		return returnGraph;
	}
	*/
}
