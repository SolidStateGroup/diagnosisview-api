package com.solidstategroup.diagnosisview.utils;

/**
 * Created by Pavlo Maksymchuk.
 */
public final class CommonUtils {

    private CommonUtils() {
    }

    /**
     * Helper method to check if give integer
     * value is within min/max range inclusive
     *
     * @param value a value to check
     * @param min   minimum range number
     * @param max   maximum range number
     * @return true if given number value within range
     */
    public static boolean inRange(int value, int min, int max) {
        if (value >= min && value <= max) {
            return true;
        }

        return false;
    }
}


