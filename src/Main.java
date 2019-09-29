import sim.Simulation;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Class to instantiate and run the sim
 *
 * Created by L. Arroyo on 9/12/2019
 */
public class Main
{
    private static final String SIMULATION_FILE_PREFIX = "scenarios/scenario";
    private static final String SIMULATION_FILE_SUFFIX = ".csv";

    public static void main(String [] args)
    {
        int fileNumber = 15;

        final URL url = Main.class.getResource(SIMULATION_FILE_PREFIX + fileNumber + SIMULATION_FILE_SUFFIX);

        File simFile;

        try
        {
            simFile = new File(url.toURI());
        }
        catch(URISyntaxException e)
        {
            throw new RuntimeException(e);
        }

        final Simulation simulation = new Simulation(simFile);

        simulation.start();
    }
}