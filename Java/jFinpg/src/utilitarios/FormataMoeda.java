package utilitarios;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

// Utilizada no Menu Estoque para Formulario de Venda e Formulário Pagamento
public class FormataMoeda {

    public String formatarDoubleParaMoeda(double valor) {
        @SuppressWarnings("deprecation")
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("pt", "BR"));
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return nf.format(valor);
    }

    public double formatarMoedaParaDouble(String valor) {
        if (valor != null && !valor.trim().isEmpty()) {
            // Remove "R$" e espaços em branco
            valor = valor.replace("R$", "").replace(" ", "").trim();
            // Substitui a vírgula por ponto
            valor = valor.replace(",", ".");
            // Tenta converter usando NumberFormat
            try {
                NumberFormat format = NumberFormat.getInstance(Locale.US);
                return format.parse(valor).doubleValue();
            } catch (ParseException e) {
                System.err.println("Erro ao converter valor: " + e.getMessage());
            }
        }
        return 0.0; // Ou lance uma exceção se preferir
    }
    
     public static String formatarMoeda(double valor) {
        // Define o Locale para o Brasil (pt-BR) para formatar como Real (R$)
        @SuppressWarnings("deprecation")
        Locale brasil = new Locale("pt", "BR");

        // Cria um objeto NumberFormat para moeda com o Locale brasileiro
        NumberFormat nf = NumberFormat.getCurrencyInstance(brasil);

        // Formata o valor numérico como moeda
        return nf.format(valor);
    }
    
    
    public static String formatarValorParaExibicao(double valor) {
        // Cria um objeto DecimalFormat com o padrão desejado
        DecimalFormat df = new DecimalFormat("#,##0.00");

        // Formata o valor numérico para exibição
        return df.format(valor);
    }
    
    
}
