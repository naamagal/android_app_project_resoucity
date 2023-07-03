package com.resourcity.naama.resourcity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ResourcePreviewFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ResourcePreviewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ResourcePreviewFragment extends Fragment {

    private static final String TAG = "ResourcePreviewFragment";

    ImageView mImagePreview;
    TextView mSeeResourceFullDetails;
    ImageView mClosePreviewimg;

    private Resource mResource;

//    private static final String RESOURCE_KEY = "resource_key";

//    public static ResourcePreviewFragment newInstance(Resource resource) {
//        ResourcePreviewFragment fragment = new ResourcePreviewFragment();
//        Bundle bundle = new Bundle();
//        bundle.putSerializable(RESOURCE_KEY, resource);
//        fragment.setArguments(bundle);
//
//        return fragment;
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_resource_preview, container, false);

        //mResource = (Resource) getArguments().getSerializable(RESOURCE_KEY);

        mImagePreview = (ImageView) v.findViewById(R.id.imagePreview);
        mSeeResourceFullDetails = (TextView) v.findViewById(R.id.resource_full_view_link);
        mClosePreviewimg = (ImageView) v.findViewById(R.id.closeImagePreview);

        mSeeResourceFullDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MapActivity mapActivity = (MapActivity) getActivity();
                mapActivity.hidePreviewFragment();
                Intent intent = new Intent(mapActivity, ViewResourceActivity.class);
                intent.putExtra("resources", mResource);
                intent.putExtra("operationUserEmail", "");
                // TODO: pass all data of resource

                startActivity(intent);
            }
        });

        mClosePreviewimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MapActivity mapActivity = (MapActivity) getActivity();
                mapActivity.hidePreviewFragment();

                Intent intentChild = new Intent(mapActivity, ViewResourceActivity.class);
//                intentChild.putExtra("result_code", REQ_CODE_ADD_RESOURCE_CHILD);
//                intentChild.putExtra("cancel", RESULT_CANCEL_RESOURCE);
//                intentChild.putExtra("lat", mResource.getLatLng().latitude);
//                intentChild.putExtra("lng", mResource.getLatLng().longitude);
//                intentChild.putExtra("operatingUser", mResource.getUserEmail());
//                startActivityForResult(intentChild, REQ_CODE_ADD_RESOURCE_CHILD);
            }
        });

        return v;
    }

    public ResourcePreviewFragment() {
        // Required empty public constructor
    }

    public void setResource(Resource setResource)
    {
        mResource = setResource;
    }
}

