package mower;

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
    private NextMedRiskMoveServiceImpl()
    {
        super(MAX_UNKNOWN_SQUARE_COUNT, MAX_TURNS_SINCE_LAST_SCAN);
    }

    // PACKAGE METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    MowerMove getNextMowerMove(boolean isMoveEligible, Mower mower)
    {
        return null;
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