package com.example.ecommercemissgirl.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.ecommercemissgirl.R;
import com.example.ecommercemissgirl.model.ImagemUpload;
import com.smarteist.autoimageslider.SliderViewAdapter;

import java.util.List;

public class SliderAdapter extends SliderViewAdapter<SliderAdapter.MyViewHolder>{

    private final List<ImagemUpload> urlsImagens;
    private final Context context;

    public SliderAdapter(List<ImagemUpload> urlsImagens, Context context) {
        this.urlsImagens = urlsImagens;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_slide_imagem, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder viewHolder, int position) {
        ImagemUpload imagemUpload = urlsImagens.get(position);

        Glide.with(context)
                .load(imagemUpload.getCaminhoImagem())
                .into(viewHolder.imgSlide);
    }

    @Override
    public int getCount() {
        return urlsImagens.size();
    }

    static class MyViewHolder extends SliderViewAdapter.ViewHolder{

        ImageView imgSlide;

        public MyViewHolder(View itemView) {
            super(itemView);
            imgSlide = itemView.findViewById(R.id.imgSlide);
        }
    }
}
