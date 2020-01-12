package com.example.tracking_app_v2;

//De fiecare data cand ne logam cu succes Serverul ne trimite datele user-ului
//Stocam acele date in clasa asta (folosim un JSON converter)
public class LoginResult {

    //nu trebuie sa folosim @SerializedName deoarece folosim aceleasi denumiri pentru variabile
    private String firstName;
    private String lastName;
    private String email;

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }
}
