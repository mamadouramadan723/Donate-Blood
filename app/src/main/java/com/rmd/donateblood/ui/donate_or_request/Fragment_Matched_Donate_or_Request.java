package com.rmd.donateblood.ui.donate_or_request;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rmd.donateblood.R;
import com.rmd.donateblood.databinding.RowDonateOrRequestBinding;
import com.rmd.donateblood.main.Activity_Login_Register;
import com.rmd.donateblood.model.Donate_or_Request_Id_Type;
import com.rmd.donateblood.sharedviewmodel.SharedViewModel_Donate_Request;
import com.squareup.picasso.Picasso;

public class Fragment_Matched_Donate_or_Request extends Fragment {

    private SharedViewModel_Donate_Request sharedViewModelDonateRequest;
    private CollectionReference donate_request_ref;
    private String userId, donate_request_id, nom, phone_number, mail, image_url, blood_group, city, description, time, type;
    private Donate_or_Request_Id_Type donateOrRequestIdType = new Donate_or_Request_Id_Type();
    private RowDonateOrRequestBinding binding;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        checkUserConnection();
        return inflater.inflate(R.layout.row_donate_or_request, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = RowDonateOrRequestBinding.bind(view);
        //no need
        binding.moreBtn.setVisibility(View.GONE);

        sharedViewModelDonateRequest = new ViewModelProvider(getActivity()).get(SharedViewModel_Donate_Request.class);
        sharedViewModelDonateRequest.get_id_and_type().observe(getViewLifecycleOwner(), new Observer<Donate_or_Request_Id_Type>() {
            @Override
            public void onChanged(Donate_or_Request_Id_Type donate_or_request_id_type) {
                donateOrRequestIdType = donate_or_request_id_type;
                getInfos();
            }
        });

    }

    private void checkUserConnection() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(getContext(), Activity_Login_Register.class));
            requireActivity().finish();
        }
    }

    private void getInfos() {
        donate_request_ref = FirebaseFirestore.getInstance().collection(donateOrRequestIdType.getType());
        Log.d("****", "id="+donateOrRequestIdType.getType());
        donate_request_ref.document(donateOrRequestIdType.getDonate_request_id())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();

                            //nom, phone_number, mail, image_url, blood_group, city, description, time, type
                            nom =  "" + document.getData().get("nom").toString();
                            phone_number =  "" + document.getData().get("phone_number").toString();
                            mail =  "" + document.getData().get("mail").toString();
                            image_url =  "" + document.getData().get("image_url").toString();
                            blood_group = "" + document.getData().get("blood_group").toString();
                            city = "" + document.getData().get("city").toString();
                            description = "" + document.getData().get("description").toString();
                            time =  "" + document.getData().get("time").toString();

                            binding.nameTv.setText(nom);
                            binding.cityTv.setText(city);
                            binding.bloodGroupTv.setText(blood_group);
                            binding.descriptionTv.setText(description);
                            Picasso.get().load(image_url).into(binding.profileImageView);
                        }
                    }
                });
    }
}