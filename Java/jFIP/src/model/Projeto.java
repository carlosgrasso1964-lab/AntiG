package model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class Projeto {

    private int id;
    private String nome;
    private LocalDate dataInicioPrevista;
    private LocalDate dataFimPrevista;
    private LocalDate dataInicioRealizada;
    private LocalDate dataFimRealizada;
    private String status;
    private BigDecimal valorPrevistoTotal = BigDecimal.ZERO;
    private BigDecimal valorRealizadoTotal = BigDecimal.ZERO;

    public Projeto() {
    }

    public Projeto(int id, String nome, LocalDate dataInicioPrevista, LocalDate dataFimPrevista, LocalDate dataInicioRealizada, LocalDate dataFimRealizada, String status) {
        this.id = id;
        this.nome = nome;
        this.dataInicioPrevista = dataInicioPrevista;
        this.dataFimPrevista = dataFimPrevista;
        this.dataInicioRealizada = dataInicioRealizada;
        this.dataFimRealizada = dataFimRealizada;
        this.status = status;
        this.valorPrevistoTotal = BigDecimal.ZERO;
        this.valorRealizadoTotal = BigDecimal.ZERO;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDate getDataInicioPrevista() {
        return dataInicioPrevista;
    }

    public void setDataInicioPrevista(LocalDate dataInicioPrevista) {
        this.dataInicioPrevista = dataInicioPrevista;
    }

    public LocalDate getDataFimPrevista() {
        return dataFimPrevista;
    }

    public void setDataFimPrevista(LocalDate dataFimPrevista) {
        this.dataFimPrevista = dataFimPrevista;
    }

    public LocalDate getDataInicioRealizada() {
        return dataInicioRealizada;
    }

    public void setDataInicioRealizada(LocalDate dataInicioRealizada) {
        this.dataInicioRealizada = dataInicioRealizada;
    }

    public LocalDate getDataFimRealizada() {
        return dataFimRealizada;
    }

    public void setDataFimRealizada(LocalDate dataFimRealizada) {
        this.dataFimRealizada = dataFimRealizada;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return nome; // Exibe apenas o nome no JComboBox
    }

    public BigDecimal getValorPrevistoTotal() {
        return valorPrevistoTotal == null ? BigDecimal.ZERO : valorPrevistoTotal;
    }

    public void setValorPrevistoTotal(BigDecimal valorPrevistoTotal) {
        this.valorPrevistoTotal = valorPrevistoTotal != null ? valorPrevistoTotal : BigDecimal.ZERO;
    }

    public BigDecimal getValorRealizadoTotal() {
        return valorRealizadoTotal == null ? BigDecimal.ZERO : valorRealizadoTotal;
    }

    public void setValorRealizadoTotal(BigDecimal valorRealizadoTotal) {
        this.valorRealizadoTotal = valorRealizadoTotal != null ? valorRealizadoTotal : BigDecimal.ZERO;
    }

// (Opcional, mas muito útil) Percentual financeiro do projeto inteiro
    public BigDecimal getPercentualFinanceiro() {
        if (valorPrevistoTotal == null || valorPrevistoTotal.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return valorRealizadoTotal.divide(valorPrevistoTotal, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP);
    }
}
