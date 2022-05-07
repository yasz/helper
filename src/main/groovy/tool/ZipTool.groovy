package tool

import common.Const
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.AesKeyStrength
import net.lingala.zip4j.model.enums.EncryptionMethod
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import static groovy.io.FileType.FILES

/**
 * Created by Peter.Yang on 2019/5/10.
 */
class ZipTool {

     static void main(String[] args) {
         ZipParameters zipParameters = new ZipParameters();
         zipParameters.setEncryptFiles(true);
         zipParameters.setEncryptionMethod(EncryptionMethod.AES);
// Below line is optional. AES 256 is used by default. You can override it to use AES 128. AES 192 is supported only for extracting.
         zipParameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);
         def fs = []
         new File('C:\\2.dev\\1.java\\ps_bg\\out').traverse(type: groovy.io.FileType.FILES, nameFilter: ~/.*pdf$/) {
             fs += it
             println(it.name)
         }

         ZipFile zipFile = new ZipFile("${Const.sem}.zip", "ruianVA123".toCharArray());
         zipFile.addFiles(fs, zipParameters)
    }

}
