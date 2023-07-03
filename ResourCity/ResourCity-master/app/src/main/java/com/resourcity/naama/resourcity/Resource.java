package com.resourcity.naama.resourcity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.graphics.Bitmap;
import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.util.ArrayList;


public class Resource implements Serializable//implements Serializable
{
    public enum ResourceTag {FOOD, HERBS, METAL, WOOD, PLASTIC, CLOTHES, FURNITURE}

    public String mId;

    public float mLat;

    public float mLng;

    public String mName;

    public ResourceTag mTagEnum;

    public String mOwnerEmail;

    // Setters and Add functions

    public ArrayList<String> mResourceNoteIdsList;

    //public ArrayList<Uri> mResourcePicturesUriList;
    public ArrayList<String> mResourcePicturesUrlList;

//    public static DatabaseReference resourceDatabase = FirebaseDatabase.getInstance().getReference("resources");;

//    public DatabaseReference resourceDatabase;

    public Resource(){
        mResourceNoteIdsList = new ArrayList<String>();
        mResourcePicturesUrlList = new ArrayList<String>();
//        resourceDatabase = FirebaseDatabase.getInstance().getReference("resources");
    }

    public Resource(String ownerEmail){
        mResourceNoteIdsList = new ArrayList<String>();
        mResourcePicturesUrlList = new ArrayList<String>();
//        resourceDatabase = FirebaseDatabase.getInstance().getReference("resources");

        mOwnerEmail = ownerEmail;
    } // Firebase needs a default constructor in order to work

    public Resource(String name, ResourceTag tagEnum, float lat, float lng, String owner)
    {
        // Create ID

        //set values
        mName = name;
        mLat = lat;
        mLng = lng;
        mTagEnum = tagEnum;

        // TODO: Operator = ??
        mOwnerEmail = owner;
        // init empty lists
        mResourceNoteIdsList = new ArrayList<String>(); // a list of Id's of related notes
        mResourcePicturesUrlList = new ArrayList<String>();
//        resourceDatabase = FirebaseDatabase.getInstance().getReference("resources");
    }


    //*******   Getters   *******//

    public String getId()
    {
        return mId;
    }

    public LatLng getLatLng()
    {
        LatLng latLng = new LatLng(mLat, mLng);
        return latLng;
    }

    public float getLat(){ return mLat; }

    public float getLng(){ return mLng; }

    public String getName()
    {
        return mName;
    }

    public ResourceTag getTagEnum(){ return mTagEnum; }

    public String getUserEmail() { return mOwnerEmail; }


    //public ArrayList<Uri> getResourcePicturesUrlList() {return mResourcePicturesUrlList; }
    public ArrayList<String> getResourcePicturesUrlList() {return mResourcePicturesUrlList; }

    public ArrayList<String> getResourceNoteIdsList() {return mResourceNoteIdsList; }

    //*******   Setters   *******//

    public void setNotesIdsList(ArrayList<String> resourceNotes)
    {
        mResourceNoteIdsList = resourceNotes;
    }
//    public void setResourceList(ArrayList<Uri> resourcePicturesUriList)
//    {
//        mResourcePicturesUrlList = resourcePicturesUriList;
//    }
    public void setResourceUrlList(ArrayList<String> resourcePicturesUrlList)
    {
        mResourcePicturesUrlList = resourcePicturesUrlList;
    }

    public void addNote(String resourceNotes)
    {
        mResourceNoteIdsList.add(resourceNotes);
    }
//    public void addResource(String resourcePicturesList)
//    {
//        mResourcePicturesList.add(resourcePicturesList);
//    }

    public void setId(String id)
    {
        mId = id;
    }

    public void setFilter(ResourceTag tag)
    {
        mTagEnum = tag;
    }

    //TODO: change lat lng type to double??
    public void setLatLng(float lat, float lng)
    {
        mLat = lat;
        mLng = lng;
    }

    public void setTitle(String title)
    {
        mName = title;
    }


//    public void addUrl(String uri)
//    {
//        mResourcePicturesUrlList.add(uri);
//    }
    public void addUrl(String url)
    {
        mResourcePicturesUrlList.add(url);
        AddResourceActivity.getResourceDatabase().child(mId).setValue(this);
    }
}
