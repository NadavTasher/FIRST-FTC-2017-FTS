package themetalrock.x.quackattack2.ftcscouting;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
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
    private final String serviceLogin = serviceProvider + "/sign/login.php";
    private final String serviceNews = serviceProvider + "/news/news.php";
    private int color = Color.parseColor("#7b4b9e");
    private InputFilter groupIDfilter = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence charSequence, int i, int i1, Spanned spanned, int i2, int i3) {
            if (charSequence != null) {
                for (int c = 0; c < charSequence.length(); c++) {
                    boolean charAllowed = false;
                    String allowed = "0123456789";
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
                    String allowed = "0123456789qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM";
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
        //Android Bars
        final Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor("#FF6829FF"));
        window.setNavigationBarColor(Color.parseColor("#FFA73CBA"));
        //Layouts
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
        TextView madebyText, withText;
        Button signup, login;
        //Initialize Widgets
        mainIcon = new ImageView(getApplicationContext());
        tmrIcon = new ImageView(getApplicationContext());
        qattIcon = new ImageView(getApplicationContext());
        loginName = new EditText(getApplicationContext());
        loginPassword = new EditText(getApplicationContext());
        signup = new Button(getApplicationContext());
        login = new Button(getApplicationContext());
        madebyText = new TextView(getApplicationContext());
        withText = new TextView(getApplicationContext());
        //Assign Values
        final Typeface custom_font = Typeface.createFromAsset(getAssets(), "sourcesanspro.ttf");
        //        main.setBackgroundColor(color);
        main.setBackground(getDrawable(R.drawable.login_gradient));
        mainIcon.setImageDrawable(getDrawable(R.drawable.ic_icon));
        tmrIcon.setImageDrawable(getDrawable(R.drawable.ic_themetalrock));
        qattIcon.setImageDrawable(getDrawable(R.drawable.ic_quackattack));
        loginName.setFilters(new InputFilter[]{groupIDfilter});
        loginPassword.setFilters(new InputFilter[]{groupPasswordfilter});
        loginName.setHint("Group ID, e.g '11633'");
        loginPassword.setHint("Password of 6-16 Characters");
        loginName.setTextSize(25);
        loginPassword.setTextSize(21);
        loginPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        loginName.setTypeface(custom_font);
        loginPassword.setTypeface(custom_font);
        login.setText(R.string.login);
        signup.setText(R.string.signup);
        loginName.setHintTextColor(Color.parseColor("#9fb2e0"));
        loginPassword.setHintTextColor(Color.parseColor("#9fb2e0"));
        loginName.setGravity(Gravity.CENTER);
        loginPassword.setError("Must Use 6-16 Chars");
        madebyText.setText(R.string.madeby);
        madebyText.setTypeface(custom_font);
        madebyText.setGravity(Gravity.CENTER);
        madebyText.setTextSize(38);
        madebyText.setTextColor(Color.BLACK);
        withText.setText(R.string.with);
        withText.setGravity(Gravity.CENTER);
        withText.setTextSize(60);
        withText.setTextColor(Color.BLACK);
        login.setBackground(getDrawable(R.drawable.button));
        signup.setBackground(getDrawable(R.drawable.button));
        login.setTextColor(Color.WHITE);
        signup.setTextColor(Color.WHITE);
        login.setTypeface(custom_font);
        signup.setTypeface(custom_font);
        login.setTextSize(22);
        signup.setTextSize(22);
        login.setAllCaps(false);
        signup.setAllCaps(false);
        //LayoutParams Setting
        int icon_size = Light.Device.screenX(getApplicationContext()) / 3;
        int loginX = (int) (Light.Device.screenX(getApplicationContext()) * 0.8);
        int loginY = (int) (Light.Device.screenY(getApplicationContext()) * 0.4);
        LinearLayout.LayoutParams genericIcon = new LinearLayout.LayoutParams(icon_size, icon_size);
        LinearLayout.LayoutParams loginParams = new LinearLayout.LayoutParams(loginX, loginY);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(loginX / 2, ViewGroup.LayoutParams.WRAP_CONTENT);
        mainIcon.setLayoutParams(genericIcon);
        tmrIcon.setLayoutParams(genericIcon);
        qattIcon.setLayoutParams(genericIcon);
        loginView.setLayoutParams(loginParams);
        login.setLayoutParams(buttonParams);
        signup.setLayoutParams(buttonParams);
        //View Adding
        loginSignupView.addView(signup);
        loginSignupView.addView(login);
        loginView.addView(loginName);
        loginView.addView(loginPassword);
        loginView.addView(loginSignupView);
        madebyView.addView(tmrIcon);
        madebyView.addView(withText);
        madebyView.addView(qattIcon);
        main.addView(mainIcon);
        main.addView(loginView);
        main.addView(madebyText);
        main.addView(madebyView);
        //Listeners
        loginPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    String text = loginPassword.getText().toString();
                    if (text.length() >= 6 && text.length() <= 16) {
                        loginPassword.setError(null);
                    } else {
                        loginPassword.setError("Must Be 6-16 Characters");
                    }
                }
            }
        });
        loginPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String text = loginPassword.getText().toString();
                if (text.length() >= 6 && text.length() <= 16) {
                    loginPassword.setError(null);
                } else {
                    loginPassword.setError("Must Be 6-16 Characters");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        loginName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String text = loginName.getText().toString();
                    if (text.length() >= 4 && text.length() <= 7) {
                        loginName.setError(null);
                    } else {
                        loginName.setError("Must Be 4-7 Characters");
                    }
                }
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog loadingDialog = new Dialog(MainScreen.this);
                LinearLayout loadingDialogLayout = new LinearLayout(getApplicationContext());
                loadingDialogLayout.setGravity(Gravity.CENTER);
                loadingDialogLayout.setOrientation(LinearLayout.HORIZONTAL);
                final ProgressBar loadingBar = new ProgressBar(getApplicationContext());
                loadingBar.setIndeterminate(true);
                final ImageView loadedStatus = new ImageView(getApplicationContext());
                int image_size = Light.Device.screenX(getApplicationContext()) / 6;
                int dialog_size = image_size + Light.Device.screenY(getApplicationContext()) / 20;
                loadingBar.setLayoutParams(new LinearLayout.LayoutParams(image_size, image_size));
                loadedStatus.setLayoutParams(new LinearLayout.LayoutParams(image_size, image_size));
                loadingDialogLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dialog_size));
                final TextView loadingText = new TextView(getApplicationContext());
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
                        Log.i("JSON-Response",s);
                        try {
                            JSONObject response = new JSONObject(s);
                            boolean success = response.getBoolean("success");
                            boolean access = response.getBoolean("access");
                            boolean accountReal = response.getBoolean("real");
                            String accountName = response.getString("login");
                            String accountKey = response.getString("key");
                            if (success) {
                                if (accountName.equals(loginName.getText().toString()) && accountKey.equals(loginPassword.getText().toString())) {
                                    if (access) {
                                        loadingBar.setVisibility(View.GONE);
                                        loadingText.setText("Login Success!");
                                        loadedStatus.setVisibility(View.VISIBLE);
                                        loadedStatus.setImageDrawable(getDrawable(R.drawable.ic_accept));
                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            public void run() {
                                                loadingDialog.dismiss();
                                                mainScreen();
                                            }
                                        }, 2000);
                                    } else {
                                        loadingBar.setVisibility(View.GONE);
                                        if (accountReal) {
                                            loadingText.setText("Login Failed, Wrong Credentials.");
                                        } else {
                                            loadingText.setText("Login Failed, No Such Account.");
                                        }
                                        loadedStatus.setVisibility(View.VISIBLE);
                                        loadedStatus.setImageDrawable(getDrawable(R.drawable.ic_decline));
                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            public void run() {
                                                loadingDialog.dismiss();
                                            }
                                        }, 2000);
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            loadingBar.setVisibility(View.GONE);
                            loadingText.setText("JSON Response Error, Code 20");
                            loadedStatus.setVisibility(View.VISIBLE);
                            loadedStatus.setImageDrawable(getDrawable(R.drawable.ic_quackattack));
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                public void run() {
                                    loadingDialog.dismiss();
                                }
                            }, 2000);
                        }
                    }
                }).execute();
            }
        });
        //Commands
        loginName.setError(null);
        loginPassword.setError(null);
        //Show The Main Login View
        setContentView(main);
    }

    private void mainScreen() {
        firstLogin();
    }
}
