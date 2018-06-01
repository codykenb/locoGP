package experimental.loader.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import de.uka.ipd.sdq.ByCounter.instrumentation.EntityToInstrument;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentationCounterPrecision;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentationParameters;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentedMethod;
import de.uka.ipd.sdq.ByCounter.utils.Barrier;
import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;

/**
 * Graphical user interface for the selection of methods to instrument.
 * Instead of manually extracting and parsing the classes, we shall use
 * Eclipse facilities.
 *
 * @author Martin Krogmann
 * @author Michael Kuperberg
 * @since 0.9
 * @version 1.2
 *
 */
public class ByLoaderGUI extends JFrame {

	private static final String DEFAULT_CLASS_NAME_TO_INSTRUMENT = "de.uka.ipd.sdq.ByCounter.example.ByCounterExample"; //"spec.benchmarks.compress.Compressor";

	private static final String DEFAULT_METHOD_NAME_TO_INSTRUMENT = "public static int dummyMethodToBeInstrumented(java.lang.String str, float f)"; //"public void compress()";

	private static Logger log = Logger.getLogger("de.uka.ipd.sdq.ByLoaderGUI");

	final static int maxGap = 20;

	private static final long serialVersionUID = -3895045635925689009L;

	/**
	 * Main method used for development purposes.
	 *
	 * @param args
	 *            Not used.
	 */
	public static void main(String[] args) {
		//Schedule a job for the event dispatch thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				(new ByLoaderGUI(null)).createAndShowGUI();
			}
		});
	}

	/**
	 * Sets the swing applications look and feel to native.
	 */
	public static void setNativeLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.out.println("Error setting native LAF: " + e);
		}
	}
	JButton addButton;
	private Barrier byCounterBarrier;
	JTree classSelectionTree;
	TreeModel classSelectionTreeModel;
	JButton exitButton;
	JTextField methodDescriptorFieldClass;
	JTextField methodDescriptorFieldSignature;
	JList methodList;
	DefaultListModel methodListModel;
	JCheckBox optArrayParameterCheckBox;
	JCheckBox optHighRegistersCheckBox;
	JCheckBox optInstrumentFromFS;
	ButtonGroup optPrecisionGroup;
	JRadioButton optPrecisionInt;
	JRadioButton optPrecisionLong;
	JCheckBox optResultLogCheckBox;

	JTextField optResultLogFileTextField;
	JButton removeButton;

	JButton startButton;

	/**
	 * Constructs the gui. Call <code>createAndShowGUI()</code> to display it.
	 * @param barrier The barrier that is released when the user selects
	 * 'start'.
	 */
//	 * @param bcft The {@link ByClassFileTransformer} that is informed of the
//	 * methods to instrument selected using this interface.
	public ByLoaderGUI(Barrier barrier) {
		super("ByCounter - Loader");
		this.byCounterBarrier = barrier;
		setNativeLookAndFeel();
	}

	public void addComponentsToPane(final Container pane) {
		JPanel controls = new JPanel();
		controls.setLayout(new GridBagLayout());

		this.startButton = new JButton("Start Application");
		this.methodListModel = new DefaultListModel();
		this.methodListModel.add(
				0,
				new MethodDescriptor(DEFAULT_CLASS_NAME_TO_INSTRUMENT,
				DEFAULT_METHOD_NAME_TO_INSTRUMENT));
		this.methodList = new JList(this.methodListModel);
		// make sure only one item can be selected at a time
		this.methodList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.addButton = new JButton("Add");
		this.removeButton = new JButton("Remove");
		this.exitButton = new JButton("Exit");
		this.methodDescriptorFieldClass = new JTextField();
		this.methodDescriptorFieldSignature = new JTextField();
		// setup the tree view for the selection of classes
		DefaultMutableTreeNode classSelectionTreeRoot = new DefaultMutableTreeNode("root");
		generateClassTree(classSelectionTreeRoot);
		this.classSelectionTreeModel = new DefaultTreeModel(classSelectionTreeRoot);
		this.classSelectionTree = new JTree(this.classSelectionTreeModel);

		// reusable constraint variable to setup all controls
		GridBagConstraints c = new GridBagConstraints();

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.weightx = 0.5;
		controls.add(new JLabel("Please specify the methods to instrument."), c);


		this.optInstrumentFromFS = new JCheckBox("Use instrumented files from filesystem. (Ignores all of the following options)", false);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		controls.add(this.optInstrumentFromFS, c);


		// Methoddescriptor group:
		JPanel methodDescriptorPanel = new JPanel();
		methodDescriptorPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Method descriptor (all classes fully qualified):"),
                BorderFactory.createEmptyBorder(5,5,5,5)));
		methodDescriptorPanel.setLayout(new GridBagLayout());
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy++;
		methodDescriptorPanel.add(new JLabel("Class:"), c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy++;
		methodDescriptorPanel.add(this.methodDescriptorFieldClass, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy++;
		methodDescriptorPanel.add(new JLabel("Method signature:"), c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy++;
		methodDescriptorPanel.add(this.methodDescriptorFieldSignature, c);
		JScrollPane treeScroller = new JScrollPane(this.classSelectionTree);
		treeScroller.setMinimumSize(new Dimension(590, 200));
		//c.fill = GridBagConstraints.HORIZONTAL;
		//c.gridx = 0;
		//c.gridy++;
		//methodDescriptorPanel.add(treeScroller, c);//TODO document this almost-done thing!!!
		controls.add(methodDescriptorPanel, c);

		// the methods to instrument list and label:
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy++;
		controls.add(new JLabel("The following methods will be instrumented:"), c);

		JScrollPane listScroller = new JScrollPane(this.methodList);
		listScroller.setMinimumSize(new Dimension(590, 200));
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 0.5;
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		controls.add(listScroller, c);
		c.weighty = 0;

		// add and remove buttons:
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 1;
		this.addButton.setMnemonic('a');
		controls.add(this.addButton, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		this.removeButton.setMnemonic('r');
		controls.add(this.removeButton, c);


		// Options:
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		JPanel optionsPanel = new JPanel();
        optionsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Options"),
                BorderFactory.createEmptyBorder(5,5,5,5)));
        optionsPanel.setLayout(new BoxLayout(optionsPanel,
                BoxLayout.PAGE_AXIS));

        this.optArrayParameterCheckBox = new JCheckBox("Use array parameter recording", false);
        this.optHighRegistersCheckBox = new JCheckBox("Use high registers as counters", true);
        optionsPanel.add(this.optArrayParameterCheckBox);
        optionsPanel.add(this.optHighRegistersCheckBox);

        this.optPrecisionGroup = new ButtonGroup();
        this.optPrecisionInt = new JRadioButton("Use int counter precision.", false);
        this.optPrecisionLong = new JRadioButton("Use long counter precision.", true);
        this.optPrecisionGroup.add(this.optPrecisionInt);
        this.optPrecisionGroup.add(this.optPrecisionLong);
        optionsPanel.add(this.optPrecisionInt);
        optionsPanel.add(this.optPrecisionLong);

        this.optResultLogCheckBox = new JCheckBox("Write results to log file(s).", false);
        this.optResultLogCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					ByLoaderGUI.this.optResultLogFileTextField.setEnabled(true);
					ByLoaderGUI.this.optResultLogFileTextField.setEditable(true);
				} else if (e.getStateChange() == ItemEvent.DESELECTED) {
					ByLoaderGUI.this.optResultLogFileTextField.setEnabled(false);
					ByLoaderGUI.this.optResultLogFileTextField.setEditable(false);
				}
			}

        });
        this.optResultLogFileTextField = new JTextField(
        		InstrumentationParameters.RESULT_LOG_DEFAULT_PREFIX);
        this.optResultLogFileTextField.setEnabled(false);
        this.optResultLogFileTextField.setEditable(false);
        optionsPanel.add(this.optResultLogCheckBox);
        optionsPanel.add(this.optResultLogFileTextField);
        optionsPanel.add(new JLabel("The following strings will be replaced:"));
        optionsPanel.add(new JLabel("{$TIME} - The System.nanoTime() value returned when instrumenting the method."));
        optionsPanel.add(new JLabel("{$CLASSNAME} - The fully qualified class name for the instrumented method."));
        optionsPanel.add(new JLabel("{$METHODNAME} - The method name of the instrumented method."));
        optionsPanel.add(new JLabel("{$METHODDESC} - The method descriptor of the instrumented method."));

        controls.add(optionsPanel, c);


        // start and exit buttons
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 1;
		this.exitButton.setMnemonic('x');
		controls.add(this.exitButton, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		this.startButton.setMnemonic('s');
		controls.add(this.startButton, c);


		// Process selection change in method list
		this.methodList.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				int index = ByLoaderGUI.this.methodList.getSelectedIndex();
				if(index >= 0) {
					MethodDescriptor d =
						(MethodDescriptor)ByLoaderGUI.this.methodListModel.getElementAt(index);
					ByLoaderGUI.this.methodDescriptorFieldClass.setText(
							d.getCanonicalClassName());
//					methodDescriptorFieldSignature.setText(
//							".." + d.getMethodName() + "..");
				}else{
					//TODO
				}
			}

		});

		// Process the start button press
		this.startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				log.fine("Instrumentation param before releasing the barrier: "+
						getInstrumentationParameters());
				// make sure ByCounter can continue
				log.fine("releasing barrier");
				ByLoaderGUI.this.byCounterBarrier.release();
				setVisible(false);
			}
		});
		// Process the add button press
		this.addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(ByLoaderGUI.this.methodDescriptorFieldClass.getText().length() > 0
						&& ByLoaderGUI.this.methodDescriptorFieldSignature.getText().length() > 0) {
					MethodDescriptor d = new MethodDescriptor(
							ByLoaderGUI.this.methodDescriptorFieldClass.getText(),
							ByLoaderGUI.this.methodDescriptorFieldSignature.getText());
					if(d.getDescriptor() != null) {
						ByLoaderGUI.this.methodListModel.addElement(d);
					} else {
						log.severe("Invalid method descriptor");
					}
				} else {
					log.severe("Empty field.");
					// TODO: display a messsage
				}
				log.fine(methodListModel.toString());
			}

		});
		// Process the remove button press
		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = methodList.getSelectedIndex();
				if(index >= 0) {
					methodListModel.removeElementAt(index);
				}else{
					//TODO
				}
			}

		});
		// Process the exit button press
		exitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}

		});
		pane.add(controls);
	}

	/**
	 * Create the GUI and show it.  For thread safety,
	 * this method is invoked from the
	 * event dispatch thread.
	 */
	public void createAndShowGUI() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//Set up the content pane.
		this.addComponentsToPane(this.getContentPane());
		//Display the window.
		this.pack();
		this.setVisible(true);
	}

	/**
	 * Generates a tree of all found packages and their classes.
	 * @param root The root element to which the tree is added.
	 */
	@SuppressWarnings("static-access")
//	 * @param prefix The package prefix in which the packages/classes are
//	 * searched.
	private void generateClassTree(DefaultMutableTreeNode root) {
		Package[] packages = null;
		packages = Package.getPackages();
		Enumeration<URL> URLs = null;
		try {
			URLs = ClassLoader.getSystemClassLoader().getSystemResources("de");
		} catch (IOException e) {
			e.printStackTrace();
		}
		for(int i = 0; i < packages.length; i++) {
			//TODO
		}
		if(URLs==null)
		{
			return;
		}
		URLs.hashCode();
		ArrayList<URL> stuff = new ArrayList<URL>();
		while(URLs.hasMoreElements()) {
			stuff.add(URLs.nextElement());
		}
		Comparator<Package> packageComparator = new Comparator<Package>() {
			public int compare(Package p1, Package p2) {
				return p1.getName().compareTo(p2.getName());
			}
		};
		Arrays.sort(packages, packageComparator);

		DefaultMutableTreeNode node = null;
		for(int i = 0; i < packages.length; i++) {
			node = new DefaultMutableTreeNode(packages[i]);
			root.add(node);
		}
	}


	/**
	 * Gets the {@link InstrumentationParameters} as specified in the GUI.
	 * @return Returns null, if instrumented files from the filesystem shall
	 * be used.
	 * Otherwise the methods to instrument and instrumentation options as
	 * {@link InstrumentationParameters}.
	 */
	public synchronized InstrumentationParameters getInstrumentationParameters() {

		if(optInstrumentFromFS.isSelected()) {
			log.fine("optInstrumentFromFS is selected; exiting");
			return null;
		}

		InstrumentationParameters params;
		ArrayList<EntityToInstrument> methods =
			new ArrayList<EntityToInstrument>(methodListModel.getSize());
		for(int i = 0; i < methodListModel.getSize(); i++) {
			methods.add(new InstrumentedMethod((MethodDescriptor)methodListModel.elementAt(i)));
		}
		params = new InstrumentationParameters(methods, true, true, false, false, InstrumentationCounterPrecision.Long);
		log.fine("Instrumentation params after adding methods, before setting conf: "+params);

		// set options
		// precision:
		if(optPrecisionInt.isSelected()) {
			params.setCounterPrecision(InstrumentationCounterPrecision.Integer);
		} else if(optPrecisionLong.isSelected()) {
			params.setCounterPrecision(InstrumentationCounterPrecision.Long);
		}

		// use of result log:
		if(optResultLogCheckBox.isSelected()) {
			params.setUseResultCollector(false);
			params.enableResultLogWriter(optResultLogFileTextField.getText());
		} else {
			params.setUseResultCollector(true);
			params.disableResultLogWriter();
		}

		// record array params
		params.setUseArrayParameterRecording(optArrayParameterCheckBox.isSelected());

		// use high registers
		params.setUseHighRegistersForCounting(optHighRegistersCheckBox.isSelected());

//		params.setStartLine(startLine); //TODO :-)
//		params.setStopLine(stopLine); //TODO :-)
		return params;
	}
}