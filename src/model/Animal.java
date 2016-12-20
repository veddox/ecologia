package model;

import java.util.HashMap;
import java.util.Random;
import java.util.ArrayList;

import main.EcologiaIO;
import controller.OccupantType;
import controller.World;

/**
 * This is the superclass of all animal classes. It holds common methods
 * needed by all animals.
 * 
 * @author Daniel Vedder
 * @version 30.8.2014
 */
public abstract class Animal 
{
	/*
	 * XXX Set ID numbers as long?
	 * Quick calculation: in a 100x100 world, an integer ID
	 * (max value: 2**31) should last for >40,000,000 updates
	 * (based on a 1000 update test run).
	 * => long IDs are not very urgent...
	 */
	protected int IDnumber; //A unique identifier for this animal
	protected int parent; //The ID number of the parent
	protected Genome genome;
	protected int generation;
	protected int offspring;
	protected OccupantType type;
	protected int x, y; //The animal's position
	protected int age;
	protected int energy;
	
	protected int movesThisTurn;
	protected int attemptedMovesThisTurn;
	protected int exhaustion;
	protected int gestationPeriod;
	protected boolean isAlive;
	protected Random random;
	
	/**
	 * The constructor.
	 * @param setID
	 * @param myType
	 * @param newGenome
	 * @param myGeneration
	 * @param setX
	 * @param setY
	 * @param setEnergy
	 * @param parentID
	 */
	public Animal(int setID, OccupantType myType, Genome newGenome,
				  int myGeneration, int setX, int setY, int setEnergy,
				  int parentID)
	{
		IDnumber = setID;
		genome = newGenome;
		generation = myGeneration;
		offspring = 0;
		type = myType;
		x = setX;
		y = setY;
		energy = setEnergy;
		parent = parentID;
		movesThisTurn = 0;
		attemptedMovesThisTurn = 0;
		exhaustion = 0;
		gestationPeriod = genome.getGestation();
		isAlive = true;
		random = new Random();
		Simulator.getField(x, y).setOccupant(type);
		EcologiaIO.analysis("Created "+type.toString()+" with ID="+IDnumber+
							" parent="+parent+" generation="+generation+
							" update="+World.getInstance().getTurn());
		String genStr = genome.asHashMap().toString();
		EcologiaIO.analysis("Genome of animal "+IDnumber+": "+
							genStr.substring(1, genStr.length()-1));
	}
	
	/*
	 * --- Generic methods needed by all animals ---
	 */
	
	/**
	 * This method has to be called by every species.
	 */
	public void update()
	{
		age++;
		movesThisTurn = 0;
		attemptedMovesThisTurn = 0;
		if (exhaustion > 0) exhaustion--;
		if (gestationPeriod > 0) gestationPeriod--;
		if (age >= genome.getAgeLimit()) {
			isAlive = false;
			World.getInstance().giveNews("A "+type.toString()+" has died!");
			Simulator.removeAnimal(x, y, type);
			return;
		}
		changeEnergy(-1);
		if (!isAlive) return;
		else if (age >= genome.getMaturityAge() && gestationPeriod == 0
				 && energy >= genome.getReproductiveEnergy()
				 && random.nextInt(3) == 0) {
			reproduce();
		}
	}
	
	/**
	 * The animal reproduces, setting down a child on a neighbouring square
	 */
	public void reproduce() 
	{
		int r = genome.getReproductionRate();
		for (int i = 0; i < r; i++) {
			int[] childField = getNeighbouringField(Direction.randomDirection());
			int ttl = 10; //Make sure we don't end up in an endless loop
			while (childField == null || World.getInstance().getFieldInfo(childField[0], childField[1]).get("Occupant") != OccupantType.NONE.toInt()) {
				if (ttl == 0) return; //If we still haven't found a space, break off
				childField = getNeighbouringField(Direction.randomDirection());
				ttl--;
			}
			int childEnergy = energy/(r+1);
			if (type == OccupantType.HERBIVORE) {
				Herbivore child = new Herbivore(World.getInstance().getNextID(), 
												new Genome(genome), generation+1, childField[0], 
												childField[1], childEnergy, IDnumber);
				Simulator.addAnimal(child);
			}
			else if (type == OccupantType.CARNIVORE) {
				Carnivore child = new Carnivore(World.getInstance().getNextID(), 
												new Genome(genome), generation+1, childField[0], 
												childField[1], childEnergy, IDnumber);
				Simulator.addAnimal(child);
			}
			offspring++;
		}
		changeEnergy(-energy/(r+1));
		gestationPeriod = genome.getGestation();
		World.getInstance().incGeneration(generation+1);
		World.getInstance().giveNews("A new "+type.toString()+" has been born!"); //XXX Comment this out?
	}
	
	/**
	 * The animal moves in the specified direction.
	 * @return success
	 */
	public boolean move(Direction dir)
	{
		/*
		 * Fix the Spring frost bug (very random freezing):
		 * If there have been more than 12 attempted (and failed) moves this turn, 
		 * e.g. due to the animal being surrounded, we are probably in an endless 
		 * loop and need to break out.
		 * 
		 * Also fix the Ghost bug: we cannot guarantee that the animal is
		 * alive at this point, so let's make sure to check.
		 */
		if (attemptedMovesThisTurn > 12 || !isAlive) {
			movesThisTurn++;
			return true; 
		}
		
		boolean success = true;
		int[] nextPos = getNeighbouringField(dir);
		//Check if the square to move to is valid
		if (nextPos == null || movesThisTurn >= genome.getSpeed() || exhaustion > genome.getStamina() ||
				OccupantType.fromInt(World.getInstance().getFieldInfo(nextPos[0], nextPos[1]).get("Occupant")) != OccupantType.NONE) {
			success = false;
			attemptedMovesThisTurn++;
		}
		
		//Execute the move
		if (success) {
			Simulator.getField(x, y).setOccupant(OccupantType.NONE);
			Simulator.getField(nextPos[0], nextPos[1]).setOccupant(type);
			x = nextPos[0];
			y = nextPos[1];
			movesThisTurn++;
			exhaustion++;
			changeEnergy(-1);
		}
		return success;
	}

	/**
	 * Search for the inputed object within the line of sight.
	 */
	public int[] search(OccupantType type)
	{
		//return randomizedSearch(type);
		//return closestSearch(type);
		return mixedSearch(type);
	}
	
	/**
	 * Search for the inputed object within the line of sight.
	 * The returned coordinates are chosen at random from a list of eligible ones.
	 */
	public int[] randomizedSearch(OccupantType type)
	{
		ArrayList<int[]> targets = new ArrayList<int[]>();
		for (int xdist = x-genome.getSight(); xdist < x+genome.getSight(); xdist++) {
			for (int ydist = y-genome.getSight(); ydist < y+genome.getSight(); ydist++) {
				if (xdist >= 0 && ydist >= 0 && xdist < World.getInstance().getSize()[0]
					&& ydist < World.getInstance().getSize()[1]) {
					if (Simulator.getField(xdist, ydist).getOccupant() == type) {
						int[] newTarget = {xdist, ydist};
						targets.add(newTarget);
					}
				}
			}
		}
		if (targets.size() > 0) return targets.get(random.nextInt(targets.size()));
		else return null;
	}
	
	/**
	 * Search for the inputed object within the line of sight.
	 * Finds the object closest to the individual.
	 */
	public int[] closestSearch(OccupantType type)
	{
		int[] target = {-1, -1};
		int minDist = genome.getSight()+1;
		for (int xdist = x-genome.getSight(); xdist < x+genome.getSight(); xdist++) {
			for (int ydist = y-genome.getSight(); ydist < y+genome.getSight(); ydist++) {
				if (xdist >= 0 && ydist >= 0 && xdist < World.getInstance().getSize()[0]
					&& ydist < World.getInstance().getSize()[1]) {
					if (Simulator.getField(xdist, ydist).getOccupant() == type) {
						int distance = getDistance(xdist, ydist);
						if (distance != 0 && distance < minDist) {
							target[0] = xdist;
							target[1] = ydist;
							if (distance == 1) break; //Ain't gonna get any better...
						}
					}
				}
			}
		}
		if (target[0] == -1) return null;
		else return target;
	}

	/**
	 * Search for the inputed object within the line of sight.
	 * A random target is chosen out of a list of targets closest to the individual.
	 */
	public int[] mixedSearch(OccupantType type)
	{
		ArrayList<int[]> targets = new ArrayList<int[]>();
		int minDist = genome.getSight()+1;
		for (int xdist = x-genome.getSight(); xdist < x+genome.getSight(); xdist++) {
			for (int ydist = y-genome.getSight(); ydist < y+genome.getSight(); ydist++) {
				if (xdist >= 0 && ydist >= 0 && xdist < World.getInstance().getSize()[0]
					&& ydist < World.getInstance().getSize()[1]) {
					if (Simulator.getField(xdist, ydist).getOccupant() == type) {
						int distance = getDistance(xdist, ydist);
						int[] newTarget = {xdist, ydist};
						if (distance < minDist) {
							targets.clear();
							minDist = distance;
						}
						if (distance <= minDist) targets.add(newTarget);
					}
				}
			}
		}
		if (targets.isEmpty()) return null;
		else return targets.get(random.nextInt(targets.size()));
	}
	
	/**
	 * Calculate the neighbouring square in the specified direction
	 * (return null if out of bounds)
	 */
	public int[] getNeighbouringField(Direction dir)
	{
		int nextX = x;
		int nextY = y;
		switch (dir) {
			case UP: nextY--; break;
			case RIGHT: nextX++; break;
			case DOWN: nextY++; break;
			case LEFT: nextX--; break;
			case TOP_RIGHT: nextY--; nextX++; break;
			case BOTTOM_RIGHT: nextY++; nextX++; break;
			case BOTTOM_LEFT: nextY++; nextX--; break;
			case TOP_LEFT: nextY--; nextX--; break;
			default: EcologiaIO.error("Invalid direction passed to Animal.getNeighbouringField()! ("+dir+") by "+type.toString()+" @"+x+"/"+y); 
		}
		if (nextX < 0 || nextX >= World.getInstance().getSize()[0] || 
				nextY < 0 || nextY >= World.getInstance().getSize()[1]) {
			return null;
		}
		else {
			int[] square = {nextX, nextY};
			return square;
		}
	}
	
	/**
	 * In which direction are the given coordinates relative to this animal?
	 * @param xpos
	 * @param ypos
	 * @return Direction
	 */
	public Direction getDirection(int xpos, int ypos)
	{
		if (xpos == x && ypos > y) return Direction.DOWN;
		else if (xpos == x && ypos < y) return Direction.UP;
		else if (xpos > x && ypos == y) return Direction.RIGHT;
		else if (xpos < x && ypos == y) return Direction.LEFT;
		else if (xpos > x && ypos > y) return Direction.BOTTOM_RIGHT;
		else if (xpos < x && ypos > y) return Direction.BOTTOM_LEFT;
		else if (xpos > x && ypos < y) return Direction.TOP_RIGHT;
		else if (xpos < x && ypos < y) return Direction.TOP_LEFT;
		else return Direction.CENTER;
	}

	/**
	 * How many steps are needed to get to the specified position?
	 */
	public int getDistance (int xpos, int ypos)
	{
		int xdist = Math.abs(xpos - x);
		int ydist = Math.abs(ypos - y);
		return Math.max(xdist, ydist);
	}

	/*
	 * --- Getters ---
	 */
	
	/**
	 * Return a hash map containing all the information about this animal.
	 */
	public HashMap<String, Integer> getInfo()
	{
		HashMap<String, Integer> info = new HashMap<String, Integer>();
		
		//Lifetime variables
		info.put("ID", IDnumber);
		info.put("Type", type.toInt());
		info.put("X", x);
		info.put("Y", y);
		info.put("Age", age);
		info.put("Energy", energy);
		info.put("Generation", generation);
		info.put("Parent", parent);
		info.put("Offspring", offspring);
		
		//Genome variables
		//XXX This is redundant with Genome.asHashMap()
		info.put("Mutation rate", genome.getMutationRate());
		info.put("Speed", genome.getSpeed());
		info.put("Stamina", genome.getStamina());
		info.put("Sight", genome.getSight());
		info.put("Metabolism", genome.getMetabolism());
		info.put("Age limit", genome.getAgeLimit());
		info.put("Strength", genome.getStrength());
		info.put("Reproductive energy", genome.getReproductiveEnergy());
		info.put("Maturity age", genome.getMaturityAge());
		info.put("Gestation", genome.getGestation());
		info.put("Reproduction rate", genome.getReproductionRate());
		
		return info;
	}
	
	//XXX Deprecate other getters? [getInfo() available]
	
	public boolean isAlive()
	{
		return isAlive;
	}
	
	public long getID()
	{
		return IDnumber;
	}
	
	public Genome getGenome()
	{
		return genome;
	}

	public int getGeneration()
	{
		return generation;
	}
	
	public int getParent()
	{
		return parent;
	}

	public int getOffspring()
	{
		return offspring;
	}

	public OccupantType getType()
	{
		return type;
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	public int getAge()
	{
		return age;
	}
	
	public int getEnergy()
	{
		return energy;
	}
	
	/*
	 * A few setters that may be needed 
	 */
	
	/**
	 * Change the energy level of this animal. If it dips to <= 0, the animal
	 * dies and is removed. This is a convenience wrapper method around
	 * setEnergy().
	 * @param amount
	 */
	public void changeEnergy(int amount)
	{
		setEnergy(energy+amount);
	}
	
	public void setEnergy(int newEnergy)
	{
		energy = newEnergy;
		if (energy <= 0) {
			isAlive = false;
			World.getInstance().giveNews("A "+type.toString()+" has starved!");
			Simulator.removeAnimal(x, y, type);
		}
	}
	
	public void setAge(int newAge)
	{
		age = newAge;
	}
	
	public void setPosition(int newX, int newY)
	{
		x = newX;
		y = newY;
	}

	public void exhaust(int e)
	{
		exhaustion += e;
		if (exhaustion < 0) exhaustion = 0;
	}	
}
