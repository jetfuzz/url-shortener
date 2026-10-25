# URL Shortening Service

[![ci](https://github.com/jetfuzz/url-shortener/actions/workflows/ci.yml/badge.svg)](https://github.com/jetfuzz/url-shortener/actions/workflows/ci.yml) [![codecov](https://codecov.io/github/jetfuzz/url-shortener/graph/badge.svg?token=B8O9JT7RKC)](https://codecov.io/github/jetfuzz/url-shortener)

A URL Shortener API that helps shorten long URLs and tracks short link usage. Built with Java, Spring Boot, PostgreSQL,
and Docker.

🔗 **[Live Demo](https://url-shortener-it6x.onrender.com/swagger-ui/index.html#/)**

## Installation

Requires JDK 25 and Docker

1. Clone the repo:

   ```bash
   git clone https://github.com/jetfuzz/url-shortener.git
   cd url-shortener
   ```

2. Create a `.env` file in the project root:

   ```properties
   DB_USERNAME=db_username
   DB_PASSWORD=db_password
   ```

3. Start the database and app:

   ```bash
   docker compose up -d db
   ./mvnw spring-boot:run
   ```

Runs on `http://localhost:8080` with Swagger UI at `/swagger-ui/index.html`

## API Endpoints

- **POST** `/shorten` - Create a short URL
- **GET** `/shorten/{code}` - Returns short URL details
- **PUT** `/shorten/{code}` - Updates target URL
- **DELETE** `/shorten/{code}` - Deletes a short URL
- **GET** `/{code}` - Redirects to original URL
- **GET** `/shorten/{code}/stats` - Returns details with access count

## Running Tests

With Docker running:

```bash
./mvnw test
```