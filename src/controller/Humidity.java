package controller;

import main.EcologiaIO;

/**
 * The different levels of humidity that are available.
 *  
 * @author Daniel Vedder
 * @version 30.8.2014
 */
public enum Humidity 
{
	SEVERE_DROUGHT,
	DROUGHT,
	DRY,
	WET,
	SATURATION;
	
	/**
	 * Return the numerical value of an entry.
	 */
	public int getValue()
	{
		switch (this) {
			case SEVERE_DROUGHT: return -2; 
			case DROUGHT: return -1;
			case DRY: return 0;
			case WET: return 1;
			case SATURATION: return 2;
			default: return 0;
		}
	}
	
	/**
	 * Return the string representation of an entry.
	 */
	public String getString()
	{
		switch (this) {
			case SEVERE_DROUGHT: return "Severe Drought"; 
			case DROUGHT: return "Drought";
			case DRY: return "Dry";
			case WET: return "Wet";
			case SATURATION: return "Saturation";
			default: return "N/A"; //Cannot be called
		}
	}
	
	/**
	 * Convert an integer into an enum entry
	 */
	public static Humidity fromString(String value)
	{
		switch(value) {
			case "Severe Drought": return Humidity.SEVERE_DROUGHT;
			case "Drought": return Humidity.DROUGHT;
			case "Dry": return Humidity.DRY;
			case "Wet": return Humidity.WET;
			case "Saturation": return Humidity.SATURATION;
			default: EcologiaIO.error("Invalid value entered in Humidity.fromString()!"); return Humidity.DRY;
		}
	}
	
	/**
	 * Convert a number into an enum entry.
	 * @param int value
	 * @return Humidity
	 */
	public static Humidity getStatus(int value)
	{
		switch(value) {
			case -2: return Humidity.SEVERE_DROUGHT;
			case -1: return Humidity.DROUGHT;
			case 0: return Humidity.DRY;
			case 1: return Humidity.WET;
			case 2: return Humidity.SATURATION;
			default: EcologiaIO.error("Invalid value entered in Humidity.getStatus()!"); return Humidity.DRY;
		}
	}
}
