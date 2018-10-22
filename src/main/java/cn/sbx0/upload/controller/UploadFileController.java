package cn.sbx0.upload.controller;

import java.io.*;
import java.util.Date;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import cn.sbx0.upload.entity.UploadFile;
import cn.sbx0.upload.entity.User;
import cn.sbx0.upload.service.UploadFileService;
import cn.sbx0.upload.service.LogService;
import cn.sbx0.upload.service.UserService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传的Controller
 *
 * @author 单红宇(CSDN CATOOP)
 * @create 2017年3月11日
 */
@Controller
public class UploadFileController extends BaseController {
    @Resource
    private UploadFileService fileUploadService;
    @Resource
    private UserService userService;
    @Resource
    private LogService logService;

    /**
     * 1. 匹配md5检查文件存在与否
     *
     * @param md5File
     * @param request
     * @return
     */
    @PostMapping("/md5Check")
    @ResponseBody
    public ObjectNode md5Check(@RequestParam(value = "md5File") String md5File,
                               HttpServletRequest request) {
        objectNode = mapper.createObjectNode();
        // 从cookie中获取登陆用户信息
        User user = userService.getCookieUser(request);
        // 日志记录
        logService.log(user, request);
        // 未登录 直接否
        if (user == null) {
            objectNode.put("status", 2);
            objectNode.put("msg", "未登录");
            return objectNode;
        }
        UploadFile uploadFile = fileUploadService.md5Check(md5File);
        if (uploadFile != null) {
            objectNode.put("status", 1);
            objectNode.put("oName", uploadFile.getOriginalName());
            objectNode.put("name", uploadFile.getName());
            objectNode.put("type", uploadFile.getType());
        } else {
            objectNode.put("status", 0);
        }
        return objectNode;
    }

    /**
     * 2. 检查切片是否存在
     *
     * @param md5File
     * @param chunk
     * @param request
     * @return
     */
    @PostMapping("/chunkCheck")
    @ResponseBody
    public Boolean chunkCheck(@RequestParam(value = "md5File") String md5File,
                              @RequestParam(value = "chunk") Integer chunk,
                              HttpServletRequest request) {
        // 从cookie中获取登陆用户信息
        User user = userService.getCookieUser(request);
        // 未登录 直接否
        if (user == null) return false;
        Boolean exist = false;
        String path = fileUploadService.path + "/temp/" + md5File + "/";//分片存放目录
        String chunkName = chunk + ".tmp";//分片名
        File file = new File(path + chunkName);
        if (file.exists()) {
            exist = true;
        }
        return exist;
    }

    /**
     * 3. 上传文件
     *
     * @param file
     * @param md5File
     * @param chunk
     * @param request
     * @return
     */
    @PostMapping("/upload")
    @ResponseBody
    public Boolean upload(@RequestParam(value = "file") MultipartFile file,
                          @RequestParam(value = "md5File") String md5File,
                          @RequestParam(value = "chunk", required = false) Integer chunk,
                          HttpServletRequest request) {
        // 从cookie中获取登陆用户信息
        User user = userService.getCookieUser(request);
        // 未登录 直接否
        if (user == null) return false;

        String path = fileUploadService.path + "/temp/" + md5File + "/";
        File dirFile = new File(path);
        if (!dirFile.exists()) { // 目录不存在，创建目录
            dirFile.mkdirs();
        }

        String chunkName;
        if (chunk == null) { // 表示是小文件，还没有一片
            chunkName = "0.tmp";
        } else {
            chunkName = chunk + ".tmp";
        }

        String filePath = path + chunkName;
        File saveFile = new File(filePath);

        try {
            if (!saveFile.exists()) {
                saveFile.createNewFile(); // 文件不存在，则创建
            }
            file.transferTo(saveFile); // 将文件保存
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    /**
     * 4. 合成切片
     *
     * @param chunks
     * @param md5File
     * @param name
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("/merge")
    @ResponseBody
    public ObjectNode merge(@RequestParam(value = "chunks", required = false) Integer chunks,
                            @RequestParam(value = "md5File") String md5File,
                            @RequestParam(value = "name") String name,
                            HttpServletRequest request) throws Exception {
        ObjectNode objectNode = mapper.createObjectNode();
        // 从cookie中获取登陆用户信息
        User user = userService.getCookieUser(request);
        // 日志记录
        logService.log(user, request);
        // 未登录
        if (user == null) {
            objectNode.put("status", 1);
            return objectNode;
        }

        String path = fileUploadService.path;
        String fileName = fileUploadService.createFileName(name);
        String filePath = fileUploadService.createPath(fileName);
        objectNode.put("url", filePath);
        // 合成后的文件
        FileOutputStream fileOutputStream = new FileOutputStream(fileUploadService.path + filePath);

        try {
            byte[] buf = new byte[1024];
            for (long i = 0; i < chunks; i++) {
                String chunkFile = i + ".tmp";
                File file = new File(path + "/temp/" + md5File + "/" + chunkFile);
                InputStream inputStream = new FileInputStream(file);
                int len;
                while ((len = inputStream.read(buf)) != -1) {
                    fileOutputStream.write(buf, 0, len);
                }
                inputStream.close();
            }
            // 删除临时文件
            File file = new File(path + "/temp/" + md5File);
            if (file.isDirectory()) {
                File[] childFiles = file.listFiles();
                // 文件夹没有内容,删除文件夹
                if (childFiles == null || childFiles.length == 0) {
                    file.delete();
                }
                // 删除文件夹内容
                boolean reslut = true;
                for (File item : file.listFiles()) {
                    reslut = reslut && item.delete();
                }
            }
        } catch (Exception e) {
            objectNode.put("status", 1);
        } finally {
            objectNode.put("status", 0);
            fileOutputStream.close();
        }

        // 保存到数据库
        UploadFile uploadFile = new UploadFile();
        uploadFile.setOriginalName(name);
        uploadFile.setName(fileName);
        uploadFile.setMd5(md5File);
        uploadFile.setTime(new Date());
        uploadFile.setExt(fileUploadService.getFileExt(fileName));
        uploadFile.setType(fileUploadService.checkType(uploadFile.getExt()));
        File newFile = new File(fileUploadService.path + filePath);
        uploadFile.setSize(newFile.length());
        fileUploadService.save(uploadFile);

        return objectNode;
    }

    /**
     * 文件列表
     *
     * @return
     */
    @RequestMapping("/list")
    @ResponseBody
    public String list(HttpServletRequest request) {
        // 从cookie中获取登陆用户信息
        User user = userService.getCookieUser(request);
        // 日志记录
        logService.log(user, request);
        // 未登录
        if (user == null) return null;

        String list = fileUploadService.getFile(fileUploadService.path);
        return list;
    }

}
