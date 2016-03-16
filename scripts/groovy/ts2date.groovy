import java.io.BufferedReader
import java.io.PrintStream
import java.io.InputStreamReader
import java.util.Date

ts2Date(new BufferedReader(new InputStreamReader(System.in)), System.out);

private static void ts2Date (BufferedReader inp, PrintStream out) {
    while (true) {
        out.print("Timestamp (or Quit): ")
        String ans=inp.readLine()

        if ((ans == null) || (ans.length() <= 0)) {
            continue
        }

        if ("q".equalsIgnoreCase(ans) || "quit".equalsIgnoreCase(ans)) {
           break
        }

        try {
            final Date    dateVal=new Date(Long.parseLong(ans))
            out.append('\t').println(dateVal)
        } catch(NumberFormatException e) {
            System.err.println(e.getClass().getSimpleName() + ": " + e.getMessage())
        }
    }
}
