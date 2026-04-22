package Main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main2 {
    public static void main(String[] args) {
       String fullPathTo = "Patern uml/qwe1/";
        String path = fullPathTo.substring(0,fullPathTo.length() - 1);


        path = path.substring(path.lastIndexOf("/") + 1);
        System.out.println(path);
        System.out.println(path);
    }
}
