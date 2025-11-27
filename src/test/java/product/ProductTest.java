package product;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Проверка работы логики добавления количества и уменьшения
 */
class ProductTest {
    private final Product product;

    public ProductTest() {
        this.product = new Product("Продукт", 10);
    }

    /**
     * Ошибка.
     *
     * <p>Проверка на вычитания количества товара на большее количество, чем есть в наличии.</p>
     *
     * <p>Считаю, что должна выбрасываться ошибка, а не списываться до 0</p>
     */
    @Test
    void subtractCountShouldThrowExceptionWithNegativeCount() {
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () ->product.subtractCount(-100)
        );

        Assertions.assertEquals(
                "Невозможно уменьшить количество Продукт меньше нуля",
                exception.getMessage()
        );
    }
}