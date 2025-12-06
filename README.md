DevBadge – GitHub Activity Scoring System

DevBadge is a full-stack project that analyzes a GitHub user’s public activity and generates a set of scores based on commits, pull requests, issues, repository structure, and overall contribution patterns.
The goal is to provide a simple and objective indicator of a developer’s consistency, impact, and code activity.

This project includes:

A Spring Boot backend that integrates with the GitHub REST API

A scoring engine that processes GitHub data and produces several metrics

A caching layer to reduce unnecessary GitHub API calls

A history system that stores past scores

A React (Vite) frontend for searching users and visualizing results

Features
1. GitHub User Analysis

The system retrieves public data from GitHub, including:

Profile information

Public repositories

Commits (with pagination)

Issues

Pull requests

2. Scoring Categories

Each user receives scores in several areas, including:

Commit quality

Consistency

Code review and collaboration activity

Overall contribution impact

The scoring service aggregates all collected data and produces an overall score.

3. Caching Layer

API responses are cached in a database table with expiration times.
This reduces repeated requests to GitHub and helps avoid rate-limit issues.

4. Score History Tracking

Each time a score changes, a new history entry is created.
This allows users to track how their activity evolves over time.

5. Frontend Interface

A simple React interface is provided where users can:

Enter a GitHub username

View the calculated scores

See score history in table format

Technologies Used
Backend

Java 17

Spring Boot 3

Spring Data JPA

MySQL

Maven

Lombok

GitHub REST API

Hibernate

Frontend

React

Vite

JavaScript

Axios

CSS modules

Project Structure
Backend
src/main/java/com/devbadge/devbadge/
│
├── config/               // CORS and Security configuration
├── controller/           // REST controllers
├── dto/                  // GitHub API DTOs and response models
├── entity/               // JPA entities (User, Score, History, Cache)
├── repository/           // Spring Data repositories
├── service/              // Scoring logic, caching, API integration
└── DevBadgeApplication   // Main application entry point

Frontend
devbadge-frontend/
│
├── src/
│   ├── components/       // UI components
│   ├── pages/            // Main pages
│   ├── api/              // Axios request helpers
│   ├── App.jsx
│   └── main.jsx
└── package.json

How to Run the Project
1. Backend

Create a MySQL database:

CREATE DATABASE devbadge;


Configure your application.properties:

spring.datasource.url=jdbc:mysql://localhost:3306/devbadge?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=
github.token=${GITHUB_TOKEN}


Set your GitHub token in your environment:

export GITHUB_TOKEN=your_token_here


Run:

mvn spring-boot:run


Backend runs at:
http://localhost:8080

2. Frontend

From inside the devbadge-frontend directory:

npm install
npm run dev


Frontend runs at:
http://localhost:5173

API Endpoints
Calculate scores for a user
GET /api/scores/{username}

Get history for a user
GET /api/scores/{username}/history

Notes and Future Improvements

Additional commit quality metrics could be added (file changes, message analysis).

More advanced contribution impact scoring can be implemented.

Tests will be added to improve stability.

A deployment configuration (Docker and CI) will be added later.


