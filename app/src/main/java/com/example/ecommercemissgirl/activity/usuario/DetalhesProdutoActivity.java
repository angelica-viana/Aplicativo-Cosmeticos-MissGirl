package com.example.ecommercemissgirl.activity.usuario;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.example.ecommercemissgirl.DAO.ItemDAO;
import com.example.ecommercemissgirl.DAO.ItemPedidoDAO;
import com.example.ecommercemissgirl.R;
import com.example.ecommercemissgirl.adapter.LojaProdutoAdapter;
import com.example.ecommercemissgirl.adapter.SliderAdapter;
import com.example.ecommercemissgirl.databinding.ActivityDetalhesProdutoBinding;
import com.example.ecommercemissgirl.databinding.DialogAddItemCarrinhoBinding;
import com.example.ecommercemissgirl.helper.FirebaseHelper;
import com.example.ecommercemissgirl.model.Favorito;
import com.example.ecommercemissgirl.model.ItemPedido;
import com.example.ecommercemissgirl.model.Produto;
import com.example.ecommercemissgirl.util.GetMask;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DetalhesProdutoActivity extends AppCompatActivity implements LojaProdutoAdapter.OnClickLister, LojaProdutoAdapter.OnClickFavorito {

    private ActivityDetalhesProdutoBinding binding;

    private final List<String> idsFavoritos = new ArrayList<>();
    private final List<Produto> produtoList = new ArrayList<>();

    private LojaProdutoAdapter lojaProdutoAdapter;

    private Produto produtoSelecionado;

    private ItemDAO itemDao;
    private ItemPedidoDAO itemPedidoDAO;

    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetalhesProdutoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        itemDao = new ItemDAO(this);
        itemPedidoDAO = new ItemPedidoDAO(this);

        configClicks();

        getExtra();

        recuperaFavoritos();

        configRvProdutos();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (dialog != null) dialog.dismiss();
    }

    private void configClicks() {
        binding.include.textTitulo.setText("Detalhes do produto");
        binding.include.include.ibVoltar.setOnClickListener(v -> finish());

        binding.likeButton.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                if(FirebaseHelper.getAutenticado()){
                    idsFavoritos.add(produtoSelecionado.getId());
                    Favorito.salvar(idsFavoritos);
                }else{
                    Toast.makeText(getBaseContext(), "Você não está autenticado no app.", Toast.LENGTH_SHORT).show();
                    binding.likeButton.setLiked(false);
                }
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                idsFavoritos.remove(produtoSelecionado.getId());
                Favorito.salvar(idsFavoritos);
            }
        });

        binding.btnAddCarrinho.setOnClickListener(v -> showDialogCarrinho());
    }

    private void addCarrinho() {
        ItemPedido itemPedido = new ItemPedido();
        itemPedido.setIdProduto(produtoSelecionado.getId());
        itemPedido.setQuantidade(1);
        itemPedido.setValor(produtoSelecionado.getValorAtual());

        itemPedidoDAO.salvar(itemPedido);

        itemDao.salvar(produtoSelecionado);

    }

    private void configRvProdutos(){
        binding.rvProdutos.setLayoutManager(new GridLayoutManager(this, 1, LinearLayoutManager.HORIZONTAL, false));
        binding.rvProdutos.setHasFixedSize(true);
        lojaProdutoAdapter = new LojaProdutoAdapter(R.layout.item_produto_similar_adapter, produtoList, this, true, idsFavoritos, this, this);
        binding.rvProdutos.setAdapter(lojaProdutoAdapter);
    }

    private void recuperaProdutos(){
        DatabaseReference produtoRef = FirebaseHelper.getDatabaseReference()
                .child("produtos");
        produtoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                produtoList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    Produto produto = ds.getValue(Produto.class);

                    for (String categoria : produtoSelecionado.getIdsCategorias()){
                        if(produto.getIdsCategorias().contains(categoria)){
                           if(!produtoList.contains(produto) && !produto.getId().equals(produtoSelecionado.getId())){
                               produtoList.add(produto);
                           }
                        }
                    }

                }

                Collections.reverse(produtoList);
                lojaProdutoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void recuperaFavoritos(){
        if(FirebaseHelper.getAutenticado()){
            DatabaseReference favoritoRef = FirebaseHelper.getDatabaseReference()
                    .child("favoritos")
                    .child(FirebaseHelper.getIdFirebase());
            favoritoRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    idsFavoritos.clear();

                    for(DataSnapshot ds : snapshot.getChildren()){
                        String idFavorito = ds.getValue(String.class);
                        idsFavoritos.add(idFavorito);
                    }
                        binding.likeButton.setLiked(idsFavoritos.contains(produtoSelecionado.getId()));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void getExtra(){
        produtoSelecionado = (Produto) getIntent().getSerializableExtra("produtoSelecionado");

        configDados();

        recuperaProdutos();
    }

    private void configDados(){
        binding.sliderView.setSliderAdapter(new SliderAdapter(produtoSelecionado.getUrlsImagens(), this));
        binding.sliderView.startAutoCycle();
        binding.sliderView.setScrollTimeInSec(4);
        binding.sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM);
        binding.sliderView.setSliderTransformAnimation(SliderAnimations.FADETRANSFORMATION);

        binding.textProduto.setText(produtoSelecionado.getTitulo());
        binding.textDescricao.setText(produtoSelecionado.getDescricao());
        binding.textValor.setText(getString(R.string.valor, GetMask.getValor(produtoSelecionado.getValorAtual())));
    }

    private void showDialogCarrinho(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        DialogAddItemCarrinhoBinding dialogBinding = DialogAddItemCarrinhoBinding
                .inflate(LayoutInflater.from(this));

        addCarrinho();

        dialogBinding.btnFechar.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.btnIrCarrinho.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivityUsuario.class);
            intent.putExtra("id", 2);
            startActivity(intent);
            finish();
        });

        builder.setView(dialogBinding.getRoot());

        dialog = builder.create();
        dialog.show();
    }


    @Override
    public void onClick(Produto produto) {
        Intent intent = new Intent(this, DetalhesProdutoActivity.class);
        intent.putExtra("produtoSelecionado", produto);
        startActivity(intent);
    }

    @Override
    public void onClickFavorito(Produto produto) {
        if (!idsFavoritos.contains(produto.getId())){
            idsFavoritos.add(produto.getId());
        }else{
            idsFavoritos.remove(produto.getId());
        }
        Favorito.salvar(idsFavoritos);
    }
}