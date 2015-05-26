package fr.lescavistes.lescavistes.core;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Sylvain on 20/05/2015.
 */
public class Results<T> implements Serializable{

    public int selected;
    public int size;
    public ArrayList<T> items;

    public Results(ArrayList items, int size, int selected){
        this.items=items;
        this.size=size;
        this.selected=selected;
    }
}
