import java.util.Arrays;

public class FFTUtil {
  public static int MAX_I = 100; // この値は適当

  long[] wImage;
  long[] wReal;
  long[] powerSpectol;
  long[] acf;
  long[] tmpReal;
  long[] tmpImage;
  int[] indices;
  int fftSize;
  int halfSize;

  public FFTUtil(int length){
    System.out.println(length);
    length = getSmallest2Power(length);
    fftSize = length;
    halfSize = length / 2;
    wReal = new long[halfSize];
    wImage = new long[halfSize];
    System.out.println(length);
    indices = new int[length];
    for (int i = 0; i < length; ++i) {
      indices[i] = i;
    }
    makeIndices(indices, 0, length);

    powerSpectol = new long[length];
    acf = new long[length];
    tmpReal = new long[length];
    tmpImage = new long[length];
    double factor = -2.0 * Math.PI / ((float)length);
    double base = (double)(1 << 16); // 2 ^ 16
    for (int i=0; i < halfSize; i++) {
      double angle = factor * (double)i;
      double realPart = Math.cos(angle);
      double imagePart = Math.sin(angle);
      wReal[i] = (long)(Math.round(realPart * base));
      wImage[i] = (long)(Math.round(imagePart * base));
    }
  }

  // 初期インデックス作成
  // 偶数と奇数に分ける
  public void makeIndices(int[] indices, int offset, int length) {
    System.out.println(offset + "\t" + length);
    if (length - offset < 2) return;
    else System.out.println("no return");
    int[] origin = Arrays.copyOfRange(indices, offset, length);
    int halfsize = (length - offset) / 2;
    for (int i=0; i < halfsize; ++i) {
      indices[offset + i] = origin[i * 2];
      indices[offset + i + halfsize] = origin[i * 2 + 1];
    }
    // 分割統治法で再起する
    makeIndices(indices, offset, offset + halfsize);
    makeIndices(indices, offset + halfsize, length);
  }

  // N で割らないので、正しくはFFTではない。 IFFT。ただし、定数なので気にしない。
  public void fft(short[] input) {
    for (int i=0; i < fftSize; ++i) {
      tmpReal[i] = 0;
      tmpImage[i] = 0;
      int baseReal = 1;
      int baseImage = 1;
      for (int j=0; j < halfSize; ++j) {
        baseReal *= wReal[i];
        baseImage *= wImage[i];
        tmpReal[i] += (wReal[i] - wReal[halfSize - i])* input[i];
      }
    }
  }

  public void powerSpectol(short[] input) {
    fft(input);
  }

  public int getSmallest2Power(int length) {
    int i = 1;
    while(i < MAX_I) {
      int power = (int)Math.pow(2.0, (double)i);
      if (length <= power) {
        return power;
      }
      ++i;
    }
    return -1;
  }

  // debug
  public void dump() {
    for (int i=0; i<fftSize; ++i) {
      System.out.println("i:" + indices[i]);
    }
  }

  // debug
  public static void main(String args[]) {
    FFTUtil fft = new FFTUtil(Integer.parseInt(args[0]));
    fft.dump();
  }
}
