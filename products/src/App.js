import logo from './logo.svg';
import './App.css';
import React, {
  useState,
  useEffect,
  useReducer,
  useRef,
  useMemo
} from 'react';

function Cookie(key) {
  var cookies = document.cookie.split(';').filter(cookie => new RegExp("^" + key).test(cookie));
  return cookies.length ? cookies[0] : null;
}

function PropsReducer(state, action) {
  switch (action.type) {
  case 'REGISTER_RESPONSE':
    return {
      ...state,
      user: action.user
    }
  case 'LOGIN_RESPONSE':
    return {
      ...state,
      user: action.user
    }
  case 'UPDATE_BANNER':
    return {
      ...state,
      showBanner: action.showBanner
    };
  case 'LOAD_PRODUCTS':
    return {
      ...state,
      products: action.products
    };
  case 'GO_TO_PROFILE':
    return {
      ...state,
      profile: true
    }
  default:
    return state;
  }
}

var props = {
  showBanner: true,
  profile: false,
  user: {
    id: null,
    name: null,
    access_token: null,
    login_type: 'register'
  },
  products: []
};

function App() {
  const host = useMemo(() => 'http://localhost:8080',[]);
  const [state, dispatch] = useReducer(PropsReducer, props);
  const [cbUser, setCbUser] = useState();
  const [product, setProduct] = useState();
  

  const loadProducts = useMemo(() => () => {
    var xhr = new XMLHttpRequest();
    xhr.open("GET", host + "/products");
    xhr.withCredentials = true;
    xhr.addEventListener("load", function() {
      var res = JSON.parse(this.response);
      if (Array.isArray(res)) {
        dispatch({ type: 'LOAD_PRODUCTS', products: res });
      }
    });
    xhr.send();
  })
  const register = useMemo(() => {
    return (event) => {
      var xhr = new XMLHttpRequest();
      xhr.open("POST", host + "/register");
      xhr.setRequestHeader("content-type", "application/json");
      xhr.withCredentials = true;
      xhr.addEventListener('load', function() {
        var user = JSON.parse(this.response);
        user.request = 'register'
        if (!user.exception) {
          user.registered = user.name + " is now successfully registered.";
        }
        dispatch({ type: 'REGISTER_RESPONSE', user });
      });
      xhr.send(JSON.stringify({
        name: document.querySelector("#register input[name=username]").value,
        password: document.querySelector("#register input[name=password]").value,
        role: document.querySelector("#register input[name=role]").checked ? "ADMIN" : "CUSTOMER"
      }));
    }
  }, []);

  const logout = useMemo(() => () => {
    delete sessionStorage.cbUserId;
    delete sessionStorage.cbAccessToken;
    window.location.reload();
  }, [cbUser]);

  const loadApp = useMemo(() => (user) => {
    user.loggedin = user.name + " is now successfully logged in.";
    setCbUser(user);
    loadProducts();
    setTimeout(() => {
      dispatch({ type: 'UPDATE_BANNER', showBanner: false });
    }, 3000); 
  }, [])

  const login = useMemo(() => {
    return (event) => {
      var xhr = new XMLHttpRequest();
      xhr.open("POST", host + "/login");
      xhr.setRequestHeader("content-type", "application/json");
      xhr.withCredentials = true;
      xhr.addEventListener('load', function() {
        var user = JSON.parse(this.response);
        user.request = 'login'
        if (!user.exception) {
          sessionStorage.cbUserId = user.id;
          sessionStorage.cbAccessToken = user.accessToken;
          loadApp(user);
        }
        dispatch({ type: 'LOGIN_RESPONSE', user });
      });
      xhr.send(JSON.stringify({
        name: document.querySelector("#login input[name=username]").value,
        password: document.querySelector("#login input[name=password]").value,
        role: document.querySelector("#login input[name=role]").checked ? "ADMIN" : "CUSTOMER"
      }));
    }
  }, []);

  const profile = useMemo(() => {
    dispatch({ type: 'GO_TO_PROFILE' })
  }, [])

  const selectProduct = useMemo(() => event => {
    var id = +event.currentTarget.id;
    setProduct(state.products.filter(p => p.id == id)[0]);
  }, [state.products]);

  const removeProduct = useMemo(() => event => {
    var id = event.currentTarget;
    while (id.id !== 'product') {
      id = id.parentElement;
    }
    id = id.dataset.id;
    var xhr = new XMLHttpRequest();
    xhr.open("DELETE", host + "/products/" + id);
    xhr.withCredentials = true;
    xhr.addEventListener("load", function() {
      var res = JSON.parse(this.response);
      console.log("deleted? ", res);
      if (new Boolean(res)) {
        state.products = state.products.filter(p => p.id !== +id);
        dispatch({ type: 'LOAD_PRODUCTS', products: state.products });
        setProduct(null);
      }
    });
    xhr.send();
  }, [state.products]);

  const addProduct = useMemo(() => event => {
    var xhr = new XMLHttpRequest();
    xhr.open("POST", host + "/products");
    xhr.withCredentials = true;
    xhr.addEventListener("load", function() {
      var product = JSON.parse(this.response);
      if (product.id) {
        state.products.push(product);
        dispatch({ type: 'LOAD_PRODUCTS', products: state.products });
        setProduct(product);
      }
    });
    xhr.send();
  }, [state.products]);

  const saveProduct = useMemo(() => event => {
    var xhr = new XMLHttpRequest();
    xhr.open("PUT", host + "/products/" + product.id);
    xhr.setRequestHeader("content-type", "application/json");
    xhr.withCredentials = true;
    xhr.addEventListener("load", function() {
      var product = JSON.parse(this.response);
      if (product.id) {
        state.products = state.products.map(p => {
          if (p.id == product.id) {
            p.name = document.querySelector("div[data-id=\"" + p.id + "\"] h1").innerHTML
            product.name = p.name
            p.description = document.querySelector("div[data-id=\"" + p.id + "\"] p").innerHTML
            product.description = p.description
            return product;
          }
          return p;
        })
        alert('Product Saved');
        dispatch({ type: 'LOAD_PRODUCTS', products: state.products });
        setProduct(product);
      }
    });
    xhr.send(JSON.stringify(product));
  }, [product]);

  const handleProductInputChange = (e) => {
    const { name, value } = e.target;
    console.log(name, value);
    setProduct((prevProduct) => ({
        ...prevProduct,
        [name]: name === 'price' ? parseFloat(value) || 0.0 : value, // Convert price to a number
    }));
  };

  // log-in with access token if exists
  useEffect(() => {
    var access_token = sessionStorage.cbAccessToken;
    var id = sessionStorage.cbUserId;
    if (access_token && id) {
      var xhr = new XMLHttpRequest();
      xhr.open("GET", "/login/access-token/" + id + "/" + access_token);
      xhr.setRequestHeader("content-type", "application/json");
      xhr.withCredentials = true;
      xhr.addEventListener('load', function() {
        var user = {};
        try {
          user = JSON.parse(this.response);
        } catch (err) {}
       
        if (user.id) {
          loadApp(user);
        }
      });
      xhr.send();
    }
  }, []);



  return (
    <div className="App">
     {
       cbUser ? <div id="root">
        {
          state.showBanner ? <div id="banner">
            Welcome, {cbUser.name}
          </div>  : null
        }

        <main style={{
          height: state.showBanner ? `calc(100vh - 40px)` : `100%`
        }}>
          <div className="left-panel">
            <h1 className="vertical-segment">
              {cbUser.role} Portal
            </h1>
            <ul>
              {
                cbUser.role == 'ADMIN' ? 
                  <li onClick={addProduct}>+ Add Product</li>
                : null
              }
            </ul>
            <div className="vertical-segment">
              <a id="profile" onClick={profile}>Go to Profile</a>
              <a id="logout" onClick={logout}>Logout</a>
            </div>
          </div>


          
          <div id="product-view">
            <div className="breadcrumbs">
              {
                product ? <>
                  <a onClick={() => setProduct(null)}>All Products</a> <span>{">"}</span> <a>{product.name}</a>
                </> : <a>All Products</a>
              }
            </div>
            
            <article>
              {
                product 

                ? 

                  cbUser.role == 'ADMIN' 

                  ? 
                    <div id="product" data-id={product.id}>
                      
                      <h1 contentEditable dangerouslySetInnerHTML={{ __html: product.name }} 
                        onChange={handleProductInputChange} type="text" name="name"></h1>


                      <p contentEditable dangerouslySetInnerHTML={{ __html: product.description }}
                       onChange={handleProductInputChange} type="text" name="description"></p>
                      
                      
                    </div>
                  : 
                    <div id="product">
                      
                      <h1>{product.name}</h1>
                      <p>{product.description}</p>
                      <h2>${product.price}</h2>
                    </div>

                :

                  state.products.map((product, i) => {
                    return <div className="product-preview" onClick={selectProduct} id={product.id}>
                      <div className="thumbnail"></div>
                      <h2>{product.name}</h2>
                    </div>
                  })
                
              }


            </article>
            {
              product ? 
                cbUser.role == 'ADMIN' 

                  ? 

                <section className="actions">
                  <button>Purchase</button>
                  <button onClick={removeProduct}>Remove</button>
                  <button onClick={saveProduct}>save</button>
                  <button>
                          <span>$</span>
                          <input onChange={handleProductInputChange} name="price" 
                            className="currency" type="number" value={product.price} />
                  </button>
                </section>
                  :
                <section className="actions">
                  <button>Purchase</button>
                  <button>
                          <span>$</span>
                          <input disabled name="price" 
                            className="currency" type="number" value={product.price} />
                  </button>
                </section>

              : null
            }
          </div>

        </main>
      </div> : 

      <div id="authentication">
        <p><i>Returning user?</i></p>
        <h1>Login</h1>
        <div id="login">
          <label for="username">Username:</label>
          <input type="text" name="username" />
          <label for="password">Password:</label>
          <input type="text" name="password" />
          <section>
            <label for="role">Admin:</label>
            <input type="checkbox" name="role" />
          </section>
          {
            (state.user.request == 'login'
            && state.user.loggedin) ? <p className="success">
            {state.user.loggedin}
            </p> : null
          }
          {
            (state.user.request == 'login'
            && state.user.exception) ? <p className="exception">
            {state.user.exception}
            </p> : null
          }
          <button onClick={login}>login</button>
        </div>
        <p><i>Otherwise,</i></p>
        <h1>Register</h1>
        <div id="register">
          <label for="username">Username:</label>
          <input type="text" name="username" />
          <label for="password">Password:</label>
          <input type="text" name="password" />
          <section>
            <label for="role">Admin:</label>
            <input type="checkbox" name="role" />
          </section>
          {
            (state.user.request == 'register'
            && state.user.registered) ? <p className="success">
            {state.user.registered}
            </p> : null
          }
          {
            (state.user.request == 'register'
            && state.user.exception) ? <p className="exception">
            {state.user.exception}
            </p> : null
          }
          <button onClick={register}>register</button>
        </div>
      </div>
     }
    </div>
  );
}

export default App;
