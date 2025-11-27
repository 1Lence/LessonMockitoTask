package shopping;

import customer.Customer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import product.Product;

import java.util.Map;
import java.util.Random;

/**
 * Тестирование функционала класса Cart
 */
class CartTest {
    private final Customer customer;
    private final Random random;
    private final Cart cart;

    public CartTest() {
        this.random = new Random();
        this.customer = new Customer(random.nextLong(), "123123123");
        this.cart = new Cart(customer);
    }

    /**
     * Получение списка продуктов должно возвращать неизменяемую коллекцию
     */
    @Test
    void getProductsShouldReturnUnmodifiableMap() {
        Product product = new Product("Какой-то продукт", 10);

        cart.add(product, 2);

        Map<Product, Integer> products = cart.getProducts();

        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            products.put(new Product("Новый продукт", 5), 1);
        });
    }

    /**
     * Ошибка.
     * Попытка добавить равное количество товаров (1 к 1) не должно выдавать ошибку
     */
    @Test
    void validateCountShouldNotThrowExceptionWithCorrectInput() {
        Product product = new Product("Равное количетво", 10);

         Assertions.assertDoesNotThrow(() -> cart.add(product, 10));
    }

    /**
     * Ошибка.
     *
     * <p>Нет проверки на минусовое значение</p>
     */
    @Test
    void cartAddShouldThrowExceptionWithNegativeCountOfProduct() {
        Product product = new Product("Минус", 10);

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> cart.add(product, -10)
        );

        Assertions.assertEquals(
                String.format("Невозможно добавить товар Минус в корзину, со значением %s", -10),
                exception.getMessage()
        );
    }

    /**
     * Ошибка?
     *
     * <p>Я думаю всё же стоит обработать случай добавления null в корзину</p>
     */
    @Test
    void cartAddShouldThrowExceptionWithNullProduct() {
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> cart.add(null, 0)
        );

        Assertions.assertEquals(
                "Невозможно добавить пустой продукт.",
                exception.getMessage()
        );
    }

    /**
     * Проверка работы метода удаления из корзины.
     */
    @Test
    void removeExistingProductShouldRemoveFromCart() {
        Product product = new Product("Минус", 10);
        cart.add(product, 2);

        cart.remove(product);

        Assertions.assertEquals(0, cart.getProducts().size());
    }

    /**
     * Проверка работы метода edit. Не должно позволять изменить на невалидное количество продуктов
     */
    @Test
    void edit() {
        Product product = new Product("Минус", 10);
        cart.add(product, 9);

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> cart.edit(product, 11)
        );

        Assertions.assertEquals(
                "Невозможно добавить товар 'Минус' в корзину, т.к. нет необходимого количества товаров",
                exception.getMessage()
        );
    }
}