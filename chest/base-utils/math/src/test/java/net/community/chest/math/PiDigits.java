/*
 * 
 */
package net.community.chest.math;

import java.io.BufferedReader;
import java.io.PrintStream;

import net.community.chest.io.ApplicationIOUtils;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jul 10, 2013 12:33:35 PM
 */
public class PiDigits extends ApplicationIOUtils {
    /**
     * Computes the nth digit of Pi in base-16. If n < 0, return -1.
     * 
     * @param nth The digit of Pi to retrieve in base-16.
     * @return The n-th digit of Pi in base-16.
     */
    public static final int piDigit(int nth) {
        // see http://stackoverflow.com/questions/12449430/how-did-the-following-piece-of-java-code-calculate-the-digits-of-pi
        if (nth < 0)
            return -1;

        // Bailey-Borwein-Plouffe algorithm to calculate a n-th digit of pi without knowing the (n-1)th digit.
        int n = nth-1;
        double x = 4 * piTerm(1, n) - 2 * piTerm(4, n) - piTerm(5, n) - piTerm(6, n);
        x = x - Math.floor(x);

        return (int) (x * 16);
    }

    private static final double piTerm(int j, int n) {
        // Calculate the left sum
        double s = 0;
        for (int k = 0; k <= n; ++k) {
            int r = 8 * k + j;
            s += powerMod(16, n - k, r) / (double) r;
            s = s - Math.floor(s);
        }

        // Calculate the right sum
        double t = 0;
        int k = n + 1;
        // Keep iterating until t converges (stops changing)
        while (true) {
            int r = 8 * k + j;
            double newt = t + Math.pow(16, n - k) / r;
            if (t == newt) {
                break;
            } else {
                t = newt;
            }
            ++k;
        }

        return s + t;
    }

    // see http://faculty.washington.edu/moishe/tcss342/PowerMod.java
    private static final long powerMod(long a, long b, long m) {
        if (b == 0L) {
            return 1L;
        }

        if (b == 1L) {
            return a;
        }

        long temp = powerMod(a, (b >> 1) & 0x7FFFFFFFL, m);
        if ((b & 0x01L) == 0L) {
            return (temp * temp) % m;
        } else {
            return ((temp * temp) % m) * a % m;
        }
    }
    
    //////////////////////////////////////////////////////////////////////////
    
    protected static final void nthPiDigit(PrintStream out, int nth) {
        long    startTime=System.currentTimeMillis();
        int     digit=piDigit(nth);
        long    endTime=System.currentTimeMillis();
        out.append("\tPI[").append(String.valueOf(nth)).append(']')
           .append(": ").append(String.valueOf(digit))
           .append(" - msec. ").println(endTime - startTime);
    }

    protected static final void nthPiDigit(BufferedReader in, PrintStream out) {
        for ( ; ; ) {
            String  ans=getval(out, in, "n-th value (or Quit)");
            if (ans.length() <= 0) {
                continue;
            }
            
            if (isQuit(ans)) {
                break;
            }
            
            try {
                nthPiDigit(out, Integer.parseInt(ans));
            } catch(NumberFormatException e) {
                // ignored
            }
        }
    }

    public static void main(String[] args) {
//      nthPiDigit(getStdin(), System.out);
        
        for (int nth : new int[] { 1, 10, 100, 10000, 100000, 1000000, 10000000}) {
            nthPiDigit(System.out, nth);
        }
    }
}
