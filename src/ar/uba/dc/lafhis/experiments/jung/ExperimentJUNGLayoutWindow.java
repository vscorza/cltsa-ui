package ar.uba.dc.lafhis.experiments.jung;


import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.DefaultCaret;

import ar.uba.dc.lafhis.experiments.jung.ExperimentJUNGCanvas.EnumLayout;
import ar.uba.dc.lafhis.experiments.jung.ExperimentJUNGCanvas.EnumMode;
import ar.uba.dc.lafhis.henos.report.ReportAutomaton;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.*;


@SuppressWarnings("serial")
public class ExperimentJUNGLayoutWindow extends JSplitPane{
    ExperimentJUNGCanvas output; //the panel where all the machines are drawn
    JTabbedPane tabbedPane;
    String lastOpenedFile = "";

    EnumLayout layout = EnumLayout.FruchtermanReingold; //current layout selected
    int[] lastEvent, prevEvent; //last event received, event before that one
    //used for trace animation
    String lastName; //name of the last event
    int Nmach = 0;  //the number of machines
    int hasC = 0;   //1 if last machine is composition

    ExperimentJUNGGraph[] graphs; //array of graphs corresponding to a lts
    boolean[] graphValidity; //true for a graph index if it has been generated already and is still valid

    boolean[] machineHasAction;
    boolean[] machineToDrawSet; //true or false for each machine depending on whether
    //it is part of the multiple LTS composition or not
    Map<Integer, EnumLayout> machineLayout; //maps a machine to the layout it was drawn as
    ReportAutomaton[] sm;
    
    //default values
    public static boolean fontFlag = false;
    public static boolean singleMode = true;
    public static int stateLimit = 0;

    JList list; //machine list

    Font f1 = new Font("Monospaced", Font.PLAIN, 12);
    Font f2 = new Font("Monospaced", Font.BOLD, 16);
    Font f3 = new Font("SansSerif", Font.PLAIN, 12);
    Font f4 = new Font("SansSerif", Font.BOLD, 16);

    JTextPane infoText;
    JTextPane visualizationText;
    
    ImageIcon drawIcon;

    public ExperimentJUNGLayoutWindow() {

        super();
        drawIcon = getDrawIcon();

        output = new ExperimentJUNGCanvas();

        Container frame	= this.getParent();
        JScrollPane left;
        //scrollable list pane
        list = new JList();
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(new DrawAction());
        list.setCellRenderer(new MyCellRenderer());
        left = new JScrollPane(list,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JPanel canvasTools = new JPanel(new BorderLayout());
        canvasTools.add("Center", output);
        
        JPanel editingPanel = new JPanel(new BorderLayout());
        JTextArea editingArea = new JTextArea();
        Font font = new Font("Courier New", Font.PLAIN, 11);
        editingArea.setFont(font);
        editingArea.setBackground(Color.BLACK);
        editingArea.setForeground(Color.WHITE);
        JScrollPane editingPane;
        //scrollable list pane
        editingPane = new JScrollPane(editingArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        editingPane.setPreferredSize(new Dimension(editingPane.getPreferredSize().width, 100));        
        editingPanel.add("Center", editingPane);
        
        
        infoText			= new JTextPane();
        infoText.setContentType("text/html");
        infoText.setText("Welcome to the <font color='blue'><b>C</b>oncurrent <b>L</b>abeled <b>T</b>ransition <b>S</b>ystem <b>A</b>nalyzer</font>.<br>Feel free to contact me at <a href='mailto:vscorza@gmail.com'>vscorza@gmail.com</a><br><b>[Status pending]</b></html>");
        visualizationText	= new JTextPane();
        visualizationText.setContentType("text/html");
        visualizationText.setText(infoText.getText());
        
        DefaultCaret caret = (DefaultCaret) infoText.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        caret = (DefaultCaret) visualizationText.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        
        JScrollPane editorSouthPane = new JScrollPane(infoText, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        editorSouthPane.setPreferredSize(new Dimension(editorSouthPane.getPreferredSize().width, 150));
        editingPanel.add("South", editorSouthPane);
        
        JScrollPane visualizationSouthPane = new JScrollPane(visualizationText, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        visualizationSouthPane.setPreferredSize(new Dimension(visualizationSouthPane.getPreferredSize().width, 150));
        canvasTools.add("South", visualizationSouthPane);
        
        tabbedPane = new JTabbedPane();
        
        tabbedPane.addTab("Editor", editingPanel);
        tabbedPane.addTab("Visualization", canvasTools);        
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add("Center", tabbedPane);
        

        ArrayList<EnumLayout> layoutTypes = new ArrayList<EnumLayout>();
        for (EnumLayout l : EnumLayout.values()) {
            if (l != EnumLayout.Aggregate)
                layoutTypes.add(l);
        }
        JComboBox layoutTypeComboBox = new JComboBox(layoutTypes.toArray(new EnumLayout[layoutTypes.size()]));
        layoutTypeComboBox.setSelectedItem(layout);
        layoutTypeComboBox.addActionListener(new LayoutPickAction());

        JCheckBox sccCheckBox = new JCheckBox("Color SCC");
        sccCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                output.colorSCC(e.getStateChange() == ItemEvent.SELECTED);
            }
        });

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(new RefreshAction());

        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                output.stop();
            }
        });
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                output.clear();
                machineLayout.clear();
                list.clearSelection();
            }
        });
        JCheckBox navigateCheckBox = new JCheckBox("Navigate");
        navigateCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    output.setInteraction(EnumMode.Activate);
                } else {
                    output.setInteraction(EnumMode.Edit);
                }
            }
        });

        JButton reachingButton = new JButton("<<");
        reachingButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                output.reaching();
            }
        });
        JButton previousButton = new JButton("<");
        previousButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                output.previous();
            }
        });
        JButton nextButton = new JButton(">");
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                output.next();
            }
        });
        JButton reachableButton = new JButton(">>");
        reachableButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                output.reachable();
            }
        });
        JButton bZoomIn = new JButton(" + ");
        bZoomIn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                output.zoomIn();
            }
        });
        JButton bZoomOut = new JButton(" - ");
        bZoomOut.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                output.zoomOut();
            }
        });
        JButton saveButton = new JButton(">Mem");
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                output.savePositions();
            }
        });
        JButton loadButton = new JButton("<Mem");
        loadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                output.loadPositions();
            }
        });

        JToolBar layoutControls = new JToolBar();
        layoutControls.setOrientation(JToolBar.HORIZONTAL);
        layoutControls.add(layoutTypeComboBox);
        layoutControls.add(bZoomIn);
        layoutControls.add(bZoomOut);
        layoutControls.add(sccCheckBox);
        layoutControls.addSeparator();
        layoutControls.add(refreshButton);
        layoutControls.add(stopButton);
        layoutControls.add(clearButton);
        layoutControls.addSeparator();
        layoutControls.add(navigateCheckBox);
        layoutControls.addSeparator();
        layoutControls.add(reachingButton);
        layoutControls.add(previousButton);
        layoutControls.add(nextButton);
        layoutControls.add(reachableButton);
        layoutControls.addSeparator();
        layoutControls.add(saveButton);
        layoutControls.add(loadButton);

        layoutControls.setFloatable(false);
        canvasTools.add("North", layoutControls);
        
		JFileChooser chooser = new JFileChooser();
		chooser.setPreferredSize(new Dimension(950, 600));
        
        JPanel leftPanel	= new JPanel(new BorderLayout());
        JPanel innerLeftPanel	= new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        
        JButton openButton	= new JButton("Open FSP");
        openButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				chooser.setCurrentDirectory(new File("../henos-automata/src/tests"));
				chooser.setMultiSelectionEnabled(false);
				chooser.setFileFilter(new FileFilter() {
					public boolean accept(File f) {
						return f.getName().toLowerCase().endsWith(".fsp")
						|| f.isDirectory();
					}
					
					public String getDescription() {
					return "FSP Files";
					}
				});
				int r = chooser.showOpenDialog(frame);
				if (r == JFileChooser.APPROVE_OPTION) {
					try {
						infoText.setText("");
						editingArea.setText("");
						String s, s2 = "";
						File f	= chooser.getSelectedFile();
						String filename = f.getPath();
						lastOpenedFile	= filename;
						BufferedReader br = new BufferedReader(new FileReader(f)); 
						while ((s = br.readLine()) != null) { 
							s2 += s + "\n"; 
						} 						
						editingArea.setText(s2);
						tabbedPane.setSelectedIndex(0);
					} catch (IOException e1) {
						e1.printStackTrace();
						infoText.setText(e1.getMessage());
					}					
				}
			}

		});
        innerLeftPanel.add(openButton, gbc);  
        JButton saveFSPButton	= new JButton("Save FSP");
        saveFSPButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(lastOpenedFile.trim().length() < 1)return;
				try {
					infoText.setText("");
					String s, s2 = "";
					File f = new File(lastOpenedFile);
					FileWriter fw = new FileWriter(f.getAbsoluteFile(), true);
					editingArea.write(fw);
					fw.close();
				} catch (IOException e1) {
					e1.printStackTrace();
					infoText.setText(e1.getMessage());
				}									
			}

		});
        innerLeftPanel.add(saveFSPButton, gbc);  
        JButton saveFSPAsButton	= new JButton("Save FSP As...");
        saveFSPAsButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					infoText.setText("");
					chooser.setCurrentDirectory(new File("../henos-automata/src/tests"));
					chooser.setMultiSelectionEnabled(false);
					chooser.setFileFilter(new FileFilter() {
						public boolean accept(File f) {
							return f.getName().toLowerCase().endsWith(".fsp")
							|| f.isDirectory();
						}
						
						public String getDescription() {
						return "FSP Files";
						}
					});
					int r = chooser.showOpenDialog(frame);
					if (r == JFileChooser.APPROVE_OPTION) {
						lastOpenedFile = chooser.getSelectedFile().getPath();
					}else return;
					
					String s, s2 = "";
					File f = new File(lastOpenedFile);
					FileWriter fw = new FileWriter(f.getAbsoluteFile(), true);
					editingArea.write(fw);
					fw.close();
				} catch (IOException e1) {
					e1.printStackTrace();
					infoText.setText(e1.getMessage());
				}									
			}

		});
        innerLeftPanel.add(saveFSPAsButton, gbc);  

        JButton runButton	= new JButton("Compile");
        runButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
					try {
						File f = new File("/tmp/current_cltsa_editor_file.tmpfsp");
						FileWriter fw = new FileWriter(f.getAbsoluteFile(), false);
						editingArea.write(fw);
						fw.close();
						compile(f);
					} catch (IOException e1) {
						e1.printStackTrace();
					}					
			}

		});
        innerLeftPanel.add(runButton, gbc);        

        JButton compileButton	= new JButton("Compile from File");
        compileButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				chooser.setCurrentDirectory(new File("../henos-automata/src/tests"));
				chooser.setMultiSelectionEnabled(false);
				chooser.setFileFilter(new FileFilter() {
					public boolean accept(File f) {
						return f.getName().toLowerCase().endsWith(".fsp")
						|| f.isDirectory();
					}
					
					public String getDescription() {
					return "FSP Files";
					}
				});
				int r = chooser.showOpenDialog(frame);
				if (r == JFileChooser.APPROVE_OPTION) {
					compile(chooser.getSelectedFile());
				}
			}

		});
        innerLeftPanel.add(compileButton, gbc);        
        JButton fileButton	= new JButton("Open Reports");
        fileButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				chooser.setCurrentDirectory(new File("../henos-automata/src/results"));
				chooser.setMultiSelectionEnabled(true);
				chooser.setFileFilter(new FileFilter() {
					public boolean accept(File f) {
						return f.getName().toLowerCase().endsWith(".rep")
						|| f.isDirectory();
					}
					public String getDescription() {
					return "REP Files";
					}
				});
				int r = chooser.showOpenDialog(frame);
				if (r == JFileChooser.APPROVE_OPTION) {
					try {
						int fileCount	= chooser.getSelectedFiles().length;
						sm				= new ReportAutomaton[fileCount];
						fileCount		= 0;
						for(File f: chooser.getSelectedFiles()) {
							String filename = chooser.getSelectedFile().getPath();
							FileInputStream is;
								is = new FileInputStream(f);
							
							sm[fileCount++]	= new ReportAutomaton(new PushbackInputStream(is));
						}
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					}					
			        new_machines();
				}
			}

		});
        innerLeftPanel.add(fileButton, gbc);
        innerLeftPanel.add(new JLabel("Reports"), gbc);
        leftPanel.add("North", innerLeftPanel);
        leftPanel.add("Center", left);
        
        setLeftComponent(leftPanel);
        setRightComponent(mainPanel);
        setDividerLocation(200);
        setBigFont(fontFlag);
        validate();

    }

    private String compile(File f) {
		String filename = f.getPath();
		String cmdString	= "../henos-automata/src/cltsa -r " + filename;
		String s3 = "Running the following command: <i>" + cmdString + "</i><br>";
		infoText.setText(s3);
		Thread cmdThread = new Thread() {
			public void run() {
				try {
					String s = "", s2 = "";

				Process p = Runtime.getRuntime().exec(cmdString);
		        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
		
				BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				
				boolean hasStd = false;
				while ((s = stdInput.readLine()) != null) {
					if(!hasStd) {
						s2+="<b>Here is the standard output of the command:</b><br>";
						hasStd = true;
					}
					s2+=s + "<br>";
					infoText.setText(s2);
				}
				boolean hasError = false;
				
				while ((s = stdError.readLine()) != null) {
					if(!hasError) {
						s2+= "Here is the standard error of the command (if any):\n";
						hasError = true;
					}
					s2+=s + "<br>";
					infoText.setText(s2);
				}
		    	s2+=openReports();
		    	infoText.setText(s2);
					} catch (IOException e1) {
						e1.printStackTrace();
					}	
				}
		};
		cmdThread.setDaemon(true);
		cmdThread.start();
	    return "";
    }
    
    private String openReports() {
        File directory = new File("/tmp");
        File[] fileList = directory.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File f, String name) {
				return name.toLowerCase().endsWith(".rep");
			}
		});
		try {
			int fileCount	= fileList.length;
			sm				= new ReportAutomaton[fileCount];
			fileCount		= 0;
			for(File f2: fileList) {
				FileInputStream is;
					is = new FileInputStream(f2.getPath());
				
				sm[fileCount++]	= new ReportAutomaton(new PushbackInputStream(is));
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return e1.getMessage();
		}					
        new_machines();
        return "";
    }
    
    private ImageIcon getDrawIcon() {
    	return null;
    	/*
        String icon = "icon/draw.gif";
        return new ImageIcon(getClass().getClassLoader().getResource(icon));
        */
    }

    public void setCurrentState(int[] currentStateNumbers) {
        this.prevEvent = Arrays.copyOf(currentStateNumbers, currentStateNumbers.length + 2);
        this.lastEvent = Arrays.copyOf(currentStateNumbers, currentStateNumbers.length + 2);

        this.prevEvent[prevEvent.length - 2] = currentStateNumbers[0];
        this.lastEvent[lastEvent.length - 2] = currentStateNumbers[0];

        this.prevEvent[prevEvent.length - 1] = 0;
        this.lastEvent[lastEvent.length - 1] = 0;

        this.lastName = "";
    }

//-----------------------------------------------------------------------------
// Listener classes
//-----------------------------------------------------------------------------

    private class LayoutPickAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            @SuppressWarnings("unchecked")
            JComboBox cb = (JComboBox) e.getSource();
            layout = (EnumLayout) cb.getSelectedItem();
        }
    }

    private class RefreshAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            refresh();
        }
    }

    private class DrawAction implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent lse) {
            if (lse.getValueIsAdjusting()) return;
            int machine = list.getSelectedIndex();
            if (machine < 0 || machine >= Nmach) return;

            visualizationText.setText(sm[machine].getAutomatonInfo());
            
            if (singleMode) {
                if (stateLimit > 0 && sm[machine].getTransitions().size() > stateLimit) {
                    int o = JOptionPane.showConfirmDialog(getParent(),
                            "The number of states of this LTS (" + sm[machine].getTransitions().size() + ") is above the set limit of " + stateLimit + ".\nDo you want to display it?",
                            "Limit reached",
                            JOptionPane.YES_NO_OPTION);
                    if (o == JOptionPane.NO_OPTION) {
                        return;
                    }
                }
                output.clear();

                ExperimentJUNGGraph graph = makeGraph(machine);

                Set<ExperimentJUNGStateVertex> currentVertex = new HashSet<ExperimentJUNGStateVertex>(1);
                for (ExperimentJUNGStateVertex aVertex : graph.getVertices())
                    try {
                        if (aVertex.getStateIndex() == lastEvent[machine])
                            currentVertex.add(aVertex);
                    } catch (NullPointerException e) {
                    }
                output.setSelectedStates(currentVertex);

                output.draw(graph, layout);
                machineLayout.clear();
                machineLayout.put(machine, layout);
            } else {
                if (!machineToDrawSet[machine]) { //toggle between whether to draw or remove the machine in multiple display mode
                    if (stateLimit > 0 && sm[machine].getTransitions().size() > stateLimit) {
                        int o = JOptionPane.showConfirmDialog(getParent(),
                                "The number of states of this LTS (" + sm[machine].getTransitions().size() + ") is above the set limit of " + stateLimit + ".\nDo you want to display it?",
                                "Limit reached",
                                JOptionPane.YES_NO_OPTION);
                        if (o == JOptionPane.NO_OPTION) {
                            return;
                        }
                    }
                    output.draw(makeGraph(machine), layout);
                    machineLayout.put(machine, layout);
                    machineToDrawSet[machine] = true;
                } else {
                    output.clear(makeGraph(machine));
                    machineLayout.remove(machine);
                    machineToDrawSet[machine] = false;
                }
                list.clearSelection();
            }
            tabbedPane.setSelectedIndex(1);
        }
    }

    private class MyCellRenderer extends JLabel implements ListCellRenderer {
        public MyCellRenderer() {
            setOpaque(true);
            setHorizontalTextPosition(SwingConstants.LEFT);
        }

        public Component getListCellRendererComponent(
                JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            setFont(fontFlag ? f4 : f3);
            setText(value + (machineLayout.containsKey(index) ? " (" + machineLayout.get(index) + ")" : ""));
            setBackground(isSelected ? Color.blue : Color.white);
            setForeground(isSelected ? Color.white : Color.black);
            if (machineHasAction != null && machineHasAction[index]) {
                setBackground(Color.red);
                setForeground(Color.white);
            }
            setForeground(isSelected ? Color.white : Color.black);
            setIcon(machineToDrawSet[index] && !singleMode ? drawIcon : null);
            return this;
        }
    }

    public void refresh() {
        if (singleMode) {
            output.refresh(layout);
            if (machineLayout != null && machineLayout.keySet().size() > 0) {
                Integer machine = machineLayout.keySet().iterator().next();
                machineLayout.clear();
                machineLayout.put(machine, layout);
            }
        } else {
            output.refresh(layout);
            for (Integer machine : machineLayout.keySet()) {
                machineLayout.put(machine, layout);
            }
        }
        list.updateUI();
    }
//-----------------------------------------------------------------------------
// LTS event handlers
//-----------------------------------------------------------------------------
/*
    public void ltsAction(LTSEvent e) {
        switch (e.kind) {
            case LTSEvent.NEWSTATE:
                prevEvent = lastEvent;
                lastEvent = (int[]) e.info;
                lastName = e.name;
                buttonHighlight(lastName);
                if (lastName != null) new_transitions(lastName, prevEvent, lastEvent);
                break;
            case LTSEvent.INVALID:
//				prevEvent = null;
//				lastEvent = null;
                new_machines(cs = (CompositeState) e.info); //info holds the new composite state
                break;
            case LTSEvent.KILL:
                break;
            default:
        }
    }
*/
    /*
     * Retrieves the newly highlighted transition and gives it to the canvas
     */
    private void new_transitions(String label, int[] from, int[] to) {
        final Set<ExperimentJUNGTransitionEdge> transitions = new HashSet<ExperimentJUNGTransitionEdge>();
        final Set<ExperimentJUNGStateVertex> outStates = new HashSet<ExperimentJUNGStateVertex>();

        for (int i = 0; i < sm.length - hasC; i++) {
            final ExperimentJUNGGraph g = makeGraph(i);
            if (machineHasAction[i]) {
                final ExperimentJUNGTransitionEdge e = g.getTransitionFromLabel(label, from != null ? from[i] : 0, to != null ? to[i] : 0);
                if (e != null) {
                    transitions.add(e);
                    //outStates.add(graphs[i].getDest(e));
                }
            }
            outStates.add(g.getStateFromNumber(to != null ? to[i] : 0));
        }

        output.select(transitions, outStates);
    }

    /*
     * Turn a machine into a JUNG graph
     */
    private ExperimentJUNGGraph makeGraph(int machine) {
        if (graphValidity != null && graphValidity[machine]) {
            return graphs[machine];
        } else {
            final ReportAutomaton automaton = sm[machine];

            final ExperimentJUNGGraph g = new ExperimentJUNGGraph(automaton);

            graphValidity[machine] = true;
            graphs[machine] = g;
            return g;
        }
    }

    protected boolean hasLabel(ReportAutomaton automaton, String label) {
    	for(int i : automaton.getLocalAlphabet()) {
    		if(getLabel(automaton, i).equals(label))
    			return true;
    	}
		return false;
	}
    
	protected String getLabel(ReportAutomaton automaton, int labelLocalIndex) {
		return automaton.getContext().getAlphabet().getSignals().get(automaton.getLocalAlphabet().get(labelLocalIndex)).getName();
	}
    
    private void buttonHighlight(String label) {
        if (label == null && machineHasAction != null) {
            for (int i = 0; i < machineHasAction.length; i++)
                machineHasAction[i] = false;
        } else if (machineHasAction != null) {
            for (int i = 0; i < sm.length - hasC; i++)
                machineHasAction[i] = hasLabel( sm[i],label);
        }
        list.repaint();
        return;
    }

    /*
     * Renew the members with a CompositeState
     */
    @SuppressWarnings("unchecked")
    private void new_machines() {
        hasC = 0; //(cs != null && cs.composition != null) ? 1 : 0;
        Nmach = sm.length;
		machineHasAction = new boolean[Nmach];
		machineToDrawSet = new boolean[Nmach];
		machineLayout = new HashMap<Integer, EnumLayout>();
		graphValidity = new boolean[Nmach];
		
		graphs = new ExperimentJUNGGraph[Nmach];

        DefaultListModel<String> lm = new DefaultListModel<String>();
        for (int i = 0; i < Nmach; i++) {
            if (hasC == 1 && i == (Nmach - 1))
                lm.addElement("||" + sm[i].getName());
            else
                lm.addElement(sm[i].getName());
        }
        list.setModel(lm);

        output.clear();
    }

    //-----------------------------------------------------------------------------
//	Parameter setters
//-----------------------------------------------------------------------------
    public void setBigFont(boolean b) {
        fontFlag = b;
        //output.setBigFont(b);
    }

    public void setDrawName(boolean b) {
        //output.setDrawName(b);
    }

    public void setNewLabelFormat(boolean b) {
        //output.setNewLabelFormat(b);
    }

    public void setMode(boolean b) {
        singleMode = b;
        output.setMode(b);

        if (machineLayout != null)
            machineLayout.clear();

        list.clearSelection();
        if (Nmach > 0) {
            machineToDrawSet = new boolean[Nmach];
        }
        list.repaint();
    }


    public ExperimentJUNGCanvas getCanvas() {
        return output;
    }

//-----------------------------------------------------------------------------
// File saver
//-----------------------------------------------------------------------------

    public void saveFile() {
        final Object[] possibilities = {"png"};
        final String s = (String) JOptionPane.showInputDialog(
                getParent(),
                "Choose a format",
                "Save Automaton",
                JOptionPane.PLAIN_MESSAGE,
                null,
                possibilities,
                "png");

        if ((s != null) && (s.length() > 0)) {
            try {
                final JFileChooser fc = new JFileChooser();
                fc.setSelectedFile(new File("output." + s));
                int returnVal = fc.showSaveDialog(getTopLevelAncestor());
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    if (s.equals("png")) {
                        savePng(file);
                    }
                }
            } catch (IOException ioe) {
            }
        }

    }

    private void savePng(File f) throws IOException {
        final BufferedImage bi = output.getImage();
        ImageIO.write(bi, "png", f);
    }

}