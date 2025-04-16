# Sheet-Cell (Shtisel) 📊

Sheet-Cell (nicknamed "Shtisel") is a modular spreadsheet engine developed as part of the **Java Application Development** course.  
The project received a **final grade of 100** and showcases a full-stack implementation of a spreadsheet system — built from the ground up in Java 21.

The system was implemented across three progressive phases:

- Console-based CLI
- JavaFX Desktop GUI
- Client-Server architecture with multi-user support

> Final runnable artifacts (.jar + run.bat) for each phase are available under `/releases/`


---

## 🧩 Features

### 🟩 Core Spreadsheet Functionality
- Support for numeric, boolean, and string values
- Cell formulas: math (`+`, `-`, `*`, `/`), logic (`AND`, `OR`, `IF`), string (`CONCAT`, `SUB`)
- Cell referencing and nested expressions
- Named ranges (A1..B3) and range-aware functions (e.g. `SUM`, `AVERAGE`)
- XML-based sheet loading with schema + business rule validation

### 🔁 Dependency & Recalculation Engine
- Tracks dependencies between cells
- Recalculates affected cells on every update (memoization & invalidation)
- Detects invalid references, circular dependencies, and incorrect function usage

### 🕘 Version Control
- Version number increases on each cell update
- Each cell tracks the version it was last modified
- Users can view the sheet in previous versions (without overwriting current state)
- Visual notifications prompt users when viewing an outdated version

### 🎨 JavaFX Graphical Interface
- Editable grid-based UI with scroll and zoom support
- Cell highlighting for dependencies (influencers/targets)
- Real-time update of formula bar
- Support for custom themes (Light, Dark, Twilight)

### 🧮 Data Tools
- Sorting: by one or multiple columns
- Filtering: by value (single or multi-column)
- What-if analysis with dynamic sliders and popup previews
- Analysis tools allow exploring how changes propagate across the sheet without affecting actual state

### 🌐 Client-Server Architecture
- Multi-user login system with individual user sessions
- Upload and manage XML-based sheets on the server
- Per-user permissions (View / Edit) with approval workflow
- Real-time synchronization of changes across all connected clients
- RESTful communication using JSON over HTTP
- Server-driven architecture handles sheet state, users, and access control centrally

---

## 🎁 Bonus Features

These features were implemented beyond the core project requirements:

- 🖼 **Custom Themes**  
  Light, Dark, and Twilight modes available in JavaFX UI, applied dynamically using CSS.

- 📊 **Graph Visualization**  
  Automatic generation of bar/line charts based on selected numeric ranges in the GUI.

- 🧠 **What-If Analysis**  
  Allows users to change inputs and preview results without committing the changes to the engine state.

- 🧾 **Multi-Column Filtering**  
  Supports filtering by multiple criteria simultaneously, with type-specific conditions (e.g. numeric >, string contains).

- 💾 **Save & Load Engine State**  
  Serialize the entire spreadsheet state to disk and reload it later, preserving history and dependencies.

- 🎯 **Dynamic Cell Highlighting**  
  When editing a formula, highlights referenced cells and visually traces dependencies.

---

## 📁 Project Structure

```pgsql
Engine/         → Core logic: cells, sheet, expressions, XML loader  
UI/             → Console + Desktop UI (JavaFX)  
client-ui/      → JavaFX client for client-server  
server/         → Tomcat backend with servlets  
releases/       → Final runnable builds (.jar + .bat)  
XML-samples/    → Sample XML sheets for testing  
```

---

## 🚀 How to Run

### Console Mode

```bash
cd releases/console
run.bat
```

### Desktop Mode (JavaFX)

```bash
cd releases/desktop
run.bat
```

### Client-Server Mode

Start server:

```bash
cd releases/Client-Server
java -jar webapp.war
```

Then start client:

```bash
cd releases/Client-Server
run.bat
```

---

## 🛠 Technical Highlights

- Clean separation of engine & UI via interfaces
- Engine is reused across all phases (console, GUI, server)
- DTOs used instead of raw strings
- Custom exceptions for XML validation & user feedback
- Immutable core data structures
- Expression parser supports tree-based evaluation

---

## 🧪 Tech Stack

- **Java 21** – Core language for all modules
- **JavaFX & FXML** – GUI framework for desktop and client apps
- **Apache Tomcat** – Server backend (servlet container)
- **HTTP & JSON** – Communication protocol between client and server
- **JAXB** – XML parsing and validation
- **Gson** – JSON serialization/deserialization
- **OkHttp** – HTTP client used by the JavaFX client


