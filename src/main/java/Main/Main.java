package Main;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class Main {
    public static void main(String[] args) {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "1234";
        String encodedPassword = encoder.encode(rawPassword);

        System.out.println("Хеш пароля: " + encodedPassword);

//
//        String g = "dsfs";
//        run("CloudFileStorage",2);
//        run(g,2);



    }

    private static void run(String name,int index){
        System.out.println("Running..." +  name + name );
    }

//    1.модіфікатор доступа - public default protected private
//    2.static
//    3.возращаємі значення
//    4.назва - com
//    5.параметри -
//   ----
//    6.рекурсія
//    7.фіналочка

}
