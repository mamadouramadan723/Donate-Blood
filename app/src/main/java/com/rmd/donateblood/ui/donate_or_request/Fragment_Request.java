package com.rmd.donateblood.ui.donate_or_request;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
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
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.rmd.donateblood.R;
import com.rmd.donateblood.databinding.FragmentDonateOrRequestBinding;
import com.rmd.donateblood.main.Activity_Login_Register;
import com.rmd.donateblood.model.Donate_or_Request;
import com.rmd.donateblood.model.Notifications;
import com.rmd.donateblood.notification.APIService;
import com.rmd.donateblood.notification.Notification_AutoSendNotif_OreoAndAbove;
import com.rmd.donateblood.notification.activity_fragment.Notification_Activity;
import com.rmd.donateblood.notification.models.Client;
import com.rmd.donateblood.notification.models.Data;
import com.rmd.donateblood.notification.models.Response;
import com.rmd.donateblood.notification.models.Sender;
import com.rmd.donateblood.notification.models.Token;

import java.util.ArrayList;
import java.util.List;

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
    private List<String> compatible_list = new ArrayList<>();
    private static final int NOTIF_ID = 123;

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

        //
        get_blood_compatibility();
        //

        Donate_or_Request data = new Donate_or_Request(userId, donate_request_id, nom, phone_number,
                mail, image_url, blood_group, city, description, time, "requests");
        request_ref.document(donate_request_id).set(data)
                .addOnCompleteListener(task -> {
                    binding.descriptionEdt.setText("");
                    progressDialog.dismiss();
                    String timeStamp = String.valueOf(System.currentTimeMillis());
                    find_donor_inside_donation_and_send_notification(timeStamp);
                    find_donor_inside_profiles_and_send_notification(timeStamp);
                    NavHostFragment.findNavController(Fragment_Request.this)
                            .navigate(R.id.action_nav_request_to_nav_request_list);
                });
    }

    private void find_donor_inside_profiles_and_send_notification(String timeStamp) {

        profile_ref.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (QueryDocumentSnapshot queryDocumentSnapshot : value) {
                    profile_ref.document(queryDocumentSnapshot.getId())
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @RequiresApi(api = Build.VERSION_CODES.O)
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        String this_blood_group, this_user_id;
                                        this_blood_group = "" + document.getData().get("blood_group").toString();
                                        this_user_id = "" + document.getData().get("userId").toString();

                                        //s'ils sont compatibles
                                        if (compatible_list.contains(this_blood_group)) {
                                            //notify donor
                                            String notif_desc = "Vous pouvez peut être sauver cette vie, cliquez pour voir";
                                            String type_notif = "requests";
                                            Notifications notification =
                                                    new Notifications(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                                            image_url, timeStamp, timeStamp, notif_desc, type_notif, donate_request_id);

                                            notif_ref.document(this_user_id)
                                                    .collection("notifs")
                                                    .document(timeStamp)
                                                    .set(notification);
                                            send_notification_to_donor(this_user_id);

                                            //notify requester
                                            notif_desc = "Cette Personne pourrait bien vous aider, cliquez pour voir";
                                            if (this_user_id.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                                type_notif = "requests";
                                            } else {
                                                type_notif = "donates";
                                            }

                                            notification =
                                                    new Notifications(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                                            image_url, timeStamp, timeStamp, notif_desc, type_notif, donate_request_id);

                                            notif_ref.document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                    .collection("notifs")
                                                    .document(timeStamp)
                                                    .set(notification);
                                            send_notification_to_requester(this_user_id);
                                        }
                                    }
                                }
                            });
                }
            }
        });
    }

    private void find_donor_inside_donation_and_send_notification(String timeStamp) {

        request_ref.whereIn("blood_group", compatible_list)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String id = document.getData().get("userId").toString();

                                //notify donor
                                String notif_desc = "Cette Requete pourrait bien vous concerner, cliquez pour voir";
                                String type_notif = "requests";
                                Notifications notification =
                                        new Notifications(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                                image_url, timeStamp, timeStamp, notif_desc, type_notif, donate_request_id);

                                notif_ref.document(id)
                                        .collection("notifs")
                                        .document(timeStamp)
                                        .set(notification);
                                send_notification_to_donor(id);

                                //notify requester
                                notif_desc = "Cette Donation pourrait bien vous concerner, cliquez pour voir";
                                if (id.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                    type_notif = "requests";
                                } else {
                                    type_notif = "donates";
                                }

                                notification =
                                        new Notifications(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                                image_url, timeStamp, timeStamp, notif_desc, type_notif, donate_request_id);

                                notif_ref.document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .collection("notifs")
                                        .document(timeStamp)
                                        .set(notification);
                                send_notification_to_requester(id);
                            }
                        }
                    }
                });
    }

    private void get_blood_compatibility() {
        //compatible_list.clear();
        Log.d("******2", "" + blood_group);
        //compatible_list.add("");
        switch (blood_group) {
            case "A+":
                compatible_list.add("A+");
                compatible_list.add("A-");
                compatible_list.add("O+");
                compatible_list.add("O-");
                break;
            case "A-":
                compatible_list.add("A-");
                compatible_list.add("0-");
                break;
            case "B+":
                compatible_list.add("B+");
                compatible_list.add("B-");
                compatible_list.add("O+");
                compatible_list.add("O-");
                break;
            case "B-":
                compatible_list.add("B-");
                compatible_list.add("O-");
                break;
            case "AB+":
                compatible_list.add("A+");
                compatible_list.add("A-");
                compatible_list.add("B+");
                compatible_list.add("B-");
                compatible_list.add("AB+");
                compatible_list.add("AB-");
                compatible_list.add("O+");
                compatible_list.add("O-");
                break;
            case "AB-":
                compatible_list.add("A-");
                compatible_list.add("B-");
                compatible_list.add("AB-");
                compatible_list.add("O-");
                break;
            case "0+":
                compatible_list.add("O+");
                compatible_list.add("O-");
                break;
            case "0-":
                compatible_list.add("0-");
                break;
            default:
                compatible_list.add(blood_group);
                break;
        }
        Log.d("******3", "" + compatible_list);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void send_notification_to_requester(String id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //O = 26

            Notification_AutoSendNotif_OreoAndAbove beforeOreoNotification = new Notification_AutoSendNotif_OreoAndAbove(getContext());
            beforeOreoNotification.notify(1, false, "Nouveau Message", "une Donation qui pourrait bien vous interesser");
        } else {
            long[] swPattern = new long[]{0, 500, 110, 500, 110, 450, 110, 200, 110,
                    170, 40, 450, 110, 200, 110, 170, 40, 500};
            Context context = getContext();
            Resources res = context.getResources();
            Intent notificationIntent = new Intent(context, Notification_Activity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(
                    context, 456, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            Notification notification = new Notification.Builder(context)
                    .setSmallIcon(R.drawable.applogo)     // drawable for API 26
                    .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.applogo))
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true)
                    .setContentIntent(contentIntent)
                    .setContentTitle("Nouveau Message")
                    .setContentText("une Offre qui pourrait bien vous interesser")
                    .setLights(Color.RED, 3000, 3000)
                    .setVibrate(swPattern)
                    .getNotification();     // avant l'API 16
            //.build();             // à partir de l'API 16

            NotificationManager notifManager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            notifManager.notify(NOTIF_ID, notification);
            Log.i("MainActivity", "Notifications launched");
        }
    }

    private void send_notification_to_donor(String id) {
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