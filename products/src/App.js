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
  default:
    return state;
  }
}

var props = {
  showBanner: true,
  user: {
    id: null,
    name: null,
    access_token: null,
    login_type: 'register'
  },
  products: []
};

function App() {
  const [state, dispatch] = useReducer(PropsReducer, props);
  const [cbUser, setCbUser] = useState();

  const loadProducts = useMemo(() => () => {
    var xhr = new XMLHttpRequest();
    xhr.open("GET", "http://localhost:8080/products");
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
      xhr.open("POST", "http://localhost:8080/register");
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
      xhr.open("POST", "http://localhost:8080/login");
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

  // log-in with access token if exists
  useEffect(() => {
    var access_token = sessionStorage.cbAccessToken;
    var id = sessionStorage.cbUserId;
    if (access_token && id) {
      var xhr = new XMLHttpRequest();
      xhr.open("GET", "http://localhost:8080/login/access-token/" + id + "/" + access_token);
      xhr.setRequestHeader("content-type", "application/json");
      xhr.withCredentials = true;
      xhr.addEventListener('load', function() {
        var user = JSON.parse(this.response);
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
                state.products.map((product, i) => {
                  return <li key={i}>{product.name}</li>
                })
              }
            </ul>
            <div className="vertical-segment">
              Logout
            </div>
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
