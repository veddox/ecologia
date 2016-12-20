package model;

import java.util.Random;


/**
 * A list of directions and common methods related to them 
 * is often needed by animals.
 * 
 * @author Daniel Vedder
 * @version 26.12.2014
 */
public enum Direction
{
	UP, DOWN, LEFT, RIGHT, TOP_RIGHT, TOP_LEFT, BOTTOM_LEFT, BOTTOM_RIGHT, CENTER;
	
	/**
	 * Return the opposite direction
	 */
	public Direction oppositeDirection()
	{
		switch (this) {
			case UP: return DOWN;
			case DOWN: return UP;
			case RIGHT: return LEFT;
			case LEFT: return RIGHT;
			case TOP_LEFT: return BOTTOM_RIGHT;
			case BOTTOM_LEFT: return TOP_RIGHT;
			case TOP_RIGHT: return BOTTOM_LEFT;
			case BOTTOM_RIGHT: return TOP_LEFT;
			default: return CENTER;
		}
	}
	
	/**
	 * Return the next direction, going clockwise (if cw = true)
	 * or anticlockwise (if cw = false)
	 * @param clockwise
	 */
	public Direction nextDirection(boolean clockwise)
	{
		if (clockwise) {
			switch (this) {
				case UP: return TOP_RIGHT;
				case TOP_RIGHT: return RIGHT;
				case RIGHT: return BOTTOM_RIGHT;
				case BOTTOM_RIGHT: return DOWN;
				case DOWN: return BOTTOM_LEFT;
				case BOTTOM_LEFT: return LEFT;
				case LEFT: return TOP_LEFT;
				case TOP_LEFT: return UP;
				default: return CENTER;
			}
		}
		else {
			switch (this) {
				case UP: return TOP_LEFT;
				case TOP_LEFT: return LEFT;
				case LEFT: return BOTTOM_LEFT;
				case BOTTOM_LEFT: return DOWN;
				case DOWN: return BOTTOM_RIGHT;
				case BOTTOM_RIGHT: return RIGHT;
				case RIGHT: return TOP_RIGHT;
				case TOP_RIGHT: return UP;
				default: return CENTER;
			}
		}
	}
	
	/**
	 * Return a random direction
	 */
	public static Direction randomDirection()
	{
		Random r = new Random();
		return fromInt(r.nextInt(8));
	}
	
	/**
	 * Return the direction that this number refers to.
	 */
	public static Direction fromInt(int d)
	{
		switch (d) {
			case 0: return UP;
			case 1: return TOP_RIGHT;
			case 2: return RIGHT;
			case 3: return BOTTOM_RIGHT;
			case 4: return DOWN;
			case 5: return BOTTOM_LEFT;
			case 6: return LEFT;
			case 7: return TOP_LEFT;
			default: return CENTER;
		}
	}
	
	/**
	 * Return a string representation of this direction.
	 */
	public String getString()
	{
		switch (this) {
			case UP: return "up";
			case DOWN: return "down";
			case RIGHT: return "right";
			case LEFT: return "left";
			case TOP_LEFT: return "top left";
			case BOTTOM_LEFT: return "bottom left";
			case TOP_RIGHT: return "top right";
			case BOTTOM_RIGHT: return "bottom right";
			default: return "center";
		}
	}
}
