package com.resourcity.naama.resourcity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ResourcePictureFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ResourcePictureFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ResourcePictureFragment extends Fragment {

//    private static ImageView mAddPictureImgClk;
//    private static ImageView mNextPictureImgBtn;
//    private static ImageView mPrevPictureImgBtn;
//    private static int RESULT_LOAD_IMAGE = 1;
//    private ImageView mResourceImage;
//    int mCurImgIdx;
//    private ArrayList<Bitmap> mResourceBitmapList;
//    private ArrayList<String> mResourceImageURLList;

    public ResourcePictureFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            ManagePictures();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_resource_picture, container, false);

    }

//    private void ManagePictures(){
//        // init
//        mResourceBitmapList= new ArrayList<Bitmap>();
//        mCurImgIdx = 0;
//
//        // ADD PICTURE TO RESOURCE
//        mAddPictureImgClk = (ImageView) getActivity().findViewById(R.id.addResourceImgClck);
//        mAddPictureImgClk.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent i = new Intent(
//                        Intent.ACTION_PICK,
//                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//
//                startActivityForResult(i, RESULT_LOAD_IMAGE);
//            }
//        });
//
//
//        mResourceImage = (ImageView) getActivity().findViewById(R.id.image_view);
//
//        // NEXT PICTURE
//        mNextPictureImgBtn = (ImageView) getActivity().findViewById(R.id.nextImgBtn);
//        mNextPictureImgBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (mCurImgIdx == mResourceBitmapList.size()){
//                    mCurImgIdx = 0;
//                }
//                Bitmap curBitmap = mResourceBitmapList.get(mCurImgIdx);
//                mResourceImage.setImageBitmap(curBitmap);
//                mCurImgIdx+=1;
//            }
//        });
//
//        // PREV PICTURE
//        mPrevPictureImgBtn = (ImageView) getActivity().findViewById(R.id.prevImgBtn);
//        mPrevPictureImgBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if ( mCurImgIdx < 0){
//                    mCurImgIdx = mResourceBitmapList.size()-1;
//                }
//                Bitmap curBitmap = mResourceBitmapList.get(mCurImgIdx);
//                mResourceImage.setImageBitmap(curBitmap);
//                mCurImgIdx-=1;
//            }
//        });
//    }
}
