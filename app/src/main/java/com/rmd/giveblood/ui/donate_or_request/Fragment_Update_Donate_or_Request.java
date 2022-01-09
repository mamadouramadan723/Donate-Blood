package com.rmd.giveblood.ui.donate_or_request;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rmd.giveblood.R;
import com.rmd.giveblood.databinding.FragmentDonateOrRequestBinding;
import com.rmd.giveblood.main.Activity_Login_Register;
import com.rmd.giveblood.model.Donate_or_Request_Id_Type;
import com.rmd.giveblood.sharedviewmodel.SharedViewModel_Donate_Request;

import java.util.HashMap;

public class Fragment_Update_Donate_or_Request extends Fragment implements AdapterView.OnItemSelectedListener {


    private SharedViewModel_Donate_Request sharedViewModelDonateRequest;
    private CollectionReference donate_request_ref;
    private FragmentDonateOrRequestBinding binding;
    private Donate_or_Request_Id_Type donateOrRequestIdType = new Donate_or_Request_Id_Type();
    private String blood_group, city, description;
    private ProgressDialog progressDialog;
    private Spinner spinner_city, spinner_region, spinner_blood_group;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        checkUserConnection();
        return inflater.inflate(R.layout.fragment_donate_or_request, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentDonateOrRequestBinding.bind(view);
        progressDialog = new ProgressDialog(getContext());

        spinner_region = view.findViewById(R.id.spinner_region);
        spinner_city = view.findViewById(R.id.spinner_city);
        spinner_blood_group = view.findViewById(R.id.spinner_blood_group);
        spinner_blood_group.setOnItemSelectedListener(this);
        spinner_region.setOnItemSelectedListener(this);
        spinner_city.setOnItemSelectedListener(this);

        sharedViewModelDonateRequest = new ViewModelProvider(getActivity()).get(SharedViewModel_Donate_Request.class);
        sharedViewModelDonateRequest.get_id_and_type().observe(getViewLifecycleOwner(), new Observer<Donate_or_Request_Id_Type>() {
            @Override
            public void onChanged(Donate_or_Request_Id_Type donate_or_request_id_type) {
                donateOrRequestIdType = donate_or_request_id_type;
                getInfos();
            }
        });

        binding.validateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.setMessage("Updating...");
                progressDialog.show();

                description = "" + binding.descriptionEdt.getText().toString();
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("city", city);
                hashMap.put("blood_group", blood_group);
                hashMap.put("description", description);

                donate_request_ref = FirebaseFirestore.getInstance().collection(donateOrRequestIdType.getType());
                donate_request_ref.document(donateOrRequestIdType.getDonate_request_id())
                        .update(hashMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                progressDialog.dismiss();
                                if (donateOrRequestIdType.getType().equals("donates")) {
                                    NavHostFragment.findNavController(Fragment_Update_Donate_or_Request.this)
                                            .navigate(R.id.action_nav_update_donate_request_to_nav_donate_list);
                                } else if (donateOrRequestIdType.getType().equals("requests")) {
                                    NavHostFragment.findNavController(Fragment_Update_Donate_or_Request.this)
                                            .navigate(R.id.action_nav_update_donate_request_to_nav_request_list);
                                }
                            }
                        });
            }
        });
    }

    private void getInfos() {
        donate_request_ref = FirebaseFirestore.getInstance().collection(donateOrRequestIdType.getType());
        donate_request_ref.document(donateOrRequestIdType.getDonate_request_id())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();
                            //les 3 variables que l'on peut modifier sont :  blood_group, city et description
                            blood_group = "" + document.getData().get("blood_group").toString();
                            city = "" + document.getData().get("city").toString();
                            description = "" + document.getData().get("description").toString();

                            //on laisse les spinner comme ils sont et on remplit le champ description
                            binding.descriptionEdt.setText(description);
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner spinner = (Spinner) parent;
        if (spinner.getId() == R.id.spinner_blood_group) {
            blood_group = parent.getItemAtPosition(position).toString();
        } else if (spinner.getId() == R.id.spinner_region) {
            if (position == 0) {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.ville_TangerTetouanAlHoceima, android.R.layout.simple_spinner_item);
                spinner_city.setAdapter(adapter);
            } else if (position == 1) {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.ville_Oriental, android.R.layout.simple_spinner_item);
                spinner_city.setAdapter(adapter);
            } else if (position == 2) {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.ville_FesMeknes, android.R.layout.simple_spinner_item);
                spinner_city.setAdapter(adapter);
            } else if (position == 3) {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.ville_RabatSaleKenitra, android.R.layout.simple_spinner_item);
                spinner_city.setAdapter(adapter);
            } else if (position == 4) {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.ville_BeniMellalKhénifra, android.R.layout.simple_spinner_item);
                spinner_city.setAdapter(adapter);
            } else if (position == 5) {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.ville_CasablancaSettat, android.R.layout.simple_spinner_item);
                spinner_city.setAdapter(adapter);
            } else if (position == 6) {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.ville_MarrakechSafi, android.R.layout.simple_spinner_item);
                spinner_city.setAdapter(adapter);
            } else if (position == 7) {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.ville_DraaTafilalet, android.R.layout.simple_spinner_item);
                spinner_city.setAdapter(adapter);
            } else if (position == 8) {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.ville_SousMassa, android.R.layout.simple_spinner_item);
                spinner_city.setAdapter(adapter);
            } else if (position == 9) {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.ville_GuelmimOuedNoun, android.R.layout.simple_spinner_item);
                spinner_city.setAdapter(adapter);
            } else if (position == 10) {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.ville_LaayouneSaguiaalHamra, android.R.layout.simple_spinner_item);
                spinner_city.setAdapter(adapter);
            } else if (position == 11) {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.ville_EdDakhlaOuededDahab, android.R.layout.simple_spinner_item);
                spinner_city.setAdapter(adapter);
            }

        } else if (spinner.getId() == R.id.spinner_city) {
            city = parent.getItemAtPosition(position).toString();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}