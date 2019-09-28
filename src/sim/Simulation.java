package sim;

import constant.Direction;
import constant.LawnSquareContent;
import constant.MowerMovementType;
import constant.SimulationRiskProfile;
import lawn.Lawn;
import lawn.LawnSquare;
import mower.Mower;
import mower.MowerMove;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Simulation class to handle the simulation
 *
 * Created by L. Arroyo on 9/12/2019
 */

public class Simulation
{
    // FIELDS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private final File simFile;

    private int maxTurns;
    private int turnsTaken;

    private int lawnArea;
    private int startingGrassToCut;
    private int totalGrassCut;

    private int activeMowers;

    private Lawn lawn;
    private List<Mower> mowers;

    private SimulationRiskProfile simulationRiskProfile;

    // CONSTRUCTORS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public Simulation(final File simFile)
    {
        this.simFile = simFile;
        this.turnsTaken = 0;
        this.totalGrassCut = 0;
        this.mowers = new ArrayList<>();
    }

    // PUBLIC METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Kicks off the simulation
     */
    public void start()
    {
        parseFile();

        displayStartingSimInfo();
        displayHorizontalRule(false);

        while(turnsTaken < maxTurns
                && totalGrassCut < startingGrassToCut
                && activeMowers > 0)
        {
            System.out.println("Turn " + (++turnsTaken) + ":" + "\n");

            boolean ismoveEligible = true;

            for(Mower mower : mowers)
            {
                if(!mower.isDisabled())
                {
                    MowerMove move = mower.determineMove(ismoveEligible);

//                    if(move.getMowerMovementType() == MowerMovementType.MOVE
//                            && simulationRiskProfile == SimulationRiskProfile.LOW)
//                    {
//                        ismoveEligible = false;
//                    }

                    displayMowerMove(move);

                    mower.makeMove(move);
                }

                if(totalGrassCut == startingGrassToCut || activeMowers == 0)
                {
                    break;
                }
            }

            displayHorizontalRule(true);
        }

        displayFinalResults();
    }

    /**
     * Gets the content of a particular lawn square based on the x and y coordinates
     *
     * @param xCoor - The x coordinate
     * @param yCoor - The y coordinate
     *
     * @return - The lawn square content if it exists, if not then a content type of fence will be returned
     */
    public LawnSquareContent getLawnSquareContent(final int xCoor, final int yCoor)
    {
        final LawnSquare lawnSquare = lawn.getLawnSquareByCoordinates(xCoor, yCoor);

        return lawnSquare == null ? LawnSquareContent.FENCE : lawnSquare.getLawnSquareContent();
    }

    /**
     * Checks to see if a mower move is valid
     *
     * Note:
     * This method just checks to see if the move is valid for reference, the move is going to be made regardless
     * and this method will force the simulation state to be updated
     *
     * @param move - The mower move that is being attempted
     *
     * @return - True if the move is valid, false otherwise
     */
    public boolean isValidMove(final MowerMove move)
    {
        boolean response = true;

        // STEER, SCAN , AND PASS WILL ALWAYS BE VALID MOVES BECAUSE THEY DON'T ACTUALLY CHANGE THE MOWERS POSITION
        if(move.getMowerMovementType() == MowerMovementType.MOVE)
        {
            LawnSquare square = lawn.getLawnSquareByCoordinates(move.getNewXCoordinate(), move.getNewYCoordinate());

            if(square.getLawnSquareContent() == LawnSquareContent.FENCE
                    || square.getLawnSquareContent() == LawnSquareContent.CRATER
                    || square.getLawnSquareContent() == LawnSquareContent.MOWER)
            {
                response = false;
            }
        }

        updateSimState(move);

        return response;
    }

    // ACCESS METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public SimulationRiskProfile getSimulationRiskProfile() {
        return simulationRiskProfile;
    }

    // PRIVATE METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Parses the simulation file and creates the initial setup for the simulation
     */
    private void parseFile()
    {
        BufferedReader reader;

        try
        {
            // 1. CONVERT THE FILE INTO A COLLECTION OF LINES
            final List<String> lines = new ArrayList<>();

            reader = new BufferedReader(new FileReader(simFile));

            String line = reader.readLine();

            while(line != null)
            {
                if(!line.trim().isEmpty())
                {
                    lines.add(line);
                }

                line = reader.readLine();
            }

            reader.close();

            // 2. PROCESS THE BASIC LAWN DIMENSIONS
            final int x = Integer.parseInt(lines.get(0).trim());
            final int y = Integer.parseInt(lines.get(1).trim());

            lawn = new Lawn(x, y);

            lawnArea = x * y;

            for(int i = 0; i < x; i++)
            {
                for(int j = 0; j < y; j++)
                {
                    lawn.addLawnSquare(new LawnSquare(i, j, LawnSquareContent.GRASS)); // DEFAULT TO GRASS
                }
            }

            // 3. PROCESS THE MOWER INFO
            activeMowers = Integer.parseInt(lines.get(2).trim()); // ASSUMES ALL MOWERS LISTED START ACTIVE

            int idx = 3;

            for(int i = 0; i < activeMowers; i++)
            {
                String [] mowerInfo = lines.get(idx++).trim().split(",");

                for(Direction direction : Direction.values())
                {
                    if(direction.name().equalsIgnoreCase(mowerInfo[2]))
                    {
                        String mowerName = "MOWER " + (i+1);
                        int mowerX = Integer.parseInt(mowerInfo[0].trim());
                        int mowerY = Integer.parseInt(mowerInfo[1].trim());

                        mowers.add(new Mower(mowerName, direction, mowerX, mowerY, this));
                    }
                }
            }

            // 4. PROCESS THE OBSTACLE INFO
            final int obstacleCount = Integer.parseInt(lines.get(idx++).trim());

            for(int i = 0; i < obstacleCount; i++)
            {
                String [] obstacleInfo = lines.get(idx++).trim().split(",");

                int obstacleX = Integer.parseInt(obstacleInfo[0].trim());
                int obstacleY = Integer.parseInt(obstacleInfo[1].trim());

                lawn.getLawnSquareByCoordinates(obstacleX, obstacleY)
                        .setLawnSquareContent(LawnSquareContent.CRATER); // ASSUMES ONLY CRATERS CAN BE OBSTACLES
            }

            // 5. PROCESS THE MAX TURNS INFO
            maxTurns = Integer.parseInt(lines.get(idx).trim());

            // 6. DETERMINE THE STARTING GRASS TO CUT TOTAL BEFORE MOWING INITIAL GRASS
            for(LawnSquare lawnSquare : lawn.getLawnSquares())
            {
                if(lawnSquare.getLawnSquareContent() == LawnSquareContent.GRASS)
                {
                    startingGrassToCut++;
                }
            }

            // 7. MOW THE GRASS WHERE THE MOWERS INITIALLY BEGIN
            for(Mower mower : mowers)
            {
                LawnSquare square = lawn.getLawnSquareByCoordinates(mower.getXCoordinate(), mower.getYCoordinate());

                square.setLawnSquareContent(LawnSquareContent.EMPTY);

                totalGrassCut++;
            }

            // 8. SET THE INITIAL SIMULATION RISK PROFILE
            determineSimulationRiskProfile();
        }
        catch(FileNotFoundException e)
        {
            String errorMsg = "[ERROR] - Cannot parse the sim file because it could not be located";

            System.out.println(errorMsg);

            throw new RuntimeException(errorMsg);
        }
        catch(Exception e)
        {
            String errorMsg = "[ERROR] - An unknown error occurred while trying to parse sim file | " + e.getMessage();

            System.out.println(errorMsg);

            throw new RuntimeException(e);
        }
    }

    /**
     * Determines the risk profile for the simulation, this profile is used to determine how
     * reckless the simulation will allow the mowers to be
     *
     * @return - The simulation risk profile
     */
    private void determineSimulationRiskProfile()
    {
         final int remainingMoves = (maxTurns - turnsTaken) * activeMowers;

         final int remainingGrass = startingGrassToCut - totalGrassCut;

         final int riskFactor = remainingMoves / remainingGrass;

         if(riskFactor >= 5)
         {
             updateSimulationRiskProfile(SimulationRiskProfile.LOW);
         }
         else if(riskFactor >= 3)
         {
             updateSimulationRiskProfile(SimulationRiskProfile.MODERATE);
         }
         else{
             updateSimulationRiskProfile(SimulationRiskProfile.AGGRESSIVE);
         }
    }

    /**
     * Updates the simulation risk profile if it is different than the current profile
     *
     * @param newProfile - The profile to update to
     */
    private void updateSimulationRiskProfile(final SimulationRiskProfile newProfile)
    {
        if(simulationRiskProfile != newProfile)
        {
            simulationRiskProfile = newProfile;

            System.out.println("\nSetting the Sim Risk Profile to " + newProfile + "\n");
        }
    }

    /**
     * Updates the state of the simulation for movement types which may affect more than just the mower involved.
     * This only applies to when the mower actually attempts to move forward, because that has the potential to
     * affect other objects such as mowers, lawn squares, the simulation, etc.
     *
     * The mower is responsible for updating itself when the move is STEER, SCAN, or PASS
     *
     * @param move - The mower move that was just made
     */
    private void updateSimState(final MowerMove move)
    {
        if(move.getMowerMovementType() == MowerMovementType.MOVE)
        {
            LawnSquare newSquare = lawn.getLawnSquareByCoordinates(move.getNewXCoordinate(), move.getNewYCoordinate());

            LawnSquare oldSquare =
                    lawn.getLawnSquareByCoordinates(move.getCurrentXCoordinate(), move.getCurrentYCoordinate());

            oldSquare.setLawnSquareContent(LawnSquareContent.EMPTY);

            if(newSquare.getLawnSquareContent() == LawnSquareContent.EMPTY)
            {
                newSquare.setLawnSquareContent(LawnSquareContent.MOWER);
            }
            else if(newSquare.getLawnSquareContent() == LawnSquareContent.GRASS)
            {
                newSquare.setLawnSquareContent(LawnSquareContent.MOWER);

                totalGrassCut++;
            }
            else if(newSquare.getLawnSquareContent() == LawnSquareContent.FENCE)
            {
                // THE MOWER WILL HANDLE DE-ACTIVATING THE ACTUAL MOWER
                System.out.println(move.getMowerName() + " was involved in a collision with a fence at ("
                        + move.getNewXCoordinate() + "," + move.getNewYCoordinate() + ")");

                activeMowers--;
            }
            else if(newSquare.getLawnSquareContent() == LawnSquareContent.CRATER)
            {
                // THE MOWER WILL HANDLE DE-ACTIVATING THE ACTUAL MOWER
                System.out.println(move.getMowerName() + " was involved in a collision with a crater at ("
                        + move.getNewXCoordinate() + "," + move.getNewYCoordinate() + ")");

                newSquare.setLawnSquareContent(LawnSquareContent.EMPTY);

                activeMowers--;
            }
            else if(newSquare.getLawnSquareContent() == LawnSquareContent.MOWER)
            {
                for(Mower mower : mowers)
                {
                    if((mower.getXCoordinate() == move.getNewXCoordinate()
                            && mower.getYCoordinate() == move.getNewYCoordinate())
                            || mower.getName().equals(move.getMowerName()))
                    {
                        System.out.println(mower.getName() + " was involved in a collision with another mower at ("
                                + move.getNewXCoordinate() + "," + move.getNewYCoordinate() + ")");

                        mower.disableMower();

                        activeMowers--;
                    }
                }

                newSquare.setLawnSquareContent(LawnSquareContent.EMPTY);
            }
        }
    }

    /**
     * Displays the starting simulation information
     */
    private void displayStartingSimInfo()
    {
        System.out.println("Starting the simulation\n");
        System.out.println("Lawn area: " + lawnArea);
        System.out.println("Total grass to cut: " + startingGrassToCut);
        System.out.println("Identified obstacles: " + (lawnArea - startingGrassToCut));
        System.out.println("Number of mowers: " + activeMowers);
        System.out.println("Maximum turn limit: " + maxTurns);
    }

    /**
     * Displays a mower move by the mower
     *
     * @param mowerMove - The mower move
     */
    private void displayMowerMove(final MowerMove mowerMove)
    {
        final StringBuilder sb = new StringBuilder();

        if(mowerMove.getMowerMovementType() == MowerMovementType.MOVE)
        {
            sb.append(mowerMove.getMowerName() + " is moving " + mowerMove.getDirection() + " from ");
            sb.append("(" + mowerMove.getCurrentXCoordinate() + "," + mowerMove.getCurrentYCoordinate() + ")");
            sb.append(" to (" + mowerMove.getNewXCoordinate() + "," + mowerMove.getNewYCoordinate() + ")");
        }
        else if(mowerMove.getMowerMovementType() == MowerMovementType.SCAN)
        {
            sb.append(mowerMove.getMowerName() + " is scanning while located at ");
            sb.append("(" + mowerMove.getCurrentXCoordinate() + "," + mowerMove.getCurrentYCoordinate() + ")");
        }
        else if(mowerMove.getMowerMovementType() == MowerMovementType.STEER)
        {
            sb.append(mowerMove.getMowerName() + " is changing directions at ");
            sb.append("(" + mowerMove.getCurrentXCoordinate() + "," + mowerMove.getCurrentYCoordinate() + ")");
            sb.append(" to face " + mowerMove.getDirection());
        }
        else if(mowerMove.getMowerMovementType() == MowerMovementType.PASS)
        {
            sb.append(mowerMove.getMowerName() + " is passing while at ");
            sb.append("(" + mowerMove.getCurrentXCoordinate() + "," + mowerMove.getCurrentYCoordinate() + ")");
        }

        System.out.println(sb.toString());
    }

    /**
     * Displays the final results after the simulation has been run
     */
    private void displayFinalResults()
    {
        StringBuilder sb = new StringBuilder("The simulation has ended, the final results are:\n");

        sb.append("\nTotal Lawn Area: " + lawnArea);
        sb.append("\nGrass To Cut: " + startingGrassToCut);
        sb.append("\nGrass Cut: " + totalGrassCut);
        sb.append("\nTurns: " + turnsTaken);

        System.out.println(sb.toString());

        // USE THIS BLOCK WHEN LETTING THE SIMULATION RUN UNTIL SUCCESS
//        if(totalGrassCut == startingGrassToCut)
//        {
//            throw new RuntimeException("Success");
//        }
    }

    /**
     * Prints a horizontal line on the screen with a new line after the divider
     *
     * @param startingGap - True if the statement should use a new line command before printing the divider
     */
    private void displayHorizontalRule(final boolean startingGap)
    {
        final String line = "_____________________________________________________________________________________\n";

        System.out.println(startingGap? "\n" + line : line);
    }
}