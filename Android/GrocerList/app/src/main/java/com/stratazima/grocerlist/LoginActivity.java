package com.stratazima.grocerlist;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

/**
 * Created by esaur_000 on 10/30/2014.
 */
public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Parse.initialize(this, "3fE0cRIrdo0RW4Y3DBiNueuJzpWijqPjufuTDllW", "gME0wQ3IB5ZAbyPYBBgShjUOfQ7e92w9U3ayF7m3");
        ParseUser currentUser = ParseUser.getCurrentUser();

        if (currentUser != null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {
            setContentView(R.layout.activity_login);
            if (savedInstanceState == null) {
                getFragmentManager().beginTransaction().add(R.id.container2, new LoginFragment()).commit();
            }
        }
    }

    public static class LoginFragment extends Fragment {
        private AutoCompleteTextView email;
        private EditText password;

        public LoginFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_login, container, false);
            email = (AutoCompleteTextView) rootView.findViewById(R.id.email);
            password = (EditText) rootView.findViewById(R.id.password);
            Button loginButton = (Button) rootView.findViewById(R.id.email_sign_in_button);
            Button registerButton = (Button) rootView.findViewById(R.id.register_button);

            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ParseUser.logInInBackground(email.getText().toString(), password.getText().toString(), new LogInCallback() {
                        @Override
                        public void done(ParseUser parseUser, com.parse.ParseException e) {
                            if (parseUser != null) {
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(getActivity(),"Error, Please Try Again Later", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });

            registerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().getFragmentManager().beginTransaction().replace(R.id.container2, new RegisterFragment()).commit();
                }
            });
            return rootView;
        }
    }

    public static class RegisterFragment extends Fragment {
        private EditText username;
        private AutoCompleteTextView email;
        private EditText password;

        public RegisterFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_register, container, false);
            username = (EditText) rootView.findViewById(R.id.register_username);
            email = (AutoCompleteTextView) rootView.findViewById(R.id.register_email);
            password = (EditText) rootView.findViewById(R.id.register_password);
            Button registerButton = (Button) rootView.findViewById(R.id.da_register_button);

            registerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ParseUser user = new ParseUser();
                    user.setUsername(username.getText().toString());
                    user.setEmail(email.getText().toString());
                    user.setPassword(password.getText().toString());

                    user.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(com.parse.ParseException e) {
                            if (e == null) {
                                Toast.makeText(getActivity(), "Congrats!", Toast.LENGTH_SHORT).show();
                                getActivity().getFragmentManager().beginTransaction().replace(R.id.container2, new LoginFragment()).commit();
                            } else {
                                e.getMessage();
                                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            });
            return rootView;
        }
    }
}
