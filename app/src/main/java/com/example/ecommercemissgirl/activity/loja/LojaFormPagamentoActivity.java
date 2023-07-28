package com.example.ecommercemissgirl.activity.loja;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.ecommercemissgirl.R;
import com.example.ecommercemissgirl.databinding.ActivityLojaFormPagamentoBinding;
import com.example.ecommercemissgirl.model.FormaPagamento;

import java.util.Locale;

public class LojaFormPagamentoActivity extends AppCompatActivity {

    private ActivityLojaFormPagamentoBinding binding;

    private FormaPagamento formaPagamento;
    private String tipoValor = null;

    private boolean novoPagamento = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLojaFormPagamentoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        iniciaComponentes();

        configClicks();

        getExtra();
    }

    private void getExtra() {
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            formaPagamento = (FormaPagamento) bundle.getSerializable("formaPagamentoSelecionada");
            configDados();
        }
    }

    private void configDados() {
        novoPagamento = false;

        binding.edtFormaPagamento.setText(formaPagamento.getNome());
        binding.edtDescricaoPagamento.setText(formaPagamento.getDescricao());
        binding.edtValor.setText(String.valueOf(formaPagamento.getValor() * 10));

        if(formaPagamento.getTipoValor().equals("DESC")){
            binding.rgValor.check(R.id.rbDesconto);
        }else{
            binding.rgValor.check(R.id.rbAcrescimo);
        }

        binding.cbCredito.setChecked(formaPagamento.isCredito());
    }

    private void configClicks() {
        binding.include.include.ibVoltar.setOnClickListener(v -> finish());
        binding.include.btnSalvar.setOnClickListener(v -> validaDados());

        binding.rgValor.setOnCheckedChangeListener((group, checkedId) -> {
            if(checkedId == R.id.rbDesconto){
                tipoValor = "DESC";
            }else if(checkedId == R.id.rbAcrescimo) {
                tipoValor = "ACRES";
            }
        } );
    }

    private void  validaDados() {
        String nome = binding.edtFormaPagamento.getText().toString().trim();
        String descricao = binding.edtDescricaoPagamento.getText().toString().trim();
        double valor = (double) binding.edtValor.getRawValue() / 100;

        if(!nome.isEmpty()) {
            if(!descricao.isEmpty()) {

                ocultaTeclado();

                binding.progressBar.setVisibility(View.VISIBLE);

                if (formaPagamento == null) formaPagamento = new FormaPagamento();
                formaPagamento.setNome(nome);
                formaPagamento.setDescricao(descricao);
                formaPagamento.setValor(valor);
                formaPagamento.setTipoValor(tipoValor);
                formaPagamento.setCredito(binding.cbCredito.isChecked());

                if(formaPagamento.getTipoValor() != null){
                    formaPagamento.salvar();

                    if(novoPagamento){
                        Intent intent = new Intent();
                        intent.putExtra("novoPagamento", formaPagamento);
                        setResult(RESULT_OK, intent);
                        finish();
                    }else {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Forma de pagamento salva com sucesso.", Toast.LENGTH_SHORT).show();
                 }

                }else {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Selecione o tipo do valor", Toast.LENGTH_SHORT).show();
                }

            }else {
                binding.edtDescricaoPagamento.requestFocus();
                binding.edtDescricaoPagamento.setError("Informação obrigatória.");
            }
        }else {
            binding.edtFormaPagamento.requestFocus();
            binding.edtFormaPagamento.setError("Informação obrigatória.");
        }
    }

    //Oculta o teclado do dispositivo
    private void ocultaTeclado(){
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(binding.edtValor.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void iniciaComponentes() {
        binding.edtValor.setLocale(new Locale("PT", "br" ));
        binding.include.textTitulo.setText("Forma de pagamento");
    }
}