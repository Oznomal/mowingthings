package lawn;

import constant.LawnSquareContent;

/**
 * Class that represents an individual lawn square
 *
 * Created by L. Arroyo on 9/11/2019
 */

public class LawnSquare
{
    // FIELDS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private final int xCoordinate;
    private final int yCoordinate;
    private LawnSquareContent lawnSquareContent;

    // CONSTRUCTORS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public LawnSquare(int xCoordinate, int yCoordinate, LawnSquareContent lawnSquareContent)
    {
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
        this.lawnSquareContent = lawnSquareContent;
    }

    // ACCESS METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public int getxCoordinate() {
        return xCoordinate;
    }

    public int getyCoordinate() {
        return yCoordinate;
    }

    public LawnSquareContent getLawnSquareContent() {
        return lawnSquareContent;
    }

    public void setLawnSquareContent(LawnSquareContent lawnSquareContent) {
        this.lawnSquareContent = lawnSquareContent;
    }
}