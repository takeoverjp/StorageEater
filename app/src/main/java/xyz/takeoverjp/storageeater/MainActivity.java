package xyz.takeoverjp.storageeater;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    final private String LOG_TAG = "MainActivity";
    private File filesDir;
    private AsyncWriter writer;
    private StorageManager sm;
    private ProgressBar loadingCircle;
    private ProgressBar progressBar;
    private TextView totalSpaceView;
    private TextView freeSpaceView;
    private TextView usableSpaceView;
    private TextView allocatableBytesView;

    private void updateView () {
        long total = filesDir.getTotalSpace();
        long free = filesDir.getFreeSpace();
        int ratio = (int)((total - free) * 100 / total);
        long usable = filesDir.getUsableSpace();
        freeSpaceView.setText(String.format("%,d byte", free));
        usableSpaceView.setText(String.format("%,d byte", usable));
        try {
            allocatableBytesView.setText(String.format("%,d byte", sm.getAllocatableBytes(sm.UUID_DEFAULT)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        progressBar.setProgress(ratio, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        filesDir = getFilesDir();
        TextView filesDirView = (TextView)findViewById(R.id.filesDirValueView);
        filesDirView.setText(getFilesDir().getAbsolutePath() + "\n"
                + getCacheDir().getAbsolutePath() + "\n"
                + getBaseContext().getExternalFilesDir(null).getAbsolutePath() + "\n"
                + getBaseContext().getExternalCacheDir().getAbsolutePath());

        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        progressBar.setMax(100);

        loadingCircle = (ProgressBar)findViewById(R.id.loadingCircle);

        totalSpaceView = (TextView)findViewById(R.id.totalSpaceValueView);
        totalSpaceView.setText(String.format("%,d byte", filesDir.getTotalSpace()));

        freeSpaceView = (TextView)findViewById(R.id.freeSpaceValueView);
        freeSpaceView.setText(String.format("%,d byte", filesDir.getFreeSpace()));

        usableSpaceView = (TextView)findViewById(R.id.usableSpaceValueView);
        usableSpaceView.setText(String.format("%,d byte", filesDir.getUsableSpace()));

        sm = (StorageManager)getBaseContext().getSystemService(Context.STORAGE_SERVICE);
        allocatableBytesView = (TextView)findViewById(R.id.allocatableBytesValueView);
        try {
            allocatableBytesView.setText(String.format("%,d byte", sm.getAllocatableBytes(StorageManager.UUID_DEFAULT)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        updateView();
    }

    public void onClickEat(View v) {
        Log.v(LOG_TAG, "Eat button clicked");
        if (writer != null && writer.getStatus() == AsyncTask.Status.RUNNING) {
            Log.v(LOG_TAG, "Already running");
            writer.cancel(false);
            return;
        }
        writer = new AsyncWriter(sm, filesDir, progressBar, loadingCircle,
                freeSpaceView, usableSpaceView, allocatableBytesView, (TextView)v);
        writer.execute();
    }

    public void onClickClear(View v) {
        Log.v(LOG_TAG, "Clear button clicked");
        File file = new File(filesDir + "/garbage");
        if (file.exists()) {
            file.delete();
        }
        updateView();
    }
}
