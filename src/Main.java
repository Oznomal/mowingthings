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
    private static final String SIMULATION_FILE_LOCATION = "/simulation.txt";

    public static void main(String [] args)
    {
        int iterations = 0;

        final URL url = Main.class.getResource(SIMULATION_FILE_LOCATION);

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

//        try
//        {
//            while(true)
//            {
//                iterations++;
//
//                final URL url = Main.class.getResource(SIMULATION_FILE_LOCATION);
//
//                File simFile;
//
//                try
//                {
//                    simFile = new File(url.toURI());
//                }
//                catch(URISyntaxException e)
//                {
//                    throw new RuntimeException(e);
//                }
//
//                final Simulation simulation = new Simulation(simFile);
//
//                simulation.start();
//            }
//        }
//        catch(Exception e)
//        {
//            System.out.println("Successfully had a run, it took " + iterations + " attempts");
//        }
    }
}