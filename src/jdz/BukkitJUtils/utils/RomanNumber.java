/**
 * RomanNumber.java
 *
 * Created by Jonodonozym on god knows when
 * Copyright © 2017. All rights reserved.
 * 
 * Last modified on Oct 5, 2017 9:22:58 PM
 */

package jdz.BukkitJUtils.utils;

import java.util.TreeMap;

/**
 * Turns an int into a roman numeral string using recursive magic
 *
 * @author Jonodonozym
 */
public final class RomanNumber {
    private final static TreeMap<Integer, String> map = new TreeMap<Integer, String>();

    static {
        map.put(1000, "M");
        map.put(900, "CM");
        map.put(500, "D");
        map.put(400, "CD");
        map.put(100, "C");
        map.put(90, "XC");
        map.put(50, "L");
        map.put(40, "XL");
        map.put(10, "X");
        map.put(9, "IX");
        map.put(5, "V");
        map.put(4, "IV");
        map.put(1, "I");
    }

    /**
     * 
     * @param number
     * @return
     */
    public static String of(int number) {
    	if (number < 1)
    		return "";
    	
        int l =  map.floorKey(number);
        if ( number == l ) {
            return map.get(number);
        }
        return map.get(l) + of(number-l);
    }
}