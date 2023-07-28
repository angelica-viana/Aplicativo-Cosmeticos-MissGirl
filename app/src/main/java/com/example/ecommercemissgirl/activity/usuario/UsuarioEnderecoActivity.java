package com.example.ecommercemissgirl.activity.usuario;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.example.ecommercemissgirl.R;
import com.example.ecommercemissgirl.adapter.EnderecoAdapter;
import com.example.ecommercemissgirl.databinding.ActivityUsuarioEnderecoBinding;
import com.example.ecommercemissgirl.databinding.DialogDeleteBinding;
import com.example.ecommercemissgirl.helper.FirebaseHelper;
import com.example.ecommercemissgirl.model.Categoria;
import com.example.ecommercemissgirl.model.Endereco;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.tsuryo.swipeablerv.SwipeLeftRightCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UsuarioEnderecoActivity extends AppCompatActivity implements EnderecoAdapter.OnClickListener {

    private ActivityUsuarioEnderecoBinding binding;

    private EnderecoAdapter enderecoAdapter;

    private final List<Endereco> enderecoList = new ArrayList<>();

    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsuarioEnderecoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        iniciaComponentes();

        configClicks();

        configRv();
    }

    @Override
    protected void onStart() {
        super.onStart();

        recuperaEndereco();
    }

    private void configRv() {
        binding.rvEnderecos.setLayoutManager(new LinearLayoutManager(this));
        binding.rvEnderecos.setHasFixedSize(true);
        enderecoAdapter = new EnderecoAdapter(enderecoList, this,this);
        binding.rvEnderecos.setAdapter(enderecoAdapter);

        binding.rvEnderecos.setListener(new SwipeLeftRightCallback.Listener() {
            @Override
            public void onSwipedLeft(int position) {
            }

            @Override
            public void onSwipedRight(int position) {
                showDialogDelete(enderecoList.get(position));
            }
        });
    }

    private void showDialogDelete(Endereco endereco){
        AlertDialog.Builder builder = new AlertDialog.Builder(
                this, R.style.CustemAlertDialog2);

        DialogDeleteBinding deleteBinding = DialogDeleteBinding
                .inflate(LayoutInflater.from(this));

        deleteBinding.btnFechar.setOnClickListener(v -> {
            dialog.dismiss();
            enderecoAdapter.notifyDataSetChanged();
        });

        deleteBinding.textTitulo.setText("Deseja remover este endereço?");

        deleteBinding.btnSim.setOnClickListener(v -> {
            enderecoList.remove(endereco);

            if(enderecoList.isEmpty()){
                binding.textInfo.setText("Nenhum endereço cadastrado.");
            }else{
                binding.textInfo.setText("");
            }

            endereco.delete();

            enderecoAdapter.notifyDataSetChanged();

            dialog.dismiss();
        });

        builder.setView(deleteBinding.getRoot());

        dialog = builder.create();
        dialog.show();

    }

    private void recuperaEndereco() {
        DatabaseReference enderecoRef = FirebaseHelper.getDatabaseReference()
                .child("enderecos")
                .child(FirebaseHelper.getIdFirebase());
        enderecoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    enderecoList.clear();
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Endereco endereco = ds.getValue(Endereco.class);
                        enderecoList.add(endereco);
                    }
                    binding.textInfo.setText("");
                }else {
                    binding.textInfo.setText("Nenhum endereço cadastrado");
                }

                binding.progressBar.setVisibility(View.GONE);
                Collections.reverse(enderecoList);
                enderecoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void configClicks() {
        binding.include.btnAdd.setOnClickListener(v ->
                startActivity(new Intent(this, UsuarioFormEnderecoActivity.class)));
    }

    private void iniciaComponentes() {
        binding.include.textTitulo.setText("Meus endereços");
        binding.include.include.ibVoltar.setOnClickListener(v -> finish());
    }

    @Override
    public void onClick(Endereco endereco) {
        Intent intent = new Intent(this, UsuarioFormEnderecoActivity.class);
        intent.putExtra("enderecoSelecionado", endereco);
        startActivity(intent);
    }
}