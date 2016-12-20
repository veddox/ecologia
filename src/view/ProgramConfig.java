package view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;

import javax.swing.*;

import main.Ecologia;
import main.EcologiaIO;
import controller.World;

/**
 * This class provides a GUI to configure program options (these can
 * also be set via commandline flags).
 * 
 * @author Daniel Vedder
 * @version 22.03.2015
 */
public class ProgramConfig extends JFrame
{
	private Box mainBox;
	private JLabel heading;
	private JCheckBox logging, debug, verbose, analyse;
	private JButton apply;
	
	/**
	 * The constructor
	 */
	public ProgramConfig()
	{
		this.setTitle("Program Configuration");
		this.setSize(250, 230);
		this.setLocation(300,200);
		this.setDefaultCloseOperation(HIDE_ON_CLOSE);
		drawConfigWindow();
	}

	private void drawConfigWindow()
	{
		mainBox = new Box(BoxLayout.Y_AXIS);
		heading = new JLabel("Initial Parameter Settings");
		mainBox.add(heading);
		mainBox.add(Box.createVerticalStrut(5));
		mainBox.add(new JSeparator());
		mainBox.add(Box.createVerticalStrut(5));
		logging = new JCheckBox("Turn on logging");
		mainBox.add(logging);
		mainBox.add(Box.createVerticalStrut(5));
		verbose = new JCheckBox("Provide verbose output");
		mainBox.add(verbose);
		mainBox.add(Box.createVerticalStrut(5));
		debug = new JCheckBox("Print debug information");
		mainBox.add(debug);
		mainBox.add(Box.createVerticalStrut(5));
		analyse = new JCheckBox("Print analysis information");
		mainBox.add(analyse);
		mainBox.add(Box.createVerticalStrut(10));
		apply = new JButton("Apply");
		mainBox.add(apply);
		mainBox.add(Box.createVerticalStrut(5));
		apply.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
              updateWorld();
        	  setVisible(false);
          }
        });
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
	public void showConfig()
	{
		EcologiaIO.debug("ProgramConfig: showing config window.");
		refresh();
		this.setVisible(true);
	}
	
	/**
	 * Refresh values displayed in the text fields.
	 */
	public void refresh()
	{
		logging.setSelected(EcologiaIO.logging);
		verbose.setSelected(EcologiaIO.verbose);
		debug.setSelected(EcologiaIO.debugging);
		analyse.setSelected(EcologiaIO.analysing);
	}
	
	/**
	 * Extract all the settings from the text fields and update the world parameters
	 */
	public void updateWorld()
	{
		EcologiaIO.logging = logging.isSelected();
		EcologiaIO.verbose = verbose.isSelected();
		EcologiaIO.debugging = debug.isSelected();
		EcologiaIO.analysing = analyse.isSelected();
		EcologiaIO.printStatus();
		if (logging.isSelected())
			World.getInstance().giveNews("Logging to "+System.getProperty("user.dir")+"/ecologia.log");
	}
}
