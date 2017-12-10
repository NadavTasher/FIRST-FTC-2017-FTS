package fantastic.fourmula.ftcscouting;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

import fantastic.fourmula.ftscouting.R;
import nadav.tasher.accounts.AccountServices;
import nadav.tasher.lightool.Light;

public class MainScreen extends Activity {
    static final String errorCodeManual = "Error Code Manual:\n" + "0-20 Connection Errors\n" + "20-40 JSON Errors, Server Response Errors\n" + "" + "" + "" + "" + "" + "" + "";
    private final String serviceProvider = "http://ftc.thepuzik.com";
    private final String servicePush = serviceProvider + "/push/push.php";
    private final String serviceLogin = serviceProvider + "/sign/login.php";
    private final String serviceSearch = serviceProvider + "/sign/search.php";
    private final String serviceNews = serviceProvider + "/news/news.php";
    private final String formatFile = serviceProvider + "/scouting/format.json";
    private final String client = "FTSAndroid";
    private SharedPreferences sp;
    private int color = Color.parseColor("#041228");
    private int secolor = color + 0x333333;
    private JSONArray alreadyScouting;
    private ImageView groupIcon;
    int textBlack = Color.WHITE;
    int textWhite = Color.WHITE;
    private AccountServices as;
    ArrayList<Template> temps = new ArrayList<>();
    String format;

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
        as.login(getApplicationContext(), sp.getString("account", ""), sp.getString("key", ""), new AccountServices.OnLogin() {
            @Override
            public void loginSucceded(String s, String s1) {
                mainScreen();
            }

            @Override
            public void wrongPassword(String s) {
                firstLogin(sp.getString("account", null));
            }

            @Override
            public void noAccount(String s) {
                firstLogin(sp.getString("account", null));
            }
        });
    }

    private void init() {
        new Light.Net.NetFile.FileReader(formatFile, new Light.Net.NetFile.FileReader.OnEnd() {
            @Override
            public void onFileRead(InputStream inputStream) {
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                    String nl;
                    while ((nl = br.readLine()) != null) {
                        if (format != null) {
                            format += "\n" + nl;
                        } else {
                            format = nl;
                        }
                    }
                    try {
                        JSONObject reader = new JSONObject(format);
                        JSONObject config = reader.getJSONObject("format");
                        Iterator<String> types = config.keys();
                        while (types.hasNext()) {
                            String name = types.next();
                            temps.add(new Template(name, config.getJSONArray(name)));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                }
            }
        }).execute();
        sp = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        as = new AccountServices(serviceLogin, "FTSAndroid", "FTS", new AccountServices.OnLogin() {
            @Override
            public void loginSucceded(String s, String s1) {
                mainScreen();
            }

            @Override
            public void wrongPassword(String s) {
                firstLogin(s);
            }

            @Override
            public void noAccount(String s) {
                firstLogin(s);
            }
        }, new AccountServices.OnSignup() {
            @Override
            public void signupSucceded(String s, String s1) {
            }

            @Override
            public void alreadyRegistered(String s) {
            }
        });
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
        final InputFilter teamNameFilter = new InputFilter() {

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
        ImageView mainIcon, tmrIcon;
        final EditText loginName, loginPassword;
        TextView madebyText, withText;
        final Button signup, login;
        //Initialize Widgets
        mainIcon = new ImageView(getApplicationContext());
        tmrIcon = new ImageView(getApplicationContext());
        loginName = new EditText(getApplicationContext());
        loginPassword = new EditText(getApplicationContext());
        signup = new Button(getApplicationContext());
        login = new Button(getApplicationContext());
        madebyText = new TextView(getApplicationContext());
        withText = new TextView(getApplicationContext());
        //Assign Values
        main.setBackgroundColor(color);
        mainIcon.setImageDrawable(getDrawable(R.drawable.ic_icon));
        tmrIcon.setImageDrawable(getDrawable(R.drawable.ic_fantastic));
        loginName.setFilters(new InputFilter[]{groupIDfilter});
        loginPassword.setFilters(new InputFilter[]{groupPasswordfilter});
        loginName.setHint("Group ID, e.g '11633'");
        loginPassword.setHint("Password of 6-16 Characters");
        loginName.setTextSize(25);
        loginPassword.setTextSize(21);
        loginPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        loginName.setTypeface(getTypeface());
        loginPassword.setTypeface(getTypeface());
        login.setText(R.string.login);
        signup.setText(R.string.signup);
        loginName.setHintTextColor(textBlack);
        loginPassword.setHintTextColor(textBlack);
        loginName.setTextColor(textBlack);
        loginPassword.setTextColor(textBlack);
        loginName.setGravity(Gravity.CENTER);
        loginPassword.setGravity(Gravity.CENTER);
        loginPassword.setError("Must Use 6-16 Chars");
        loginName.setText(account);
        madebyText.setText(R.string.madeby);
        madebyText.setTypeface(getTypeface());
        madebyText.setGravity(Gravity.CENTER);
        madebyText.setTextSize(38);
        madebyText.setTextColor(textBlack);
        withText.setText(R.string.with);
        withText.setGravity(Gravity.CENTER);
        withText.setTextSize(60);
        withText.setTextColor(textBlack);
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
        main.addView(mainIcon);
        main.addView(loginView);
        main.addView(madebyText);
//        main.addView(madebyView);
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
                    as.login(getApplicationContext(), loginName.getText().toString(), loginPassword.getText().toString(), new AccountServices.OnLogin() {
                        @Override
                        public void loginSucceded(final String s, final String s1) {
                            loadingBar.setVisibility(View.GONE);
                            loadingText.setText("Login Success!");
                            loadedStatus.setVisibility(View.VISIBLE);
                            loadedStatus.setImageDrawable(getDrawable(R.drawable.ic_accept));
                            sp.edit().putString("account", s).putString("key", s1).commit();
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                public void run() {
                                    loadingDialog.dismiss();
                                    mainScreen();
                                }
                            }, 2000);
                        }

                        @Override
                        public void wrongPassword(String s) {
                            loadingBar.setVisibility(View.GONE);
                            loadingText.setText("Login Failed, Wrong Credentials.");
                            loadedStatus.setVisibility(View.VISIBLE);
                            loadedStatus.setImageDrawable(getDrawable(R.drawable.ic_decline));
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                public void run() {
                                    loadingDialog.dismiss();
                                }
                            }, 2000);
                        }

                        @Override
                        public void noAccount(String s) {
                            loadingBar.setVisibility(View.GONE);
                            loadingText.setText("Login Failed, No Such Account.");
                            loadedStatus.setVisibility(View.VISIBLE);
                            loadedStatus.setImageDrawable(getDrawable(R.drawable.ic_decline));
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                public void run() {
                                    loadingDialog.dismiss();
                                }
                            }, 2000);
                        }
                    });
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
                    AlertDialog.Builder adb = new AlertDialog.Builder(MainScreen.this);
                    adb.setTitle("Team Name");
                    final EditText extraName = new EditText(getApplicationContext());
                    extraName.setTypeface(getTypeface());
                    extraName.setHint("Team's Name Goes Here");
                    extraName.setTextSize(24);
                    extraName.setFilters(new InputFilter[]{teamNameFilter});
                    adb.setView(extraName);
                    adb.setPositiveButton("Sign Up", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            as.signup(getApplicationContext(), loginName.getText().toString(), loginPassword.getText().toString(), new AccountServices.OnSignup() {
                                @Override
                                public void signupSucceded(String s, String s1) {
                                    loadingBar.setVisibility(View.GONE);
                                    loadingText.setText("Sign-Up Succeeded.");
                                    loadedStatus.setVisibility(View.VISIBLE);
                                    loadedStatus.setImageDrawable(getDrawable(R.drawable.ic_accept));
                                    sp.edit().putString("account", s).putString("key", s1).commit();
                                    as.writePublic(getApplicationContext(), s, s1, "name", extraName.getText().toString(), new AccountServices.OnWrite() {
                                        @Override
                                        public void onWrite() {
                                        }

                                        @Override
                                        public void onFail() {
                                        }
                                    });
                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        public void run() {
                                            loadingDialog.dismiss();
                                            mainScreen();
                                        }
                                    }, 2000);
                                }

                                @Override
                                public void alreadyRegistered(String s) {
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
                            });
                        }
                    });
                    adb.setNegativeButton("Cancel", null);
                    adb.show();
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
        //Commands
        loginName.setError(null);
        loginPassword.setError(null);
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
        groupIcon = new ImageView(this);
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
                Toast.makeText(getApplicationContext(), "Coming Soon...", Toast.LENGTH_LONG).show();
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
        final Button addGroups = new Button(this);
        //TODO add That button
        addGroups.setText("Add Team To Scouting List");
        addGroups.setBackground(getDrawable(R.drawable.back_2));
        addGroups.setTextColor(Color.parseColor("#22dd22"));
        addGroups.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Light.Device.screenY(this) / 10));
        addGroups.setTextSize(25);
        addGroups.setTypeface(getTypeface());
        addGroups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSearch(content);
            }
        });
        fullTable.addView(addGroups);
        fullTable.setOrientation(LinearLayout.VERTICAL);
        fullTable.setGravity(Gravity.CENTER);
        as.read(getApplicationContext(), sp.getString("account", ""), sp.getString("key", ""), "scd", "groups", new AccountServices.OnRead() {
            @Override
            public void onRead(String s) {
                try {
                    fullTable.removeAllViews();
                    fullTable.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
                    JSONArray groups = new JSONArray(s);
                    alreadyScouting = groups;
                    for (int g = 0; g < groups.length(); g++) {
                        fullTable.addView(getGroupListView(groups.getString(g), content));
                    }
                    fullTable.addView(addGroups);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail() {
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
                        group.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Light.Device.screenY(this) / 9));
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
        final JSONArray array = new JSONArray();
        array.put(id);
        as.read(getApplicationContext(), sp.getString("account", ""), sp.getString("key", ""), "scd", "groups", new AccountServices.OnRead() {
            @Override
            public void onRead(String s) {
                try {
                    JSONArray myarr = new JSONArray(s);
                    for (int g = 0; g < myarr.length(); g++) {
                        array.put(myarr.get(g));
                    }
                    as.write(getApplicationContext(), sp.getString("account", ""), sp.getString("key", ""), "scd", "groups", array.toString(), new AccountServices.OnWrite() {
                        @Override
                        public void onWrite() {
                            doAfter.doAfter();
                        }

                        @Override
                        public void onFail() {
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail() {
                as.write(getApplicationContext(), sp.getString("account", ""), sp.getString("key", ""), "scd", "groups", array.toString(), new AccountServices.OnWrite() {
                    @Override
                    public void onWrite() {
                        doAfter.doAfter();
                    }

                    @Override
                    public void onFail() {
                    }
                });
            }
        });
    }

    private void removeGroup(final String id, final DoAfter doAfter) {
        as.read(getApplicationContext(), sp.getString("account", ""), sp.getString("key", ""), "scd", "groups", new AccountServices.OnRead() {
            @Override
            public void onRead(String s) {
                try {
                    JSONArray myarr = new JSONArray(s);
                    for (int g = 0; g < myarr.length(); g++) {
                        if (myarr.getString(g).equals(id)) {
                            myarr.remove(g);
                        }
                    }
                    as.write(getApplicationContext(), sp.getString("account", ""), sp.getString("key", ""), "scd", "groups", myarr.toString(), new AccountServices.OnWrite() {
                        @Override
                        public void onWrite() {
                            doAfter.doAfter();
                        }

                        @Override
                        public void onFail() {
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail() {
            }
        });
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
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewGroup(id, content);
            }
        });
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

    void viewGroup(final String id, final FrameLayout content) {
        groupIcon.setImageDrawable(getDrawable(R.drawable.ic_home));
        groupIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadAccountData(content);
            }
        });
        final LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(Gravity.CENTER);
        as.checkFile(getApplicationContext(), sp.getString("account", null), sp.getString("key", null), id, new AccountServices.OnCheck() {
            @Override
            public void onCheck(boolean b) {
                if (b) {
                    as.read(getApplicationContext(), sp.getString("account", null), sp.getString("key", null), id, "config", new AccountServices.OnRead() {
                        @Override
                        public void onRead(String s) {
                            Log.i("JSON", s);
                            try {
                                JSONObject teamConf = new JSONObject(s);
                                for (int type = 0; type < temps.size(); type++) {
                                    Template t = temps.get(type);
                                    ll.addView(getTemplate(t, teamConf, id, content));
                                }
                            } catch (JSONException e) {
                            }
                        }

                        @Override
                        public void onFail() {
                        }
                    });
                } else {
                    for (int type = 0; type < temps.size(); type++) {
                        Template t = temps.get(type);
                        ll.addView(getTemplate(t, null, id, content));
                    }
                }
            }

            @Override
            public void onFail() {
            }
        });
        content.removeAllViews();
        content.addView(ll);
    }

    void saveTeamConfig(final String group, final String tochange, final String value, final FrameLayout content) {
        as.read(getApplicationContext(), sp.getString("account", null), sp.getString("key", null), group, "config", new AccountServices.OnRead() {
            @Override
            public void onRead(String s) {
                try {
                    JSONObject saveable;
                    if (s != null || !s.equals("")) {
                        saveable = new JSONObject(s);
                    } else {
                        saveable = new JSONObject();
                    }
                    saveable.put(tochange, value);
                    as.write(getApplicationContext(), sp.getString("account", null), sp.getString("key", null), group, "config", saveable.toString(), new AccountServices.OnWrite() {
                        @Override
                        public void onWrite() {
                            viewGroup(group, content);
                        }

                        @Override
                        public void onFail() {
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail() {
                try {
                    JSONObject saveable = new JSONObject();
                    saveable.put(tochange, value);
                    as.write(getApplicationContext(), sp.getString("account", null), sp.getString("key", null), group, "config", saveable.toString(), new AccountServices.OnWrite() {
                        @Override
                        public void onWrite() {
                            viewGroup(group, content);
                        }

                        @Override
                        public void onFail() {
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    void loadOptions(Template t, ArrayList<String> st) {
        st.addAll(t.options);
    }

    LinearLayout getTemplate(final Template t, JSONObject teamConfig, final String gid, final FrameLayout content) {
        final ArrayList<String> nopt = new ArrayList<>();
        loadOptions(t, nopt);
        int selection = -1;
        if (teamConfig != null) {
            if (teamConfig.has(t.name)) {
                try {
                    String s = teamConfig.getString(t.name);
                    for (int o = 0; o < nopt.size(); o++) {
                        if (nopt.get(o).equalsIgnoreCase(s)) {
                            selection = o;
                            break;
                        }
                    }
                    if (selection == -1) {
                        nopt.add(teamConfig.getString(t.name));
                        selection = nopt.size() - 2;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                selection = 0;
            }
        } else {
            selection = 0;
        }
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(Gravity.CENTER);
        TextView name = new TextView(this);
        name.setTypeface(getTypeface());
        name.setTextSize(30);
        name.setTextColor(Color.WHITE);
        name.setText(t.name);
        ll.setBackground(getDrawable(R.drawable.back_transparant));
        final Button b = new Button(this);
        b.setText(nopt.get(selection));
        b.setTypeface(getTypeface());
        b.setTextSize(25);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setBackground(getDrawable(R.drawable.button));
        name.setGravity(Gravity.CENTER);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog d = new Dialog(MainScreen.this);
                LinearLayout lld = new LinearLayout(getApplicationContext());
                lld.setGravity(Gravity.CENTER);
                lld.setOrientation(LinearLayout.VERTICAL);
                lld.setBackgroundColor(color);
                lld.setPadding(10, 10, 10, 10);
                Button other = new Button(getApplicationContext());
                other.setTextSize(25);
                other.setAllCaps(false);
                other.setTextColor(Color.WHITE);
                other.setTypeface(getTypeface());
                other.setText("Other");
                other.setPadding(10, 10, 10, 10);
                other.setBackground(getDrawable(R.drawable.button));
                other.setLayoutParams(new LinearLayout.LayoutParams((int) (Light.Device.screenX(getApplicationContext()) * 0.8), (int) (Light.Device.screenY(getApplicationContext()) * 0.1)));
                other.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder adb = new AlertDialog.Builder(MainScreen.this);
                        adb.setTitle("Other");
                        final EditText extraName = new EditText(getApplicationContext());
                        extraName.setTypeface(getTypeface());
                        extraName.setTextSize(24);
                        adb.setView(extraName);
                        adb.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                saveTeamConfig(gid, t.name, extraName.getText().toString(), content);
                                d.dismiss();
                            }
                        });
                        adb.setNegativeButton("Cancel", null);
                        adb.show();
                    }
                });
                for (int op = 0; op < nopt.size(); op++) {
                    Button opti = new Button(getApplicationContext());
                    opti.setLayoutParams(new LinearLayout.LayoutParams((int) (Light.Device.screenX(getApplicationContext()) * 0.8), (int) (Light.Device.screenY(getApplicationContext()) * 0.1)));
                    opti.setText(nopt.get(op));
                    opti.setTypeface(getTypeface());
                    opti.setTextColor(Color.WHITE);
                    opti.setTextSize(25);
                    opti.setAllCaps(false);
                    opti.setBackground(getDrawable(R.drawable.button));
                    final int finalOp = op;
                    opti.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            b.setText(nopt.get(finalOp));
                            d.dismiss();
                            saveTeamConfig(gid, t.name, nopt.get(finalOp), content);
                        }
                    });
                    lld.addView(opti);
                }
                lld.addView(other);
                ScrollView sv = new ScrollView(getApplicationContext());
                sv.addView(lld);
                d.setContentView(sv);
                d.setCancelable(true);
                d.show();
            }
        });
        ll.setPadding(25, 20, 25, 20);
        ll.addView(name);
        ll.addView(b);
        return ll;
    }

    void setAkaOnTextView(final TextView aka, String id) {
        if (aka.getText().toString().equals("")) {
            final String soFar = " aka ";
            as.readPublic(getApplicationContext(), id, "name", new AccountServices.OnRead() {
                @Override
                public void onRead(String s) {
                    aka.setText(soFar + s);
                }

                @Override
                public void onFail() {
                }
            });
        }
    }

    interface DoAfter {
        void doAfter();
    }

    class Template {
        String name;
        ArrayList<String> options = new ArrayList<>();

        public Template(String name, JSONArray opt) {
            this.name = name;
            for (int i = 0; i < opt.length(); i++) {
                try {
                    options.add(opt.getString(i));
                } catch (JSONException e) {
                }
            }
        }
    }
}
