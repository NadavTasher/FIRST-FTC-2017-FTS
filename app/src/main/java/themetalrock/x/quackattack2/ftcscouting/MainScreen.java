package themetalrock.x.quackattack2.ftcscouting;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.Shape;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import nadav.tasher.lightool.Light;

public class MainScreen extends Activity {
    static final String errorCodeManual = "Error Code Manual:\n" + "0-20 Connection Errors\n" + "20-40 JSON Errors, Server Response Errors\n" + "" + "" + "" + "" + "" + "" + "";
    private final String serviceProvider = "http://ftc.thepuzik.com";
    private final String servicePush = serviceProvider + "/push/push.php";
    private final String serviceLogin = serviceProvider + "/sign/login.php";
    private final String serviceSearch = serviceProvider + "/sign/search.php";
    private final String serviceNews = serviceProvider + "/news/news.php";
    private final String client = "FTSAndroid";
    private SharedPreferences sp;
    private int color = Color.parseColor("#6ba593");
    private int secolor = color + 0x333333;
    private JSONArray alreadyScouting;
    private ImageView groupIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private int remakeSecondColor() {
        secolor = color + 0x333333;
        return secolor;
    }

    private Typeface getTypeface() {
        return Typeface.createFromAsset(getAssets(), "ssp.ttf");
    }

    private void checkCredentials() {
        ArrayList<Light.Net.PHP.Post.PHPParameter> loginParameters = new ArrayList<>();
        loginParameters.add(new Light.Net.PHP.Post.PHPParameter("login", sp.getString("account", "")));
        loginParameters.add(new Light.Net.PHP.Post.PHPParameter("key", sp.getString("key", "")));
        loginParameters.add(new Light.Net.PHP.Post.PHPParameter("action", "verifyCred"));
        loginParameters.add(new Light.Net.PHP.Post.PHPParameter("version", String.valueOf(Light.Device.getVersionCode(getApplicationContext(), getPackageName()))));
        new Light.Net.PHP.Post(serviceLogin, loginParameters, new Light.Net.PHP.Post.OnPost() {
            @Override
            public void onPost(String s) {
                Log.i("JSON-Response", s);
                try {
                    JSONObject response = new JSONObject(s);
                    boolean success = response.getBoolean("success");
                    boolean access = response.getBoolean("access");
                    if (success) {
                        if (access) {
                            mainScreen();
                        } else {
                            firstLogin(sp.getString("account", null));
                        }
                    }
                } catch (JSONException e) {
                    checkCredentials();
                }
            }
        }).execute();
    }

    private void init() {
        sp = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        splash();
        new Light.Net.Pinger(5000, new Light.Net.Pinger.OnEnd() {
            @Override
            public void onPing(String s, boolean b) {
                if (s.equals(serviceProvider) && b) {
                    if (!sp.contains("account")) {
                        firstLogin(sp.getString("account", null));
                    } else {
                        checkCredentials();
                    }
                } else {
                    resetPopup("No Response From Service Provider", 10);
                }
            }
        }).execute(serviceProvider);
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
    }

    private void firstLogin(String account) {
        //Text Filters
        InputFilter groupPasswordfilter = new InputFilter() {

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
        InputFilter teamNameFilter = new InputFilter() {

            @Override
            public CharSequence filter(CharSequence charSequence, int i, int i1, Spanned spanned, int i2, int i3) {
                if (charSequence != null) {
                    for (int c = 0; c < charSequence.length(); c++) {
                        boolean charAllowed = false;
                        String allowed = "0123456789qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM ";
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
        InputFilter groupIDfilter = new InputFilter() {

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
        //Android Bars
        final Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(color);
        window.setNavigationBarColor(color);
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
        final EditText loginName, loginPassword, extraName;
        TextView madebyText, withText;
        final Button signup, login;
        //Initialize Widgets
        mainIcon = new ImageView(getApplicationContext());
        tmrIcon = new ImageView(getApplicationContext());
        qattIcon = new ImageView(getApplicationContext());
        loginName = new EditText(getApplicationContext());
        loginPassword = new EditText(getApplicationContext());
        extraName = new EditText(getApplicationContext());
        signup = new Button(getApplicationContext());
        login = new Button(getApplicationContext());
        madebyText = new TextView(getApplicationContext());
        withText = new TextView(getApplicationContext());
        //Assign Values
        main.setBackgroundColor(color);
        mainIcon.setImageDrawable(getDrawable(R.drawable.ic_icon));
        tmrIcon.setImageDrawable(getDrawable(R.drawable.ic_themetalrock));
        qattIcon.setImageDrawable(getDrawable(R.drawable.ic_quackattack));
        loginName.setFilters(new InputFilter[]{groupIDfilter});
        loginPassword.setFilters(new InputFilter[]{groupPasswordfilter});
        extraName.setFilters(new InputFilter[]{teamNameFilter});
        loginName.setHint("Group ID, e.g '11633'");
        loginPassword.setHint("Password of 6-16 Characters");
        extraName.setHint("Team's Name, For Sign Up");
        loginName.setTextSize(25);
        extraName.setTextSize(24);
        loginPassword.setTextSize(21);
        loginPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        loginName.setTypeface(getTypeface());
        loginPassword.setTypeface(getTypeface());
        extraName.setTypeface(getTypeface());
        login.setText(R.string.login);
        signup.setText(R.string.signup);
        loginName.setHintTextColor(Color.BLACK);
        loginPassword.setHintTextColor(Color.BLACK);
        extraName.setHintTextColor(Color.BLACK);
        loginName.setGravity(Gravity.CENTER);
        loginPassword.setGravity(Gravity.CENTER);
        extraName.setGravity(Gravity.CENTER);
        loginPassword.setError("Must Use 6-16 Chars");
        loginName.setText(account);
        madebyText.setText(R.string.madeby);
        madebyText.setTypeface(getTypeface());
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
        login.setTypeface(getTypeface());
        signup.setTypeface(getTypeface());
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
        loginView.addView(extraName);
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
                String text = loginName.getText().toString();
                String text2 = loginPassword.getText().toString();
                if (text.length() >= 4 && text.length() <= 7 && text2.length() >= 6 && text2.length() <= 16) {
                    final Dialog loadingDialog = new Dialog(MainScreen.this);
                    LinearLayout loadingDialogLayout = new LinearLayout(getApplicationContext());
                    loadingDialogLayout.setPadding(10, 10, 10, 10);
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
                    loadingText.setTypeface(getTypeface());
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
                    loginParameters.add(new Light.Net.PHP.Post.PHPParameter("client", client));
                    loginParameters.add(new Light.Net.PHP.Post.PHPParameter("version", String.valueOf(Light.Device.getVersionCode(getApplicationContext(), getPackageName()))));
                    new Light.Net.PHP.Post(serviceLogin, loginParameters, new Light.Net.PHP.Post.OnPost() {
                        @Override
                        public void onPost(String s) {
                            Log.i("JSON-Response", s);
                            try {
                                JSONObject response = new JSONObject(s);
                                boolean success = response.getBoolean("success");
                                boolean access = response.getBoolean("access");
                                boolean accountReal = response.getBoolean("real");
                                final String accountName = response.getString("login");
                                final String accountKey = response.getString("key");
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
                                                    sp.edit().putString("account", accountName).putString("key", accountKey).commit();
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
                                loadedStatus.setImageDrawable(getDrawable(R.drawable.ic_warning));
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        loadingDialog.dismiss();
                                    }
                                }, 2000);
                            }
                        }
                    }).execute();
                } else {
                    if (text2.length() >= 6 && text.length() <= 16) {
                        loginPassword.setError(null);
                    } else {
                        loginPassword.setError("Must Be 6-16 Characters");
                    }
                    if (text.length() >= 4 && text.length() <= 7) {
                        loginName.setError(null);
                    } else {
                        loginName.setError("Must Be 4-7 Characters");
                    }
                }
            }
        });
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = loginName.getText().toString();
                String text2 = loginPassword.getText().toString();
                String text3 = extraName.getText().toString();
                if (text.length() >= 4 && text.length() <= 7 && text2.length() >= 6 && text2.length() <= 16 && text3.length() != 0) {
                    final Dialog loadingDialog = new Dialog(MainScreen.this);
                    LinearLayout loadingDialogLayout = new LinearLayout(getApplicationContext());
                    loadingDialogLayout.setPadding(10, 10, 10, 10);
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
                    loadingText.setTypeface(getTypeface());
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
                    loginParameters.add(new Light.Net.PHP.Post.PHPParameter("name", extraName.getText().toString()));
                    loginParameters.add(new Light.Net.PHP.Post.PHPParameter("action", "signup"));
                    loginParameters.add(new Light.Net.PHP.Post.PHPParameter("client", client));
                    loginParameters.add(new Light.Net.PHP.Post.PHPParameter("version", String.valueOf(Light.Device.getVersionCode(getApplicationContext(), getPackageName()))));
                    new Light.Net.PHP.Post(serviceLogin, loginParameters, new Light.Net.PHP.Post.OnPost() {
                        @Override
                        public void onPost(String s) {
                            Log.i("JSON-Response", s);
                            try {
                                JSONObject response = new JSONObject(s);
                                boolean success = response.getBoolean("success");
                                if (success) {
                                    boolean taken = response.getBoolean("taken");
                                    if (!taken) {
                                        boolean access = response.getBoolean("access");
                                        boolean signedup = response.getBoolean("signup");
                                        final String accountName = response.getString("login");
                                        final String accountKey = response.getString("key");
                                        if (accountName.equals(loginName.getText().toString()) && accountKey.equals(loginPassword.getText().toString())) {
                                            if (signedup) {
                                                if (access) {
                                                    loadingBar.setVisibility(View.GONE);
                                                    loadingText.setText("Sign-Up Succeeded.");
                                                    loadedStatus.setVisibility(View.VISIBLE);
                                                    loadedStatus.setImageDrawable(getDrawable(R.drawable.ic_accept));
                                                    Handler handler = new Handler();
                                                    handler.postDelayed(new Runnable() {
                                                        public void run() {
                                                            loadingDialog.dismiss();
                                                            sp.edit().putString("account", accountName).putString("key", accountKey).commit();
                                                            mainScreen();
                                                        }
                                                    }, 2000);
                                                } else {
                                                    loadingBar.setVisibility(View.GONE);
                                                    loadingText.setText("Failed To Save Account Settings.");
                                                    loadedStatus.setVisibility(View.VISIBLE);
                                                    loadedStatus.setImageDrawable(getDrawable(R.drawable.ic_decline));
                                                    Handler handler = new Handler();
                                                    handler.postDelayed(new Runnable() {
                                                        public void run() {
                                                            loadingDialog.dismiss();
                                                        }
                                                    }, 2000);
                                                }
                                            } else {
                                                loadingBar.setVisibility(View.GONE);
                                                loadingText.setText("Internal Server Error");
                                                loadedStatus.setVisibility(View.VISIBLE);
                                                loadedStatus.setImageDrawable(getDrawable(R.drawable.ic_warning));
                                                Handler handler = new Handler();
                                                handler.postDelayed(new Runnable() {
                                                    public void run() {
                                                        loadingDialog.dismiss();
                                                    }
                                                }, 2000);
                                            }
                                        }
                                    } else {
                                        loadingBar.setVisibility(View.GONE);
                                        loadingText.setText("Account Is Already Registered!");
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
                            } catch (JSONException e) {
                                loadingBar.setVisibility(View.GONE);
                                loadingText.setText("JSON Response Error, Code 20");
                                loadedStatus.setVisibility(View.VISIBLE);
                                loadedStatus.setImageDrawable(getDrawable(R.drawable.ic_warning));
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        loadingDialog.dismiss();
                                    }
                                }, 2000);
                            }
                        }
                    }).execute();
                } else {
                    if (text2.length() >= 6 && text.length() <= 16) {
                        loginPassword.setError(null);
                    } else {
                        loginPassword.setError("Must Be 6-16 Characters");
                    }
                    if (text.length() >= 4 && text.length() <= 7) {
                        loginName.setError(null);
                    } else {
                        loginName.setError("Must Be 4-7 Characters");
                    }
                    if (extraName.getText().toString().length() == 0) {
                        extraName.setError("Must Not Be Empty");
                    } else {
                        extraName.setError(null);
                    }
                }
            }
        });
        //Commands
        loginName.setError(null);
        loginPassword.setError(null);
        extraName.setError(null);
        //Show The Main Login View
        setContentView(main);
    }

    private void mainScreen() {
        //TODO actual stuff
        LinearLayout fullScreen = new LinearLayout(this);
        getWindow().setStatusBarColor(secolor);
        getWindow().setNavigationBarColor(color);
        final FrameLayout content = new FrameLayout(this);
        final LinearLayout navbar = new LinearLayout(this);
        final LinearLayout navbarItems = new LinearLayout(this);
        final ImageView mainIcon = new ImageView(this);
        final ImageView liveIcon = new ImageView(this);
        groupIcon=new ImageView(this);
        final FrameLayout livePadder = new FrameLayout(this);
        ScrollView contentScroll = new ScrollView(this);
        contentScroll.addView(content);
        final int screenY = Light.Device.screenY(this);
        final int nutSize = (screenY / 7) - screenY / 30;
        final int newsSize = (screenY / 9) - screenY / 30;
        final int navY = screenY / 8;
        final LinearLayout.LayoutParams iconParms = new LinearLayout.LayoutParams(nutSize, nutSize);
        final LinearLayout.LayoutParams smallIconParms = new LinearLayout.LayoutParams(newsSize, newsSize);
        final LinearLayout.LayoutParams navParms = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, navY);
        content.setBackgroundColor(color);
        content.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        fullScreen.setOrientation(LinearLayout.VERTICAL);
        fullScreen.setGravity(Gravity.START | Gravity.CENTER_HORIZONTAL);
        fullScreen.setBackgroundColor(color);
        navbar.setBackgroundColor(secolor);
        navbar.setOrientation(LinearLayout.HORIZONTAL);
        navbar.setGravity(Gravity.CENTER);
        navbar.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        mainIcon.setLayoutParams(iconParms);
        mainIcon.setImageDrawable(getDrawable(R.drawable.ic_icon));
        mainIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder ab = new AlertDialog.Builder(MainScreen.this);
                ab.setTitle(R.string.app_name);
                ab.setMessage("This App Was Made By:\nNadav Tasher #11633\nShirelle Danon #11635\nVersion: " + Light.Device.getVersionName(getApplicationContext(), getPackageName()) + "\nBuild: " + Light.Device.getVersionCode(getApplicationContext(), getPackageName()));
                ab.setCancelable(true);
                ab.setPositiveButton("Close", null);
                ab.show();
            }
        });
        liveIcon.setLayoutParams(smallIconParms);
        livePadder.setBackground(getDrawable(R.drawable.back));
        liveIcon.setImageDrawable(getDrawable(R.drawable.ic_live));
        liveIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO go to live scores and updates(messages)
                //TODO write this part(Shirelle)
            }
        });
        livePadder.addView(liveIcon);
        ObjectAnimator liveFlash = ObjectAnimator.ofFloat(liveIcon, View.ALPHA, 1f, 0.1f);
        liveFlash.setDuration(800);
        liveFlash.setRepeatMode(ObjectAnimator.REVERSE);
        liveFlash.setRepeatCount(ObjectAnimator.INFINITE);
        liveFlash.start();
        groupIcon.setLayoutParams(smallIconParms);
        groupIcon.setBackground(getDrawable(R.drawable.back));
        groupIcon.setImageDrawable(getDrawable(R.drawable.ic_magnifying));
        groupIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSearch(content);
            }
        });
        navbarItems.setOrientation(LinearLayout.HORIZONTAL);
        navbarItems.setGravity(Gravity.CENTER);
        navbar.addView(navbarItems);
        navbar.setPadding(10, 10, 10, 10);
        navbar.setLayoutParams(navParms);
        navbar.setGravity(Gravity.CENTER);
        navbarItems.addView(livePadder);
        navbarItems.addView(mainIcon);
        navbarItems.addView(groupIcon);
        fullScreen.addView(navbar);
        fullScreen.addView(contentScroll);
        setContentView(fullScreen);
        loadAccountData(content);
    }

    private void openSearch(final FrameLayout content) {
        groupIcon.setImageDrawable(getDrawable(R.drawable.ic_home));
        groupIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadAccountData(content);
            }
        });
        LinearLayout fullSearch = new LinearLayout(this);
        LinearLayout searchBar = new LinearLayout(this);
        final LinearLayout results = new LinearLayout(this);
        fullSearch.setOrientation(LinearLayout.VERTICAL);
        fullSearch.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.START);
        results.setOrientation(LinearLayout.VERTICAL);
        results.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.START);
        searchBar.setOrientation(LinearLayout.HORIZONTAL);
        searchBar.setGravity(Gravity.CENTER);
        final TextView comment = new TextView(this);
        comment.setTypeface(getTypeface());
        comment.setTextSize(25);
        comment.setGravity(Gravity.CENTER);
        comment.setTextColor(Color.WHITE);
        comment.setText("Search By:\nID e.g. 11633\nName e.g. MetalRock");
        final EditText search = new EditText(this);
        search.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        //        search.setPadding(10,10,10,10);
        searchBar.setPadding(10, 10, 10, 10);
        searchBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        searchBar.addView(search);
        fullSearch.addView(searchBar);
        fullSearch.addView(results);
        results.addView(comment);
        search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        search.setTextSize(30);
        search.setLines(1);
        search.setHint("Type Here To Search");
        search.setGravity(Gravity.CENTER);
        search.setInputType(InputType.TYPE_CLASS_TEXT);
        results.setPadding(10, 10, 10, 10);
        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    //            comment.setVisibility(View.GONE);
                    ArrayList<Light.Net.PHP.Post.PHPParameter> searchP = new ArrayList<>();
                    searchP.add(new Light.Net.PHP.Post.PHPParameter("type", "id"));
                    searchP.add(new Light.Net.PHP.Post.PHPParameter("client", client));
                    searchP.add(new Light.Net.PHP.Post.PHPParameter("version", String.valueOf(Light.Device.getVersionCode(getApplicationContext(), getPackageName()))));
                    new Light.Net.PHP.Post(serviceSearch, searchP, new Light.Net.PHP.Post.OnPost() {
                        @Override
                        public void onPost(String s) {
                            try {
                                JSONObject response = new JSONObject(s);
                                boolean success = response.getBoolean("success");
                                if (success) {
                                    results.removeAllViews();
                                    JSONArray groups = response.getJSONArray("list");
                                    if (!search.getText().toString().equals("")) {
                                        for (int g = 0; g < groups.length(); g++) {
                                            if (groups.getString(g).contains(search.getText().toString())) {
                                                results.addView(getGroupSearch(groups.getString(g), content));
                                            }
                                        }
                                    }
                                    search.setText(null);
                                    if (results.getChildCount() == 0) {
                                        results.addView(comment);
                                    }
                                }
                            } catch (JSONException e) {
                                resetPopup("Failed Reading Data From Server", 21);
                            }
                        }
                    }).execute();
                    return true;
                }
                return false;
            }
        });
        content.removeAllViews();
        content.addView(fullSearch);
    }

    private void loadAccountData(final FrameLayout content) {
        groupIcon.setImageDrawable(getDrawable(R.drawable.ic_magnifying));
        groupIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSearch(content);
            }
        });
        final LinearLayout fullTable = new LinearLayout(getApplicationContext());
        TextView noData = new TextView(getApplicationContext());
        noData.setTypeface(getTypeface());
        noData.setGravity(Gravity.CENTER);
        noData.setTextSize(32);
        noData.setText("No Data");
        noData.setTextColor(Color.WHITE);
        Button addGroups=new Button(this);
        //TODO add That button
        fullTable.addView(noData);
        fullTable.setOrientation(LinearLayout.VERTICAL);
        fullTable.setGravity(Gravity.CENTER);
        ArrayList<Light.Net.PHP.Post.PHPParameter> readFilePara = new ArrayList<>();
        readFilePara.add(new Light.Net.PHP.Post.PHPParameter("login", sp.getString("account", "")));
        readFilePara.add(new Light.Net.PHP.Post.PHPParameter("key", sp.getString("key", "")));
        readFilePara.add(new Light.Net.PHP.Post.PHPParameter("action", "read"));
        readFilePara.add(new Light.Net.PHP.Post.PHPParameter("tag", "groups"));
        readFilePara.add(new Light.Net.PHP.Post.PHPParameter("file", "scd"));
        readFilePara.add(new Light.Net.PHP.Post.PHPParameter("filters", ""));
        readFilePara.add(new Light.Net.PHP.Post.PHPParameter("client", client));
        readFilePara.add(new Light.Net.PHP.Post.PHPParameter("version", String.valueOf(Light.Device.getVersionCode(getApplicationContext(), getPackageName()))));
        final Light.Net.PHP.Post getGroups = new Light.Net.PHP.Post(serviceLogin, readFilePara, new Light.Net.PHP.Post.OnPost() {
            @Override
            public void onPost(String s) {
                try {
                    JSONObject response = new JSONObject(s);
                    boolean success = response.getBoolean("success");
                    if (success) {
                        fullTable.removeAllViews();
                        fullTable.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
                        String result = response.getString("result");
                        JSONArray groups = new JSONArray(result);
                        alreadyScouting = groups;
                        for (int g = 0; g < groups.length(); g++) {
                            fullTable.addView(getGroupListView(groups.getString(g),content));
                        }
                    }
                } catch (JSONException e) {
                    resetPopup("Failed Reading Data From Server", 21);
                }
            }
        });
        ArrayList<Light.Net.PHP.Post.PHPParameter> checkFilePara = new ArrayList<>();
        checkFilePara.add(new Light.Net.PHP.Post.PHPParameter("login", sp.getString("account", "")));
        checkFilePara.add(new Light.Net.PHP.Post.PHPParameter("key", sp.getString("key", "")));
        checkFilePara.add(new Light.Net.PHP.Post.PHPParameter("action", "checkFile"));
        checkFilePara.add(new Light.Net.PHP.Post.PHPParameter("file", "scd"));
        checkFilePara.add(new Light.Net.PHP.Post.PHPParameter("filters", ""));
        checkFilePara.add(new Light.Net.PHP.Post.PHPParameter("client", client));
        checkFilePara.add(new Light.Net.PHP.Post.PHPParameter("version", String.valueOf(Light.Device.getVersionCode(getApplicationContext(), getPackageName()))));
        new Light.Net.PHP.Post(serviceLogin, checkFilePara, new Light.Net.PHP.Post.OnPost() {
            @Override
            public void onPost(String s) {
                try {
                    JSONObject o = new JSONObject(s);
                    boolean success = o.getBoolean("success");
                    if (success) {
                        if (o.getString("file").equals("scd")) {
                            getGroups.execute();
                        }
                    }
                } catch (JSONException e) {
                    resetPopup("Failed Reading Date From Server", 22);
                }
            }
        }).execute();
        noData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONArray groups = new JSONArray();
                groups.put("11344");
                groups.put("16332");
                groups.put("11235");
                groups.put("45351");
                ArrayList<Light.Net.PHP.Post.PHPParameter> readFilePara = new ArrayList<>();
                readFilePara.add(new Light.Net.PHP.Post.PHPParameter("login", sp.getString("account", "")));
                readFilePara.add(new Light.Net.PHP.Post.PHPParameter("key", sp.getString("key", "")));
                readFilePara.add(new Light.Net.PHP.Post.PHPParameter("action", "write"));
                readFilePara.add(new Light.Net.PHP.Post.PHPParameter("tag", "groups"));
                readFilePara.add(new Light.Net.PHP.Post.PHPParameter("file", "scd"));
                readFilePara.add(new Light.Net.PHP.Post.PHPParameter("value", groups.toString()));
                readFilePara.add(new Light.Net.PHP.Post.PHPParameter("version", String.valueOf(Light.Device.getVersionCode(getApplicationContext(), getPackageName()))));
                new Light.Net.PHP.Post(serviceLogin, readFilePara, new Light.Net.PHP.Post.OnPost() {
                    @Override
                    public void onPost(String s) {
                        try {
                            JSONObject response = new JSONObject(s);
                            boolean success = response.getBoolean("success");
                            if (success) {
                                if (response.getBoolean("wrote")) {
                                    resetPopup("Wrote Data!", -20);
                                }
                            }
                        } catch (JSONException e) {
                            resetPopup("Failed Reading Date From Server", 23);
                        }
                    }
                }).execute();
            }
        });
        fullTable.setPadding(20, 20, 20, 20);
        content.removeAllViews();
        content.addView(fullTable);
    }

    LinearLayout getGroupSearch(final String id, final FrameLayout content) {
        LinearLayout group = new LinearLayout(this);
        LinearLayout row1 = new LinearLayout(this);
        LinearLayout row2 = new LinearLayout(this);
        LinearLayout row2right = new LinearLayout(this);
        LinearLayout.LayoutParams buttonParms = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Light.Device.screenY(this) / 12);
        group.setOrientation(LinearLayout.VERTICAL);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2right.setOrientation(LinearLayout.HORIZONTAL);
        group.setPadding(20, 20, 20, 20);
        group.setGravity(Gravity.CENTER);
        row1.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        row1.setPadding(10, 10, 10, 10);
        row2.setGravity(Gravity.CENTER);
        TextView gid = new TextView(this), aka = new TextView(this);
        Button add = new Button(this);
        gid.setText("#" + id);
        gid.setTypeface(getTypeface());
        gid.setTextSize(40);
        gid.setTextColor(Color.WHITE);
        aka.setText(null);
        aka.setTypeface(getTypeface());
        aka.setTextSize(24);
        aka.setTextColor(Color.WHITE - 0x222222);
        row1.addView(gid);
        row1.addView(aka);
        add.setText("Add");
        add.setBackground(getDrawable(R.drawable.button));
        add.setTypeface(getTypeface());
        add.setTextSize(25);
        add.setPadding(10, 10, 10, 10);
        add.setTextColor(Color.parseColor("#22dd22"));
        add.setAllCaps(false);
        add.setLayoutParams(buttonParms);
        row2.addView(add);
        group.addView(row1);
        group.addView(row2);
        group.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Light.Device.screenY(this) / 5));
        group.setBackground(getDrawable(R.drawable.back_2));
        try {
            if (alreadyScouting != null) {
                for (int t = 0; t < alreadyScouting.length(); t++) {
                    if (alreadyScouting.getString(t).equals(id)) {
                        add.setVisibility(View.GONE);
                        group.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Light.Device.screenY(this) / 10));
                        break;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        add.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                addGroup(id, new DoAfter() {
                    @Override
                    public void doAfter() {
                        loadAccountData(content);
                    }
                });
            }
        });
        setAkaOnTextView(aka, id);
        return group;
    }

    private void addGroup(String id, final DoAfter doAfter) {
        final ArrayList<Light.Net.PHP.Post.PHPParameter> writeData = new ArrayList<>();
        writeData.add(new Light.Net.PHP.Post.PHPParameter("login", sp.getString("account", "")));
        writeData.add(new Light.Net.PHP.Post.PHPParameter("key", sp.getString("key", "")));
        writeData.add(new Light.Net.PHP.Post.PHPParameter("action", "write"));
        writeData.add(new Light.Net.PHP.Post.PHPParameter("tag", "groups"));
        writeData.add(new Light.Net.PHP.Post.PHPParameter("file", "scd"));
        writeData.add(new Light.Net.PHP.Post.PHPParameter("version", String.valueOf(Light.Device.getVersionCode(getApplicationContext(), getPackageName()))));
        final Light.Net.PHP.Post writeNew = new Light.Net.PHP.Post(serviceLogin, writeData, new Light.Net.PHP.Post.OnPost() {
            @Override
            public void onPost(String s) {
                try {
                    JSONObject response = new JSONObject(s);
                    boolean success = response.getBoolean("success");
                    if (success) {
                        if (response.getBoolean("wrote")) {
                            doAfter.doAfter();
                        }
                    }
                } catch (JSONException e) {
                    resetPopup("Failed Reading Date From Server", 23);
                }
            }
        });
        final JSONArray array = new JSONArray();
        array.put(id);
        ArrayList<Light.Net.PHP.Post.PHPParameter> readFilePara = new ArrayList<>();
        readFilePara.add(new Light.Net.PHP.Post.PHPParameter("login", sp.getString("account", "")));
        readFilePara.add(new Light.Net.PHP.Post.PHPParameter("key", sp.getString("key", "")));
        readFilePara.add(new Light.Net.PHP.Post.PHPParameter("action", "read"));
        readFilePara.add(new Light.Net.PHP.Post.PHPParameter("tag", "groups"));
        readFilePara.add(new Light.Net.PHP.Post.PHPParameter("file", "scd"));
        readFilePara.add(new Light.Net.PHP.Post.PHPParameter("filters", ""));
        readFilePara.add(new Light.Net.PHP.Post.PHPParameter("client", client));
        readFilePara.add(new Light.Net.PHP.Post.PHPParameter("version", String.valueOf(Light.Device.getVersionCode(getApplicationContext(), getPackageName()))));
        final Light.Net.PHP.Post getGroups = new Light.Net.PHP.Post(serviceLogin, readFilePara, new Light.Net.PHP.Post.OnPost() {
            @Override
            public void onPost(String s) {
                try {
                    JSONObject response = new JSONObject(s);
                    boolean success = response.getBoolean("success");
                    if (success) {
                        String result = response.getString("result");
                        JSONArray myarr = new JSONArray(result);
                        for (int g = 0; g < myarr.length(); g++) {
                            array.put(myarr.get(g));
                        }
                        writeData.add(new Light.Net.PHP.Post.PHPParameter("value", array.toString()));
                        writeNew.execute();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    resetPopup(e.toString(), 25);
                }
            }
        });
        ArrayList<Light.Net.PHP.Post.PHPParameter> checkFilePara = new ArrayList<>();
        checkFilePara.add(new Light.Net.PHP.Post.PHPParameter("login", sp.getString("account", "")));
        checkFilePara.add(new Light.Net.PHP.Post.PHPParameter("key", sp.getString("key", "")));
        checkFilePara.add(new Light.Net.PHP.Post.PHPParameter("action", "checkFile"));
        checkFilePara.add(new Light.Net.PHP.Post.PHPParameter("file", "scd"));
        checkFilePara.add(new Light.Net.PHP.Post.PHPParameter("filters", ""));
        checkFilePara.add(new Light.Net.PHP.Post.PHPParameter("client", client));
        checkFilePara.add(new Light.Net.PHP.Post.PHPParameter("version", String.valueOf(Light.Device.getVersionCode(getApplicationContext(), getPackageName()))));
        new Light.Net.PHP.Post(serviceLogin, checkFilePara, new Light.Net.PHP.Post.OnPost() {
            @Override
            public void onPost(String s) {
                try {
                    JSONObject o = new JSONObject(s);
                    boolean success = o.getBoolean("success");
                    if (success) {
                        if (o.getString("file").equals("scd")) {
                            getGroups.execute();
                        }
                    } else {
                        writeData.add(new Light.Net.PHP.Post.PHPParameter("value", array.toString()));
                        writeNew.execute();
                    }
                } catch (JSONException e) {
                    resetPopup("Failed Reading Data From Server", 22);
                }
            }
        }).execute();
    }

    private void removeGroup(final String id, final DoAfter doAfter) {
        final ArrayList<Light.Net.PHP.Post.PHPParameter> writeData = new ArrayList<>();
        writeData.add(new Light.Net.PHP.Post.PHPParameter("login", sp.getString("account", "")));
        writeData.add(new Light.Net.PHP.Post.PHPParameter("key", sp.getString("key", "")));
        writeData.add(new Light.Net.PHP.Post.PHPParameter("action", "write"));
        writeData.add(new Light.Net.PHP.Post.PHPParameter("tag", "groups"));
        writeData.add(new Light.Net.PHP.Post.PHPParameter("file", "scd"));
        writeData.add(new Light.Net.PHP.Post.PHPParameter("version", String.valueOf(Light.Device.getVersionCode(getApplicationContext(), getPackageName()))));
        final Light.Net.PHP.Post writeNew = new Light.Net.PHP.Post(serviceLogin, writeData, new Light.Net.PHP.Post.OnPost() {
            @Override
            public void onPost(String s) {
                try {
                    JSONObject response = new JSONObject(s);
                    boolean success = response.getBoolean("success");
                    if (success) {
                        if (response.getBoolean("wrote")) {
                            doAfter.doAfter();
                        }
                    }
                } catch (JSONException e) {
                    resetPopup("Failed Reading Date From Server", 23);
                }
            }
        });
        final JSONArray array = new JSONArray();
        ArrayList<Light.Net.PHP.Post.PHPParameter> readFilePara = new ArrayList<>();
        readFilePara.add(new Light.Net.PHP.Post.PHPParameter("login", sp.getString("account", "")));
        readFilePara.add(new Light.Net.PHP.Post.PHPParameter("key", sp.getString("key", "")));
        readFilePara.add(new Light.Net.PHP.Post.PHPParameter("action", "read"));
        readFilePara.add(new Light.Net.PHP.Post.PHPParameter("tag", "groups"));
        readFilePara.add(new Light.Net.PHP.Post.PHPParameter("file", "scd"));
        readFilePara.add(new Light.Net.PHP.Post.PHPParameter("filters", ""));
        readFilePara.add(new Light.Net.PHP.Post.PHPParameter("client", client));
        readFilePara.add(new Light.Net.PHP.Post.PHPParameter("version", String.valueOf(Light.Device.getVersionCode(getApplicationContext(), getPackageName()))));
        final Light.Net.PHP.Post getGroups = new Light.Net.PHP.Post(serviceLogin, readFilePara, new Light.Net.PHP.Post.OnPost() {
            @Override
            public void onPost(String s) {
                try {
                    JSONObject response = new JSONObject(s);
                    boolean success = response.getBoolean("success");
                    if (success) {
                        String result = response.getString("result");
                        JSONArray myarr = new JSONArray(result);
                        for (int g = 0; g < myarr.length(); g++) {
                            if (!myarr.getString(g).equals(id)) {
                                array.put(myarr.get(g));
                            }
                        }
                        writeData.add(new Light.Net.PHP.Post.PHPParameter("value", array.toString()));
                        writeNew.execute();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    resetPopup(e.toString(), 25);
                }
            }
        });
        ArrayList<Light.Net.PHP.Post.PHPParameter> checkFilePara = new ArrayList<>();
        checkFilePara.add(new Light.Net.PHP.Post.PHPParameter("login", sp.getString("account", "")));
        checkFilePara.add(new Light.Net.PHP.Post.PHPParameter("key", sp.getString("key", "")));
        checkFilePara.add(new Light.Net.PHP.Post.PHPParameter("action", "checkFile"));
        checkFilePara.add(new Light.Net.PHP.Post.PHPParameter("file", "scd"));
        checkFilePara.add(new Light.Net.PHP.Post.PHPParameter("filters", ""));
        checkFilePara.add(new Light.Net.PHP.Post.PHPParameter("client", client));
        checkFilePara.add(new Light.Net.PHP.Post.PHPParameter("version", String.valueOf(Light.Device.getVersionCode(getApplicationContext(), getPackageName()))));
        new Light.Net.PHP.Post(serviceLogin, checkFilePara, new Light.Net.PHP.Post.OnPost() {
            @Override
            public void onPost(String s) {
                try {
                    JSONObject o = new JSONObject(s);
                    boolean success = o.getBoolean("success");
                    if (success) {
                        if (o.getString("file").equals("scd")) {
                            getGroups.execute();
                        }
                    } else {
                        writeData.add(new Light.Net.PHP.Post.PHPParameter("value", array.toString()));
                        writeNew.execute();
                    }
                } catch (JSONException e) {
                    resetPopup("Failed Reading Data From Server", 22);
                }
            }
        }).execute();
    }

    LinearLayout getGroupListView(final String id, final FrameLayout content) {
        LinearLayout group = new LinearLayout(this);
        LinearLayout row1 = new LinearLayout(this);
        LinearLayout row2 = new LinearLayout(this);
        LinearLayout row2right = new LinearLayout(this);
        LinearLayout.LayoutParams buttonParms = new LinearLayout.LayoutParams(Light.Device.screenX(this) / 3, Light.Device.screenY(this) / 12);
        group.setOrientation(LinearLayout.VERTICAL);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2right.setOrientation(LinearLayout.HORIZONTAL);
        group.setPadding(20, 20, 20, 20);
        group.setGravity(Gravity.CENTER);
        row1.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        row1.setPadding(10, 10, 10, 10);
        row2.setGravity(Gravity.CENTER);
        row2right.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
        TextView gid = new TextView(this), aka = new TextView(this);
        final Button remove = new Button(this), more = new Button(this);
        gid.setText("#" + id);
        gid.setTypeface(getTypeface());
        gid.setTextSize(40);
        gid.setTextColor(Color.WHITE);
        aka.setText(null);
        aka.setTypeface(getTypeface());
        aka.setTextSize(24);
        aka.setTextColor(Color.WHITE - 0x222222);
        row1.addView(gid);
        row1.addView(aka);
        remove.setText("Remove");
        remove.setBackground(getDrawable(R.drawable.button));
        remove.setTypeface(getTypeface());
        remove.setTextSize(25);
        remove.setPadding(10, 10, 10, 10);
        remove.setTextColor(Color.parseColor("#bb2222"));
        remove.setAllCaps(false);
        remove.setLayoutParams(buttonParms);
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeGroup(id, new DoAfter() {
                    @Override
                    public void doAfter() {
                        loadAccountData(content);
                    }
                });
            }
        });
        more.setText("More");
        more.setTextColor(Color.parseColor("#2222bb"));
        more.setAllCaps(false);
        more.setBackground(getDrawable(R.drawable.button));
        more.setTypeface(getTypeface());
        more.setTextSize(25);
        more.setPadding(10, 10, 10, 10);
        more.setLayoutParams(buttonParms);
        row2.addView(remove);
        row2right.addView(more);
        group.addView(row1);
        group.addView(row2);
        row2.addView(row2right);
        row2right.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        group.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Light.Device.screenY(this) / 5));
        group.setBackground(getDrawable(R.drawable.back_2));
        setAkaOnTextView(aka, id);
        return group;
    }

    void setAkaOnTextView(final TextView aka, String id) {
        if (aka.getText().toString().equals("")) {
            final String soFar = " aka ";
            ArrayList<Light.Net.PHP.Post.PHPParameter> akaGet = new ArrayList<>();
            akaGet.add(new Light.Net.PHP.Post.PHPParameter("login", id));
            akaGet.add(new Light.Net.PHP.Post.PHPParameter("action", "readPublic"));
            akaGet.add(new Light.Net.PHP.Post.PHPParameter("tag", "name"));
            akaGet.add(new Light.Net.PHP.Post.PHPParameter("version", String.valueOf(Light.Device.getVersionCode(getApplicationContext(), getPackageName()))));
            new Light.Net.PHP.Post(serviceLogin, akaGet, new Light.Net.PHP.Post.OnPost() {
                @Override
                public void onPost(String s) {
                    try {
                        JSONObject result = new JSONObject(s);
                        if (result.getBoolean("success")) {
                            aka.setText(soFar + result.getString("result"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).execute();
        }
    }

    interface DoAfter {
        void doAfter();
    }
}
