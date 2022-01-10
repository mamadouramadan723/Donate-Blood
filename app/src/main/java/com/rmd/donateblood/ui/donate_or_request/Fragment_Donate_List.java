package com.rmd.donateblood.ui.donate_or_request;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.rmd.donateblood.R;
import com.rmd.donateblood.databinding.FragmentDonateOrRequestListBinding;
import com.rmd.donateblood.main.Activity_Login_Register;
import com.rmd.donateblood.model.Donate_or_Request;
import com.rmd.donateblood.ui.recyclerAdapter.Recycler_Adapter_Donate_or_Request;

import java.util.ArrayList;
import java.util.List;


public class Fragment_Donate_List extends Fragment {
    
    private Recycler_Adapter_Donate_or_Request recyclerAdapterDonateRequest;
    private CollectionReference donate_request_ref;
    private List<Donate_or_Request> donateOrRequestList;
    private FragmentDonateOrRequestListBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_donate_or_request_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        checkUserConnection();

        donate_request_ref = FirebaseFirestore.getInstance().collection("donates");
        binding = FragmentDonateOrRequestListBinding.bind(view);
        donateOrRequestList = new ArrayList<>();
        getAllDonatesorRequests();
    }

    private void getAllDonatesorRequests() {
        donate_request_ref.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                donateOrRequestList.clear();
                for (QueryDocumentSnapshot document : value) {
                    Donate_or_Request data = document.toObject(Donate_or_Request.class);
                    donateOrRequestList.add(data);
                    recyclerAdapterDonateRequest = new
                            Recycler_Adapter_Donate_or_Request(getContext(), getActivity(),
                            Fragment_Donate_List.this,  donateOrRequestList);
                    binding.donateOrRequestListRecyclerview.setAdapter(recyclerAdapterDonateRequest);
                }
            }
        });
    }

    private void checkUserConnection() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(getContext(), Activity_Login_Register.class));
            requireActivity().finish();
        }
    }
}