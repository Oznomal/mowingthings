package mower;

import constant.Direction;
import constant.LawnSquareContent;
import constant.MowerMovementType;
import constant.SimulationRiskProfile;
import sim.Simulation;

import java.util.ArrayList;
import java.util.List;

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
    private final List<LawnSquareContent> surroundingSquares;

    private Direction direction;
    private int xCoordinate;
    private int yCoordinate;
    private boolean isDisabled;

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
        this.surroundingSquares = new ArrayList<>();
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

    // CUSTOM PUBLIC METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Determines the next mower move
     *
     * @param moveEligible - Determines if the mower should consider the possibility of making a move
     *
     * @return - The next mower move the mower will attempt to make
     */
    public MowerMove determineMove(boolean moveEligible)
    {
        final SimulationRiskProfile riskProfile = simulation.getSimulationRiskProfile();

        MowerMove response = null;

        if(riskProfile == SimulationRiskProfile.LOW
                && surroundingSquares.isEmpty()
                || surroundingSquares.contains(null))
        {
            response = new MowerMove(name, MowerMovementType.SCAN, direction, xCoordinate, yCoordinate);
        }
        else{
            if(moveEligible)
            {
                LawnSquareContent facingContent = surroundingSquares.get(direction.getIndex());

                if(facingContent == LawnSquareContent.GRASS)
                {
                    int newXCoordinate = xCoordinate + direction.getxIncrement();
                    int newYCoordinate = yCoordinate + direction.getyIncrement();

                    surroundingSquares.set(direction.getIndex(), null);

                    response = new MowerMove(name,
                            MowerMovementType.MOVE,
                            direction,
                            xCoordinate,
                            yCoordinate,
                            newXCoordinate,
                            newYCoordinate);
                }
                else{
                    boolean surroundedGrassSquare = false;

                    for(int i = 0; i < 8; i++)
                    {
                        LawnSquareContent content = surroundingSquares.get(i);

                        if(content == LawnSquareContent.GRASS)
                        {
                            surroundedGrassSquare = true;

                            Direction newDirection =Direction.getDirectionByIndex(i);

                            response = new MowerMove(name, MowerMovementType.STEER, newDirection, xCoordinate, yCoordinate);

                            for(int j = 0; j < 8; j++)
                            {
                                surroundingSquares.set(j, null); // SET SQUARES TO NULL TO FORCE RE-SCAN
                            }

                            break;
                        }
                    }

                    if(!surroundedGrassSquare)
                    {
                        for(int i = 0; i < 8; i++) {
                            LawnSquareContent content = surroundingSquares.get(i);

                            if (content == LawnSquareContent.EMPTY)
                            {
                                Direction newDirection = Direction.getDirectionByIndex(i);

                                response = new MowerMove(name, MowerMovementType.STEER, newDirection, xCoordinate, yCoordinate);

                                for(int j = 0; j < 8; j++)
                                {
                                    surroundingSquares.set(j, null); // SET SQUARES TO NULL TO FORCE RE-SCAN
                                }

                                break;
                            }
                        }
                    }
                }
            }
            else{
                response = new MowerMove(name, MowerMovementType.SCAN, direction, xCoordinate, yCoordinate);
            }
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
            }
            else if(mowerMove.getMowerMovementType() == MowerMovementType.STEER)
            {
                steer(mowerMove.getDirection());
            }
            else if(mowerMove.getMowerMovementType() == MowerMovementType.SCAN)
            {
                scan();
            }
            else{
                pass();
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
        if(surroundingSquares.isEmpty())
        {
            for(int i = 0; i < 8; i++)
            {
                surroundingSquares.add(null);
            }
        }

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
}