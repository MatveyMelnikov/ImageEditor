package com.example.imageeditor.recyclerview;

import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imageeditor.R;

import java.util.ArrayList;

public class CustomRecyclerAdapter extends
        RecyclerView.Adapter<CustomRecyclerAdapter.CustomViewHolder> {
    private final ArrayList<Integer> colors;
    private final RecyclerListener recyclerListener;

    public CustomRecyclerAdapter(RecyclerListener recyclerListener, ArrayList<Integer> colors)
    {
        this.recyclerListener = recyclerListener;
        this.colors = colors;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CustomViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        int color = colors.get(position);

        holder.pixel.setBackgroundColor(colors.get(position));
        //holder.pixel.getLayoutParams().width = pixelWidth;
        //holder.pixel.setBackgroundColor(0xFF00FF00);
        //holder.pixel.invalidate();

        holder.pixel.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        recyclerListener.onElementClick(
                                ((ColorDrawable)view.getBackground()).getColor()
                        );
                    }
                }
        );
    }

    @Override
    public int getItemCount() {
        return colors.size();
    }

    static class CustomViewHolder extends RecyclerView.ViewHolder {
        public View pixel;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            pixel = itemView.findViewById(R.id.pixel);
        }
    }
}
