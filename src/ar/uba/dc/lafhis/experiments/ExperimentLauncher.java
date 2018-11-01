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
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import ar.uba.dc.lafhis.experiments.exchange.JSONCompatible;
import ar.uba.dc.lafhis.experiments.exchange.JSONCompatibleBoolean;
import ar.uba.dc.lafhis.experiments.exchange.JSONCompatibleObject;
import ar.uba.dc.lafhis.experiments.exchange.JSONCompatibleString;
import ar.uba.dc.lafhis.experiments.jung.ExperimentJUNGGameEdgeValue;
import ar.uba.dc.lafhis.experiments.jung.ExperimentJUNGGameNodeValue;
import ar.uba.dc.lafhis.experiments.jung.ExperimentJUNGHelper;
import ar.uba.dc.lafhis.experiments.jung.ExperimentJUNGLayoutWindow;
import ar.uba.dc.lafhis.experiments.visualization.ExperimentJUNGGraphVisualization;
import ar.uba.dc.lafhis.henos.report.ReportAutomaton;
import edu.uci.ics.jung.graph.DirectedGraph;

public class ExperimentLauncher {

	public static void main(String[] args) {
		/*
		String serializedAutomaton	= "<automaton,<CTX,<4,[<x1.off,0>,<x1.on,0>,<y1.off,1>,<y1.on,1>]>,1,[test fluent],2,[ass_1,goal_1]>,4,[0,1,2,3],2,[<0,1,2,[1,3],1>,<0,0,0,[],0>],1,[0],[[0],[0]],[[0,0],[0,1]]>";
		InputStream is				= new ByteArrayInputStream(serializedAutomaton.getBytes(StandardCharsets.UTF_8));
		ReportAutomaton automaton	= new ReportAutomaton(new PushbackInputStream(is));
		System.out.println(automaton.toString());
		*/
		try {
		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
		} catch (Exception e) {
		    // If Nimbus is not available, you can set the GUI to another look and feel.
		}
		
		JFrame frame = new JFrame("Simple Graph View");
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(new Dimension(screenSize.width - 200, screenSize.height - 200));
		frame.getContentPane().add(new ExperimentJUNGLayoutWindow());
		frame.setVisible(true); 
	}

}
