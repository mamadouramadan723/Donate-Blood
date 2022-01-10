package com.rmd.giveblood.notification.activity_fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.rmd.giveblood.R;
import com.rmd.giveblood.main.Activity_Login_Register;
import com.rmd.giveblood.model.Notifications;

import java.util.ArrayList;
import java.util.List;

public class Notifications_Fragment extends Fragment {

    private List<Notifications> notifications;
    private CollectionReference notif_ref;
    private RecyclerView recyclerView;
    private Notifications_RecyclerAdapter notificationsRecyclerAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        userIsConnected();
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true); //to show menu

        recyclerView = view.findViewById(R.id.recyclerView_notification);
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

                            notificationsRecyclerAdapter = new Notifications_RecyclerAdapter(getContext(), notifications);
                            recyclerView.setAdapter(notificationsRecyclerAdapter);
                        }
                    }
                });
    }

    private void userIsConnected() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null){
            startActivity(new Intent(getContext(), Activity_Login_Register.class));
            //getActivity().finish();
        }
    }
}
