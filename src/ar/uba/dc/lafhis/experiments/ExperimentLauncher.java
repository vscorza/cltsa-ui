package ar.uba.dc.lafhis.experiments;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.swing.JFrame;

import ar.uba.dc.lafhis.experiments.exchange.JSONCompatible;
import ar.uba.dc.lafhis.experiments.exchange.JSONCompatibleBoolean;
import ar.uba.dc.lafhis.experiments.exchange.JSONCompatibleObject;
import ar.uba.dc.lafhis.experiments.exchange.JSONCompatibleString;
import ar.uba.dc.lafhis.experiments.jung.ExperimentJUNGGameEdgeValue;
import ar.uba.dc.lafhis.experiments.jung.ExperimentJUNGGameNodeValue;
import ar.uba.dc.lafhis.experiments.jung.ExperimentJUNGHelper;
import ar.uba.dc.lafhis.experiments.visualization.ExperimentJUNGGraphVisualization;
import ar.uba.dc.lafhis.henos.report.ReportAutomaton;
import edu.uci.ics.jung.graph.DirectedGraph;

public class ExperimentLauncher {

	public static void main(String[] args) {
		String serializedAutomaton	= "<automaton,<CTX,<4,[<x1.off,0>,<x1.on,0>,<y1.off,1>,<y1.on,1>]>,1,[test fluent],2,[ass_1,goal_1]>,4,[0,1,2,3],2,[<0,1,2,[1,3],1>,<0,0,0,[],0>],1,[0],[[0],[0]],[[0,0],[0,1]]>";
		InputStream is				= new ByteArrayInputStream(serializedAutomaton.getBytes(StandardCharsets.UTF_8));
		ReportAutomaton automaton	= new ReportAutomaton(new PushbackInputStream(is));
		System.out.println(automaton.toString());
		JFrame frame = new JFrame("Simple Graph View");
		 frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		 JSONCompatibleObject jsonGraph = new JSONCompatibleObject(ExperimentJUNGHelper.getInstance().getReportAutomatonGraph(automaton));
		 try {
			//size of the screen
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			frame.getContentPane().add((new ExperimentJUNGGraphVisualization(jsonGraph, new Dimension(screenSize.width - 200, screenSize.height - 200))).getVisualComponent());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 frame.pack();
		 frame.setVisible(true); 
				
		/*
		GamePrunningExperiment firstExperiment = new GamePrunningExperiment("Aug.Bisc.");
		
		Dictionary<String, JSONCompatible> parameters = new Hashtable<String, JSONCompatible>();
		
		parameters.put(GamePrunningExperiment.ASSUMPTIONS_PARAM
				, new JSONCompatibleBoolean(true));
		parameters.put(GamePrunningExperiment.NO_G_PARAM
				, new JSONCompatibleBoolean(true));
		parameters.put(GamePrunningExperiment.SHORT_NAME_PARAM
				, new JSONCompatibleString("aug.bisc"));
		parameters.put(GamePrunningExperiment.CTRL_NAME_PARAM
				, new JSONCompatibleString("C"));
		parameters.put(GamePrunningExperiment.ENV_NAME_PARAM
				, new JSONCompatibleString("BISCOTTI"));
		parameters.put(GamePrunningExperiment.EXP_NAME_PARAM
				, new JSONCompatibleString("EXP"));
		parameters.put(GamePrunningExperiment.GOAL_NAME_PARAM
				, new JSONCompatibleString("G1"));
		parameters.put(GamePrunningExperiment.LTS_LOC_PARAM
				, new JSONCompatibleString("../ltsa/dist/examples/SafetyCuts/Tests/Augmented-Biscotti.lts"));

		firstExperiment.runExperiment(parameters, "../ltsa/dist/examples/SafetyCuts/Results/Augmented-Biscotti.result");

		JFrame frame = new JFrame("Simple Graph View");
		 frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		 JSONCompatibleObject jsonGraph = (JSONCompatibleObject)firstExperiment.results.get(GamePrunningExperiment.GAME_GRAPH_RESULT);
		 try {
			//size of the screen
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			frame.getContentPane().add((new ExperimentJUNGGraphVisualization(jsonGraph, new Dimension(screenSize.width - 200, screenSize.height - 200))).getVisualComponent());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 frame.pack();
		 frame.setVisible(true); 
		 */
	}

}
