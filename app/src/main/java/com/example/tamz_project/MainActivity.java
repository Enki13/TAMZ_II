package com.example.tamz_project;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
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
import android.widget.Toast;

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
    private MediaPlayer mp;
    private int orient;

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
        mp = MediaPlayer.create(this, R.raw.button);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStart() {
        super.onStart();
        loadSettings();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

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
        if (drawView == null)
            drawView = findViewById(R.id.custom_view);
        switch (item.getItemId()) {
            case R.id.undo:
                drawView.undo();
                return true;
            case R.id.redo:
                drawView.redo();
                return true;
            case R.id.action_save:
                String s = saveImage();
                if (!s.equals(""))
                    Toast.makeText(getApplicationContext(), "Image saved in " + s, Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getApplicationContext(), "Exeption occured while trying to save image.", Toast.LENGTH_SHORT).show();
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
                mp.start();
                drawView.setLayer(1);
                break;
            }
            case R.id.middle_layer: {
                mp.start();
                drawView.setLayer(2);
                break;
            }
            case R.id.upper_layer: {
                mp.start();
                drawView.setLayer(3);
                break;
            }

            case R.id.erase_layer: {
                mp.start();
                drawView.eraseLayer();
                break;
            }
            case R.id.erase_all: {
                mp.start();
                drawView.eraseAll();
                break;
            }

            case R.id.brush_size: {
                mp.start();
                brushSize();
                break;
            }

            case R.id.brush_color: {
                mp.start();
                brushColor();
                break;
            }

            case R.id.brush_mode: {
                mp.start();
                brushMode();
                break;
            }

            case R.id.orient_portrait: {
                if (orient == 2) {
                    mp.start();
                    orient = 1;
                    saveSettings(4);
                    changeOrient(orient);
                }
                break;
            }

            case R.id.orient_landscape: {
                if (orient == 1) {
                    mp.start();
                    orient = 2;
                    saveSettings(4);
                    changeOrient(orient);
                }
                break;
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

    public String saveImage() {

        try {
            Date date = Calendar.getInstance().getTime();
            DateFormat dateFormat = new SimpleDateFormat("yyyymmdd_hhmmss");
            String strDate = dateFormat.format(date) + ".png";

            File path = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File f = new File(path, strDate);
            f.createNewFile();
            FileOutputStream out = new FileOutputStream(f);
            Bitmap bitmap = drawView.getCanvasBitmap();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return f.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public void brushSize() {
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.brush_size_dialog, (ViewGroup) findViewById(R.id.dialog_root_element));
        final TextView txt_size = (TextView) layout.findViewById(R.id.txt_brush_size);
        SeekBar seekBar = (SeekBar) layout.findViewById(R.id.seek_brush_size);
        txt_size.setText(String.valueOf(drawView.getSize()));
        seekBar.setProgress(drawView.getSize());
        final int[] size = {drawView.getSize()};

        popDialog.setTitle("Select size");
        popDialog.setView(layout);


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
        popDialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                drawView.setBrushSize(size[0]);
                saveSettings(1);
            }
        });
        popDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        popDialog.show();
    }

    public void brushColor() {
        ColorPickerDialogBuilder
                .with(this)
                .setTitle("Choose color")
                .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                .density(10)
                .lightnessSliderOnly()
                .setPositiveButton("Ok", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                        drawView.setBrushColor(selectedColor);
                        saveSettings(2);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .build()
                .show();
    }

    public void brushMode() {
        final AlertDialog.Builder adb = new AlertDialog.Builder(this);
        CharSequence items[] = new CharSequence[]{"Stroke", "Fill"};
        final int[] mode = new int[1];
        adb.setSingleChoiceItems(items, drawView.getBrushMode(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int n) {
                mode[0] = n;
            }
        });
        adb.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                drawView.setMode(mode[0]);
                saveSettings(3);
            }
        });
        adb.setNegativeButton("Cancel", null);
        adb.setTitle("Select mode");
        adb.show();
    }

    public void changeOrient(final int o) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to change orientation? It will reset your canvas.")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (o == 2)
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        if (o == 1)
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }

    public void saveSettings(int i) {
        if (drawView == null)
            drawView = findViewById(R.id.custom_view);
        SharedPreferences pref = getApplicationContext().getSharedPreferences("SBPrefs", 0);
        SharedPreferences.Editor editor = pref.edit();
        switch (i) {
            case 1:
                editor.putInt("size", drawView.getSize());
                break;
            case 2:
                editor.putInt("color", drawView.getColor());
                break;
            case 3:
                editor.putInt("mode", drawView.getBrushMode());
                break;
            case 4:
                editor.putInt("orientation", orient);
                break;
        }
        editor.commit();
    }

    public void loadSettings(){
        SharedPreferences pref = getApplicationContext().getSharedPreferences("SBPrefs", 0);
        SharedPreferences.Editor editor = pref.edit();
        if (drawView == null)
            drawView = findViewById(R.id.custom_view);
        drawView.setBrushSize(pref.getInt("size", 5));
        drawView.setBrushColor(pref.getInt("color", 0xFF000000));
        drawView.setMode(pref.getInt("mode", 0));
        orient = pref.getInt("orientation", 1);
        if(orient == 1)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if(orient == 2)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }
}
