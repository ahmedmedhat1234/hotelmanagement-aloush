package payment;

public interface Payment {
    boolean processPayment(double amount);
    boolean processRefund(double amount);
    PaymentStatus getPaymentStatus();
}