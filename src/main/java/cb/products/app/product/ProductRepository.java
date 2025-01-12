package cb.products.app.product;


import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepository {

	@Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Product> rowMapper = (rs, rowNum) -> {
        Product product = new Product();
        product.setId(rs.getLong("id"));
        product.setName(rs.getString("name"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getDouble("price"));
        product.setImageId(rs.getInt("image_id"));
        return product;
    };

    public List<Product> findAll() {
        return jdbcTemplate.query("SELECT * FROM \"PRODUCT\"", rowMapper);
    }

    public Product findById(Long id) {
        return jdbcTemplate.queryForObject("SELECT * FROM \"PRODUCT\" WHERE id = ?", rowMapper, id);
    }

    public Product create() {
        String insertSQL = "INSERT INTO \"PRODUCT\" (name, price, description, image_id) VALUES (?, ?, ?, NULL)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connect -> {
            PreparedStatement ps = connect.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, "");
            ps.setDouble(2, 0.00);
            ps.setString(3, "");
            return ps;
        }, keyHolder);

        Map<String, Object> keys = keyHolder.getKeys();
        Number generatedId = (Number) keys.get("id");

        Product product = new Product();
        product.setDescription("");
        product.setName("");
        product.setPrice(0.0);
        product.setId(generatedId.longValue());

        return product;
    }

    public Product save(Product product) {
        String insertSQL = "INSERT INTO \"PRODUCT\" (name, price, description, image_id)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connect -> {
            PreparedStatement ps = connect.prepareStatement(insertSQL);
            ps.setString(1, product.getName());
            ps.setDouble(2, product.getPrice());
            ps.setString(3, product.getDescription());
            ps.setInt(4, product.getImageId());
            return ps;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();

        product.setId(generatedId.longValue());

        return product;
    }

    public Product update(Product product) {
        jdbcTemplate.update(
            "UPDATE \"PRODUCT\" SET name = ?, price = ?, description = ? WHERE id = ?",
            product.getName(), product.getPrice(), product.getDescription(), product.getId()
        );
        return product;
    }

    public boolean deleteById(Long id) {
        int deleted = jdbcTemplate.update("DELETE FROM \"PRODUCT\" WHERE id = ?", id);
        if (deleted == 1) {
            return true;
        }
        return false;
    }

    public byte[] findImageById(Long id) {
        return jdbcTemplate.queryForObject(
            "SELECT src FROM \"IMAGE\" WHERE id = ?", 
            (rs, rowNum) -> rs.getBytes("src"),
            id
        );
    }
}
