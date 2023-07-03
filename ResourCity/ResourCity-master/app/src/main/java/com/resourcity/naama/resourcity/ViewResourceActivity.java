package com.resourcity.naama.resourcity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewParent;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import pub.devrel.easypermissions.EasyPermissions;

public class ViewResourceActivity extends AppCompatActivity {


    public final static int REQ_CODE_ADD_NOTE = 2;

    // STORAGE & FIREBASE
    private StorageReference mStorageReference;
    private DatabaseReference mNoteDatabase;

    // TITLE AND CATEGORY
    private static TextView mTitleResourceTextView;
    private static TextView mFilterResourceTextView;
    //content
    String mResourceTitle;
    String mResourceFilter;

    // NOTE
    private static TextView mNoteTitleTextView;
    private static TextView mNoteDescTextView;
    private static ImageView mNoteImageView;
    private static ImageView mNextNoteImgBtn;
    private static ImageView mPrevNoteImgBtn;
    int mCurNoteIdx;
    // content
    private ArrayList<String> mResourceNoteIdsList;

    // BUTTONS
    private static Button mSaveBtn;
    private static Button mDeleteBtn;

    // IMAGE
    private static ImageView mAddPictureBtn;
    private static ImageView mAddIdeaBtn;
    private static ImageView mNextPictureImgBtn;
    private static ImageView mPrevPictureImgBtn;
    private ImageView mResourceImage;
    int mCurImgIdx;
    //content
    private ArrayList<Bitmap> mResourceBitmapList;
    private ArrayList<String> mResourceImageUrlList;
    //private ResourceNote mChangedNote;
    ArrayList<ResourceNote> mResourceNotesList;

    Resource mResource;
    ResourceNote mCurNote;
    String mOperationUserEmail;

    private static int RESULT_LOAD_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_resource);

        // Firebase storage
        mStorageReference = FirebaseStorage.getInstance().getReference("resources");
        mNoteDatabase = FirebaseDatabase.getInstance().getReference("notes");

        // GET User and Resource Objects :To retrieve object in second Activity
        //Bundle b = getIntent().getExtras();
        mResource = (Resource) getIntent().getSerializableExtra("resourceData");
        mOperationUserEmail = (String) getIntent().getSerializableExtra("operationUserEmail");

        // RESOURCE TITLE
        mTitleResourceTextView = (TextView) findViewById(R.id.title_resource_TextView_v);
        mResourceTitle = mResource.getName();
        mTitleResourceTextView.setText(mResourceTitle);

        // RESOURCE FILTER CATEGORY
        mFilterResourceTextView = (TextView) findViewById(R.id.filterTextView_v);
        mResourceFilter = mResource.getTagEnum().toString();
        mFilterResourceTextView.setText("Category: " + mResourceFilter);

        // TODO: check if operating user is the same one who create this resource (more options..?)
        ManageImages();

        ManageNotes();

        // Save | Exit
        mSaveBtn = (Button) findViewById(R.id.SaveResourceBtn);
        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                Intent resultIntent = new Intent();
//                resultIntent.putExtra("resource_object", mResource);
//                setResult(, resultIntent);
                finish();
            }
        });

        mDeleteBtn = (Button) findViewById(R.id.DeleteResourceBtn_v);
        mDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { deleteResource(); }
        });
    }

    private void deleteResource()
    {
        for (String note : mResource.getResourceNoteIdsList())
        {
            DatabaseReference noteDBRef = AddResourceActivity.getNoteDatabase().child(note);
            noteDBRef.removeValue();
        }

        DatabaseReference resDBRef = AddResourceActivity.getResourceDatabase().child(mResource.getId());
        resDBRef.removeValue();
        finish();
    }

    private void ManageImages(){

        // Image and Note CONTENT!!
        mResourceBitmapList= new ArrayList<Bitmap>();
        mResourceImageUrlList = new ArrayList<String>();
        mResourceImageUrlList = mResource.getResourcePicturesUrlList();
        mCurImgIdx = mResourceImageUrlList.size() -1;

        // TODO: (TO CHECK) take mResourceImageUrlList values and assign the related bitmap to mResourceBitmapList!
        for (int i = 0; i < mResourceImageUrlList.size(); i++){
            Bitmap img = MapActivity.getImageBitmap(mResourceImageUrlList.get(i));
            mResourceBitmapList.add(img);
        }

        mResourceImage = (ImageView) findViewById(R.id.resource_image_view_v);
        if ( ! mResourceBitmapList.isEmpty()){
            // on create: assign first img (idx =0) to be viewed!
            Bitmap curBitmap = mResourceBitmapList.get(0);
            mResourceImage.setImageBitmap(curBitmap);
        }

        // ADD IMAGE
        mAddPictureBtn = (ImageView) findViewById(R.id.addResourceImgClck_v);
        mAddPictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                // TODO: MAKE SURE THE mResourceBitmapList and mResourceBitmapList both are updated!
                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        // NEXT IMAGE
        mNextPictureImgBtn = (ImageView) findViewById(R.id.nextImgBtn_v);
        mNextPictureImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurImgIdx == mResourceBitmapList.size()){
                    mCurImgIdx = 0;
                }
                if (mResourceBitmapList.size() != 0 && mResourceBitmapList.size() != 1) {
                    Bitmap curBitmap = mResourceBitmapList.get(mCurImgIdx);
                    mResourceImage.setImageBitmap(curBitmap);
                    mCurImgIdx+=1;
                }
            }
        });

        // PREV IMAGE
        mPrevPictureImgBtn = (ImageView) findViewById(R.id.prevImgBtn_v);
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

    private void ManageNotes(){
        mResourceNoteIdsList = new ArrayList<String>();
        mResourceNoteIdsList = mResource.getResourceNoteIdsList();

        mResourceNotesList = new ArrayList<ResourceNote>();
        for (int i = 0; i < mResourceNoteIdsList.size(); i++){
            mCurNote = getNoteById(mResourceNoteIdsList.get(i));
            if (mCurNote != null){
                mResourceNotesList.add(mCurNote);
            }
        }
        mCurNoteIdx = 0;

        // NOTE TITLE
        mNoteTitleTextView = (TextView) findViewById(R.id.note_title_text_view_v);
        // NOTE DESC
        mNoteDescTextView = (TextView) findViewById(R.id.note_desc_text_view_v);
        // NOTE IMG
        mNoteImageView =(ImageView) findViewById(R.id.note_image_view_v);
        // ADD NOTE BTN
        mAddIdeaBtn = (ImageView) findViewById(R.id.add_note_img_v);
        mAddIdeaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent resultIntent = new Intent(ViewResourceActivity.this, AddNoteActivity.class);
                // TODO: put extra: USER-OPERATING!!
                resultIntent.putExtra("add_note", REQ_CODE_ADD_NOTE);
                startActivityForResult(resultIntent, REQ_CODE_ADD_NOTE);
            }
        });

        // NEXT NOTE
        mNextNoteImgBtn = (ImageView) findViewById(R.id.nextNoteBtn_v);
        mNextNoteImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurNoteIdx == mResourceNoteIdsList.size()){
                    mCurNoteIdx = 0;
                }
                if (mResourceNoteIdsList.size() != 0 && mResourceNoteIdsList.size() != 1) {
//                    Bitmap curBitmap = mResourceNoteList.get(mCurNoteIdx).getImg();
//                    mResourceImage.setImageBitmap(curBitmap);
                    mCurNoteIdx += 1;
                }
                mCurNote = mResourceNotesList.get(mCurNoteIdx);
            }
        });

        // PREV NOTE
        mPrevNoteImgBtn = (ImageView) findViewById(R.id.prevNoteBtn_v);
        mPrevNoteImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( mCurNoteIdx < 0){
                    mCurNoteIdx = mResourceNoteIdsList.size()-1;
                }
                if (mResourceNoteIdsList.size() != 0 && mResourceNoteIdsList.size() != 1) {
//                    Bitmap curBitmap = mResourceNoteList.get(mCurNoteIdx).getImg();
//                    mResourceImage.setImageBitmap(curBitmap);
                    mCurNoteIdx -= 1;
                }
                mCurNote = mResourceNotesList.get(mCurNoteIdx);
            }
        });

        if (mResourceNotesList.size() > 0) {
            mCurNote = mResourceNotesList.get(mCurNoteIdx);
            UpdateNoteView();
        }
    }

    private void UpdateNoteView(){
        // CONTENT!!!
        mNoteTitleTextView.setText(mCurNote.getTitle());
        mNoteDescTextView.setText(mCurNote.getDescription());

        String noteUrl = mCurNote.getImgUrl();
        Bitmap noteImg = MapActivity.getImageBitmap(noteUrl);
        mNoteImageView.setImageBitmap(noteImg);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // ADD NOTE
        if(requestCode == REQ_CODE_ADD_NOTE ) {
            String noteId = (String) data.getExtras().getSerializable("note_id");

            mResource.addNote(noteId);
            ResourceNote curNote = getNoteById(noteId);
            mResourceNotesList.add(curNote);
            Toast.makeText(ViewResourceActivity.this, "Note successfully added", Toast.LENGTH_SHORT).show();
        }


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

//            mResourceImage = (ImageView) findViewById(R.id.image_view_va);
                Bitmap bitmapImg = BitmapFactory.decodeFile(picturePath);
                mResourceBitmapList.add(bitmapImg);
                //mResourceImageUrlList.add("");
                mResourceImage.setImageBitmap(bitmapImg);
                mCurImgIdx +=1;
            }
        }
    }

    private String getFileExtension(Uri uri)
    {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    private StorageTask uploadPhoto(Uri uri)
    {
        StorageTask uploadTask = null;

        Toast.makeText(ViewResourceActivity.this, "I'm in uploading photo", Toast.LENGTH_SHORT).show();
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
                        //mProgressBar.setProgress(0);
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
                Toast.makeText(ViewResourceActivity.this, "Failed to upload photo", Toast.LENGTH_LONG).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                //mProgressBar.setProgress((int) progress);
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

    private ResourceNote getNoteById(String noteId){

//        DatabaseReference noteDatabaseRef = mNoteDatabase.child(noteId);
//        noteDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot noteSnapshot) {
//                mChangedNote = noteSnapshot.getValue(ResourceNote.class);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//            }
//        });

        ArrayList<ResourceNote> allNotesallResources = MapActivity.mNoteList;
        for (ResourceNote note : allNotesallResources ){
            if (note.getId().equals(noteId)){
                return note;
            }
        }
        return null;
    }

    @Override
    public void onBackPressed() {
    }

//    private void setUpViewPager(){
//        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
//        //adapter.AddFragment(new CameraFragment());
//        adapter.AddFragment(new GalleryFragment());
//        ViewPager viewPager = (ViewPager) findViewById(R.id.add_picture_fragment_container_va)
//        viewPager.setAdapter(adapter);
//    }

//    private void initImageLoader(){
//        UniversalImageLoader universalImageLoader = new UniversalImageLoader(ViewResourceActivity.this);
//        ImageLoader.getInstance().init(universalImageLoader.getConfig());
//    }
//
//    TODO: see all options for imgURL !!!
//    "http://site.com/image.png" // from Web
//    "file:///mnt/sdcard/image.png" // from SD card
//    "file:///mnt/sdcard/video.mp4" // from SD card (video thumbnail)
//    "content://media/external/images/media/13" // from content provider
//    "content://media/external/video/media/13" // from content provider (video thumbnail)
//    "assets://image.png" // from assets
//    "drawable://" + R.drawable.img // from drawables (non-9patch images)

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
//    public void enableTxtViews(int isAdd){
//        EditText tiltleTxt = (ViewResourceActivity.this).findViewById(R.id.title_resource_TextView_v);
//        tiltleTxt.setEnabled(isAdd == 1);
//
//        EditText filterTxt = (ViewResourceActivity.this).findViewById(R.id.filterEditTxt_va);
//        filterTxt.setEnabled(isAdd == 1);
//
//        Button addResourceBtn = (ViewResourceActivity.this).findViewById(R.id.addResource_va);
//        addResourceBtn.setVisibility(isAdd);
//
//    }

//    protected void ShowAddPictureFragment(){
//        FrameLayout addPicture_frag_container = (ViewResourceActivity.this).findViewById(R.id.add_picture_fragment_container_va);
//        addPicture_frag_container.setVisibility(View.VISIBLE);
//
//        mFragmentTransaction.hide((GalleryFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.gallery_fragment));
//
//        mFragmentTransaction.hide((PhotoFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.photo_fragment));
//    }

//    protected void hideAddPictureFragment(){
//        FrameLayout addPicture_frag_container = (ViewResourceActivity.this).findViewById(R.id.add_picture_fragment_container_va);
//        addPicture_frag_container.setVisibility(View.INVISIBLE);
//
//        mFragmentTransaction.hide((GalleryFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.gallery_fragment));
//
//        mFragmentTransaction.hide((PhotoFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.photo_fragment));
//    }
}
