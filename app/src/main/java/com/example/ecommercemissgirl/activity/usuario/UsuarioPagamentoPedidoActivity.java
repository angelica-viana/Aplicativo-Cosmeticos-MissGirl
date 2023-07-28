package com.example.ecommercemissgirl.activity.usuario;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.ecommercemissgirl.DAO.ItemDAO;
import com.example.ecommercemissgirl.DAO.ItemPedidoDAO;
import com.example.ecommercemissgirl.api.MercadoPagoService;
import com.example.ecommercemissgirl.databinding.ActivityUsuarioPagamentoPedidoBinding;
import com.example.ecommercemissgirl.helper.FirebaseHelper;
import com.example.ecommercemissgirl.model.Endereco;
import com.example.ecommercemissgirl.model.FormaPagamento;
import com.example.ecommercemissgirl.model.ItemPedido;
import com.example.ecommercemissgirl.model.Loja;
import com.example.ecommercemissgirl.model.Pedido;
import com.example.ecommercemissgirl.model.Produto;
import com.example.ecommercemissgirl.model.StatusPedido;
import com.example.ecommercemissgirl.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mercadopago.android.px.configuration.AdvancedConfiguration;
import com.mercadopago.android.px.core.MercadoPagoCheckout;
import com.mercadopago.android.px.model.Payment;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UsuarioPagamentoPedidoActivity extends AppCompatActivity {

    private ActivityUsuarioPagamentoPedidoBinding binding;

    private final int REQUEST_MERCADO_PAGO = 100;

    private FormaPagamento formaPagamento;
    private Endereco enderecoSelecionado;
    private Usuario usuario;
    private Loja loja;

    private ItemPedidoDAO itemPedidoDAO;
    private List<ItemPedido> itemPedidoList = new ArrayList<>();

    private Retrofit retrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsuarioPagamentoPedidoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        recuperaDados();

        iniciaRetrofit();

    }

    private void configJSON() {
        JsonObject dados = new JsonObject();

        JsonArray itemsList = new JsonArray();

        JsonObject payer = new JsonObject();
        JsonObject phone = new JsonObject();
        JsonObject address = new JsonObject();
        JsonObject payment_methods = new JsonObject();

        JsonObject removerBoleto = new JsonObject();
        removerBoleto.addProperty("id", "bolbradesco");

        JsonObject removerLoterica = new JsonObject();
        removerLoterica.addProperty("id", "pec");

        JsonArray excluded_payment_methods = new JsonArray();
        excluded_payment_methods.add(removerBoleto);
        excluded_payment_methods.add(removerLoterica);

        String telefone = usuario.getTelefone()
                .replace("(", "")
                .replace(")", "")
                .replace(" ", "");
        phone.addProperty("area_code", telefone.substring(0, 2));
        phone.addProperty("number", telefone.substring(2, 12));

        address.addProperty("street_name", enderecoSelecionado.getLogradouro());
        if (enderecoSelecionado.getNumero() != null) {
            address.addProperty("street_number", enderecoSelecionado.getNumero());
        }
        address.addProperty("zip_code", enderecoSelecionado.getCep());

        payment_methods.addProperty("installments", loja.getParcelas());
        payment_methods.add("excluded_payment_methods", excluded_payment_methods);

        JsonObject item;
        for (ItemPedido itemPedido : itemPedidoList) {
            Produto produto = itemPedidoDAO.getProduto(itemPedido.getId());

            item = new JsonObject();

            item.addProperty("title", produto.getTitulo());
            item.addProperty("currency_id", "BRL");
            item.addProperty("picture_url", produto.getUrlsImagens().get(0).getCaminhoImagem());
            item.addProperty("quantity", itemPedido.getQuantidade());
            item.addProperty("unit_price", produto.getValorAtual());

            itemsList.add(item);

            itemPedido.setNomeProduto(produto.getTitulo());

        }

        dados.add("items", itemsList);

        payer.addProperty("name", usuario.getNome());
        payer.addProperty("email", usuario.getEmail());
        payer.add("phone", phone);
        payer.add("address", address);

        dados.add("payer", payer);
        dados.add("payment_methods", payment_methods);

        efetuarPagamento(dados);
    }

    private void iniciaRetrofit() {
        retrofit = new Retrofit
                .Builder()
                .baseUrl("https://api.mercadopago.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private void efetuarPagamento(JsonObject dados) {
        String url = "checkout/preferences?access_token=" + loja.getAccessToken();

        MercadoPagoService mercadoPagoService = retrofit.create(MercadoPagoService.class);
        Call<JsonObject> call = mercadoPagoService.efetuarPagamento(url, dados);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                String id = response.body().get("id").getAsString();
                continuaPagamento(id);
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {

            }
        });
    }

    private void continuaPagamento(String idPagamento) {
        final AdvancedConfiguration advancedConfiguration =
                new AdvancedConfiguration.Builder().setBankDealsEnabled(false).build();

        new MercadoPagoCheckout
                .Builder(loja.getPublicKey(), idPagamento)
                .setAdvancedConfiguration(advancedConfiguration).build()
                .startPayment(this, REQUEST_MERCADO_PAGO);
    }

    private void recuperaDados() {
        itemPedidoDAO = new ItemPedidoDAO(this);
        ItemDAO itemDAO = new ItemDAO(this);
        itemPedidoList = itemPedidoDAO.getList();

        recuperaUsuario();

        getExtra();
    }

    private void getExtra() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            enderecoSelecionado = (Endereco) bundle.getSerializable("enderecoSelecionado");
            formaPagamento = (FormaPagamento) bundle.getSerializable("pagamentoSelecionado");
        }
    }

    private void recuperaUsuario() {
        DatabaseReference usuarioRef = FirebaseHelper.getDatabaseReference()
                .child("usuarios")
                .child(FirebaseHelper.getIdFirebase());
        usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usuario = snapshot.getValue(Usuario.class);

                if (usuario != null) {
                    recuperaLoja();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void recuperaLoja() {
        DatabaseReference lojaRef = FirebaseHelper.getDatabaseReference()
                .child("loja");
        lojaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loja = snapshot.getValue(Loja.class);

                if (loja != null) {
                    configJSON();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void validaRetorno(Payment payment) {
        // approved, rejected, in_process

        String status = payment.getPaymentStatus();
        String statusDetail = payment.getPaymentStatusDetail();

        switch (status) {
            case "approved":
                finalizarPedido(StatusPedido.APROVADO);
                break;
            case "rejected":
                finalizarPedido(StatusPedido.CANCELADO);
                switch (statusDetail) {
                    case "cc_rejected_bad_filled_card_number":
                        Toast.makeText(this, "NÃºmero do cartÃ£o invÃ¡lido!", Toast.LENGTH_LONG).show();
                        break;
                    case "cc_rejected_bad_filled_date":
                        Toast.makeText(this, "Data de vencimento invalida!", Toast.LENGTH_LONG).show();
                        break;
                    case "cc_rejected_bad_filled_other":
                        Toast.makeText(this, "Algum dado do cartÃ£o inserido Ã© invalido!", Toast.LENGTH_LONG).show();
                        break;
                    case "cc_rejected_bad_filled_security_code":
                        Toast.makeText(this, "CÃ³digo de seguranÃ§a do cartÃ£o Ã© invalido!", Toast.LENGTH_LONG).show();
                        break;
                    case "cc_rejected_call_for_authorize":
                        Toast.makeText(this, "VocÃª deve autorizar a operadora do seu cartÃ£o a liberar o pagamento do valor ao Mercado Pago.", Toast.LENGTH_LONG).show();
                        break;
                    case "cc_rejected_card_disabled":
                        Toast.makeText(this, "Ligue para a operadora do seu cartÃ£o para ativar seu cartÃ£o. O telefone estÃ¡ no verso do seu cartÃ£o.", Toast.LENGTH_LONG).show();
                        break;
                    case "cc_rejected_max_attempts":
                        Toast.makeText(this, "VocÃª atingiu o limite de tentativas permitido.", Toast.LENGTH_LONG).show();
                        break;
                    case "cc_rejected_insufficient_amount":
                        Toast.makeText(this, "Pagamento negado por falta de saldo.", Toast.LENGTH_LONG).show();
                        break;
                    default:
                        Toast.makeText(this, "NÃ£o pudemos processar seu pagamento, tente novamente mais tarde.", Toast.LENGTH_LONG).show();
                        break;
                }
                break;
            case "in_process":
                finalizarPedido(StatusPedido.PENDENTE);
                break;
        }

    }

    private void finalizarPedido(StatusPedido statusPedido) {
        Pedido pedido = new Pedido();
        pedido.setIdCliente(FirebaseHelper.getIdFirebase());
        pedido.setEndereco(enderecoSelecionado);
        pedido.setTotal(itemPedidoDAO.getTotalPedido());
        pedido.setPagamento(formaPagamento.getNome());
        pedido.setStatusPedido(statusPedido);

        if (formaPagamento.getTipoValor().equals("DESC")) {
            pedido.setDesconto(formaPagamento.getValor());
        } else {
            pedido.setAcrescimo(formaPagamento.getValor());
        }

        pedido.setItemPedidoList(itemPedidoDAO.getList());

        pedido.salvar(true);

        itemPedidoDAO.limparCarrinho();

        Intent intent = new Intent(this, MainActivityUsuario.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("id", 1);
        startActivity(intent);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MERCADO_PAGO) {
            if (resultCode == MercadoPagoCheckout.PAYMENT_RESULT_CODE) {
                Payment payment = (Payment) data.getSerializableExtra(MercadoPagoCheckout.EXTRA_PAYMENT_RESULT);

                validaRetorno(payment);
            }else {
                finish();
            }
        }
    }
}