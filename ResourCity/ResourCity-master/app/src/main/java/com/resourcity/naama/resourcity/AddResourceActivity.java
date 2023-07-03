package com.resourcity.naama.resourcity;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import pub.devrel.easypermissions.EasyPermissions;

public class AddResourceActivity extends AppCompatActivity {

//    private static FragmentManager mFragmentManager;
//    private static FragmentTransaction mFragmentTransaction;

    private static int RESULT_LOAD_IMAGE = 1;
    public final static int REQ_CODE_ADD_NOTE = 2;

    private static ImageView mAddNoteImgClk;
    private static ImageView mAddPictureImgClk;
    private static Button mAddResourceBtn;
    private ImageView mResourceImage;

    private static Button mAddPictureBtn;
    private static ImageView mNextPictureImgBtn;
    private static ImageView mPrevPictureImgBtn;
    int mCurImgIdx;
    private ArrayList<Bitmap> mResourceBitmapList;

    private static EditText mTitleResourceTextView;
    private static Spinner mFiltersSpinner;
    private ArrayAdapter<CharSequence> mFiltersAdapter;

    // DATA INSERTED INTO
    private int RESULT_ADD_RESOURCE;
    private int RESULT_CANCEL_RESOURCE;
    private String mResourceTitle;
    private Resource.ResourceTag mSelectedFilter;
    private float mLat;
    private float mLng;
    private String mOpUserEmail;

    //private ArrayList<ResourceNote> mResourceNoteList;

    // STORAGE & FIREBASE
    private StorageReference mStorageReference;
    private ProgressBar mProgressBar;

    private static DatabaseReference resourceDatabase = FirebaseDatabase.getInstance().getReference("resources");
    private static DatabaseReference mNoteDatabase = FirebaseDatabase.getInstance().getReference("notes");
    Resource mResource;
    boolean saveButtonPushed;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

//        mNoteDatabase = FirebaseDatabase.getInstance().getReference("notes");
        //MapActivity.mNoteList = new ArrayList<ResourceNote>();

        setContentView(R.layout.activity_add_resource);

        String userEmail = (String) getIntent().getSerializableExtra("operatingUser");

        //saveResourceToDB(userEmail);
        saveResourceToDB();

        updateNotes();

//        mFragmentManager = getSupportFragmentManager();
//        mFragmentTransaction = mFragmentManager.beginTransaction();

        mStorageReference = FirebaseStorage.getInstance().getReference("resources");

        // TITLE TEXT VIEW
        mTitleResourceTextView = (EditText) findViewById(R.id.title_resource_txt_a);

        // FILTER SPINNER
        // TODO: TO CHECK: simple_gallery_item instead of simple_spinner_item
        mFiltersSpinner = (Spinner) findViewById(R.id.filterSpinner_a);
        mFiltersAdapter = ArrayAdapter.createFromResource(this, R.array.filterArray, android.R.layout.simple_spinner_item);
        mFiltersAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mFiltersSpinner.setAdapter(mFiltersAdapter);

        mProgressBar = findViewById(R.id.progress_bar_a);

        // ADD NOTE

        ManageNoteClick();

        ManagePictures();

        AddResourceClick();
    }

    private void ManageNoteClick(){
        mAddNoteImgClk = (ImageView) findViewById(R.id.add_note_img_a);
        mAddNoteImgClk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent resultIntent = new Intent(AddResourceActivity.this, AddNoteActivity.class);
                // TODO: put extra: USER-OPERATING!!
                resultIntent.putExtra("add_note", REQ_CODE_ADD_NOTE);
                startActivityForResult(resultIntent, REQ_CODE_ADD_NOTE);
            }
        });

        TextView txtAddNote= (TextView) findViewById(R.id.addNoteText_a);
        txtAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent resultIntent = new Intent(AddResourceActivity.this, AddNoteActivity.class);
                // TODO: put extra: USER-OPERATING!!
                resultIntent.putExtra("add_note", REQ_CODE_ADD_NOTE);
                startActivityForResult(resultIntent, REQ_CODE_ADD_NOTE);
            }
        });
    }

    private void ManagePictures() {
        // init
        mResourceBitmapList= new ArrayList<Bitmap>();
        //mResourceImageURIList = new ArrayList<Uri>();
        mCurImgIdx = 0;

        // ADD PICTURE TO RESOURCE
        mAddPictureImgClk = (ImageView) findViewById(R.id.addResourceImgClck_a);
        mAddPictureImgClk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });


        mResourceImage = (ImageView) findViewById(R.id.resource_image_view_a);

        // NEXT PICTURE
        mNextPictureImgBtn = (ImageView) findViewById(R.id.nextImgBtn_a);
        mNextPictureImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurImgIdx == mResourceBitmapList.size()){
                    mCurImgIdx = 0;
                }
                if (mResourceBitmapList.size() != 0 && mResourceBitmapList.size() != 1) {
                    Bitmap curBitmap = mResourceBitmapList.get(mCurImgIdx);
                    mResourceImage.setImageBitmap(curBitmap);
                    mCurImgIdx += 1;
                }
            }
        });

        // PREV PICTURE
        mPrevPictureImgBtn = (ImageView) findViewById(R.id.prevImgBtn_a);
        mPrevPictureImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( mCurImgIdx < 0){
                    mCurImgIdx = mResourceBitmapList.size()-1;
                }
                if (mResourceBitmapList.size() != 0 && mResourceBitmapList.size() != 1) {
                    Bitmap curBitmap = mResourceBitmapList.get(mCurImgIdx);
                    mResourceImage.setImageBitmap(curBitmap);
                    mCurImgIdx -= 1;
                }
            }
        });
    }

    private void AddResourceClick()
    {
        mAddResourceBtn = (Button) findViewById(R.id.addResourceBtn_a);
        mAddResourceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //return back to map activity

                String filterStr = mFiltersSpinner.getSelectedItem().toString();
                mResourceTitle = mTitleResourceTextView.getText().toString();

                if(filterStr.equals("SELECT CATEGORY")){
                    // TODO: to change for error
                    Toast.makeText(AddResourceActivity.this, "Filter MUST be chosen", Toast.LENGTH_SHORT).show();
                }
                if( mResourceTitle.isEmpty()){
                    // TODO: to change for error
                    Toast.makeText(AddResourceActivity.this, "Resource title MUST be not empty", Toast.LENGTH_SHORT).show();
                }
                else {
                    saveButtonPushed = true;
                    Bundle extras = getIntent().getExtras();
                    if (extras != null) {
                        RESULT_ADD_RESOURCE = extras.getInt("result_code");
                        mLat = (float) extras.getDouble("lat");
                        mLng = (float) extras.getDouble("lng");
                        mResource.setLatLng(mLat, mLng);
                        // TODO: take user???
                        //The key argument here must match that used in the other activity
                    }

                    Resource.ResourceTag filterItem = Resource.ResourceTag.valueOf(filterStr);
                    mResource.setTitle(mResourceTitle);
                    mResource.setFilter(filterItem);

                    //mResource.setResourceList(mResourceImageURIList);
//                    mResource.setNotesList(mResourceNoteList);

                    resourceDatabase.child(mResource.getId()).setValue(mResource);

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("resource_object", mResource);
                    setResult(RESULT_ADD_RESOURCE, resultIntent);

                    finish();
                }
            }
        });
    }

//    String userEmail
    private void saveResourceToDB()
    {
        saveButtonPushed = false;
//        resource = new Resource(userEmail);
        mResource = new Resource();
        String id = resourceDatabase.push().getKey();
        mResource.setId(id);
        resourceDatabase.child(id).setValue(mResource);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (!saveButtonPushed)
        {
            DatabaseReference resDBRef = resourceDatabase.child(mResource.getId());
            resDBRef.removeValue();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // ADD NOTE
        if(requestCode == REQ_CODE_ADD_NOTE ) {
            String noteId = (String) data.getExtras().getSerializable("add_note");

            mResource.addNote(noteId);
            //mResourceNoteList.add(noteToAdd);
            Toast.makeText(AddResourceActivity.this, "Note successfully added", Toast.LENGTH_SHORT).show();
            //
            updateNotes();

        }

        // CAMERA
        String[] galleryPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (! EasyPermissions.hasPermissions(this, galleryPermissions)) {
            EasyPermissions.requestPermissions(this, "Access for storage",
                    101, galleryPermissions);
        }
        if (EasyPermissions.hasPermissions(this, galleryPermissions)) {
            //pick Image From Gallery
            if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
                Uri selectedImage = data.getData();

                uploadPhoto(selectedImage);

                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();

                Bitmap bitmapImg = BitmapFactory.decodeFile(picturePath);
                mResourceBitmapList.add(bitmapImg);
                //mResourceImageURIList.add(selectedImage);
                mResourceImage.setImageBitmap(bitmapImg);
                mCurImgIdx +=1;
            }
        }
    }

//    private void initImageLoader(){
//        UniversalImageLoader universalImageLoader = new UniversalImageLoader(AddResourceActivity.this);
//        ImageLoader.getInstance().init(universalImageLoader.getConfig());
//    }
//
//    public void setImageToResource(String imgURL){
//        //last parameter is to append on the beginning..
//
//        String fileName = "IMG_20180629_185001.jpg";
//        fileName = fileName.replace(':', '/');
//        fileName = fileName.replace('/', '_');
//        //SanDisk SD card
//        imgURL = "file:///sdcard/DCIM/"+fileName;
//        //imgURL = "https://media-cdn.tripadvisor.com/media/photo-s/08/a5/95/9a/kastel-stafilic-old-olive.jpg";
//        UniversalImageLoader.setImage(imgURL, mResourceImage, null, "");
//    }

    private String getFileExtension(Uri uri)
    {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    private StorageTask uploadPhoto(Uri uri)
    {
        StorageTask uploadTask = null;

        Toast.makeText(AddResourceActivity.this, "I'm in uploading photo", Toast.LENGTH_SHORT).show();
        final String[] urlStringL = new String[1];
        final StorageReference fileRef = mStorageReference.child(System.currentTimeMillis() + "." + getFileExtension(uri));

        fileRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
            {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setProgress(0);
                    }
                }, 500);

//                StorageMetadata smd = taskSnapshot.getMetadata();
//                StorageReference sr = smd.getReference();
//                Task<Uri> tu = sr.getDownloadUrl();
//                String imageUrl = tu.getResult().toString();
//                resource.addUrl(imageUrl);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                urlStringL[0] = "";
                Toast.makeText(AddResourceActivity.this, "Failed to upload photo", Toast.LENGTH_LONG).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                mProgressBar.setProgress((int) progress);
            }
        }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
            {
//                String imageUrl = task.getResult().toString(); //.getMetadata().getReference().getDownloadUrl().getResult().toString();
//                mResource.addUrl(imageUrl);

                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Uri downloadUrl = uri;
                        String imageUrl = downloadUrl.toString();
                        mResource.addUrl(imageUrl);
                    }});
            }
        });
        return uploadTask;
    }

    private void updateNotes()
    {
        mNoteDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                MapActivity.mNoteList.clear();
                for (DataSnapshot noteSnapshot : dataSnapshot.getChildren())
                {
                    ResourceNote note = noteSnapshot.getValue(ResourceNote.class);
                    MapActivity.mNoteList.add(note);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}

//            @Override
//            public void onChildChanged(){
//            }
        });
    }

    public static DatabaseReference getResourceDatabase()
    {
        return resourceDatabase;
    }
    public static DatabaseReference getNoteDatabase()
    {
        return mNoteDatabase;
    }
}