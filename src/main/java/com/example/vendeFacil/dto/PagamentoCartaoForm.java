package com.example.vendeFacil.dto;

// Dados digitados na tela de pagamento quando o cliente paga com cartão de crédito.
// São apenas repassados ao gateway (Asaas) para processar a cobrança e NUNCA são
// persistidos no banco. Os campos extras (CPF, CEP, telefone, número) são exigidos
// pelo Asaas no objeto creditCardHolderInfo.
public class PagamentoCartaoForm {

    private String numero;          // número do cartão
    private String titular;         // nome impresso no cartão
    private String validade;        // MM/AA
    private String cvv;
    private String cpf;             // CPF do titular
    private String cep;             // CEP do titular
    private String telefone;        // telefone do titular
    private String numeroEndereco;  // número do endereço do titular

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getTitular() {
        return titular;
    }

    public void setTitular(String titular) {
        this.titular = titular;
    }

    public String getValidade() {
        return validade;
    }

    public void setValidade(String validade) {
        this.validade = validade;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getNumeroEndereco() {
        return numeroEndereco;
    }

    public void setNumeroEndereco(String numeroEndereco) {
        this.numeroEndereco = numeroEndereco;
    }
}
