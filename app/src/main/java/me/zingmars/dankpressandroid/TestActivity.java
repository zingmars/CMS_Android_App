/**
 * Created by zingmars on 28.10.2015.
 */
package me.zingmars.dankpressandroid;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class TestActivity extends AppCompatActivity {
    Utilities utils = new Utilities();

    class POSTer implements Runnable {
        POSTer() {
        }
        public void run() {
            Log.i("App", "Button pressed.");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        App.createMenu(this, 5);

        Button button = (Button) findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                POSTer poster = new POSTer();
                Thread thread = new Thread(poster);
                thread.start();
            }
        });
    }
}
