package recognizer;

public class Main {

    public static void main(String[] args) {
        float[] n2 = {1.5f, 3.9f, 4.1f, 3.3f};                                  // sample2
        float[] n1 = {2.1f, 2.45f, 3.673f, 4.32f, 2.05f, 1.93f, 5.67f, 6.01f};  //sample1
        DTW dt = new DTW(n1, n2);
        System.out.println(dt);
    }
}