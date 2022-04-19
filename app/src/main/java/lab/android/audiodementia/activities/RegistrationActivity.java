package lab.android.audiodementia.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import lab.android.audiodementia.R;
import lab.android.audiodementia.background.Background;
import lab.android.audiodementia.background.HttpHandler;
import lab.android.audiodementia.background.UserRegisterEvent;
import lab.android.audiodementia.client.HttpResponse;
import lab.android.audiodementia.client.PasswordHash;
import lab.android.audiodementia.client.RestClient;
import lab.android.audiodementia.alerts.AlertDialogGenerator;
import lab.android.audiodementia.user.UserSession;
import java.util.Map;
import java.util.HashMap;

public class RegistrationActivity extends AppCompatActivity {

    private UserSession session;
    private EditText usernameInput;
    private EditText passInput;
    private EditText confirmInput;
    private Background background;
    private HttpHandler httpHandler;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        background = Background.getInstance();
        setContentView(R.layout.activity_registration);

        session = new UserSession(this);
        httpHandler = new HttpHandler();

        usernameInput = findViewById(R.id.registration_login_input);
        passInput = findViewById(R.id.registration_pass_input);
        confirmInput = findViewById(R.id.registration_confirm_input);

        Button loginButton = findViewById(R.id.registration_login_btn);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Button registerButton = findViewById(R.id.registration_reg_btn);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameInput.getText().toString().toLowerCase().trim();
                String pass = passInput.getText().toString().trim();
                String confirm = confirmInput.getText().toString().trim();

                if (checkInput(username, pass, confirm))
                    register(username, PasswordHash.sha1Hash(pass));
            }
        });
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

    private boolean checkInput(String username, String pass, String confirm) {
        if (username.equals(""))
        {
            usernameInput.setError("Username is required!");
            usernameInput.requestFocus();
            return false;
        }
        if (pass.equals(""))
        {
            passInput.setError("Password is required!");
            passInput.requestFocus();
            return false;
        }
        if (confirm.equals(""))
        {
            confirmInput.setError("Please confirm password!");
            confirmInput.requestFocus();
            return false;
        }
        if (!confirm.equals(pass))
        {
            confirmInput.setError("Confirmation password is unmatched!");
            confirmInput.requestFocus();
            return false;
        }
        return true;
    }

    private void loadLogin() {
        Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void register(final String username, final String pass)
    {
        Map<String, String> params = new HashMap<>();
        params.put("login", username);
        params.put("pass", pass);
        this.httpHandler.execute(RestClient::register, params, this::onRegister);
    }

    public void onRegister(HttpResponse event){
        if (event.isSuccess()) {
            loadLogin();
        }
        else {
            AlertDialogGenerator.MakeAlertDialog(this, "Registration error", event.getMessage());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UserRegisterEvent event){
        if (event.isSuccess()) {
            loadLogin();
        }
        else {
            AlertDialogGenerator.MakeAlertDialog(this, "Registration error", event.getMessage());
        }
    }

}
