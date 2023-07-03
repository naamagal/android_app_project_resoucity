package com.resourcity.naama.resourcity;

import android.app.Dialog;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int ERROR_DIALOG_REQUEST = 9001;

    private EditText editTextEmail;
    private EditText editTextPassword;
    private TextView textViewSignup;
    private Button loginBtn;

    private FirebaseAuth firebaseAuth;

    private Context thisContext = this;

    private User mOpUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();

        if(isServicesOK()){
            init();
        }
    }

    private void init(){
        loginBtn = (Button) findViewById(R.id.loginBtn);
        editTextEmail = (EditText) findViewById(R.id.emailEditText);
        editTextPassword = (EditText) findViewById(R.id.pwdEditText);
        textViewSignup = (TextView) findViewById(R.id.notAMemberTxt);

        if (firebaseAuth.getCurrentUser() != null)
        {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            startActivity(intent);
        }

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();

                if (firebaseAuth.getCurrentUser() != null)
                {
                    String emailStr = firebaseAuth.getCurrentUser().getEmail();

                    Intent intent = new Intent(MainActivity.this, MapActivity.class);
                    intent.putExtra("operatingUser", emailStr);
                    startActivity(intent);
                }
            }
        });

        TextView JoinSignTxt = (TextView) findViewById(R.id.notAMemberTxt);
        JoinSignTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                ViewResourceActivity
//                Intent intent = new Intent(MainActivity.this, JoinActivity.class);
//                startActivity(intent);
                signUpUser();
            }
        });
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void signUpUser()
    {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email))
        {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Please enter a valid password", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(thisContext, "Registered successfully", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(MainActivity.this, MapActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(thisContext, "Could not register... please try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void loginUser()
    {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            Intent intent = new Intent(MainActivity.this, MapActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(thisContext, "Could not Sign in... please enter valid email and password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        if (firebaseAuth.getCurrentUser() != null)
        {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            startActivity(intent);
        }
    }
}
