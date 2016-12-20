package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;
import javax.swing.Scrollable;

import main.EcologiaIO;
import controller.OccupantType;
import controller.World;

/**
 * This class provides a graphical representation of the simulation.
 * 
 * @author Daniel Vedder
 * @version 29.8.2014
 */
public class Display extends JPanel implements Scrollable, MouseListener
{
	private int[] size;
	private InfoBox infobox;
	
	/**
	 * The constructor
	 * @param int[2] size
	 */
	public Display(int[] setSize)
	{
		EcologiaIO.debug("Display: initialising.");
		size = setSize;
		this.setSize(size[0]*20, size[1]*20);
		this.setPreferredSize(new Dimension(size[0]*20, size[1]*20));
		this.setBackground(Color.GRAY);
		infobox = new InfoBox();
		this.addMouseListener(this);
	}
	
	/**
	 * Update the display
	 */
	public void update()
	{
		repaint();
		infobox.refresh();
	}
	
	/**
	 * Draw the current status of the simulation onto the panel.
	 */
	public void paintComponent(Graphics g)
	{
		for (int x = 0; x < size[0]; x++) {
			for (int y = 0; y < size[1]; y++) {
				//the grass density on it affects the colour of the tile
				if (World.getInstance().getFieldInfo(x, y).get("Grass density") > 20) {
					g.setColor(Color.green);
				}
				else if ((World.getInstance().getFieldInfo(x, y).get("Grass density") <= 20)
						 && (World.getInstance().getFieldInfo(x, y).get("Grass density") > 0)) {
					g.setColor(Color.yellow);
				}
				else {
					g.setColor(Color.white);
				}
				g.fillRect(x*20, y*20, 20, 20);//colour the tiles
				g.setColor(Color.black);
				g.drawRect(x*20, y*20, 20, 20);//draw the tiles as squares
				//draw in any animal occupants of the tile, or a water tile
				if (OccupantType.fromInt(World.getInstance().getFieldInfo(x, y).get("Occupant"))
					== OccupantType.CARNIVORE) {
					g.setColor(Color.red);
					g.fillOval(x*20+4, y*20+4, 12, 12);
				}
				else if (OccupantType.fromInt(World.getInstance().getFieldInfo(x, y).get("Occupant"))
						 == OccupantType.HERBIVORE) {
					g.setColor(Color.gray);
					g.fillOval(x*20+4, y*20+4, 12, 12);
				}
				else if (OccupantType.fromInt(World.getInstance().getFieldInfo(x, y).get("Occupant"))
						 == OccupantType.WATER) {
					g.setColor(Color.blue);
					g.fillRect(x*20+2, y*20+2, 16, 16);
				}
			}
		}
	}

	/**
	 * Return the current infobox instance
	 */
	public InfoBox getInfoBox()
	{
		return infobox;
	}

	//Override methods from the Scrollable and MouseListener interfaces
	
	@Override
	public void mouseClicked(MouseEvent click) {
		int fieldX = click.getX()/20;
		int fieldY = click.getY()/20;
		if (fieldX >= 0 && fieldX < World.getInstance().getSize()[0] && fieldY >= 0
			&& fieldY < World.getInstance().getSize()[1]) {
			infobox.show(click.getX()/20, click.getY()/20);
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// Auto-generated method stub
		
	}

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		// Auto-generated method stub
		return null;
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle arg0, int arg1, int arg2) {
		// Auto-generated method stub
		return 0;
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		// Auto-generated method stub
		return false;
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		// Auto-generated method stub
		return false;
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle arg0, int arg1, int arg2) {
		// Auto-generated method stub
		return 0;
	}
}
