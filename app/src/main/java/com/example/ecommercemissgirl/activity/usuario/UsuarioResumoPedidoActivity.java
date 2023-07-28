package com.example.ecommercemissgirl.activity.usuario;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.example.ecommercemissgirl.DAO.ItemPedidoDAO;
import com.example.ecommercemissgirl.R;
import com.example.ecommercemissgirl.databinding.ActivityUsuarioResumoPedidoBinding;
import com.example.ecommercemissgirl.helper.FirebaseHelper;
import com.example.ecommercemissgirl.model.Endereco;
import com.example.ecommercemissgirl.model.FormaPagamento;
import com.example.ecommercemissgirl.model.Pedido;
import com.example.ecommercemissgirl.model.StatusPedido;
import com.example.ecommercemissgirl.util.GetMask;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UsuarioResumoPedidoActivity extends AppCompatActivity {

    private ActivityUsuarioResumoPedidoBinding binding;

    private final List<Endereco> enderecoList = new ArrayList<>();

    private FormaPagamento formaPagamento;

    private ItemPedidoDAO itemPedidoDAO;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsuarioResumoPedidoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        itemPedidoDAO = new ItemPedidoDAO(this);

        recuperaEndereco();

        configClicks();

        getExtra();
    }

    private void getExtra() {
       formaPagamento = (FormaPagamento) getIntent().getExtras().getSerializable("pagamentoSelecionado");
       configDados();
    }

    private void configClicks() {
        binding.btnAlterarEndereco.setOnClickListener(v -> {
            resultLauncher.launch(new Intent(this, UsuarioSelecionaEnderecoActivity.class));
        });

        binding.btnAlterarPagamento.setOnClickListener(v -> finish());

        binding.btnFinalizar.setOnClickListener(v -> {
            if (this.formaPagamento.isCredito()) {
                Intent intent = new Intent(this, UsuarioPagamentoPedidoActivity.class);
                intent.putExtra("enderecoSelecionado", enderecoList.get(0));
                intent.putExtra("pagamentoSelecionado", formaPagamento);
                startActivity(intent);
            }else {
                finalizarPedido();
            }
        });
    }

    private void finalizarPedido() {
        Pedido pedido = new Pedido();
        pedido.setIdCliente(FirebaseHelper.getIdFirebase());
        pedido.setEndereco(enderecoList.get(0));
        pedido.setTotal(itemPedidoDAO.getTotalPedido());
        pedido.setPagamento(formaPagamento.getNome());
        pedido.setStatusPedido(StatusPedido.PENDENTE);

        if(formaPagamento.getTipoValor().equals("DESC")){
            pedido.setDesconto(formaPagamento.getValor());
        }else{
            pedido.setAcrescimo(formaPagamento.getValor());
        }

        pedido.setItemPedidoList(itemPedidoDAO.getList());

        pedido.salvar(true);

        itemPedidoDAO.limparCarrinho();

        Intent intent = new Intent(this, MainActivityUsuario.class);
        //Limpar as activitys para não consumir memória do dispositivo do usuário.
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("id", 1);
        startActivity(intent);

    }

    private void configDados() {
        ItemPedidoDAO itemPedidoDAO = new ItemPedidoDAO(this);

        binding.include.textTitulo.setText("Resumo Pedido");
        binding.include.include.ibVoltar.setOnClickListener(v -> finish());

        if(!enderecoList.isEmpty()){
            Endereco endereco = enderecoList.get(0);

            StringBuilder enderecoCompleto = new StringBuilder();
            enderecoCompleto.append(endereco.getLogradouro())
                    .append(", ")
                    .append(endereco.getNumero())
                    .append("\n")
                    .append(endereco.getBairro())
                    .append(", ")
                    .append(endereco.getLocalidade())
                    .append("/")
                    .append(endereco.getUf())
                    .append("\n")
                    .append("CEP: ")
                    .append(endereco.getCep());

            binding.textEnderecoEntrega.setText(enderecoCompleto);

            binding.btnAlterarEndereco.setText("Alterar endereço de entrega");
        }else {
            binding.textEnderecoEntrega.setText("Nenhum endereço cadastrado.");
            binding.btnAlterarEndereco.setText("Cadastrar endereço");
        }

        binding.textNomePagamento.setText(formaPagamento.getNome());

        if(formaPagamento.getTipoValor().equals("DESC")){
            binding.textTipoPagamento.setText("Desconto");
        }else{
            binding.textTipoPagamento.setText("Acréscimo");
        }

        double valorExtra = formaPagamento.getValor();

        binding.textValorTipoPagamento.setText(
                getString(R.string.valor, GetMask.getValor(valorExtra))
        );

        if(itemPedidoDAO.getTotalPedido() >= valorExtra){
            binding.textValorTotal.setText(getString(R.string.valor, GetMask.getValor(itemPedidoDAO.getTotalPedido() - valorExtra)));
            binding.textValor.setText(getString(R.string.valor, GetMask.getValor(itemPedidoDAO.getTotalPedido() - valorExtra)));
        }else{
            binding.textValorTotal.setText(getString(R.string.valor, GetMask.getValor(0)));
            binding.textValor.setText(getString(R.string.valor, GetMask.getValor(0)));
        }
    }

    private void recuperaEndereco() {
        DatabaseReference enderecoRef = FirebaseHelper.getDatabaseReference()
                .child("enderecos")
                .child(FirebaseHelper.getIdFirebase());
        enderecoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    Endereco endereco = ds.getValue(Endereco.class);
                    enderecoList.add(endereco);
                }

                binding.progressBar.setVisibility(View.GONE);
                Collections.reverse(enderecoList);

                configDados();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private final ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK){
                    Endereco endereco = (Endereco) result.getData().getSerializableExtra("enderecoSelecionado");
                    enderecoList.add(0, endereco);
                    configDados();
                }
            }
    );

}