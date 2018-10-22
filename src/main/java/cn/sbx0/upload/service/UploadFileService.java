package cn.sbx0.upload.service;

import cn.sbx0.upload.dao.UploadFileDao;

import cn.sbx0.upload.entity.UploadFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class UploadFileService {
    @Resource
    private UploadFileDao uploadFileDao;
    @Value("${file.uploadFolder}")
    public String path; // 上传路径

    public boolean save(UploadFile uploadFile) {
        try {
            uploadFileDao.save(uploadFile);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 判断文件是否存在
     *
     * @param md5
     * @return
     */
    public UploadFile md5Check(String md5) {
        return uploadFileDao.existsByMd5(md5);
    }

    /**
     * 创建 路径
     *
     * @param fileName
     * @return
     */
    public String createPath(String fileName) {
        String ext = getFileExt(fileName);
        String type = checkType(ext) + "/";
        File dirFile = new File(path + type);
        if (!dirFile.exists()) { // 目录不存在，创建目录
            dirFile.mkdirs();
        }
        return type + fileName;
    }

    /**
     * 获取文件扩展名
     *
     * @return string
     */
    public String getFileExt(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }

    /**
     * 创建唯一文件名
     *
     * @return 文件名
     */
    public String createFileName(String originalFilename) {
        String ext = getFileExt(originalFilename);
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateString = simpleDateFormat.format(date);
        int randomNum = (int) (Math.random() * 899 + 100);
        String fileName = dateString + randomNum + ext;
        return fileName;
    }

    /**
     * 获取指定文件夹下的文件
     *
     * @param path
     * @return
     */
    public String getFile(String path) {
        String list = "";
        File file = new File(path);
        File[] array = file.listFiles();
        for (int i = 0; i < array.length; i++) {
            if (array[i].isFile()) {
                list += array[i].getPath() + "<br>";
            } else if (array[i].isDirectory()) {
                list += getFile(array[i].getPath());
            }
        }
        return list;
    }

    /**
     * 判断类型
     *
     * @param ext
     * @return
     */
    public String checkType(String ext) {
        switch (ext) {
            case ".jpg":
            case ".png":
            case ".gif":
            case ".bmp":
            case ".tif":
            case ".pcx":
            case ".tga":
            case ".exif":
            case ".fpx":
            case ".svg":
            case ".psd":
            case ".cdr":
            case ".pcd":
            case ".dxf":
            case ".ufo":
            case ".eps":
            case ".ai":
            case ".raw":
            case ".WMF":
            case ".webp":
                return "image";
            case ".zip":
            case ".7z":
            case ".XZ":
            case ".BZIP2":
            case ".GZIP":
            case ".TAR":
            case ".WIM":
                return "zip";
            case ".doc":
            case ".pdf":
            case ".txt":
            case ".md":
                return "doc";
            case ".mp4":
            case ".flv":
            case ".avi":
            case ".wmv":
            case ".asf":
            case ".wmvhd":
            case ".dat":
            case ".vob":
            case ".mpg":
            case ".mpeg":
            case ".3gp":
            case ".3g2":
            case ".mkv":
            case ".rm":
            case ".rmvb":
            case ".mov":
            case ".qt":
            case ".ogg":
            case ".ogv":
            case ".oga":
            case ".mod":
                return "video";
            default:
                return "other";
        }
    }
}
