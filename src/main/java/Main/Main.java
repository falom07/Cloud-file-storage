package Main;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String path = "qwe/qwe/qwe/qwesdf/q/qwe/sdf/sfwer/txt.txt";
        String fileName = path.substring(path.lastIndexOf("/") + 1);
        System.out.println(fileName);


    }

    private static void run(String name, int index) {
        System.out.println("Running..." + name + name);
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
