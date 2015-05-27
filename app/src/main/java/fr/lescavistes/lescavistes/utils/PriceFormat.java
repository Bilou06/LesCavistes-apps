package fr.lescavistes.lescavistes.utils;

import java.text.NumberFormat;

/**
 * Created by Sylvain on 27/05/2015.
 */
public class PriceFormat {

    static NumberFormat formatter = NumberFormat.getCurrencyInstance();

    public static String format(Double p){
        String moneyString = formatter.format(p);
        if (moneyString.endsWith(".00")) {//todo replace that with something less locale specific
            int centsIndex = moneyString.lastIndexOf(".00");
            if (centsIndex != -1) {
                moneyString = moneyString.substring(1, centsIndex);
            }
        }
        return moneyString;
    }

}
