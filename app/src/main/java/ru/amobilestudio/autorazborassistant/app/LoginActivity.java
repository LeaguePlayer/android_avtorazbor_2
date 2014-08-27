package ru.amobilestudio.autorazborassistant.app;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

import ru.amobilestudio.autorazborassistant.asyncs.GetSelectsDataAsync;
import ru.amobilestudio.autorazborassistant.asyncs.LoginAsync;
import ru.amobilestudio.autorazborassistant.helpers.ActivityHelper;
import ru.amobilestudio.autorazborassistant.helpers.AlertDialogHelper;
import ru.amobilestudio.autorazborassistant.helpers.ConnectionHelper;
import ru.amobilestudio.autorazborassistant.helpers.UserInfoHelper;


public class LoginActivity extends ActionBarActivity implements View.OnClickListener {

    private EditText loginInput;
    private EditText passInput;
    private Button sendButton;

    private ArrayList<String> _errors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityHelper.hideActionBar(this);

        /*if login go to MainActivity*/
        if(UserInfoHelper.isLogin(this)){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return;
        }

        setContentView(R.layout.activity_login);

        loginInput = (EditText) findViewById(R.id.login_name);
        passInput = (EditText) findViewById(R.id.login_pass);
        sendButton = (Button) findViewById(R.id.login_send);

        sendButton.setOnClickListener(this);

        if(ConnectionHelper.checkNetworkConnection(this)){
            GetSelectsDataAsync dataAsync = new GetSelectsDataAsync(this);
            dataAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(UserInfoHelper.isLogin(this)){
            finish();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.login_send:
                if(ConnectionHelper.checkNetworkConnection(this)){
                    if(validateLoginForm()){
                        LoginAsync loginAsync = new LoginAsync(this);
                        loginAsync.execute(loginInput.getText().toString(), passInput.getText().toString());
                    }else
                        AlertDialogHelper.showAlertDialog(this, getString(R.string.error_title), TextUtils.join("\n", _errors), true);
                }
                break;
        }
    }

    private boolean validateLoginForm() {
        boolean valid = true;
        _errors = new ArrayList<String>();

        String login = loginInput.getText().toString();
        String pass = passInput.getText().toString();

        if(login.equals("")){
            _errors.add(getString(R.string.login_name_empty));
            valid = valid && false;
        }

        if(pass.equals("")){
            _errors.add(getString(R.string.login_pass_empty));
            valid = valid && false;
        }

        return valid;
    }
}
