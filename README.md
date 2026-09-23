# GitCognito 🚀

GitCognito is an intelligent, full-stack application that connects to GitHub repositories, indexes codebase files asynchronously, generates vector embeddings, and provides AI-powered code analysis and chat capabilities.

---

## 🏗️ System Architecture & Indexing Flow

The core backend handles repository indexing via a robust asynchronous pipeline designed to process files efficiently without blocking the user interface.
### Key Workflow Phases:
1. **Trigger Layer:** Users initiate indexing from the frontend dashboard via a `POST /api/repos/{id}/index` request.
2. **Synchronous Validation:** The `RepoController` validates repository ownership, prevents duplicate concurrent indexing jobs, and updates the database status to `INDEXING`.
3. **Asynchronous Execution (`doIndex`):** 
   - Clears existing vector embeddings for the repository.
   - Fetches the repository tree using the GitHub API (`GitHubApiClient`).
   - Filters out unwanted files (such as `node_modules`, lock files, binaries, and oversized files) using `CodeFileFilter`.
4. **Chunking & Vector Storage Loop:** 
   - Files are fetched individually, parsed, and split into code chunks via `CodeChunker`.
   - Chunks are batched (batches of 32) and sent to the OpenAI Embeddings API.
   - Embeddings are securely stored in PostgreSQL using the `pgvector` extension.
5. **Real-time Progress Tracking:** The frontend polls `GET /api/repos/{id}/status` every 3–5 seconds to display live progress bars until the status turns `READY`.

---

## 🛠️ Tech Stack

* **Backend:** Java, Spring Boot, Spring Security, Spring Data JPA, PostgreSQL (`pgvector`)
* **Frontend:** Next.js, React, Tailwind CSS
* **Infrastructure & Tools:** Docker, Docker Compose, Maven, GitHub API, OpenAI API

---

## ⚙️ Getting Started & Local Setup

### Prerequisites
* Java Development Kit (JDK 17+)
* Node.js & npm
* Docker and Docker Compose (for PostgreSQL with `pgvector`)

