package Main;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Main3 {
    public static void main(String[] args) {
        List<Integer> list = List.of(10, 12, 3, 2);

        long list2 = list.stream().distinct().skip(2).reduce(2, (a, b) -> a + b);
        System.out.println(list2);


    }



}
