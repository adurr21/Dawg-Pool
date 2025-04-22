package edu.uga.dawgpool.models;
public class Ride {

    private String rid;
    public String type; // "offer" or "request"
    public String to; // location
    public String from; // location
    public long datetime; // using long because was getting errors with TimeStamp
    public String postedBy; // user who posted, probably their uid
    public String acceptedBy; // null until accepted by a rider (then its their uid)
    public String status; // "open", "accepted", or "completed
    public int points; // 50 by default

    public Ride() {
        // Required for Firebase
    }

    public Ride(String rid, String type, String to, String from, long datetime,
                String postedBy, String acceptedBy, String status, int points) {
        this.rid = rid;
        this.type = type;
        this.to = to;
        this.from = from;
        this.datetime = datetime;
        this.postedBy = postedBy;
        this.acceptedBy = acceptedBy;
        this.status = status;
        this.points = points;
    }
}
