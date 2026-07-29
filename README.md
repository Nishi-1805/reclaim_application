# Reclaim – Intelligent Lost & Found Matching & Verification Platform

## Overview

Reclaim is a full-stack web application that helps users report lost and found items while reducing fraudulent ownership claims through an intelligent verification workflow.

Unlike conventional Lost & Found systems that rely only on manual browsing, Reclaim combines automated item matching with ownership verification questions to improve the chances of returning items to their rightful owners.

The platform provides separate functionalities for users and administrators while maintaining a secure authentication system using JWT.

---

# Features

### Authentication

* User Registration
* User Login
* JWT Authentication
* Role-Based Authorization
* Secure Logout

---

### Item Management

* Report Lost Item
* Report Found Item
* Upload Multiple Images
* Cloudinary Image Storage
* Update Item Details
* Delete Item
* View Personal Posts
* Browse Public Items

---

### Intelligent Matching

* Automatic Lost–Found Matching
* Match Score Generation
* View Suggested Matches

---

### Claim Verification

* Raise Ownership Claim
* Ownership Verification Questions
* Verification Score Calculation
* Claim Approval/Rejection Workflow

---

### Notifications

* Match Notifications
* Claim Status Updates
* Item Updates

---

### User Dashboard

* Recent Matches
* Notifications
* Statistics
* Quick Actions

---

### Administrator Module

* User Management
* Report Management
* Platform Monitoring

---

# Technology Stack

## Backend

* Java 21
* Spring Boot
* Spring Security
* JWT Authentication
* Spring Data JPA
* Hibernate
* Maven

## Frontend

* React
* React Router
* Material UI (MUI)
* Axios
* Context API

## Database

* MySQL

## Cloud Storage

* Cloudinary

## Tools

* Git
* GitHub
* Swagger
* VS Code
* Spring Tool Suite (STS)

---

# System Architecture

```
React Frontend
       │
Axios REST APIs
       │
Spring Boot Backend
       │
Spring Security + JWT
       │
Business Services
       │
Spring Data JPA
       │
MySQL Database

Cloudinary
(Image Storage)
```

---

# Project Structure

## Backend

```
src
 ├── config
 ├── constants
 ├── controller
 ├── dto
 ├── entity
 ├── enums
 ├── exception
 ├── repository
 ├── security
 ├── service
 ├── util
```

## Frontend

```
src
 ├── assets
 ├── components
 ├── constants
 ├── context
 ├── hooks
 ├── layouts
 ├── pages
 ├── routes
 ├── services
 ├── theme
 ├── utils
```

---

# Installation

## Backend

```bash
git clone <repository-url>

cd reclaim_application

mvn clean install

mvn spring-boot:run
```

Backend runs on:

```
http://localhost:8080
```

---

## Frontend

```bash
cd reclaim-frontend

npm install

npm run dev
```

Frontend runs on:

```
http://localhost:5173
```

---

# Configuration

Sensitive configuration files are intentionally excluded from version control.

Create the following files before running the project:

Backend

```
application.properties
```

Frontend

```
.env
```

Configure:

* Database
* JWT Secret
* Cloudinary Credentials
* API Base URL

---

# Authentication

All secured APIs require a JWT token.

Example Header

```
Authorization: Bearer <JWT_TOKEN>
```

---

# Screenshots

<img width="1264" height="832" alt="Screenshot 2026-07-30 003351" src="https://github.com/user-attachments/assets/0ce43368-2cf6-4a39-a82b-15fe07ce2ced" />
<img width="1920" height="1080" alt="Screenshot 2026-07-30 003528" src="https://github.com/user-attachments/assets/a9316fd6-0e93-4e81-90bd-4f80ef343cad" />
<img width="1920" height="1080" alt="Screenshot 2026-07-30 003551" src="https://github.com/user-attachments/assets/d8bc2392-8808-4da3-bf64-f16c535874ef" />
<img width="1920" height="1080" alt="Screenshot 2026-07-30 003606" src="https://github.com/user-attachments/assets/feb20141-6d92-44f1-b0e8-857f4aff7bd8" />






---

# Future Enhancements

* Email Notifications
* AI-based Image Matching
* Mobile Application
* OCR-based Item Recognition
* Location-Based Search
* Google Maps Integration

---

# Contributors

**Nishi Mishra**

CDAC PG-DAC Major Project

---

# License

This project is developed for academic and educational purposes.
