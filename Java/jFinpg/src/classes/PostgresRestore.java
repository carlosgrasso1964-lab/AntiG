
package classes;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class PostgresRestore {
    public PostgresRestore() {
        JFileChooser open = new JFileChooser(new File("C:/BKP"));
        int op = open.showOpenDialog(null);
        if (op == JFileChooser.APPROVE_OPTION) {
            File arq = open.getSelectedFile();
            String nomeDoArquivo = open.getName(arq); // aqui pega somente o nome do arquivo
            
            // Defina um método para determinar se está na nuvem ou localmente
            boolean isCloud = checkIfRunningInCloud();

            // Use um if para definir a senha com base no ambiente
            String pw;
            String url;
            String user;
            String host = "";
            String port = "5432";
            String dbName = "jfin";
            
            if (isCloud) {
                // Nuvem
                user = System.getenv("PG_USER");
                pw = System.getenv("PG_PASSWORD");
                url = System.getenv("PG_URL");
            } else {
                // Local
                user = System.getenv("PGLUSER");
                pw = System.getenv("PGLSENHA");
                url = System.getenv("PGLURL");
            }
            
            // Extraindo host do URL da conexão
            if (url != null && url.startsWith("jdbc:postgresql://")) {
                url = url.substring(18);
                int colonIndex = url.indexOf(':');
                int slashIndex = url.indexOf('/');
                if (colonIndex != -1 && slashIndex != -1) {
                    host = url.substring(0, colonIndex);
                    port = url.substring(colonIndex + 1, slashIndex);
                    dbName = url.substring(slashIndex + 1);
                }
            }
            
            // aqui você testa se a string recebeu o caminho do arquivo   
            final List<String> comandos = new ArrayList<>();
            comandos.add("C:\\Program Files\\PostgreSQL\\17\\bin\\pg_restore.exe");
            comandos.add("-h");
            comandos.add(host);
            comandos.add("-p");
            comandos.add(port);
            comandos.add("-U");
            comandos.add(user);
            comandos.add("-c");
            comandos.add("-d");
            comandos.add(dbName);
            comandos.add("-v");
            comandos.add(arq.getAbsolutePath());
            
            ProcessBuilder pb = new ProcessBuilder(comandos);
            pb.environment().put("PGPASSWORD", pw);
            
            try {
                final Process process = pb.start();
                final BufferedReader r = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String line = r.readLine();
                while (line != null) {
                    System.err.println(line);
                    line = r.readLine();
                }
                r.close();

                process.waitFor();
                process.destroy();
                JOptionPane.showMessageDialog(null, "Restauração realizada com sucesso!");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(null, "Operação cancelada.");
        }
    }

    // Função para determinar se está rodando na nuvem
    private boolean checkIfRunningInCloud() {
        // Lógica para determinar se está na nuvem, pode ser baseado no hostname, endereço IP, variáveis de ambiente específicas, etc.
        String env = System.getenv("PG_PASSWORD");
        return env != null && env.equals("CLOUD");
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                PostgresRestore execRestore = new PostgresRestore();
            }
        });
    }
}
