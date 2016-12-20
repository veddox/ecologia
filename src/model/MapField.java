package model;

import java.util.HashMap;

import controller.Humidity;
import controller.OccupantType;

/**
 * This is a representation of a discrete area (tile) on the map. It monitors 
 * what animals are on it, what it's grass density is, etc.
 * 
 * @author Daniel Vedder
 * @version 30.8.2014
 */
public class MapField 
{
	private int x, y;
	private int grassDensity;
	private boolean isNearWater;
	private Humidity localHumidity;
	private OccupantType occupant;
	
	/**
	 * The constructor.
	 */
	public MapField(int xstart, int ystart, OccupantType newOccupant,
					Humidity startingHumidity, int startingGrassDensity)
	{
		x = xstart;
		y = ystart;
		occupant = newOccupant;
		localHumidity = startingHumidity;
		grassDensity = startingGrassDensity;
		isNearWater = false;
	}
	
	/**
	 * Recalculate the grass density based on humidity values.
	 * Min: 0 Max: 100
	 */
	public void calculateGrassDensity()
	{
		grassDensity = grassDensity + 2*localHumidity.getValue();
		if (grassDensity >= 100) grassDensity = 100;
		else if (grassDensity <= 0)	grassDensity = 0;
		//If this is a water tile, the grass density is always 100
		if (occupant == OccupantType.WATER)	grassDensity = 100;
	}

	/*
	 * Getters and setters
	 */

	/**
	 * Return a hash map containing all the information about this field.
	 */
	public HashMap<String, Integer> getInfo()
	{
		HashMap<String, Integer> info = new HashMap<String, Integer>();
		info.put("X", x);
		info.put("Y", y);
		info.put("Grass density", grassDensity);
		info.put("Local humidity", localHumidity.getValue());
		info.put("Occupant", occupant.toInt());
		return info;
	}
	
	public void setNearWater(boolean newValue)
	{
		isNearWater = newValue;
	}
	
	public boolean nearWater()
	{
		return isNearWater;
	}
	
	public int getGrassDensity() {
		return grassDensity;
	}

	public OccupantType getOccupant() {
		return occupant;
	}

	public void setOccupant(OccupantType occupant) {
		this.occupant = occupant;
	}

	public Humidity getLocalHumidity() {
		return localHumidity;
	}

	public void setLocalHumidity(Humidity localHumidity) {
		this.localHumidity = localHumidity;
	}
	
	public void reduceGrassDensity(int amount)
	{
		grassDensity -= amount;
		if (grassDensity < 0) grassDensity = 0;
	}
	
}
