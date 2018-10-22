package cn.sbx0.upload.dao;

import cn.sbx0.upload.entity.UploadFile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 用户Dao
 */
public interface UploadFileDao extends PagingAndSortingRepository<UploadFile, Integer> {

    /**
     * 根据md5判断文件是否存在
     *
     * @param md5
     * @return
     */
    @Query(value = "select * from files where md5 = ?1", nativeQuery = true)
    UploadFile existsByMd5(String md5);

}
