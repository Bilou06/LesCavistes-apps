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

    public Results(){
        this.items=new ArrayList<T>();
        this.size=0;
        this.selected=0;
    }

    public T getSelected(){
        if(items == null)
            return null;

        if(selected >= items.size())
            return null;

        return items.get(selected);
    }
}
