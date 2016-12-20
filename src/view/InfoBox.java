package view;

import java.awt.BorderLayout;
import java.util.HashMap;

import javax.swing.*;

import main.EcologiaIO;
import controller.Humidity;
import controller.OccupantType;
import controller.World;

/**
 * This class is responsible for displaying information about a tile that was
 * clicked on in the simulator.
 * 
 * @author Daniel Vedder
 * @version 4.9.2014
 */
public class InfoBox extends JFrame 
{
	private int xtile, ytile; //The coordinates of the currently active tile
	private HashMap<String, Integer> animalInfo;
	private JTabbedPane tab_pane;
	private Box tile_box, animal_box;
	private JLabel coordinates, occupied_by, humidity, grasslevel; //JLabels needed for the tile panel
	private JLabel id, type, energy, age, generation, parent, offspring, speed, stamina, efficiency;
	private JLabel age_limit, strength, rep_energy, mat_age, gestation, repr_rate, eyesight, mut_rate; //JLabels needed for the animal panel
	
	/**
	 * The constructor.
	 */
	public InfoBox()
	{
		this.setTitle("Information");
		this.setSize(230, 380);
		this.setLocation(400,150);
		this.setAlwaysOnTop(true);
		this.setDefaultCloseOperation(HIDE_ON_CLOSE);
		drawInfoBox();
	}
	
	/**
	 * Initialise the infobox.
	 */
	private void drawInfoBox()
	{
		tab_pane = new JTabbedPane();
		this.add(tab_pane, BorderLayout.CENTER);
		drawTileBox();
		drawAnimalBox();
		this.add(new JPanel(), BorderLayout.EAST);
		this.add(new JPanel(), BorderLayout.SOUTH);
		this.add(new JPanel(), BorderLayout.WEST);
	}
	
	/**
	 * Draw the tile box.
	 */
	private void drawTileBox()
	{
		tile_box = new Box(BoxLayout.Y_AXIS);
		tab_pane.addTab("Tile", tile_box);
		coordinates = new JLabel(); //Coordinates
		tile_box.add(coordinates);
		tile_box.add(Box.createVerticalStrut(10));
		occupied_by = new JLabel(); //Occupant
		tile_box.add(occupied_by);
		humidity = new JLabel(); //Humidity
		tile_box.add(humidity);
		grasslevel = new JLabel(); //Grass Density
		tile_box.add(grasslevel);
	}
	
	/**
	 * Draw the animal box.
	 */
	private void drawAnimalBox()
	{
		animal_box = new Box(BoxLayout.Y_AXIS);
		tab_pane.addTab("Animal", animal_box);
		id = new JLabel("Animal ID: "); //ID number
		animal_box.add(id);
		type = new JLabel("Type: "); //Type
		animal_box.add(type);
		animal_box.add(Box.createVerticalStrut(10));
		energy = new JLabel("Energy: "); //Energy
		animal_box.add(energy);
		age = new JLabel("Age: "); //Age
		animal_box.add(age);
		generation = new JLabel("Generation: "); //Generation
		animal_box.add(generation);
		parent = new JLabel("Parent: "); //Parent ID
		animal_box.add(parent);
		offspring = new JLabel("Offspring: "); //Offspring
		animal_box.add(offspring);
		animal_box.add(Box.createVerticalStrut(10));
		animal_box.add(new JLabel("Genome"));
		animal_box.add(Box.createVerticalStrut(5));
		speed = new JLabel("Speed: "); //Speed
		animal_box.add(speed);
		stamina = new JLabel("Stamina: "); //Speed
		animal_box.add(stamina);
		efficiency = new JLabel("Metabolic efficiency: "); //Efficiency
		animal_box.add(efficiency);
		age_limit = new JLabel("Age limit: "); //Age limit
		animal_box.add(age_limit);
		strength = new JLabel("Strength: "); //Strength
		animal_box.add(strength);
		rep_energy = new JLabel("Reproductive energy: "); //Reproduction energy
		animal_box.add(rep_energy);
		mat_age = new JLabel("Sexual maturity age: "); //Age of sexual maturity
		animal_box.add(mat_age);
		gestation = new JLabel("Gestation period: "); //Minimum length of the reproductive cycle
		animal_box.add(gestation);
		repr_rate = new JLabel("Reproduction rate: "); //Number of offspring per reproduction
		animal_box.add(repr_rate);
		eyesight = new JLabel("Eyesight range: "); //Eyesight
		animal_box.add(eyesight);
		mut_rate = new JLabel("Mutation rate: "); //Mutation rate
		animal_box.add(mut_rate);
	}
	
	/**
	 * Displays the information about the specified tile
	 * @param int Tile coordinates
	 */
	public void show(int tileX, int tileY)
	{
		xtile = tileX;
		ytile = tileY;
		refresh();
		this.setVisible(true);
		EcologiaIO.debug("Showing InfoBox for ("+xtile+"/"+ytile+")");
	}
	
	/**
	 * Refresh the Infobox with the data of a new tile.
	 */
	public void refresh()
	{
		animalInfo = World.getInstance().getAnimalInfo(xtile, ytile);
		coordinates.setText("Tile: "+xtile+"/"+ytile);
		occupied_by.setText("Occupant: "+OccupantType.fromInt(World.getInstance().getFieldInfo(xtile, ytile).get("Occupant")).toString());
		humidity.setText("Humidity: "+Humidity.getStatus(World.getInstance().getFieldInfo(xtile, ytile).get("Local humidity")).getString());
		grasslevel.setText("Grass density: "+World.getInstance().getFieldInfo(xtile, ytile).get("Grass density"));
		if (animalInfo != null) { //Only display information if an animal actually occupies the tile
			id.setText("Animal ID: "+animalInfo.get("ID"));
			type.setText("Type: "+OccupantType.fromInt(animalInfo.get("Type")).toString());
			energy.setText("Energy: "+animalInfo.get("Energy"));
			age.setText("Age: "+animalInfo.get("Age"));
			generation.setText("Generation: "+animalInfo.get("Generation"));
			parent.setText("Parent: "+animalInfo.get("Parent"));
			offspring.setText("Offspring: "+animalInfo.get("Offspring"));
			speed.setText("Speed: "+animalInfo.get("Speed"));
			stamina.setText("Stamina: "+animalInfo.get("Stamina"));
			efficiency.setText("Efficiency: "+animalInfo.get("Metabolism"));
			age_limit.setText("Age limit: "+animalInfo.get("Age limit"));			
			strength.setText("Strength: "+animalInfo.get("Strength"));
			rep_energy.setText("Reproductive energy: "+animalInfo.get("Reproductive energy"));
			mat_age.setText("Age of maturity: "+animalInfo.get("Maturity age"));
			gestation.setText("Gestation period: "+animalInfo.get("Gestation"));
			repr_rate.setText("Reproduction rate: "+animalInfo.get("Reproduction rate"));
			eyesight.setText("Range of eyesight: "+animalInfo.get("Sight"));
			mut_rate.setText("Mutation rate: "+animalInfo.get("Mutation rate"));
		}
		else { //If there is no animal here, display N/A
			id.setText("Animal ID: N/A");
			type.setText("Type: "+OccupantType.fromInt(World.getInstance().getFieldInfo(xtile, ytile).get("Occupant")).toString());
			energy.setText("Energy: N/A");
			age.setText("Age: N/A");
			generation.setText("Generation: N/A");
			parent.setText("Parent: N/A");
			offspring.setText("Offspring: N/A");
			speed.setText("Speed: N/A");
			stamina.setText("Stamina: N/A");
			efficiency.setText("Efficiency: N/A");
			age_limit.setText("Age limit: N/A");			
			strength.setText("Strength: N/A");
			rep_energy.setText("Reproductive energy: N/A");
			mat_age.setText("Age of maturity: N/A");
			gestation.setText("Gestation period: N/A");
			repr_rate.setText("Reproduction rate: N/A");
			eyesight.setText("Range of eyesight: N/A");
			mut_rate.setText("Mutation rate: N/A");
		}
	}

}
