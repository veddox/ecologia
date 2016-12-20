package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import main.EcologiaIO;
import controller.Humidity;
import controller.OccupantType;
import controller.World;

/**
 * The Simulator class is the main class of the model package. It manages all 
 * elements of the actual simulation, and passes any relevant information on 
 * to World.
 * 
 * @author Daniel Vedder
 * @version 30.8.2014
 */
public class Simulator 
{
	private static ArrayList<Herbivore> herbivorePopulation;
	private static ArrayList<Carnivore> carnivorePopulation;
	private static MapField[][] map;
	private Random random;
	
	/**
	 * The constructor.
	 */
	public Simulator()
	{
		EcologiaIO.debug("Creating simulator");
		random = new Random();
		initMap();
		initWaterTiles();
		initPopulations();
		updateWorld();
	}
	
	/**
	 * Updates the model each turn.
	 */
	public void update()
	{
		//Calculate the new grass density on each plot
		EcologiaIO.debug("Simulator: Recalculating grass density.");
		double averageDensity = 0;
		int xsize = World.getInstance().getSize()[0];
		int ysize = World.getInstance().getSize()[1];
		for (int x = 0; x < xsize; x++) {
			for (int y = 0; y < ysize; y++) {
				if (!map[x][y].nearWater()) {
					map[x][y].setLocalHumidity(World.getInstance().getHumidity());
				}
				map[x][y].calculateGrassDensity();
				averageDensity += map[x][y].getGrassDensity();
			}
		}
		averageDensity = averageDensity/(xsize*ysize);
		World.getInstance().setAverageGrassDensity((int) averageDensity);
		
		//Each animal has its turn
		EcologiaIO.debug("Simulator: Updating herbivores.");
		for (int h = 0; h < herbivorePopulation.size(); h++) {
			herbivorePopulation.get(h).update();
		}
		EcologiaIO.debug("Simulator: Updating carnivores.");
		for (int c = 0; c < carnivorePopulation.size(); c++) { // <-- C++ in a Java program :D
			carnivorePopulation.get(c).update();
		}
		double hunt_success = (double) Carnivore.fights_won / (double) Carnivore.total_fights;
		EcologiaIO.analysis("Carnivore hunt success rate: "+(int) (hunt_success*100)+"%");

		updateWorld();
	}
	
	/**
	 * Send the current state of the simulation on to World
	 */
	public void updateWorld()
	{		
		EcologiaIO.debug("Simulator: Collecting information to send to World.");
		//The states of all animals are collected and passed on to the World
		ArrayList<HashMap<String, Integer>> animalInfo = new ArrayList<HashMap<String, Integer>>();
		for (int hi = 0; hi < herbivorePopulation.size(); hi++) {
			animalInfo.add(herbivorePopulation.get(hi).getInfo());
		}
		for (int ci = 0; ci < carnivorePopulation.size(); ci++) {
			animalInfo.add(carnivorePopulation.get(ci).getInfo());
		}
		World.getInstance().setAnimals(animalInfo);
		
		//Update the population counters
		World.getInstance().setCarnivoreCount(carnivorePopulation.size());
		World.getInstance().setHerbivoreCount(herbivorePopulation.size());
	}
	
	/*
	 * Component initialisation
	 */
	
	/**
	 * Initialise the map.
	 */
	private void initMap()
	{
		EcologiaIO.debug("Simulator: initialising map.");
		int xsize = World.getInstance().getSize()[0];
		int ysize = World.getInstance().getSize()[1];
		map = new MapField[xsize][ysize];
		for (int x = 0; x < xsize; x++) {
			for (int y = 0; y < ysize; y++) {
				map[x][y] = new MapField(x, y, OccupantType.NONE,
										 World.getInstance().getHumidity(),
										 World.getInstance().getStartGrassDensity());
			}
		}
	}
	
	/**
	 * Initialise the water tiles.
	 */
	private void initWaterTiles()
	{
		EcologiaIO.debug("Simulator: initialising water tiles.");
		for (int i = 0; i < World.getInstance().getWaterTiles(); i++) {
			//Each water tile is placed in a random location
			int setX = random.nextInt(World.getInstance().getSize()[0]);
			int setY = random.nextInt(World.getInstance().getSize()[1]);
			while (map[setX][setY].getOccupant() != OccupantType.NONE) {
				setX = random.nextInt(World.getInstance().getSize()[0]);
				setY = random.nextInt(World.getInstance().getSize()[1]);
			}
			map[setX][setY].setOccupant(OccupantType.WATER);
			//The fields around each water tile are watered
			for (int x = setX-2; x <= setX+2; x++) {
				for (int y = setY-2; y <= setY+2; y++) {
					try {
						Simulator.getField(x, y).setNearWater(true);
						Simulator.getField(x, y).setLocalHumidity(Humidity.SATURATION);
					}
					catch (ArrayIndexOutOfBoundsException aioobe) {} //Can be safely ignored
				}
			}
		}
	}
	
	/**
	 * Initialise the animal populations.
	 */
	private void initPopulations()
	{
		carnivorePopulation = new ArrayList<Carnivore>();
		herbivorePopulation = new ArrayList<Herbivore>();
		//Create the initial carnivore population, setting each carnivore down at a random position
		EcologiaIO.debug("Simulator: initialising carnivores.");
		for (int j = 0; j < World.getInstance().getStartNoCarnivores(); j++) {
			int setXCarnivore = random.nextInt(World.getInstance().getSize()[0]);
			int setYCarnivore = random.nextInt(World.getInstance().getSize()[1]);
			while (map[setXCarnivore][setYCarnivore].getOccupant() != OccupantType.NONE) {
				setXCarnivore = random.nextInt(World.getInstance().getSize()[0]);
				setYCarnivore = random.nextInt(World.getInstance().getSize()[1]);
			}
			int startEnergyCarnivores = World.getInstance().getStartEnergyCarnivores();
			carnivorePopulation.add(new Carnivore(World.getInstance().getNextID(), 
					Carnivore.defaultGenome, 1, setXCarnivore, setYCarnivore, 
					startEnergyCarnivores, 0));
		}
		//Create the initial herbivore population, setting each herbivore down at a random position
		EcologiaIO.debug("Simulator: initialising herbivores.");
		for (int i = 0; i < World.getInstance().getStartNoHerbivores(); i++) {
			int setXHerbivore = random.nextInt(World.getInstance().getSize()[0]);
			int setYHerbivore = random.nextInt(World.getInstance().getSize()[1]);
			while (map[setXHerbivore][setYHerbivore].getOccupant() != OccupantType.NONE) {
				setXHerbivore = random.nextInt(World.getInstance().getSize()[0]);
				setYHerbivore = random.nextInt(World.getInstance().getSize()[1]);
			}
			int startEnergyHerbivores = World.getInstance().getStartEnergyHerbivores();
			herbivorePopulation.add(new Herbivore(World.getInstance().getNextID(), 
					Herbivore.defaultGenome, 1, setXHerbivore, setYHerbivore, 
					startEnergyHerbivores, 0));
		}
	}
	
	/*
	 * Interface methods for interacting with map and animals
	 */
	
	/**
	 * Returns the field at the required position.
	 * @param x, y
	 * @return MapField
	 */

	public static MapField getField(int x, int y)
	{
		return map[x][y];
	}
	
	/**
	 * Return the animal at (x, y), or null if there is no animal at that field.
	 */
	public static Animal getAnimal(int x, int y)
	{
		Animal a = getHerbivore(x, y);
		if (a == null) a = getCarnivore(x, y);
		return a;
	}
	
	/**
	 * Return the herbivore at (x, y), or null if there is no animal at that field.
	 */
	public static Herbivore getHerbivore(int x, int y)
	{
		for (int h = 0; h < herbivorePopulation.size(); h++) {
			if (herbivorePopulation.get(h).getX() == x && herbivorePopulation.get(h).getY() == y) {
				return herbivorePopulation.get(h);
			}
		}
		return null;
	}
	
	/**
	 * Return the carnivore at (x, y), or null if there is no animal at that field.
	 */
	public static Carnivore getCarnivore(int x, int y)
	{
		for (int c = 0; c < carnivorePopulation.size(); c++) {
			if (carnivorePopulation.get(c).getX() == x && carnivorePopulation.get(c).getY() == y) {
				return carnivorePopulation.get(c);
			}
		}
		return null;
	}
	
	/**
	 * Add an animal to the population
	 * @param animal
	 */
	public static void addAnimal(Animal a)
	{
		EcologiaIO.debug("Simulator: adding a "+a.getType().toString());
		if (a.getType() == OccupantType.HERBIVORE) {
			herbivorePopulation.add((Herbivore) a);
		}
		else if (a.getType() == OccupantType.CARNIVORE) {
			carnivorePopulation.add((Carnivore) a);
		}
		else {
			EcologiaIO.error("Simulator: Invalid OccupantType passed to addAnimal()!",
							 EcologiaIO.FATAL_ERROR);
		}
	}
	
	/**
	 * Remove an animal from the population
	 * @param x, y coordinates
	 * @param type Make sure we are removing the right animal
	 */
	public static void removeAnimal(int x, int y, OccupantType type)
	{
		Animal a = null;
		if (type == OccupantType.CARNIVORE) a = getCarnivore(x, y);
		else if (type == OccupantType.HERBIVORE) a = getHerbivore(x, y);
		if (a == null) {
			EcologiaIO.error("Simulator.removeAnimal(): no "+type.toString()+" at "+x+"/"+y+".");
		}
		else if (type == OccupantType.HERBIVORE) {
			herbivorePopulation.remove((Herbivore) a);
			map[x][y].setOccupant(OccupantType.NONE);
			EcologiaIO.debug("Simulator: removing a herbivore.");
		}
		else if (type == OccupantType.CARNIVORE) {
			carnivorePopulation.remove((Carnivore) a);
			map[x][y].setOccupant(OccupantType.NONE);
			EcologiaIO.debug("Simulator: removing a carnivore.");
		}
		else {
			EcologiaIO.error("Simulator: Invalid OccupantType passed to removeAnimal()!",
							 EcologiaIO.FATAL_ERROR);
		}
		if (a != null) EcologiaIO.analysis("Animal "+a.getID()+" died at age "+a.getAge());
	}
}
