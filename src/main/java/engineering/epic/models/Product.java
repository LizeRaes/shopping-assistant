package engineering.epic.models;

import java.util.Objects;

public class Product {
    private String name;
    private String description;
    private double price;
    private String discountPrice;
    private String buyOnCredit;

    public Product() {}

    public Product(String name, double price, String description) {
        this.name = name;
        this.price = price;
        this.description = description;
    }

    public Product(String name, double basePrice, String description, String discountPrice, String buyOnCredit) {
        this.name = name;
        this.price = basePrice;
        this.description = description;
        this.discountPrice = discountPrice;
        this.buyOnCredit = buyOnCredit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Double.compare(product.price, price) == 0 &&
                Objects.equals(name, product.name) &&
                Objects.equals(description, product.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, price, description);
    }

    @Override
    public String toString() {
        return "Product{" +
                "name='" + name + '\'' +
                ", price=" + price +
                ", description='" + description + '\'' +
                '}';
    }

    public Object getPricingScheme() {
        // TODO
        return "discount";
    }

    public Object getMaxImposableQuantity() {
        // TODO
        return 10;
    }
}