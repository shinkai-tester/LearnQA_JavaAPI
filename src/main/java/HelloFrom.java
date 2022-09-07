import java.util.Scanner;

public class HelloFrom {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String name = in.nextLine();
        System.out.printf("Hello from %s%n", name);
    }
}
