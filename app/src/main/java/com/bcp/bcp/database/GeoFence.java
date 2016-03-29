package com.bcp.bcp.database;

/**
 * Created by anjup on 3/22/16.
 */
public class GeoFence {

    int id;
    String lat,lng,radius,fenceName;

    public GeoFence(){}

    public GeoFence(int id, String lat, String lng, String radius, String fenceName){

        this.id = id;
        this.lat = lat;
        this.lng = lng;
        this.radius = radius;
        this.fenceName = fenceName;

    }
    public GeoFence(String lat, String lng, String radius, String fenceName){

        this.lat = lat;
        this.lng = lng;
        this.radius = radius;
        this.fenceName = fenceName;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getRadius() {
        return radius;
    }

    public void setRadius(String radius) {
        this.radius = radius;
    }

    public String getFenceName() {
        return fenceName;
    }

    public void setFenceName(String fenceName) {
        this.fenceName = fenceName;
    }
}
