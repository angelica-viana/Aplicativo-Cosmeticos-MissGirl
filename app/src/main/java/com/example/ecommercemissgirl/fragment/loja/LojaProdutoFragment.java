package com.example.ecommercemissgirl.fragment.loja;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.example.ecommercemissgirl.R;
import com.example.ecommercemissgirl.activity.loja.LojaFormProdutoActivity;
import com.example.ecommercemissgirl.adapter.LojaProdutoAdapter;
import com.example.ecommercemissgirl.databinding.DialogLojaProdutoBinding;
import com.example.ecommercemissgirl.databinding.FragmentLojaProdutoBinding;
import com.example.ecommercemissgirl.helper.FirebaseHelper;
import com.example.ecommercemissgirl.model.Produto;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LojaProdutoFragment extends Fragment implements LojaProdutoAdapter.OnClickLister {

    private final List<Produto> produtoList = new ArrayList<>();

    private LojaProdutoAdapter lojaProdutoAdapter;

    private FragmentLojaProdutoBinding binding;

    private AlertDialog dialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentLojaProdutoBinding.inflate(inflater, container, false);
        return  binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        configClicks();

        configRv();
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperaProdutos();
    }

    private void configClicks() {
        binding.toolbar.btnAdd.setOnClickListener(
                v -> startActivity(new Intent(requireContext(), LojaFormProdutoActivity.class)));
        }

    private void configRv(){
        binding.rvProdutos.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.rvProdutos.setHasFixedSize(true);
        lojaProdutoAdapter = new LojaProdutoAdapter(R.layout.item_produto_adapter, produtoList, requireContext(), false, new ArrayList<>(), this, null);
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
                    produtoList.add(produto);
                }

                listEmpty();

                binding.progressBar.setVisibility(View.GONE);
                Collections.reverse(produtoList);
                lojaProdutoAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void listEmpty() {
        if(produtoList.isEmpty()){
            binding.textInfo.setText("Nenhum produto cadastrado.");
        }else {
            binding.textInfo.setText("");
        }
    }

    private void showDialog(Produto produto){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.CustemAlertDialog);

        DialogLojaProdutoBinding dialogBinding = DialogLojaProdutoBinding
                .inflate(LayoutInflater.from(requireContext()));

        dialogBinding.cbRascunho.setChecked(produto.isRascunho());

        for (int i = 0; i < produto.getUrlsImagens().size(); i++) {
            if (produto.getUrlsImagens().get(i).getIndex() == 0) {
                Glide.with(requireContext())
                        .load(produto.getUrlsImagens().get(i).getCaminhoImagem())
                        .into(dialogBinding.imagemProduto);
            }
        }

            dialogBinding.cbRascunho.setOnCheckedChangeListener((check, b) -> {
                produto.setRascunho(check.isChecked());
                produto.salvar(false);
            });

        dialogBinding.btnEditar.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), LojaFormProdutoActivity.class);
            intent.putExtra("produtoSelecionado", produto);
            startActivity(intent);
            dialog.dismiss();
        });

        dialogBinding.btnRemover.setOnClickListener(v -> {
            produto.remover();
            dialog.dismiss();
            Toast.makeText(requireContext(), "Produto removido com sucesso!", Toast.LENGTH_SHORT).show();

           listEmpty();
        });

        dialogBinding.txtNomeProduto.setText(produto.getTitulo());

        dialogBinding.btnFechar.setOnClickListener(v -> dialog.dismiss());

        builder.setView(dialogBinding.getRoot());

        dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onClick(Produto produto) {
        showDialog(produto);
    }
}