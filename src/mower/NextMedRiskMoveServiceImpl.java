package mower;

import constant.Direction;
import constant.LawnSquareContent;
import constant.MowerMovementType;

import java.util.List;

/**
 * Concrete singleton implementation for determining the next med risk mower move
 *
 * Created by L. Arroyo on 9/28/2019
 */
class NextMedRiskMoveServiceImpl extends NextMowerMoveService
{
    // FIELDS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private static final int MAX_UNKNOWN_SQUARE_COUNT = 5;
    private static final int MAX_TURNS_SINCE_LAST_SCAN = 3;

    private static NextMedRiskMoveServiceImpl nextMedRiskMoveService;

    // CONSTRUCTOR
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private NextMedRiskMoveServiceImpl(){}

    // PACKAGE METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Determines the next med risk mower move
     *
     * @return - The mower move the mower should attempt to make
     */
    // THIS METHOD PATTERN WILL BE THE SAME FOR THE LOW, MED, AND HIGH PATTERNS BUT DIFFERENT FOR NO RISK
    // IN ORDER TO AVOID DUPLICATION WE WOULD NEED TO IMPLEMENT THIS METHOD IN THE ABSTRACT CLASS AND THEN
    // ABSTRACT THE 2 METHODS TO DETERMINE THE ELIGIBLE/INELIGIBLE MOVES AND IMPLEMENT THEM IN EACH CONCRETE
    // CLASS. 1, THAT WILL NOT WORK 100% BECAUSE THE NO RISK CLASS DOES NOT NEED BOTH METHODS AND 2, THAT WOULD
    // BE A REALLY WEIRD PATTERN BECAUSE THIS IS THE METHOD THAT SHOULD BE CALLED BY THE MOWER AND IT FEELS
    // BACKWARDS TO ONLY CALL A SINGLE METHOD WHICH RESIDES IN THE ABSTRACT CLASS BUT SOLELY DEPENDS ON METHODS
    // IN THE CONCRETE CLASS, SO THAT IS WHY I AM ALLOWING DUPLICATES FOR THIS METHODS IMPLEMENTATION
    @SuppressWarnings("Duplicates")
    @Override
    MowerMove getNextMowerMove( Mower mower)
    {
        MowerMove response;

        if(!mower.isStrategic())
        {
            response = getRandomMowerMove(mower);
        }
        // IF THE SURROUNDING SQUARES ARE EMPTY, HAVE TOO MANY UNKNOWNS, OR MAX TURNS SINCE LAST SCAN WE WANT TO SCAN
        else if(mower.getSurroundingSquares().isEmpty()
                || getSurroundingSquareUnknownCount(mower.getSurroundingSquares()) >= MAX_UNKNOWN_SQUARE_COUNT
                || mower.getTurnsSinceLastScan() >= MAX_TURNS_SINCE_LAST_SCAN)
        {
            response = new MowerMove(mower.getName(),
                    MowerMovementType.SCAN, mower.getDirection(), mower.getXCoordinate(), mower.getYCoordinate());
        }
        else
        {
            response = determineMoveEligibleMove(mower);
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

        final List<Integer> prefGrassMoves = getSubListForContentType(preferredMoves, surroundingSquares, LawnSquareContent.GRASS);
        final List<Integer> prefEmptyMoves = getSubListForContentType(preferredMoves, surroundingSquares, LawnSquareContent.EMPTY);
        final List<Integer> medGrassMoves  = getSubListForContentType(medRiskMoves, surroundingSquares, LawnSquareContent.GRASS);
        final List<Integer> medEmptyMoves  = getSubListForContentType(medRiskMoves, surroundingSquares, LawnSquareContent.EMPTY);

        // MOVE OR STEER TOWARDS PREFERRED GRASS
        if(!prefGrassMoves.isEmpty())
        {
            response = getMoveOrSteerMoveForSublist(prefGrassMoves, mower);
        }
        // MOVE OR STEER TOWARDS MED GRASS
        else if(!medGrassMoves.isEmpty())
        {
            response = getMoveOrSteerMoveForSublist(medGrassMoves, mower);
        }
        // MOVE OR STEER TOWARDS PREF EMPTY
        else if(!prefEmptyMoves.isEmpty())
        {
            response = getMoveOrSteerMoveForSublist(prefEmptyMoves, mower);
        }
        // MOVE OR STEER TOWARDS MED EMPTY
        else if(!medEmptyMoves.isEmpty())
        {
            response = getMoveOrSteerMoveForSublist(medEmptyMoves, mower);
        }
        // IF ONLY HIGH RISK MOVES ARE AVAILABLE SCAN
        else{
            response = new MowerMove(name, MowerMovementType.SCAN, currDirection, currXCoor, currYCoor);
        }

        return response;
    }

    // ACCESS METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Lazy singleton implementation, prevents the class from having to be instantiated unless it is needed
     *
     * @return - The instance of this class
     */
    static NextMedRiskMoveServiceImpl getInstance()
    {
        if(nextMedRiskMoveService == null)
        {
            nextMedRiskMoveService = new NextMedRiskMoveServiceImpl();
        }

        return nextMedRiskMoveService;
    }
}
