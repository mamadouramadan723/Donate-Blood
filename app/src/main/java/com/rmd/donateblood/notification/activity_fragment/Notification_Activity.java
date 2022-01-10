package com.rmd.donateblood.notification.activity_fragment;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.rmd.donateblood.R;
import com.rmd.donateblood.main.Activity_Login_Register;
import com.rmd.donateblood.model.Notifications;
import com.rmd.donateblood.notification.APIService;

import java.util.ArrayList;
import java.util.List;

public class Notification_Activity extends AppCompatActivity {

    private List<Notifications> notifications;
    private CollectionReference notif_ref;
    private RecyclerView recyclerView;
    private Notifications_RecyclerAdapter notificationsRecyclerAdapter;

    private APIService apiService;
    private Boolean notify = false;
    private CollectionReference token_ref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userIsConnected();
        setContentView(R.layout.fragment_notification);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Notifications");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.recyclerView_notification);
        notif_ref = FirebaseFirestore.getInstance().collection("notification");

        notifications = new ArrayList<>();

        getMy_concerned_notif();
    }

    private void getMy_concerned_notif() {
        notif_ref.document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("notifs")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        notifications.clear();
                        for (QueryDocumentSnapshot document : value){
                            Notifications mynotifications = document.toObject(Notifications.class);
                            notifications.add(mynotifications);

                            notificationsRecyclerAdapter = new Notifications_RecyclerAdapter(Notification_Activity.this,  notifications);
                            recyclerView.setAdapter(notificationsRecyclerAdapter);
                        }
                    }
                });
    }

    private void userIsConnected() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null){
            startActivity(new Intent(this, Activity_Login_Register.class));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}