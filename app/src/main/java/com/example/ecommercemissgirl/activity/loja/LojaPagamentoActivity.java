package com.example.ecommercemissgirl.activity.loja;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import com.example.ecommercemissgirl.R;
import com.example.ecommercemissgirl.adapter.LojaPagamentoAdapter;
import com.example.ecommercemissgirl.databinding.ActivityLojaPagamentoBinding;
import com.example.ecommercemissgirl.databinding.DialogDeleteBinding;
import com.example.ecommercemissgirl.helper.FirebaseHelper;
import com.example.ecommercemissgirl.model.FormaPagamento;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.tsuryo.swipeablerv.SwipeLeftRightCallback;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LojaPagamentoActivity extends AppCompatActivity implements LojaPagamentoAdapter.OnClick {


    private ActivityLojaPagamentoBinding binding;

    private LojaPagamentoAdapter lojaPagamentoAdapter;

    private final List<FormaPagamento> formaPagamentoList = new ArrayList<>();

    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLojaPagamentoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        iniciaComponentes();

        configClicks();

        configRV();

        recuperaFormaPagamento();
    }

    private void recuperaFormaPagamento() {
        DatabaseReference pagamentoRef = FirebaseHelper.getDatabaseReference()
                .child("formapagamento");
        pagamentoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    formaPagamentoList.clear();
                    for (DataSnapshot ds : snapshot.getChildren()){
                        FormaPagamento formaPagamento = ds.getValue(FormaPagamento.class);
                        formaPagamentoList.add(formaPagamento);
                    }
                    binding.textInfo.setText("");
                }else {
                    binding.textInfo.setText("Nenhuma forma de pagamento cadastrada.");
                }

                binding.progressBar.setVisibility(View.GONE);
                Collections.reverse(formaPagamentoList);
                lojaPagamentoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void configRV() {
        binding.rvPagamentos.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPagamentos.setHasFixedSize(true);
        lojaPagamentoAdapter = new LojaPagamentoAdapter(formaPagamentoList, this, this);
        binding.rvPagamentos.setAdapter(lojaPagamentoAdapter);

        binding.rvPagamentos.setListener(new SwipeLeftRightCallback.Listener() {
            @Override
            public void onSwipedLeft(int position) {
            }

            @Override
            public void onSwipedRight(int position) {
                showDialogDelete(formaPagamentoList.get(position));
            }
        });
    }

    private void showDialogDelete(FormaPagamento formaPagamento){
        AlertDialog.Builder builder = new AlertDialog.Builder(
                this, R.style.CustemAlertDialog2);

        DialogDeleteBinding deleteBinding = DialogDeleteBinding
                .inflate(LayoutInflater.from(this));

        deleteBinding.btnFechar.setOnClickListener(v -> {
            dialog.dismiss();
            lojaPagamentoAdapter.notifyDataSetChanged();
        });

        deleteBinding.textTitulo.setText("Deseja remover este pagamento?");

        deleteBinding.btnSim.setOnClickListener(v -> {
            formaPagamentoList.remove(formaPagamento);

            if(formaPagamentoList.isEmpty()){
                binding.textInfo.setText("Nenhuma forma de pagamento cadastrada.");
            }else{
                binding.textInfo.setText("");
            }

            formaPagamento.remover();

            lojaPagamentoAdapter.notifyDataSetChanged();

            dialog.dismiss();
        });

        builder.setView(deleteBinding.getRoot());

        dialog = builder.create();
        dialog.show();
    }

    private void configClicks() {
        binding.include.btnAdd.setOnClickListener(v ->
                        resultLauncher.launch(
                                new Intent(this, LojaFormPagamentoActivity.class))
        );
    }

    private final ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK){
                   FormaPagamento pagamento = (FormaPagamento) result.getData().getSerializableExtra("novoPagamento");
                   formaPagamentoList.add(pagamento);
                   lojaPagamentoAdapter.notifyItemInserted(formaPagamentoList.size());
                   binding.textInfo.setText("");
                }
            }
    );

    private void iniciaComponentes(){
        binding.include.textTitulo.setText("Formas de pagamento");
        binding.include.include.ibVoltar.setOnClickListener(v -> finish());
    }

    @Override
    public void onClickListener(FormaPagamento formaPagamento) {
    Intent intent = new Intent(this, LojaFormPagamentoActivity.class);
    intent.putExtra("formaPagamentoSelecionada", formaPagamento);
    startActivity(intent);
    }
}



