package view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import controller.World;
import main.Ecologia;
import main.EcologiaIO;

/**
 * This class is used to graphically configure simulation parameters 
 * prior to the start of a run.
 * 
 * @author Daniel Vedder
 * @version 30.12.2014
 */
public class SimulationConfig extends JFrame
{
	private Box mainBox;
	private JLabel heading, dimensions, waterLabel, nCarnLabel, nHerbLabel, grassLabel, energyCarnLabel, energyHerbLabel;
	private JTextField width, height, no_water_tiles, no_carnivores, no_herbivores, grassDensity, energyHerbivores, energyCarnivores;
	private JButton confirm;
	private boolean showRestartDialog;
	
	/**
	 * The constructor
	 */
	public SimulationConfig()
	{
		this.setTitle("Simulation Configuration");
		this.setSize(320, 320);
		this.setLocation(400,150);
		this.setDefaultCloseOperation(HIDE_ON_CLOSE);
		drawConfigWindow();
	}
	
	/**
	 * Create the interface
	 */
	private void drawConfigWindow()
	{
		mainBox = new Box(BoxLayout.Y_AXIS);
		heading = new JLabel("Initial Parameter Settings");
		//Dimension settings
		dimensions = new JLabel("World dimensions (x*y): ");
		width = new JTextField(String.valueOf(World.getInstance().getSize()[0]), 4);
		height = new JTextField(String.valueOf(World.getInstance().getSize()[1]), 4);
		Box dimBox = new Box(BoxLayout.X_AXIS);
		dimBox.add(dimensions);
		dimBox.add(Box.createHorizontalStrut(15));
		dimBox.add(width);
		dimBox.add(Box.createHorizontalStrut(15));
		dimBox.add(height);
		//Initial numbers of animals and water tiles
		Box waterBox = new Box(BoxLayout.X_AXIS);
		waterLabel = new JLabel("Number of water tiles: ");
		no_water_tiles = new JTextField(String.valueOf(World.getInstance().getWaterTiles()), 3);
		waterBox.add(waterLabel);
		waterBox.add(Box.createHorizontalStrut(30));
		waterBox.add(no_water_tiles);
		Box grassBox = new Box(BoxLayout.X_AXIS);
		grassLabel = new JLabel("Starting grass density: ");
		grassDensity = new JTextField(String.valueOf(World.getInstance().getStartGrassDensity()), 4);
		grassBox.add(grassLabel);
		grassBox.add(Box.createHorizontalStrut(25));
		grassBox.add(grassDensity);
		Box nCarnBox = new Box(BoxLayout.X_AXIS);
		nCarnLabel = new JLabel("Number of carnivores: ");
		no_carnivores = new JTextField(String.valueOf(World.getInstance().getStartNoCarnivores()), 3);
		nCarnBox.add(nCarnLabel);
		nCarnBox.add(Box.createHorizontalStrut(25));
		nCarnBox.add(no_carnivores);
		Box nHerbBox = new Box(BoxLayout.X_AXIS);
		nHerbLabel = new JLabel("Number of herbivores: ");
		no_herbivores = new JTextField(String.valueOf(World.getInstance().getStartNoHerbivores()), 3);
		nHerbBox.add(nHerbLabel);
		nHerbBox.add(Box.createHorizontalStrut(25));
		nHerbBox.add(no_herbivores);
		//Initial energy for the animals
		Box energyCarnBox = new Box(BoxLayout.X_AXIS);
		energyCarnLabel = new JLabel("Start energy carnivores: ");
		energyCarnivores = new JTextField(String.valueOf(World.getInstance().getStartEnergyCarnivores()), 4);
		energyCarnBox.add(energyCarnLabel);
		energyCarnBox.add(Box.createHorizontalStrut(25));
		energyCarnBox.add(energyCarnivores);
		Box energyHerbBox = new Box(BoxLayout.X_AXIS);
		energyHerbLabel = new JLabel("Start energy herbivores: ");
		energyHerbivores = new JTextField(String.valueOf(World.getInstance().getStartEnergyHerbivores()), 4);
		energyHerbBox.add(energyHerbLabel);
		energyHerbBox.add(Box.createHorizontalStrut(25));
		energyHerbBox.add(energyHerbivores);
		//The confirm button
		confirm = new JButton("Confirm");
		confirm.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
              EcologiaIO.log("SimulationConfig: World parameter settings updated.");
        	  if (showRestartDialog) {
        		  int restart = JOptionPane.showConfirmDialog(null, 
        				  "Please note: The new settings will only take \neffect on the next run.\nRestart now?", "Restart?", 
        						  JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        		  if (restart != JOptionPane.CANCEL_OPTION) updateWorld();
        		  if (restart == JOptionPane.YES_OPTION) Ecologia.getInstance().reset();
        	  }
        	  setVisible(false);
          }
        });
		//Draw everything
		mainBox.add(heading);
		mainBox.add(Box.createVerticalStrut(5));
		mainBox.add(new JSeparator());
		mainBox.add(Box.createVerticalStrut(5));
		mainBox.add(dimBox);
		mainBox.add(Box.createVerticalStrut(10));
		mainBox.add(grassBox);
		mainBox.add(Box.createVerticalStrut(10));
		mainBox.add(waterBox);
		mainBox.add(Box.createVerticalStrut(10));
		mainBox.add(nCarnBox);
		mainBox.add(Box.createVerticalStrut(10));
		mainBox.add(nHerbBox);
		mainBox.add(Box.createVerticalStrut(10));
		mainBox.add(energyCarnBox);
		mainBox.add(Box.createVerticalStrut(10));
		mainBox.add(energyHerbBox);
		mainBox.add(Box.createVerticalStrut(10));
		mainBox.add(confirm);
		this.add(mainBox, BorderLayout.CENTER);
		this.add(new JPanel(), BorderLayout.NORTH);
		this.add(new JPanel(), BorderLayout.EAST);
		this.add(new JPanel(), BorderLayout.SOUTH);
		this.add(new JPanel(), BorderLayout.WEST);		
	}
	
	/**
	 * Show the configuration window
	 * @param showRestart Show the restart dialog when closing this window?
	 */
	public void showConfig(boolean showRestart)
	{
		EcologiaIO.debug("SimulationConfig: showing config window.");
		showRestartDialog = showRestart;
		refresh();
		this.setVisible(true);
	}
	
	/**
	 * Refresh values displayed in the text fields.
	 */
	public void refresh()
	{
		width.setText(String.valueOf(World.getInstance().getSize()[0]));
		height.setText(String.valueOf(World.getInstance().getSize()[1]));
		grassDensity.setText(String.valueOf(World.getInstance().getStartGrassDensity()));
		no_water_tiles.setText(String.valueOf(World.getInstance().getWaterTiles()));
		no_carnivores.setText(String.valueOf(World.getInstance().getStartNoCarnivores()));
		no_herbivores.setText(String.valueOf(World.getInstance().getStartNoHerbivores()));
		energyCarnivores.setText(String.valueOf(World.getInstance().getStartEnergyCarnivores()));
		energyHerbivores.setText(String.valueOf(World.getInstance().getStartEnergyHerbivores()));
	}
	
	/**
	 * Extract all the settings from the text fields and update the world parameters
	 */
	public void updateWorld()
	{
		World.getInstance().setSize(new int[] {new Integer(width.getText()), new Integer(height.getText())});
		World.getInstance().setStartGrassDensity(new Integer(grassDensity.getText()));
		World.getInstance().setStartNoWaterTiles(new Integer(no_water_tiles.getText()));
		World.getInstance().setStartNoHerbivores(new Integer(no_herbivores.getText()));
		World.getInstance().setStartNoCarnivores(new Integer(no_carnivores.getText()));
		World.getInstance().setStartEnergyCarnivores(new Integer(energyCarnivores.getText()));
		World.getInstance().setStartEnergyHerbivores(new Integer(energyHerbivores.getText()));
	}
}
