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
    private Context mContext;
    private TileClickListener mTileClickListener;
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

    public void setTileClickListener (TileClickListener tileClickListener) {
        mTileClickListener = tileClickListener;
    }
    // endregion

    // region Interfaces
    public interface TileClickListener {
        public void onTileClicked(View v, int position);
    }
    // endregion

    // region Constructors
    public MinesweeperGridAdapter(Context context) {
//        super(context);

        mContext = context;
        mLayoutInflater = LayoutInflater.from(mContext);
    }
    // endregion

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.minesweeper_tile, parent, false);
            viewHolder = new ViewHolder(convertView);

            convertView.setLayoutParams(getGridItemLayoutParams(convertView, 8));
            convertView.setOnClickListener(mTileOnClickListener);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

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
    private ViewGroup.LayoutParams getGridItemLayoutParams(View view, int numOfColumns){
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();

        Point size = new Point();
        ((Activity)mContext).getWindowManager().getDefaultDisplay().getSize(size);
        int screenWidth = size.x;

        layoutParams.width = screenWidth/numOfColumns - MinesweeperUtils.dp2px(mContext, 4);
        layoutParams.height = screenWidth/numOfColumns - MinesweeperUtils.dp2px(mContext, 4);

        return layoutParams;
    }
    // endregion

    // region Inner Classes

    public static class ViewHolder {
        @InjectView(R.id.tile_iv) ImageView mTileImageView;
        @InjectView(R.id.tile_tv) TextView mTileTextView;

        ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

    // endregion

}
