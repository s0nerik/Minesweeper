package com.etiennelawlor.minesweeper.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.etiennelawlor.minesweeper.R;
import com.etiennelawlor.minesweeper.fragments.MinesweeperFragment;
import com.etiennelawlor.minesweeper.interfaces.MinesweeperInterface;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;


public class MinesweeperActivity extends BaseGameActivity implements // region Interfaces
GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, MinesweeperInterface
// endregion
{

    // region Constants
    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    private static final String STATE_RESOLVING_ERROR = "resolving_error";
    public static final int REQUEST_LEADERBOARD = 1002;
    public static final int REQUEST_ACHIEVEMENT = 1003;
    // endregion

    // region Member Variables
    private GoogleApiClient mGoogleApiClient;
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;
    // endregion

    // region Lifecycle Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Games.API)
//                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mResolvingError = savedInstanceState != null
                && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);

        setContentView(R.layout.activity_minesweeper);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, MinesweeperFragment.newInstance())
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mResolvingError) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }
    // endregion

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.minesweeper, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case R.id.action_leaderboards:
                if (isSignedIn()) {
                    startActivityForResult(Games.Leaderboards.getLeaderboardIntent(getApiClient(),
                                    getResources().getString(R.string.leaderboard_fastest_time)),
                            REQUEST_LEADERBOARD);
                } else {
                    Toast.makeText(this, "Please sign in to view leaderboards", Toast.LENGTH_SHORT).show();
                }

                return true;
            case R.id.action_achievements:
                if (isSignedIn()) {
                    startActivityForResult(Games.Achievements.getAchievementsIntent(getApiClient()),
                            REQUEST_ACHIEVEMENT);
                } else {
                    Toast.makeText(this, "Please sign in to view achievements", Toast.LENGTH_SHORT).show();
                }

                return true;
            case R.id.action_rules:

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        this);

                // set title
                alertDialogBuilder.setTitle("Minesweeper Rules");

                // set dialog message
                alertDialogBuilder
                        .setMessage(getString(R.string.rules))
                        .setCancelable(true)
                        .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();

                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mGoogleApiClient.isConnecting() &&
                        !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            }
        } else {
//            mHelper.onActivityResult(requestCode, resultCode, data);
        }

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(getClass().getSimpleName(), "onConnected");

        // Connected to Google Play services!
        // The good stuff goes here.
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(getClass().getSimpleName(), "onConnectionSuspended");
        // The connection has been interrupted.
        // Disable any UI components that depend on Google APIs
        // until onConnected() is called.
    }

    @Override
    public void onSignInFailed() {
        Log.d(getClass().getSimpleName(), "onSignInFailed");
    }

    @Override
    public void onSignInSucceeded() {
        Log.d(getClass().getSimpleName(), "onSignInSucceeded");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d(getClass().getSimpleName(), "onConnectionFailed");

        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GooglePlayServicesUtil.getErrorDialog()
            showErrorDialog(result.getErrorCode());
            mResolvingError = true;
        }
    }

    // region Minesweeper Interface Methods
    @Override
    public GoogleApiClient getGoogleApiClient() {
        return getApiClient();
    }

    @Override
    public boolean isUserSignedIn() {
        return isSignedIn();
    }

    @Override
    public void displayAlert(String title, String message) {
        showAlert(title, message);
    }
    // endregion

    // region Helper Methods

    // Creates a dialog for an error message
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }
    // endregion

    // region Inner Classes

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GooglePlayServicesUtil.getErrorDialog(errorCode,
                    this.getActivity(), REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((MinesweeperActivity)getActivity()).onDialogDismissed();
        }
    }
    // endregion
}
