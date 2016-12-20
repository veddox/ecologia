package model;

import java.util.HashMap;
import java.util.Random;

import main.EcologiaIO;
import controller.OccupantType;

/**
 * A genome holds a number of variables ("genes") that determine an animals characteristics.
 * Note: this class has three constructors!
 * 
 * @author Daniel Vedder
 * @version 30.8.2014
 */
public class Genome 
{
	private int mutationRate; //The probability of a mutation occurring in percent.
	private int speed; //How fast is the animal (fields/update)?
	//XXX Remove stamina again?
	private int stamina; //For how long can this animal keep moving before it needs a rest?
	private int sight; //How far can the animal see (fields distant)?
	private int metabolism; //How efficient is it's metabolism?
	private int ageLimit; //The age at which it will die of old age
	private int strength; //How strong is it in a fight
	private int reproductiveEnergy; //How much energy it needs before it will reproduce - 50% will be transferred to the child
	private int maturityAge; //The age at which it reaches sexual maturity
	private int gestation; //The minimum time needed for the reproductive cycle
	private int reproductionRate; //How many offspring are produced at once?
	
	private final int DEFAULT_MUTATION_RATE = 0; //Suggested default: 0
	
	private static Genome herbivoreGenome, carnivoreGenome;
	
	private Random random;
	
	/**
	 * The default constructor provides a standard genome.
	 */
	public Genome()
	{
		mutationRate = 5;
		speed = 1;
		stamina = 10;
		sight = 3;
		metabolism = 10;
		ageLimit = 180;
		strength = 10;
		reproductiveEnergy = 140;
		maturityAge = 20;
		gestation = 10;
		reproductionRate = 1;
	}
	
	/**
	 * This constructor creates a new genome based on the parent genome passed 
	 * to it, mutating it at random.
	 */
	public Genome(Genome parentGenome)
	{
		random = new Random();
		/* Before we can mutate the mutation rate, we need to know a 
		 * preliminary mutation rate or we get a NullPointerException
		 */
		mutationRate = DEFAULT_MUTATION_RATE;
		// Mutate the parent's genes to get this genome
		// XXX Warning: magic numbers!
		mutationRate = parentGenome.getMutationRate()+mutation(1);
		speed = parentGenome.getSpeed()+mutation(1);
		stamina = parentGenome.getStamina()+mutation(1);
		sight = parentGenome.getSight()+mutation(1);
		metabolism = parentGenome.getMetabolism()+mutation(1);
		ageLimit = parentGenome.getAgeLimit()+mutation(10);
		strength = parentGenome.getStrength()+mutation(1);
		reproductiveEnergy = parentGenome.getReproductiveEnergy()+mutation(10);
		maturityAge = parentGenome.getMaturityAge()+mutation(1);
		gestation = parentGenome.getGestation()+mutation(1);
		reproductionRate = parentGenome.getReproductionRate()+mutation(1);
		checkGenome();
	}
	
	/**
	 * This constructor creates a genome from the values passed to it.
	 */
	public Genome(int mutationRate, int speed, int stamina, int sight, int metabolism,
				  int ageLimit, int strength, int reproductiveEnergy, int maturityAge,
				  int gestation, int reproductionRate)
	{
		this.mutationRate = mutationRate;
		this.speed = speed;
		this.stamina = stamina;
		this.sight = sight;
		this.metabolism = metabolism;
		this.ageLimit = ageLimit;
		this.strength = strength;
		this.reproductiveEnergy = reproductiveEnergy;
		this.maturityAge = maturityAge;
		this.gestation = gestation;
		this.reproductionRate = reproductionRate;
		checkGenome();
	}
	
	/**
	 * This constructor creates a genome from a HashMap.
	 */
	public Genome(HashMap<String, Integer> genVars)
	{
		this.mutationRate = genVars.get("mutationRate");
		this.speed = genVars.get("speed");
		this.stamina = genVars.get("stamina");
		this.sight = genVars.get("sight");
		this.metabolism = genVars.get("metabolism");
		this.ageLimit = genVars.get("ageLimit");
		this.strength = genVars.get("strength");
		this.reproductiveEnergy = genVars.get("reproductiveEnergy");
		this.maturityAge = genVars.get("maturityAge");
		this.gestation = genVars.get("gestation");
		this.reproductionRate = genVars.get("reproductionRate");
		checkGenome();
	}
	
	/**
	 * Returns a mutation factor depending on the specified mutation rate.
	 * @param coefficient Influences the size of the returned factor.
	 * @return factor The wanted mutation factor.
	 */
	private int mutation(int coefficient)
	{
		int factor = 0;
		if (random.nextInt(100) < mutationRate) { //Does a mutation take place?
			if (random.nextInt(2) == 0) { //If yes there is a 50% chance of...
				factor = factor+coefficient;  //...adding the coefficient to the factor
			}
			else {
				factor = factor-coefficient; //...subtracting the coefficient from the factor
			}
		}
		return factor; //return the (perhaps) mutated factor
	}
	
	/**
	 * Check to make sure that no "gene" has a value below zero
	 */
	private void checkGenome()
	{
		if (mutationRate < 0) mutationRate = 0;
		if (speed < 0) speed = 0;
		if (sight < 0) sight = 0;
		if (metabolism < 0) metabolism = 0;
		if (ageLimit < 0) ageLimit = 0;
		if (strength < 0) strength = 0;
		if (reproductiveEnergy < 0) reproductiveEnergy = 0;
		if (maturityAge < 0) maturityAge = 0;
		if (gestation < 0) gestation = 0;
		if (reproductionRate < 0) reproductionRate = 0;
	}
	
	/**
	 * Return all the "genes" of this genome in a single HashMap.
	 * @return genomeInfo
	 */
	public HashMap<String, Integer> asHashMap()
	{
		HashMap<String, Integer> genomeInfo = new HashMap<String, Integer>();
		genomeInfo.put("mutationRate", mutationRate);
		genomeInfo.put("speed", speed);
		genomeInfo.put("stamina", stamina);
		genomeInfo.put("sight", sight);
		genomeInfo.put("metabolism", metabolism);
		genomeInfo.put("ageLimit", ageLimit);
		genomeInfo.put("strength", strength);
		genomeInfo.put("reproductiveEnergy", reproductiveEnergy);
		genomeInfo.put("maturityAge", maturityAge);
		genomeInfo.put("gestation", gestation);
		genomeInfo.put("reproductionRate", reproductionRate);
		return genomeInfo;
	}
	
	/*
	 * The Getters for each "gene"
	 * XXX Are these invalidated with asHashMap()?
	 */
	
	public int getMutationRate() 
	{
		return mutationRate;
	}

	public int getSpeed() 
	{
		return speed;
	}
	
	public int getStamina()
	{
		return stamina;
	}

	public int getSight() 
	{
		return sight;
	}

	public int getMetabolism() 
	{
		return metabolism;
	}

	public int getAgeLimit() 
	{
		return ageLimit;
	}

	public int getStrength() 
	{
		return strength;
	}

	public int getReproductiveEnergy() 
	{
		return reproductiveEnergy;
	}

	public int getMaturityAge() 
	{
		return maturityAge;
	}

	public int getGestation() 
	{
		return gestation;
	}

	public int getReproductionRate()
	{
		return reproductionRate;
	}
	
}
