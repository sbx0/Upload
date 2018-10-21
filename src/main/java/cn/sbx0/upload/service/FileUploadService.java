package cn.sbx0.upload.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

@Service
public class FileUploadService {
    // 文件允许格式
    private String[] allowFiles =
            {".jpg", ".png", ".gif",
                    ".zip",
                    ".doc", ".pdf", ".txt", ".md",
                    ".mp4",
            };
    @Value("${file.uploadFolder}")
    public String path;

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
                return "image/";
            case ".zip":
                return "zip/";
            case ".doc":
            case ".pdf":
            case ".txt":
            case ".md":
                return "doc/";
            case ".mp4":
                return "video/";
            default:
                return "";
        }
    }

    /**
     * 创建 路径
     *
     * @param originalFilename
     * @return
     */
    public String createPath(String originalFilename) {
        String ext = getFileExt(originalFilename);
        String type = checkType(ext);
        return type + createFileName() + ext;
    }

    /**
     * 文件类型判断
     *
     * @param fileName
     * @return
     */
    public boolean checkFileType(String fileName) {
        String fileType = getFileExt(fileName);
        Iterator<String> type = Arrays.asList(this.allowFiles).iterator();
        while (type.hasNext()) {
            String ext = type.next();
            if (fileType.toLowerCase().endsWith(ext)) {
                return true;
            }
        }
        return false;
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
    public String createFileName() {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateString = simpleDateFormat.format(date);
        int randomNum = (int) (Math.random() * 899 + 100);
        String fileName = dateString + randomNum;
        return fileName;
    }
}
