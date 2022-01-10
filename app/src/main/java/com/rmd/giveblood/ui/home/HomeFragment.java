package com.rmd.giveblood.ui.home;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.type.Date;
import com.rmd.giveblood.R;
import com.rmd.giveblood.main.Activity_Login_Register;

import java.util.HashMap;

public class HomeFragment extends Fragment {

    private static final String TAG = "warning";
    @ServerTimestamp Date time;
    private CollectionReference token_ref;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        checkUserConnection();
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        token_ref = FirebaseFirestore.getInstance().collection("tokens");
        updateToken();

    }

    private void checkUserConnection() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(getContext(), Activity_Login_Register.class));
            requireActivity().finish();
        }
    }

    private void updateToken() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            //save uid of currently signed in user in shared preferences
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("Current_USERID", FirebaseAuth.getInstance().getCurrentUser().getUid());
            editor.apply();
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                                return;
                            }

                            // Get new FCM registration token
                            String token = task.getResult();
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("token", token);

                            token_ref.document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .set(hashMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                Log.d("token", "Success");
                                            }
                                        }
                                    });

                            // Log and toast
                            /*String msg = getString(R.string.msg_token_fmt, token);
                            Log.d(TAG, msg);
                            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();*/
                        }
                    });
        }
    }
}