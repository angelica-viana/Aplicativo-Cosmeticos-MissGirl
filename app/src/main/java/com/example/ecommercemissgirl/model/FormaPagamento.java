package com.example.ecommercemissgirl.model;

import com.example.ecommercemissgirl.helper.FirebaseHelper;
import com.google.firebase.database.DatabaseReference;

import java.io.Serializable;

public class FormaPagamento implements Serializable {

    private String id;
    private String nome;
    private String descricao;
    private double valor;
    private String tipoValor; // "DESC ou ACRES"
    private boolean credito = false;

    public FormaPagamento() {
        DatabaseReference pagamentoRef = FirebaseHelper.getDatabaseReference();
        this.setId(pagamentoRef.push().getKey());
    }

    public void salvar() {
        DatabaseReference pagamentoRef = FirebaseHelper.getDatabaseReference()
                .child("formapagamento")
                .child(this.getId());
        pagamentoRef.setValue(this);
    }

    public void remover() {
        DatabaseReference pagamentoRef = FirebaseHelper.getDatabaseReference()
                .child("formapagamento")
                .child(this.getId());
        pagamentoRef.removeValue();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public String getTipoValor() {
        return tipoValor;
    }

    public void setTipoValor(String tipoValor) {
        this.tipoValor = tipoValor;
    }

    public boolean isCredito() {
        return credito;
    }

    public void setCredito(boolean credito) {
        this.credito = credito;
    }
}
