package com.example.tracking_app_v2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    //creez cele 2 variabile globale pentru transmiterea de date pe server
    private Retrofit retrofit;
    private RetrofitInterface retrofitInterface;
    //aici o sa fie URL-ul la care se fac request-urile
    //daca il rulez din emulator folosesc 10.0.2.2 (echivalent localhost)
    //eu folosesc ngrok ca si local tunnel provider (link-ul de mai jos)
    private static String Base_URL = "https://54760dd7.ngrok.io";

    //facem un getter pentru a putea accesa url-ul si din RegisterActivity
    //daca se schimba URL-ul trebuie modificat intr-un singur loc (aici)
    public static String getBase_URL() {
        return Base_URL;
    }

    //declaram global obiectul result care va tine datele returnate de pe server
    private static LoginResult result;

    //ii facem si un getter ca sa il putem accesa din LocationActivity
    public static LoginResult getResult() {
        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //instantiem obiectul retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(Base_URL)
                //folosim urmatoarea comanda pentru a seta conevertorul JSON to Java object
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        //instantiem obiectul retrofitInterface
        retrofitInterface = retrofit.create(RetrofitInterface.class);

        //cautam butonul de login si ii adaugam un event listener
        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleLogin();
            }
        });

        //adaugam navigarea Login -----> Register
        findViewById(R.id.toRegister).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openRegisterActivity();
            }
        });

        //adaugam on Change event listeners
        changeEventAdder((EditText)findViewById(R.id.emailEdit));
        changeEventAdder((EditText)findViewById(R.id.passwordEdit));

    }

    private void changeEventAdder(EditText fieldEdit) {
        fieldEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final TextView TextViewLoginError = findViewById(R.id.textViewLoginError);
                TextViewLoginError.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    // functie de validat adresa de email
    private boolean isValidEmail(String email) {
        String EMAIL_PATTERN = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    // functie de validat parola
    private boolean isValidPassword(String pass) {
        String PASS_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$";
        Pattern pattern = Pattern.compile(PASS_PATTERN);
        Matcher matcher = pattern.matcher(pass);
        return matcher.matches();
    }

    private void openRegisterActivity() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    private void openLocationActivity() {
        Intent intent = new Intent(this, LocationActivity.class);
        startActivity(intent);
    }

    private void handleLogin() {

        boolean errorOccurred = false;

        final EditText emailEdit = findViewById(R.id.emailEdit);
        final EditText passwordEdit = findViewById(R.id.passwordEdit);

        final String email = emailEdit.getText().toString();
        if (!isValidEmail(email)) {
            emailEdit.setError("Adresa email nu este valida!");
            errorOccurred = true;
        }

        final String pass = passwordEdit.getText().toString();
        if (!isValidPassword(pass)) {
            passwordEdit.setError("Parola trebuie sa fie de cel putin 8 caractere!\nEa trebuie sa contina cel putin o cifra si o litera!");
            errorOccurred = true;
        }

        //verificam daca nu avem erori de validare
        if (!errorOccurred) {
            final TextView TextViewLoginError = findViewById(R.id.textViewLoginError);

            //Cand dam click pe butonul de login se trimite email-ul si parola spre server
            HashMap<String, String> map = new HashMap<>();

            map.put("email", emailEdit.getText().toString());
            map.put("password", passwordEdit.getText().toString());

            //folosim interfata retrofit pentru a crea request-ul
            Call<LoginResult> call = retrofitInterface.executeLogin(map);

            //pentru a executa request-ul folosim enqueue
            //aceasta metoda asteapta un Callback
            call.enqueue(new Callback<LoginResult>() {
                @Override
                public void onResponse(Call<LoginResult> call, Response<LoginResult> response) {
                    //este apleata atunci cand serverul raspunde la request-ul nostru
                    //verificam daca utilizatorul s-a logat cu succes (200)
                    if (response.code() == 200) {
                        //salvam datele returnate de server in result
                        result = response.body();
                        openLocationActivity();
                        //daca datele introduse sunt gresite
                    } else if (response.code() == 404) {
                        TextViewLoginError.setText("Date de autentificare gresite!");
                        TextViewLoginError.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onFailure(Call<LoginResult> call, Throwable t) {
                    //este apelata atunci cand avem request fail
                    TextViewLoginError.setText(t.getMessage());
                    TextViewLoginError.setVisibility(View.VISIBLE);
                }
            });
        }
    }
}
