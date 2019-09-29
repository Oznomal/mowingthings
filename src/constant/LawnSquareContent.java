package constant;

/**
 * Enum that represents that type of content that can be in a lawn square
 *
 * Created by L. Arroyo on 9/11/2019
 */

public enum LawnSquareContent
{
    EMPTY,  // ONLY PRESENT IN MEDIUM AND PREFERRED LISTS
    GRASS,  // ONLY PRESENT IN MEDIUM AND PREFERRED LISTS
    CRATER, // ONLY PRESENT IN FORBIDDEN LISTS
    FENCE,  // ONLY PRESENT IN FORBIDDEN LISTS
    MOWER,  // ONLY PRESENT IN HIGH RISK LISTS
    UNKNOWN // ONLY PRESENT IN HIGH RISK LISTS
}