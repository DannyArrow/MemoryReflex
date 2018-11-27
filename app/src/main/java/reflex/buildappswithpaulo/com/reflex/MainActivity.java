package reflex.buildappswithpaulo.com.reflex;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import reflex.buildappswithpaulo.com.reflex.view.ReflexView;

public class MainActivity extends AppCompatActivity {
    private ReflexView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.getWindow().clearFlags( WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        RelativeLayout layout = findViewById(R.id.relativeLayout);
        gameView = new ReflexView(this,getPreferences(Context.MODE_PRIVATE), layout);

        layout.addView(gameView, 0);

    }




    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView.resume(this);

    }
}
