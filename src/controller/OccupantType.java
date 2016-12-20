package controller;

/**
 * This is a list of all the possible elements that can occupy a field.
 * 
 * @author Daniel Vedder
 * @version 29.8.2014
 */
public enum OccupantType 
{
	NONE,
	HERBIVORE,
	CARNIVORE,
	WATER;
	
	/**
	 * Convert an enum entry to an integer
	 */
	public int toInt()
	{
		switch (this) {
			case NONE: return 0; 
			case HERBIVORE: return 1;
			case CARNIVORE: return 2;
			case WATER: return 3;
			default: return -1; //Cannot be called
		}
	}
	
	/**
	 * Convert the corresponding enum entry for this integer
	 */
	public static OccupantType fromInt(int i)
	{
		switch (i) {
			case 1: return HERBIVORE;
			case 2: return CARNIVORE;
			case 3: return WATER;
			default: return NONE;
		}
	}
	
	/**
	 * Return the string representation of an entry.
	 * 
	 * @override toString() in Object
	 */
	public String toString()
	{
		switch (this) {
			case NONE: return "None"; 
			case HERBIVORE: return "Herbivore";
			case CARNIVORE: return "Carnivore";
			case WATER: return "Water";
			default: return "N/A"; //Cannot be called
		}
	}
	
	/**
	 * Transform a string into an occupant type
	 */
	public static OccupantType fromString(String s)
	{
		switch (s) {
			case "Herbivore": return HERBIVORE;
			case "Carnivore": return CARNIVORE;
			case "Water": return WATER;
			default: return NONE;
		}
	}
}
