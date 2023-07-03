package com.resourcity.naama.resourcity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.plus.model.people.Person;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ResourceNoteFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ResourceNoteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ResourceNoteFragment extends Fragment {


    private ImageView mNoteImageView;

    private TextView mDesctionTextView;
    private EditText mNoteTitleTextView;
    private ImageView mAddNote;
    private TextView mLikesNumTxt;
    private TextView mDislikesNumTxt;

    //TODO to activate!
    private ImageView userProfileImg;
    private TextView userName;
    private Bitmap userProfileBitMap;

    // extends Fragment
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_resource_note, container, false);

        mNoteImageView = (ImageView) v.findViewById(R.id.note_image_view);
        mAddNote = (ImageView) v.findViewById(R.id.add_note_img);
        mNoteTitleTextView = (EditText) v.findViewById(R.id.note_title_text_view);
        mDesctionTextView = (TextView) v.findViewById(R.id.note_desc_text_view);
//        mLikesNumTxt = (TextView) v.findViewById(R.id.likes_number_txt);
//        mLikesNumTxt = (TextView) v.findViewById(R.id.dislikes_number_txt);

        // TODO how to?
        //mNoteBitmap = (Bitmap) mNoteImageView.getBitmap;

        mAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MapActivity mapActivity = (MapActivity) getActivity();
                mapActivity.hidePreviewFragment();
                Intent intent = new Intent(mapActivity, ViewResourceActivity.class);
                intent.putExtra("isAdd", 0);
                startActivity(intent);
            }
        });

        return v;
    }
}
