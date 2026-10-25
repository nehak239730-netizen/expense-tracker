# Expense Tracker (Spring Boot + MySQL + HTML/CSS/JS)

A beginner-friendly full-stack app to record, view, update, delete and filter your expenses.
Ek beginner-friendly full-stack app jisme aap apne kharche add, dekh, update, delete aur filter kar sakte ho.

**Stack:** Java 17, Spring Boot 3.3, Spring Web, Spring Data JPA/Hibernate, MySQL 8, Maven, plain HTML/CSS/JavaScript (Fetch API), Docker + Docker Compose, JUnit 5 + Spring Boot Test.

---

## 1. Features

| # | Feature | Where |
|---|---------|-------|
| 1 | Add expense (title, amount, category, date, description) | Form on the left / `POST /api/expenses` |
| 2 | Dashboard listing all expenses | Table on the right / `GET /api/expenses` |
| 3 | View one expense by id | `GET /api/expenses/{id}` (also used by the Edit button) |
| 4 | Update and delete | `PUT` / `DELETE /api/expenses/{id}` |
| 5 | Filter by date range and category | `GET /api/expenses/filter?start=&end=&category=` |
| 6 | Total, count, average | `GET /api/expenses/summary` (same optional filters) |
| 7 | Validation + meaningful errors (400, 404, 405, 415, 500) | `ExpenseRequest` + `GlobalExceptionHandler` |
| 8 | Frontend talks to API with Fetch | `static/js/app.js` |
| 9 | Data saved in MySQL | Spring Data JPA |
| 10 | Docker setup | `Dockerfile`, `docker-compose.yml` |

---

## 2. Project structure

```
expense-tracker/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── .env.example
├── .gitignore
├── .dockerignore
├── README.md
├── docs/
│   └── sample-requests.http
├── postman/
│   └── ExpenseTracker.postman_collection.json
└── src/
    ├── main/
    │   ├── java/com/expensetracker/
    │   │   ├── ExpenseTrackerApplication.java
    │   │   ├── model/Expense.java
    │   │   ├── repository/ExpenseRepository.java
    │   │   ├── service/ExpenseService.java
    │   │   ├── controller/ExpenseController.java
    │   │   ├── dto/ExpenseRequest.java
    │   │   ├── dto/ExpenseSummary.java
    │   │   ├── dto/ErrorResponse.java
    │   │   └── exception/
    │   │       ├── GlobalExceptionHandler.java
    │   │       ├── ResourceNotFoundException.java
    │   │       └── BadRequestException.java
    │   └── resources/
    │       ├── application.properties
    │       └── static/
    │           ├── index.html
    │           ├── css/style.css
    │           └── js/app.js
    └── test/
        ├── java/com/expensetracker/
        │   ├── ExpenseTrackerApplicationTests.java
        │   ├── service/ExpenseServiceTest.java
        │   └── controller/ExpenseControllerTest.java
        └── resources/application.properties
```

Request flow / Request ka raasta:

```
Browser (app.js)  --Fetch-->  Controller  -->  Service  -->  Repository  -->  MySQL
                  <--JSON--   (HTTP)          (rules)       (DB access)
                              Errors anywhere  --> GlobalExceptionHandler --> clean JSON error
```

---

## 3. What every file does (English + Hinglish)

### Root files

**`pom.xml`**
- EN: Maven's recipe. Lists the libraries (Spring Web, JPA, Validation, MySQL driver, test tools) and Java version 17.
- Hinglish: Maven ki recipe. Isme likha hai kaunsi libraries chahiye (Spring Web, JPA, Validation, MySQL driver, testing) aur Java 17 use karna hai.

**`Dockerfile`**
- EN: Instructions to build a Docker image of the app. Stage 1 builds the JAR with Maven, stage 2 runs it on a small Java image as a non-root user.
- Hinglish: App ka Docker image banane ki instructions. Pehle stage me Maven se JAR banta hai, dusre stage me chhote Java image par non-root user se chalta hai (zyada safe).

**`docker-compose.yml`**
- EN: Starts two containers together: MySQL (`db`) and the app (`app`). The app waits until MySQL is healthy. Data is kept in a Docker volume.
- Hinglish: Ek hi command se do container chalata hai: MySQL aur app. App tab tak wait karta hai jab tak MySQL ready na ho. Data Docker volume me save rehta hai, container delete hone par bhi.

**`.env.example`**
- EN: Template for database passwords. Copy it to `.env` and change the values. The real `.env` is never uploaded to GitHub.
- Hinglish: Database password ka template. Ise copy karke `.env` banao aur password badlo. Asli `.env` GitHub par kabhi upload nahi hoti.

**`.gitignore`**
- EN: Tells Git which files to skip (`target/`, `.env`, IDE files).
- Hinglish: Git ko batata hai kaunsi files upload nahi karni (`target/`, `.env`, IDE files).

**`.dockerignore`**
- EN: Files Docker should not copy into the image (keeps the image small and secrets out).
- Hinglish: Docker image me kaunsi files nahi jaani chahiye (image chhoti rehti hai, secrets bahar rehte hain).

**`README.md`** - EN: this guide. Hinglish: ye guide.

### Java files (`src/main/java/com/expensetracker/`)

**`ExpenseTrackerApplication.java`**
- EN: Entry point with the `main` method. Starting it boots Spring Boot and the web server on port 8080.
- Hinglish: Yahin se app start hoti hai (`main` method). Spring Boot aur web server port 8080 par chalu ho jata hai.

**`model/Expense.java`**
- EN: The Entity. One object is one row of the `expenses` table (id, title, amount, category, date, description, created_at). Amount uses `BigDecimal` for exact money math.
- Hinglish: Database table `expenses` ki ek row ko Java object me dikhata hai. Paisa `BigDecimal` me rakha hai taaki hisaab bilkul sahi rahe (double me galtiyan aati hain).

**`repository/ExpenseRepository.java`**
- EN: Talks to the database. Extending `JpaRepository` gives save/find/delete for free; `JpaSpecificationExecutor` allows optional filters.
- Hinglish: Database se baat karta hai. `JpaRepository` se save/find/delete bina likhe mil jata hai; `JpaSpecificationExecutor` se optional filters lagte hain.

**`service/ExpenseService.java`**
- EN: Business logic: create, update, delete, filter, and calculate the summary (total, count, average). Throws errors like "not found".
- Hinglish: Asli logic yahan hai: add, update, delete, filter aur summary (total, count, average) nikalna. Galat cheez par error throw karta hai.

**`controller/ExpenseController.java`**
- EN: Defines the REST URLs (`/api/expenses...`) and returns the right HTTP status codes (200, 201, 204).
- Hinglish: REST URLs (`/api/expenses...`) yahan define hain aur sahi status code (200, 201, 204) return hota hai.

**`dto/ExpenseRequest.java`**
- EN: The shape of JSON the client sends, with validation rules (title required, amount ≥ 0.01, date not in the future, ...).
- Hinglish: Client jo JSON bhejta hai uska format, saath me rules (title zaroori, amount ≥ 0.01, future date nahi chalegi, ...).

**`dto/ExpenseSummary.java`**
- EN: The dashboard numbers returned by `/summary`.
- Hinglish: Dashboard ke numbers (total, count, average) jo `/summary` return karta hai.

**`dto/ErrorResponse.java`**
- EN: One standard JSON shape for all errors (status, message, path, field errors).
- Hinglish: Har error ka ek jaisa JSON format (status, message, path, field errors).

**`exception/GlobalExceptionHandler.java`**
- EN: `@RestControllerAdvice` class that catches exceptions and turns them into clear JSON errors. It never leaks stack traces.
- Hinglish: Saare errors yahin pakde jate hain aur saaf JSON message me badal jate hain. Stack trace bahar nahi dikhta (security).

**`exception/ResourceNotFoundException.java`** - EN: thrown when an id does not exist (404). Hinglish: jab id database me nahi milti (404).

**`exception/BadRequestException.java`** - EN: thrown for logical mistakes like start date after end date (400). Hinglish: logical galti par, jaise start date end date ke baad ho (400).

### Resources (`src/main/resources/`)

**`application.properties`**
- EN: App settings: port, MySQL connection, Hibernate. Passwords come from environment variables (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`), not from the file.
- Hinglish: App ki settings: port, MySQL connection, Hibernate. Password file me nahi likha; environment variables (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`) se aata hai.

**`static/index.html`** - EN: the dashboard page (form, summary, filters, table). Hinglish: dashboard ka page (form, summary, filters, table).

**`static/css/style.css`** - EN: styling and responsive layout. Hinglish: design aur mobile-friendly layout.

**`static/js/app.js`**
- EN: Calls the REST API with Fetch, draws the table and numbers, handles add/edit/delete/filter, shows errors. It uses `textContent`, so user text cannot inject HTML (XSS safe).
- Hinglish: Fetch se REST API call karta hai, table aur numbers dikhata hai, add/edit/delete/filter sambhalta hai aur errors dikhata hai. `textContent` use hota hai, isliye koi HTML inject nahi kar sakta.

### Tests (`src/test/`)

**`ExpenseTrackerApplicationTests.java`** - EN: checks the app starts. Hinglish: check karta hai app start hoti hai ya nahi.

**`service/ExpenseServiceTest.java`** - EN: fast unit tests of the service with Mockito (summary maths, not-found, bad date range). Hinglish: service ke fast unit tests (summary ka hisaab, not-found, galat date range).

**`controller/ExpenseControllerTest.java`** - EN: full API tests with MockMvc (201, 400, 404, filter, summary, delete). Hinglish: poori API ke tests (201, 400, 404, filter, summary, delete).

**`test/resources/application.properties`** - EN: switches tests to in-memory H2, so MySQL is not needed to run tests. Hinglish: tests ke liye H2 in-memory database, isliye `mvn test` ko MySQL nahi chahiye.

### Helper files

**`postman/ExpenseTracker.postman_collection.json`** - EN: ready-made Postman requests. Hinglish: Postman me import karne ke liye ready requests.

**`docs/sample-requests.http`** - EN: same requests as plain text (VS Code REST Client). Hinglish: wahi requests text format me (VS Code REST Client ke liye).

---

## 4. Windows setup (one time)

Open **PowerShell** for the commands below. / Neeche ke commands PowerShell me chalao.

### 4.1 Install the tools

| Tool | Needed for | Download |
|------|-----------|----------|
| JDK 17 (Eclipse Temurin) | running Java | https://adoptium.net (choose 17, `.msi`, tick "Set JAVA_HOME") |
| Maven 3.9+ | building | https://maven.apache.org/download.cgi (unzip, e.g. to `C:\tools\maven`, add `C:\tools\maven\bin` to PATH) |
| Docker Desktop | **Option A** (easiest) | https://www.docker.com/products/docker-desktop |
| MySQL 8 Community + Workbench | **Option B** (without Docker) | https://dev.mysql.com/downloads/installer/ |
| Git | GitHub upload | https://git-scm.com/download/win |
| Postman | API testing | https://www.postman.com/downloads/ |

You only need **Docker Desktop** *or* **MySQL + Maven + JDK**. Docker alone is enough to run the app (the JDK/Maven live inside the image).
Sirf **Docker Desktop** se bhi app chal jayegi. Tests ya local run ke liye JDK + Maven chahiye.

### 4.2 Verify the installation

Close and reopen PowerShell after installing, then run:

```powershell
java -version      # should show 17.x
mvn -version       # should show Apache Maven 3.9.x and Java 17
git --version
docker --version   # only if using Docker
docker compose version
```

---

## 5. Run the project

### Option A: Docker (recommended / sabse aasan)

```powershell
cd C:\path\to\expense-tracker

# 1. Create your private settings file
copy .env.example .env
notepad .env        # change both passwords, save, close

# 2. Build and start MySQL + app
docker compose up --build
```

Wait for a log line like `Started ExpenseTrackerApplication`. Then open **http://localhost:8080**.

Useful commands:

```powershell
docker compose up --build -d     # run in background
docker compose logs -f app       # watch app logs
docker compose ps                # status of containers
docker compose down              # stop (data is KEPT)
docker compose down -v           # stop AND delete all data
```

MySQL is also reachable from your PC on port **3307** (user from `.env`) for tools like MySQL Workbench.

### Option B: Without Docker (local MySQL)

1. Make sure the **MySQL80** Windows service is running (`services.msc`).
2. (Optional) create the database manually. The app also creates it automatically.
   ```powershell
   mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS expense_db;"
   ```
3. Set the credentials **for this PowerShell window only** (not stored in any file):
   ```powershell
   $env:DB_USERNAME = "root"
   $env:DB_PASSWORD = "your_mysql_root_password"
   ```
4. Start the app:
   ```powershell
   cd C:\path\to\expense-tracker
   mvn spring-boot:run
   ```
5. Open **http://localhost:8080**.

Tip: for real use, create a dedicated MySQL user instead of `root`:
```sql
CREATE USER 'expense_user'@'localhost' IDENTIFIED BY 'a_strong_password';
GRANT ALL PRIVILEGES ON expense_db.* TO 'expense_user'@'localhost';
FLUSH PRIVILEGES;
```
and set `$env:DB_USERNAME = "expense_user"`.

### Run the tests

```powershell
mvn test
```
Tests use in-memory H2, so MySQL does not have to be running. / Tests ke liye MySQL chalu hona zaroori nahi.

### Build a JAR (optional)

```powershell
mvn clean package
java -jar target\expense-tracker-1.0.0.jar
```

---

## 6. REST API reference

Base URL: `http://localhost:8080`

| Method | URL | Purpose | Success |
|--------|-----|---------|---------|
| GET | `/api/expenses` | All expenses (newest first) | 200 |
| GET | `/api/expenses/{id}` | One expense | 200 / 404 |
| POST | `/api/expenses` | Create | 201 + `Location` header |
| PUT | `/api/expenses/{id}` | Update | 200 / 404 |
| DELETE | `/api/expenses/{id}` | Delete | 204 / 404 |
| GET | `/api/expenses/filter?start=YYYY-MM-DD&end=YYYY-MM-DD&category=Food` | Filter (all 3 params optional) | 200 |
| GET | `/api/expenses/summary?start=&end=&category=` | Total, count, average (same optional filters) | 200 |

**Request body (POST / PUT)**

```json
{
  "title": "Lunch at canteen",
  "amount": 250.50,
  "category": "Food",
  "date": "2026-10-20",
  "description": "Thali and lassi"
}
```

**Validation rules**

| Field | Rule |
|-------|------|
| title | required, max 100 chars |
| amount | required, ≥ 0.01, up to 10 digits + 2 decimals |
| category | required, max 50 chars |
| date | required, `YYYY-MM-DD`, not in the future |
| description | optional, max 500 chars |

**Summary response**

```json
{ "totalAmount": 1150.50, "transactionCount": 2, "averageAmount": 575.25 }
```

**Error response (validation example, HTTP 400)**

```json
{
  "timestamp": "2026-10-24T10:15:30.123",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed. Please correct the highlighted fields.",
  "path": "/api/expenses",
  "fieldErrors": {
    "title": "Title is required",
    "amount": "Amount must be at least 0.01"
  }
}
```

### Sample requests in PowerShell

PowerShell's `curl` is an alias for something else, so use `Invoke-RestMethod`:

```powershell
# Create
$body = @{ title="Lunch"; amount=250.50; category="Food"; date="2026-10-20"; description="Thali" } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/expenses -ContentType "application/json" -Body $body

# List all
Invoke-RestMethod http://localhost:8080/api/expenses

# One by id
Invoke-RestMethod http://localhost:8080/api/expenses/1

# Filter by date range + category
Invoke-RestMethod "http://localhost:8080/api/expenses/filter?start=2026-10-01&end=2026-10-31&category=Food"

# Summary
Invoke-RestMethod "http://localhost:8080/api/expenses/summary"

# Delete
Invoke-RestMethod -Method Delete -Uri http://localhost:8080/api/expenses/1
```

If you prefer curl, use **`curl.exe`** (with the `.exe`) in PowerShell, or run it in Git Bash:

```bash
curl -X POST http://localhost:8080/api/expenses \
  -H "Content-Type: application/json" \
  -d '{"title":"Lunch","amount":250.50,"category":"Food","date":"2026-10-20","description":"Thali"}'
```

---

## 7. Test with Postman

1. Start the app (Section 5).
2. Open Postman → **Import** → choose `postman/ExpenseTracker.postman_collection.json`.
3. The collection has a variable `baseUrl = http://localhost:8080` (edit it if you changed the port).
4. Run the requests **in order**:
   1. **Create expense** → expect `201 Created`. The test script saves the new id into `expenseId` automatically.
   2. **Get all expenses** → expect `200` and your expense in the list.
   3. **Get expense by id** → `200`.
   4. **Update expense** → `200` with new values.
   5. **Filter** and **Summary** → `200`.
   6. **Delete expense** → `204 No Content`.
   7. **ERROR demo - invalid body** → `400` with `fieldErrors`.
   8. **ERROR demo - id not found** → `404`.
5. Or click the collection → **Run** to execute everything at once.

Hinglish: Postman kholo → Import → JSON file choose karo → requests ko order me chalao. Create ke baad `expenseId` khud set ho jata hai, isliye baaki requests seedha kaam karengi.

---

## 8. How to verify everything works locally

Tick each box:

- [ ] `http://localhost:8080` shows the dashboard with **Total spent ₹0.00**.
- [ ] Add an expense with the form → green toast "Expense saved", row appears, totals update.
- [ ] Add a second expense in a different category. Count = 2, average is correct.
- [ ] Click **Edit** → form fills → change amount → **Update expense** → row changes.
- [ ] Use **From / To / Category** → **Apply filters** → table and summary both change. **Clear** resets.
- [ ] Click **Delete** → confirm → row disappears.
- [ ] Submit the form with an empty title → red message under the form (server validation).
- [ ] Open `http://localhost:8080/api/expenses` in the browser → JSON list.
- [ ] Open `http://localhost:8080/api/expenses/999999` → JSON 404 error.
- [ ] Restart the app (`docker compose restart app` or stop/start) → **data is still there** (persisted in MySQL).
- [ ] `mvn test` → `BUILD SUCCESS`.
- [ ] Optional: check the table in MySQL
  ```powershell
  docker exec -it expense-mysql mysql -u expense_user -p expense_db -e "SELECT * FROM expenses;"
  ```
  (Docker users; enter the `MYSQL_PASSWORD` from `.env`.)

---

## 9. Common errors and fixes

| Problem | Why | Fix |
|---------|-----|-----|
| `'mvn' is not recognized` | Maven not in PATH | Add `...\maven\bin` to PATH, reopen PowerShell |
| `'java' is not recognized` or wrong version | JDK missing or old | Install JDK 17, check `java -version`, set `JAVA_HOME` |
| `release version 17 not supported` | Maven is using an older Java | Point `JAVA_HOME` to JDK 17 |
| `Port 8080 was already in use` | Another program uses 8080 | `netstat -ano \| findstr :8080` then `taskkill /PID <pid> /F`, or run with `--server.port=8081` |
| `Communications link failure` / `Connection refused` | MySQL not running or wrong port | Start MySQL service; for Docker use `docker compose ps` |
| `Access denied for user 'root'@'localhost'` | Wrong password | Re-set `$env:DB_PASSWORD` in the same PowerShell window |
| `Public Key Retrieval is not allowed` | MySQL 8 auth | Already handled by `allowPublicKeyRetrieval=true` in the URL; check you did not remove it |
| `Unknown database 'expense_db'` | DB missing and auto-create off | Keep `createDatabaseIfNotExist=true` or create it manually |
| `error while interpolating ... Set MYSQL_PASSWORD in the .env file` | `.env` missing | `copy .env.example .env` and fill in values |
| Docker: `Cannot connect to the Docker daemon` / `error during connect` | Docker Desktop not running | Open Docker Desktop and wait for "Engine running" |
| Docker: `port is already allocated` (3307 or 8080) | Port used by another app | Change the left side of `ports:` in `docker-compose.yml` |
| Changed `.env` password but login still fails | MySQL only reads passwords on first start | `docker compose down -v` then `docker compose up --build` (this deletes data) |
| App container keeps restarting | DB not ready or wrong credentials | `docker compose logs app` and `docker compose logs db` |
| Page loads but table says error / blank | App not running or API error | Open the browser DevTools (F12) → Console/Network tab; check `/api/expenses` |
| Postman: `Could not get response` | App is not running / wrong `baseUrl` | Start the app, check the collection variable |
| Postman 415 error | Missing JSON header | Body → raw → **JSON** (Content-Type: application/json) |
| Postman 400 `Request body is missing or invalid JSON` | Bad JSON or date format | Use `YYYY-MM-DD`, numeric `amount`, valid commas/quotes |
| Date rejected: "cannot be in the future" | Rule in `ExpenseRequest` | Use today's date or earlier |
| `mvn test` fails to download libraries | No internet / proxy | Check connection, retry `mvn -U test` |
| Fonts look different | Google Fonts needs internet | Works offline with a fallback system font |

---

## 10. Upload to GitHub

1. Create a free account at https://github.com.
2. On GitHub click **New repository**. Name it `expense-tracker`. Leave "Add README", ".gitignore" and license **unchecked**. Click **Create repository**.
3. In PowerShell:

```powershell
cd C:\path\to\expense-tracker

git config --global user.name "Your Name"
git config --global user.email "you@example.com"

git init
git branch -M main
git add .
git status          # CHECK: .env and target/ must NOT be listed
git commit -m "Initial commit: Expense Tracker REST API"

git remote add origin https://github.com/<your-username>/expense-tracker.git
git push -u origin main
```

4. When asked to sign in, use the browser prompt. If a password is requested, GitHub no longer accepts account passwords: create a **Personal Access Token** (GitHub → Settings → Developer settings → Personal access tokens) and paste it as the password.
5. Refresh the repository page to see your code.

Later changes / Baad ke changes:

```powershell
git add .
git commit -m "Describe your change"
git push
```

Safety check: if you ever committed `.env` by mistake, change those passwords immediately, then remove it with `git rm --cached .env` and commit again.

---

## 11. Security notes

- No passwords are stored in source code. They come from environment variables or the untracked `.env` file.
- `.env` and `target/` are in `.gitignore`.
- The Docker image runs as a non-root user.
- Input is validated on the server (the browser checks are only a convenience).
- Errors never expose stack traces or SQL.
- The frontend uses `textContent` instead of `innerHTML` to avoid XSS.
- `spring.jpa.hibernate.ddl-auto=update` is handy for learning. For production use a migration tool like Flyway and a least-privilege DB user.
- The API has no login. Before putting it on the internet, add authentication (Spring Security) and HTTPS.

---

## 12. Ideas for next steps

- Add pagination for large lists.
- Add a monthly chart.
- Add login with Spring Security and JWT.
- Add Flyway migrations.
