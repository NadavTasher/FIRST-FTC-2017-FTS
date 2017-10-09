package themetalrock.x.quackattack2.ftcscouting;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import nadav.tasher.lightool.Light;

public class MainScreen extends Activity {
    private int color= Color.parseColor("#6699CC");
    private final String serviceProvider="http://ftc.thepuzik.com";
    private final String servicePush=serviceProvider+"/push/push.php";
    private final String serviceLogin=serviceProvider+"/sign/in.php";
    private final String serviceNews=serviceProvider+"/news/news.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }
    private void init(){
        splash();
        mainScreen();
    }
    private void resetPopup(String error,int errorCode){
        AlertDialog.Builder adb=new AlertDialog.Builder(this);
        adb.setTitle("Fatal Error");
        adb.setMessage(error+"\nCode: "+String.valueOf(errorCode));
        adb.setPositiveButton("Reset", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                init();
            }
        });
        adb.setCancelable(false);
        adb.show();
    }
    private void splash(){
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
                if(s.equals(serviceProvider)&&b){

                }else{
                    resetPopup("No Response From Service Provider",10);
                }
            }
        }).execute(serviceProvider);
    }
    private void mainScreen(){

    }
}
