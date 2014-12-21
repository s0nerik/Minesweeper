package com.etiennelawlor.minesweeper.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.etiennelawlor.minesweeper.R;
import com.etiennelawlor.minesweeper.activities.BaseGameActivity;
import com.etiennelawlor.minesweeper.adapters.MinesweeperGridAdapter;
import com.etiennelawlor.minesweeper.interfaces.MinesweeperInterface;
import com.etiennelawlor.minesweeper.utils.MinesweeperUtils;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;
import com.google.android.gms.games.leaderboard.ScoreSubmissionData;

import org.w3c.dom.Text;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * Created by etiennelawlor on 9/11/14.
 */

public class MinesweeperFragment extends Fragment {

    // region Member Variables
    private boolean [][] mBoard;
    private boolean [][] mClickedTiles;
    private boolean mFlagModeActive = false;
    private MinesweeperGridAdapter mAdapter;
    private GameState mGameState;
    private int mFlagsCount;
    private long mStartTime = 0L;
    private Handler mCustomHandler = new Handler();
    private long mTimeInMilliseconds = 0L;
    private long mTimeSwapBuff = 0L;
    private long mUpdatedTime = 0L;
    private int mClickedTileCount;
    private MinesweeperInterface mCoordinator;

    private Runnable mUpdateTimerThread = new Runnable() {
        public void run() {

            if(isAdded() && isResumed()){
                mTimeInMilliseconds = SystemClock.uptimeMillis() - mStartTime;
                mUpdatedTime = mTimeSwapBuff + mTimeInMilliseconds;
                int secs = (int) (mUpdatedTime / 1000);
                int mins = secs / 60;
                secs = secs % 60;
                int milliseconds = (int) (mUpdatedTime % 1000) / 10;
                mTimerTextView.setText(String.format("%02d", mins) + ":"
                        + String.format("%02d", secs) + ":"
                        + String.format("%02d", milliseconds));
                mCustomHandler.postDelayed(this, 0);
            }

        }
    };

    @InjectView(R.id.flags_tv) TextView mFlagsTextView;
    @InjectView(R.id.timer_tv) TextView mTimerTextView;
    @InjectView(R.id.face_ib) ImageButton mFaceImageButton;
    @InjectView(R.id.minesweeper_gv) GridView mMinesweeperGridView;
    // endregion

    // region Listeners
    private MinesweeperGridAdapter.TileClickListener mTileClickListener = new MinesweeperGridAdapter.TileClickListener() {
        @Override
        public void onTileClicked(View v, int position) {

            if(mGameState != GameState.ENDED){

                if(mGameState == GameState.NOT_STARTED){
                    mStartTime = SystemClock.uptimeMillis();
                    mCustomHandler.postDelayed(mUpdateTimerThread, 0);
                    mGameState = GameState.IN_PLAY;
                }

                int i = position / 8;
                int j = position % 8;

                ImageView flagImageView = (ImageView) v.findViewById(R.id.flag_iv);
                ImageView tileImageView = (ImageView) v.findViewById(R.id.tile_iv);
                TextView tileTextView = (TextView) v.findViewById(R.id.tile_tv);

                if(!mClickedTiles[i][j]){ // If tile has not been clicked

                    if(mFlagModeActive){
                        if(flagImageView.getVisibility() == View.GONE) {
                            if(mFlagsCount != 0){
                                tileImageView.setVisibility(View.GONE);
                                flagImageView.setVisibility(View.VISIBLE);
                                mFlagsTextView.setText(String.valueOf(--mFlagsCount));
                            }
                        }else {
                            tileImageView.setVisibility(View.VISIBLE);
                            flagImageView.setVisibility(View.GONE);

                            mFlagsTextView.setText(String.valueOf(++mFlagsCount));
                        }
                    } else {
                        if(flagImageView.getVisibility() == View.GONE){

                            mClickedTiles[i][j] = true;
                            mClickedTileCount++;

                            if(mBoard[i][j] == true){ // GAME OVER
                                mGameState = GameState.ENDED;

                                mCustomHandler.removeCallbacks(mUpdateTimerThread);

                                mFaceImageButton.setImageResource(R.drawable.ic_sad);

                                tileImageView.setImageResource(R.drawable.ic_mine);
                                tileImageView.setBackgroundColor(getResources().getColor(R.color.red));
//                                v.setBackgroundColor(getResources().getColor(R.color.red));

                                uncoverAllMines(i, j);
                                uncoverFalseFlags();

                                Style croutonStyle = new Style.Builder()
                                        .setHeight(MinesweeperUtils.dp2px(getActivity(), 50))
//                                .setTextColor(getResources().getColor(R.color.white))
                                        .setGravity(Gravity.CENTER)
                                        .setBackgroundColor(R.color.yellow_800)
                                        .build();

                                Crouton.makeText(getActivity(), "Game Over. Look out for the mines.", croutonStyle)
                                        .setConfiguration(new Configuration.Builder()
                                                .setDuration(1500)
                                                .setInAnimation(R.anim.crouton_in)
                                                .setOutAnimation(R.anim.crouton_out)
                                                .build())
                                        .show();

                            } else {
                                int adjacentMineCount = getAdjacentMineCount(i, j);

                                if(adjacentMineCount>0)
                                    tileTextView.setText(String.valueOf(adjacentMineCount));

                                int numColor = 0;
                                switch (adjacentMineCount){
                                    case 1:
                                        numColor = R.color.blue;
                                        break;
                                    case 2:
                                        numColor = R.color.dark_green;
                                        break;
                                    case 3:
                                    case 4:
                                    case 5:
                                    case 6:
                                    case 7:
                                    case 8:
                                        numColor = R.color.red;
                                        break;
                                    default:
                                        break;

                                }

                                if(numColor != 0)
                                    tileTextView.setTextColor(getResources().getColor(numColor));

                                tileImageView.setVisibility(View.GONE);
                                tileTextView.setVisibility(View.VISIBLE);

                                if(adjacentMineCount == 0){
                                    clickNeighbors(i, j, position);
                                }

                                if(mClickedTileCount == (8*8)-10){ // YOU WIN!!!
                                    mGameState = GameState.ENDED;

                                    mCustomHandler.removeCallbacks(mUpdateTimerThread);

                                    mFaceImageButton.setImageResource(R.drawable.ic_cool);

                                    if (mCoordinator.isUserSignedIn()) {
                                        Games.Leaderboards.submitScoreImmediate(mCoordinator.getGoogleApiClient(),
                                                getResources().getString(R.string.leaderboard_fastest_time),
                                                mTimeInMilliseconds)
                                                .setResultCallback(mSubmitScoreResultCallback);
                                    } else {
                                        Toast.makeText(getActivity(), "Please sign in to submit score", Toast.LENGTH_SHORT).show();
                                    }

                                    String achievementId = "";

                                    double seconds = mTimeInMilliseconds/1000;
                                    if(seconds < 30){
                                        achievementId = getString(R.string.achievement_cheetah);
                                    } else if(seconds < 60){
                                        achievementId = getString(R.string.achievement_zebra);
                                    } else if(seconds < 120){
                                        achievementId = getString(R.string.achievement_rabbit);
                                    } else if(seconds < 300){
                                        achievementId = getString(R.string.achievement_pig);
                                    } else if(seconds < 600){
                                        achievementId = getString(R.string.achievement_snail);
                                    }

                                    if(!TextUtils.isEmpty(achievementId)){
                                        if (mCoordinator.isUserSignedIn()) {
                                            Games.Achievements.unlock(mCoordinator.getGoogleApiClient(), achievementId);
                                        } else {
                                            Toast.makeText(getActivity(), "Please sign in to unlock achievement", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    };

    @OnClick(R.id.face_ib) void onClicked(){
        setUpNewGame();
    }

    @OnCheckedChanged(R.id.flag_tb) void onChecked(boolean isChecked){
        if(isChecked)
            mFlagModeActive = true;
        else
            mFlagModeActive = false;
    };

    // endregion

    // region Callbacks
    private ResultCallback<Leaderboards.SubmitScoreResult> mSubmitScoreResultCallback = new ResultCallback<Leaderboards.SubmitScoreResult>() {
        @Override
        public void onResult(Leaderboards.SubmitScoreResult submitScoreResult) {
            if(submitScoreResult!=null){
                ScoreSubmissionData scoreSubmissionData = submitScoreResult.getScoreData();
                if(scoreSubmissionData!=null){
                    ScoreSubmissionData.Result result = scoreSubmissionData.getScoreResult(LeaderboardVariant.TIME_SPAN_ALL_TIME);
                    if(result!=null){
                        if(result.newBest) {

                            Style croutonStyle = new Style.Builder()
                                    .setHeight(MinesweeperUtils.dp2px(getActivity(), 50))
//                                .setTextColor(getResources().getColor(R.color.white))
                                    .setGravity(Gravity.CENTER)
                                    .setBackgroundColor(R.color.blue_500)
                                    .build();

                            Crouton.makeText(getActivity(), String.format("New High Score! %s is your new personal best!", result.formattedScore), croutonStyle)
                                    .setConfiguration(new Configuration.Builder()
                                            .setDuration(3000)
                                            .setInAnimation(R.anim.crouton_in)
                                            .setOutAnimation(R.anim.crouton_out)
                                            .build())
                                    .show();
                        } else {

                            String formattedScore = mTimerTextView.getText().toString();

                            if(formattedScore.startsWith("00:"))
                                formattedScore = formattedScore.substring(3);

                            Style croutonStyle = new Style.Builder()
                                    .setHeight(MinesweeperUtils.dp2px(getActivity(), 50))
//                                .setTextColor(getResources().getColor(R.color.white))
                                    .setGravity(Gravity.CENTER)
                                    .setBackgroundColor(R.color.green_500)
                                    .build();

                            Crouton.makeText(getActivity(), String.format("Congratulations! You beat Minesweeper in %s.", formattedScore), croutonStyle)
                                    .setConfiguration(new Configuration.Builder()
                                            .setDuration(3000)
                                            .setInAnimation(R.anim.crouton_in)
                                            .setOutAnimation(R.anim.crouton_out)
                                            .build())
                                    .show();
                        }

                    }
                }
            }

        }
    };
    // endregion

    // region Constructors
    public static MinesweeperFragment newInstance(Bundle extras) {
        MinesweeperFragment fragment = new MinesweeperFragment();
        fragment.setRetainInstance(true);
        fragment.setArguments(extras);
        return fragment;
    }

    public static MinesweeperFragment newInstance() {
        MinesweeperFragment fragment = new MinesweeperFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    public MinesweeperFragment() {
    }
    // endregion

    // region Lifecycle Methods
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof MinesweeperInterface)
            mCoordinator = (MinesweeperInterface) activity;
        else
            throw new ClassCastException("Parent container must implement the MinesweeperInterface");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_minesweeper, container, false);
        ButterKnife.inject(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpNewGame();
    }

    @Override
    public void onResume() {
        super.onResume();

        if(mGameState == GameState.IN_PLAY){
            mStartTime = SystemClock.uptimeMillis();
            mCustomHandler.postDelayed(mUpdateTimerThread, 0);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        mTimeSwapBuff += mTimeInMilliseconds;
        mCustomHandler.removeCallbacks(mUpdateTimerThread);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
    // endregion

    // region Helper Methods
    private void setUpNewGame(){
        // Reset Variables
        mGameState = GameState.NOT_STARTED;
        mCustomHandler.removeCallbacks(mUpdateTimerThread);
        mFaceImageButton.setImageResource(R.drawable.ic_happy);
        mClickedTileCount = 0;
        mTimeSwapBuff = 0;
        mFlagsCount = 10;
        mFlagsTextView.setText(String.valueOf(mFlagsCount));
        mTimerTextView.setText("00:00:00");
        mBoard = new boolean [8][8];
        mClickedTiles = new boolean [8][8];

        resetBoard();
        placeMines();
    }

    private void resetBoard(){
        mAdapter = new MinesweeperGridAdapter(getActivity());
        mAdapter.setTileClickListener(mTileClickListener);
        mMinesweeperGridView.setAdapter(mAdapter);
    }

    private void placeMines(){
        Random generator = new Random();
        for(int i=0; i<10; i++){
            int x = generator.nextInt(8);
            int y = generator.nextInt(8);

            if(mBoard[x][y] == false) {
                mBoard[x][y] = true; // place mine
            } else {
                // Collision occurred, replace mine
                i--;
            }
        }
    }

    private int getAdjacentMineCount(int i, int j){
        int adjacentMineCount = 0;

        if(isMineAtNorthWest(i, j))
            adjacentMineCount++;

        if(isMineAtNorth(i, j))
            adjacentMineCount++;

        if(isMineAtNorthEast(i, j))
            adjacentMineCount++;

        if(isMineAtEast(i, j))
            adjacentMineCount++;

        if(isMineAtSouthEast(i, j))
            adjacentMineCount++;

        if(isMineAtSouth(i, j))
            adjacentMineCount++;

        if(isMineAtSouthWest(i, j))
            adjacentMineCount++;

        if(isMineAtWest(i, j))
            adjacentMineCount++;

        return adjacentMineCount;
    }

    boolean isMineAtNorthWest(int i, int j){
        if(i-1<0 || j-1<0){ // Index out of bounds error checking
            return false;
        } else if(mBoard[i-1][j-1] == true){
            return true;
        } else {
            return false;
        }
    }

    boolean isMineAtNorth(int i, int j){
        if(i-1<0){ // Index out of bounds error checking
            return false;
        } else if(mBoard[i-1][j] == true){
            return true;
        } else {
            return false;
        }
    }

    boolean isMineAtNorthEast(int i, int j){
        if(i-1<0 || j+1>7){ // Index out of bounds error checking
            return false;
        } else if(mBoard[i-1][j+1] == true){
            return true;
        } else {
            return false;
        }
    }

    boolean isMineAtEast(int i, int j){
        if(j+1>7){ // Index out of bounds error checking
            return false;
        } else if(mBoard[i][j+1] == true){
            return true;
        } else {
            return false;
        }
    }

    boolean isMineAtSouthEast(int i, int j){
        if(i+1>7 || j+1>7){ // Index out of bounds error checking
            return false;
        } else if(mBoard[i+1][j+1] == true){
            return true;
        } else {
            return false;
        }
    }

    boolean isMineAtSouth(int i, int j){
        if(i+1>7){ // Index out of bounds error checking
            return false;
        } else if(mBoard[i+1][j] == true){
            return true;
        } else {
            return false;
        }
    }

    boolean isMineAtSouthWest(int i, int j){
        if(i+1>7 || j-1<0){ // Index out of bounds error checking
            return false;
        } else if(mBoard[i+1][j-1] == true){
            return true;
        } else {
            return false;
        }
    }

    boolean isMineAtWest(int i, int j){
        if(j-1<0){ // Index out of bounds error checking
            return false;
        } else if(mBoard[i][j-1] == true){
            return true;
        } else {
            return false;
        }
    }

    private void clickNeighbors(int i, int j, int position){
        clickNorthWestNeighbor(i, j, position);
        clickNorthNeighbor(i, j, position);
        clickNorthEastNeighbor(i, j, position);
        clickEastNeighbor(i, j, position);
        clickSouthEastNeighbor(i, j, position);
        clickSouthNeighbor(i, j, position);
        clickSouthWestNeighbor(i, j, position);
        clickWestNeighbor(i, j, position);
    }

    private void clickNorthWestNeighbor(int i, int j, int position){
        if(i-1<0 || j-1<0) { // Index out of bounds error checking
            // error
        } else if(mClickedTiles[i-1][j-1] == false){
            if (mTileClickListener != null) {
                mTileClickListener.onTileClicked(mMinesweeperGridView.getChildAt(position - 8 - 1), position - 8 - 1);
            }
        }
    }

    private void clickNorthNeighbor(int i, int j, int position){
        if(i-1<0) { // Index out of bounds error checking
            // error
        } else if(mClickedTiles[i-1][j] == false){
            if (mTileClickListener != null) {
                mTileClickListener.onTileClicked(mMinesweeperGridView.getChildAt(position - 8), position - 8);
            }
        }
    }

    private void clickNorthEastNeighbor(int i, int j, int position){
        if(i-1<0 || j+1>7) { // Index out of bounds error checking
            // error
        } else if(mClickedTiles[i-1][j+1] == false){
            if (mTileClickListener != null) {
                mTileClickListener.onTileClicked(mMinesweeperGridView.getChildAt(position - 8 + 1), position - 8 + 1);
            }
        }
    }

    private void clickEastNeighbor(int i, int j, int position){
        if(j+1>7) { // Index out of bounds error checking
            // error
        } else if(mClickedTiles[i][j+1] == false){
            if (mTileClickListener != null) {
                mTileClickListener.onTileClicked(mMinesweeperGridView.getChildAt(position + 1), position + 1);
            }
        }
    }

    private void clickSouthEastNeighbor(int i, int j, int position){
        if(i+1>7 || j+1>7) { // Index out of bounds error checking
            // error
        } else if(mClickedTiles[i+1][j+1] == false){
            if (mTileClickListener != null) {
                mTileClickListener.onTileClicked(mMinesweeperGridView.getChildAt(position + 8 + 1), position + 8 + 1);
            }
        }
    }

    private void clickSouthNeighbor(int i, int j, int position){
        if(i+1>7) { // Index out of bounds error checking
            // error
        } else if(mClickedTiles[i+1][j] == false){
            if (mTileClickListener != null) {
                mTileClickListener.onTileClicked(mMinesweeperGridView.getChildAt(position + 8), position + 8);
            }
        }
    }

    private void clickSouthWestNeighbor(int i, int j, int position){
        if(i+1>7 || j-1<0) { // Index out of bounds error checking
            // error
        } else if(mClickedTiles[i+1][j-1] == false){
            if (mTileClickListener != null) {
                mTileClickListener.onTileClicked(mMinesweeperGridView.getChildAt(position + 8 - 1), position + 8 - 1);
            }
        }
    }

    private void clickWestNeighbor(int i, int j, int position){
        if(j-1<0) { // Index out of bounds error checking
            // error
        } else if(mClickedTiles[i][j-1] == false){
            if (mTileClickListener != null) {
                mTileClickListener.onTileClicked(mMinesweeperGridView.getChildAt(position - 1), position - 1);
            }
        }
    }

    private void uncoverAllMines(int xPos, int yPos){
        for(int i=0; i<8; i++){
            for(int j=0; j<8; j++){
                if(mBoard[i][j] == true && (i!=xPos || j!=yPos)){
                    int position = (i*8)+j;

                    View gridItem = mMinesweeperGridView.getChildAt(position);
                    ImageView tileImageView = (ImageView) gridItem.findViewById(R.id.tile_iv);
                    ImageView flagImageView = (ImageView) gridItem.findViewById(R.id.flag_iv);

//                    if(flagImageView.getVisibility() == View.VISIBLE)
//                        flagImageView.setImageResource(R.drawable.ic_error_flag_filled);
//                    else {
//                        tileImageView.setImageResource(R.drawable.ic_mine);
//                        tileImageView.setBackgroundColor(getResources().getColor(R.color.gray14));
//                    }

                    if(tileImageView.getVisibility() == View.VISIBLE) {
                        tileImageView.setImageResource(R.drawable.ic_mine);
                        tileImageView.setBackgroundColor(getResources().getColor(R.color.gray14));
                    }
                }
            }
        }
    }

    private void uncoverFalseFlags(){
        for(int i=0; i<8; i++) {
            for (int j = 0; j < 8; j++) {
                int position = (i*8)+j;

                View gridItem = mMinesweeperGridView.getChildAt(position);
                ImageView flagImageView = (ImageView) gridItem.findViewById(R.id.flag_iv);

                if (flagImageView.getVisibility() == View.VISIBLE && mBoard[i][j] == false) {
                    flagImageView.setImageResource(R.drawable.ic_false_flag);
                }
            }
        }
    }
    // endregion

    // region Enums

    public enum GameState {
        NOT_STARTED,
        IN_PLAY,
        ENDED
    }

    // endregion
}