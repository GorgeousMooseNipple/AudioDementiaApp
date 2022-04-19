package lab.android.audiodementia.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import lab.android.audiodementia.R;
import lab.android.audiodementia.background.Background;
import lab.android.audiodementia.background.HttpHandler;
import lab.android.audiodementia.background.UserSignInEvent;
import lab.android.audiodementia.client.PasswordHash;
import lab.android.audiodementia.client.RestClient;
import lab.android.audiodementia.alerts.AlertDialogGenerator;
import lab.android.audiodementia.user.UserSession;

public class LoginActivity extends AppCompatActivity {

    private UserSession session;
    private EditText loginInput;
    private EditText passInput;
    private Background background;
    private boolean showPlaylists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        background = Background.getInstance();
        session = new UserSession(this);


        if(session.isLoggedIn())
            loadMain();

        setContentView(R.layout.activity_login);

        loginInput = findViewById(R.id.login_login_input);
        passInput = findViewById(R.id.login_pass_input);

        Button loginButton = findViewById(R.id.login_login_btn);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = loginInput.getText().toString().toLowerCase().trim();
                String pass = passInput.getText().toString().trim();
                if (checkInput(username, pass))
                    loginUser(username, PasswordHash.sha1Hash(pass));
            }
        });

        Button registerButton = findViewById(R.id.login_register_btn);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(intent);
                finish();
            }
        });

        try {
            Bundle extras = getIntent().getExtras();
            showPlaylists = extras.getString("SHOW_PLAYLISTS") != null;
        }
        catch (NullPointerException ex) {
            ex.printStackTrace();
            showPlaylists = false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        background.register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        background.unregister(this);
    }

    private void loadMain() {
        Intent intent = new Intent(LoginActivity.this, BaseActivity.class);
        if (showPlaylists)
            intent.putExtra("SHOW_PLAYLISTS", "DO_IT");
        startActivity(intent);
        finish();
    }

    private boolean checkInput(String username, String pass) {
        if (username.equals("")) {
            loginInput.setError("Username is required!");
            loginInput.requestFocus();
            return false;
        }
        if (pass.equals("")) {
            passInput.setError("Password is required!");
            passInput.requestFocus();
            return false;
        }
        return true;
    }

    private void loginUser(final String username, final String pass) {
        background.execute(new Runnable() {
            @Override
            public void run() {
                background.postEvent(RestClient.signIn(username, pass));
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UserSignInEvent event){
        if (event.isSuccess()) {
            session.saveLogIn(event.getLogin(), event.getPass(), event.getToken(), event.getRefresh(), event.getId());
            loadMain();
        }
        else {
            AlertDialogGenerator.MakeAlertDialog(this, "Sign in error", event.getMessage());
        }
    }

}
