package cn.sbx0.upload.dao;

import cn.sbx0.upload.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * 用户Dao
 */
public interface UserDao extends PagingAndSortingRepository<User, Integer> {
    @Query(value = "select 1 from users where name = ?1", nativeQuery = true)
    String existsByName(String name);

    @Query(value = "select * from users", nativeQuery = true)
    List<User> findAll();
}
