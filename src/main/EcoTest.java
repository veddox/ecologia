package main;

import controller.*;
import model.*;

/**
 * This class is used to test new features in Ecologia during development.
 * Only executes when in debugging is turned on and the testing variable 
 * (below) is set to true.
 * 
 * TODO: expand this into a proper testing framework?
 * 
 * @author Daniel Vedder
 * @date 24.12.2014
 */
public class EcoTest
{
	private boolean testing;
	
	int tpcX, tpcY;
	
	public EcoTest()
	{
		testing = false; //Is a test supposed to be run?
	}
	
	/**
	 * Run a test (called first thing every update by the main class when in debug mode)
	 */
	public void runTest()
	{
		if (testing) {
			EcologiaIO.debug("Ecotest: running test...");
			//Insert a test method here >>>
			reproduceMidwinter();
		}
	}
	
	/**
	 * Try and reproduce the Midwinter bug (random freezing)
	 */
	public void reproduceMidwinter()
	{
		int turn = World.getInstance().getTurn();
		if (turn == 1) {
			EcologiaIO.debug("Ecotest: Creating a herbivore at (1, 0)");
			Herbivore herbivore = new Herbivore(World.getInstance().getNextID(), 
					Herbivore.defaultGenome, -1, 1, 0, 
					World.getInstance().getStartEnergyHerbivores(), 0);
			Simulator.addAnimal(herbivore);
			EcologiaIO.debug("Ecotest: Creating a carnivore at (1, 3)");
			Carnivore carnivore = new Carnivore(World.getInstance().getNextID(), 
					Carnivore.defaultGenome, -1, 1, 3, 
					World.getInstance().getStartEnergyCarnivores(), 0);
			Simulator.addAnimal(carnivore);
		}
		if (turn == 2) {
			Simulator.getAnimal(1, 3).move(Direction.UP);
			EcologiaIO.debug("Ecotest: herbivore at (1, 0) should flee up -> freeze");
			EcologiaIO.debug("Ecotest: Midwinter bug is fixed, so no more ice ;-)");
		}
	}
	
	/**
	 * Does reproduction work?
	 */
	public void reproductionTest()
	{
		int turn = World.getInstance().getTurn();
		if (turn == 1) {
			EcologiaIO.debug("Creating a carnivore at (1, 1) with 301 energy");
			Carnivore carnivore = new Carnivore(World.getInstance().getNextID(), 
					Carnivore.defaultGenome, -2, 1, 1, 301, 0);
			Simulator.addAnimal(carnivore);
		}
		if (turn == 25) {
			EcologiaIO.debug("Carnivore at (1, 1) should reproduce...");
		}
	}
	
	/**
	 * Test the new setup of the news ticker.
	 */
	public void newsTest()
	{
		EcologiaIO.debug("Testing news ticker...");
		World.getInstance().giveNews("Testing 123...");
	}
	
	/**
	 * Add, move and remove an animal
	 */
	public void testPopulationChanges()
	{
		int turn = World.getInstance().getTurn();
		if (turn == 1) {
			tpcX = 0;
			tpcY = 0;
			EcologiaIO.debug("Creating a carnivore at (0, 0)");
			Carnivore carnivore = new Carnivore(World.getInstance().getNextID(), 
					Carnivore.defaultGenome, -1, tpcX, tpcY, 
					World.getInstance().getStartEnergyCarnivores(), 0);
			Simulator.addAnimal(carnivore);
		}
		else if (turn < 5 && turn > 1) {
			Animal carnivore = Simulator.getAnimal(tpcX, tpcY);
			if (!carnivore.move(Direction.BOTTOM_RIGHT)) {
				EcologiaIO.debug("Failed to move!");
			}
			tpcX = carnivore.getX();
			tpcY = carnivore.getY();
			EcologiaIO.debug("Carnivore is moving right and down to ("+tpcX+", "+tpcY+")");
		}
		else if (turn == 5) {
			EcologiaIO.debug("Deleting carnivore at ("+tpcX+", "+tpcY+")");
			Simulator.removeAnimal(tpcX, tpcY, OccupantType.CARNIVORE);
		}
	}
}
