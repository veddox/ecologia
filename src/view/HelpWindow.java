package view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.io.*;

import main.Ecologia;
import main.EcologiaIO;

/**
 * This window displays the help file for Ecologia.
 * 
 * @author Daniel Vedder
 * @version 03.03.2015
 */
@SuppressWarnings("serial")
public class HelpWindow extends JFrame 
{
	JTextArea text;
	JScrollPane scroller;
	Box main_panel, button_panel;
	JButton help, concepts, license;
	
	public HelpWindow()
	{
		this.setTitle("Help");
		this.setSize(580, 450);
		this.setLocation(300, 150);
		this.setDefaultCloseOperation(HIDE_ON_CLOSE);
		createGUI();
		loadDocFile("help");
	}
	
	/**
	 * Add the text area which will display the text and the buttons to choose
	 * which text to display.
	 */
	public void createGUI()
	{
		//Add the text area
		main_panel = new Box(BoxLayout.Y_AXIS);
		text = new JTextArea();
		text.setEditable(false);
		text.setLineWrap(true);
		text.setWrapStyleWord(true);
		scroller = new JScrollPane(text, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
								   ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		main_panel.add(scroller);
		main_panel.add(Box.createVerticalStrut(5));
		//Add the buttons
		help = new JButton("Help");
		concepts = new JButton("Concepts");
		license = new JButton("License");
		help.addActionListener(new ActionListener()
        {
	          public void actionPerformed(ActionEvent e)
	          {
	              loadDocFile("help");
	          }
	    });
		concepts.addActionListener(new ActionListener()
        {
	          public void actionPerformed(ActionEvent e)
	          {
	              loadDocFile("concepts");
	          }
	    });
		license.addActionListener(new ActionListener()
        {
	          public void actionPerformed(ActionEvent e)
	          {
	              loadDocFile("COPYING");
	          }
	    });
		button_panel = new Box(BoxLayout.X_AXIS);
		button_panel.add(Box.createVerticalStrut(3));
		button_panel.add(help);
		button_panel.add(Box.createVerticalStrut(3));
		button_panel.add(concepts);
		button_panel.add(Box.createVerticalStrut(3));
		button_panel.add(license);
		button_panel.add(Box.createVerticalStrut(3));
		main_panel.add(button_panel);
		this.add(main_panel, BorderLayout.CENTER);
		//Add some fillers for the optics
		this.add(new JPanel(), BorderLayout.NORTH);
		this.add(new JPanel(), BorderLayout.EAST);
		this.add(new JPanel(), BorderLayout.WEST);
		this.add(new JPanel(), BorderLayout.SOUTH);
	}
	
	/**
	 * Load a documentation file.
	 * @param String fileName
	 */
	public void loadDocFile(String filename)
	{
		String helptext = "";
		try {
			InputStreamReader isr = new InputStreamReader(getClass().getResourceAsStream("/doc/"+filename));
			BufferedReader helpfile_reader = new BufferedReader(isr);
			String line = helpfile_reader.readLine();
			while (line != null) {
				helptext = helptext+line+"\n";
				line = helpfile_reader.readLine();
			}
			helpfile_reader.close();
		}
		catch (IOException ioe) {
			helptext = "Error loading file!";
			EcologiaIO.error("HelpWindow: could not load file 'doc/"+filename+"'!", ioe);
		}
		text.setText(helptext);
		text.setCaretPosition(0);
	}
}
