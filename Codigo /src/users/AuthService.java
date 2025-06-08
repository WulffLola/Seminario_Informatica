package users;

public class AuthService {

    public static boolean login(String username, String password) {
        // Datos ficticios por ahora
        String validUser = "admin";
        String validPass = "1234";

        return username.equals(validUser) && password.equals(validPass);
    }
}
