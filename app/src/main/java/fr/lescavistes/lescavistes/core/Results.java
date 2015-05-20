package fr.lescavistes.lescavistes.core;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Sylvain on 20/05/2015.
 */
public class Results implements Serializable{

    public int selected;
    public int size;
    public ArrayList<Shop> shops;

    public Results(ArrayList shops, int size, int selected){
        this.shops=shops;
        this.size=size;
        this.selected=selected;
    }
}
