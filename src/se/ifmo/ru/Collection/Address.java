package se.ifmo.ru.Collection;

import org.json.simple.JSONObject;

public class Address {
    private String zipCode = null;
    private Location town = null;

    public String getZipCode() {
        return zipCode;
    }

    public Location getTown() {
        return town;
    }

    Address(String zipCode, Location town){
        this.zipCode = zipCode;
        this.town = town;
    }
    Address(JSONObject jo){
        this.zipCode = (String) jo.get("zipCode");
        this.town = new Location((JSONObject)jo.get("town"));
    }
}
