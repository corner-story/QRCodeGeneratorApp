package com.lambdafate.qrcode.ui.notifications;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class NotificationsViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public NotificationsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Hello, World!");
    }

    public LiveData<String> getText() {
        return mText;
    }

    public void setText(String v){
        mText.setValue(v);
    }
}