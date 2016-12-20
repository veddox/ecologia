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
	
	//Parameter variables
	//TODO Reduce these to a single hashmap
	/*private int[] size; //The size of the world (x*y)
	private int timelapse; //When running, the simulation will be updated every so many milliseconds.
	private int stopAt; //The simulation will stop once this update is reached.
	private int autorun; //The number of updates the simulation will run for automatically before quitting
	private Humidity humidity; //The humidity level
	private int startGrassDensity; //The initial grass density on all fields
	private int waterTiles; //The number of water tiles that will be created.
	private int startNoCarnivores, startNoHerbivores; //The starting number of carnivores/herbivores.
	private int startEnergyCarnivores, startEnergyHerbivores; //The starting energy for carnivores/herbivores.
	*/

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
		//Parameter settings
		/*size = new int[] {100, 100}; //Default: 100*100
		timelapse = 100; //Default: 300 - can be changed at run-time
		stopAt = 200; //Default: 100 - can be changed at run-time
		autorun = -1; //Default: -1 (off)
		waterTiles = 10; //Default: 10
		humidity = Humidity.WET; //Default: Humidity.WET - can be changed at run-time
		startGrassDensity = 100; //Default: 100
		startNoCarnivores = 50; //Default: 50 - Hypothetical ideal: 5 (?)
		startNoHerbivores = 200; //Default: 200 - Hypothetical ideal: 160 (?)
		startEnergyCarnivores = 150; //Default: 150
		startEnergyHerbivores = 100; //Default: 100 */
		parameters = new HashMap<String, Integer>();

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
				else if (section.equals("[world]")) {
					/*switch (var) {
						case "width": size[0] = value; break;
						case "height": size[1] = value; break;
						case "timelapse": timelapse = value; break;
						case "stopAt": stopAt = value; break;
						case "autorun": autorun = value; break;
						case "waterTiles": waterTiles = value; break;
						case "humidity": humidity = Humidity.getStatus(value); break;
						case "startGrassDensity": startGrassDensity = value; break;
						case "startHerbivores": startNoHerbivores = value; break;
						case "startCarnivores": startNoCarnivores = value; break;
						case "startEnergyHerbivores": startEnergyHerbivores = value; break;
						case "startEnergyCarnivores": startEnergyCarnivores = value; break;
						default: EcologiaIO.error("Invalid config variable in the [world] section: "+var);
						}*/
					setParam(var, value);
				}
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
		else EcologiaIO.error("setParam: invalid parameter "+param, EcologiaIO.FATAL_ERROR);
	}
	
	
/*
	
	public int[] getSize() 
	{
		return size;
	}

	public void setSize(int[] size) 
	{
		this.size = size;
	}

	public int getTimelapse() 
	{
		return timelapse;
	}

	public void setTimelapse(int timelapse) 
	{
		if (timelapse < 0) return;
		if (this.timelapse != timelapse)
			EcologiaIO.debug("Timelapse changed to "+timelapse+" ms.");
		this.timelapse = timelapse;
	}

	public int getStopAt() 
	{
		return stopAt;
	}

	public void setStopAt(int stopAt) 
	{
		this.stopAt = stopAt;
	}
	
	public int getAutorun()
	{
		return autorun;
	}
	
	public void setAutorun(int autorun)
	{
		this.autorun = autorun;
	}
	
	public Humidity getHumidity() 
	{
		return humidity;
	}

	public void setHumidity(Humidity humidity) 
	{
		this.humidity = humidity;
	}

	public int getStartGrassDensity()
	{
		return startGrassDensity;
	}

	public void setStartGrassDensity(int startGrassDensity)
	{
		this.startGrassDensity = startGrassDensity;
	}

	public int getWaterTiles()
	{
		return waterTiles;
	}

	public void setStartNoWaterTiles(int startNoWaterTiles)
	{
		this.waterTiles = startNoWaterTiles;
	}

	public int getStartNoCarnivores()
	{
		return startNoCarnivores;
	}

	public void setStartNoCarnivores(int startNoCarnivores)
	{
		this.startNoCarnivores = startNoCarnivores;
	}

	public int getStartNoHerbivores()
	{
		return startNoHerbivores;
	}

	public void setStartNoHerbivores(int startNoHerbivores)
	{
		this.startNoHerbivores = startNoHerbivores;
	}

	public int getStartEnergyCarnivores() 
	{
		return startEnergyCarnivores;
	}

	public void setStartEnergyCarnivores(int startEnergyCarnivores) 
	{
		this.startEnergyCarnivores = startEnergyCarnivores;
	}

	public int getStartEnergyHerbivores() 
	{
		return startEnergyHerbivores;
	}

	public void setStartEnergyHerbivores(int startEnergyHerbivores) 
	{
		this.startEnergyHerbivores = startEnergyHerbivores;
	}

*/

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
