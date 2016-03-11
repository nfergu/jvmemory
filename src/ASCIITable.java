import java.io.PrintWriter;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;

public class ASCIITable {

    private final ASCIITableHeader[] header;
    private final Iterable<String[]> rows;

    public ASCIITable(ASCIITableHeader[] header, Iterable<String[]> rows) {
        for (String[] row : rows) {
            if (row.length != header.length) {
                throw new IllegalArgumentException("Row length [" + row.length +
                        "] did not match header length [" + header.length + "]");
            }
        }
        this.header = header;
        this.rows = rows;
    }

    public void print(PrintWriter out) {
        int[] columnWidths = new int[header.length];
        String[] headerNames = Arrays.stream(header).map(ASCIITableHeader::getName).toArray(String[]::new);
        updateColumnWidths(columnWidths, headerNames);
        for (String[] row : rows) {
            updateColumnWidths(columnWidths, row);
        }
        printSeparator(out, columnWidths);
        Alignment[] headerAlignments = new Alignment[header.length];
        Arrays.fill(headerAlignments, Alignment.CENTER);
        printRow(out, headerNames, headerAlignments, columnWidths);
        printSeparator(out, columnWidths);
        printRows(out, rows, header, columnWidths);
        printSeparator(out, columnWidths);
    }

    private void printRows(PrintWriter out, Iterable<String[]> rows, ASCIITableHeader[] header, int[] columnWidths) {
        for (String[] row: rows) {
            printRow(out, row, Arrays.stream(header).map(ASCIITableHeader::getAlignment).toArray(Alignment[]::new), columnWidths);
        }
    }

    private void printRow(PrintWriter out, String[] row, Alignment[] alignments, int[] columnWidths) {
        int i = 0;
        for (String cell : row) {
            out.print("|");
            int columnWidth = getRealColumnWidth(columnWidths[i]);
            out.print(paddedValue(cell, columnWidth, alignments[i]));
            i++;
        }
        out.println("|");
    }

    private String paddedValue(String value, int width, Alignment alignment) {
        switch(alignment) {
            case LEFT: return " " + StringUtils.rightPad(value, width - 1, ' ');
            case RIGHT: return StringUtils.leftPad(value, width - 1, ' ') + " ";
            case CENTER: return StringUtils.center(value, width, ' ');
            default: throw new IllegalStateException("Unknown alignment value [" + alignment + "]");
        }
    }

    private void printSeparator(PrintWriter out, int[] columnWidths) {
        for (int columnWidth : columnWidths) {
            out.print("+");
            out.print(StringUtils.repeat("-", getRealColumnWidth(columnWidth)));
        }
        out.println("+");
    }

    private int getRealColumnWidth(int columnWidth) {
        return columnWidth + 2;
    }

    private void updateColumnWidths(int[] columnWidths, String[] row) {
        for (int i = 0; i < columnWidths.length; i++) {
            if (row[i].length() > columnWidths[i]) {
                columnWidths[i] = row[i].length();
            }
        }
    }

    public static class ASCIITableHeader {
        private final String name;
        private final Alignment alignment;
        public ASCIITableHeader(String name, Alignment alignment) {
            this.alignment = alignment;
            this.name = name;
        }
        public Alignment getAlignment() {
            return alignment;
        }
        public String getName() {
            return name;
        }
    }

    public enum Alignment {
        LEFT,
        RIGHT,
        CENTER
    }

}






















