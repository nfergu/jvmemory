import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.google.common.io.CountingInputStream;
import edu.tufts.eaftan.hprofparser.parser.HprofParser;
import hu.ssh.progressbar.ProgressBar;
import hu.ssh.progressbar.console.ConsoleProgressBar;

public class JVMemory {

    public static void main(String[] args) throws InterruptedException, IOException {

        String inputFile = args[0];
        long fileSize = new File(inputFile).length();

        JVMemoryHandler handler = new JVMemoryHandler();
        HprofParser parser = new HprofParser(handler);

        // Phase 1 (class and string data)
        CountingInputStream inputStreamPhase1 = getInputStream(inputFile);
        ProgressRunnable progressRunnablePhase1 = getProgressRunnable("Loading class data", 1, 2, inputStreamPhase1, fileSize);
        Thread threadPhase1 = startProgressThread(progressRunnablePhase1);
        parser.parseFirstPass(new DataInputStream(inputStreamPhase1));
        cleanUp(inputStreamPhase1, progressRunnablePhase1, threadPhase1);

        // Phase 2 (instance data)
        CountingInputStream inputStreamPhase2 = getInputStream(inputFile);
        ProgressRunnable progressRunnablePhase2 = getProgressRunnable("Loading instance data", 2, 2, inputStreamPhase2, fileSize);
        Thread threadPhase2 = startProgressThread(progressRunnablePhase2);
        parser.parseSecondPass(new DataInputStream(inputStreamPhase2));
        cleanUp(inputStreamPhase2, progressRunnablePhase2, threadPhase2);

        System.out.flush();

        // Sort in descending order, based on the number of bytes
        Iterator<ClassStats> sorted =
                handler.getHistogram().values().stream().sorted((a, b) -> Long.compare(b.getSizeInBytes(), a.getSizeInBytes())).iterator();

        List<String[]> rows = getRows(sorted);
        ASCIITable.ASCIITableHeader[] header = getHeader();

        ASCIITable asciiTable = new ASCIITable(header, rows);
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(System.out, "UTF-8"));
        asciiTable.print(writer);
        writer.flush();

    }

    private static Thread startProgressThread(ProgressRunnable progressRunnablePhase1) {
        Thread thread = new Thread(progressRunnablePhase1);
        thread.start();
        return thread;
    }

    private static ASCIITable.ASCIITableHeader[] getHeader() {
        return new ASCIITable.ASCIITableHeader[] {
                new ASCIITable.ASCIITableHeader("num", ASCIITable.Alignment.RIGHT),
                new ASCIITable.ASCIITableHeader("#instances", ASCIITable.Alignment.RIGHT),
                new ASCIITable.ASCIITableHeader("#bytes", ASCIITable.Alignment.RIGHT),
                new ASCIITable.ASCIITableHeader("class name", ASCIITable.Alignment.LEFT),
            };
    }

    private static List<String[]> getRows(Iterator<ClassStats> sorted) {
        List<String[]> rows = new ArrayList<>(); int i = 0;
        while (sorted.hasNext()) {
            ClassStats classStats = sorted.next();
            String[] row = new String[4];
            row[0] = ++i + ":";
            row[1] = String.valueOf(classStats.getInstanceCount());
            row[2] = String.valueOf(classStats.getSizeInBytes());
            row[3] = classStats.getClassName();
            rows.add(row);
        }
        return rows;
    }

    private static void cleanUp(CountingInputStream inputStream, ProgressRunnable progressRunnable, Thread thread)
            throws IOException, InterruptedException {
        inputStream.close();
        progressRunnable.setDone();
        thread.join();
    }

    private static ProgressRunnable getProgressRunnable(String phaseDescription, int phaseNumber, int totalPhases, CountingInputStream inputStream,
            long fileSize) {
        return new ProgressRunnable(phaseDescription, phaseNumber, totalPhases, inputStream, fileSize);
    }

    private static CountingInputStream getInputStream(String arg) throws FileNotFoundException {
        FileInputStream fs = new FileInputStream(arg);
        return new CountingInputStream(new BufferedInputStream(fs));
    }

    private static final class ProgressRunnable implements Runnable {

        private static final int TOTAL_STEPS = 1000;

        private final String phaseDescription;
        private final int phaseNumber;
        private final int totalPhases;
        private final CountingInputStream inputStream;
        private final long fileSize;

        private int currentStep = 0;

        private volatile boolean done = false;

        private ProgressRunnable(String phaseDescription, int phaseNumber, int totalPhases, CountingInputStream inputStream, long fileSize) {
            this.phaseDescription = phaseDescription;
            this.phaseNumber = phaseNumber;
            this.totalPhases = totalPhases;
            this.inputStream = inputStream;
            this.fileSize = fileSize;
        }

        @Override
        public void run() {
            System.out.println(phaseDescription + " (phase " + phaseNumber + "/" + totalPhases + ")");
            ProgressBar progressBar = ConsoleProgressBar.on(System.out).withFormat("[:bar] :percent% ETA: :eta").withTotalSteps(TOTAL_STEPS);
            while (!done && !Thread.currentThread().isInterrupted()) {
                float proportionDone = (float) inputStream.getCount() / (float) fileSize;
                int expectedStep = (int) Math.floor((float) TOTAL_STEPS * proportionDone);
                int ticks = expectedStep - currentStep;
                if (ticks > 0) {
                    progressBar.tick(ticks);
                    currentStep += ticks;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            if (done) {
                progressBar.complete();
            }
        }

        public void setDone() {
            this.done = true;
        }

    }

}
































