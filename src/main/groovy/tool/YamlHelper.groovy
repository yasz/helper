package tool
import org.yaml.snakeyaml.Yaml

class YamlHelper {

    static HashMap file2hash(String filePath) {
        return new Yaml().loadAs(new FileInputStream(filePath),HashMap.class)

    }
}
