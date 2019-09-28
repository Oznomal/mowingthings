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
    private final List<LawnSquareContent> surroundingSquares;

    private Direction direction;
    private int xCoordinate;
    private int yCoordinate;
    private boolean isDisabled;
    private int positionWithinSurroundingSquares; // INDEX MOWER IS IN WITHIN SS LIST, -1 = CENTER, MIN_VAL = UNKNOWN

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
        this.positionWithinSurroundingSquares = Integer.MIN_VALUE;
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

        if(riskProfile == SimulationRiskProfile.LOW)
        {
            response = getNextLowRiskMove(moveEligible);
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

    /**
     * Forces a re-scan by setting the surrounding squares to null
     */
    private void forceReScan()
    {
        for(int j = 0; j < 8; j++)
        {
            surroundingSquares.set(j, null); // SET SQUARES TO NULL TO FORCE RE-SCAN
        }
    }

    /**
     * Determines the next low risk move for the mower
     *
     * @param isMoveEligible - Whether or not the mower is permitted to make a move
     *
     * @return - A Mower Move
     */
    @SuppressWarnings("Duplicates") // CERTAIN THINGS IN HERE WILL SHOW AS A DUPLICATE BUT DON'T REQUIRE OWN METHOD
    private MowerMove getNextLowRiskMove(final boolean isMoveEligible)
    {
        MowerMove response;

        // IF THE SURROUNDING SQUARES ARE EMPTY OR HAVE MORE NULLS THAN ALLOWED RE-SCAN
        if(surroundingSquares.isEmpty()
                || getSurroundingSquareNullCount() >= SimulationRiskProfile.LOW.getMaxNullCount())
        {
            response = new MowerMove(name, MowerMovementType.SCAN, direction, xCoordinate, yCoordinate);

            positionWithinSurroundingSquares = -1;
        }
        // IF THE MOWER IS MOVE ELIGIBLE DETERMINE NEXT ACTION
        else if(isMoveEligible)
        {
            final List<List<Integer>> possibleMovesList = getPossibleMovesByRanking(positionWithinSurroundingSquares);
            final List<Integer> medRiskMoves   = possibleMovesList.get(2);
            final List<Integer> preferredMoves = possibleMovesList.get(3);

            LawnSquareContent facingContent = surroundingSquares.get(direction.getIndex());

            // IF THE MOWER IS ALREADY POINTING TOWARDS A GRASS SQUARE AND IT IS A PREFERRED MOVE, TAKE IT!
            if(facingContent == LawnSquareContent.GRASS && preferredMoves.contains(direction.getIndex()))
            {
                response = getMowerMoveForMovingInCurrentDirection();
            }
            // IF THE MOWER IS NOT POINTING TOWARDS A GRASS PREFERRED MOVE THEN SEE WHICH PREFERRED MOVE TO TAKE
            else if(!preferredMoves.isEmpty())
            {
                // CHECK ALL OF THE PREFERRED MOVES TO SEE WHICH ONES ARE GRASS
                List<Integer> preferredGrassSquares = new ArrayList<>();

                for(Integer idx : preferredMoves)
                {
                    if(surroundingSquares.get(idx) == LawnSquareContent.GRASS)
                    {
                        preferredGrassSquares.add(idx);
                    }
                }

                Random random = new Random();

                Direction newDirection;

                // 1. IF THERE ARE GRASS MOVES AVAILABLE STEER TO ONE OF THEM
                if(!preferredGrassSquares.isEmpty())
                {
                    int idx = random.nextInt(preferredGrassSquares.size());

                    newDirection = Direction.getDirectionByIndex(preferredGrassSquares.get(idx));

                    response = new MowerMove(name, MowerMovementType.STEER, newDirection, xCoordinate, yCoordinate);
                }
                // 2. IF GRASS MOVES ARE NOT AVAILABLE BUT FACING A PREFERRED MOVE, TAKE IT
                else if(preferredMoves.contains(direction.getIndex()))
                {
                    response = getMowerMoveForMovingInCurrentDirection();
                }
                // 3. IF NO GRASS PREF MOVES AVAILABLE AND NOT FACING A PREFERRED MOVE,
                //    THEN SELECT A RANDOM PREF MOVE TO STEER TOWARDS
                else{
                    int idx = random.nextInt(preferredMoves.size());

                    newDirection = Direction.getDirectionByIndex(preferredMoves.get(idx));

                    response = new MowerMove(name, MowerMovementType.STEER, newDirection, xCoordinate, yCoordinate);
                }
            }
            // IF MED MOVES ARE NOT EMPTY WE ARE GOING TO MAKE A 50/50 CHOICE TO EITHER SCAN OR TAKE MED RISK MOVE
            else if(!medRiskMoves.isEmpty())
            {
                Random random = new Random();

                // 50/50 OPTION 1: SCAN
                if(random.nextBoolean())
                {
                    response = new MowerMove(name, MowerMovementType.SCAN, direction, xCoordinate, yCoordinate);
                }
                // 50/50 OPTION 3: SELECT A MEDIUM RISK MOVE
                else{
                    // IF ALREADY FACING A MEDIUM RISK MOVE, THEN TAKE IT
                    if(medRiskMoves.contains(direction.getIndex()))
                    {
                        response = getMowerMoveForMovingInCurrentDirection();
                    }
                    // CHOOSE A RANDOM MEDIUM RISK MOVE TO STEER TO
                    else{
                        int idx = random.nextInt(medRiskMoves.size());

                        Direction newDirection = Direction.getDirectionByIndex(medRiskMoves.get(idx));

                        response = new MowerMove(name, MowerMovementType.STEER, newDirection, xCoordinate, yCoordinate);
                    }
                }
            }
            // IF ONLY HIGH RISK MOVES ARE AVAILABLE THEN SCAN
            else{
                response = new MowerMove(name, MowerMovementType.SCAN, direction, xCoordinate, yCoordinate);
            }
        }
        // IF THE MOWER IS NOT MOVE ELIGIBLE THEN WE ARE ALWAYS GOING TO SCAN
        else{
            response = new MowerMove(name, MowerMovementType.SCAN, direction, xCoordinate, yCoordinate);
        }

        return response;
    }

    /**
     * Ranks the possible moves based off of moves which are forbidden, high risk, medium risk, and preferred
     *
     * Note: This does not take into account whether or not a square is Grass or Empty when placing them in the
     *       preferred moves set, it just organizes them by risk
     *
     * Returns list of sets in the following order:
     *
     * 0 - Forbidden Moves: Moves which are guaranteed to cause a collision (Fence or Crater)
     *
     * 1 - High Risk Moves: Moves which represent another mower, not recommended but may work because mowers can move
     *                      and the surrounding square model can become outdated in the event that other mowers which
     *                      were picked up in a previous scan occur before this mower in the rotation of mowers in
     *                      the sim and have moved prior to this mowers current turn
     *
     * 2 - Med Risk Moves:  Moves which represent the squares which a high risk move could have moved to in a previous
     *                      move, ex: if a mower is positioned at index 1 in the model (NORTHEAST), then
     *                      the squares it could have moved to in its next move would be index 0 (NORTH) OR 2 (EAST)
     *                      these are considered medium risk moves because the mower could have
     *                      moved there and the model may just be outdated
     *
     * 3 - Pref Moves:     These are the safest moves a mower can make, this is the subset of remaining moves that are
     *                     not considered forbidden, high risk, or medium risk.
     *
     * @param positionWithinSurroundingSquares - The index position within the surrounding squares the mower is at
     *
     * @return - 4 lists of moves: forbidden, high risk, medium risk, and preferred (in that order)
     */
    private List<List<Integer>> getPossibleMovesByRanking(final int positionWithinSurroundingSquares)
    {
        List<Integer> forbiddenMoves = new ArrayList<>();
        List<Integer> highRiskMoves  = new ArrayList<>();
        List<Integer> medRiskMoves   = new ArrayList<>();
        List<Integer> preferredMoves = new ArrayList<>();

        Set<Integer> unknownMoves = determineUnknownIndexes(positionWithinSurroundingSquares);

        // 1. LOOP THROUGH THE LIST AND GET THE SQUARES THE SQUARE INDEXES WHICH ARE FORBIDDEN / HIGH RISK
        for(int i = 0; i < 8; i++)
        {
            LawnSquareContent content = surroundingSquares.get(i);

            if(content == LawnSquareContent.MOWER)
            {
                highRiskMoves.add(i);
            }
            else if(content == LawnSquareContent.FENCE || content == LawnSquareContent.CRATER)
            {
                forbiddenMoves.add(i);
            }
        }

        // 2. LOOP THROUGH THE LIST CHECKING FOR MOWERS AND ADDING MED RISK SQUARES IF THEY ARE FOUND
        if(!highRiskMoves.isEmpty())
        {
            for(Integer idx : highRiskMoves)
            {
                for(Integer riskyIndex : determineMedRiskMovesForHighRiskSquare(idx))
                {
                    if(!forbiddenMoves.contains(riskyIndex)
                            && !highRiskMoves.contains(riskyIndex)
                            && !medRiskMoves.contains(riskyIndex))
                    {
                        medRiskMoves.add(riskyIndex);
                    }
                }
            }
        }

        // 3. USE THE REMAINING INDEXES TO COMPLETE THE PREFERRED MOVES SET
        for(int i = 0; i < 8; i++)
        {
            if(!forbiddenMoves.contains(i) && !highRiskMoves.contains(i) && !medRiskMoves.contains(i))
            {
                preferredMoves.add(i);
            }
        }

        return new ArrayList<>(Arrays.asList(forbiddenMoves, highRiskMoves, medRiskMoves, preferredMoves));
    }

    private Set<Integer> determineUnknownIndexes(final int positionWithinSurroundingSquares)
    {
        if(positionWithinSurroundingSquares == Integer.MIN_VALUE)
        {
            return new HashSet<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7)); // UNKNOWN POSITION
        }
        else if(positionWithinSurroundingSquares == -1)
        {
            return new HashSet<>();
        }
        else if(positionWithinSurroundingSquares == 0)
        {
            return new HashSet<>(Arrays.asList(0, 1, 7));
        }
        else if(positionWithinSurroundingSquares == 1)
        {
            return new HashSet<>(Arrays.asList(0, 1, 2, 3, 7));
        }
        else if(positionWithinSurroundingSquares == 2)
        {
            return new HashSet<>(Arrays.asList(1, 2, 3));
        }
        else if(positionWithinSurroundingSquares == 3)
        {
            return new HashSet<>(Arrays.asList(1, 2, 3, 4, 5));
        }
        else if(positionWithinSurroundingSquares == 4)
        {
            return new HashSet<>(Arrays.asList(3, 4, 5));
        }
        else if(positionWithinSurroundingSquares == 5)
        {
            return new HashSet<>(Arrays.asList(3, 4, 5, 6, 7));
        }
        else if(positionWithinSurroundingSquares == 6)
        {
            return new HashSet<>(Arrays.asList(5, 6, 7));
        }
        else if(positionWithinSurroundingSquares == 7)
        {
            return new HashSet<>(Arrays.asList(0, 1, 5, 6, 7));
        }
        else{
            // THIS SHOULD NOT BE REACHED EVER BECAUSE THE INDEX SHOULD ALWAYS BE BETWEEN 0-7
            throw new RuntimeException("[UNEXPECTED INDEX ERROR] :: determineUnknownIndexes, positionWithinSquares="
                    + positionWithinSurroundingSquares);
        }
    }

    /**
     * Determines med risk moves based on a surrounding square that contains a mower or movable obstacle
     *
     * @param surroundingSquareIndex - The index of the surrounding square that contains a mower
     *
     * @return - A set of indexes for surrounding squares that are considered a med risk move based on the mower
     */
    private Set<Integer> determineMedRiskMovesForHighRiskSquare(final int surroundingSquareIndex)
    {
        if(surroundingSquareIndex == 0)
        {
            return new HashSet<>(Arrays.asList(1, 2, 6, 7));
        }
        else if(surroundingSquareIndex == 1)
        {
            return new HashSet<>(Arrays.asList(0, 2));
        }
        else if(surroundingSquareIndex == 2)
        {
            return new HashSet<>(Arrays.asList(0, 1, 3, 4));
        }
        else if(surroundingSquareIndex == 3)
        {
            return new HashSet<>(Arrays.asList(2, 4));
        }
        else if(surroundingSquareIndex == 4)
        {
            return new HashSet<>(Arrays.asList(2, 3, 5, 6));
        }
        else if(surroundingSquareIndex == 5)
        {
            return new HashSet<>(Arrays.asList(4, 6));
        }
        else if(surroundingSquareIndex == 6)
        {
            return new HashSet<>(Arrays.asList(0, 4, 5, 7));
        }
        else if(surroundingSquareIndex == 7)
        {
            return new HashSet<>(Arrays.asList(0, 6));
        }
        else{
            // THIS SHOULD NOT BE REACHED EVER BECAUSE THE INDEX SHOULD ALWAYS BE BETWEEN 0-7
            throw new RuntimeException("[UNEXPECTED INDEX ERROR] :: determineMedRiskMovesForHighRiskSquare, idx="
                    + surroundingSquareIndex);
        }
    }

    /**
     * Gets the number of null squares in the surrounding squares list
     * @return
     */
    private int getSurroundingSquareNullCount()
    {
        if(surroundingSquares == null || surroundingSquares.isEmpty())
        {
            return Integer.MAX_VALUE;
        }

        int count = 0;

        for(LawnSquareContent content : surroundingSquares)
        {
            if(content == null)
            {
                count++;
            }
        }

        return count;
    }

    /**
     * Creates a Move mower move for the current direction
     *
     * @return - The mower move
     */
    private MowerMove getMowerMoveForMovingInCurrentDirection()
    {
        int newXCoor = xCoordinate + direction.getxIncrement();
        int newYCoor = yCoordinate + direction.getyIncrement();

        surroundingSquares.set(direction.getIndex(), null);

        positionWithinSurroundingSquares = direction.getIndex();

        return new MowerMove(name, MowerMovementType.MOVE, direction, xCoordinate, yCoordinate, newXCoor, newYCoor);
    }
}