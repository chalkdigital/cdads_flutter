package com.chalkdigital.network.response;

import android.support.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

public class NetworkResponse implements Serializable{

    @Nullable
    private Object seatbid;
    @Nullable
    private Object base_sdk_event_url;
    @Nullable
    private Object mediationpriority;

    boolean isNoFillCase;

    @Nullable
    private ArrayList<Seatbid> seatbids;

    @Nullable
    public ArrayList<Seatbid> getSeatbid() {
        return seatbids;
    }

    public void parseResponse() {
        if (seatbid != null && seatbid instanceof ArrayList){
            ArrayList<Seatbid> seatbids = new ArrayList<>();
            for (Object o: ((ArrayList)seatbid)) {
                if (o instanceof Map){
                    Seatbid s = new Seatbid((Map<String, Object>)o);
                    seatbids.add(s);
                }
            }
            this.seatbids = seatbids;
            this.base_sdk_event_url = null;
            this.mediationpriority = null;
            isNoFillCase = false;
        }else{
            this.seatbids = null;
            if (mediationpriority!=null && mediationpriority instanceof ArrayList && ((ArrayList) mediationpriority).size()>0){
                isNoFillCase = true;
            }
        }
        this.seatbid = null;
    }

    @Nullable
    public Object getBase_sdk_event_url() {
        return base_sdk_event_url;
    }

    @Nullable
    public Object getSdks() {
        return mediationpriority;
    }
}
