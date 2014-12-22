package com.etiennelawlor.minesweeper.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.etiennelawlor.minesweeper.R;
import com.etiennelawlor.minesweeper.utils.MinesweeperUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by etiennelawlor on 9/11/14.
 */
public class MinesweeperGridAdapter extends BaseAdapter {

    // region Constants
    protected final LayoutInflater mLayoutInflater;
    // endregion

    // region Member Variables
    private int mGridItemWidth;
    private int mGridItemHeight;
    private Context mContext;
    private TileClickListener mTileClickListener;
    private TileLongClickListener mTileLongClickListener;

    // endregion

    // region Listeners
    private View.OnClickListener mTileOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (Integer) v.getTag(R.id.position_key);

            if (mTileClickListener != null) {
                mTileClickListener.onTileClicked(v, position);
            }
        }
    };

    private View.OnLongClickListener mTileOnLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            int position = (Integer) v.getTag(R.id.position_key);

            if (mTileLongClickListener != null) {
                mTileLongClickListener.onTileLongClicked(v, position);
            }
            return true;
        }
    };
    
    public void setTileClickListener (TileClickListener tileClickListener) {
        mTileClickListener = tileClickListener;
    }

    public void setTileLongClickListener (TileLongClickListener tileLongClickListener) {
        mTileLongClickListener = tileLongClickListener;
    }
    // endregion

    // region Interfaces
    public interface TileClickListener {
        public void onTileClicked(View v, int position);
    }

    public interface TileLongClickListener {
        public void onTileLongClicked(View v, int position);
    }
    // endregion

    // region Constructors
    public MinesweeperGridAdapter(Context context) {
//        super(context);

        mContext = context;
        mLayoutInflater = LayoutInflater.from(mContext);
        int screenWidth = MinesweeperUtils.getScreenWidth(mContext);

        mGridItemWidth = (screenWidth - MinesweeperUtils.dp2px(mContext, 32))/8;
        mGridItemHeight = (screenWidth - MinesweeperUtils.dp2px(mContext, 32))/8;

    }
    // endregion

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.minesweeper_tile, parent, false);
            viewHolder = new ViewHolder(convertView);

            convertView.setLayoutParams(getGridItemLayoutParams(convertView));
            convertView.setOnClickListener(mTileOnClickListener);
            convertView.setOnLongClickListener(mTileOnLongClickListener);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.mTileTextView.setText("");
        viewHolder.mTileTextView.setVisibility(View.GONE);
        viewHolder.mFlagImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_flag));
        viewHolder.mFlagImageView.setVisibility(View.GONE);
        viewHolder.mTileImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.selector_tile));
        viewHolder.mTileImageView.setVisibility(View.VISIBLE);

        convertView.setTag(R.id.position_key, position);

        return convertView;
    }

    @Override
    public int getCount() {
        return 64;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    // region Helper Methods
    private ViewGroup.LayoutParams getGridItemLayoutParams(View view){
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();

        layoutParams.width = mGridItemWidth;
        layoutParams.height = mGridItemHeight;

        return layoutParams;
    }
    // endregion

    // region Inner Classes

    public static class ViewHolder {
        @InjectView(R.id.tile_iv) ImageView mTileImageView;
        @InjectView(R.id.tile_tv) TextView mTileTextView;
        @InjectView(R.id.flag_iv) ImageView mFlagImageView;

        ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

    // endregion

}
