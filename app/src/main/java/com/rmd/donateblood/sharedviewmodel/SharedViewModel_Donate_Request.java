package com.rmd.donateblood.sharedviewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.rmd.donateblood.model.Donate_or_Request_Id_Type;

public class SharedViewModel_Donate_Request extends ViewModel {
    private MutableLiveData<Donate_or_Request_Id_Type> data = new MutableLiveData<>();

    public void set_id_and_type(Donate_or_Request_Id_Type value){
        data.setValue(value);
    }
    public LiveData<Donate_or_Request_Id_Type> get_id_and_type(){
        return data;
    }
}
