package com.smartdengg.rxgallery.example.entity;

import com.smartdengg.rxgallery.entity.FolderEntity;

/**
 * Created by SmartDengg on 2016/6/11.
 */
public class WrapperFolderEntity extends FolderEntity {

    private boolean checked;

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean isChecked() {
        return checked;
    }
}
