package com.example.tracking_app_v2;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

public class RegisterActivity extends AppCompatActivity {

    private Retrofit retrofit;
    private RetrofitInterface retrofitInterface;

    private String Base_URL = LoginActivity.getBase_URL();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //instantiem obiectul retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(Base_URL)
                //folosim urmatoarea comanda pentru a seta conevertorul JSON to Java object
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        //instantiem obiectul retrofitInterface
        retrofitInterface = retrofit.create(RetrofitInterface.class);

        findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleRegister();
            }
        });

        //adaugam navigarea Register -----> Login
        findViewById(R.id.toLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openLoginActivity();
            }
        });

        //adaugam on Change event listeners
        changeEventAdder((EditText)findViewById(R.id.emailEdit));

    }

    private void changeEventAdder(EditText fieldEdit) {
        fieldEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final TextView TextViewRegisterError = findViewById(R.id.textViewRegisterError);
                TextViewRegisterError.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void openLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
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

    // functie de validat celelalte campuri
    private boolean isValidField(String fieldContent, int length) {
        if (fieldContent != null && fieldContent.length() >= length) {
            return true;
        }
        return false;
    }

    private void handleRegister() {

        boolean errorOccurred = false;

        final EditText firstNameEdit = findViewById(R.id.firstNameEdit);
        final EditText lastNameEdit = findViewById(R.id.lastNameEdit);
        final EditText emailEdit = findViewById(R.id.emailEdit);
        final EditText passwordEdit = findViewById(R.id.passwordEdit);

        final String firstName = firstNameEdit.getText().toString();
        if (!isValidField(firstName, 1)) {
            firstNameEdit.setError("Acest camp nu poate fi gol!");
            errorOccurred = true;
        }

        final String lastName = lastNameEdit.getText().toString();
        if (!isValidField(lastName, 1)) {
            lastNameEdit.setError("Acest camp nu poate fi gol!");
            errorOccurred = true;
        }

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

        if (!errorOccurred) {

            final TextView TextViewRegisterError = findViewById(R.id.textViewRegisterError);

            HashMap<String, String> map = new HashMap<>();

            map.put("firstName", firstNameEdit.getText().toString());
            map.put("lastName", lastNameEdit.getText().toString());
            map.put("email", emailEdit.getText().toString());
            map.put("password", passwordEdit.getText().toString());

            Call<Void> call = retrofitInterface.executeSignup(map);

            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    //verificam daca noul utilizator a fost inregistrat cu succes (200)
                    if (response.code() == 201) {
                        Toast.makeText(RegisterActivity.this, "Inregistrat cu succes!", Toast.LENGTH_LONG).show();
                        openLoginActivity();
                        //verificam daca exista acest cont deja (400)
                    } else if (response.code() == 400) {
                        TextViewRegisterError.setText("Exista deja un cont cu aceasta adresa de email!");
                        TextViewRegisterError.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    TextViewRegisterError.setText(t.getMessage());
                    TextViewRegisterError.setVisibility(View.VISIBLE);
                }
            });
        }
    }
}
