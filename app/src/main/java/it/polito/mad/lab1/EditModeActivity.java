package it.polito.mad.lab1;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;


import java.io.ByteArrayOutputStream;

public class EditModeActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 3;


    public static final String PREFS0 = "MyImage";
    public static final String PREFS1 = "MyName";
    public static final String PREFS2 = "MyEmail";
    public static final String PREFS3 = "MyPhone";
    public static final String PREFS4 = "MyBio";


    private Button b, clear_all;
    private EditText et1, et3, phone_num;
    private MultiAutoCompleteTextView et2;
    private ImageView img;
    private Uri image_uri;
    private Bitmap imageBitmap;
    private Toolbar toolbar;
    private boolean imageAlreadySet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_mode);

        //to grant permission to write on storage (it's needed only from Android M + )
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        }


        imageAlreadySet = false;
        b = findViewById(R.id.b1);
        et1 = findViewById(R.id.et1);
        et2 = findViewById(R.id.et2);
        et3 = findViewById(R.id.et3);
        phone_num = findViewById(R.id.phone_num);
        img = findViewById(R.id.imageView);
        toolbar = findViewById(R.id.toolbar);
        clear_all = findViewById(R.id.b2);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //to restore image when you rotate phone
        if (savedInstanceState != null) {
            if (savedInstanceState.get("image") != null) {
                String uri = savedInstanceState.getString("image");
                image_uri = Uri.parse(uri);
                if(image_uri!=null){
                    img.setImageURI(image_uri);
                    imageAlreadySet=true;
                }
            }
        }

        if(getSharedPreferences(PREFS0, Context.MODE_PRIVATE)!=null){
            SharedPreferences sp0= getSharedPreferences(PREFS0, Context.MODE_PRIVATE);
            String myUri=sp0.getString("d_image", "null");

            if(!imageAlreadySet) {
                if (myUri != null && myUri.compareTo("null") != 0) {
                    image_uri = Uri.parse((myUri));
                    img.setImageURI(image_uri);
                } else {
                    int drawableResource = R.drawable.ic_account;
                    Drawable d = getResources().getDrawable(drawableResource);
                    img.setImageDrawable(d);
                }
            }
        }

        if(getSharedPreferences(PREFS1, Context.MODE_PRIVATE)!=null){
            SharedPreferences sp1= getSharedPreferences(PREFS1, Context.MODE_PRIVATE);
            String name=sp1.getString("d_name", "");
            et1.setText(name);
        }

        if(getSharedPreferences(PREFS2, Context.MODE_PRIVATE)!=null){
            SharedPreferences sp2= getSharedPreferences(PREFS2, Context.MODE_PRIVATE);
            String mail=sp2.getString("d_mail", "");
            et2.setText(mail);
        }

        if(getSharedPreferences(PREFS3, Context.MODE_PRIVATE)!=null){
            SharedPreferences sp3= getSharedPreferences(PREFS3, Context.MODE_PRIVATE);
            String Phone=sp3.getString("d_phone", "");
            phone_num.setText(Phone);
        }

        if(getSharedPreferences(PREFS4, Context.MODE_PRIVATE)!=null){
            SharedPreferences sp4= getSharedPreferences(PREFS4, Context.MODE_PRIVATE);
            String bio=sp4.getString("d_bio", "");
            et3.setText(bio);
        }

        //***
        //This part offers some Suffixes related to the Email service field such as "gmail.com" ...
        // We implemented "ArrayAdapter" , "MultiAutoCompleteTextView" with "Tokenizer()" to achive this goal.
        //***
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, MAILS);
        et2.setAdapter(adapter);
        et2.setThreshold(1);
        et2.setTokenizer(new MultiAutoCompleteTextView.Tokenizer() {

            @Override
            public int findTokenStart(CharSequence text, int cursor) {
                int i = cursor;
                int len = text.length();

                while (i > 0) {   // EX: abcdefgh@  ==> return 8 ==> 8 number of characters - '@'
                    if (text.charAt(i - 1) == '@')
                        return i - 1;
                    else {
                        i--;
                    }
                }

                for(int j = len; j > 0; j--){
                    if (j > 2 && text.charAt(j - 2) == '@') {
                            return j-2;   //return (j-2) number of characters
                    }
                    else
                        return 0;
                }
                return 0;
            }

            @Override
            public int findTokenEnd(CharSequence text, int cursor) {
                return 0;
            }

            @Override
            public CharSequence terminateToken(CharSequence text) {
                return text;
            }
        });

        //Monitor the number of input characters in "Short bio" field to be less than 50.
        et3.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = et3.getText().toString();
                int characters = text.length();
                if (characters > 49) {
                    et3.setError("Please enter less character");
                } else if (characters < 49) {
                    et3.setError(null);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        //set a listener to react to event of pressing confirm_button
        b.setOnClickListener(v -> {
            String name = et1.getText().toString();
            String mail = et2.getText().toString();
            String bio = et3.getText().toString();
            String phone = phone_num.getText().toString();

            Intent ris = new Intent(this, MainActivity.class);
            ris.putExtra("name", name);
            ris.putExtra("mail", mail);
            ris.putExtra("bio", bio);
            ris.putExtra("phone", phone);
            ris.putExtra("image", image_uri);

            Context context = getApplicationContext();
            CharSequence text = "Data saved";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();

            setResult(Activity.RESULT_OK, ris);
            finish();
        });

        //set a listener to react to event of pressing clear_all button
        clear_all.setOnClickListener( v -> {
            et1.setText("");
            et2.setText("");
            et3.setText("");
            phone_num.setText("");
        });
    }

    //**
    // String array used to give some suffixes options for Email address field
    //**
    private static final String[] MAILS = new String[]{
            "@gmail.com", "@yahoo.com", "@studenti.polito.it", "@polito.it", "@mac.hush.com", "@hush.com"
    };

    /**
     * needed to open the Gallery
     */
    public void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    /**
    * needed to attach the photo_menu.xml to the activity_main.xml
    */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.photo_menu, menu);
        return true;
    }

    /**
    * needed to manage event when photo_button is pressed
    */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.edit_photo) {
            openCamera();
        }else if (id==R.id.open_gallery){
            openGallery();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Intent ris = new Intent(this, MainActivity.class);
                setResult(Activity.RESULT_CANCELED, ris);
                finish(); //the activity kill him self (its work is terminated)
            }
        }
    }

    /**
    * needed to start the camera
    */
    public void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }


    /**
    * needed to receive back data from the Gallery and from the Camera
    */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) { //data from gallery
            image_uri = data.getData();
            img.setImageURI(image_uri);
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) { //data from camera
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            image_uri = getImageUri(this, imageBitmap);
            img.setImageURI(image_uri);
        }
    }

    /**
    * retrieve the URI from a bitmap
    */
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    /**
    * needed to save url of image when you rotate the phone
    */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (image_uri != null) {
            outState.putString("image", image_uri.toString());
        }
    }

    /**
    * when back button is pressed, return to main Activity
    */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    /**
    * Add action when you press back button in the toolbar
    */
    @Override
    public boolean onSupportNavigateUp() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        return true;
    }
}