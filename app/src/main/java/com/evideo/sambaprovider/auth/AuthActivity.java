
package com.evideo.sambaprovider.auth;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.evideo.sambaprovider.R;
import com.evideo.sambaprovider.SambaProviderApplication;
import com.evideo.sambaprovider.ShareManager;
import com.evideo.sambaprovider.base.OnTaskFinishedCallback;
import com.evideo.sambaprovider.nativefacade.SmbClient;


public class AuthActivity extends AppCompatActivity {
  private static final String TAG = "AuthActivity";

  private static final String SHARE_URI_KEY = "shareUri";

  private EditText mSharePathEditText;
  private EditText mDomainEditText;
  private EditText mUsernameEditText;
  private EditText mPasswordEditText;

  private CheckBox mPinShareCheckbox;

  private ProgressDialog progressDialog;

  private ShareManager mShareManager;
  private SmbClient mClient;

  private final View.OnClickListener mLoginListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      tryAuth();
    }
  };

  private final OnTaskFinishedCallback<Void> callback = new OnTaskFinishedCallback<Void>() {
    @Override
    public void onTaskFinished(@Status int status, @Nullable Void item, @Nullable Exception e) {
      progressDialog.dismiss();

      if (status == SUCCEEDED) {
        setResult(RESULT_OK);
        finish();
      } else {
        Log.i(TAG, "Authentication failed: ", e);

        showMessage(R.string.credential_error);
      }
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    final Context context = getApplicationContext();
    mShareManager = SambaProviderApplication.getServerManager(context);
    mClient = SambaProviderApplication.getSambaClient(context);

    Intent authIntent = getIntent();
    String shareUri = authIntent.getStringExtra(SHARE_URI_KEY);

    prepareUI(shareUri);
  }

  private void prepareUI(String shareUri) {
    mSharePathEditText = (EditText) findViewById(R.id.share_path);
    mUsernameEditText = (EditText) findViewById(R.id.username);
    mDomainEditText = (EditText) findViewById(R.id.domain);
    mPasswordEditText = (EditText) findViewById(R.id.password);

    CheckBox passwordCheckbox = (CheckBox) findViewById(R.id.needs_password);
    mPinShareCheckbox = (CheckBox) findViewById(R.id.pin_share);

    mSharePathEditText.setText(shareUri);
    mSharePathEditText.setEnabled(false);

//    passwordCheckbox.setVisibility(View.GONE);
    mPinShareCheckbox.setVisibility(View.VISIBLE);

    Button mLoginButton = (Button) findViewById(R.id.mount);
    mLoginButton.setText(getResources().getString(R.string.login));
    mLoginButton.setOnClickListener(mLoginListener);

    final Button cancel = (Button) findViewById(R.id.cancel);
    cancel.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        finish();
      }
    });
  }

  private void tryAuth() {
    progressDialog = ProgressDialog.show(
            this, null, getResources().getString(R.string.authenticating), true);

    final String username = mUsernameEditText.getText().toString();
    final String password = mPasswordEditText.getText().toString();

    if (username.isEmpty() || password.isEmpty()) {
      showMessage(R.string.empty_credentials);
      return;
    }

    new AuthorizationTask(
      mSharePathEditText.getText().toString(),
      username,
      password,
      mDomainEditText.getText().toString(),
      mPinShareCheckbox.isChecked(),
      mShareManager,
      mClient,
      callback).execute();
  }

  private void showMessage(@StringRes int id) {
    Snackbar.make(mPinShareCheckbox, id, Snackbar.LENGTH_SHORT).show();
  }

  public static PendingIntent createAuthIntent(Context context, String shareUri) {
    Intent authIntent = new Intent();
    authIntent.setComponent(new ComponentName(
            context.getPackageName(),
            AuthActivity.class.getName()));
    authIntent.putExtra(SHARE_URI_KEY, shareUri);

    return PendingIntent.getActivity(
            context, 0, authIntent, PendingIntent.FLAG_UPDATE_CURRENT);
  }
}
