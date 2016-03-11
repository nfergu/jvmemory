public class ClassStats {

    private final String className;
    private long instancecount = 0;
    private long sizeInBytes = 0;

    public ClassStats(String className) {
        this.className = className;
    }

    public void addInstance(long sizeInBytes) {
        this.instancecount++;
        this.sizeInBytes += sizeInBytes;
    }

    public long getInstanceCount() {
        return instancecount;
    }
    public long getSizeInBytes() {
        return sizeInBytes;
    }
    public String getClassName() {
        return className;
    }

}
