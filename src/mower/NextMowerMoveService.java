package mower;

import constant.LawnSquareContent;
import constant.MowerMovementType;

import java.util.*;

/**
 * I created this abstract class because I decided on the strategy to have risk profiles after I had made my initial
 * design. I could have left all of this logic for determining the next move based on the risk levels in the
 * mower class but it makes more sense to break it out into pattern. This allows me to add
 * more risk profiles and easily implement them while still keeping things nice and modular
 *
 * Created by L. Arroyo on 9/28/2019
 */
abstract class NextMowerMoveService
{
    // ABSTRACT METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Gets the next mower move
     *
     * @param isMoveEligible - If the mower is eligible to make a MOVE move type
     * @param mower - The mower to determine the next move for
     *
     * @return - A mower move
     */
    abstract MowerMove getNextMowerMove(final boolean isMoveEligible, final Mower mower);

    // PROTECTED METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
     * @param surroundingSquares - The surrounding squares model for the mower
     *
     * @return - 4 lists of moves: forbidden, high risk, medium risk, and preferred (in that order)
     */
    List<List<Integer>> getPossibleMovesByRanking(final int positionWithinSurroundingSquares,
                                                            final List<LawnSquareContent> surroundingSquares)
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

    /**
     * Gets the number of null squares in the surrounding squares list
     *
     * Note:
     * If the list is null or empty it will return INT_MAX
     *
     * @return - The number of null squares in the surrounding square list
     */
    int getSurroundingSquareNullCount(final List<LawnSquareContent> surroundingSquares)
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
     * @param mower - The mower for the move
     *
     * @return - The mower move
     */
    MowerMove getMowerMoveForMovingInCurrentDirection(final Mower mower)
    {
        int newXCoor = mower.getXCoordinate() + mower.getDirection().getxIncrement();
        int newYCoor = mower.getYCoordinate() + mower.getDirection().getyIncrement();

        mower.getSurroundingSquares().set(mower.getDirection().getIndex(), null);

        mower.setPositionWithinSurroundingSquares(mower.getDirection().getIndex());

        return new MowerMove(mower.getName(),
                MowerMovementType.MOVE,
                mower.getDirection(),
                mower.getXCoordinate(),
                mower.getYCoordinate(),
                newXCoor,
                newYCoor);
    }

    // PRIVATE METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Determines the indexes in the surrounding squares model that cannot be determined based on the mowers current
     * position within the model
     *
     * @param positionWithinSurroundingSquares - The mowers position within the surrounding squares model
     *
     * @return - A set of indexes that the mower does not know the contents of based on its position
     */
    private Set<Integer> determineUnknownIndexes(final int positionWithinSurroundingSquares)
    {
        if(positionWithinSurroundingSquares == Integer.MIN_VALUE)
        {
            return new HashSet<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7)); // MOWER HAS NO KNOWLEDGE OF SQUARES AROUND IT
        }
        else if(positionWithinSurroundingSquares == -1)
        {
            return new HashSet<>(); // THIS MEANS THE MOWER IS IN THE DEAD CENTER OF THE SURROUNDING MODEL
        }
        else if(positionWithinSurroundingSquares == 0)
        {
            return new HashSet<>(Arrays.asList(0, 1, 7)); // MOWER IN NORTH SQUARE OF MODEL
        }
        else if(positionWithinSurroundingSquares == 1)
        {
            return new HashSet<>(Arrays.asList(0, 1, 2, 3, 7)); // MOWER IN NORTHEAST SQUARE OF MODEL
        }
        else if(positionWithinSurroundingSquares == 2)
        {
            return new HashSet<>(Arrays.asList(1, 2, 3)); // MOWER IN EAST SQUARE OF MODEL
        }
        else if(positionWithinSurroundingSquares == 3)
        {
            return new HashSet<>(Arrays.asList(1, 2, 3, 4, 5)); // MOWER IN SOUTHEAST SQUARE OF MODEL
        }
        else if(positionWithinSurroundingSquares == 4)
        {
            return new HashSet<>(Arrays.asList(3, 4, 5)); // MOWER IN SOUTH SQUARE OF MODEL
        }
        else if(positionWithinSurroundingSquares == 5)
        {
            return new HashSet<>(Arrays.asList(3, 4, 5, 6, 7)); // MOWER IN SOUTHWEST SQUARE OF MODEL
        }
        else if(positionWithinSurroundingSquares == 6)
        {
            return new HashSet<>(Arrays.asList(5, 6, 7)); // MOWER IN WEST SQUARE OF MODEL
        }
        else if(positionWithinSurroundingSquares == 7)
        {
            return new HashSet<>(Arrays.asList(0, 1, 5, 6, 7)); // MOWER IN NORTHWEST SQUARE OF MODEL
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
}
