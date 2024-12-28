# Simple Java HTTP Server

A basic HTTP/1.1 web server implementation built in Java, inspired by [Coding Challenges - Web Server](https://codingchallenges.fyi/challenges/challenge-webserver). This project implements a subset of the HTTP/1.1 protocol, focusing on handling GET and POST requests.

## Features

- HTTP/1.1 protocol subset implementation
- Support for GET requests to serve static files
- Support for POST requests to upload files
- Request parsing inspired by [rawhttp](https://github.com/renatoathaydes/rawhttp)

## Requirements

- Java 17 or higher
- Maven

## Installation

1. Clone the repository
2. Build the project using Maven:
```bash
mvn clean install
```

## Usage

1. Start the server:
```bash
java -jar target/http-server-1.0.jar
```

2. The server will start listening on port 80

### Making Requests

#### GET Request
- Serves files from the `www` directory
- Requires valid Host header
```bash
curl -H "Host: localhost" http://localhost/index.html
```

#### POST Request
- Uploads files to the `user-uploads` directory
- Supports application/json and text/html content types
- Requires valid Host header
```bash
curl -X POST -H "Host: localhost" \
     -H "Content-Type: application/json" \
     --data '{"key": "value"}' \
     http://localhost/example.json
```

## Implementation Details

- **Request Parsing**: The server parses HTTP requests based on the HTTP/1.1 specification
- **Host Validation**: All requests must include a valid Host header (either "localhost" or "127.0.0.1")
- **Content Type Validation**: POST requests are validated for supported content types
- **File Handling**:
    - GET requests serve files from the `www` directory
    - POST requests save files to the `user-uploads` directory
- **Threading**: Each client connection is handled in a separate thread

## Limitations

- Only supports GET and POST methods
- Limited content type support (application/json and text/html)
- Basic content type validation (doesn't verify actual content format)
- No support for query parameters
- No support for request headers besides Host and Content-Type