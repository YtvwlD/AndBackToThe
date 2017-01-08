package de.ytvwld.andbacktothe;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class Main extends Activity
{
    private static final String TAG = "AndBackToThe";
    private Context context = this;
    private BackupManager backupManager;
    private Switch toggleBackup;
    private Spinner currentTransportSpinner;
    private String currentTransport;
    private boolean backupEnabled;
    private String[] availableTransports;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // get the backupManager
        backupManager = new BackupManager(context);

        // get our controls
        toggleBackup = (Switch) findViewById(R.id.toggle_backup);
        currentTransportSpinner = (Spinner) findViewById(R.id.spinner_current_transport);

        try
        {
          // load the data

          backupEnabled = (boolean)(backupManager.getClass().getMethod("isBackupEnabled").invoke(backupManager));
          currentTransport = (String)(backupManager.getClass().getMethod("getCurrentTransport").invoke(backupManager));
          availableTransports = (String[])(backupManager.getClass().getMethod("listAllTransports").invoke(backupManager));
          // move the current transport to the first position
          availableTransports[Arrays.asList(availableTransports).indexOf(currentTransport)] = availableTransports[0];
          availableTransports[0] = currentTransport;

          Log.v(TAG, "Backup enabled: " + backupEnabled);
          toggleBackup.setChecked(backupEnabled);
          Log.v(TAG, "Current transport: " + currentTransport);
          Log.v(TAG, "Available transports: ");
          for(String transport: availableTransports)
          {
            Log.v(TAG, " " + transport);
          }
          ArrayAdapter<String> adapter = new ArrayAdapter(context, android.R.layout.simple_spinner_item, availableTransports);
          adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
          //adapter.setNotifyOnChange(false); // Don't act on our changes.
          currentTransportSpinner.setAdapter(adapter);

          // set the listeners

          toggleBackup.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton button, boolean isChecked) {
              Log.v(TAG, "Setting backup to: " + isChecked);
              try
              {
                backupManager.getClass().getMethod("setBackupEnabled", new Class[] {boolean.class}).invoke(backupManager, isChecked);
                backupEnabled = isChecked;
              }
              catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e)
              {
                Log.wtf(TAG, "We are a system app, but couldn't enable or disable the backup.");
                Log.wtf(TAG, e);
                System.exit(-1);
              }
            }
          });

          currentTransportSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
            {
              String selected = (String)parent.getItemAtPosition(pos);
              if (selected == currentTransport)
              {
                Log.v(TAG, "User selected the already used transport, ignoring.");
                return;
              }
              Log.v(TAG, "User selected: " + selected);
              try
              {
                backupManager.getClass().getMethod("selectBackupTransport", new Class[] {String.class}).invoke(backupManager, selected);
                currentTransport = selected;
              }
              catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e)
              {
                Log.wtf(TAG, "We are a system app, but couldn't set the backup transport.");
                Log.wtf(TAG, e);
                System.exit(-1);
              }
            }

            public void onNothingSelected(AdapterView <?> parent)
            {
              Log.v(TAG, "User selected nothing.");
            }
          });
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
