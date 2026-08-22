package utilitarios;

public class ChamaPrinc {

    private static String usuarioLogado;

    public static void login(String usuario) {
        usuarioLogado = usuario;
    }

    public static void fazerAlgumaCoisa() {
        // VocÃª pode acessar 'usuarioLogado' aqui
        System.out.println("Usuário logado: " + usuarioLogado);
    }

}
