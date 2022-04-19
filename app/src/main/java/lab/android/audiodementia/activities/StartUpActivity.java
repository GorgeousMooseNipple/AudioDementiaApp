package lab.android.audiodementia.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;

import lab.android.audiodementia.R;
import lab.android.audiodementia.background.Background;
import lab.android.audiodementia.background.ConnectionCheckEvent;
import lab.android.audiodementia.background.Function;
import lab.android.audiodementia.background.HttpHandler;
import lab.android.audiodementia.background.Provider;
import lab.android.audiodementia.background.ResponseEvent;
import lab.android.audiodementia.client.CacheManager;
import lab.android.audiodementia.client.HttpResponse;
import lab.android.audiodementia.client.RestClient;
import lab.android.audiodementia.model.Genre;
import lab.android.audiodementia.model.Playlist;
import lab.android.audiodementia.user.UserSession;

public class StartUpActivity extends AppCompatActivity{

    private Background background = Background.getInstance();
    private ProgressDialog dialog;
    private TextView message;
    private Button tryAgainButton;
    private Handler handler;
    private HttpHandler httpHandler;
    private int counter;
    private UserSession session;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        // TODO: uncomment this to clear persistent user data for login test purposes
//        session = new UserSession(this);
//        if(session.isLoggedIn()) {
//            session.logout();
//        }

        dialog = new ProgressDialog(this);
        this.counter = 0;

        setContentView(R.layout.activity_startup);
        message = findViewById(R.id.startup_message);
        tryAgainButton = findViewById(R.id.startup_reconnect_button);

        handler = new Handler();

        httpHandler = new HttpHandler();
    }

    @Override
    protected void onStart() {
        super.onStart();
        background.register(this);
        background.execute(new Runnable() {
            @Override
            public void run() {
                CacheManager.enableHttpCaching(getApplicationContext());
            }
        });
         tryConnect();
        message.setText(String.valueOf(counter));
        tryAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 tryConnect();
                ++counter;
                message.setText(String.valueOf(counter));
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        background.unregister(this);
    }

    private void tryConnect() {
        if (message.getVisibility() == View.VISIBLE) {
            message.setText("Can't connect to the server");
            message.setVisibility(View.GONE);
        }
        if (tryAgainButton.getVisibility() == View.VISIBLE) {
            tryAgainButton.setVisibility(View.GONE);
        }
        dialog.setMessage("Connection established");
        dialog.show();
        background.execute(new Runnable() {
            @Override
            public void run() {
                background.postEvent(RestClient.checkConnection(10000));
            }
        });
    }

    private void loadLogin() {
        Intent intent = new Intent(StartUpActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConnectionCheckEvent event) {
        if(dialog.isShowing())
            dialog.dismiss();
        if(event.isSuccess()) {
            loadLogin();
        }
        else
        {
            if (message.getVisibility() == View.GONE) {
                message.setText("Can't connect to the server");
                message.setVisibility(View.VISIBLE);
            }
            if (tryAgainButton.getVisibility() == View.GONE) {
                tryAgainButton.setVisibility(View.VISIBLE);
            }
        }
    }
}
