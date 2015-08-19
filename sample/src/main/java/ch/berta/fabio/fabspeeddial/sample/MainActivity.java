package ch.berta.fabio.fabspeeddial.sample;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import ch.berta.fabio.fabspeeddial.FabMenu;
import ch.berta.fabio.sample.R;

public class MainActivity extends AppCompatActivity {

    private static final long MENU_SHOW_DELAY = 200l;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final FabMenu fabMenu = (FabMenu) findViewById(R.id.fab_menu);
        if (savedInstanceState == null) {
            fabMenu.hideMenuButton(false);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    fabMenu.showMenuButton(true);
                }
            }, MENU_SHOW_DELAY);
        }

        final FloatingActionButton fab1 = (FloatingActionButton) findViewById(R.id.fab_1);
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(fabMenu, "This is a snackbar", Snackbar.LENGTH_SHORT).show();
            }
        });
    }
}
