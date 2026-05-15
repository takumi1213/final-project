package sample.common.dao.mapper;
import org.apache.ibatis.annotations.Mapper;
import sample.common.dao.entity.Login;

@Mapper
public interface LoginMapper {
    Login findByUsername(String username);
    void insert(Login login);
}