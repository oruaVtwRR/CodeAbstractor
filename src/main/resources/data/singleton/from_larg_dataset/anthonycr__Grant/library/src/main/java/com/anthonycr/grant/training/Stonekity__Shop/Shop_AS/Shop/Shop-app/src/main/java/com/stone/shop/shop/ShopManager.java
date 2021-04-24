package com.stone.shop.shop;

import com.stone.shop.shop.model.Shop;

public class ShopManager {

    private static final String TAG = "ShopManager";

    private static Object lock = new Object();
    private static ShopManager shopManager = new ShopManager();

    private Shop shop;

    private ShopManager() {
    }

    public static ShopManager getInstance() {
        return shopManager;
    }

    public void setSelectedShop(Shop shop) {
        if(null != shop) {
            this.shop = shop;
        }
    }

    public Shop getSelectedShop() {
        return this.shop;
    }


}
