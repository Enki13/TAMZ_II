package com.example.tamz_project;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    private MyDrawView drawView;
    final MediaPlayer mp = MediaPlayer.create(this, R.raw.button);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        setNavigationViewListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.undo:
                drawView.undo();
                return true;
            case R.id.redo:
                drawView.redo();
                return true;
            case R.id.action_save:
                saveImage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        if (drawView == null)
            drawView = findViewById(R.id.custom_view);
        switch (item.getItemId()) {

            case R.id.lower_layer: {
                drawView.setLayer(1);
                mp.start();
                break;
            }
            case R.id.middle_layer: {
                drawView.setLayer(2);
                mp.start();
                break;
            }
            case R.id.upper_layer: {
                drawView.setLayer(3);
                mp.start();
                break;
            }

            case R.id.erase_layer: {
                drawView.eraseLayer();
                mp.start();
                break;
            }
            case R.id.erase_all: {
                drawView.eraseAll();
                mp.start();
                break;
            }

            case R.id.brush_size: {
                final Dialog yourDialog = new Dialog(this);
                LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.brush_size_dialog, (ViewGroup) findViewById(R.id.dialog_root_element));
                yourDialog.setContentView(layout);
                final TextView txt_size = (TextView) layout.findViewById(R.id.txt_brush_size);
                Button b_confirm = (Button) layout.findViewById(R.id.btn_confirm);
                Button b_cancel = (Button) layout.findViewById(R.id.btn_cancel);
                SeekBar seekBar = (SeekBar) layout.findViewById(R.id.seek_brush_size);
                final int[] size = {5};
                SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        int progress = seekBar.getProgress();
                        if (progress == 0) {
                            size[0] = progress + 1;
                        } else {
                            size[0] = progress;
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        //add code here
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBark, int progress, boolean fromUser) {
                        if (progress == 0) {
                            txt_size.setText(String.valueOf(progress + 1));
                        } else {
                            txt_size.setText(String.valueOf(progress));
                        }
                    }
                };
                seekBar.setOnSeekBarChangeListener(seekBarListener);
                b_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        yourDialog.cancel();
                    }
                });
                b_confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        drawView.setBrushSize(size[0]);
                        yourDialog.cancel();
                    }
                });
                mp.start();
                yourDialog.show();
            }
        }
        //close navigation drawer
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setNavigationViewListener() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.lower_layer);
        navigationView.setNavigationItemSelectedListener(this);
    }

    void saveImage() {

        try {
            Date date = Calendar.getInstance().getTime();
            DateFormat dateFormat = new SimpleDateFormat("yyyymmdd_hhmmss");
            String strDate = dateFormat.format(date);

            File dir = new File("/sdcard/drawapp/");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File f = new File(dir ,strDate + ".png");
            f.createNewFile();
            FileOutputStream out = new FileOutputStream(f);
            Bitmap bitmap = drawView.getCanvasBitmap();
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
