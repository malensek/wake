package wake.plugins;
public class Symlink implements Plugin {

    /* No dependencies: */
    private List<WakeFile> dependencies = new ArrayList<>();

    public Symlink() {

    }

    @Override
    public String name() {
        return "Symlink";
    }

    @Override
    public boolean wants(WakeFile file) {
        return file.getExtension().equals("symlink");
    }

    @Override
    public List<WakeFile> requires(WakeFile file) {
        return this.dependencies;
    }
}
