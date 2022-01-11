package com.rmd.donateblood.ui.donate_or_request;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rmd.donateblood.R;
import com.rmd.donateblood.databinding.RowDonateOrRequestBinding;
import com.rmd.donateblood.main.Activity_Login_Register;
import com.squareup.picasso.Picasso;

import java.net.URLEncoder;

public class Activity_Matched_Donate_or_Request extends AppCompatActivity {

    private CollectionReference donate_request_ref;
    private String userId, donate_request_id, nom, phone_number, mail, image_url, blood_group, city, description, time, type;
    private RowDonateOrRequestBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkUserConnection();
        binding = RowDonateOrRequestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //no need
        binding.moreBtn.setVisibility(View.GONE);

        Intent intent = getIntent();
        type = ""+intent.getStringExtra("type_notif");
        donate_request_id = ""+intent.getStringExtra("donate_request_id");
        getInfos();

        //setOnClickListener
        binding.callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phone_number));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
        binding.whatsappBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PackageManager packageManager = getPackageManager();
                Intent i = new Intent(Intent.ACTION_VIEW);

                String text = "Bonjour.\n" +
                        "S'il vous plaît, je viens de voir votre publication sur " +
                        getResources().getString(R.string.app_name);
                try {
                    String url = "https://api.whatsapp.com/send?phone=+212" + phone_number + "&text=" + URLEncoder.encode(text, "UTF-8");
                    i.setPackage("com.whatsapp");
                    i.setData(Uri.parse(url));
                    if (i.resolveActivity(packageManager) != null) {
                        startActivity(i);
                    } else {
                        Toast.makeText(Activity_Matched_Donate_or_Request.this, "Make Sure you've installed Whatsapp", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void checkUserConnection() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, Activity_Login_Register.class));
            finish();
        }
    }

    private void getInfos() {
        donate_request_ref = FirebaseFirestore.getInstance().collection(type);

        donate_request_ref.document(donate_request_id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();

                            //nom, phone_number, mail, image_url, blood_group, city, description, time, type
                            nom = "" + document.getData().get("nom").toString();
                            phone_number = "" + document.getData().get("phone_number").toString();
                            mail = "" + document.getData().get("mail").toString();
                            image_url = "" + document.getData().get("image_url").toString();
                            blood_group = "" + document.getData().get("blood_group").toString();
                            city = "" + document.getData().get("city").toString();
                            description = "" + document.getData().get("description").toString();
                            time = "" + document.getData().get("time").toString();

                            Log.d("++++++", ""+blood_group);
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