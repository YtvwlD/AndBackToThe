package de.ytvwld.andbacktothe;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import java.lang.reflect.InvocationTargetException;

public class Main extends Activity
{
    private static final String TAG = "AndBackToThe";
    private Context context = this;
    private BackupManager backupManager;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        backupManager = new BackupManager(context);
        try
        {
          boolean backupEnabled = (boolean)(backupManager.getClass().getMethod("isBackupEnabled").invoke(backupManager));
          String currentTransport = (String)(backupManager.getClass().getMethod("getCurrentTransport").invoke(backupManager));
          Log.v(TAG, "Backup enabled: " + backupEnabled);
          Log.v(TAG, "Transport: " + currentTransport);
        }
        catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e)
        {
          Log.e(TAG, "Must be a system app.");
          AlertDialog.Builder builder = new AlertDialog.Builder(this);
          builder.setMessage(R.string.dialog_no_system_app)
                 .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                     System.exit(-1);
                   }
                 });
          builder.create().show();
        }
    }
}
