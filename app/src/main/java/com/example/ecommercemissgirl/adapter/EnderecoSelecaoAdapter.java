package com.example.ecommercemissgirl.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommercemissgirl.R;
import com.example.ecommercemissgirl.model.Endereco;

import java.util.List;

public class EnderecoSelecaoAdapter extends RecyclerView.Adapter<EnderecoSelecaoAdapter.MyViewHolder> {

    private final List<Endereco> enderecoList;
    private final OnClickListener clickListener;

    public EnderecoSelecaoAdapter(List<Endereco> enderecoList, OnClickListener clickListener) {
        this.enderecoList = enderecoList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selecao_endereco_adapter, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Endereco endereco = enderecoList.get(position);

        holder.textNomeEndereco.setText(endereco.getNomeEndereco());


        StringBuilder enderecoCompleto = new StringBuilder();
        enderecoCompleto.append(endereco.getLogradouro())
                .append(", ")
                .append(endereco.getNumero())
                .append(", ")
                .append(endereco.getBairro())
                .append(", ")
                .append(endereco.getLocalidade())
                .append("-")
                .append(endereco.getUf())
                .append("\n")
                .append("CEP: ")
                .append(endereco.getCep());

        holder.textEndereco.setText(enderecoCompleto);

        holder.itemView.setOnClickListener(v -> clickListener.onClick(endereco));
    }

    @Override
    public int getItemCount() {
        return enderecoList.size();
    }

    public interface OnClickListener {
         void onClick(Endereco endereco);
    }

    static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView textNomeEndereco, textEndereco;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textNomeEndereco = itemView.findViewById(R.id.textNomeEndereco);
            textEndereco = itemView.findViewById(R.id.textEndereco);

        }
    }
}
