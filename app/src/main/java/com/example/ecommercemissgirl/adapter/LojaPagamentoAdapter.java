package com.example.ecommercemissgirl.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommercemissgirl.R;
import com.example.ecommercemissgirl.model.FormaPagamento;
import com.example.ecommercemissgirl.util.GetMask;

import java.util.List;

public class LojaPagamentoAdapter extends RecyclerView.Adapter<LojaPagamentoAdapter.MyViewHolder> {

    private final List<FormaPagamento> formaPagamentoList;
    private final Context context;
    private final OnClick onClick;

    public LojaPagamentoAdapter(List<FormaPagamento> formaPagamentoList, Context context, OnClick onClick) {
        this.formaPagamentoList = formaPagamentoList;
        this.context = context;
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_forma_pagamento_adapter, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        FormaPagamento formaPagamento = formaPagamentoList.get(position);

        holder.textNomePagamento.setText(formaPagamento.getNome());
        //holder.textDescricaoPagamento.setText(formaPagamento.getDescricao());
        holder.textValor.setText(context.getString(R.string.valor, GetMask.getValor(formaPagamento.getValor())));

        if(formaPagamento.getTipoValor().equals("DESC")){
            holder.textTipoPagamento.setText("Desconto");
        }else {
            holder.textTipoPagamento.setText("Acréscimo");
        }

        holder.itemView.setOnClickListener(v -> onClick.onClickListener(formaPagamento));
    }

    @Override
    public int getItemCount() {
        return formaPagamentoList.size();
    }

    public interface OnClick {
        void onClickListener(FormaPagamento formaPagamento);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView textNomePagamento, textDescricaoPagamento, textValor, textTipoPagamento;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textNomePagamento = itemView.findViewById(R.id.textNomePagamento);
            textDescricaoPagamento = itemView.findViewById(R.id.textDescricaoPagamento);
            textValor = itemView.findViewById(R.id.textValor);
            textTipoPagamento = itemView.findViewById(R.id.textTipoPagamento);
        }
    }
}
