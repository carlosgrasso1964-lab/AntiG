package classes;

public class ChamaPrinc {

    private static String usuarioLogado;

    public static void login(String usuario) {
        usuarioLogado = usuario;
    }

    public static void fazerAlgumaCoisa() {
        // VocÃª pode acessar 'usuarioLogado' aqui
        System.out.println("Usuário logado: " + usuarioLogado);
    }

    //public static void main(String[] args) {
    //    login("exemplo_usuário");
    //    fazerAlgumaCoisa();
    //}
}
