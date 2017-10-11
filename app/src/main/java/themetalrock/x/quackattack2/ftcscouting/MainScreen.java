package themetalrock.x.quackattack2.ftcscouting;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import nadav.tasher.lightool.Light;

public class MainScreen extends Activity {
    static final String errorCodeManual =
            "Error Code Manual:\n" +
                    "0-20 Connection Errors\n" +
                    "20-40 JSON Errors, Server Response Errors\n" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "";
    private final String serviceProvider = "http://ftc.thepuzik.com";
    private final String servicePush = serviceProvider + "/push/push.php";
    private final String serviceLogin = serviceProvider + "/sign/in.php";
    private final String serviceNews = serviceProvider + "/news/news.php";
    private int color = Color.parseColor("#6699CC");
    private InputFilter groupIDfilter = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence charSequence, int i, int i1, Spanned spanned, int i2, int i3) {
            if (charSequence != null) {
                for (int c = 0; c < charSequence.length(); c++) {
                    boolean charAllowed = false;
                    String allowed = "0123456789ABCDEFabcdef";
                    for (int a = 0; a < allowed.length(); a++) {
                        if (charSequence.charAt(c) == allowed.charAt(a)) {
                            charAllowed = true;
                            break;
                        }
                    }
                    if (!charAllowed) return "";
                }
                return null;
            }
            return null;
        }
    };
    private InputFilter groupPasswordfilter = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence charSequence, int i, int i1, Spanned spanned, int i2, int i3) {
            if (charSequence != null) {
                for (int c = 0; c < charSequence.length(); c++) {
                    boolean charAllowed = false;
                    String allowed = "0123456789abcdefghijklmnop";
                    for (int a = 0; a < allowed.length(); a++) {
                        if (charSequence.charAt(c) == allowed.charAt(a)) {
                            charAllowed = true;
                            break;
                        }
                    }
                    if (!charAllowed) return "";
                }
                return null;
            }
            return null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        splash();
        mainScreen();
    }

    private void resetPopup(String error, int errorCode) {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle("Fatal Error");
        adb.setMessage(error + "\nCode: " + String.valueOf(errorCode));
        adb.setPositiveButton("Reset", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                init();
            }
        });
        adb.setCancelable(false);
        adb.show();
    }

    private void splash() {
        final Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(color);
        window.setNavigationBarColor(color);
        LinearLayout ll = new LinearLayout(this);
        ll.setGravity(Gravity.CENTER);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setBackgroundColor(color);
        ImageView icon = new ImageView(this);
        icon.setImageDrawable(getDrawable(R.drawable.ic_icon));
        int is = (int) (Light.Device.screenX(getApplicationContext()) * 0.8);
        icon.setLayoutParams(new LinearLayout.LayoutParams(is, is));
        ll.addView(icon);
        setContentView(ll);
        new Light.Net.Pinger(5000, new Light.Net.Pinger.OnEnd() {
            @Override
            public void onPing(String s, boolean b) {
                if (s.equals(serviceProvider) && b) {
                    mainScreen();
                } else {
                    resetPopup("No Response From Service Provider", 10);
                }
            }
        }).execute(serviceProvider);
    }

    private void firstLogin() {
        LinearLayout main = new LinearLayout(getApplicationContext());
        main.setOrientation(LinearLayout.VERTICAL);
        main.setGravity(Gravity.CENTER);
        //
        LinearLayout loginView = new LinearLayout(getApplicationContext());
        loginView.setOrientation(LinearLayout.VERTICAL);
        loginView.setGravity(Gravity.CENTER);
        //
        LinearLayout loginSignupView = new LinearLayout(getApplicationContext());
        loginSignupView.setOrientation(LinearLayout.HORIZONTAL);
        loginSignupView.setGravity(Gravity.CENTER);
        //
        LinearLayout madebyView = new LinearLayout(getApplicationContext());
        madebyView.setOrientation(LinearLayout.HORIZONTAL);
        madebyView.setGravity(Gravity.CENTER);
        //
        ImageView mainIcon, tmrIcon, qattIcon;
        final EditText loginName, loginPassword;
        TextView madebyText;
        Button signup, login;
        //Initialize Widgets
        mainIcon = new ImageView(getApplicationContext());
        tmrIcon = new ImageView(getApplicationContext());
        qattIcon = new ImageView(getApplicationContext());
        loginName = new EditText(getApplicationContext());
        loginPassword = new EditText(getApplicationContext());
        signup = new Button(getApplicationContext());
        login = new Button(getApplicationContext());
        madebyText=new TextView(getApplicationContext());
        //Assign Values
        main.setBackgroundColor(color);
        mainIcon.setImageDrawable(getDrawable(R.drawable.ic_icon));
        tmrIcon.setImageDrawable(getDrawable(R.drawable.ic_themetalrock));
        qattIcon.setImageDrawable(getDrawable(R.drawable.ic_quackattack));
        loginName.setFilters(new InputFilter[]{groupIDfilter});
        loginPassword.setFilters(new InputFilter[]{groupPasswordfilter});
        loginName.setHint("Group ID, without '#'. e.g '11633'");
        loginPassword.setHint("Password, Length of 6+ Characters");
        loginPassword.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD);
        login.setText(R.string.login);
        signup.setText(R.string.signup);
        madebyText.setText(R.string.madeby);
        madebyText.setGravity(Gravity.CENTER);
        madebyText.setTextSize(30);
        madebyText.setTextColor(Color.BLACK);
        //LayoutParams Setting
        int icon_size= Light.Device.screenX(getApplicationContext())/4;
        LinearLayout.LayoutParams genericIcon=new LinearLayout.LayoutParams(icon_size,icon_size);
        mainIcon.setLayoutParams(genericIcon);
        tmrIcon.setLayoutParams(genericIcon);
        qattIcon.setLayoutParams(genericIcon);
        //View Adding
        loginSignupView.addView(signup);
        loginSignupView.addView(login);
        loginView.addView(loginName);
        loginView.addView(loginPassword);
        loginView.addView(loginSignupView);
        main.addView(mainIcon);
        main.addView(loginView);
        main.addView(madebyText);
        main.addView(madebyView);

        //OnClick Listeners
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog loadingDialog=new Dialog(MainScreen.this);
                LinearLayout loadingDialogLayout=new LinearLayout(getApplicationContext());
                loadingDialogLayout.setGravity(Gravity.CENTER);
                loadingDialogLayout.setOrientation(LinearLayout.HORIZONTAL);
                ProgressBar loadingBar=new ProgressBar(getApplicationContext());
                loadingBar.setIndeterminate(true);
                ImageView loadedStatus=new ImageView(getApplicationContext());
                int image_size= Light.Device.screenX(getApplicationContext())/6;
                int dialog_size=image_size+ Light.Device.screenY(getApplicationContext())/20;
                loadingBar.setLayoutParams(new LinearLayout.LayoutParams(image_size,image_size));
                loadedStatus.setLayoutParams(new LinearLayout.LayoutParams(image_size,image_size));
                loadingDialogLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dialog_size));
                TextView loadingText=new TextView(getApplicationContext());
                loadingText.setText(R.string.checking_conn_w_server);
                //TODO add custom font to textview
                loadingText.setTextSize(21);
                loadingText.setTextColor(Color.WHITE);
                loadingBar.setVisibility(View.VISIBLE);
                loadedStatus.setVisibility(View.GONE);
                loadingDialogLayout.addView(loadedStatus);
                loadingDialogLayout.addView(loadingBar);
                loadingDialogLayout.addView(loadingText);
                loadingDialog.setCancelable(false);
                loadingDialog.setContentView(loadingDialogLayout);
                loadingDialog.show();
                ArrayList<Light.Net.PHP.Post.PHPParameter> loginParameters = new ArrayList<>();
                loginParameters.add(new Light.Net.PHP.Post.PHPParameter("login", loginName.getText().toString()));
                loginParameters.add(new Light.Net.PHP.Post.PHPParameter("key", loginPassword.getText().toString()));
                loginParameters.add(new Light.Net.PHP.Post.PHPParameter("action", "verifyCred"));
                loginParameters.add(new Light.Net.PHP.Post.PHPParameter("version", String.valueOf(Light.Device.getVersionCode(getApplicationContext(), getPackageName()))));
                new Light.Net.PHP.Post(serviceLogin, loginParameters, new Light.Net.PHP.Post.OnPost() {
                    @Override
                    public void onPost(String s) {
                        try {
                            JSONObject response = new JSONObject(s);
                            boolean success=response.getBoolean("success");
                            boolean access=response.getBoolean("access");
                            String accountName=response.getString("login");
                            String accountKey=response.getString("key");
                            if(success){
                                if(accountName.equals(loginName.getText().toString())&&accountKey.equals(loginPassword.getText().toString())){
                                    if(access){
                                        //Next Screen
                                        Log.e("LOGINRESULT",String.valueOf(access));
                                    }else{
                                        //Tell User About Failure
                                        Toast.makeText(getApplicationContext(),"Wrong Credentials, Or Non Existant Account!",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            resetPopup("Fatal JSON Error", 20);
                        }
                    }
                }).execute();
            }
        });
        //Show The Main Login View
        setContentView(main);
    }
    private void mainScreen() {
        firstLogin();
    }
}
