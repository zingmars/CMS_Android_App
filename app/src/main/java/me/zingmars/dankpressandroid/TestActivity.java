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

            //Logout
            /*List<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
            AbstractMap.SimpleEntry<String, String> username = new AbstractMap.SimpleEntry<>("username", "yolo");
            AbstractMap.SimpleEntry<String, String> token = new AbstractMap.SimpleEntry<>("token", "93vBJqbSTCnrlXwn");
            params.add(username);
            params.add(token);
            String response = utils.HTMLPOST("http://192.168.1.2/dankpress/api/logout", params);*/

            //Login
            /*List<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
            AbstractMap.SimpleEntry<String, String> username = new AbstractMap.SimpleEntry<>("username", "yolo");
            AbstractMap.SimpleEntry<String, String> password = new AbstractMap.SimpleEntry<>("password", "swag");
            params.add(username);
            params.add(password);
            String response = utils.HTMLPOST("http://192.168.1.2/dankpress/api/login", params);
            //Split the string and save the username, the token, and the admin state
            //It doesn't matter if the user spoofs the admin login, server checks for rights every request anyway.
            */

            //New post
/*            List<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
            AbstractMap.SimpleEntry<String, String> username = new AbstractMap.SimpleEntry<>("username", "yolo");
            AbstractMap.SimpleEntry<String, String> token = new AbstractMap.SimpleEntry<>("token", "TVC2FbUUvcJjJqct");
            AbstractMap.SimpleEntry<String, String> title = new AbstractMap.SimpleEntry<>("title", "API Test post #1");
            AbstractMap.SimpleEntry<String, String> shortbody = new AbstractMap.SimpleEntry<>("short", "EHLO :)))");
            AbstractMap.SimpleEntry<String, String> longbody = new AbstractMap.SimpleEntry<>("long", "Greetings from the BEST™ Android App (In the World... Of Warcraft).");
            params.add(username);
            params.add(token);
            params.add(title);
            params.add(shortbody);
            params.add(longbody);
            String response = utils.HTMLPOST("http://192.168.1.2/dankpress/api/newPost", params);*/

            //Edit post
            /*List<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
            AbstractMap.SimpleEntry<String, String> username = new AbstractMap.SimpleEntry<>("username", "yolo");
            AbstractMap.SimpleEntry<String, String> token = new AbstractMap.SimpleEntry<>("token", "TVC2FbUUvcJjJqct");
            AbstractMap.SimpleEntry<String, String> title = new AbstractMap.SimpleEntry<>("title", "API Test post #1, UPDATED");
            AbstractMap.SimpleEntry<String, String> shortbody = new AbstractMap.SimpleEntry<>("short", "EHLO :)))");
            AbstractMap.SimpleEntry<String, String> longbody = new AbstractMap.SimpleEntry<>("long", "Greetings from the BEST™ Android App (In the World... Of Warcraft).");
            AbstractMap.SimpleEntry<String, String> reason = new AbstractMap.SimpleEntry<>("reason", "Because I can");
            params.add(username);
            params.add(token);
            params.add(title);
            params.add(shortbody);
            params.add(longbody);
            params.add(reason);
            String response = utils.HTMLPOST("http://192.168.1.2/dankpress/api/editPost/3", params);*/

            // Delete post
            /*List<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
            String response = utils.HTMLPOST("http://192.168.1.2/dankpress/api/deletePost/3", params);*/

            // Check token state
            /*List<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
            AbstractMap.SimpleEntry<String, String> username = new AbstractMap.SimpleEntry<>("username", "yolo");
            AbstractMap.SimpleEntry<String, String> token = new AbstractMap.SimpleEntry<>("token", "TVC2FbUUvcJjJqct");
            params.add(username);
            params.add(token);
            String response = utils.HTMLPOST("http://192.168.1.2/dankpress/api/checkToken", params);
            */

            /*Log.i("App",  (response != null) ? response : "FAIL");*/
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
