package bgu.spl.mics.application;

import bgu.spl.mics.StatsManager;

/**
 * The main entry point for the GurionRock Pro Max Ultra Over 9000 simulation.
 * <p>
 * This class initializes the system and starts the simulation by setting up
 * services, objects, and configurations.
 * </p>
 */
public class GurionRockRunner {

    /**
     * The main method of the simulation. This method sets up the necessary
     * components, parses configuration files, initializes services, and starts
     * the simulation.
     *
     * @param args Command-line arguments. The first argument is expected to be
     * the path to the configuration file.
     */
    public static void main(String[] args) {
//implement countdownlatch so the tick will start only after initiallize of everyone

        StatsManager.initialize();

    }
}
