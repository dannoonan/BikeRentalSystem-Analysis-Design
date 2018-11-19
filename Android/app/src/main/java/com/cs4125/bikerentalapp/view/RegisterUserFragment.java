package com.cs4125.bikerentalapp.view;

import android.arch.lifecycle.LiveData;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cs4125.bikerentalapp.R;
import com.cs4125.bikerentalapp.repository.UserRepository;
import com.cs4125.bikerentalapp.sl.ServiceLocator;
import com.cs4125.bikerentalapp.viewmodel.IRegisterViewModel;
import com.cs4125.bikerentalapp.viewmodel.RegisterViewModel;
import com.cs4125.bikerentalapp.web.ResponseBody;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;


public class RegisterUserFragment extends Fragment {

    private EditText usernameField;
    private EditText emailField;
    private EditText passwordField;
    private EditText confirmPasswordField;
    private EditText studentCardId;
    private Button   registerButton;
    private NavController navController;

    private IRegisterViewModel registerViewModel;


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_user, container, false);

        configureUiItems(view);
        configureServiceLocator();
        configureViewModel();

        return view;
    }

    private void configureServiceLocator(){
        ServiceLocator.bindCustomServiceImplementation(
                IRegisterViewModel.class,
                RegisterViewModel.class);
    }

    private void configureViewModel(){
        registerViewModel = ServiceLocator.get(RegisterViewModel.class);
        registerViewModel.init(ServiceLocator.get(UserRepository.class));
    }


    private void configureUiItems(View view) {
        bindUiItems(view);
        Navigation.setViewNavController(view, new NavController(getContext()));
        navController = NavHostFragment.findNavController(this);

        registerButton.setOnClickListener(view1 -> registerUser());
    }

    private void bindUiItems(View view){
        usernameField = view.findViewById(R.id.usernameTxt);
        emailField = view.findViewById(R.id.emailTxt);
        passwordField = view.findViewById(R.id.passwordTxt);
        confirmPasswordField = view.findViewById(R.id.confirmPasswordTxt);
        studentCardId = view.findViewById(R.id.cardTxt);
        registerButton = view.findViewById(R.id.registerBtn);
    }

    private void registerUser(){
        String username = usernameField.getText().toString();
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();
        String confirmPassword = confirmPasswordField.getText().toString();
        String id = studentCardId.getText().toString();

        if(password.equals(confirmPassword)){
            LiveData<ResponseBody> liveResponse = registerViewModel
                    .register(username, password, email, id);

            liveResponse.observe(this, this::observeResponse);
        }
        else{
            showToast("Passwords did not match");
        }
    }

    private void observeResponse(@Nullable ResponseBody response){
        if(response != null) {
            if (response.getResponseCode() == 200) {
                showToast("Registration Successful");

                navController.navigate(R.id.action_registerFragment_to_menuFragment);
            } else {
                showToast("Registration Failed");
            }
        }
    }

    private void showToast(String message){
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}