package main;

import controller.World;
import view.GUI;
import model.Simulator;

/**
 * Ecologia is a relatively simple ecosystem simulator, designed to show basic
 * relationships between predators, prey and producers.
 *
 *  Copyright (C) 2014-2016 Daniel Vedder
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * 
 * This is the main class, which launches the application and acts as a
 * manager, updating the program each round.
 *
 * @author Daniel Vedder
 * @version 28.8.2014
 */
public class Ecologia implements Runnable 
{
	public static Ecologia eco; //The singleton object
	public static final String version = "1.1";

	private static boolean noGUI = false;
	
	private GUI gui;
	private Simulator simulator;
	private EcoTest tester;
	
	private Thread runningThread;
	
	/**
	 * Launch the program.
	 * see printHelp() for possible arguments
	 */
	public static void main(String[] args) 
	{		
		//Parse commandline arguments
		int i = 0;
		String a;
		while (i < args.length) {
			a = args[i];
			if (a.equals("--version") || a.equals("-V"))  {
				System.out.println("Ecologia "+version);
				System.exit(0);
			}
			else if (a.equals( "--help") || a.equals("-h")) {
				printHelp();
				System.exit(0);
			}
			else if (a.equals("--logging") || a.equals("-l"))
				EcologiaIO.logging = true;
			else if (a.equals("--verbose") || a.equals("-v"))
				EcologiaIO.verbose = true;
			else if (a.equals("--debug") || a.equals("-d"))
				EcologiaIO.debugging = true;
			else if (a.equals("--analyse") || a.equals("-a"))
				EcologiaIO.analysing = true;
			else if (a.equals("--no-graphics"))
				noGUI = true;
			else if (a.equals("--autorun")) {
				World.getInstance().setAutorun(new Integer(args[i+1]));
				i++;
			}
			else if (a.equals("--config")) {
				World.getInstance().readConfigFile(args[i+1]);
				i++;
			}
			else if (a.equals("--timelapse")) {
				World.getInstance().setTimelapse(new Integer(args[i+1]));
				i++;
			}
			else EcologiaIO.error("Invalid commandline parameter: "+a);
			i++;
		}
		
		//Set up logging
		if (EcologiaIO.logging) EcologiaIO.archiveLog();
		EcologiaIO.printStatus();
		
		//Only use no-graphics mode when on autorun
		if (noGUI && (World.getInstance().getAutorun() < 0)) {
			EcologiaIO.error("Returning to graphics mode as autorun not enabled.");
			noGUI = false;
		}
		else if (noGUI) EcologiaIO.log("Running in no-graphics mode.");

		//Create an instance
		eco = new Ecologia();
	}

	/**
	 * The Singleton method.
	 */
	public static Ecologia getInstance()
	{
		return eco;
	}
	
	/**
	 * Ecologia implements Singleton, so the constructor is private.
	 */
	private Ecologia()
	{
		EcologiaIO.log("Launching Ecologia...");
		simulator = new Simulator();
		if (!noGUI) gui = new GUI();
		tester = new EcoTest();
		EcologiaIO.debug("Launch completed.");
		if (World.getInstance().getAutorun() > 0) autorun();
	}
	
	/**
	 * Perform an automatic run.
	 */
	private void autorun()
	{
		EcologiaIO.log("Performing autorun for "+World.getInstance().getAutorun()+" updates.");
		World.getInstance().setStopAt(-1);
		startThread();
	}

	/**
	 * Reset the simulator in order to start a new run.
	 * 
	 * XXX: Depending on how long the simulation has already
	 * been running, this can take quite a long time.
	 */
	public void reset()
	{
		EcologiaIO.archiveLog();
		EcologiaIO.log("Resetting Ecologia...");
		World.getInstance().reset();
		simulator = null;
		simulator = new Simulator();
		if (!noGUI) {
			gui.reset();
			gui = null;
			gui = new GUI();
			gui.update();
		}
	}
	
	/**
	 * Start the simulation.
	 */
	public void startThread()
	{
  	  World.getInstance().setRunning(true);
  	  runningThread = new Thread(this);
      runningThread.start();
	}

	/**
	 * Run the simulation.
	 */
	public void run() 
	{
		World.getInstance().giveNews("Simulation is running.");
		while (World.getInstance().isRunning()) {
			iterate();
		}
		World.getInstance().giveNews("Simulation has stopped.");
		if (!noGUI) gui.update(); //Make sure the above news is displayed by the GUI
	}
	
	/**
	 * Perform one iteration of the simulation.
	 */
	public synchronized void iterate()
	{
		int autorun = World.getInstance().getAutorun();
		World.getInstance().incrementTurn();
		int turn = World.getInstance().getTurn();
		EcologiaIO.log("Executing update "+turn);
		if (EcologiaIO.debugging) tester.runTest();
		simulator.update();
		EcologiaIO.log("Average grass density: "+World.getInstance().getAverageGrassDensity()+"%");	
		EcologiaIO.log("Herbivore count: "+World.getInstance().getHerbivoreCount());
		EcologiaIO.log("Carnivore count: "+World.getInstance().getCarnivoreCount());
		EcologiaIO.log("Generation counter: "+World.getInstance().getGeneration());

		//If the stopAt number is reached, pause the simulation
		if (World.getInstance().getStopAt() == turn) {
			World.getInstance().setRunning(false);
		}
		if (!noGUI) gui.update();
		//Stop the simulation if there are no more animals
		if (World.getInstance().getCarnivoreCount() == 0 &&
			World.getInstance().getHerbivoreCount() == 0) {
			World.getInstance().setRunning(false);
			if (autorun > 0) turn = autorun;
			else return;
		}
		//Check if an autorun has completed
		if (turn == autorun) {
			EcologiaIO.log("Completed autorun, shutting down.");
			System.exit(0);
		}
		//Pause for as long as the user wants
		try {
			Thread.sleep(World.getInstance().getTimelapse());
		}
		catch (InterruptedException ie) {}
	}
	
	/**
	 * Print a short help text when invoked from the commandline
	 */
	private static void printHelp()
	{
		System.out.println("Ecologia "+version+", the simple ecosystem simulator.");
		System.out.println("\nCommandline options:\n");
		System.out.println("--help    -h	Print this help text");
		System.out.println("--version -V	Print the version number\n");
		System.out.println("--logging -l	Enable logging to file");
		System.out.println("--verbose -v	Give verbose output");
		System.out.println("--debug   -d	Print debugging information");
		System.out.println("--analyse -a	Print simulation analysis information\n");
		System.out.println("--no-graphics	Do not start the GUI (requires --autorun)\n");
		System.out.println("--config <file>	Specify a configuration file to use");
		System.out.println("--autorun <n>	Autorun the simulation for n updates, then quit");
		System.out.println("--timelapse <ms>	Set the timelapse between updates\n");
		System.out.println("Copyright (c) 2014-2016 Daniel Vedder");
		System.out.println("Licensed under the terms of the GNU General Public License v3\n");
	}
}
