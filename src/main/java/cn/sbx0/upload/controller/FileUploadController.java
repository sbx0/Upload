package cn.sbx0.upload.controller;

import java.io.*;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import cn.sbx0.upload.entity.User;
import cn.sbx0.upload.service.FileUploadService;
import cn.sbx0.upload.service.LogService;
import cn.sbx0.upload.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * 文件上传的Controller
 *
 * @author 单红宇(CSDN CATOOP)
 * @create 2017年3月11日
 */
@Controller
public class FileUploadController extends BaseController {
    @Resource
    private FileUploadService fileUploadService;
    @Resource
    private UserService userService;
    @Resource
    private LogService logService;

    /**
     * 文件上传具体实现方法（单文件上传）
     *
     * @param file
     * @return
     * @author 单红宇(CSDN CATOOP)
     * @create 2017年3月11日
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public String upload(@RequestParam("file") MultipartFile file, HttpServletRequest request, Map<String, Object> map) {
        objectNode = mapper.createObjectNode();
        // 从cookie中获取登陆用户信息
        User user = userService.getCookieUser(request);
        // 日志记录
        logService.log(user, request);
        if (user == null) {
            map.put("message", "未登陆");
            return "show";
        }
        if (!file.isEmpty()) {
            try {
                String originalFilename = file.getOriginalFilename();
                if (!fileUploadService.checkFileType(originalFilename)) {
                    map.put("message", "非法文件格式");
                }
                String filePath = fileUploadService.createPath(originalFilename);
                map.put("url", "/" + filePath);
                String path = fileUploadService.path + filePath;
                File newFile = new File(path);
                if (!newFile.getParentFile().exists()) {
                    newFile.getParentFile().mkdirs();
                }
                FileOutputStream fileOutputStream = new FileOutputStream(newFile);
                BufferedOutputStream out = new BufferedOutputStream(fileOutputStream);
                out.write(file.getBytes());
                out.flush();
                out.close();
            } catch (FileNotFoundException e) {
                map.put("message", e.getMessage());
            } catch (IOException e) {
                map.put("message", e.getMessage());
            }
        } else {
            map.put("message", "文件为空");
        }
        return "show";
    }

//    /**
//     * 多文件上传 主要是使用了MultipartHttpServletRequest和MultipartFile
//     *
//     * @param request
//     * @return
//     * @author 单红宇(CSDN CATOOP)
//     * @create 2017年3月11日
//     */
//    @RequestMapping(value = "/upload/batch", method = RequestMethod.POST)
//    public @ResponseBody
//    String batchUpload(HttpServletRequest request) {
//        List<MultipartFile> files = ((MultipartHttpServletRequest) request).getFiles("file");
//        MultipartFile file = null;
//        BufferedOutputStream stream = null;
//        for (int i = 0; i < files.size(); ++i) {
//            file = files.get(i);
//            if (!file.isEmpty()) {
//                try {
//                    byte[] bytes = file.getBytes();
//                    stream = new BufferedOutputStream(new FileOutputStream(new File(file.getOriginalFilename())));
//                    stream.write(bytes);
//                    stream.close();
//                } catch (Exception e) {
//                    stream = null;
//                    return "You failed to upload " + i + " => " + e.getMessage();
//                }
//            } else {
//                return "You failed to upload " + i + " because the file was empty.";
//            }
//        }
//        return "upload successful";
//    }
}
