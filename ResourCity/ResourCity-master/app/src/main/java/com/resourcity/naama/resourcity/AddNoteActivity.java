package com.resourcity.naama.resourcity;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import pub.devrel.easypermissions.EasyPermissions;

public class AddNoteActivity extends AppCompatActivity {

    private static final int RESULT_LOAD_IMAGE = 1;
    private int RESULT_ADD_NOTE;

    private ProgressBar mProgressBar;

    private String mUserEmail;

    private String mNoteTitle;
    private String mNoteDesc;
    private Bitmap mNoteBitmap;
    private String mNoteUrl;

    private ResourceNote mNote;

    private static Button mAddNoteBtn;
    private static EditText mNoteTitleEditText;
    private static EditText mNoteDescEditText;
    private static ImageView mNoteImageView;

    private boolean mSaveButtonPushed;
    private DatabaseReference mNoteDatabase;
    private StorageReference mStorageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        mProgressBar = findViewById(R.id.progress_bar_n);

        mStorageReference = FirebaseStorage.getInstance().getReference("resources");

        // get user operating!
        mUserEmail = (String) getIntent().getSerializableExtra("operatingUser");

        mNoteDatabase = FirebaseDatabase.getInstance().getReference("notes");
        saveNoteToDB();

        mNoteTitleEditText = (EditText) findViewById(R.id.note_title_txt_n);
        mNoteDescEditText = (EditText) findViewById(R.id.note_desc_txt_n);

        mNoteImageView = (ImageView) findViewById(R.id.note_image_view_n);
        mNoteImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        mAddNoteBtn = (Button) findViewById(R.id.AddNoteBtn_n);
        mAddNoteBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               // TODO: return back from this activity (to Add resource of view resource)
               // take all content (title, desc, img)

               mNoteTitle = mNoteTitleEditText.getText().toString();
               if(mNoteTitle.isEmpty()){
                   // TODO: to change for error
                   Toast.makeText(AddNoteActivity.this, "Note title MUST be not empty", Toast.LENGTH_SHORT).show();
               }
               mNoteDesc = mNoteDescEditText.getText().toString();
               if( mNoteDesc.isEmpty()){
                   // TODO: to change for error
                   Toast.makeText(AddNoteActivity.this, "Note description MUST be not empty", Toast.LENGTH_SHORT).show();
               }

               else {
                   mSaveButtonPushed = true;

                   Bundle extras = getIntent().getExtras();
                   if (extras != null) {
                       RESULT_ADD_NOTE = extras.getInt("add_note");
                   }

                       mNote.setUserWroteNote(mUserEmail);
                       mNote.setImgUrl(mNoteUrl);
                       mNote.setTitle(mNoteTitle);
                       mNote.setDescription(mNoteDesc);

                       String noteID = mNote.getId();
                       mNoteDatabase.child(noteID).setValue(mNote);

                       Intent resultIntent = new Intent();
                       resultIntent.putExtra("add_note", mNote.getId());
                       setResult(RESULT_ADD_NOTE, resultIntent);

                   finish();
               }
           }
       });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String[] galleryPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (! EasyPermissions.hasPermissions(this, galleryPermissions)) {
            EasyPermissions.requestPermissions(this, "Access for storage",
                    101, galleryPermissions);
        }
        if (EasyPermissions.hasPermissions(this, galleryPermissions)) {
            //pick Image From Gallery
            if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
                Uri selectedImage = data.getData();

                // to upload to server only when returning back from NoteActivity to AddResourceActivity.
                uploadPhoto(selectedImage);

                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();

                mNoteBitmap = BitmapFactory.decodeFile(picturePath);
                mNoteImageView.setImageBitmap(mNoteBitmap);
            }
        }
    }

    private void saveNoteToDB()
    {
        mSaveButtonPushed = false;
        mNote = new ResourceNote();
        String id = mNoteDatabase.push().getKey();
        mNote.setId(id);
        mNoteDatabase.child(id).setValue(mNote);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (!mSaveButtonPushed)
        {
            DatabaseReference resDBRef = mNoteDatabase.child(mNote.getId());
            resDBRef.removeValue();
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

        Toast.makeText(AddNoteActivity.this, "I'm in uploading photo", Toast.LENGTH_SHORT).show();
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

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                urlStringL[0] = "";
                Toast.makeText(AddNoteActivity.this, "Failed to upload photo", Toast.LENGTH_LONG).show();
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


                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Uri downloadUrl = uri;
                        String imageUrl = downloadUrl.toString();
                        mNoteUrl = imageUrl;
                    }});
            }
        });
        return uploadTask;
    }
}
