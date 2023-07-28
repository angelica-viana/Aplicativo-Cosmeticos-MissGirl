package com.example.ecommercemissgirl.model;

public enum StatusPedido {
    PENDENTE, APROVADO, CANCELADO;

    public static String getStatus(StatusPedido status) {
        String statusPedido;
        switch (status){
            case PENDENTE:
                statusPedido = "Pendente";
                break;
            case APROVADO:
                statusPedido = "Aprovado";
                break;
            default:
                statusPedido = "Cancelado";
                break;
        }
        return statusPedido;
    }

}
