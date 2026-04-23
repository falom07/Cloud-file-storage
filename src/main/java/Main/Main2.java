package Main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main2 {
    public static void main(String[] args) {
       String fullPath = "Patern uml/qwe1/dfdf";
        fullPath = fullPath.substring(fullPath.lastIndexOf("/") + 1);

        System.out.println(fullPath);
    }
}
