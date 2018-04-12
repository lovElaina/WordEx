package developer.zyc.wordex;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    private SharedPreferences.Editor YYY; //SAVE

    private SharedPreferences XXX; //OPEN


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        XXX= PreferenceManager.getDefaultSharedPreferences(this);
        boolean isfirstopen = XXX.getBoolean("first",true);

        if(!isfirstopen){
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        YYY = XXX.edit();
        YYY.putBoolean("first",false);
        YYY.apply();
        Intent intent = new Intent(this,WelcomeActivity.class);
        startActivity(intent);
        finish();
    }
}
