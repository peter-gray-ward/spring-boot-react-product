package cb.products.app.user;

import cb.products.app.Model;

public class Auth extends Model {
    private boolean authenticated;

    public boolean getAuthenticated() { return this.authenticated; }
    public void setAuthenticated(boolean authenticated) { this.authenticated = authenticated; }
}
