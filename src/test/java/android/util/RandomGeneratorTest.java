package android.util;

import org.junit.Test;

public class RandomGeneratorTest {
    @Test public void randTest() {
        Integer result;
        result = RandomGenerator.rand(0, 2);
        assert result <= 0;
    }
}
