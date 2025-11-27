package shopping;

import customer.Customer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import product.Product;
import product.ProductDao;

import java.util.Map;
import java.util.Random;

/**
 * Тестирование сервиса покупок
 */
@ExtendWith(MockitoExtension.class)
class ShoppingServiceTest {
    private final ProductDao productDao;
    private final Random random;

    private final ShoppingService shoppingService;
    private Customer customer;

    public ShoppingServiceTest(@Mock ProductDao productDao) {
        this.productDao = productDao;
        shoppingService = new ShoppingServiceImpl(productDao);
        random = new Random();
    }

    /**
     * Подготовка тестовых данных перед каждым тестом
     */
    @BeforeEach
    void stepUp(){
        customer = new Customer(random.nextLong(100), "+79123456789");
    }

    /**
     * Проверка на то, что метод возвращает новую корзину.
     */
    @Test
    void getCartTest() {
        Assertions.assertEquals(shoppingService.getCart(customer), shoppingService.getCart(customer));
    }

    /**
     * Как будто нет смысла тестировать, он просто вызывает метод дао для поиска.
     *
     * <p>Но было бы неплохо сделать проверку на то, что name != null</p>
     */
    @Test
    void getProductByName() {

    }

    /**
     * Проверка на попытку совершить покупку, когда корзина пустая
     */
    @Test
    void buyWithEmptyCartShouldReturnFalse() throws BuyException {
        Cart emptyCart = shoppingService.getCart(customer);

        boolean result = shoppingService.buy(emptyCart);

        Assertions.assertFalse(result);
        Mockito.verify(productDao, Mockito.never()).save(Mockito.any(Product.class));
    }

    /**
     * Ошибка.
     *
     * <p>После успешной покупки не очищается корзина, обнуляется лишь {@code count}</p>
     *
     * Проверка успешной покупки. После покупки корзина должна очищаться.
     * Количество товаров должно уменьшаться и сохраняться с помощью DAO
     */
    @Test
    void buySuccessfulPurchaseShouldUpdateProductCounts() throws BuyException {
        Cart cart = shoppingService.getCart(customer);
        Product product1 = new Product("Product1", 10);
        Product product2 = new Product("Product2", 5);

        cart.add(product1, 3);
        cart.add(product2, 2);

        boolean result = shoppingService.buy(cart);

        Assertions.assertTrue(result);
        Mockito.verify(productDao, Mockito.times(1)).save(product1);
        Mockito.verify(productDao, Mockito.times(1)).save(product2);

        Map<Product, Integer> productMap = cart.getProducts();

        Assertions.assertEquals(0, productMap.size());
        Assertions.assertEquals(7, product1.getCount());
        Assertions.assertEquals(3, product2.getCount());
    }

    /**
     * Проверка успешной покупки.
     * Количество товаров должно уменьшаться и сохраняться с помощью DAO
     */
    @Test
    void buyWithInsufficientProductQuantityShouldThrowBuyException() {
        Cart cart = shoppingService.getCart(customer);
        Product product = new Product("Какой-то продукт", 3);
        cart.add(product, 2);

        product.subtractCount(2);

        BuyException exception = Assertions.assertThrows(BuyException.class, () -> {
            shoppingService.buy(cart);
        });

        Assertions.assertEquals(
                exception.getMessage(),
                "В наличии нет необходимого количества товара 'Какой-то продукт'"
        );
        Mockito.verify(productDao, Mockito.never()).save(Mockito.any(Product.class));
    }

    /**
     * Ошибка.
     * Проверка на возможность купить товары с несуществующей тележки.
     *
     * <p>Не хватает проверки на null</p>
     */
    @Test
    void nullCartTest() {
        BuyException exception = Assertions.assertThrows(BuyException.class, () -> {
            shoppingService.buy(null);
        });

        Assertions.assertEquals(
                exception.getMessage(),
                "В наличии нет необходимого количества товара 'null'"
        );
    }

    /**
     * Проверка на возможность купить товар другому покупателю, если до него кто-то уже купил этот товар
     *
     */
    @Test
    void twoCustomersBuyShouldThrowBuyException() throws BuyException {
        Cart firstCustomerCart = shoppingService.getCart(customer);

        Customer secondCustomer = new Customer(random.nextLong(), "123");
        Cart secondCustomerCart = shoppingService.getCart(secondCustomer);

        Product product = new Product("Какой-то продукт", 10);

        firstCustomerCart.add(product, 9);
        secondCustomerCart.add(product, 9);

        shoppingService.buy(firstCustomerCart);

        BuyException exception = Assertions.assertThrows(BuyException.class, () -> {
            shoppingService.buy(secondCustomerCart);
        });

        Assertions.assertEquals(
                exception.getMessage(),
                "В наличии нет необходимого количества товара 'Какой-то продукт'"
        );
    }

    /**
     * Ошибка.
     * Проверка на покупку отрицательного количества товара.
     *
     * <p>Вообще ответственность на складывание отрицательного количества товаров лежит на классе {@code Cart},
     * но как-будто и в сервисе можно было сделать дополнительную проверку</p>
     */
    @Test
    public void testBuyWithNegativeCountInCart() {
        Cart cart = shoppingService.getCart(customer);

        Product product1 = new Product("Товар", 10);
        cart.add(product1, -10);

        BuyException exception = Assertions.assertThrows(BuyException.class, () -> {
            shoppingService.buy(cart);
        });

        Assertions.assertEquals(
                exception.getMessage(),
                "В наличии нет необходимого количества товара 'Товар'"
        );
    }

    /**
     * Ошибка.
     * Проверка на покупку нулевого количества товара.
     *
     * <p>Вообще ответственность на складывание нулевого количества товаров лежит на классе {@code Cart},
     * но как-будто и в сервисе можно было сделать дополнительную проверку</p>
     */
    @Test
    void buyProductWithZeroCountShouldThrowBuyException() {
        Cart cart = shoppingService.getCart(customer);
        Product zeroStockProduct = new Product("Ноль", 1);
        cart.add(zeroStockProduct, 0);

        BuyException exception = Assertions.assertThrows(BuyException.class, () -> {
            shoppingService.buy(cart);
        });

        Assertions.assertEquals(
                exception.getMessage(),
                "В наличии нет необходимого количества товара 'Ноль'"
        );
    }
}