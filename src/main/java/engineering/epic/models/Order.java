package engineering.epic.models;

import java.util.List;
import java.util.Objects;

public class Order {
    private String clientName;
    private String address;
    private List<CartItem> items;
    private double total;

    public Order() {}

    public Order(String clientName, String address, List<CartItem> items, double total) {
        this.clientName = clientName;
        this.address = address;
        this.items = items;
        this.total = total;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Double.compare(order.total, total) == 0 &&
                Objects.equals(clientName, order.clientName) &&
                Objects.equals(address, order.address) &&
                Objects.equals(items, order.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientName, address, items, total);
    }

    @Override
    public String toString() {
        return "Order{" +
                "clientName='" + clientName + '\'' +
                ", address='" + address + '\'' +
                ", items=" + items +
                ", total=" + total +
                '}';
    }
}