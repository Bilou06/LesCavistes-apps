package fr.lescavistes.lescavistes;

import android.graphics.drawable.Drawable;

/**
 * Created by Sylvain on 05/05/2015.
 */
public class ShopListItem {

    public final String title;        // the text for the ListView item title
    public final String description;  // the text for the ListView item description
    public final String address;  // the text for the ListView item description
    public final int id;

    public ShopListItem(int id, String title, String description, String address) {
        this.title = title;
        this.description = description;
        this.address = address;
        this.id = id;
    }

    public ShopListItem(Shop shop) {
        this.title = shop.getName();
        this.description = String.valueOf(shop.getDist()) + " km";
        this.id = shop.getId();
        this.address = shop.getAddress();
    }
}
