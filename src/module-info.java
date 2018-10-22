/**
 * 
 */
/**
 * @author mariano
 *
 */
module henos.reporter {
	exports ar.uba.dc.lafhis.experiments.jung;
	exports ar.uba.dc.lafhis.experiments.ui;
	exports ar.uba.dc.lafhis.experiments;
	exports ar.uba.dc.lafhis.henos.report;
	exports ar.uba.dc.lafhis.experiments.visualization;
	exports ar.uba.dc.lafhis.experiments.exchange;

	requires collections.generic;
	requires java.desktop;
	requires json.simple;
	requires jung.algorithms;
	requires jung.api;
	requires jung.graph.impl;
	requires jung.visualization;
}