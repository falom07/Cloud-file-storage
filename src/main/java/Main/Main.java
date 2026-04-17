package Main;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String path = "qwe/qwe/qwe/qwesdf/q/qwe/sdf/sfwer/txt.txt";
        String path2 = "qwe/qwe/qwe/qwesdf/q/qwe/sdf/sfwer/txt2.txt";

        String path3 = "qwe/qwe/qwe/qwesdf/q/qwe/sdf/sfwer";
        String path4 = "qwe/qwe/qwe/qwesdf/q/qwe/sdf/sfwer2";

        String path5 = "qwe/qwe/qwe/qwesdf/q/qwe/sdf/sfwer";
        String path6 = "qwe/qwe/qwe/qwesdf/q/sfwer";

        String path7 = "qwe/qwe/qwe/qwesdf/q/qwe/sdf/sfwer/txt.txt";
        String path8 = "qwe/qwe/qwe/qwesdf/q/qwe/sdf/txt.txt";

        String fileName = path7.substring(path7.lastIndexOf("/") + 1);
        String fileName2 = path8.substring(path8.lastIndexOf("/") + 1);

        System.out.println(fileName + "\n" + fileName2);
        String pathFileFrom2 = path.substring(0, path.lastIndexOf("/") + 1);
        System.out.println(path.substring(path.length() - 1 ,path.length()).equals("/"));
        System.out.println(pathFileFrom2);



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
