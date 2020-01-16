package com.example.tracking_app_v2;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

//folosim interfata asta pentru request-urile HTTP

public interface RetrofitInterface {
    @POST("/users/login")
    Call<JWTResult> executeLogin(@Body HashMap<String, String> map);
    //la req de POST ni se returneaza o metoda Call ce stocheaza un obiect LoginResult
    //metoda va avea si un Body si prin el se transmit cele 3 date (stocam intr-un hashmap)

    @POST("/users")
    Call<Void> executeSignup(@Body HashMap<String, String> map);
    //aici nu avem nici un return deci clar e Call de Void
    //folosim din nou un HashMap pentru datele ce vor fi transmise (firstName, lastName, email, password)

    @POST("/users/{userEmail}/locations")
    Call<Void> executeSendLocation(@Path("userEmail") String userEmail, @Body HashMap<String, String> map);
    //pentru transmiterea de locatie, o sa transmitem "email", "lat" si "long"

}
