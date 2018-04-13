package it.polito.mad.lab1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_CODE = 1;
    static final int RC_SIGN_IN = 123; //costant per sign-in authentication
    private EditText et1;
    private EditText et2;
    private EditText et3;
    private EditText phone_num;
    private ImageView imageView;
    private Uri image_uri;
    private Toolbar toolbar;
    private boolean imageAlreadySet;

    public static final String PREFS0 = "MyImage";
    public static final String PREFS1 = "MyName";
    public static final String PREFS2 = "MyEmail";
    public static final String PREFS3 = "MyPhone";
    public static final String PREFS4 = "MyBio";


    private String name;
    private String mail;
    private String bio;
    private String Phone;


    //FIREBASE database
    private FirebaseDatabase mFirebaseDatabase; //reference to the WHOLE database
    private DatabaseReference mMessagesDatabaseReference; //reference SPECIFIC part of database
    private ChildEventListener mChildEventListener;

    //FIREBASE authentication
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener; //listener when change state authentication (log-in /log-off)

    //FIREBASE storage (to store users' images)
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mChatPhotosStorageReference; //reference to the folder of photos on Firebase storage



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // initialize firebase variables
        mFirebaseDatabase = FirebaseDatabase.getInstance(); //keep reference to database
        mFirebaseAuth = FirebaseAuth.getInstance(); //keep reference to FirebaseAuth
        mFirebaseStorage = FirebaseStorage.getInstance(); //keep reference to Firebase storage

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user!=null){
                    //user is signed in
                    //Toast.makeText(MainActivity.this, "You're now signed in. Welcome to FriendlyChat.", Toast.LENGTH_SHORT).show();
                    onSignedInInitialize(savedInstanceState);
                }else{
                    //user is signed out
                    onSignedOutCleanup();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.EmailBuilder().build(),
                                            new AuthUI.IdpConfig.GoogleBuilder().build())
                                    ).build(),
                            RC_SIGN_IN);
                }
            }
        };

    }


    private void onSignedInInitialize(Bundle savedInstanceState){
        setContentView(R.layout.activity_main);

        imageAlreadySet=false;
        et1 = findViewById(R.id.et1);
        et1.setKeyListener(null); //because android:editable is DEPRECATED
        et2 = findViewById(R.id.et2);
        et2.setKeyListener(null);
        et3 = findViewById(R.id.et3);
        et3.setKeyListener(null);
        phone_num = findViewById(R.id.phone_num);
        phone_num.setKeyListener(null);
        imageView = findViewById(R.id.imageView);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher_round);

        //to restore image when you rotate phone
        if (savedInstanceState != null) {
            if (savedInstanceState.get("image") != null) {
                String uri = savedInstanceState.getString("image");
                image_uri = Uri.parse(uri);
                if(image_uri!=null){
                    imageView.setImageURI(image_uri);
                    imageAlreadySet=true;
                }
            }
        }

        //Load preferences from the storage when application is restarted
        SharedPreferences sp0= getSharedPreferences(PREFS0, Context.MODE_PRIVATE);
        String myUri=sp0.getString("d_image", "null"); //Get the reference Uri

        SharedPreferences sp1= getSharedPreferences(PREFS1, Context.MODE_PRIVATE);
        name=sp1.getString("d_name", "");

        SharedPreferences sp2= getSharedPreferences(PREFS2, Context.MODE_PRIVATE);
        mail=sp2.getString("d_mail", "");

        SharedPreferences sp3= getSharedPreferences(PREFS3, Context.MODE_PRIVATE);
        Phone=sp3.getString("d_phone", "");

        SharedPreferences sp4= getSharedPreferences(PREFS4, Context.MODE_PRIVATE);
        bio=sp4.getString("d_bio", "");

        et1.setText(name);
        et2.setText(mail);
        et3.setText(bio);
        phone_num.setText(Phone);

        if(!imageAlreadySet) {
            if (myUri != null && myUri.compareTo("null") != 0) {
                image_uri = Uri.parse(myUri);
                imageView.setImageURI(image_uri);
            } else {
                int drawableResource = R.drawable.ic_account;
                Drawable d = getResources().getDrawable(drawableResource);
                imageView.setImageDrawable(d);
            }
        }
    }


    /**
     * needed to attach the edit_menu.xml to the activity_main.xml
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        return true;
    }

    /**
    * to manage event when edit_button is pressed
    */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.edit_button) {
            Intent i = new Intent(this, EditModeActivity.class);
            startActivityForResult(i, REQUEST_CODE); //in this way you can interact with another activity
        }else if(item.getItemId() == R.id.sign_out_menu){
            AuthUI.getInstance().signOut(this); //sign-out
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


  /**
  * needed to receive back data from the editModeActivity
  */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                Bundle ris = data.getExtras();
                name = ris.get("name").toString();
                mail = ris.get("mail").toString();
                bio = ris.get("bio").toString();
                Phone = ris.get("phone").toString();
                image_uri = (Uri) ris.get("image");

                et1.setText(name);
                et2.setText(mail);
                et3.setText(bio);
                phone_num.setText(Phone);

                // Sets the ImageView with the Image URI
                if(image_uri==null){
                    SharedPreferences sp0= getSharedPreferences(PREFS0, Context.MODE_PRIVATE);
                    String myUri=sp0.getString("d_image", "null");

                    if (myUri != null && myUri.compareTo("null") != 0) {
                        image_uri = Uri.parse(myUri);
                        imageView.setImageURI(image_uri);
                    } else {
                        int drawableResource = R.drawable.ic_account;
                        Drawable d = getResources().getDrawable(drawableResource);
                        imageView.setImageDrawable(d);
                    }
                }
                else{
                    imageView.setImageURI(image_uri);
                }

                imageView.invalidate();

                //Save data on storage when edit activity pass data
                //to main activity (Confirm button pressed)
                SharedPreferences sp0= getSharedPreferences(PREFS0, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor0=sp0.edit();
                editor0.putString("d_image", String.valueOf(image_uri));
                editor0.commit();

                SharedPreferences sp1=getSharedPreferences(PREFS1, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor1=sp1.edit();
                editor1.putString("d_name", name);
                editor1.commit();

                SharedPreferences sp2=getSharedPreferences(PREFS2, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor2=sp2.edit();
                editor2.putString("d_mail", mail);
                editor2.commit();

                SharedPreferences sp3=getSharedPreferences(PREFS3, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor3=sp3.edit();
                editor3.putString("d_phone", Phone);
                editor3.commit();

                SharedPreferences sp4=getSharedPreferences(PREFS4, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor4=sp4.edit();
                editor4.putString("d_bio", bio);
                editor4.commit();

            } else {
                //the user has no grant the permission to write on storage
                finish();
            }
        }
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




    private void onSignedOutCleanup(){
        //mUsername = ANONYMOUS;
        //mMessageAdapter.clear(); //clear messages from the screen
        //detachDatabaseReadListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        //detachDatabaseReadListener();
        //mMessageAdapter.clear();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

}