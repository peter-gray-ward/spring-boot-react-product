# Authentication

Login Results in the following cookies being set,

```bash
cbUserId
cbAccessToken
`

the latter of which must match the session value with key { user id }.access_token. Athentication requests are handled with AuthController, a @RestController with three paths: 1. /login, 2. /register, 3. /logout.