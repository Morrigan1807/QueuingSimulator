package shop;

public class Customer {
    private static int iter = 0;
    public final int number;

    public Customer()
    {
        number = ++iter;
    }
}

