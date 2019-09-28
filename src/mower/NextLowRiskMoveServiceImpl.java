package mower;

import constant.Direction;
import constant.LawnSquareContent;
import constant.MowerMovementType;

import java.util.ArrayList;
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

    // PUBLIC CLASS
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

        // GET THE VALUES FROM THE OBJECT TO MAKE THE CODE CLEANER BELOW THIS
        final List<LawnSquareContent> surroundingSquares = mower.getSurroundingSquares();
        final String name = mower.getName();
        final Direction currDirection = mower.getDirection();
        final int currXCoor = mower.getXCoordinate();
        final int currYCoor = mower.getYCoordinate();

        // IF THE SURROUNDING SQUARES ARE EMPTY, HAVE TOO MANY UNKNOWNS, OR MAX TURNS SINCE LAST SCAN WE WANT TO SCAN
        if(surroundingSquares.isEmpty()
                || getSurroundingSquareUnknownCount(surroundingSquares) >= MAX_UNKNOWN_SQUARE_COUNT
                || mower.getTurnsSinceLastScan() >= MAX_TURNS_SINCE_LAST_SCAN)
        {
            response = new MowerMove(name, MowerMovementType.SCAN, currDirection, currXCoor, currYCoor);
        }
        // IF THE MOWER IS MOVE ELIGIBLE DETERMINE NEXT ACTION
        else if(isMoveEligible)
        {
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

                    response = new MowerMove(name, MowerMovementType.STEER, newDirection, currXCoor, currYCoor);
                }
                // 2. IF GRASS MOVES ARE NOT AVAILABLE BUT FACING A PREFERRED MOVE, TAKE IT
                else if(preferredMoves.contains(currDirection.getIndex()))
                {
                    response = getMowerMoveForMovingInCurrentDirection(mower);
                }
                // 3. IF NO GRASS PREF MOVES AVAILABLE AND NOT FACING A PREFERRED MOVE,
                //    THEN SELECT A RANDOM PREF MOVE TO STEER TOWARDS
                else{
                    int idx = random.nextInt(preferredMoves.size());

                    newDirection = Direction.getDirectionByIndex(preferredMoves.get(idx));

                    response = new MowerMove(name, MowerMovementType.STEER, newDirection, currXCoor, currYCoor);
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
                        int idx = random.nextInt(medRiskMoves.size());

                        Direction newDirection = Direction.getDirectionByIndex(medRiskMoves.get(idx));

                        response = new MowerMove(name, MowerMovementType.STEER, newDirection, currXCoor, currYCoor);
                    }
                }
            }
            // IF ONLY HIGH RISK MOVES ARE AVAILABLE THEN SCAN
            else{
                response = new MowerMove(name, MowerMovementType.SCAN, currDirection, currXCoor, currYCoor);
            }
        }
        // IF THE MOWER IS NOT MOVE ELIGIBLE THEN WE ARE ALWAYS GOING TO SCAN
        else{
            response = new MowerMove(name, MowerMovementType.SCAN, currDirection, currXCoor, currYCoor);
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
