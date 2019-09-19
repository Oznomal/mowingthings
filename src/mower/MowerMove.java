package mower;

import constant.Direction;
import constant.MowerMovementType;

/**
 * Class represents a move a mower can attempt to make
 *
 * Created by L. Arroyo onm 9/11/2019
 */

public class MowerMove
{
    // FIELDS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private final String mowerName;
    private final MowerMovementType mowerMovementType;
    private final Direction direction;
    private final int currentXCoordinate;
    private final int currentYCoordinate;
    private int newXCoordinate;
    private int newYCoordinate;

    // CONSTRUCTORS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public MowerMove(final String mowerName,
                     final MowerMovementType mowerMovementType,
                     final Direction direction,
                     final int currentXCoordinate,
                     final int currentYCoordinate)
    {
        this.mowerName = mowerName;
        this.mowerMovementType = mowerMovementType;
        this.direction = direction;
        this.currentXCoordinate = currentXCoordinate;
        this.currentYCoordinate = currentYCoordinate;
    }

    public MowerMove(final String mowerName,
                     final MowerMovementType mowerMovementType,
                     final Direction direction,
                     final int currentXCoordinate,
                     final int currentYCoordinate,
                     final int newXCoordinate,
                     final int newYCoordinate)
    {
        this.mowerName = mowerName;
        this.mowerMovementType = mowerMovementType;
        this.direction = direction;
        this.currentXCoordinate = currentXCoordinate;
        this.currentYCoordinate = currentYCoordinate;
        this.newXCoordinate = newXCoordinate;
        this.newYCoordinate = newYCoordinate;
    }

    // ACCESS METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public String getMowerName() {
        return mowerName;
    }

    public MowerMovementType getMowerMovementType() {
        return mowerMovementType;
    }

    public Direction getDirection() {
        return direction;
    }

    public int getCurrentXCoordinate() {
        return currentXCoordinate;
    }

    public int getCurrentYCoordinate() {
        return currentYCoordinate;
    }

    public int getNewXCoordinate() {
        return newXCoordinate;
    }

    public int getNewYCoordinate() {
        return newYCoordinate;
    }
}