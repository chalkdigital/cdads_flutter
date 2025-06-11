package com.chalkdigital.network.response;

import java.util.ArrayList;

public class GetUUidsResponse extends SSBBaseResponse{

    private String uuidListVersion;
    private ArrayList<String> uuids;

    public String getUuidListVersion() {
        return uuidListVersion;
    }

    public void setUuidListVersion(String uuidListVersion) {
        this.uuidListVersion = uuidListVersion;
    }

    public ArrayList<String> getUuids() {
        return uuids;
    }

    public void setUuids(ArrayList<String> uuids) {
        this.uuids = uuids;
    }
}
