package mower;

import constant.Direction;
import constant.LawnSquareContent;
import constant.MowerMovementType;

import java.util.List;
import java.util.Random;

/**
 * Concrete singleton implementation for determining the next low risk mower move
 *
 * Created by L. Arroyo on 9/28/2019
 */
class NextLowRiskMoveServiceImpl extends NextMowerMoveService
{
    // FIELDS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private static final int MAX_UNKNOWN_SQUARE_COUNT = 3;
    private static final int MAX_TURNS_SINCE_LAST_SCAN = 2;

    private static NextLowRiskMoveServiceImpl nextLowRiskMowerMoveService;

    // CONSTRUCTOR
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private NextLowRiskMoveServiceImpl(){}

    // PUBLIC METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Determines the next low risk mower move
     *
     * @param isMoveEligible - If the mower is eligible to make a MOVE move type
     *
     * @return - The mower move the mower should attempt to make
     */
    @Override
    public MowerMove getNextMowerMove(boolean isMoveEligible, final Mower mower)
    {
        MowerMove response;

        // IF THE SURROUNDING SQUARES ARE EMPTY, HAVE TOO MANY UNKNOWNS, OR MAX TURNS SINCE LAST SCAN WE WANT TO SCAN
        if(mower.getSurroundingSquares().isEmpty()
                || getSurroundingSquareUnknownCount(mower.getSurroundingSquares()) >= MAX_UNKNOWN_SQUARE_COUNT
                || mower.getTurnsSinceLastScan() >= MAX_TURNS_SINCE_LAST_SCAN)
        {
            response = new MowerMove(mower.getName(),
                    MowerMovementType.SCAN, mower.getDirection(), mower.getXCoordinate(), mower.getYCoordinate());
        }
        else if(isMoveEligible)
        {
            response = determineMoveEligibleMove(mower);
        }
        else{
            response = determineMoveIneligibleMove(mower);
        }

        return response;
    }
    // PRIVATE METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Determines the next mower move for move eligible moves
     *
     * @param mower - The mower that the move is for
     *
     * @return - The next mower move
     */
    @SuppressWarnings("Duplicates") // FOR THE VARIABLE INITIALIZATIONS AT THE TOP
    private MowerMove determineMoveEligibleMove(final Mower mower)
    {
        MowerMove response;

        // GET THE VALUES FROM THE OBJECT TO MAKE THE CODE CLEANER BELOW THIS
        final List<LawnSquareContent> surroundingSquares = mower.getSurroundingSquares();
        final String name = mower.getName();
        final Direction currDirection = mower.getDirection();
        final int currXCoor = mower.getXCoordinate();
        final int currYCoor = mower.getYCoordinate();

        final List<List<Integer>> possibleMovesList = getPossibleMovesByRanking(surroundingSquares);
        final List<Integer> medRiskMoves   = possibleMovesList.get(2);
        final List<Integer> preferredMoves = possibleMovesList.get(3);

        LawnSquareContent facingContent = surroundingSquares.get(currDirection.getIndex());

        // IF THE MOWER IS ALREADY POINTING TOWARDS A GRASS SQUARE AND IT IS A PREFERRED MOVE, TAKE IT!
        if(facingContent == LawnSquareContent.GRASS && preferredMoves.contains(currDirection.getIndex()))
        {
            response = getMowerMoveForMovingInCurrentDirection(mower);
        }
        // IF THE MOWER IS NOT POINTING TOWARDS A GRASS PREFERRED MOVE THEN SEE WHICH PREFERRED MOVE TO TAKE
        else if(!preferredMoves.isEmpty())
        {
            // CHECK ALL OF THE PREFERRED MOVES TO SEE WHICH ONES ARE GRASS
            List<Integer> preferredGrassSquares =
                    getSubListForContentType(preferredMoves, surroundingSquares, LawnSquareContent.GRASS);

            // 1. IF THERE ARE GRASS MOVES AVAILABLE STEER TO ONE OF THEM
            if(!preferredGrassSquares.isEmpty())
            {
                response = getRandomMowerSteerMove(preferredGrassSquares, mower);
            }
            // 2. IF GRASS MOVES ARE NOT AVAILABLE BUT FACING A PREFERRED MOVE, TAKE IT
            else if(preferredMoves.contains(currDirection.getIndex()))
            {
                response = getMowerMoveForMovingInCurrentDirection(mower);
            }
            // 3. IF NO GRASS PREF MOVES AVAILABLE AND NOT FACING A PREFERRED MOVE,
            //    THEN SELECT A RANDOM PREF MOVE TO STEER TOWARDS
            else{
                response = getRandomMowerSteerMove(preferredMoves, mower);
            }
        }
        // IF MED MOVES ARE NOT EMPTY WE ARE GOING TO MAKE A 50/50 CHOICE TO EITHER SCAN OR TAKE MED RISK MOVE
        else if(!medRiskMoves.isEmpty())
        {
            Random random = new Random();

            // 50/50 OPTION 1: SCAN
            if(random.nextBoolean())
            {
                response = new MowerMove(name, MowerMovementType.SCAN, currDirection, currXCoor, currYCoor);
            }
            // 50/50 OPTION 2: SELECT A MEDIUM RISK MOVE
            else{
                // IF ALREADY FACING A MEDIUM RISK MOVE, THEN TAKE IT
                if(medRiskMoves.contains(currDirection.getIndex()))
                {
                    response = getMowerMoveForMovingInCurrentDirection(mower);
                }
                // CHOOSE A RANDOM MEDIUM RISK MOVE TO STEER TO
                else{
                    response = getRandomMowerSteerMove(medRiskMoves, mower);
                }
            }
        }
        // IF ONLY HIGH RISK MOVES ARE AVAILABLE THEN SCAN
        else{
            response = new MowerMove(name, MowerMovementType.SCAN, currDirection, currXCoor, currYCoor);
        }

        return response;
    }

    /**
     * Determines the next mower move for move ineligible moves
     *
     * @param mower - The mower that the move is for
     *
     * @return - The next mower move
     */
    @SuppressWarnings("Duplicates") // FOR THE VARIABLE INITIALIZATIONS AT THE TOP
    private MowerMove determineMoveIneligibleMove(final Mower mower)
    {
        MowerMove response = null;

        // GET THE VALUES FROM THE OBJECT TO MAKE THE CODE CLEANER BELOW THIS
        final List<LawnSquareContent> surroundingSquares = mower.getSurroundingSquares();
        final String name = mower.getName();
        final Direction currDirection = mower.getDirection();
        final int currXCoor = mower.getXCoordinate();
        final int currYCoor = mower.getYCoordinate();

        final List<List<Integer>> possibleMovesList = getPossibleMovesByRanking(surroundingSquares);
        final List<Integer> highRiskMoves  = possibleMovesList.get(1);
        final List<Integer> medRiskMoves   = possibleMovesList.get(2);
        final List<Integer> preferredMoves = possibleMovesList.get(3);

        // THIS COULD BE DONE IN A WAY TO USE LESS MEMORY BUT IT WOULD CAUSE THE IMPLEMENTATION TO LOOK MESSY
        // IN MY OPINION AND SINCE THESE LISTS ARE GOING TO BE VERY SMALL I WILL TAKE READABILITY OVER MEMORY
        final List<Integer> prefGrassMoves = getSubListForContentType(preferredMoves, surroundingSquares, LawnSquareContent.GRASS);
        final List<Integer> prefEmptyMoves = getSubListForContentType(preferredMoves, surroundingSquares, LawnSquareContent.EMPTY);
        final List<Integer> medGrassMoves  = getSubListForContentType(medRiskMoves, surroundingSquares, LawnSquareContent.GRASS);
        final List<Integer> medEmptyMoves  = getSubListForContentType(medRiskMoves, surroundingSquares, LawnSquareContent.EMPTY);
        final List<Integer> highUnkMoves   = getSubListForContentType(highRiskMoves, surroundingSquares, LawnSquareContent.UNKNOWN);
        final List<Integer> highMowerMoves = getSubListForContentType(highRiskMoves, surroundingSquares, LawnSquareContent.MOWER);

        final LawnSquareContent facingContent = surroundingSquares.get(currDirection.getIndex());

        // 1. STEER OR SCAN IF PREF GRASS IS AVAILABLE
        if(!prefGrassMoves.isEmpty())
        {
            response = getScanOrSteerMoveForSublist(prefGrassMoves, mower);
        }
        // 2. STEER OR SCAN IF PREF EMPTY IS AVAILABLE
        else if(!prefEmptyMoves.isEmpty())
        {
            response = getScanOrSteerMoveForSublist(prefEmptyMoves, mower);
        }
        // 3. STEER OR SCAN IF MED GRASS IS AVAILABLE
        else if(!medGrassMoves.isEmpty())
        {
            response = getScanOrSteerMoveForSublist(medGrassMoves, mower);
        }
        // 4. STEER OR SCAN IF MED EMPTY IS AVAILABLE
        else if(!medEmptyMoves.isEmpty())
        {
            response = getScanOrSteerMoveForSublist(medEmptyMoves, mower);
        }
        // 5. IF FACING STATIC OBSTACLE ATTEMPT TO STEER TOWARDS UNKNOWN OR MOWER
        else if(facingContent == LawnSquareContent.CRATER || facingContent == LawnSquareContent.FENCE)
        {
            if(!highUnkMoves.isEmpty())
            {
                response = getRandomMowerSteerMove(highUnkMoves, mower);
            }
            else if(!highMowerMoves.isEmpty())
            {
                response = getRandomMowerSteerMove(highMowerMoves, mower);
            }
        }

        // IF AFTER ALL OF THE FILTERING THE RESPONSE IS STILL BY CHANCE NULL, SCAN
        if(response == null)
        {
            response = new MowerMove(name, MowerMovementType.SCAN, currDirection, currXCoor, currYCoor);
        }

        return response;
    }

    /**
     * Determines a scan or steer move based on a sublist of moves, if the mower already facing one of the
     * moves then the move will be a scan. However, if the mower is not facing one of the moves in the sublist
     * the move will be steering towards a random direction in the sublist
     *
     * @param sublist - The sublist of moves
     * @param mower - The mower
     *
     * @return - The scan or steer move
     */
    private MowerMove getScanOrSteerMoveForSublist(final List<Integer> sublist, final Mower mower)
    {
        MowerMove response;

        if(sublist.contains(mower.getDirection().getIndex()))
        {
            response = new MowerMove(mower.getName(),
                    MowerMovementType.SCAN, mower.getDirection(), mower.getXCoordinate(), mower.getYCoordinate());
        }
        else
        {
            response = getRandomMowerSteerMove(sublist, mower);
        }

        return response;
    }

    // ACCESS METHOD
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Lazy singleton implementation, prevents the class from having to be instantiated unless it is needed
     *
     * @return - The instance of this class
     */
    static NextLowRiskMoveServiceImpl getInstance()
    {
        if(nextLowRiskMowerMoveService == null)
        {
            nextLowRiskMowerMoveService = new NextLowRiskMoveServiceImpl();
        }

        return nextLowRiskMowerMoveService;
    }
}
