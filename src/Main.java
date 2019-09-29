import sim.Simulation;

import java.io.File;

/**
 * Class to instantiate and run the sim
 *
 * Created by L. Arroyo on 9/12/2019
 */
public class Main
{
    private static final String SIMULATION_FILE_PREFIX = "scenarios/scenario";
    private static final String SIMULATION_FILE_SUFFIX = ".csv";
    private static final boolean USE_TEST_FILES = false;

    public static void main(String [] args)
    {
        if(!USE_TEST_FILES && args.length < 1)
        {
            throw new RuntimeException("[FATAL ERROR] :: main - Cannot load file from args");
        }

        if(USE_TEST_FILES)
        {
            for(int i = 0; i < 16; i++)
            {
                final File simFile = new File(SIMULATION_FILE_PREFIX + i + SIMULATION_FILE_SUFFIX);

                final Simulation simulation = new Simulation(simFile, true); // DISPLAY LESS CRYPTIC READOUT

                simulation.start();
            }
        }
        else{
            File simFile = new File(args[0].trim());

            final Simulation simulation = new Simulation(simFile, false); // DISPLAY THE CLASS FORMAT

            simulation.start();
        }
    }
}