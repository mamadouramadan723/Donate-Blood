package com.rmd.donateblood.ui.recyclerAdapter;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rmd.donateblood.R;
import com.rmd.donateblood.databinding.RowDonateOrRequestBinding;
import com.rmd.donateblood.model.Donate_or_Request;
import com.rmd.donateblood.model.Donate_or_Request_Id_Type;
import com.rmd.donateblood.sharedviewmodel.SharedViewModel_Donate_Request;
import com.squareup.picasso.Picasso;

import java.net.URLEncoder;
import java.util.List;

public class Recycler_Adapter_Donate_or_Request extends RecyclerView.Adapter<Recycler_Adapter_Donate_or_Request.Donate_or_Request_List_ViewHolder> {

    private Context context;
    private FragmentActivity activity;
    private Fragment fragment;
    private List<Donate_or_Request> donateOrRequestList;
    private RowDonateOrRequestBinding binding;
    private CollectionReference donate_or_request_ref;
    private SharedViewModel_Donate_Request sharedViewModelDonateRequest;

    public Recycler_Adapter_Donate_or_Request(Context context, FragmentActivity activity, Fragment fragment, List<Donate_or_Request> donateOrRequestList) {
        this.context = context;
        this.activity = activity;
        this.fragment = fragment;
        this.donateOrRequestList = donateOrRequestList;
    }

    @NonNull
    @Override
    public Recycler_Adapter_Donate_or_Request.Donate_or_Request_List_ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_donate_or_request, parent, false);
        sharedViewModelDonateRequest = new ViewModelProvider(activity).get(SharedViewModel_Donate_Request.class);
        return new Recycler_Adapter_Donate_or_Request.Donate_or_Request_List_ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Recycler_Adapter_Donate_or_Request.Donate_or_Request_List_ViewHolder holder, int position) {

        binding = RowDonateOrRequestBinding.bind(holder.itemView);

        //getValues
        String userId, donate_request_id, nom, phone_number, mail, image_url, blood_group, city, description, time, type;
        userId = donateOrRequestList.get(position).getUserId();
        donate_request_id = donateOrRequestList.get(position).getDonate_request_id();
        nom = donateOrRequestList.get(position).getNom();
        phone_number = donateOrRequestList.get(position).getPhone_number();
        mail = donateOrRequestList.get(position).getMail();
        image_url = donateOrRequestList.get(position).getImage_url();
        blood_group = donateOrRequestList.get(position).getBlood_group();
        city = donateOrRequestList.get(position).getCity();
        description = donateOrRequestList.get(position).getDescription();
        time = donateOrRequestList.get(position).getTime();
        type = donateOrRequestList.get(position).getType();

        //setValues
        binding.nameTv.setText(nom);
        binding.time.setText(time);
        binding.cityTv.setText(city);
        binding.bloodGroupTv.setText(blood_group);
        binding.descriptionTv.setText(description);
        Picasso.get().load(image_url).into(binding.profileImageView);

        //visibility
        if (!userId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            binding.moreBtn.setVisibility(View.GONE);
        }
        if (description.equals("")) {
            binding.layoutDesc.setVisibility(View.GONE);
        }
        //setOnClickListener
        binding.callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phone_number));
                if (intent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(intent);
                }
            }
        });
        binding.whatsappBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PackageManager packageManager = context.getPackageManager();
                Intent i = new Intent(Intent.ACTION_VIEW);

                String text = "Bonjour.\n" +
                        "S'il vous plaît, je viens de voir votre publication sur " +
                        context.getResources().getString(R.string.app_name);
                try {
                    String url = "https://api.whatsapp.com/send?phone=+212" + phone_number + "&text=" + URLEncoder.encode(text, "UTF-8");
                    i.setPackage("com.whatsapp");
                    i.setData(Uri.parse(url));
                    if (i.resolveActivity(packageManager) != null) {
                        context.startActivity(i);
                    } else {
                        Toast.makeText(context, "Make Sure you've installed Whatsapp", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        binding.otherBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        binding.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                show_more_options(donate_request_id, type);
            }
        });
    }

    private void show_more_options(String donate_request_id, String type) {
        PopupMenu popupMenu = new PopupMenu(context, binding.moreBtn, Gravity.END);
        //add item in menu
        //on aurait pu verifier si cette demande appartient à l'user actuel mais on a desactivé le boutton more
        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
        popupMenu.getMenu().add(Menu.NONE, 1, 0, "Modify");

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0) {
                    //delete clicked
                    beginDelete(donate_request_id, type);
                } else if (id == 1) {
                    sharedViewModelDonateRequest.set_id_and_type(new Donate_or_Request_Id_Type(donate_request_id, type));
                    if(type.equals("donates")){
                        NavHostFragment.findNavController(fragment)
                                .navigate(R.id.action_nav_donate_list_to_nav_update_donate_request);
                    }
                    else if(type.equals("requests")){
                        NavHostFragment.findNavController(fragment)
                                .navigate(R.id.action_nav_request_list_to_nav_update_donate_request);
                    }

                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void beginDelete(String donate_request_id, String type) {
        donate_or_request_ref = FirebaseFirestore.getInstance().collection(type);
        donate_or_request_ref.document(donate_request_id)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Delete Successfully", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(context, "Error Deletion", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return donateOrRequestList.size();
    }

    class Donate_or_Request_List_ViewHolder extends RecyclerView.ViewHolder {

        public Donate_or_Request_List_ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}