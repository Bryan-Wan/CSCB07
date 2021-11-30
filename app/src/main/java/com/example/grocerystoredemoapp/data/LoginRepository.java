package com.example.grocerystoredemoapp.data;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.grocerystoredemoapp.data.model.LoggedInUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */
public class LoginRepository {

    private static volatile LoginRepository instance;

    private FirebaseAuth mAuth;

    // If user credentials will be cached in local storage, it is recommended it be encrypted
    // @see https://developer.android.com/training/articles/keystore
    private LoggedInUser user = null;

    // private constructor : singleton access
    private LoginRepository(FirebaseAuth mAuth) {
        this.mAuth = mAuth;
    }

    public static LoginRepository getInstance(FirebaseAuth mAuth) {
        if (instance == null) {
            instance = new LoginRepository(mAuth);
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return user != null;
    }

    public void logout() {
        user = null;
        FirebaseAuth.getInstance().signOut();
    }

    private void setLoggedInUser(LoggedInUser user) {
        this.user = user;
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }

    public Result<LoggedInUser> login(String username, String password) {
        // handle login
        // Authenticate using Firebase Auth
        try {
            // Sign in using email and password
            mAuth.signInWithEmailAndPassword(username, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            final String loginActivityTag = "Login";
                            if (task.isSuccessful()) {
                                // Sign in success, set the user with the signed-in user's information
                                Log.d(loginActivityTag, "signInWithEmail:success");
                                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                setLoggedInUser(new LoggedInUser(
                                        firebaseUser.getUid(),
                                        firebaseUser.getDisplayName()
                                ));
                            } else {
                                // On sign in failure, log it
                                Log.w(loginActivityTag, "signInWithEmail:failure", task.getException());
                                //Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            return new Result.Success<LoggedInUser>(user);
        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }
}