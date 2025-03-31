# Patient Register Application

This is a JavaFX-based desktop application designed to manage patients, their medical examinations, and EKG data. The application integrates with multiple data sources including:

- **MongoDB** for storing patient and examination records
- **eXist-db** for managing EKG data in XML format
- **LDAP** (e.g., OpenLDAP) for user authentication and role-based access control

## Features

- Patient record management (view, edit, search)
- Display of examination details
- Visualization of EKG signals
- User authentication with LDAP
- Role-based access: doctors and administrators
- Password change functionality

## Technologies

- **Java 17**
- **JavaFX** (UI)
- **MongoDB Java Driver**
- **eXist-db XMLDB API**
- **Apache Directory LDAP API**
- **PlantUML**, **Mermaid**, and **draw.io** were used to generate diagrams

## Project Structure

src/ 
├── auth/ # LDAP connector
├── model/ # Data models (Patient, Examination, EkgData, etc.) 
├── service/ # Business logic (AuthService, PatientService, etc.) 
├── db/ # Database connectors (MongoDB, eXist-db) 
└── ui/ # JavaFX windows and forms


## Getting Started

To run the application:

1. Set up a local MongoDB instance
2. Set up eXist-db and ensure the EKG collection exists
3. Configure your LDAP server (e.g., OpenLDAP)
4. Update connection strings in `PatientRegisterApplication.java`
5. Run the application using JavaFX

## Author

This application was developed as part of a university project focused on integrating multiple data sources in a medical context.

---

*Feel free to modify this README to suit the final state of your project.*
