package controller;

import dao.ParcelamentoDAO;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import model.Parcelamento;

public class ParcelamentoController {

    public void gerarParcelas(int parcelas, BigDecimal valorCompra, BigDecimal valorParcela, String tipo, String cliforn, String vcliforn, String documento, String classif, String descricao, String status, String prev, String opc, String corrige, String emissao, String vcto) throws SQLException, ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calEmi = Calendar.getInstance();
        Calendar calVcto = Calendar.getInstance();

        calEmi.setTime(sdf.parse(emissao));
        calVcto.setTime(sdf.parse(vcto));

        ParcelamentoDAO dao = new ParcelamentoDAO();

        for (int i = 1; i <= parcelas; i++) {
            Parcelamento p = new Parcelamento();

            String tipoUpper = tipo.toUpperCase().trim();
            if (tipoUpper.equals("S")) { // Saí­da - Contas à Pagar
                p.setRecurso("0079");
                p.setVrecurso("2.001.002");
            } else if (tipoUpper.equals("E")) { // Entrada - Contas à Receber
                p.setRecurso("0022");
                p.setVrecurso("1.002.001");
            } else {
                throw new IllegalArgumentException("Tipo inválido! Use 'E' para Entrada ou 'S' para Saí­da.");
            }

            p.setClifor(cliforn);
            p.setVCliFor(vcliforn);
            p.setDtlancto(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            
            // Formatando datas para o banco (yyyy-MM-dd)
            SimpleDateFormat sdfBanco = new SimpleDateFormat("yyyy-MM-dd");
            p.setDtlancto(sdfBanco.format(calEmi.getTime())); // igualei data de lancto com dtemissão
            p.setDtEmi(sdfBanco.format(calEmi.getTime()));
            p.setDtVcto(sdfBanco.format(calVcto.getTime()));
            
            p.setDocumento(documento + " - " + i + "/" + parcelas);
            p.setClassif(classif);
            p.setDescricao(descricao + " (" + i + "/" + parcelas + ")");
            p.setValor(valorParcela);
            p.setStatusMov(status);
            p.setPrev(prev);

            dao.salvar(p);

            // Próximas datas
            if (corrige.equals("S")) {
                avancarData(calEmi, opc);
            }
            avancarData(calVcto, opc);
        }
    }

    private void avancarData(Calendar cal, String opc) {
        switch (opc) {
            case "1" -> cal.add(Calendar.MONTH, 1);
            case "2" -> cal.add(Calendar.MONTH, 3);
            case "3" -> cal.add(Calendar.MONTH, 2);
            case "4" -> cal.add(Calendar.YEAR, 1);
            case "5" -> cal.add(Calendar.WEEK_OF_YEAR, 1);
            case "6" -> cal.add(Calendar.DAY_OF_MONTH, 1);
            case "7" -> cal.add(Calendar.MONTH, 4);
        }
    }
}