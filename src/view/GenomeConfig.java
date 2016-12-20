package view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.*;

import main.Ecologia;
import main.EcologiaIO;
import controller.Humidity;
import controller.OccupantType;
import controller.World;

/**
 * This class provides GUI configuration facilities
 * for setting default genome values.
 * 
 * @author Daniel Vedder
 * @version 1.1.2015
 */
public class GenomeConfig extends JFrame
{
	private Box mainBox;
	private JComboBox<String> typeChooser;
	private JTextField mutationRate, speed, stamina, sight, metabolism, ageLimit, strength;
	private JTextField reproductiveEnergy, maturityAge, gestation, reproductionRate;
	private JButton confirm;
	private boolean showRestartDialog;
	
	/**
	 * The constructor
	 */
	public GenomeConfig()
	{
		this.setTitle("Genome Configuration");
		this.setSize(290, 500);
		this.setLocation(400,150);
		this.setDefaultCloseOperation(HIDE_ON_CLOSE);
		drawGenomeConfigWindow();
	}
	
	/**
	 * Create the interface
	 */
	public void drawGenomeConfigWindow()
	{
		mainBox = new Box(BoxLayout.Y_AXIS);
		JLabel heading = new JLabel("Initial Genome Settings");
		//Animal type chooser
		Box typePanel = new Box(BoxLayout.X_AXIS);
		JLabel type = new JLabel("Animal type: ");
		typeChooser = new JComboBox<String>(new String[] {OccupantType.HERBIVORE.toString(),
														  OccupantType.CARNIVORE.toString()});
		typeChooser.setMaximumSize(new Dimension(140, 30));
		typeChooser.setSelectedItem(OccupantType.HERBIVORE.toString());
		typeChooser.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
              update();
          }
        });		
		typePanel.add(type);
		typePanel.add(typeChooser);
		//Genome variables
		Box mrBox = new Box(BoxLayout.X_AXIS);
		JLabel mrLabel = new JLabel("Mutation rate: ");
		mutationRate = new JTextField(3);
		mrBox.add(mrLabel);
		mrBox.add(Box.createHorizontalStrut(80));
		mrBox.add(mutationRate);
		Box speedBox = new Box(BoxLayout.X_AXIS);
		JLabel speedLabel = new JLabel("Speed: ");
		speed = new JTextField(3);
		speedBox.add(speedLabel);
		speedBox.add(Box.createHorizontalStrut(135));
		speedBox.add(speed);
		Box staminaBox = new Box(BoxLayout.X_AXIS);
		JLabel staminaLabel = new JLabel("Stamina: ");
		stamina = new JTextField(3);
		staminaBox.add(staminaLabel);
		staminaBox.add(Box.createHorizontalStrut(135));
		staminaBox.add(stamina);
		Box sightBox = new Box(BoxLayout.X_AXIS);
		JLabel sightLabel = new JLabel("Sight: ");
		sight = new JTextField(3);
		sightBox.add(sightLabel);
		sightBox.add(Box.createHorizontalStrut(140));
		sightBox.add(sight);
		Box metabolismBox = new Box(BoxLayout.X_AXIS);
		JLabel metabolismLabel = new JLabel("Metabolic efficiency: ");
		metabolism = new JTextField(3);
		metabolismBox.add(metabolismLabel);
		metabolismBox.add(Box.createHorizontalStrut(40));
		metabolismBox.add(metabolism);
		Box alBox = new Box(BoxLayout.X_AXIS);
		JLabel alLabel = new JLabel("Age limit: ");
		ageLimit = new JTextField(3);
		alBox.add(alLabel);
		alBox.add(Box.createHorizontalStrut(120));
		alBox.add(ageLimit);
		Box strengthBox = new Box(BoxLayout.X_AXIS);
		JLabel strengthLabel = new JLabel("Strength: ");
		strength = new JTextField(3);
		strengthBox.add(strengthLabel);
		strengthBox.add(Box.createHorizontalStrut(120));
		strengthBox.add(strength);
		Box reBox = new Box(BoxLayout.X_AXIS);
		JLabel reLabel = new JLabel("Reproductive energy: ");
		reproductiveEnergy = new JTextField(3);
		reBox.add(reLabel);
		reBox.add(Box.createHorizontalStrut(40));
		reBox.add(reproductiveEnergy);
		Box maBox = new Box(BoxLayout.X_AXIS);
		JLabel maLabel = new JLabel("Maturity age: ");
		maturityAge = new JTextField(3);
		maBox.add(maLabel);
		maBox.add(Box.createHorizontalStrut(90));
		maBox.add(maturityAge);
		Box geBox = new Box(BoxLayout.X_AXIS);
		JLabel geLabel = new JLabel("Gestation period: ");
		gestation = new JTextField(3);
		geBox.add(geLabel);
		geBox.add(Box.createHorizontalStrut(90));
		geBox.add(gestation);
		Box rrBox = new Box(BoxLayout.X_AXIS);
		JLabel rrLabel = new JLabel("Reproduction rate: ");
		reproductionRate = new JTextField(3);
		rrBox.add(rrLabel);
		rrBox.add(Box.createHorizontalStrut(90));
		rrBox.add(reproductionRate);
		//The confirm button
		confirm = new JButton("Confirm");
		confirm.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
        	  if (showRestartDialog) {
        		  int restart = JOptionPane.showConfirmDialog(null, 
        				  "Please note: The new settings will only take \neffect on the next run.\nRestart now?", "Restart?", 
        						  JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        		  if (restart != JOptionPane.CANCEL_OPTION) {
					  updateWorld();
					  EcologiaIO.log("GenomeConfig: Genome settings for "+
									 typeChooser.getSelectedItem()+" updated in World!");
				  }
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
		mainBox.add(typePanel);
		mainBox.add(Box.createVerticalStrut(10));
		mainBox.add(mrBox);
		mainBox.add(Box.createVerticalStrut(10));
		mainBox.add(speedBox);
		mainBox.add(Box.createVerticalStrut(10));
		mainBox.add(staminaBox);
		mainBox.add(Box.createVerticalStrut(10));
		mainBox.add(sightBox);
		mainBox.add(Box.createVerticalStrut(10));
		mainBox.add(metabolismBox);
		mainBox.add(Box.createVerticalStrut(10));
		mainBox.add(alBox);
		mainBox.add(Box.createVerticalStrut(10));
		mainBox.add(strengthBox);
		mainBox.add(Box.createVerticalStrut(10));
		mainBox.add(reBox);
		mainBox.add(Box.createVerticalStrut(10));
		mainBox.add(maBox);
		mainBox.add(Box.createVerticalStrut(10));
		mainBox.add(geBox);
		mainBox.add(Box.createVerticalStrut(10));
		mainBox.add(rrBox);
		mainBox.add(Box.createVerticalStrut(10));
		mainBox.add(confirm);
		//Add all the boxes
		this.add(mainBox, BorderLayout.CENTER);
		this.add(new JPanel(), BorderLayout.NORTH);
		this.add(new JPanel(), BorderLayout.EAST);
		this.add(new JPanel(), BorderLayout.SOUTH);
		this.add(new JPanel(), BorderLayout.WEST);		
	}
	
	/**
	 * Update the box and make it visible
	 * @param showRestart Show the restart dialog when closing this window?
	 */
	public void showGenomeConfig(boolean showRestart)
	{
		EcologiaIO.debug("GenomeConfig: showing genome config window.");
		showRestartDialog = showRestart;
		update();
		this.setVisible(true);
	}
	
	/**
	 * Update all the text fields.
	 */
	private void update()
	{
		OccupantType currentType = OccupantType.fromString((String) typeChooser.getSelectedItem());
		HashMap<String, Integer> genomeInfo = World.getInstance().getDefaultGenome(currentType);
		mutationRate.setText(Integer.toString(genomeInfo.get("mutationRate")));
		speed.setText(Integer.toString(genomeInfo.get("speed")));
		stamina.setText(Integer.toString(genomeInfo.get("stamina")));
		sight.setText(Integer.toString(genomeInfo.get("sight")));
		metabolism.setText(Integer.toString(genomeInfo.get("metabolism")));
		ageLimit.setText(Integer.toString(genomeInfo.get("ageLimit")));
		strength.setText(Integer.toString(genomeInfo.get("strength")));
		reproductiveEnergy.setText(Integer.toString(genomeInfo.get("reproductiveEnergy")));
		maturityAge.setText(Integer.toString(genomeInfo.get("maturityAge")));
		gestation.setText(Integer.toString(genomeInfo.get("gestation")));
		reproductionRate.setText(Integer.toString(genomeInfo.get("reproductionRate")));
	}
	
	/**
	 * Update the default genome values
	 */
	private void updateWorld()
	{
		OccupantType currentType = OccupantType.fromString((String) typeChooser.getSelectedItem());
		int setMutationRate = new Integer(mutationRate.getText());
		int setSpeed = new Integer(speed.getText());
		int setStamina = new Integer(stamina.getText());
		int setSight = new Integer(sight.getText());
		int setMetabolism = new Integer(metabolism.getText());
		int setAgeLimit = new Integer(ageLimit.getText());
		int setStrength = new Integer(strength.getText());
		int setReproductiveEnergy = new Integer(reproductiveEnergy.getText());
		int setMaturityAge = new Integer(maturityAge.getText());
		int setGestation = new Integer(gestation.getText());
		int setReproductionRate = new Integer(reproductionRate.getText());
		World.getInstance().setDefaultGenome(currentType, setMutationRate, setSpeed, setStamina,
											 setSight, setMetabolism, setAgeLimit, setStrength,
											 setReproductiveEnergy, setMaturityAge, setGestation,
											 setReproductionRate);
	}
}
