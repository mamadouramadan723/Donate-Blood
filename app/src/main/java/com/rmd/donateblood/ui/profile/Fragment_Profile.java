package com.rmd.donateblood.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rmd.donateblood.R;
import com.rmd.donateblood.databinding.FragmentProfileBinding;
import com.rmd.donateblood.main.Activity_Login_Register;
import com.squareup.picasso.Picasso;


public class Fragment_Profile extends Fragment {

    private FragmentProfileBinding binding;
    private CollectionReference profile_ref;
    private FirebaseAuth firebaseAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        checkUserConnection();

        binding = FragmentProfileBinding.bind(view);
        profile_ref = FirebaseFirestore.getInstance().collection("profiles");

        binding.updateProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(Fragment_Profile.this)
                        .navigate(R.id.action_nav_profile_to_nav_profile_update);
            }
        });
        getUserInfos();
    }

    private void checkUserConnection() {
        if (firebaseAuth.getCurrentUser() == null) {
            startActivity(new Intent(getContext(), Activity_Login_Register.class));
            requireActivity().finish();
        }
    }

    private void getUserInfos() {

        profile_ref.document(firebaseAuth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        String nom, phone_number, mail, image_url;
                        nom = "" + document.getData().get("nom").toString();
                        phone_number = "" + document.getData().get("phone_number").toString();
                        mail = "" + document.getData().get("mail").toString();
                        image_url = "" + document.getData().get("image_url").toString();

                        binding.usernameTv.setText(nom);
                        binding.phoneNumberTv.setText(phone_number);
                        binding.mailTv.setText(mail);
                        Picasso.get().load(image_url).into(binding.profileImgv);
                    }
                });
    }

}