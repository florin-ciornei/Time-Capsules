package com.LightMediaApps.TimeCapsules;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.LightMediaApps.TimeCapsules.model.Capsule;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

public class CreateActivity extends AppCompatActivity {

    private final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 1;
    private int RESULT_LOAD_IMAGE = 1;
    private String selectedGifUrl = null;//the selected gif that will be put in the capsule
    private byte[] imageBytes = null;
    private LinearLayout gifsParent;
    private ImageView selectedGif, selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        selectedGif = (ImageView) findViewById(R.id.selectedGif);
        selectedImage = (ImageView) findViewById(R.id.imagePreview);
        gifsParent = (LinearLayout) findViewById(R.id.gifsParent);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void createCapsule(View v) {
        GregorianCalendar createdDate = new GregorianCalendar();
        GregorianCalendar openDate = new GregorianCalendar();
        try {
            openDate.set(
                    Integer.parseInt(((EditText) findViewById(R.id.year)).getText().toString()),
                    Integer.parseInt(((EditText) findViewById(R.id.month)).getText().toString())-1,
                    Integer.parseInt(((EditText) findViewById(R.id.day)).getText().toString())
            );
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Please enter a valid open date", Toast.LENGTH_LONG).show();
            return;
        }
        long createdTime = createdDate.getTimeInMillis();
        long openTime = openDate.getTimeInMillis();

        if (openTime < createdTime) {
            Toast.makeText(getApplicationContext(), "Open date should be in the future", Toast.LENGTH_LONG).show();
            return;
        }

        String description = ((EditText) findViewById(R.id.capsuleDescription)).getText().toString();
        if (description.length() == 0) {
            Toast.makeText(getApplicationContext(), "Please enter a capsule description", Toast.LENGTH_LONG).show();
            return;
        }

        Capsule capsule = new Capsule(description, createdTime, openTime);

        capsule.setInverseCreatedTime(-createdTime);

        capsule.setGifURL(selectedGifUrl);

        //set capsule content
        EditText capsuleText = (EditText) findViewById(R.id.capsuleText);
        if (capsuleText.getText().toString().length() > 0)
            capsule.setText(capsuleText.getText().toString());

        if (imageBytes == null && capsuleText.length() == 0 && selectedGifUrl == null) {
            Toast.makeText(getApplicationContext(), "Please some content to capsule (text, image or gif)", Toast.LENGTH_LONG).show();
            return;
        }


        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        String key = database.child("capsules").push().getKey();
        capsule.setId(key);
        capsule.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());

        if (imageBytes != null) {
            String imageName = "images/" + key + ".jpg";
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference imageRef = storageRef.child(imageName);
            UploadTask uploadTask = imageRef.putBytes(imageBytes);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getApplicationContext(), "Image upload error", Toast.LENGTH_LONG).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...
                }
            });
            capsule.setImageName(imageName);
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("/capsules/" + key, capsule);
        updates.put("/user-capsules/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/" + key, capsule);
        database.updateChildren(updates);

        Toast.makeText(getApplicationContext(), "Capsule added!", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(getBaseContext(), SearchActivity.class);
        startActivity(intent);
    }

    public void searchGifs(View v) {
        GetGifsAsync task = new GetGifsAsync();
        task.execute(
                "https://api.giphy.com/v1/gifs/search?api_key=pK31v1xz09ASdU6GM566V4Z3GZuanb4E&q=" +
                        ((EditText) findViewById(R.id.searchGif)).getText().toString()
                        + "&limit=10");
    }

    public void setGifs(String[] gifURLs) {
        gifsParent.removeAllViews();
        final CreateActivity self = this;
        for (final String url : gifURLs) {
            final ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, getResources().getDisplayMetrics()),
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    )
            );
            imageView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    selectedGifUrl = url;
                    selectedGif.setVisibility(View.VISIBLE);
                    Glide.with(self).load(url).into(selectedGif);
                    gifsParent.removeAllViews();
                    findViewById(R.id.removeGifButton).setVisibility(View.VISIBLE);
                }
            });
            Glide.with(this).load(url).into(imageView);
            gifsParent.addView(imageView);
        }
    }

    public void selectImage(View v) {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_STORAGE);
            return;
        }

        Intent i = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);

        findViewById(R.id.removeImageButton).setVisibility(View.VISIBLE);
    }

    public void removeImage(View view) {
        findViewById(R.id.removeImageButton).setVisibility(View.GONE);
        selectedImage.setVisibility(View.GONE);
        imageBytes = null;
    }

    public void removeGif(View view) {
        findViewById(R.id.removeGifButton).setVisibility(View.GONE);
        selectedGif.setVisibility(View.GONE);
        gifsParent.removeAllViews();
        selectedGifUrl = null;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectImage(null);
                } else {
                    Toast.makeText(this, "Gimme permission m8 !!!11", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    //for loading an image form the gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImageURI = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImageURI,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            selectedImage.setVisibility(View.VISIBLE);
            selectedImage.setImageBitmap(BitmapFactory.decodeFile(picturePath));

            //get the bytes from image that will be uploaded
            selectedImage.setDrawingCacheEnabled(true);
            selectedImage.buildDrawingCache();
            Bitmap bitmap = ((BitmapDrawable) selectedImage.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            imageBytes = baos.toByteArray();
        }
    }

    private String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        if (url == null)
            return jsonResponse;

        HttpURLConnection urlConnection = null;
        InputStream is = null;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.connect();
            if (urlConnection.getResponseCode() == 200) {
                is = urlConnection.getInputStream();
                jsonResponse = readFromStream(is);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
            if (is != null)
                is.close();
        }
        return jsonResponse;
    }

    private String readFromStream(InputStream is) throws IOException {
        StringBuilder output = new StringBuilder();
        if (is != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(is, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    private class GetGifsAsync extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            URL url = null;
            String jsonResponse = "";
            try {
                url = new URL(strings[0]);
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return jsonResponse;
        }

        @Override
        protected void onPostExecute(String s) {
            JSONObject root = null;
            try {
                root = new JSONObject(s);
                JSONArray gifs = root.getJSONArray("data");
                ArrayList<String> gifURLs = new ArrayList<String>();
                for (int i = 0; i < gifs.length(); i++) {
                    String gifURL = gifs.getJSONObject(i).getJSONObject("images").getJSONObject("fixed_height_small").getString("url");
                    gifURLs.add(gifURL);
                }
                setGifs(gifURLs.toArray(new String[0]));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
