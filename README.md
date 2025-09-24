# BDD Test Runner

Desktop application untuk mengelola dan menjalankan BDD (Behavior Driven Development) test scenarios menggunakan Cucumber dan Selenium.

## Fitur Utama

- **Manajemen Scenario**: Create, Edit, Delete scenario BDD
- **Dynamic Step Management**: Tambah/hapus test steps secara dinamis
- **Test Execution**: Jalankan scenario menggunakan Cucumber + Selenium
- **Database Integration**: Penyimpanan persistent dengan MySQL
- **Real-time Logging**: Live log execution dan screenshot capture
- **Report Generation**: View hasil eksekusi dan laporan

## Requirements

### Software Prerequisites
- **Java 17** atau lebih tinggi
- **MySQL 8.0** atau lebih tinggi
- **Maven 3.6** atau lebih tinggi
- **Chrome Browser** (untuk Selenium WebDriver)

### Database Setup
1. Install dan jalankan MySQL server
2. Buat database (opsional, aplikasi akan create otomatis):
```sql
CREATE DATABASE bdd_test_runner;
```

## Setup dan Instalasi

### 1. Clone atau Download Project
```bash
cd C:\Users\User\Fadhli_DIR\BelajarJava\auto-test-runner
```

### 2. Konfigurasi Database
Edit file `src/main/resources/database.properties`:
```properties
# Database Connection Settings
db.url=jdbc:mysql://localhost:3306/bdd_test_runner?useSSL=false&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true&serverTimezone=UTC
db.username=root
db.password=your_mysql_password
db.driver=com.mysql.cj.jdbc.Driver
```

### 3. Build Project
```bash
mvn clean compile
```

### 4. Jalankan Aplikasi
```bash
mvn exec:java -Dexec.mainClass="com.fadhli.automation.BddTestRunnerApplication"
```

Atau build JAR dan jalankan:
```bash
mvn clean package
java -jar target/bdd-test-runner-1.0.0.jar
```

## Struktur Project

```
src/main/java/com/fadhli/automation/
├── BddTestRunnerApplication.java    # Main application entry point
├── model/                           # Entity models
│   ├── Scenario.java
│   ├── Step.java
│   └── ExecutionResult.java
├── dao/                            # Data Access Layer
│   ├── BaseDao.java
│   ├── DaoException.java
│   └── ScenarioDao.java
├── service/                        # Business Logic Layer
├── ui/                            # Swing UI Components
│   └── MainWindow.java
├── util/                          # Utility Classes
│   └── DatabaseUtil.java
└── executor/                      # Test Execution Engine
```

## Cara Penggunaan

### 1. Menjalankan Aplikasi
- Pastikan MySQL server sudah running
- Jalankan aplikasi dengan perintah di atas
- Aplikasi akan otomatis membuat database schema

### 2. Manajemen Scenario
- **Create New**: Klik tombol "Create New" untuk membuat scenario baru
- **Edit**: Pilih scenario dan klik "Edit" atau double-click pada row
- **Delete**: Pilih scenario dan klik "Delete"
- **Refresh**: Klik "Refresh" atau tekan F5 untuk reload data

### 3. Menjalankan Test
- Pilih scenario yang ingin dijalankan
- Klik tombol "Run Test"
- Monitor progress di execution console
- View hasil di log files dan screenshot

### 4. Database Status
- Menu Tools > Database Status untuk cek koneksi database
- View connection pool statistics
- Monitor database health

## File Konfigurasi

### database.properties
Konfigurasi koneksi database dan connection pool settings.

### logback.xml
Konfigurasi logging dengan berbagai appender:
- Console logging untuk development
- File logging untuk production
- Separate log untuk test execution

## Troubleshooting

### Database Connection Issues
1. Pastikan MySQL server running
2. Check username/password di database.properties
3. Pastikan database accessible dari aplikasi
4. Check firewall settings

### Application Startup Issues
1. Pastikan Java 17 installed dan di PATH
2. Check MySQL connector dependency
3. View logs di console atau log files

### Test Execution Issues
1. Pastikan Chrome browser installed
2. Check ChromeDriver compatibility
3. Monitor log files untuk error details

## Development

### Build Commands
```bash
# Compile only
mvn compile

# Run tests
mvn test

# Package JAR
mvn package

# Clean build
mvn clean package
```

### Code Style
Project mengikuti:
- Google Java Style Guide
- Clean Code principles
- Consistent naming conventions
- Repository pattern untuk data access
- Service layer untuk business logic

## Future Enhancements

- [ ] Scenario form dialog implementation
- [ ] Cucumber test execution engine
- [ ] Parallel test execution
- [ ] Multi-feature support
- [ ] PDF/HTML report export
- [ ] Git integration untuk scenario versioning

## License

This project is for educational and development purposes.

## Contact

Untuk pertanyaan atau issue, silahkan buat GitHub issue atau contact developer.