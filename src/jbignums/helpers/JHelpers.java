/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jbignums.helpers;

import java.util.Arrays;

/**
 *
 * @author Kestutis
 */
public final class JHelpers {
    private JHelpers(){ }
    public static <T> boolean isVariableInArray(T value, T[] goodValues)
    {
        return Arrays.asList(goodValues).contains(value);
    }
}
