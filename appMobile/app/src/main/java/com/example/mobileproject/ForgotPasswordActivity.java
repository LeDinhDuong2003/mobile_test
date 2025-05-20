package com.example.mobileproject;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.mobileproject.fragment.CodeFragment;
import com.example.mobileproject.fragment.PhoneFragment;
import com.example.mobileproject.fragment.ResetPasswordFragment;

public class ForgotPasswordActivity extends AppCompatActivity implements
        PhoneFragment.PhoneFragmentListener,
        CodeFragment.CodeFragmentListener,
        ResetPasswordFragment.ResetPasswordFragmentListener {

    private static final String TAG = "🔥 quan 🔥";
    public static final String PHONE_FRAGMENT_TAG = "phone_fragment";
    public static final String CODE_FRAGMENT_TAG = "code_fragment";
    public static final String RESET_PASSWORD_FRAGMENT_TAG = "reset_password_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        if (savedInstanceState == null) {
            loadFragment(new PhoneFragment(), PHONE_FRAGMENT_TAG, false);
        }
    }

    private void loadFragment(Fragment fragment, String tag, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment, tag);
        if (addToBackStack) {
            transaction.addToBackStack(tag);
        }
        transaction.commit();
    }

    @Override
    public void onPhoneVerified(String phone, String code) {
        Log.d(TAG, "🔥 Phone verified: " + phone + ", code: " + code);
        CodeFragment codeFragment = CodeFragment.newInstance(phone, code);
        loadFragment(codeFragment, CODE_FRAGMENT_TAG, true);
    }

    @Override
    public void onCodeVerified(String phone) {
        Log.d(TAG, "🔥 Code verified for phone: " + phone);
        ResetPasswordFragment resetPasswordFragment = ResetPasswordFragment.newInstance(phone);
        loadFragment(resetPasswordFragment, RESET_PASSWORD_FRAGMENT_TAG, true);
    }

    @Override
    public void onPasswordReset() {
        Log.d(TAG, "🔥 Password reset successful, finishing activity");
        finish();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}