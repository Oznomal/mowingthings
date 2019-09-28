package mower;

/**
 * Concrete singleton implementation for determining the next high risk mower move
 *
 * Created by L. Arroyo on 9/28/2019
 */
class NextHighRiskMoveServiceImpl extends NextMowerMoveService
{
    // FIELDS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private static NextHighRiskMoveServiceImpl nextHighRiskMoveService;

    // CONSTRUCTOR
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private NextHighRiskMoveServiceImpl(){}

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
    static NextHighRiskMoveServiceImpl getInstance()
    {
        if(nextHighRiskMoveService == null)
        {
            nextHighRiskMoveService = new NextHighRiskMoveServiceImpl();
        }

        return nextHighRiskMoveService;
    }
}
