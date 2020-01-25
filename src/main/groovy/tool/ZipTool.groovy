package tool

import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.zip.Zip64Mode
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.poi.ss.usermodel.Workbook

/**
 * Created by Peter.Yang on 2019/5/10.
 */
class ZipTool {
    static void zipDir(OutputStream opo,List<InputStream> ips) {
        /**
         * created by yang on 19:53 2019/5/23.
         * describtion: 将指定目录压缩为一个压缩包
         * @param :

         */

        ZipArchiveOutputStream zous = new ZipArchiveOutputStream(new FileOutputStream("1.zip"));        //web端时直接用response流
        zous.setUseZip64(Zip64Mode.AsNeeded);
        //遍历文件list
        ips.each {
            zous.addRawArchiveEntry(new ZipArchiveEntry(),it)
        }
        zous.close()
    }

    public static void main(String[] args) {
        zipDir()
    }

}
