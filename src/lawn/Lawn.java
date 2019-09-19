package lawn;

import java.util.ArrayList;
import java.util.List;

/**
 * Class used to represent the whole entire lawn
 *
 * Created by L. Arroyo on 9/11/2019
 */

public class Lawn
{
    // FIELDS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private final int xLength;
    private final int yLength;
    private final List<LawnSquare> lawnSquares;

    // CONSTRUCTORS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public Lawn(int xLength, int yLength)
    {
        this.xLength = xLength;
        this.yLength = yLength;
        this.lawnSquares = new ArrayList<>();
    }

    // ACCESS METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public List<LawnSquare> getLawnSquares()
    {
        return lawnSquares;
    }

    // CUSTOM PUBLIC METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Gets a specific lawn square by the x and y coordinate
     *
     * @param x - The x coordinate
     * @param y - The y coordinate
     *
     * @return - The lawn square if it is located, if the square does not exist then null is returned
     */
    public LawnSquare getLawnSquareByCoordinates(final int x, final int y)
    {
        LawnSquare result = null;

        for(LawnSquare lawnSquare : lawnSquares)
        {
            if(lawnSquare.getxCoordinate() == x && lawnSquare.getyCoordinate() == y)
            {
                result = lawnSquare;
            }
        }

        return result;
    }

    /**
     * Adds a lawn square to the lawn model
     *
     * @param square - The lawn square to add to the model
     *
     * @throws RuntimeException - When trying to add a null square to the model
     */
    public void addLawnSquare(final LawnSquare square)
    {
        if(square != null)
        {
            lawnSquares.add(square);
        }
        else{
            final String errorMsg = "[ERROR] - Cannot add a null square to the lawn model";

            System.out.println(errorMsg);

            throw new RuntimeException(errorMsg);
        }
    }
}