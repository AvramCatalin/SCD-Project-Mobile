package com.example.tracking_app_v2;

public class JWTResult {
    //nu trebuie sa folosim @SerializedName deoarece folosim aceleasi denumiri pentru variabile
    private String jwt;

    public String getJwt() {
        return jwt;
    }

}