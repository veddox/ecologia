package controller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import main.EcologiaIO;
import model.Carnivore;
import model.Genome;
import model.Herbivore;
import model.Simulator;

/**
 * The World class acts as a communicator between the model and the view packages. It receives
 * the current status of the simulation from model and passes it on to view. Conversely, user
 * input from view is forwarded to model. It also stores all simulation settings.
 * 
 * @author Daniel Vedder
 * @version 29.8.2014
 */
public class World 
{
	private static World world; //The Singleton instance of this class
	
	//Parameter variables are stored in this hashmap
	private HashMap<String, Integer> parameters;

	//Runtime variables
	private boolean running; //Is the simulation running?
	private int turn; //The update number
	private int nextID; //The next ID number that will be handed out to a newborn animal
	private int herbivoreCounter, carnivoreCounter; //Keep count of the herbivores and carnivores
	private int highestGeneration; //What generation have we reached by now?
	private int averageGrassDensity; //A measure of how much food is available for the herbivores
	private ArrayList<HashMap<String, Integer>> animals; //A list of properties of each animal
	private ArrayList<String> news; //A collection of news items that have accumulated
	
	/**
	 * This class implements Singleton, therefore the constructor is private.
	 */
	private World()
	{
		parameters = new HashMap<String, Integer>();
		//Parameter settings (defaults, can be changed via the config file)
		parameters.put("xsize", 100);
		parameters.put("ysize", 100);
		parameters.put("timelapse", 100);
		parameters.put("stopAt", 200);
		parameters.put("autorun", -1);
		parameters.put("waterTiles", 10);
		parameters.put("humidity", 1);
		parameters.put("startGrassDensity", 100);
		parameters.put("startNoCarnivores", 50);
		parameters.put("startNoHerbivores", 200);
		parameters.put("startEnergyCarnivores", 150);
		parameters.put("startEnergyHerbivores", 100);
		
		reset(); //Runtime variables
	}
	
	/**
	 * The Singleton method.
	 */
	public static World getInstance()
	{
		if (world == null) {
			world = new World();
		}
		return world;
	}
	
	/**
	 * Read and parse a config file.
	 * XXX This is really messy, but it works.
	 */
	public void readConfigFile(String filename)
	{
		EcologiaIO.debug("Beginning to read config file "+filename);
		try {
			BufferedReader confReader = new BufferedReader(new FileReader(filename));
			String line = confReader.readLine();
			//Initialize some necessary helper variables
			String section = "";
			String var = "";
			int value = -1;
			HashMap<String, Integer> herbGen = getDefaultGenome(OccupantType.HERBIVORE);
			HashMap<String, Integer> carnGen = getDefaultGenome(OccupantType.CARNIVORE);
			//Inspect each line
			while (line != null) {
				//Split lines into variable/value pairs
				line = line.trim();
				if (!line.startsWith("#")) { //Ignore commented lines
					String[] elements = line.split(" ");
					if (elements.length >= 2) {
						var = elements[0].trim();
						try {
							value = new Integer(elements[1].trim());
						}
						catch (NumberFormatException nfe) {
							EcologiaIO.error("Invalid integer for configuration variable "+var, nfe);
							return;
						}
					}
				}
				//Set the current section
				if (line.startsWith("[") && line.endsWith("]")) section = line;
				//Deal with world variables
				else if (section.equals("[world]")) setParam(var, value);
				//Configure default animal genomes
				else if (section.equals("[herbivore]")) {
					if (herbGen.containsKey(var)) herbGen.put(var, value);
					else EcologiaIO.error("Invalid config variable in the [herbivore] section: "+var);
				}
				else if (section.equals("[carnivore]")) {
					if (carnGen.containsKey(var)) carnGen.put(var, value);
					else EcologiaIO.error("Invalid config variable in the [carnivore] section: "+var);
				}
				line = confReader.readLine();
			}
			//Wrap up
			confReader.close();
			Herbivore.defaultGenome = new Genome(herbGen);
			Carnivore.defaultGenome = new Genome(carnGen);
			EcologiaIO.log("Parsed config file "+filename);
		}
		catch (IOException ioe) {
			EcologiaIO.error("Failed to read config file "+filename, ioe);
		}
	}
	
	/**
	 * Reset the world run-time variables, ready for a new run.
	 * This method should only be called from the Ecologia main class!
	 */
	public void reset()
	{
		running = false;
		turn = 0;
		nextID = 0;
		herbivoreCounter = 0;
		carnivoreCounter = 0;
		highestGeneration = 1;
		averageGrassDensity = parameters.get("startGrassDensity");
		animals = null;
		news = new ArrayList<String>();
	}
	
	/**
	 * Display a news item - calling with null as a parameter resets the news list
	 * @param news
	 */
	public void giveNews(String message)
	{
		if (message == null) {
			news.clear();
		}
		else {
			message = turn+": "+message;
			news.add(message);
			EcologiaIO.log(message);
		}
	}
	
	/**
	 * Return information about the animal at the given position as a hash map
	 * @param x, y
	 * @return HashMap, or null if no animal at the specified location
	 */
	public HashMap<String, Integer>	getAnimalInfo(int x, int y)
	{
		HashMap<String, Integer> info = null;
		for (int a = 0; a < animals.size(); a++) {
			if (animals.get(a).get("X") == x && animals.get(a).get("Y") == y) {
				info = animals.get(a);
				break;
			}
		}
		return info;
	}
	
	/**
	 * Return information about the map field at the given position as a hash map
	 * @param x, y
	 * @return HashMap, or null if out of bounds
	 */
	public HashMap<String, Integer>	getFieldInfo(int x, int y)
	{
		return Simulator.getField(x, y).getInfo();
	}

	/*
	 * All the getters and setters for the parameter settings and runtime variables
	 */

	/**
	 * Return a hash map holding all the genome values
	 */
	public HashMap<String, Integer> getDefaultGenome(OccupantType type)
	{
		if (type == OccupantType.HERBIVORE) return Herbivore.defaultGenome.asHashMap();
		else if (type == OccupantType.CARNIVORE) return Carnivore.defaultGenome.asHashMap();
		else {
			EcologiaIO.error("Invalid OccupantType passed to World.getDefaultGenome()",
					EcologiaIO.FATAL_ERROR);
			return null;
		}
	}
	
	/**
	 * Interface for the Genome method
	 */
	public void setDefaultGenome(OccupantType type, int mutationRate, int speed, int stamina,
								 int sight, int metabolism, int ageLimit, int strength,
								 int reproductiveEnergy, int maturityAge, int gestation,
								 int reproductionRate)
	{
		Genome genome = new Genome(mutationRate, speed, stamina, sight, metabolism,
								   ageLimit, strength, reproductiveEnergy, maturityAge,
								   gestation, reproductionRate);
		if (type == OccupantType.HERBIVORE) Herbivore.defaultGenome = genome;
		else if (type == OccupantType.CARNIVORE) Carnivore.defaultGenome = genome;
	}

	/**
	 * Return a parameter value.
	 */
	public int getParam(String param)
	{
		if (parameters.containsKey(param))
			return parameters.get(param);
		else {
			EcologiaIO.error("getParam: invalid parameter "+param, EcologiaIO.FATAL_ERROR);
			return -1; //Will never be reached, but is syntactically needed
		}
	}

	/**
	 * Set a parameter value.
	 */
	public void setParam(String param, int value)
	{
		//XXX Is this check necessary here?
		if (parameters.containsKey(param))
			parameters.put(param, value);
		else EcologiaIO.error("setParam: invalid parameter "+param, EcologiaIO.CONTINUABLE_ERROR);
	}

	// Getters/Setters for runtime variables
	
	public int getHerbivoreCount() 
	{
		return herbivoreCounter;
	}

	public void setHerbivoreCount(int herbivoreCounter) 
	{
		this.herbivoreCounter = herbivoreCounter;
	}

	public int getCarnivoreCount() 
	{
		return carnivoreCounter;
	}

	public void setCarnivoreCount(int carnivoreCounter) 
	{
		this.carnivoreCounter = carnivoreCounter;
	}

	public int getAverageGrassDensity() 
	{
		return averageGrassDensity;
	}

	public void setAverageGrassDensity(int averageGrassDensity) 
	{
		this.averageGrassDensity = averageGrassDensity;
	}

	public boolean isRunning() 
	{
		return running;
	}

	public void setRunning(boolean running) 
	{
		this.running = running;
	}

	public int getTurn() 
	{
		return turn;
	}
	
	/**
	 * Increment the turn variable by one.
	 */
	public void incrementTurn()
	{
		turn++;
	}

	/**
	 * Get the next unique animal ID number and increment the counter.
	 */
	public int getNextID()
	{
		nextID++;
		if (nextID == Integer.MAX_VALUE)
			EcologiaIO.error("Animal ID number integer overflow!",
							 EcologiaIO.BREAK_ERROR);
		return nextID;
	}

	/**
	 * Increment the generation counter as necessary.
	 */
	public void incGeneration(int n)
	{
		if (n > highestGeneration) {
			highestGeneration = n;
		}
	}

	public int getGeneration()
	{
		return highestGeneration;
	}
	
	public void setAnimals(ArrayList<HashMap<String, Integer>> animalInfo)
	{
		animals = animalInfo;
	}
	
	public ArrayList<String> collectNews()
	{
		return news;
	}
}
