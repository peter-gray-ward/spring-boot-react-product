package cb.products.app.user;

public class User {
	private Long id;
    private String name;
    private String password;
    private String accessToken;
    private String role;
    private String exception;

    public User() {}
    public User(String exception) {
        this.exception = exception;
    }
    public User(Long id, String name, String password, String role) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.role = role;
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getPassword() { return password; }
    public String getAccessToken() { return accessToken; }
    public String getRole() { return role; }
    public String getException() { return exception; }
    // Setters
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPassword(String password) { this.password = password; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public void setRole(String role) { this.role = role; }
    public void setException (String exception) { this.exception = exception; }
}