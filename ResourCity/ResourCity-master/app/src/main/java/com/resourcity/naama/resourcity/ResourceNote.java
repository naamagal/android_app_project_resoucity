package com.resourcity.naama.resourcity;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;

// CLASS CONTAINS DATA OF NOTES
public class ResourceNote implements Serializable {

    // TODO: to activate!
    // private Bitmap mNoteBitmap;

    private String mId;
    private String mEmailUserWroteNote;
    private String mNoteUrl;
    private String mDescription;
    private String mNoteTitle;

    // list of user email who liked the note..
    private ArrayList<String> mLikeList;
    private ArrayList<String> mDislikeList;

    public ResourceNote()
    {
        this.mLikeList = new ArrayList<String>();
        this.mDislikeList = new ArrayList<String>();
    }

//    public ResourceNote(String emailUserWroteNote, Bitmap notePicture, String noteTitle, String noteDescription) {
//        this.mEmailUserWroteNote = emailUserWroteNote;
//        //this.mNoteBitmap = notePicture;
//        this.mDescription = noteDescription;
//        this.mNoteTitle = noteTitle;
//
//        this.mLikeList = new ArrayList<String>();
//        this.mDislikeList = new ArrayList<String>();
//    }

    //*******   Getters   *******//
    // TODO: ADD ALL GETTERS AND SETTERS? IS IT NECSSARY AT ALL??

    // GETTERS

    public String getId()
    {
        return mId;
    }

    public String getUserWroteNote() {
        return this.mEmailUserWroteNote;
    }

    public String getTitle() {
        return this.mNoteTitle;
    }

    public String getDescription() {
        return this.mDescription;
    }

    public String getImgUrl() {
        return this.mNoteUrl;
    }


    // SETTERS

    public void setId(String id)
    {
        mId = id;
    }

    public void setUserWroteNote(String emailUserWroteNote ) {
        this.mEmailUserWroteNote = emailUserWroteNote;
    }

    public void setTitle(String noteTitle) {

        this.mNoteTitle = noteTitle;
    }

    public void setDescription(String desc) {

        this.mDescription = desc;
    }

    public void setImgUrl(String noteUrl) {

        this.mNoteUrl = noteUrl;
    }

    // LIKES AND DISLIKES

    public ArrayList<String> getLikes() {
        return this.mLikeList;
    }

    public ArrayList<String> getDislikes() {
        return this.mDislikeList;
    }


    public int getLikesNum() {
        return this.mLikeList.size();
    }

    public int getDislikesNum() {
        return this.mDislikeList.size();
    }


    public void AddLike( String userWhoLikeIt) {

        if ( ! this.mLikeList.contains(userWhoLikeIt)) {
            this.mLikeList.add(userWhoLikeIt);
        }
        if ( this.mDislikeList.contains(userWhoLikeIt)) {
            this.mDislikeList.remove(userWhoLikeIt);
        }
    }

    public void addDislike(String userWhoDislikeIt) {

        if ( ! this.mDislikeList.contains(userWhoDislikeIt)) {
            this.mDislikeList.add(userWhoDislikeIt);
        }
        if ( this.mLikeList.contains(userWhoDislikeIt)) {
            this.mLikeList.remove(userWhoDislikeIt);
        }
    }
}
