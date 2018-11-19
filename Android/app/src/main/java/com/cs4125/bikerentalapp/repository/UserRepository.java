package com.cs4125.bikerentalapp.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.cs4125.bikerentalapp.model.dao.UserDao;
import com.cs4125.bikerentalapp.model.entity.User;
import com.cs4125.bikerentalapp.util.UserType;
import com.cs4125.bikerentalapp.web.ResponseBody;
import com.cs4125.bikerentalapp.web.Webservice;

import java.io.IOException;
import java.util.concurrent.Executor;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {
    private final Webservice webservice;
    private final UserDao userDao;
    private final Executor executor;

    public UserRepository(Webservice webservice, UserDao userDao, Executor executor){
        this.webservice = webservice;
        this.userDao = userDao;
        this.executor = executor;
    }

    public LiveData<User> getUser(String userId) {
        refreshUser(userId);
        return userDao.load(userId);
    }

    public LiveData<ResponseBody> registerUser(String username, String password,
                                               String email, String studentCardId){

        MutableLiveData<ResponseBody> liveResponse = new MutableLiveData<>();
        Call<ResponseBody> response = getRegisterRequest(username, password, email, studentCardId);

        response.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                liveResponse.postValue(response.body());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

        return liveResponse;
    }

    public LiveData<ResponseBody> loginUser(String username, String password){
        MutableLiveData<ResponseBody> liveResponse = new MutableLiveData<>();

        Call<ResponseBody> call = webservice.loginUser(username, password);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                liveResponse.postValue(response.body());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

        return liveResponse;
    }


    private void refreshUser(final String username) {
        executor.execute(() -> {
            User user = userDao.load(username).getValue();
            if (!(user == null)) {
                Response<User> response = null;
                try {
                    response = webservice.getUser(username).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                userDao.save(response.body());
            }
        });
    }

    private Call<ResponseBody> getRegisterRequest(String username, String password,
                                                  String email, String studentCardId){
        Call<ResponseBody> request;
        if(studentCardId.equals("")){
            request = webservice.registerUser(username, password, email, UserType.ADMIN.getValue());
        }
        else{
            request = webservice.registerUser(username, password, email, UserType.CUSTOMER.getValue(), studentCardId);
        }

        return request;
    }
}