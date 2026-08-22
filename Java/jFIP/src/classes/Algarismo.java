
package classes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Algarismo {
     public static int Alga() {
        DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy");
        Date date = new Date();
        String pega = dateFormat.format(date);
        int num = Integer.parseInt(pega);
        int soma = 0;
        // Primeira soma dos dígitos da data
        while (num > 0) {
            soma += (num % 10); // Ex.: 2 + 9 + 0 + 9 + 2 + 0 + 2 + 5 = 29
            num /= 10;
        }
        // Soma dos dígitos até obter um único dígito
        while (soma > 9) {
            int temp = soma;
            soma = 0;
            while (temp > 0) {
                soma += (temp % 10); // Ex.: 2 + 9 = 11, depois 1 + 1 = 2
                temp /= 10;
            }
        }
        return soma; // Ex.: 2
    }
}