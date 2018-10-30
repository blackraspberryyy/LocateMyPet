package com.example.jcv.locatemypet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by asus-pc on 6/25/2017.
 */

public class CustomGrid extends BaseAdapter{
    private Context mContext;
    private ArrayList<String> name;

    public CustomGrid(Context mContext, ArrayList<String> name, ArrayList<Integer> imageId) {
        this.mContext = mContext;
        this.name = name;
        this.imageId = imageId;
    }

    private ArrayList<Integer> imageId;

    @Override
    public int getCount() {
        return name.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View grid;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(convertView == null){
            grid = new View(mContext);
            grid = inflater.inflate(R.layout.custom_grid, null);
            TextView petName = (TextView) grid.findViewById(R.id.petName);
            ImageView petImage = (ImageView) grid.findViewById(R.id.petImg);

            petName.setText(name.get(position));
            petImage.setImageResource(imageId.get(position));
        }
        else{
            grid = (View) convertView;
        }

        return grid;
    }
}
