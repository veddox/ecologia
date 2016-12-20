package model;

import main.EcologiaIO;
import controller.OccupantType;
import controller.World;

import java.util.ArrayList;

/**
 * This class simulates a herbivore.
 * 
 * @author Daniel Vedder
 * @version 30.8.2014
 */
public class Herbivore extends Animal 
{	
	private int[] predatorPosition;
	
	public static Genome defaultGenome = new Genome(0, 2, 10, 4, 10, 150, 10, 120, 15, 10, 2);
	
	/**
	 * The constructor.
	 * @param setID
	 * @param newGenome
	 * @param myGeneration
	 * @param setX
	 * @param setY
	 * @param setEnergy
	 * @param parentID
	 */
	public Herbivore(int setID, Genome newGenome, int myGeneration, int setX,
					 int setY, int setEnergy, int parentID) 
	{
		super(setID, OccupantType.HERBIVORE, newGenome, myGeneration, setX,
			  setY, setEnergy, parentID);
		predatorPosition = new int[2];
	}
	
	/**
	 * Each turn, the herbivore looks out for predators and flees if it finds any,
	 * or otherwise grazes, if need be moving to better feeding grounds
	 */
	public void update()
	{
		super.update();
		if (!isAlive) return; //Don't do anything more if the animal is dead
		predatorPosition = search(OccupantType.CARNIVORE);
		if (predatorPosition != null) flee();
		else if (Simulator.getField(x, y).getGrassDensity() < 20
				 && exhaustion < genome.getStamina() - genome.getSpeed()) {
			moveToNewGrazingGrounds();
			feed();
		}
		else feed();
		
	}
	
	/**
	 * Graze the current tile.
	 * XXX: here be magic numbers!
	 */
	private void feed()
	{
		if (movesThisTurn < genome.getSpeed() && exhaustion < genome.getStamina()
				&& Simulator.getField(x, y).getGrassDensity() > 0) {
			movesThisTurn++;
			int feedEnergy = genome.getMetabolism()/3;
			changeEnergy(feedEnergy);
			Simulator.getField(x, y).reduceGrassDensity(feedEnergy*2);
		}
	}
	
	/**
	 * Search the surrounding squares for one with a higher grass density and move there
	 */
	private void moveToNewGrazingGrounds()
	{
		int currentGrassDensity = Simulator.getField(x, y).getGrassDensity();
		Direction dir = Direction.randomDirection();
		ArrayList<Direction> possibleDirs = new ArrayList<Direction>();
		// Search within range of sight
		for (int xdist = x-genome.getSight(); xdist < x+genome.getSight(); xdist++) {
			for (int ydist = y-genome.getSight(); ydist < y+genome.getSight(); ydist++) {
				if (!(xdist == x && ydist == y) && xdist >= 0 && ydist >= 0 && 
					xdist < World.getInstance().getParam("xsize") && ydist < World.getInstance().getParam("ysize") &&
						Simulator.getField(xdist, ydist).getGrassDensity() > currentGrassDensity) {
					Direction d = super.getDirection(xdist, ydist);
					if (!possibleDirs.contains(d)) possibleDirs.add(d);
				}
			}
		}
		// Try to move into one of the possible directions
	    int ttl = 12;
	    while (ttl > 0) {
			if (possibleDirs.isEmpty()) break;
			dir = possibleDirs.get(super.random.nextInt(possibleDirs.size()));
			if (super.move(dir)) return;
			possibleDirs.remove(dir);
			ttl--;
		}
		// If nothing is found, move randomly
		while (movesThisTurn < genome.getSpeed()) {
			boolean moved = super.move(dir);
			if (!moved) dir = Direction.randomDirection();
		}
	}
	
	/**
	 * Run away from a predator
	 */
	private void flee()
	{
		Direction predDir = super.getDirection(predatorPosition[0], predatorPosition[1]);
		if (predDir == Direction.CENTER) //Should never happen
			EcologiaIO.error("Herbivore @ "+x+"/"+y+" is fleeing in direction CENTER from carnivore @"+predatorPosition[0]+"/"+predatorPosition[1]+"!",
							 EcologiaIO.BREAK_ERROR);
		Direction flightDir = predDir.oppositeDirection();
		while (movesThisTurn < genome.getSpeed()) {
			boolean success = super.move(flightDir);
			if (!success) flightDir = Direction.randomDirection();
		}
	}

}
