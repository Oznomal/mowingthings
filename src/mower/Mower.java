package mower;

import constant.Direction;
import constant.LawnSquareContent;
import constant.MowerMovementType;
import constant.SimulationRiskProfile;
import sim.Simulation;

import java.util.*;

/**
 * Class that represents an actual mower
 *
 * Created by L. Arroyo on 9/11/2019
 */

public class Mower
{
    // FIELDS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private final Simulation simulation;
    private final String name;

    private Direction direction;
    private int xCoordinate;
    private int yCoordinate;
    private boolean isDisabled;
    private int turnsSinceLastScan;
    private List<LawnSquareContent> surroundingSquares;

    // CONSTRUCTORS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public Mower(String name, Direction direction, int xCoordinate, int yCoordinate, Simulation simulation)
    {
        this.name = name;
        this.direction = direction;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
        this.simulation = simulation;
        this.isDisabled = false;
        this.surroundingSquares = new ArrayList<>(Collections.nCopies(8, LawnSquareContent.UNKNOWN));
        this.turnsSinceLastScan = 0;
    }

    // ACCESS METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public String getName() {
        return name;
    }

    public Direction getDirection() {
        return direction;
    }

    public int getXCoordinate() {
        return xCoordinate;
    }

    public int getYCoordinate() {
        return yCoordinate;
    }

    public boolean isDisabled() {
        return isDisabled;
    }

    public void setDisabled(boolean disabled) {
        isDisabled = disabled;
    }

    public List<LawnSquareContent> getSurroundingSquares()
    {
        return surroundingSquares;
    }

    public int getTurnsSinceLastScan() {
        return turnsSinceLastScan;
    }

    // CUSTOM PUBLIC METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Determines the next mower move
     *
     * Note:
     * Decided to keep the instances for the NextMowerMoveService local to take advantage of the lazy singletons
     *
     * @param isMoveEligible - Determines if the mower should consider the possibility of making a move
     *
     * @return - The next mower move the mower will attempt to make
     */
    public MowerMove determineMove(boolean isMoveEligible)
    {
        final SimulationRiskProfile riskProfile = simulation.getSimulationRiskProfile();

        MowerMove response;

        if(riskProfile == SimulationRiskProfile.LOW)
        {
            NextMowerMoveService lowRiskMoveService = NextLowRiskMoveServiceImpl.getInstance();

            response = lowRiskMoveService.getNextMowerMove(isMoveEligible, this);
        }
        else if(riskProfile == SimulationRiskProfile.MEDIUM)
        {
            NextMowerMoveService medRiskMoveService = NextMedRiskMoveServiceImpl.getInstance();

            response = medRiskMoveService.getNextMowerMove(isMoveEligible, this);
        }
        else if(riskProfile == SimulationRiskProfile.HIGH)
        {
            NextMowerMoveService highRiskMoveService = NextHighRiskMoveServiceImpl.getInstance();

            response = highRiskMoveService.getNextMowerMove(isMoveEligible, this);
        }
        else{
            // THIS SHOULD NEVER BE REACHED BECAUSE RISK PROFILE SHOULD ALWAYS BE SET
            throw new RuntimeException("[RISK PROFILE ERROR] :: determineMove - The risk profile is invalid");
        }

        return response;
    }

    /**
     * Makes the mower move
     *
     * @param mowerMove - The mower move to make
     */
    public void makeMove(final MowerMove mowerMove)
    {
        if(simulation.isValidMove(mowerMove))
        {
            if(mowerMove.getMowerMovementType() == MowerMovementType.MOVE)
            {
                move();

                updateSurroundingSquaresAfterMove();

                turnsSinceLastScan++;
            }
            else if(mowerMove.getMowerMovementType() == MowerMovementType.STEER)
            {
                steer(mowerMove.getDirection());

                turnsSinceLastScan++;
            }
            else if(mowerMove.getMowerMovementType() == MowerMovementType.SCAN)
            {
                scan();

                turnsSinceLastScan = 0;
            }
            else{
                pass();

                turnsSinceLastScan++;
            }
        }
        else{
            disableMower();
        }
    }

    /**
     * Disables a mower when they make an invalid movement
     */
    public void disableMower()
    {
        if(!isDisabled)
        {
            isDisabled = true;
        }

        xCoordinate = Integer.MIN_VALUE;
        yCoordinate = Integer.MIN_VALUE;
    }

    // CUSTOM PRIVATE METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Moves the mower forward 1 space in the current direction
     */
    private void move()
    {
        xCoordinate += direction.getxIncrement();
        yCoordinate += direction.getyIncrement();
    }

    /**
     * Changes the mowers direction
     *
     * @param direction - The new direction to set the mower to
     *
     * @throws RuntimeException - When the direction is null
     */
    private void steer(final Direction direction)
    {
        if(direction != null)
        {
            this.direction = direction;
        }
        else{
            final String errorMsg = "[ERROR] - Cannot change the mower direction to null";

            System.out.println(errorMsg);

            throw new RuntimeException(errorMsg);
        }
    }

    /**
     * Scans the mowers surrounding squares in a clockwise fashion and returns a list of the lawn square content
     * that is surrounding the mower with the first entry being the norther most square
     *
     * @return - A collection of lawn square content with the first entry being the northern most square
     */
    private List<LawnSquareContent> scan()
    {
        surroundingSquares.set(0, simulation.getLawnSquareContent(xCoordinate, yCoordinate + 1));
        surroundingSquares.set(1, simulation.getLawnSquareContent(xCoordinate + 1, yCoordinate + 1));
        surroundingSquares.set(2, simulation.getLawnSquareContent(xCoordinate + 1, yCoordinate));
        surroundingSquares.set(3, simulation.getLawnSquareContent(xCoordinate + 1, yCoordinate - 1));
        surroundingSquares.set(4, simulation.getLawnSquareContent(xCoordinate, yCoordinate - 1));
        surroundingSquares.set(5, simulation.getLawnSquareContent(xCoordinate - 1, yCoordinate - 1));
        surroundingSquares.set(6, simulation.getLawnSquareContent(xCoordinate - 1, yCoordinate));
        surroundingSquares.set(7, simulation.getLawnSquareContent(xCoordinate - 1, yCoordinate + 1));

        System.out.println(surroundingSquares);

        return surroundingSquares;
    }

    /**
     * Does nothing and passes the mowers turn
     */
    private void pass()
    {
        // DOING NOTHING TO PASS THE MOWERS TURN
    }

    /**
     * Updates the surrounding squares model after a move
     */
    private void updateSurroundingSquaresAfterMove()
    {
        List<LawnSquareContent> newModel = new ArrayList<>(Collections.nCopies(8, null));

        if(direction == Direction.NORTH) // MOVING TO POSITION 0 IN THE EXISTING MODEL
        {
            // CONVERT THE EXISTING SQUARES TO THEIR NEW POSITION IN THE MODEL
            newModel.set(2, surroundingSquares.get(1));
            newModel.set(3, surroundingSquares.get(2));
            newModel.set(5, surroundingSquares.get(6));
            newModel.set(6, surroundingSquares.get(7));

            // SET THE VALUE OF THE SQUARE THE MOWER MOVED FROM TO EMPTY
            newModel.set(4, LawnSquareContent.EMPTY);

            // SET THE VALUES OF THE UNKNOWN SQUARES
            newModel.set(0, LawnSquareContent.UNKNOWN);
            newModel.set(1, LawnSquareContent.UNKNOWN);
            newModel.set(7, LawnSquareContent.UNKNOWN);
        }
        else if(direction == Direction.NORTHEAST) // MOVING TO POSITION 1 IN THE EXISTING MODEL
        {
            // CONVERT THE EXISTING SQUARES TO THEIR NEW POSITION IN THE MODEL
            newModel.set(4, surroundingSquares.get(2));
            newModel.set(6, surroundingSquares.get(0));

            // SET THE VALUE OF THE SQUARE THE MOWER MOVED FROM TO EMPTY
            newModel.set(5, LawnSquareContent.EMPTY);

            // SET THE VALUES OF THE UNKNOWN SQUARES
            newModel.set(0, LawnSquareContent.UNKNOWN);
            newModel.set(1, LawnSquareContent.UNKNOWN);
            newModel.set(2, LawnSquareContent.UNKNOWN);
            newModel.set(3, LawnSquareContent.UNKNOWN);
            newModel.set(7, LawnSquareContent.UNKNOWN);
        }
        else if(direction == Direction.EAST) // MOVING TO POSITION 2 IN THE EXISTING MODEL
        {
            // CONVERT THE EXISTING SQUARES TO THEIR NEW POSITION IN THE MODEL
            newModel.set(0, surroundingSquares.get(1));
            newModel.set(4, surroundingSquares.get(3));
            newModel.set(5, surroundingSquares.get(4));
            newModel.set(7, surroundingSquares.get(0));

            // SET THE VALUE OF THE SQUARE THE MOWER MOVED FROM TO EMPTY
            newModel.set(6, LawnSquareContent.EMPTY);

            // SET THE VALUES OF THE UNKNOWN SQUARES
            newModel.set(1, LawnSquareContent.UNKNOWN);
            newModel.set(2, LawnSquareContent.UNKNOWN);
            newModel.set(3, LawnSquareContent.UNKNOWN);
        }
        else if(direction == Direction.SOUTHEAST) // MOVING TO POSITION 3 IN THE EXISTING MODEL
        {
            // CONVERT THE EXISTING SQUARES TO THEIR NEW POSITION IN THE MODEL
            newModel.set(0, surroundingSquares.get(2));
            newModel.set(6, surroundingSquares.get(4));

            // SET THE VALUE OF THE SQUARE THE MOWER MOVED FROM TO EMPTY
            newModel.set(7, LawnSquareContent.EMPTY);

            // SET THE VALUES OF THE UNKNOWN SQUARES
            newModel.set(1, LawnSquareContent.UNKNOWN);
            newModel.set(2, LawnSquareContent.UNKNOWN);
            newModel.set(3, LawnSquareContent.UNKNOWN);
            newModel.set(4, LawnSquareContent.UNKNOWN);
            newModel.set(5, LawnSquareContent.UNKNOWN);
        }
        else if(direction == Direction.SOUTH) // MOVING TO POSITION 4 IN THE EXISTING MODEL
        {
            // CONVERT THE EXISTING SQUARES TO THEIR NEW POSITION IN THE MODEL
            newModel.set(1, surroundingSquares.get(2));
            newModel.set(2, surroundingSquares.get(3));
            newModel.set(6, surroundingSquares.get(5));
            newModel.set(7, surroundingSquares.get(6));

            // SET THE VALUE OF THE SQUARE THE MOWER MOVED FROM TO EMPTY
            newModel.set(0, LawnSquareContent.EMPTY);

            // SET THE VALUES OF THE UNKNOWN SQUARES
            newModel.set(3, LawnSquareContent.UNKNOWN);
            newModel.set(4, LawnSquareContent.UNKNOWN);
            newModel.set(5, LawnSquareContent.UNKNOWN);
        }
        else if(direction == Direction.SOUTHWEST) // MOVING TO POSITION 5 IN THE EXISTING MODEL
        {
            // CONVERT THE EXISTING SQUARES TO THEIR NEW POSITION IN THE MODEL
            newModel.set(0, surroundingSquares.get(6));
            newModel.set(2, surroundingSquares.get(4));

            // SET THE VALUE OF THE SQUARE THE MOWER MOVED FROM TO EMPTY
            newModel.set(1, LawnSquareContent.EMPTY);

            // SET THE VALUES OF THE UNKNOWN SQUARES
            newModel.set(3, LawnSquareContent.UNKNOWN);
            newModel.set(4, LawnSquareContent.UNKNOWN);
            newModel.set(5, LawnSquareContent.UNKNOWN);
            newModel.set(6, LawnSquareContent.UNKNOWN);
            newModel.set(7, LawnSquareContent.UNKNOWN);
        }
        else if(direction == Direction.WEST) // MOVING TO POSITION 6 IN THE EXISTING MODEL
        {
            // CONVERT THE EXISTING SQUARES TO THEIR NEW POSITION IN THE MODEL
            newModel.set(0, surroundingSquares.get(7));
            newModel.set(1, surroundingSquares.get(0));
            newModel.set(3, surroundingSquares.get(4));
            newModel.set(4, surroundingSquares.get(5));

            // SET THE VALUE OF THE SQUARE THE MOWER MOVED FROM TO EMPTY
            newModel.set(2, LawnSquareContent.EMPTY);

            // SET THE VALUES OF THE UNKNOWN SQUARES
            newModel.set(5, LawnSquareContent.UNKNOWN);
            newModel.set(6, LawnSquareContent.UNKNOWN);
            newModel.set(7, LawnSquareContent.UNKNOWN);
        }
        else if(direction == Direction.NORTHWEST) // MOVING TO POSITION 7 IN THE EXISTING MODEL
        {
            // CONVERT THE EXISTING SQUARES TO THEIR NEW POSITION IN THE MODEL
            newModel.set(2, surroundingSquares.get(0));
            newModel.set(4, surroundingSquares.get(6));

            // SET THE VALUE OF THE SQUARE THE MOWER MOVED FROM TO EMPTY
            newModel.set(3, LawnSquareContent.EMPTY);

            // SET THE VALUES OF THE UNKNOWN SQUARES
            newModel.set(0, LawnSquareContent.UNKNOWN);
            newModel.set(1, LawnSquareContent.UNKNOWN);
            newModel.set(5, LawnSquareContent.UNKNOWN);
            newModel.set(6, LawnSquareContent.UNKNOWN);
            newModel.set(7, LawnSquareContent.UNKNOWN);
        }
        else{
            // SHOULD NEVER REACH THIS BECAUSE ALL DIRECTIONS ARE COVERED
            throw new RuntimeException("[UPDATE MODEL ERROR] :: updateSurroundingSquaresAfterMove - Invalid Direction");
        }

        surroundingSquares = newModel;
    }
}