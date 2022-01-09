package com.rmd.giveblood.ui.notification;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.rmd.giveblood.R;
import com.rmd.giveblood.main.Activity_Login_Register;


public class Fragment_Notification extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        checkUserConnection();
    }

    private void checkUserConnection() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(getContext(), Activity_Login_Register.class));
            requireActivity().finish();
        }
    }
}