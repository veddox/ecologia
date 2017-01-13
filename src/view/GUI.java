package view;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.*;

import controller.Humidity;
import controller.World;
import main.*;

/**
 * This class is the main class of the view package. It combines all the different
 * GUI components required for the programme.
 * 
 * @author Daniel Vedder
 * @version 29.8.2014
 */
public class GUI extends JFrame
{
	private static final long serialVersionUID = 4727895060816956404L;
	private Box information;
	private JMenuBar menubar;
	private JMenu file, configuration, help_menu;
	private JMenuItem new_run, exit, programConfigBox, simConfigBox, genomeConfigBox, configFileDialog, help, about;
	private JLabel update_counter, herbivore_counter, carnivore_counter, generation_counter, grass_counter;
	private JComboBox<String> humidityChooser;
	private JTextArea ticker; //XXX Remove this at some point? - Expensive?
	private JTextField stopAtField;
	private JCheckBox disableDisplay;
	private JScrollPane scrollticker, scrollscreen;
	private JButton run, next;
	private JSlider speedSlider;
	private Display display;
	private ProgramConfig programConfig;
	private SimulationConfig simulationConfig;
	private GenomeConfig genomeConfig;
	private JFileChooser configChooser;
	private HelpWindow helpWindow;
	
	/**
	 * The constructor.
	 */
	public GUI()
	{
		EcologiaIO.debug("Creating GUI");
		this.setTitle("Ecologia "+Ecologia.version);
		this.setSize(1000, 560);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		createMenu();
		addInformationPanel();
		addDisplay();
		programConfig = new ProgramConfig();
		simulationConfig = new SimulationConfig();
		genomeConfig = new GenomeConfig();
        configChooser = new JFileChooser(System.getProperty("user.dir"));
		helpWindow = new HelpWindow();
		this.setVisible(true);
	}
	
	/**
	 * Update the GUI.
	 */
	public synchronized void update()
	{
		EcologiaIO.debug("GUI: updating display.");
		//Update the display
		if (!disableDisplay.isSelected()) {
			display.update();
		}
		displayNews();
		//Make sure the "run" button is displaying the right text
		if (World.getInstance().isRunning()) run.setText("Stop");
		else run.setText("Start");
		//Update the humidity from the combo box
		Humidity setHumidity = Humidity.fromString((String) humidityChooser.getSelectedItem());
		if (setHumidity.getValue() != World.getInstance().getParam("humidity")) {
			World.getInstance().setParam("humidity", setHumidity.getValue());
			EcologiaIO.log("Humidity set to "+setHumidity.getString());
		}
		//Update the simulation speed from the speed slider
		int setSpeed = speedSlider.getMaximum() - speedSlider.getValue();
		World.getInstance().setParam("timelapse", setSpeed);
		//Update the stopAt variable from user input
		try {
			World.getInstance().setParam("stopAt", Integer.parseInt((stopAtField.getText())));
		}
		catch (NumberFormatException nfe) {}
		//Update the various counters
		update_counter.setText("Updates: "+ World.getInstance().getTurn());
		herbivore_counter.setText("Herbivores: "+ World.getInstance().getHerbivoreCount());
		carnivore_counter.setText("Carnivores: "+ World.getInstance().getCarnivoreCount());
		generation_counter.setText("Generations: "+World.getInstance().getGeneration());
		grass_counter.setText("Grass density: "+World.getInstance().getAverageGrassDensity());
		humidityChooser.setSelectedItem(Humidity.getStatus(World.getInstance().getParam("humidity")).getString());
	}
	
	/**
	 * Add the menubar
	 */
	private void createMenu()
	{
		EcologiaIO.debug("GUI: creating menubar.");
		menubar = new JMenuBar();
		file = new JMenu("File");
		configuration = new JMenu("Configuration");
		help_menu = new JMenu("Help");
		new_run = new JMenuItem("New Run");
		exit = new JMenuItem("Exit");
		programConfigBox = new JMenuItem("Ecologia");
		simConfigBox = new JMenuItem("Simulation");
		genomeConfigBox = new JMenuItem("Genomes");
		configFileDialog = new JMenuItem("Configuration file");
		help = new JMenuItem("Help");
		about = new JMenuItem("About");
		menubar.add(file);
		menubar.add(configuration);
		menubar.add(help_menu);
		file.add(new_run);
		file.add(exit);
        new_run.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
        	  int restart = JOptionPane.showConfirmDialog(null, "Restart now?", "Restart?",
														  JOptionPane.OK_CANCEL_OPTION,
														  JOptionPane.QUESTION_MESSAGE);
        	  if (restart == JOptionPane.OK_OPTION) Ecologia.getInstance().reset();
          }
        });
        new_run.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK));
        exit.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
        	  int confirm = JOptionPane.showConfirmDialog(null, "Quit Ecologia?", "Quit?",
														  JOptionPane.YES_NO_OPTION,
														  JOptionPane.WARNING_MESSAGE);
        	  if(confirm == JOptionPane.YES_OPTION){
        		  EcologiaIO.log("Stopping Ecologia.");
        		  System.exit(0);
        	  }
          }
        });
        exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.ALT_MASK));
		configuration.add(programConfigBox);
		configuration.add(simConfigBox);
		configuration.add(genomeConfigBox);
		configuration.add(configFileDialog);
		programConfigBox.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
              programConfig.showConfig();
          }
        });
        simConfigBox.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
              simulationConfig.showConfig(true);
          }
        });
        genomeConfigBox.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
              genomeConfig.showGenomeConfig(true);
          }
        });
        configFileDialog.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
              int returnVal = configChooser.showDialog(null, "Load config file");
              if (returnVal == JFileChooser.APPROVE_OPTION) {
            	  int restart = JOptionPane.showConfirmDialog(null, 
            			  "Please note: Loading a config file requires a restart.\nRestart now?", 
            			  "Restart?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            	  if (restart == JOptionPane.YES_OPTION) {
                	  World.getInstance().readConfigFile(configChooser.
                			  getSelectedFile().getAbsolutePath());
            		  Ecologia.getInstance().reset();
            	  }
              }
          }
        });
		help_menu.add(help);
		help.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
              helpWindow.setVisible(true);
          }
        });
        help.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.ALT_MASK));
		help_menu.add(about);
        about.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
              JOptionPane.showMessageDialog(null, "Ecologia "+Ecologia.version+
											"\n(c) 2014 - 2016 Daniel Vedder\nLicensed under the GPLv3", 
											"About", JOptionPane.INFORMATION_MESSAGE);
          }
        });
		this.setJMenuBar(menubar);
	}
	
	/**
	 * Add the information panel at the side
	 */
	private void addInformationPanel()
	{
		EcologiaIO.debug("GUI: creating information panel.");
		//Configure the main information panel
		information = new Box(BoxLayout.Y_AXIS);
		this.add(information, BorderLayout.EAST);
		information.setBackground(Color.lightGray);
		//Add the counters at the top
		update_counter = new JLabel("Updates: "+ World.getInstance().getTurn());
		herbivore_counter = new JLabel("Herbivores: "+ World.getInstance().getHerbivoreCount());
		carnivore_counter = new JLabel("Carnivores: "+ World.getInstance().getCarnivoreCount());
		generation_counter = new JLabel("Generations: "+World.getInstance().getGeneration());
		grass_counter = new JLabel("Grass density: "+World.getInstance().getAverageGrassDensity());
		information.add(update_counter);
		information.add(Box.createVerticalStrut(3));
		information.add(herbivore_counter);
		information.add(Box.createVerticalStrut(3));
		information.add(carnivore_counter);
		information.add(Box.createVerticalStrut(3));
		information.add(generation_counter);
		information.add(Box.createVerticalStrut(3));
		information.add(grass_counter);
		information.add(Box.createVerticalStrut(3));
		//Add the event ticker
		ticker = new JTextArea();
		ticker.setEditable(false);
		ticker.setLineWrap(true);
		ticker.setWrapStyleWord(true);
		ticker.setText(" --- Runtime Protocol ---");
		scrollticker = new JScrollPane(ticker);
		scrollticker.setWheelScrollingEnabled(true);
		information.add(scrollticker);
		information.add(Box.createVerticalStrut(10));
		//Add the humidity chooser
		Box hum_panel = new Box(BoxLayout.X_AXIS);
		JLabel humidity = new JLabel("Humidity: ");
		humidityChooser = new JComboBox<String>(new String[] 
				{Humidity.SATURATION.getString(), Humidity.WET.getString(), Humidity.DRY.getString(), 
				Humidity.DROUGHT.getString(), Humidity.SEVERE_DROUGHT.getString()});
		humidityChooser.setMaximumSize(new Dimension(140, 30));
		humidityChooser.setSelectedItem(Humidity.getStatus(World.getInstance().getParam("humidity")).getString());
		hum_panel.add(humidity);
		hum_panel.add(humidityChooser);
		information.add(hum_panel);
		information.add(Box.createVerticalStrut(10));
		//Add the "Start/Stop" and "Next" buttons
		Box buttonPanel = new Box(BoxLayout.X_AXIS);
		run = new JButton("Start");
		//This button starts or pauses the simulation.
        run.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
        	  if (World.getInstance().isRunning() == false) {
        		  Ecologia.getInstance().startThread();
        	  }
        	  else {
        		  run.setText("Start");
        		  World.getInstance().setRunning(false);
        	  }
          }
        });
		next = new JButton("Next ");
		//This button advances the simulation by one update.
        next.addActionListener(new ActionListener() 
        {
          public void actionPerformed(ActionEvent e)
          {
              Ecologia.getInstance().iterate();
          }
        });
		buttonPanel.add(Box.createVerticalStrut(1));
		buttonPanel.add(run);
		buttonPanel.add(Box.createVerticalStrut(1));
		buttonPanel.add(next);
		buttonPanel.add(Box.createVerticalStrut(1));
		information.add(buttonPanel);
		information.add(Box.createVerticalStrut(10));
		//Add the simulation speed slider
		information.add(new JLabel("Simulation speed:"));
		information.add(Box.createVerticalStrut(3));
		speedSlider = new JSlider(0, 1500, 1500-World.getInstance().getParam("timelapse"));
		speedSlider.setMajorTickSpacing(300);
		speedSlider.setMinorTickSpacing(50);
		speedSlider.setPaintTicks(true);
		speedSlider.setSnapToTicks(true);
		information.add(speedSlider);
		information.add(Box.createVerticalStrut(10));
		//Add the "Pause at update:" function
		Box stopPanel = new Box(BoxLayout.X_AXIS);
		JLabel stopLabel = new JLabel("Pause at update:");
		stopAtField = new JTextField(5);
		stopAtField.setMaximumSize(stopAtField.getPreferredSize());
		stopAtField.setText(Integer.toString(World.getInstance().getParam("stopAt")));
		stopPanel.add(Box.createVerticalStrut(3));
		stopPanel.add(stopLabel);
		stopPanel.add(Box.createVerticalStrut(1));
		stopPanel.add(stopAtField);
		stopPanel.add(Box.createVerticalStrut(3));
		stopPanel.setMaximumSize(stopPanel.getPreferredSize());
		information.add(stopPanel);
		information.add(Box.createVerticalStrut(10));
		//Add the disable display check box
		disableDisplay = new JCheckBox("Freeze display");
		information.add(disableDisplay);
		information.add(Box.createVerticalStrut(10));
	}
	
	/**
	 * Add the actual display.
	 */
	private void addDisplay()
	{
		display = new Display(new int[] {World.getInstance().getParam("xsize"),
							   World.getInstance().getParam("ysize")});
		scrollscreen = new JScrollPane(display, JScrollPane. VERTICAL_SCROLLBAR_ALWAYS,
									   JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		this.add(scrollscreen, BorderLayout.CENTER);
	}

	/**
	 * Destroy all windows in preparation for a new run.
	 */
	public void reset()
	{
		EcologiaIO.debug("Resetting the GUI.");
		programConfig.dispose();
		simulationConfig.dispose();
		genomeConfig.dispose();
		helpWindow.dispose();
		display.getInfoBox().dispose();
		this.dispose();
	}
	
	/**
	 * Display news items on the ticker
	 */
	public void displayNews()
	{
		EcologiaIO.debug("GUI: updating news.");
		ArrayList<String> news = World.getInstance().collectNews();
		if (!news.isEmpty()) {
			for (int i = 0; i < news.size(); i++) {
				ticker.append("\n"+news.get(i));
			}
			World.getInstance().giveNews(null); //reset the news list
			ticker.setCaretPosition(ticker.getText().length());
		}
	}
	
}
