package edu.uga.dawgpool.models;

public class User {
    public String uid;
    public String email;
    public int ridePoints;

    public User() {
        // required for Firebase
    }
    // constructor
    public User(String uid, String email, int ridePoints) {
        this.uid = uid;
        this.email = email;
        this.ridePoints = ridePoints;
    }
}