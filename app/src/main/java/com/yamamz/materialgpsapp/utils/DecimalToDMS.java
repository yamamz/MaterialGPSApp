package com.yamamz.materialgpsapp.utils;

import java.text.DecimalFormat;

/**
 * Created by yamamz on 10/3/2016.
 */

public class DecimalToDMS {

    public   String decimalToDMS(double coord) {
        String output, degrees, minutes, seconds;

        // gets the modulus the coordinate divided by one (MOD1).
        // in other words gets all the numbers after the decimal point.
        // e.g. mod := -79.982195 % 1 == 0.982195
        //
        // next get the integer part of the coord. On other words the whole number part.
        // e.g. intPart := -79

        double mod = coord % 1;
        int intPart = (int) coord;

        //set degrees to the value of intPart
        //e.g. degrees := "-79"

        degrees = String.valueOf(intPart);

        // next times the MOD1 of degrees by 60 so we can find the integer part for minutes.
        // get the MOD1 of the new coord to find the numbers after the decimal point.
        // e.g. coord :=  0.982195 * 60 == 58.9317
        //	mod   := 58.9317 % 1    == 0.9317
        //
        // next get the value of the integer part of the coord.
        // e.g. intPart := 58

        coord = mod * 60;
        mod = coord % 1;
        intPart = (int) coord;
        if (intPart < 0) {
            // Convert number to positive if it's negative.
            intPart *= -1;
        }

        // set minutes to the value of intPart.
        // e.g. minutes = "58"
        minutes = String.valueOf(intPart);

        //do the same again for minutes
        //e.g. coord := 0.9317 * 60 == 55.902
        //e.g. intPart := 55
        coord = mod * 60;
        float intPart1 = (float) coord;
        if (intPart1 < 0) {
            // Convert number to positive if it's negative.
            intPart1 *= -1;
        }

        // set seconds to the value of intPart.
        // e.g. seconds = "55"
        DecimalFormat df = new DecimalFormat("###.###");
        seconds = String.valueOf(df.format(intPart1));

        // I used this format for android but you can change it
        // to return in whatever format you like
        // e.g. output = "-79/1,58/1,56/1"
        //output = degrees + "/1," + minutes + "/1," + seconds + "/1";

        //Standard output of D°M′S″
        output = degrees + "°" + minutes + "'" + seconds + "\"";

        return output;
    }


}
