package com.rmd.donateblood.notification.activity_fragment;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.rmd.donateblood.R;
import com.rmd.donateblood.main.Activity_Login_Register;
import com.rmd.donateblood.model.Donate_or_Request_Id_Type;
import com.rmd.donateblood.model.Notifications;
import com.rmd.donateblood.sharedviewmodel.SharedViewModel_Donate_Request;
import com.squareup.picasso.Picasso;

import java.util.List;

public class Notifications_RecyclerAdapter extends RecyclerView.Adapter<Notifications_RecyclerAdapter.NotificationViewHolder>{

    private Context context;
    private Fragment fragment;
    private FragmentActivity activity;
    private List<Notifications> notifications;
    private CollectionReference notif_ref;
    private SharedViewModel_Donate_Request sharedViewModelDonateRequest;

    public Notifications_RecyclerAdapter() { }

    public Notifications_RecyclerAdapter(Context context, List<Notifications> notifications) {
        this.context = context;
        this.notifications = notifications;
    }

    public Notifications_RecyclerAdapter(Context context, Fragment fragment, FragmentActivity activity, List<Notifications> notifications) {
        this.context = context;
        this.fragment = fragment;
        this.activity = activity;
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        userIsConnected();
        View view = LayoutInflater.from(context).inflate(R.layout.row_notification, parent, false);
        notif_ref = FirebaseFirestore.getInstance().collection("notification");
        sharedViewModelDonateRequest = new ViewModelProvider(activity).get(SharedViewModel_Donate_Request.class);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        //get data
        String userId = ""+notifications.get(position).getUserId();
        String image_url = ""+notifications.get(position).getImage_url();
        String notifId = ""+notifications.get(position).getNotifId();
        String notif_time = ""+notifications.get(position).getNotif_time();
        String notif_desc = ""+notifications.get(position).getNotif_desc();
        String type_notif = ""+notifications.get(position).getType_notif();
        String donate_request_id = ""+notifications.get(position).getDonate_request_id();
        CharSequence dateCharSeq = DateFormat.format("d MMM yyyy - h:mm a", Long.parseLong(notif_time)); //a ou p pour a.m ou p.m

        //set data
        holder.time_notif_Textview.setText(dateCharSeq);
        holder.desc_notif_Textview.setText(notif_desc);
        Picasso.get().load(image_url).into(holder.profile_notifiant_ImageView);

        holder.button_more_notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showmoreOptions(holder, notifId, image_url, type_notif, donate_request_id);
            }

        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedViewModelDonateRequest.set_id_and_type(new Donate_or_Request_Id_Type(donate_request_id, type_notif));
                Log.d("*****", "donate_request_id : "+donate_request_id);
                NavHostFragment.findNavController(fragment)
                        .navigate(R.id.action_nav_notification_to_nav_matched);
            }
        });
    }

    private void showmoreOptions(NotificationViewHolder holder, String notifId, String image_url,
                                 String type_notif, String offre_demande_id) {
        PopupMenu popupMenu = new PopupMenu(context, holder.button_more_notification, Gravity.END);

        //add item in menu
        //on aurait pu verifier si cette demande appartient à l'user actuel mais on a desactivé le boutton more
        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Supprimer");
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0){
                    //delete clicked
                    beginDelete(notifId, image_url);
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void beginDelete(String notifId, String image_url) {
        FirebaseStorage.getInstance().getReferenceFromUrl(image_url)
                .delete();

        notif_ref.document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("notifs")
                .document(notifId)
                .delete();
    }

    private void userIsConnected() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null){
            context.startActivity(new Intent(context, Activity_Login_Register.class));
        }
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder{

        ImageView profile_notifiant_ImageView;
        ImageButton button_more_notification;
        TextView desc_notif_Textview, time_notif_Textview;
        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            profile_notifiant_ImageView = itemView.findViewById(R.id.profile_notifiant_ImageView);
            button_more_notification = itemView.findViewById(R.id.button_more_notification);
            desc_notif_Textview = itemView.findViewById(R.id.desc_notif_Textview);
            time_notif_Textview = itemView.findViewById(R.id.time_notif_Textview);
        }
    }
}
