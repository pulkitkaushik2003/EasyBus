# Maven and Maven Daemon Setup Plan

## Steps to Complete:

1. [x] Maven is already installed and working via Maven Wrapper (mvnw.cmd)
   - Maven 3.9.11
   - Java 22.0.1
   - Working directory: C:\Users\Welcome\.m2\wrapper\dists\apache-maven-3.9.11\d6d3cbd4012d4c1d840e93277aca316c

2. [ ] Download Maven Daemon (mvnd)
3. [ ] Extract and set up mvnd
4. [ ] Verify mvnd installation
5. [ ] Test mvnd with the Spring Boot project

## Detailed Instructions:

### 1. Maven Status (COMPLETE)
- Maven Wrapper is functional: `mvnw.cmd --version` works
- Java version: 22.0.1 (Oracle Corporation)
- Ready for mvnd setup

### 4. Install Maven Daemon
- Download from: https://github.com/apache/maven-mvnd/releases
- Choose the Windows version (maven-mvnd-*-windows-amd64.zip)

### 5. Set up mvnd
- Extract to a directory
- Add mvnd bin directory to PATH

### 6. Verify mvnd
- Run: `mvnd --version`

### 7. Test with Project
- Run: `mvnd clean install`
- Run: `mvnd spring-boot:run`
