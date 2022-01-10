package com.rmd.donateblood.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rmd.donateblood.R;
import com.rmd.donateblood.model.User;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

//Pour s'authentifier
public class Activity_Login_Register extends AppCompatActivity {

    private static final String TAG = "Activity_Login_Register";
    private final int AUTHUI_REQUEST_CODE = 10001;

    private CollectionReference profile_ref, notif_ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Connection - Inscription");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        profile_ref = FirebaseFirestore.getInstance().collection("profiles");
        notif_ref = FirebaseFirestore.getInstance().collection("notification");

        checkUserConnection();
    }

    private void checkUserConnection() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(this, Activity_Main.class));
            //finish();
        }else {
            loginRegister();
        }
    }

    public void loginRegister() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build()
        );

        Intent intent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                //.setTosAndPrivacyPolicyUrls("https://example.com", "https://example.com")
                .setLogo(R.drawable.applogo)
                .setIsSmartLockEnabled(false)//pour ne pas qu'il utilise automatiquement les anciens credentiels
                .setAlwaysShowSignInMethodScreen(true) //si on a qu'un seul provider on ne le listera pas, on ira directement pour se logger
                .build();

        startActivityForResult(intent, AUTHUI_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTHUI_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String  userId, nom, phone_number, mail, image_url, blood_group;
                User my_user;

                //Checking for Utilisateur (New/Old)
                if (user.getMetadata().getCreationTimestamp() == user.getMetadata().getLastSignInTimestamp()) {
                    //This is a New Utilisateur
                    Toast.makeText(this, "Bienvenu au nouvel utilisateur", Toast.LENGTH_SHORT).show();

                    //add this new utilisateur in firestore
                    nom = ""+user.getDisplayName();
                    mail = ""+user.getEmail();
                    userId = user.getUid();
                    phone_number = "";
                    blood_group ="Not indicated";
                    image_url = "https://firebasestorage.googleapis.com/v0/b/donate-blood-2c0cc.appspot.com/o/applogo.png?alt=media&token=b9dd8cfd-4508-4659-a95f-7473dfd4bf47";
                    my_user = new User(userId, nom, phone_number, mail, image_url, blood_group );
                    String timeStamp = String.valueOf(System.currentTimeMillis());

                    String finalNom = nom;
                    profile_ref
                            .document(userId)
                            .set(my_user)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        //creer le document pour stocker les notifs du user
                                        HashMap<Object, String> hashMap = new HashMap<>();
                                        hashMap.put(userId, timeStamp);
                                        notif_ref
                                                .document(userId)
                                                .set(hashMap);

                                        Toast.makeText(Activity_Login_Register.this, " : Welcome "+ finalNom, Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                } else {
                    //This is a returning utilisateur
                    Toast.makeText(this, "Bon retour parmi nous", Toast.LENGTH_SHORT).show();
                }

                //dès que je m'authentifie je retour au main
                startActivity(new Intent(this, Activity_Main.class));
                finish();

            } else {
                // Signing in failed : si on clique le boutton de retour sans se logger
                IdpResponse response = IdpResponse.fromResultIntent(data);
                if (response == null) {
                    Log.d(TAG, "onActivityResult: the utilisateur has cancelled the sign in request");
                } else {
                    Log.e(TAG, "onActivityResult: ", response.getError());
                }
            }
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //goto previous activity
        return super.onSupportNavigateUp();
    }
}