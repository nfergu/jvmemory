import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;
import edu.tufts.eaftan.hprofparser.handler.NullRecordHandler;
import edu.tufts.eaftan.hprofparser.parser.datastructures.Value;

public class JVMemoryHandler extends NullRecordHandler {

    private Map<Long, String> strings = new HashMap<>();

    private Map<Long, String> classNames = new HashMap<>();

    private Map<String, ClassStats> histogram = new HashMap<>();

    @Override
    public void stringInUTF8(long id, String data) {
        strings.put(id, data);
    }

    @Override
    public void loadClass(int classSerialNum, long classObjId, int stackTraceSerialNum, long classNameStringId) {
        classNames.put(classObjId, strings.get(classNameStringId));
    }

    @Override
    public void instanceDump(long objId, int stackTraceSerialNum, long classObjId, Value<?>[] instanceFieldValues) {
        String className = classNames.get(classObjId);
        long totalSize = 0;
        for (Value<?> value : instanceFieldValues) {
            totalSize += value.type.sizeInBytes();
        }
        histogram.computeIfAbsent(className, k -> new ClassStats(k)).addInstance(totalSize);
    }

    @Override
    public void objArrayDump(long objId, int stackTraceSerialNum, long elemClassObjId, long[] elems) {
        DO THIS
    }

    @Override
    public void primArrayDump(long objId, int stackTraceSerialNum, byte elemType, Value<?>[] elems) {
        DO THIS
    }

    public Map<String, ClassStats> getHistogram() {
        return histogram;
    }

}
