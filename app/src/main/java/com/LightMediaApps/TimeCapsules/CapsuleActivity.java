package com.LightMediaApps.TimeCapsules;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.LightMediaApps.TimeCapsules.model.Capsule;
import com.LightMediaApps.TimeCapsules.model.User;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

public class CapsuleActivity extends AppCompatActivity {
    private Capsule capsule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capsule);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        capsule = (Capsule) getIntent().getSerializableExtra("capsule");
        ImageView contentGif = (ImageView) findViewById(R.id.contentGif);
        final ImageView contentImage = (ImageView) findViewById(R.id.contentImage);
        TextView contentText = (TextView) findViewById(R.id.contentText);

        ((TextView) findViewById(R.id.capsuleOpenDate)).setText(capsule.getOpenDateFormatted());
        ((TextView) findViewById(R.id.capsuleDescription)).setText(capsule.getDescription());

        if (!capsule.isOpened()) {
            ((TextView) findViewById(R.id.willOpenOnText)).setText("Will open on");
        } else {
            ((TextView) findViewById(R.id.willOpenOnText)).setText("Opened on");
            findViewById(R.id.contentTitle).setVisibility(View.VISIBLE);

            //load capsule content
            if (capsule.getDescription().length() > 0) {
                contentText.setText(capsule.getText());
                contentText.setVisibility(View.VISIBLE);
            }
            if (capsule.getGifURL().length() != 0) {
                contentGif.setVisibility(View.VISIBLE);
                Glide.with(this).load(capsule.getGifURL()).into(contentGif);
            }

            final CapsuleActivity self = this;
            if (capsule.getImageName().length() != 0) {
                contentImage.setVisibility(View.VISIBLE);
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();
                StorageReference imageRef = storageRef.child(capsule.getImageName());
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(self).load(uri.toString()).into(contentImage);
                    }
                });
            } else {
                contentImage.setVisibility(View.GONE);
            }
        }


        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        final CapsuleActivity self = this;

        mDatabase.child("users").child(capsule.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                ((TextView) findViewById(R.id.capsuleUsername)).setText(user.getUsername());
                final ImageView profileImage = (ImageView) findViewById(R.id.capsuleProfileImage);
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();
                StorageReference imageRef = storageRef.child(user.getImage());
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(self).load(uri.toString()).into(profileImage);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        findViewById(R.id.deleteCapsule).setVisibility(capsule.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) ?
                View.VISIBLE : View.GONE);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void deleteCapsule(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.myDialog));
        builder.setCancelable(true);
        builder.setTitle("Delete");
        builder.setMessage("Do you want to delete your capsule?");
        builder.setPositiveButton("YES",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("CAPSULE", capsule.getId());
                        FirebaseDatabase.getInstance().getReference()
                                .child("capsules").child(capsule.getId()).removeValue();
                        FirebaseDatabase.getInstance().getReference()
                                .child("user-capsules").child(capsule.getUserId()).child(capsule.getId()).removeValue();
                        Intent intent = new Intent(getBaseContext(), SearchActivity.class);
                        startActivity(intent);
                    }
                });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
