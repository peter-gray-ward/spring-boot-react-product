package cb.products.app.product;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product findById(Long id) {
        return productRepository.findById(id);
    }

    public Product save() {
        return productRepository.create();
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public Product update(Product product) {
        return productRepository.update(product);
    }

    public boolean deleteById(Long id) {
        return productRepository.deleteById(id);
    }

    public byte[] findImageById(Long id) {
        return productRepository.findImageById(id);
    }
}
