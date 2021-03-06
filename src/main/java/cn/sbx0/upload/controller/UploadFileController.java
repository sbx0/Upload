package cn.sbx0.upload.controller;

import java.io.*;
import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import cn.sbx0.upload.entity.UploadFile;
import cn.sbx0.upload.entity.User;
import cn.sbx0.upload.service.BaseService;
import cn.sbx0.upload.service.UploadFileService;
import cn.sbx0.upload.service.LogService;
import cn.sbx0.upload.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传的Controller
 */
@Controller
public class UploadFileController extends BaseController<UploadFile, Integer> {
    @Autowired
    private UploadFileService uploadFileService;
    @Resource
    private UserService userService;
    @Resource
    private LogService logService;

    @Autowired
    public UploadFileController(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public BaseService<UploadFile, Integer> getService() {
        return uploadFileService;
    }

    @Override
    public ObjectNode delete(Integer id, HttpServletRequest request) {
        UploadFile uploadFile = uploadFileService.findById(id);
        if (uploadFile == null) {
            json.put(STATUS_NAME, STATUS_CODE_NOT_FOUND);
            return json;
        } else {
            // 从cookie中获取登陆用户信息
            User user = userService.getCookieUser(request);
            if (user != null && user.getAuthority() == 0) {
                if (uploadFileService.deleteFile(uploadFile)) {
                    return super.delete(id, request);
                } else {
                    json.put(STATUS_NAME, STATUS_CODE_FILED);
                    return json;
                }
            } else {
                json.put(STATUS_NAME, STATUS_CODE_NO_PERMISSION);
                return json;
            }
        }
    }

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
        json = mapper.createObjectNode();
        // 从cookie中获取登陆用户信息
        User user = userService.getCookieUser(request);
        // 日志记录
        logService.log(user, request);
        // 未登录 直接否
        if (user == null) {
            json.put("status", 2);
            json.put("msg", "未登录");
            return json;
        }
        UploadFile uploadFile = uploadFileService.md5Check(md5File);
        if (uploadFile != null) {
            json.put("status", 1);
            json.put("oName", uploadFile.getOriginalName());
            json.put("name", uploadFile.getName());
            json.put("type", uploadFile.getType());
        } else {
            json.put("status", 0);
        }
        return json;
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
        String path = uploadFileService.getPath() + "/temp/" + md5File + "/";//分片存放目录
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

        String path = uploadFileService.getPath() + "/temp/" + md5File + "/";
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
        ObjectNode json = mapper.createObjectNode();
        // 从cookie中获取登陆用户信息
        User user = userService.getCookieUser(request);
        // 日志记录
        logService.log(user, request);
        // 未登录
        if (user == null) {
            json.put("status", 1);
            return json;
        }

        String path = uploadFileService.getPath();
        String fileName = uploadFileService.createFileName(name);
        String filePath = uploadFileService.createPath(fileName);
        json.put("url", filePath);
        // 合成后的文件
        FileOutputStream fileOutputStream = new FileOutputStream(path + filePath);

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
                // 文件夹有内容,先删除内容
                if (!(childFiles == null || childFiles.length == 0)) {
                    // 删除文件夹内容
                    boolean reslut = true;
                    for (File item : file.listFiles()) {
                        reslut = reslut && item.delete();
                    }
                }
                // 删除文件夹
                file = new File(path + "/temp/" + md5File);
                file.delete();
            }
        } catch (Exception e) {
            json.put("status", 1);
        } finally {
            json.put("status", 0);
            fileOutputStream.close();
        }

        // 保存到数据库
        UploadFile uploadFile = new UploadFile();
        uploadFile.setOriginalName(name);
        uploadFile.setName(fileName);
        uploadFile.setMd5(md5File);
        uploadFile.setTime(new Date());
        uploadFile.setExt(uploadFileService.getFileExt(fileName));
        uploadFile.setType(uploadFileService.checkType(uploadFile.getExt()));
        File newFile = new File(uploadFileService.getPath() + filePath);
        uploadFile.setSize(newFile.length());
        uploadFileService.save(uploadFile);

        return json;
    }

    /**
     * 文件列表
     *
     * @return
     */
    @RequestMapping("/list")
    public String list(Integer page, Integer size, String sort, String direction, String type, Map<String, Object> map, HttpServletRequest request) {
        // 从cookie中获取登陆用户信息
        User user = userService.getCookieUser(request);
        if (user == null) {
            return "error";
        } else {
            if (page == null) page = 1;
            if (size == null) size = 50;
            if (BaseService.checkNullStr(sort)) sort = "id";
            if (BaseService.checkNullStr(direction)) direction = "desc";
            // 物理手段的文件列表
            // String list = uploadFileService.getFile(uploadFileService.getPath());
            // 获取数据库中的文件列表
            Page<UploadFile> uploadFiles = uploadFileService.findAll(page - 1, size, sort, direction, type);
            if (uploadFiles != null) {
                // 当页数大于总页数时，查询最后一页的数据
                if (page > uploadFiles.getTotalPages()) {
                    uploadFiles = uploadFileService.findAll(uploadFiles.getTotalPages() - 1, size, sort, direction, type);
                }
                map.put("size", uploadFiles.getPageable().getPageSize());
                map.put("page", uploadFiles.getPageable().getPageNumber() + 1);
                map.put("sort", sort);
                map.put("direction", direction);
                map.put("type", type);
                map.put("lists", uploadFiles.getContent());
                map.put("totalPages", uploadFiles.getTotalPages());
                map.put("totalElements", uploadFiles.getTotalElements());
                // 判断上下页
                if (page + 1 <= uploadFiles.getTotalPages()) map.put("next_page", page + 1);
                if (page - 1 > 0) map.put("prev_page", page - 1);
                if (page - 1 > uploadFiles.getTotalPages()) map.put("prev_page", null);
            }
            return "list";
        }
    }

}
