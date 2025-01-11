package cb.products.app.user;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class UserService  {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id);
    }

    public User findByUsername(String name) {
        return userRepository.findByUsername(name);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public User update(User user) {
        return userRepository.update(user);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}
