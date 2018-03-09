import java.util.Random;

public class PRNG {
    public static float randomFloat(float i) {
	Random rand = new Random();
	return rand.nextFloat() * (i - 0) + 0;
    }
}
