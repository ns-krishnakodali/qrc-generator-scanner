package com.techathon.qrc_gen;

public class userDetails {
    String  ItemNumber, ItemDescription, WareHouseId, AisleNumber, ProductRack, ActBulk, ImageURL;

    public  userDetails(){

    }
    public userDetails(String itemNumber, String itemDescription, String wareHouseId, String aisleNumber, String productRack, String actBulk, String imageURL) {
        ItemNumber = itemNumber;
        ItemDescription = itemDescription;
        AisleNumber = aisleNumber;
        WareHouseId = wareHouseId;
        ProductRack = productRack;
        ActBulk = actBulk;
        ImageURL = imageURL;
    }

    public String getItemNumber() {
        return ItemNumber;
    }

    public void setItemNumber(String itemNumber) {
        ItemNumber = itemNumber;
    }

    public String getItemDescription() {
        return ItemDescription;
    }

    public void setItemDescription(String itemDescription) {
        ItemDescription = itemDescription;
    }

    public String getWareHouseId() {
        return WareHouseId;
    }

    public void setWareHouseId(String wareHouseId) {
        WareHouseId = wareHouseId;
    }

    public String getAisleNumber() {
        return AisleNumber;
    }

    public void setAisleNumber(String aisleNumber) {
        AisleNumber = aisleNumber;
    }

    public String getProductRack() {
        return ProductRack;
    }

    public void setProductRack(String productRack) {
        ProductRack = productRack;
    }

    public String getActBulk() {
        return ActBulk;
    }

    public void setActBulk(String actBulk) {
        ActBulk = actBulk;
    }

    public String getImageURL() {
        return ImageURL;
    }

    public void setImageURL(String imageURL) {
        ImageURL = imageURL;
    }
}
