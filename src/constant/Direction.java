package constant;

/**
 * Enum that represents the various directions a mower can face or travel
 *
 * Created by L. Arroyo on 9/11/19
 */

public enum Direction
{
    // VALUES
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    NORTH(0, 0, 1),
    NORTHEAST(1, 1, 1),
    EAST(2, 1, 0),
    SOUTHEAST(3, 1, -1),
    SOUTH(4, 0, -1),
    SOUTHWEST(5, -1, -1),
    WEST(6, -1, 0),
    NORTHWEST(7, -1, 1);

    // FIELDS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private final int index;
    private final int xIncrement;
    private final int yIncrement;

    // CONSTRUCTORS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    Direction(final int index, final int xIncrement, final int yIncrement)
    {
        this.index = index;
        this.xIncrement = xIncrement;
        this.yIncrement = yIncrement;
    }

    // ACCESS METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public int getIndex() {
        return index;
    }

    public int getxIncrement() {
        return xIncrement;
    }

    public int getyIncrement() {
        return yIncrement;
    }

    // STATIC METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Gets a direction based on the index value
     *
     * @param index - The index value
     *
     * @return - The direction
     */
    public static Direction getDirectionByIndex(final int index)
    {
        Direction result = null;

        for(Direction direction : values())
        {
            if(direction.index == index)
            {
                result = direction;
            }
        }

        return result;
    }
}