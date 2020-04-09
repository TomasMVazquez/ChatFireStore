package com.applications.toms.chatfirestore.fragments;


import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.applications.toms.chatfirestore.R;
import com.applications.toms.chatfirestore.model.User;
import com.applications.toms.chatfirestore.util.Keys;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private static final int IMAGE_REQUEST = 1;
    public static final String NAME = "fragment_title_profile";

    //Componentes
    private CircleImageView image_profile;
    private TextView username;
    private RelativeLayout relativeProfile;

    //Firebase
    private DocumentReference userRef;
    private StorageReference storageReference;
    private UploadTask uploadTask;

    //Constructor
    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        //Componentes
        image_profile = view.findViewById(R.id.profile_image);
        username = view.findViewById(R.id.username);
        relativeProfile = view.findViewById(R.id.relativeProfile);

        //Firebase
        storageReference = FirebaseStorage.getInstance().getReference(Keys.KEY_STORAGE);
        FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore reference = FirebaseFirestore.getInstance();

        userRef = reference.collection(Keys.KEY_USERS).document(fuser.getUid());

        //Check DB si el usuario tiene un avatar
        userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                User user = documentSnapshot.toObject(User.class);
                username.setText(user.getUsername());
                if (getContext() != null) {
                    if (user.getImageURL().equals(getString(R.string.image_default))) {
                        image_profile.setImageResource(R.mipmap.ic_launcher);
                    } else {
                        Glide.with(Objects.requireNonNull(getContext()).getApplicationContext()).load(user.getImageURL()).into(image_profile);
                    }
                }
            }
        });

        //En caso de click en la imagen abrir para que seleccione una imagen
        image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImage();
            }
        });

        return view;
    }

    //Al abrir imagen
    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    //Guardar umagen en DB
    private void uploadImage(Uri uri){
        final ProgressDialog pd = new ProgressDialog(getContext());
        pd.setMessage(getString(R.string.image_uploading));
        pd.show();

        if (uri != null){
            final StorageReference fileReference = storageReference
                    .child(System.currentTimeMillis() + "." + getFileExtension(uri));

            uploadTask = fileReference.putFile(uri);

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>(){

                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }

                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();

                        userRef.update(Keys.KEY_USERS_IMAGEURL,mUri);

                        pd.dismiss();
                    }else {
                        Snackbar.make(relativeProfile,getString(R.string.error_image_db),Snackbar.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Snackbar.make(relativeProfile,e.getMessage(),Snackbar.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });

        }else {
            Snackbar.make(relativeProfile,getString(R.string.error_image_upload),Snackbar.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null){
            Uri imageUri = data.getData();

            if (uploadTask != null && uploadTask.isInProgress()){
                Snackbar.make(relativeProfile,getString(R.string.error_image_upload_progress),Snackbar.LENGTH_SHORT).show();
            }else {
                uploadImage(imageUri);
            }

        }
    }
}
