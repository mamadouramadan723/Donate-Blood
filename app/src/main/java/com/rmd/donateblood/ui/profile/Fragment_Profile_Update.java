package com.rmd.donateblood.ui.profile;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rmd.donateblood.R;
import com.rmd.donateblood.databinding.FragmentProfileUpdateBinding;
import com.rmd.donateblood.main.Activity_Login_Register;
import com.squareup.picasso.Picasso;

import java.util.HashMap;


public class Fragment_Profile_Update extends Fragment {


    private FragmentProfileUpdateBinding binding;
    private CollectionReference profile_ref, donate_ref, request_ref;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private String userId, nom, phone_number, mail, image_url;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri image_uri;
    private StorageReference storageReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_update, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        checkUserConnection();
        progressDialog = new ProgressDialog(getContext());

        binding = FragmentProfileUpdateBinding.bind(view);
        profile_ref = FirebaseFirestore.getInstance().collection("profiles");
        donate_ref = FirebaseFirestore.getInstance().collection("donates");
        request_ref = FirebaseFirestore.getInstance().collection("reqests");
        storageReference = FirebaseStorage.getInstance().getReference("Users Profiles Images");

        binding.validateUpdateProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                update_Profile();
            }
        });

        binding.profileUpdateImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleImageClick();
            }
        });
        getUserInfos();
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
                        nom = "" + document.getData().get("nom").toString();
                        phone_number = "" + document.getData().get("phone_number").toString();
                        mail = "" + document.getData().get("mail").toString();
                        image_url = "" + document.getData().get("image_url").toString();

                        binding.usernameEdt.setText(nom);
                        binding.phoneNumberEdt.setText(phone_number);
                        binding.mailEdt.setText(mail);
                        Picasso.get().load(image_url).into(binding.profileUpdateImg);
                    }
                });
    }

    private void update_Profile() {
        progressDialog.setMessage("Updating...");
        progressDialog.show();

        nom = binding.usernameEdt.getText().toString();
        mail = binding.mailEdt.getText().toString();
        phone_number = binding.phoneNumberEdt.getText().toString();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("nom", nom);
        hashMap.put("mail", mail);
        hashMap.put("image_url", image_url);
        hashMap.put("phone_number", phone_number);

        profile_ref.document(firebaseAuth.getCurrentUser().getUid())
                .update(hashMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        //update his infos in his publications
                        donate_ref.addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                for (QueryDocumentSnapshot queryDocumentSnapshot : value) {
                                    donate_ref.document(queryDocumentSnapshot.getId())
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()&& task.getResult() != null) {
                                                        DocumentSnapshot document = task.getResult();
                                                        String userId;
                                                        userId = "" + document.getData().get("userId").toString();

                                                        //si cette donation appartient à cet utilisateur
                                                        if(userId.equals(firebaseAuth.getCurrentUser().getUid())){
                                                            donate_ref.document(queryDocumentSnapshot.getId())
                                                                    .update(hashMap);
                                                        }
                                                    }
                                                }
                                            });
                                }
                            }
                        });
                        request_ref.addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                for (QueryDocumentSnapshot queryDocumentSnapshot : value) {
                                    request_ref.document(queryDocumentSnapshot.getId())
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()&& task.getResult() != null) {
                                                        DocumentSnapshot document = task.getResult();
                                                        String userId;
                                                        userId = "" + document.getData().get("userId").toString();

                                                        //si cette donation appartient à cet utilisateur
                                                        if(userId.equals(firebaseAuth.getCurrentUser().getUid())){
                                                            request_ref.document(queryDocumentSnapshot.getId())
                                                                    .update(hashMap);
                                                        }
                                                    }
                                                }
                                            });
                                }
                            }
                        });

                        progressDialog.dismiss();
                        NavHostFragment.findNavController(Fragment_Profile_Update.this)
                                .navigate(R.id.action_nav_profile_update_to_nav_profile);
                    }
                });

    }

    public void handleImageClick() {
        Intent intent = new Intent();
        //intent.setType("*/*"); //pour n'importe qu'elle type (video/mp4, video/mpeg, image/png, image/jpg, ...)
        //pour seulement les images: image/* ou image/extension_d'image
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            image_uri = data.getData();
            Picasso.get().load(image_uri).into(binding.profileUpdateImg);
            uploadImage();
        }
    }

    private void uploadImage() {

        progressDialog.setMessage("Mise à jour en Cours ...");
        progressDialog.show();
        if (image_uri != null) {
            final StorageReference file_reference;
            storageReference.child(firebaseAuth.getCurrentUser().getUid())
                    .child("my_photo" + getFileExtension(image_uri))
                    .putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();

                            while (!uriTask.isSuccessful()) ;

                            //Toast.makeText(getContext(), "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                            String downloadUri = "" + uriTask.getResult();
                            //après upload obtenir un lien
                            if (uriTask.isSuccessful()) {
                                image_url = downloadUri;
                                progressDialog.dismiss();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(getContext(), "No file selected", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getActivity().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
}