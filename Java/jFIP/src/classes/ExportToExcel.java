package classes;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.Vector;

public class ExportToExcel {

    public void exportTable(JTable table, String filePath) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Fluxo de Caixa");

        // === CABEÇALHO ===
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < table.getColumnCount(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(table.getColumnName(i));

            // Estilo do cabeçalho
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            cell.setCellStyle(headerStyle);
        }

        // === DADOS ===
        CellStyle currencyStyle = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        currencyStyle.setDataFormat(format.getFormat("R$ #,##0.00"));
        currencyStyle.setAlignment(HorizontalAlignment.RIGHT);

        for (int row = 0; row < table.getRowCount(); row++) {
            Row excelRow = sheet.createRow(row + 1);
            for (int col = 0; col < table.getColumnCount(); col++) {
                Cell cell = excelRow.createCell(col);
                Object value = table.getValueAt(row, col);

                if (value != null) {
                    String strValue = value.toString().trim();
                    if (strValue.matches("-?\\d{1,3}(\\.\\d{3})*(,\\d{1,2})?")) {
                        // É número formatado (ex: 1.234,56)
                        try {
                            double num = Double.parseDouble(strValue.replace(".", "").replace(",", "."));
                            cell.setCellValue(num);
                            if (col > 0) { // colunas numéricas (exceto "Conta")
                                cell.setCellStyle(currencyStyle);
                            }
                        } catch (Exception e) {
                            cell.setCellValue(strValue);
                        }
                    } else {
                        cell.setCellValue(strValue);
                    }
                } else {
                    cell.setCellValue(""); // evita null
                }
            }
        }

        // === AJUSTAR LARGURA DAS COLUNAS ===
        sheet.setColumnWidth(0, 9000); // Conta
        for (int i = 1; i < table.getColumnCount(); i++) {
            sheet.setColumnWidth(i, 3800);
        }

        // === SALVAR ===
        try (FileOutputStream out = new FileOutputStream(filePath)) {
            workbook.write(out);
        }
        workbook.close();
    }
}
