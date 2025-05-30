package edu.uga.dawgpool.models;

import java.io.Serializable;

public class Ride implements Serializable {

    private String rid;
    public String type; // "offer" or "request"
    public String to; // location
    public String from; // location
    public long datetime; // using long because was getting errors with TimeStamp
    public String postedBy; // user who posted, probably their uid
    public String acceptedBy; // null until accepted by a rider (then its their uid)
    public String driverEmail;
    public String riderEmail;
    public String status; // "open", "accepted", or "completed"
    public int points; // 50 by default
    public boolean confirmedByDriver;
    public boolean confirmedByRider;

    public Ride() {
        // Required for Firebase
    }

    public Ride(String rid, String type, String to, String from, long datetime,
                String postedBy, String acceptedBy, String driverEmail, String riderEmail, String status, int points, boolean confirmedByDriver, boolean confirmedByRider) {
        this.rid = rid;
        this.type = type;
        this.to = to;
        this.from = from;
        this.datetime = datetime;
        this.postedBy = postedBy;
        this.acceptedBy = acceptedBy;
        this.driverEmail = driverEmail;
        this.riderEmail = riderEmail;
        this.status = status;
        this.points = points;
        this.confirmedByDriver = confirmedByDriver;
        this.confirmedByRider = confirmedByRider;
    }

    public String getRid() {
        return rid;
    }
}
