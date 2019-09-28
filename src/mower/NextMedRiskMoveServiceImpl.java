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
    private static NextMedRiskMoveServiceImpl nextMedRiskMoveService;

    // CONSTRUCTOR
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private NextMedRiskMoveServiceImpl(){}

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
