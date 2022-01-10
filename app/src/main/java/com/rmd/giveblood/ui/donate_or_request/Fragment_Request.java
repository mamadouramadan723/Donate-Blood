package com.rmd.giveblood.ui.donate_or_request;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.rmd.giveblood.R;
import com.rmd.giveblood.databinding.FragmentDonateOrRequestBinding;
import com.rmd.giveblood.main.Activity_Login_Register;
import com.rmd.giveblood.model.Donate_or_Request;
import com.rmd.giveblood.model.Notifications;
import com.rmd.giveblood.notification.APIService;
import com.rmd.giveblood.notification.models.Client;
import com.rmd.giveblood.notification.models.Data;
import com.rmd.giveblood.notification.models.Response;
import com.rmd.giveblood.notification.models.Sender;
import com.rmd.giveblood.notification.models.Token;

import retrofit2.Call;
import retrofit2.Callback;


public class Fragment_Request extends Fragment implements AdapterView.OnItemSelectedListener {

    private String userId, donate_request_id, nom, phone_number, mail, image_url, blood_group, city, description, time;
    private Spinner spinner_ville, spinner_region, spinner_blood_group;
    private CollectionReference request_ref, profile_ref, notif_ref, token_ref;
    private ProgressDialog progressDialog;
    private FragmentDonateOrRequestBinding binding;
    private FirebaseAuth firebaseAuth;
    private APIService apiService;

    public Fragment_Request() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_donate_or_request, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        checkUserConnection();

        binding = FragmentDonateOrRequestBinding.bind(view);
        progressDialog = new ProgressDialog(getContext());

        spinner_region = view.findViewById(R.id.spinner_region);
        spinner_ville = view.findViewById(R.id.spinner_city);
        spinner_blood_group = view.findViewById(R.id.spinner_blood_group);
        spinner_blood_group.setOnItemSelectedListener(this);
        spinner_region.setOnItemSelectedListener(this);
        spinner_ville.setOnItemSelectedListener(this);

        request_ref = FirebaseFirestore.getInstance().collection("requests");
        profile_ref = FirebaseFirestore.getInstance().collection("profiles");
        notif_ref = FirebaseFirestore.getInstance().collection("notification");
        token_ref = FirebaseFirestore.getInstance().collection("tokens");

        //setOnClickListener
        binding.validateBtn.setOnClickListener(view1 -> upload_Request());

        //function
        getUserInfos();
        //cretae apiService
        apiService = Client.getRetrofit().create(APIService.class);
    }

    private void upload_Request() {
        progressDialog.setMessage("Uploading...");
        progressDialog.show();

        time = String.valueOf(System.currentTimeMillis());
        donate_request_id = userId + "__" + time;
        description = "" + binding.descriptionEdt.getText().toString();

        Donate_or_Request data = new Donate_or_Request(userId, donate_request_id, nom, phone_number,
                mail, image_url, blood_group, city, description, time, "requests");
        request_ref.document(donate_request_id).set(data)
                .addOnCompleteListener(task -> {
                    binding.descriptionEdt.setText("");
                    progressDialog.dismiss();
                    find_donor_and_send_notification();
                    NavHostFragment.findNavController(Fragment_Request.this)
                            .navigate(R.id.action_nav_request_to_nav_request_list);
                });
    }

    private void find_donor_and_send_notification() {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        request_ref.whereEqualTo("blood_group", blood_group)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String id = document.getData().get("userId").toString();
                                String notif_desc = "Cette Donation pourrait bien vous concerner, cliquez pour voir";
                                String type_notif = "donates";
                                Notifications notification =
                                        new Notifications(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                                image_url, timeStamp, timeStamp, notif_desc, type_notif, timeStamp);
                                notif_ref.document(id)
                                        .collection("notifs")
                                        .document(timeStamp)
                                        .set(notification);
                                send_notification(id);
                            }
                        }
                    }
                });
    }

    private void send_notification(String id) {
        //revcevoir le token du demandeur
        token_ref.document(id)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    Token token = documentSnapshot.toObject(Token.class);

                    Data data = new Data(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                            "Une Donation qui pourrait bien vous interesser ", "Nouveau Message", id, R.drawable.applogo);
                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotifiaction(sender)
                            .enqueue(new Callback<Response>() {
                                @Override
                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                                    Toast.makeText(getContext(), "Success : " + response.message(), Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onFailure(Call<Response> call, Throwable t) {
                                    Log.d("++++", "echec sending notif");
                                }
                            });
                }
            }
        });
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
                        userId = "" + document.getData().get("userId").toString();
                        nom = "" + document.getData().get("nom").toString();
                        phone_number = "" + document.getData().get("phone_number").toString();
                        mail = "" + document.getData().get("mail").toString();
                        image_url = "" + document.getData().get("image_url").toString();
                    }
                });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner spinner = (Spinner) parent;
        if (spinner.getId() == R.id.spinner_blood_group) {
            blood_group = parent.getItemAtPosition(position).toString();
        } else if (spinner.getId() == R.id.spinner_region) {
            if (position == 0) {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.ville_TangerTetouanAlHoceima, android.R.layout.simple_spinner_item);
                spinner_ville.setAdapter(adapter);
            } else if (position == 1) {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.ville_Oriental, android.R.layout.simple_spinner_item);
                spinner_ville.setAdapter(adapter);
            } else if (position == 2) {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.ville_FesMeknes, android.R.layout.simple_spinner_item);
                spinner_ville.setAdapter(adapter);
            } else if (position == 3) {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.ville_RabatSaleKenitra, android.R.layout.simple_spinner_item);
                spinner_ville.setAdapter(adapter);
            } else if (position == 4) {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.ville_BeniMellalKhénifra, android.R.layout.simple_spinner_item);
                spinner_ville.setAdapter(adapter);
            } else if (position == 5) {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.ville_CasablancaSettat, android.R.layout.simple_spinner_item);
                spinner_ville.setAdapter(adapter);
            } else if (position == 6) {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.ville_MarrakechSafi, android.R.layout.simple_spinner_item);
                spinner_ville.setAdapter(adapter);
            } else if (position == 7) {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.ville_DraaTafilalet, android.R.layout.simple_spinner_item);
                spinner_ville.setAdapter(adapter);
            } else if (position == 8) {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.ville_SousMassa, android.R.layout.simple_spinner_item);
                spinner_ville.setAdapter(adapter);
            } else if (position == 9) {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.ville_GuelmimOuedNoun, android.R.layout.simple_spinner_item);
                spinner_ville.setAdapter(adapter);
            } else if (position == 10) {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.ville_LaayouneSaguiaalHamra, android.R.layout.simple_spinner_item);
                spinner_ville.setAdapter(adapter);
            } else if (position == 11) {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.ville_EdDakhlaOuededDahab, android.R.layout.simple_spinner_item);
                spinner_ville.setAdapter(adapter);
            }

        } else if (spinner.getId() == R.id.spinner_city) {
            city = parent.getItemAtPosition(position).toString();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}