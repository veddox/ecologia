package model;

import main.EcologiaIO;
import controller.OccupantType;
import controller.World;

/**
 * This class simulates a carnivore.
 * 
 * @author Daniel Vedder
 * @version 30.8.2014
 */
public class Carnivore extends Animal 
{
	public static int fights_won = 1;
	public static int total_fights = 1; //Start at 1 to avoid division by zero errors

	private int[] preyPosition;
	private Direction currentDirection;
	
	public static Genome defaultGenome = new Genome(0, 3, 10, 4, 18, 200, 11, 200, 30, 10, 1);
	
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
	public Carnivore(int setID, Genome newGenome, int myGeneration, int setX,
					 int setY, int setEnergy, int parentID) 
	{
		super(setID, OccupantType.CARNIVORE, newGenome, myGeneration, setX,
			  setY, setEnergy, parentID);
		preyPosition = new int[2];
		currentDirection = Direction.randomDirection();
	}
	
	/**
	 * Each turn, the carnivore looks for a herbivore and moves towards it.
	 * If no herbivore can be found, it moves in a random direction.
	 */
	public void update()
	{
		super.update();
		if (!isAlive) return; //Don't do anything more if the animal is dead
		else if (energy > 50 && exhaustion > genome.getStamina() - (2 * genome.getSpeed())) return; //rest
		preyPosition = super.search(OccupantType.HERBIVORE);
		if (preyPosition != null) hunt();
		else {
			while (movesThisTurn < genome.getSpeed()) {
				boolean moved = super.move(currentDirection);
				if (!moved) currentDirection = Direction.randomDirection();
			}
		}
	}

	/**
	 * The carnivore runs toward a herbivore
	 */
	private void hunt()
	{
		EcologiaIO.debug("Carnivore @"+x+"/"+y+" is hunting!");
		currentDirection = super.getDirection(preyPosition[0], preyPosition[1]);
		while (movesThisTurn < genome.getSpeed()) {
			boolean success = super.move(currentDirection);
			if (!success) currentDirection = currentDirection.nextDirection(true);
			else if (getDistance(preyPosition[0], preyPosition[1]) == 1
					 && movesThisTurn < genome.getSpeed() && isAlive)
				attack();
			else currentDirection = super.getDirection(preyPosition[0], preyPosition[1]);
		}
	}
	
	/**
	 * The carnivore has run down a herbivore and now tries to kill it
	 * XXX Warning: here be magic numbers!
	 */
	private void attack()
	{
		EcologiaIO.debug("Carnivore @"+x+"/"+y+" is attacking a prey!");
		Herbivore prey = Simulator.getHerbivore(preyPosition[0], preyPosition[1]);//(x, y);
		if (prey == null) {
			EcologiaIO.error("Carnivore at "+x+"/"+y+" is attacking a non-existent prey!");
			return;
		}
		total_fights++;
		//Choose a fight algorithm from the methods below - currently strengthFight is used
		if (strengthFight(genome.getStrength(), prey.getGenome().getStrength())) {
			//Predators get (50+(metabolism*4))% of their preys energy
			changeEnergy((int) ((prey.getEnergy()/2)+prey.getEnergy()*(genome.getMetabolism()*0.04)));
			World.getInstance().giveNews("A Herbivore has been killed!");
			Simulator.removeAnimal(preyPosition[0], preyPosition[1], OccupantType.HERBIVORE);
			/* It would be more efficient to use currentDirection, but I'm not
			 * sure whether it's guaranteed to be accurate */
			super.move(getDirection(preyPosition[0], preyPosition[1]));
			fights_won++;
		}
		else {
			EcologiaIO.debug("A Herbivore has won a fight.");
			//Reduce each combatants energy by their strength in the fight
			//XXX Change this back again?
			changeEnergy(-30); //(-genome.getStrength());
			prey.changeEnergy(-30); //(-prey.getGenome().getStrength());
			exhaust(2);
			prey.exhaust(2);
		}
		movesThisTurn = genome.getSpeed(); //an attack ends the carnivore's turn
	}

	//The following methods are various fighting algorithms
	//XXX Warning: here be magic numbers!

	/**
	 * A fight method based on the strength and current energy of the combatants.
	 * This method is currently not used.
	 */
	private boolean strengthEnergyFight(int pred_str, int pred_en, int prey_str, int prey_en)
	{
		int predStrength = pred_str+(pred_en/50);
		int preyStrength = prey_str+(prey_en/50)+super.random.nextInt(10);
		return (predStrength > preyStrength);
	}

	/**
	 * A fight method based on the strength of the combatants.
	 * This is the method currently employed.
	 */
	private boolean strengthFight(int predStrength, int preyStrength)
	{
		int randomFactor = super.random.nextInt(10)-3;
		preyStrength += randomFactor;
		return (predStrength > preyStrength);
	}	
		

}
