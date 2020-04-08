package ar.uba.dc.lafhis.experiments;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.Painter;
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
		            UIManager.put("TextPane[Enabled].backgroundPainter", new Painter<JComponent>() {
		                @Override
		                public void paint(Graphics2D g, JComponent comp, int width, int height) {
		                    g.setColor(comp.getBackground());
		                    g.fillRect(0, 0, width, height);
		                }
		            });		            
		            break;
		        }
		    }
		} catch (Exception e) {
		    // If Nimbus is not available, you can set the GUI to another look and feel.
		}
		
		JFrame frame = new JFrame("CLTSA (Alpha)");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		//frame.setSize(new Dimension(screenSize.width - 200, screenSize.height - 200));
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
		//frame.setUndecorated(true);
		frame.setVisible(true);
		frame.getContentPane().add(new ExperimentJUNGLayoutWindow());
		frame.setVisible(true);
		URL url = ClassLoader.getSystemResource("ar/uba/dc/lafhis/experiments/logo.png");
		Toolkit kit = Toolkit.getDefaultToolkit();
		Image img = kit.createImage(url);
		frame.setIconImage(img);
	}

}
