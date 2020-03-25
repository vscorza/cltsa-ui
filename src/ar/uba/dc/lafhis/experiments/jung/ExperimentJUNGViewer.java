package ar.uba.dc.lafhis.experiments.jung;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;


import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;

import ar.uba.dc.lafhis.experiments.jung.ExperimentJUNGCanvas.EnumLayout;
import ar.uba.dc.lafhis.experiments.jung.ExperimentJUNGCanvas.EnumMode;
import ar.uba.dc.lafhis.experiments.jung.ExperimentJUNGGraph.ExperimentJUNGNavigator;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.EdgeShape.QuadCurve;
import edu.uci.ics.jung.visualization.picking.PickedInfo;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.DefaultEdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.DefaultVertexLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

/**
 * JUNG viewer for LTS graphs
 * @author Cédric Delforge
 */
@SuppressWarnings("serial")
public class ExperimentJUNGViewer extends VisualizationViewer<ExperimentJUNGStateVertex,ExperimentJUNGTransitionEdge> {
    private EnumMode interaction;
    private ExperimentJUNGNavigator navigator;
    private ExperimentJUNGCanvas canvas;
    
	private static Transformer<ExperimentJUNGTransitionEdge, Font> edgeFont	= new Transformer<ExperimentJUNGTransitionEdge, Font>(){
		public Font transform(ExperimentJUNGTransitionEdge edge) {
				return new Font ("SansSerif", Font.PLAIN , 10);
		}			 
	};
	private static Transformer<ExperimentJUNGStateVertex, Font> vertexFont	= new Transformer<ExperimentJUNGStateVertex, Font>(){
		public Font transform(ExperimentJUNGStateVertex vertex) {
				return new Font ("SansSerif", Font.PLAIN , 10);
		}			 
	};    
    
	private static Transformer<ExperimentJUNGStateVertex,Shape> vertexSize = new Transformer<ExperimentJUNGStateVertex,Shape>(){
        public Shape transform(ExperimentJUNGStateVertex vertex){
            return new Ellipse2D.Double(-3, -3, 6, 6);
        }
	};		 
 
	
	public ExperimentJUNGViewer(Layout<ExperimentJUNGStateVertex, ExperimentJUNGTransitionEdge> layout, Dimension preferredSize, ExperimentJUNGCanvas canvas) {
		super(layout, preferredSize);
		
		this.canvas = canvas;
		interaction = EnumMode.Edit;
		navigator = ((ExperimentJUNGGraph)layout.getGraph()).getNavigator();
		
		final ExperimentJUNGViewerPluggableMouse pluggableMouse = new ExperimentJUNGViewerPluggableMouse();
		setGraphMouse(pluggableMouse);
		pluggableMouse.setMode(interaction);
		
		paintEdge();
		paintVertex();
		paintBackground();
	}
	
	/*
	 * Repaints the viewer with the proper colors
	 */
	public void refresh() {
		paintEdge();
		paintVertex();
		paintBackground();
		repaint();
	}
	
	public void setInteraction(EnumMode interact) {
		if (interact != interaction) {
			interaction = interact;
			((ExperimentJUNGViewerPluggableMouse)getGraphMouse()).setMode(interaction);

			if (interaction == EnumMode.Activate) {
				Set<ExperimentJUNGStateVertex> picks;
				if (this.getPickedVertexState().getPicked().size() > 0) {
					picks = new HashSet<ExperimentJUNGStateVertex>(this.getPickedVertexState().getPicked());
				} else {
					picks = ((ExperimentJUNGGraph)this.getGraphLayout().getGraph()).getInitials();
					for (ExperimentJUNGStateVertex v: picks) {
						this.getPickedVertexState().pick(v, true);
					}
				}
				navigator = ((ExperimentJUNGGraph)this.getGraphLayout().getGraph()).getNavigator(picks);
			}
			refresh();
		}
	}
	

//-----------------------------------------------------------------------------	
//	Methods for mouse effects, offered in the viewer for clarity
//-----------------------------------------------------------------------------	
					
	
	protected ExperimentJUNGNavigator getNavigator() {
		return navigator;
	}
	protected void setNavigator(ExperimentJUNGNavigator nav) {
		navigator = nav;
		repaint();
	}
	
	protected void addLayout(EnumLayout l) {
		PickedState<ExperimentJUNGStateVertex> ps = this.getPickedVertexState();
		if ( ps.getPicked().size() > 1) {
			canvas.addLayout(ps, l);
		}
	}

	protected void cluster() {
		PickedState<ExperimentJUNGStateVertex> ps = this.getPickedVertexState();
		if ( ps.getPicked().size() > 1) {
			canvas.cluster(ps);
		}		
	}
	protected void selectNext() {
		PickedState<ExperimentJUNGStateVertex> ps = this.getPickedVertexState();
		Set<ExperimentJUNGStateVertex> picked = new HashSet<ExperimentJUNGStateVertex>(ps.getPicked());
		ps.clear();
		for (ExperimentJUNGStateVertex v: picked) {
			select(navigator.getNext(v));
		}
	}
	protected void selectPrevious() {
		PickedState<ExperimentJUNGStateVertex> ps = this.getPickedVertexState();
		Set<ExperimentJUNGStateVertex> picked = new HashSet<ExperimentJUNGStateVertex>(ps.getPicked());
		ps.clear();
		for (ExperimentJUNGStateVertex v: picked) {
			select(navigator.getPrevious(v));
		}	
	}
	protected void selectReachable() {
		PickedState<ExperimentJUNGStateVertex> ps = this.getPickedVertexState();
		Set<ExperimentJUNGStateVertex> picked = new HashSet<ExperimentJUNGStateVertex>(ps.getPicked());
		ps.clear();
		for (ExperimentJUNGStateVertex v: picked) {
			select(navigator.getReachable(v));
		}		
	}
	protected void selectReaching() {
		PickedState<ExperimentJUNGStateVertex> ps = this.getPickedVertexState();
		Set<ExperimentJUNGStateVertex> picked = new HashSet<ExperimentJUNGStateVertex>(ps.getPicked());
		ps.clear();
		for (ExperimentJUNGStateVertex v: picked) {
			select(navigator.getReaching(v));
		}		
	}
	protected void selectAutomaton() {
		PickedState<ExperimentJUNGStateVertex> ps = this.getPickedVertexState();
		Set<ExperimentJUNGStateVertex> picked = new HashSet<ExperimentJUNGStateVertex>(ps.getPicked());
		ps.clear();
		for (ExperimentJUNGStateVertex v: picked) {
			select(((ExperimentJUNGGraph)this.getGraphLayout().getGraph()).getAutomatonFromState(v));
		}		
	}	
	protected void selectSCC() {
		PickedState<ExperimentJUNGStateVertex> ps = this.getPickedVertexState();
		Set<ExperimentJUNGStateVertex> picked = new HashSet<ExperimentJUNGStateVertex>(ps.getPicked());
		ps.clear();
		for (ExperimentJUNGStateVertex v: picked) {
			select(((ExperimentJUNGGraph)this.getGraphLayout().getGraph()).getSCCFromState(v));
		}
	}
	private void select(Set<ExperimentJUNGStateVertex> vertices) {
		for (ExperimentJUNGStateVertex s: vertices) {
			getPickedVertexState().pick(s, true);
		}
		if (interaction == EnumMode.Activate) {
			navigator = ((ExperimentJUNGGraph)this.getGraphLayout().getGraph()).getNavigator(new HashSet<ExperimentJUNGStateVertex>(this.getPickedVertexState().getPicked()));
			refresh();
		}
	}
	
//-----------------------------------------------------------------------------	
//	Set the proper graphical settings
//-----------------------------------------------------------------------------	
    private static class EditColors {
    	static class Vertex {
    		static final Color def = new Color(1f,0f,1f,.3f);// Color.cyan;
        	static final Color picked = Color.yellow;
        	static final Color animated = Color.red;
        	static final Color error = Color.magenta;
        	static final Color label = Color.black;    	
    	}
    	static class Edge {
    	   	static final Color def = new Color(.1f,0.1f,.1f,.2f);// Color.darkGray;
        	static final Color picked = Vertex.picked;
        	static final Color animated = Vertex.animated;
        	static final Color label = Vertex.label;    		
    	}
    }

    private static class NavigationColors {
    	static class Vertex {
	    	static final Color def = new Color(.1f,0.1f,.1f,.2f);// Color.darkGray;//was darkgray
	    	static final Color navigated = Color.lightGray;
	    	static final Color next = Color.red;
	    	static final Color current = Color.orange;
	    	static final Color label = Color.black;
    	}
    	static class Edge {
	    	static final Color def  = new Color(.1f,0.1f,.1f,.2f);// Color.darkGray;//Color.black;
	    	static final Color next = Vertex.next;
	    	static final Color label = Vertex.label;
	    	static final Color label_next = Color.orange;
    	}
    }
    
    /*
     * inspired by http://martin.ankerl.com/2009/12/09/how-to-create-random-colors-programmatically/
     */
	private Color isInSCC(ExperimentJUNGStateVertex v) {
		int num = ((ExperimentJUNGGraph)this.getGraphLayout().getGraph()).numberSCC(v);
		if (num == 0)
			return null;

		float hue = 0.5f;
		for (int i = 0; i < num; i++) {
			hue += 1.6180339887498948482; //golden ratio (rounded)
			hue %= 1;
		}
		return Color.getHSBColor(hue, 0.5f, 0.95f);	
	}
	
	private void paintEdge() {
		if (interaction == EnumMode.Edit) {
			paintEdgeEdit();
		} else if (interaction == EnumMode.Activate) {
			paintEdgeActivate();
		}
	}
	private void paintEdgeActivate() {
		final Transformer<ExperimentJUNGTransitionEdge,String> stringer = new Transformer<ExperimentJUNGTransitionEdge,String>(){
            public String transform(ExperimentJUNGTransitionEdge e) {
                return e.toString();
            }
        };
        
        class EdgePaintTransformer implements Transformer<ExperimentJUNGTransitionEdge,Paint> {
            public EdgePaintTransformer() { 
                super();
            }
        	public Paint transform(ExperimentJUNGTransitionEdge e) {
        		return navigator.getPath().contains(e) ?
        					NavigationColors.Edge.next
        				:
        					NavigationColors.Edge.def;
        	}
        };
      
        class EdgeLabelColorRenderer extends DefaultEdgeLabelRenderer {
            public EdgeLabelColorRenderer() {
                super(NavigationColors.Edge.label);
            }

			public <E> Component getEdgeLabelRendererComponent(JComponent vv, Object value,
                    Font font, boolean isSelected, E edge) {
                Component out = super.getEdgeLabelRendererComponent(vv, value, font, isSelected, edge);
                super.setForeground(
                		navigator.getPath().contains(edge) ?
                				NavigationColors.Edge.label_next
                				:
                					NavigationColors.Edge.label
                		);
                return out;
            }
        }

        final Transformer<ExperimentJUNGTransitionEdge,Paint> colorer = new EdgePaintTransformer();
        final DefaultEdgeLabelRenderer labelColorer = new EdgeLabelColorRenderer();
        final Transformer<ExperimentJUNGTransitionEdge,Paint> arrowColorer = new EdgePaintTransformer();
        final Transformer<ExperimentJUNGTransitionEdge,Paint> arrowFiller = new EdgePaintTransformer();
        final QuadCurve<ExperimentJUNGStateVertex, ExperimentJUNGTransitionEdge> shaper = new EdgeShape.QuadCurve<ExperimentJUNGStateVertex,ExperimentJUNGTransitionEdge>();
        shaper.setControlOffsetIncrement(canvas.getCurve());

        getRenderContext().setVertexShapeTransformer(vertexSize);
		getRenderContext().setEdgeFontTransformer(edgeFont);
		getRenderContext().setVertexFontTransformer(vertexFont);
    	getRenderContext().setEdgeLabelTransformer(stringer);
    	getRenderContext().setEdgeDrawPaintTransformer(colorer);
    	getRenderContext().setEdgeLabelRenderer(labelColorer);
    	getRenderContext().setArrowDrawPaintTransformer(arrowColorer);
    	getRenderContext().setArrowFillPaintTransformer(arrowFiller);
    	getRenderContext().setEdgeShapeTransformer(shaper);
    	getRenderContext().getEdgeLabelRenderer().setRotateEdgeLabels(false);
	}

	private void paintEdgeEdit() {
		final Transformer<ExperimentJUNGTransitionEdge,String> stringer = new Transformer<ExperimentJUNGTransitionEdge,String>(){
            public String transform(ExperimentJUNGTransitionEdge e) {
                return e.toString();
            }
        };
        
        class EdgePaintTransformer implements Transformer<ExperimentJUNGTransitionEdge,Paint> {
            protected final PickedInfo<ExperimentJUNGTransitionEdge> p;
            public EdgePaintTransformer( PickedInfo<ExperimentJUNGTransitionEdge> p ) { 
                super();
                this.p = p;
            }
        	public Paint transform(ExperimentJUNGTransitionEdge e) {
        		return p.isPicked(e) ?
        					EditColors.Edge.picked
        				:
        					canvas.getSelectedTransitions().contains(e) ?
    							EditColors.Edge.animated
							:
								EditColors.Edge.def;
        	}
        };
        
        class EdgeLabelColorRenderer extends DefaultEdgeLabelRenderer {
            public EdgeLabelColorRenderer() {
                super(EditColors.Edge.label);
            }

			public <E> Component getEdgeLabelRendererComponent(JComponent vv, Object value,
                    Font font, boolean isSelected, E edge) {
                Component out = super.getEdgeLabelRendererComponent(vv, value, font, isSelected, edge);
                super.setForeground(canvas.getSelectedTransitions().contains(edge) ? EditColors.Edge.animated.darker().darker() : EditColors.Edge.label);
                return out;
            }
        }
        final Transformer<ExperimentJUNGTransitionEdge,Paint> colorer = new EdgePaintTransformer(this.getPickedEdgeState());
        final QuadCurve<ExperimentJUNGStateVertex, ExperimentJUNGTransitionEdge> shaper = new EdgeShape.QuadCurve<ExperimentJUNGStateVertex,ExperimentJUNGTransitionEdge>();
        shaper.setControlOffsetIncrement(canvas.getCurve());

        getRenderContext().setVertexShapeTransformer(vertexSize);
		getRenderContext().setEdgeFontTransformer(edgeFont);
		getRenderContext().setVertexFontTransformer(vertexFont);
    	getRenderContext().setEdgeLabelTransformer(stringer);
    	getRenderContext().setEdgeDrawPaintTransformer(colorer);
    	getRenderContext().setEdgeLabelRenderer(new EdgeLabelColorRenderer());
    	getRenderContext().getEdgeLabelRenderer().setRotateEdgeLabels(false);
    	getRenderContext().setArrowDrawPaintTransformer(new EdgePaintTransformer(this.getPickedEdgeState()));
    	getRenderContext().setArrowFillPaintTransformer(new EdgePaintTransformer(this.getPickedEdgeState()));
    	getRenderContext().setEdgeShapeTransformer(shaper);
	}
	private void paintVertex() {
		if (interaction == EnumMode.Edit) {
			paintVertexEdit();
		} else if (interaction == EnumMode.Activate) {
			paintVertexActivate();
		}
	}
	private void paintVertexActivate() {
		final Transformer<ExperimentJUNGStateVertex,String> stringer = new Transformer<ExperimentJUNGStateVertex,String>(){
            public String transform(ExperimentJUNGStateVertex v) {
                return v.toString();
            }
        };
        
        class VertexPaintTransformer implements Transformer<ExperimentJUNGStateVertex,Paint> {
            public VertexPaintTransformer() { 
                super();
            }
        	public Paint transform(ExperimentJUNGStateVertex v) {
        		return navigator.getCurrent().contains(v) ?
        					NavigationColors.Vertex.current
        				:
        					navigator.getNext().contains(v) ?
        							NavigationColors.Vertex.next
        					:
        						navigator.getReached().contains(v) ?
        								NavigationColors.Vertex.navigated
	        					:
	        						NavigationColors.Vertex.def;
        	}
        };
        class VertexLabelColorRenderer extends DefaultVertexLabelRenderer {
        	protected Color unpickedLabelColor = Color.black;
			public VertexLabelColorRenderer(Color pickedVertexLabelColor, Color unpickedColor) {
				super(pickedVertexLabelColor);
				unpickedLabelColor = unpickedColor;
			}

			public <E> Component getVertexLabelRendererComponent(JComponent vv, Object value,
                    Font font, boolean isSelected, E edge) {
                Component out = super.getVertexLabelRendererComponent(vv, value, font, isSelected, edge);
                super.setForeground(unpickedLabelColor);
                return out;
			}
        }
        final Transformer<ExperimentJUNGStateVertex,Paint> colorer = new VertexPaintTransformer();

        getRenderContext().setVertexShapeTransformer(vertexSize);
		getRenderContext().setEdgeFontTransformer(edgeFont);
		getRenderContext().setVertexFontTransformer(vertexFont);
    	getRenderContext().setVertexLabelTransformer(stringer);
    	getRenderContext().setVertexFillPaintTransformer(colorer);
    	DefaultVertexLabelRenderer labeler = new VertexLabelColorRenderer(NavigationColors.Vertex.label, NavigationColors.Vertex.label);
    	getRenderContext().setVertexLabelRenderer(labeler);
    	getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
	}
	private void paintVertexEdit() {
		final Transformer<ExperimentJUNGStateVertex,String> stringer = new Transformer<ExperimentJUNGStateVertex,String>(){
            public String transform(ExperimentJUNGStateVertex v) {
                return v.toString();
            }
        };
        
        class VertexPaintTransformer implements Transformer<ExperimentJUNGStateVertex,Paint> {
            protected final PickedInfo<ExperimentJUNGStateVertex> p;


            public VertexPaintTransformer( PickedInfo<ExperimentJUNGStateVertex> p ) { 
                super();
                this.p = p;
            }
        	public Paint transform(ExperimentJUNGStateVertex v)
			{
        		Color color;
        		return  p.isPicked(v) ? EditColors.Vertex.picked
        				: ((canvas.shouldColorSCC() && (color = isInSCC(v)) != null) ? color
        					: canvas.getSelectedVertices().contains(v) ? EditColors.Vertex.animated
        						: (v.toString().equals("0") && canvas.getSelectedVertices().size() == 0 ? EditColors.Vertex.animated
		        					: ( v.toString().equals("-1") ? EditColors.Vertex.error
		        						: EditColors.Vertex.def)		        					)
        					);
        	}
        };
        
        class VertexLabelColorRenderer extends DefaultVertexLabelRenderer {
        	protected Color unpickedLabelColor = Color.black;
			public VertexLabelColorRenderer(Color pickedVertexLabelColor, Color unpickedColor) {
				super(pickedVertexLabelColor);
				unpickedLabelColor = unpickedColor;
			}

			public <E> Component getVertexLabelRendererComponent(JComponent vv, Object value,
                    Font font, boolean isSelected, E edge) {
                Component out = super.getVertexLabelRendererComponent(vv, value, font, isSelected, edge);
                super.setForeground(unpickedLabelColor);
                return out;
			}
        }
        final Transformer<ExperimentJUNGStateVertex,Paint> colorer = new VertexPaintTransformer(getPickedVertexState());
        final VertexLabelColorRenderer labeler = new VertexLabelColorRenderer(EditColors.Vertex.label, EditColors.Vertex.label);

        getRenderContext().setVertexShapeTransformer(vertexSize);
		getRenderContext().setEdgeFontTransformer(edgeFont);
		getRenderContext().setVertexFontTransformer(vertexFont);
    	getRenderContext().setVertexLabelTransformer(stringer);
    	getRenderContext().setVertexFillPaintTransformer(colorer);
    	getRenderContext().setVertexLabelRenderer(labeler);
    	getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
	}
	
	public BufferedImage getImage() {
		Point2D center = getCenter();
		Dimension d = getSize();
        int width = getWidth();
        int height = getHeight();
        
        float scalex = (float)width/d.width;
        float scaley = (float)height/d.height;
        try {
            renderContext.getMultiLayerTransformer().getTransformer(Layer.VIEW).scale(scalex, scaley, center);
    
            BufferedImage bi = new BufferedImage(width, height,
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = bi.createGraphics();
            graphics.setRenderingHints(renderingHints);
            paint(graphics);
            graphics.dispose();
            return bi;
        } finally {
        	renderContext.getMultiLayerTransformer().getTransformer(Layer.VIEW).setToIdentity();
        }
    }

	private void paintBackground() {
		this.setBackground(interaction == EnumMode.Edit ? Color.white : Color.gray);
	}
}
