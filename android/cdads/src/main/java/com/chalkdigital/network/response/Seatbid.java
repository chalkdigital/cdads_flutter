package com.chalkdigital.network.response;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

public class Seatbid implements Serializable{

    private String seat;
    private ArrayList<Bid> bid;

    public Seatbid(Map<String , Object> map) {
        setBid(map.get("bid"));
        setSeat(map.get("seat"));
    }

    public String getSeat() {
        return seat;
    }

    public void setSeat(final Object seat) {
        this.seat = TypeParser.parseString(seat, "");
    }

    public ArrayList<Bid> getBid() {
        return bid;
    }

    public void setBid(final Object bid) {
        if (bid!=null && bid instanceof ArrayList){
            ArrayList<Bid> bids = new ArrayList<>();
            for (Object o: ((ArrayList)bid)) {
                if (o instanceof Map){
                    Bid b = new Bid((Map<String, Object>)o);
                    bids.add(b);
                }
            }
            this.bid = bids;
        }else
            this.bid = null;
    }
}
