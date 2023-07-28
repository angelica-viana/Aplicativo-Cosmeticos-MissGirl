package com.example.ecommercemissgirl.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ecommercemissgirl.R;
import com.example.ecommercemissgirl.helper.FirebaseHelper;
import com.example.ecommercemissgirl.model.ItemPedido;

import com.example.ecommercemissgirl.model.Produto;
import com.example.ecommercemissgirl.util.GetMask;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class DetalhesPedidoAdapter extends RecyclerView.Adapter<DetalhesPedidoAdapter.MyViewHolder> {

    private final List<ItemPedido> itemPedidoList;
    private final Context context;

    public DetalhesPedidoAdapter(List<ItemPedido> itemPedidoList, Context context) {
        this.itemPedidoList = itemPedidoList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_produto_detalhe_pedido, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        ItemPedido itemPedido = itemPedidoList.get(position);

        recuperaProduto(itemPedido.getIdProduto(), holder);

        holder.textQuantidade.setText(context.getString(R.string.quantidade, itemPedido.getQuantidade()));
        holder.textValor.setText(context.getString(R.string.valor, GetMask.getValor(itemPedido.getValor() * itemPedido.getQuantidade())));

    }

    private void recuperaProduto(String idProduto, MyViewHolder holder) {
        DatabaseReference produtoRef = FirebaseHelper.getDatabaseReference()
                .child("produtos")
                .child(idProduto);
        produtoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Produto produto = snapshot.getValue(Produto.class);
                    holder.textTitulo.setText(produto.getTitulo());
                    Glide.with(context)
                            .load(produto.getUrlsImagens().get(0).getCaminhoImagem())
                            .into(holder.imgProduto);
                }else{
                    holder.textTitulo.setText("Produto não localizado.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return itemPedidoList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView imgProduto;
        TextView textTitulo, textValor, textQuantidade;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imgProduto = itemView.findViewById(R.id.imgProduto);
            textTitulo = itemView.findViewById(R.id.textTitulo);
            textValor = itemView.findViewById(R.id.textValor);
            textQuantidade = itemView.findViewById(R.id.textQuantidade);
        }
    }
}
