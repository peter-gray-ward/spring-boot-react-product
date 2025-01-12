package cb.products.app.user;


import java.sql.PreparedStatement;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

	@Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<User> rowMapper = (rs, rowNum) -> new User(
        rs.getLong("id"),
        rs.getString("name"),
        rs.getString("password"),
        rs.getString("role")
    );

    public List<User> findAll() {
        return jdbcTemplate.query("SELECT id, name, password, role FROM \"USER\"", rowMapper);
    }

    public User findById(Long id) {
        List<User> user = jdbcTemplate.query("SELECT id, name, password, role FROM \"USER\" WHERE id = ?", rowMapper, id);
        if (user.size() == 0) {
            return new User("Unable to find user " + id);
        }
        return user.get(0);
    }

    public User findByUsername(String username) {
        List<User> user = jdbcTemplate.query(
            "SELECT id, name, password, role"
            + " FROM \"USER\""
            + " WHERE name = ?", 
            rowMapper, 
            username
        );
        
        if (user.size() == 0) {
            return new User("Unable to find user " + username);
        }
        return user.get(0);
    }

    public User save(User user) {
        String insertSQL = "INSERT INTO \"USER\" (name, password, role) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(insertSQL);
            ps.setString(1, user.getName());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole());
            return ps;
        }, keyHolder);
        
        Number generatedId = keyHolder.getKey();

        user.setId(generatedId.longValue());

        return user;
    }

    public User update(User user) {
        jdbcTemplate.update(
            "UPDATE users SET name = ?, password = ?, role = ? WHERE id = ?",
            user.getName(), user.getPassword(), user.getId(), user.getRole()
        );
        return user;
    }

    public int deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
    }
}
