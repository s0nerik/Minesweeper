package com.etiennelawlor.minesweeper.interfaces;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by etiennelawlor on 9/14/14.
 */
public interface MinesweeperInterface {
    public GoogleApiClient getGoogleApiClient();
    public boolean isUserSignedIn();
    public void displayAlert(String title, String message);
}
