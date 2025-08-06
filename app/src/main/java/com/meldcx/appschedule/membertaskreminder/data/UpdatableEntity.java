package com.meldcx.appschedule.membertaskreminder.data;

import androidx.room.ColumnInfo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

public abstract class UpdatableEntity {

    @SerializedName("syncStatus")
    @Expose
    @ColumnInfo(name = "syncStatus")
    public Boolean mSyncStatus = true;

    @ColumnInfo(name = "updatedFields")
    public String mUpdatedFields;

    @SerializedName("pending")
    @Expose
    @ColumnInfo(name = "pending")
    public Boolean mPending = false;

    @Expose
    @SerializedName("updatedAt")
    @ColumnInfo(name = "updatedAt")
    public Date mUpdatedAt;

    @SerializedName("isDeleted")
    @Expose
    @ColumnInfo(name = "isDeleted")
    public Boolean isDeleted = false;

    public Boolean getSyncStatus() {
        return mSyncStatus;
    }

    public void setSyncStatus(Boolean syncStatus) {
        mSyncStatus = syncStatus;
    }

    public String getUpdatedFields() {
        return mUpdatedFields;
    }

    public void setUpdatedFields(String updatedFields) {
        mUpdatedFields = updatedFields;
    }

    public Boolean getPending() {
        return mPending;
    }

    public void setPending(Boolean pending) {
        mPending = pending;
    }

    public Date getUpdatedAt() {
        return mUpdatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        mUpdatedAt = updatedAt;
    }
}
